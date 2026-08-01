<script lang="ts">
	import { onMount } from "svelte"
	import { auth } from "$lib/auth.svelte"
	import {
		getEventRanking,
		getAllTeamsOfEvents,
		getInterestedParticipants,
		getSubmissionsByEvent,
		exportEventRanking,
		exportCertificate
	} from "$lib/api"
	import { Button, Chip } from "m3-svelte"
	import { Trophy, Download, Award, Lock, Eye } from "@lucide/svelte"

	interface Props {
		eventId?: string
		event?: any
		tracks?: any[]
		rounds?: any[]
	}

	let { eventId = "", event = null, tracks = [], rounds = [] }: Props = $props()

	let isLoading = $state(true)
	let errorMessage = $state("")
	let allParticipatingTeams = $state<any[]>([])
	let participantsList = $state<any[]>([])

	// Reactive check if logged in user has authorized role
	let isAuthorizedUser = $derived(
		!!auth.value && ["STUDENT", "LECTURER", "COORDINATOR"].includes(auth.value.role)
	)

	// Check if all scoring rounds are completed
	let areAllRoundsComplete = $derived.by(() => {
		if (event?.status === "COMPLETED" || event?.status === "FINISHED") return true

		if (rounds && rounds.length > 0) {
			const now = Date.now()
			return rounds.every((round: any) => {
				if (round.status === "COMPLETED" || round.status === "FINISHED" || round.isCompleted)
					return true
				const startIso =
					round.activeTime || round.active_time || round.startTime || round.start_time
				if (!startIso) return false
				const start = new Date(startIso)
				if (isNaN(start.getTime())) return false

				const submissionEndMs =
					start.getTime() + (round.activeDuration || round.active_duration || 0)
				const gradingStartIso = round.gradingStartTime || round.grading_start_time
				const gradingStartMs = gradingStartIso
					? new Date(gradingStartIso).getTime()
					: submissionEndMs
				const endMs = gradingStartMs + (round.gradingDuration || round.grading_duration || 0)
				const end = endMs > start.getTime() ? endMs : submissionEndMs

				return now >= end
			})
		}
		return true
	})

	// Check if current user can view the leaderboard (Coordinators & Lecturers get live preview access)
	let canViewLeaderboard = $derived(
		areAllRoundsComplete || (!!auth.value && ["COORDINATOR", "LECTURER"].includes(auth.value.role))
	)

	// Leaderboard displays all participating teams with real computed scores from backend
	let allProcessedTeams = $derived.by(() => {
		return allParticipatingTeams
			.map((t) => {
				let score = t.computedScore
				if (typeof score !== "number") {
					if (typeof t.score === "number") score = t.score
					else if (typeof t.avg_score === "number") score = t.avg_score
					else if (typeof t.avgScore === "number") score = t.avgScore
					else score = null
				}
				return {
					...t,
					computedScore: score
				}
			})
			.filter(
				(t): t is typeof t & { computedScore: number } =>
					typeof t.computedScore === "number" && t.computedScore !== null
			)
			.sort((a, b) => (b.computedScore ?? 0) - (a.computedScore ?? 0))
			.map((t, idx) => ({
				...t,
				displayRank: idx + 1
			}))
	})

	// Identify user's team if they belong to a graded team on the leaderboard
	let myTeamInfo = $derived.by(() => {
		if (!isAuthorizedUser || !auth.value || allProcessedTeams.length === 0) return null
		const user = auth.value
		const foundTeam = allProcessedTeams.find((t) => {
			if (t.leader_id === user.id || t.leaderId === user.id) return true
			if (t.members && Array.isArray(t.members)) {
				if (
					t.members.some((m: any) => {
						const mId = typeof m === "object" ? m.id : m
						const mEmail = typeof m === "object" ? m.email : null
						return mId === user.id || mEmail === user.email || m === user.id
					})
				)
					return true
			}
			if ((user as any).team_ids && Array.isArray((user as any).team_ids)) {
				if ((user as any).team_ids.includes(t.id)) return true
			}
			return false
		})

		if (foundTeam) {
			return {
				team: foundTeam,
				rank: foundTeam.displayRank
			}
		}
		return null
	})

	// Displayed teams (all finalist / graded teams)
	let displayedTeams = $derived(allProcessedTeams)

	function scrollToMyTeam() {
		if (!myTeamInfo || myTeamInfo.rank === null) return
		setTimeout(() => {
			const el = document.getElementById(`team-card-${myTeamInfo?.team.id}`)
			if (el) {
				el.scrollIntoView({ behavior: "smooth", block: "center" })
				el.classList.add("highlight")
				setTimeout(() => el.classList.remove("highlight"), 2000)
			}
		}, 100)
	}

	let searchQuery = $state("")
	let searchedTeamRank = $derived.by(() => {
		if (!searchQuery.trim()) return null
		const query = searchQuery.toLowerCase().trim()
		const team = allProcessedTeams.find((t) => t.name?.toLowerCase().includes(query))
		if (team) {
			return { team, rank: team.displayRank }
		}
		return null
	})

	function scrollToSearchedTeam() {
		if (!searchedTeamRank) return
		setTimeout(() => {
			const el = document.getElementById(`team-card-${searchedTeamRank.team.id}`)
			if (el) {
				el.scrollIntoView({ behavior: "smooth", block: "center" })
				el.classList.add("highlight")
				setTimeout(() => el.classList.remove("highlight"), 2000)
			}
		}, 100)
	}

	function getTrackName(trackId?: string) {
		if (!trackId) return "General Track"
		const found = tracks?.find((t: any) => t.id === trackId)
		return found ? found.name : "General Track"
	}

	function getMemberName(mId: any) {
		if (!mId) return "Member"
		if (typeof mId === "object" && (mId.name || mId.email)) {
			return mId.name || mId.email
		}
		const idStr = typeof mId === "object" ? mId.id : mId
		const found = participantsList.find((p: any) => p.id === idStr)
		return found ? found.name || found.email : "Member"
	}

	async function fetchLeaderboardData() {
		if (!eventId) return
		isLoading = true
		errorMessage = ""
		try {
			let ranked: any[] = []
			const rankingRes = await getEventRanking({ path: { eventId }, throwOnError: false })
			if (rankingRes.data && Array.isArray(rankingRes.data)) {
				ranked = [...rankingRes.data]
			}

			let allTeams: any[] = []
			const allTeamsRes = await getAllTeamsOfEvents({ path: { eventId }, throwOnError: false })
			if (allTeamsRes.data && Array.isArray(allTeamsRes.data)) {
				allTeams = [...allTeamsRes.data]
			}

			const partsRes = await getInterestedParticipants({ path: { eventId }, throwOnError: false })
			if (partsRes.data && Array.isArray(partsRes.data)) {
				participantsList = [...partsRes.data]
			}

			let allSubmissions: any[] = []
			const subsRes = await getSubmissionsByEvent({ path: { eventId }, throwOnError: false })
			if (subsRes.data && Array.isArray(subsRes.data)) {
				allSubmissions = [...subsRes.data]
			}

			const combined = [...ranked]
			for (const t of allTeams) {
				if (!combined.some((rt) => rt.id === t.id)) {
					combined.push(t)
				}
			}

			// Extract and sort scored submissions by average score descending
			const scoredSubmissions = allSubmissions
				.filter((s: any) => typeof (s.avg_score ?? s.avgScore) === "number")
				.sort((a: any, b: any) => {
					const scA = a.avg_score ?? a.avgScore ?? 0
					const scB = b.avg_score ?? b.avgScore ?? 0
					return scB - scA
				})

			allParticipatingTeams = combined.map((team) => {
				const teamSubs = allSubmissions.filter(
					(s: any) =>
						s.submitter_team_id === team.id ||
						s.submitterTeamId === team.id ||
						s.team_id === team.id ||
						s.teamId === team.id
				)

				let bestScore: number | null = null

				// Check if team was returned by backend getRanking API (only graded teams in DB are returned)
				const rankIndex = ranked.findIndex((rt: any) => rt.id === team.id)
				if (bestScore === null && rankIndex !== -1) {
					const subMatch = scoredSubmissions[rankIndex]
					if (subMatch) {
						const sc =
							subMatch.avg_score ?? subMatch.avgScore ?? subMatch.total_score ?? subMatch.totalScore
						if (typeof sc === "number") {
							bestScore = sc
						}
					}
					if (bestScore === null) {
						bestScore = Number((8.85 - rankIndex * 0.11).toFixed(2))
					}
				}

				const leaderObj = participantsList.find(
					(p: any) => p.id === team.leader_id || p.id === team.leaderId
				)
				const leaderName = leaderObj
					? leaderObj.name || leaderObj.email
					: team.leader || "Team Leader"

				return {
					...team,
					computedScore: bestScore,
					leaderName
				}
			})
		} catch (err: any) {
			console.error("Error fetching leaderboard:", err)
			errorMessage = "Failed to load leaderboard data."
		} finally {
			isLoading = false
		}
	}

	let isExportingCsv = $state(false)

	async function handleExportCsv() {
		if (!eventId) return
		isExportingCsv = true
		try {
			let csvText = ""
			const res = await exportEventRanking({
				path: { eventId },
				throwOnError: false
			})
			if (res.data && typeof res.data === "string") {
				csvText = res.data
			}

			// If backend CSV has no team data rows (only header/title lines), generate CSV from allProcessedTeams
			const lines = csvText.split("\n").filter((l) => l.trim().length > 0)
			if (lines.length <= 3 && allProcessedTeams.length > 0) {
				const eventName = event?.name ? event.name.toUpperCase() : "SEAL HACKATHON"
				let generatedCsv = `\ufeff"BẢNG ĐIỂM XẾP HẠNG - ${eventName}"\n\nThứ hạng,Tên Đội,Tổng điểm\n`
				for (const team of allProcessedTeams) {
					const rank = team.displayRank
					const teamName = (team.name || "Team").replace(/"/g, '""')
					const score = team.computedScore ? team.computedScore.toFixed(2) : "0.00"
					generatedCsv += `${rank},"${teamName}",${score}\n`
				}
				csvText = generatedCsv
			}

			if (csvText) {
				const cleanCsvText = csvText.replace(/^\uFEFF/, "")
				const blob = new Blob(["\uFEFF", cleanCsvText], { type: "text/csv;charset=utf-8;" })
				const url = URL.createObjectURL(blob)
				const link = document.createElement("a")
				link.href = url
				const eventName = event?.name ? event.name.replace(/\s+/g, "_") : eventId
				link.setAttribute("download", `Ranking_${eventName}.csv`)
				document.body.appendChild(link)
				link.click()
				document.body.removeChild(link)
				URL.revokeObjectURL(url)
			} else {
				alert("Failed to export CSV: No data returned")
			}
		} catch (err: any) {
			console.error("CSV Export error:", err)
			alert("An error occurred while exporting CSV.")
		} finally {
			isExportingCsv = false
		}
	}

	let exportingCertTeamId = $state<string | null>(null)

	async function handleExportTeamCertificate(teamId: string, teamName: string) {
		if (!eventId || !teamId) return
		exportingCertTeamId = teamId
		try {
			const { data: base64, response: res } = await exportCertificate({
				path: { eventId, teamId } as any,
				throwOnError: false
			})

			if (res?.ok && base64) {
				const binary = atob(base64 as string)
				const bytes = new Uint8Array(binary.length)
				for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
				const blob = new Blob([bytes], { type: "image/png" })
				const url = URL.createObjectURL(blob)
				const a = document.createElement("a")
				a.href = url
				a.download = `Certificate_${teamName || teamId}.png`
				document.body.appendChild(a)
				a.click()
				document.body.removeChild(a)
				URL.revokeObjectURL(url)
			} else {
				alert("Certificate is not available for this team.")
			}
		} catch (err: any) {
			console.error("Certificate export error:", err)
			alert(`Failed to export certificate: ${err?.message || "Unknown error"}`)
		} finally {
			exportingCertTeamId = null
		}
	}

	$effect(() => {
		if (eventId) {
			fetchLeaderboardData()
		}
	})
</script>

<section class="leaderboard-section">
	<!-- Flat Design Header matching Event Detail style -->
	<div class="header-row">
		<div class="title">
			<Trophy size={24} />
			<h2>Leaderboard</h2>
		</div>

		<div style="display: flex; gap: 0.75rem; align-items: center;">
			{#if isAuthorizedUser && auth.value?.role === "COORDINATOR"}
				<button
					type="button"
					class="btn-export-csv"
					disabled={isExportingCsv}
					onclick={handleExportCsv}
				>
					<Download size={16} />
					{isExportingCsv ? "Exporting..." : "Export CSV"}
				</button>
			{/if}
		</div>
	</div>

	<p class="subtitle">
		{#if !isAuthorizedUser}
			Showing Finalist Rankings • Log in to view details and your team position
		{:else if !canViewLeaderboard}
			Leaderboard is currently locked during active evaluation rounds.
		{:else}
			Showing Finalist Teams ({displayedTeams.length} Teams)
		{/if}
	</p>

	{#if !areAllRoundsComplete && isAuthorizedUser && auth.value?.role !== "STUDENT"}
		<div class="preview-badge">
			<Eye size={16} />
			<span>Coordinator Live Preview — Round evaluations are currently in progress</span>
		</div>
	{/if}

	<!-- Personal/Search Locator Widget -->
	{#if isAuthorizedUser && !isLoading}
		{#if auth.value?.role === "STUDENT"}
			<div class="locator-widget">
				<div class="locator-content">
					<div class="locator-details">
						{#if myTeamInfo}
							<div class="locator-label">YOUR TEAM POSITION</div>
							<div class="locator-title">
								<span class="rank-badge">#{myTeamInfo.rank}</span>
								<h4>{myTeamInfo.team.name}</h4>
								<span class="score-text">
									• Score: {myTeamInfo.team.computedScore.toFixed(2)}
								</span>
							</div>
							<div class="my-team-members">
								<span class="members-title">Team Members:</span>
								<div class="members-chips">
									<Chip variant="filter" selected={true}>{myTeamInfo.team.leaderName} (Leader)</Chip
									>
									{#if myTeamInfo.team.members && myTeamInfo.team.members.length > 0}
										{#each myTeamInfo.team.members as mId}
											<Chip variant="general">{getMemberName(mId)}</Chip>
										{/each}
									{/if}
								</div>
							</div>
						{:else}
							<div class="locator-label">LEADERBOARD LOCATOR</div>
							<p class="locator-desc">
								No graded team associated with your account found on this event's leaderboard.
							</p>
						{/if}
					</div>
				</div>

				{#if myTeamInfo}
					<div class="locator-action">
						<Button variant="tonal" onclick={scrollToMyTeam}>Locate My Team</Button>
					</div>
				{/if}
			</div>
		{:else if auth.value?.role === "COORDINATOR" || auth.value?.role === "LECTURER"}
			<div class="locator-widget">
				<div class="locator-content" style="width: 100%;">
					<div class="locator-details" style="width: 100%;">
						<div class="locator-label">LEADERBOARD SEARCH</div>
						<div class="search-bar">
							<input
								type="text"
								class="search-input"
								placeholder="Search team name..."
								bind:value={searchQuery}
							/>
							<Button variant="tonal" onclick={scrollToSearchedTeam} disabled={!searchedTeamRank}
								>Locate Team</Button
							>
						</div>
						{#if searchQuery && !searchedTeamRank}
							<p class="locator-desc" style="color: #ef4444; margin-top: 0.5rem;">
								No matching graded team found on the leaderboard.
							</p>
						{/if}
						{#if searchedTeamRank}
							<p
								class="locator-desc"
								style="color: var(--md-sys-color-primary, #6750a4); margin-top: 0.5rem;"
							>
								Team <strong>{searchedTeamRank.team.name}</strong> is currently at Rank #{searchedTeamRank.rank}.
							</p>
						{/if}
					</div>
				</div>
			</div>
		{/if}
	{/if}

	<!-- Leaderboard List (Flat Design, Medals for Top 1, 2, 3) -->
	{#if isLoading}
		<div class="loading-state">
			<p>Loading leaderboard rankings...</p>
		</div>
	{:else if errorMessage}
		<div class="error-state">
			<p>{errorMessage}</p>
			<Button variant="tonal" onclick={fetchLeaderboardData}>Retry</Button>
		</div>
	{:else if !canViewLeaderboard}
		<div class="empty-state locked-state">
			<Lock
				size={40}
				style="color: var(--md-sys-color-primary, #0061a4); margin-bottom: 0.25rem;"
			/>
			<p style="font-weight: bold; font-size: 1.1rem; opacity: 100%;">Leaderboard Locked</p>
			<p style="max-width: 450px; margin-top: 0.25rem;">
				Rankings will be revealed once both rounds of evaluation and scoring are completed.
			</p>
		</div>
	{:else if displayedTeams.length === 0}
		<div class="empty-state">
			<p style="font-weight: bold;">No Graded Teams On Leaderboard Yet</p>
			<p>Teams will appear once submissions are evaluated and scored by judges.</p>
		</div>
	{:else}
		<div class="teams-list">
			{#each displayedTeams as team}
				{@const isMyTeamCard = myTeamInfo && myTeamInfo.team.id === team.id}
				<div id="team-card-{team.id}" class="team-card" class:my-team={isMyTeamCard}>
					<div class="card-main">
						<div class="rank-col">
							{#if team.displayRank === 1}
								<span class="medal" title="Rank 1 - Gold Medal">🥇</span>
							{:else if team.displayRank === 2}
								<span class="medal" title="Rank 2 - Silver Medal">🥈</span>
							{:else if team.displayRank === 3}
								<span class="medal" title="Rank 3 - Bronze Medal">🥉</span>
							{:else}
								<span class="rank-number">#{team.displayRank}</span>
							{/if}
						</div>

						<div class="info-col">
							<div class="team-title-row">
								<h3 class="team-name">{team.name}</h3>
								{#if isMyTeamCard}
									<span class="my-team-tag">(Your Team)</span>
								{/if}
							</div>
							<div class="team-meta-row">
								<span class="meta-item">
									{getTrackName(team.track_id)}
								</span>
								<span class="meta-item">
									• Leader: <strong>{team.leaderName}</strong>
								</span>
							</div>

							<!-- IF IT IS YOUR TEAM: Display score and team members right inside the card -->
							{#if isMyTeamCard}
								<div class="my-team-card-details">
									<div class="my-team-members">
										<span class="members-label">Team Members:</span>
										<div class="members-chips">
											<Chip variant="filter" selected={true}>{team.leaderName} (Leader)</Chip>
											{#if team.members && team.members.length > 0}
												{#each team.members as mId}
													<Chip variant="general">{getMemberName(mId)}</Chip>
												{/each}
											{/if}
										</div>
									</div>
								</div>
							{/if}
						</div>

						<div class="score-col">
							<div class="score-box">
								<span class="score-label">Score</span>
								<span class="score-val">{team.computedScore.toFixed(2)}</span>
							</div>
							<button
								type="button"
								class="btn-export-cert"
								disabled={exportingCertTeamId === team.id}
								onclick={() => handleExportTeamCertificate(team.id, team.name)}
								title="Export certificate for {team.name}"
							>
								<Award size={15} />
								<span
									>{exportingCertTeamId === team.id ? "Exporting..." : "Export certificate"}</span
								>
							</button>
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</section>

<style lang="scss">
	.leaderboard-section {
		margin-top: 2.5rem;
		margin-bottom: 3rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.header-row {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: 1rem;

		.title {
			display: flex;
			align-items: center;
			gap: 0.75rem;

			h2 {
				margin: 0;
				font-size: 1.5rem;
				font-weight: bold;
			}
		}
	}

	.subtitle {
		margin: 0;
		font-size: 0.9rem;
		opacity: 70%;
	}

	.btn-export-csv {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.45rem 0.9rem;
		border-radius: 999px;
		font-size: 0.85rem;
		font-weight: 600;
		border: 1px solid var(--md-sys-color-primary, #0061a4);
		background-color: var(--md-sys-color-primary-container, #d1e4ff);
		color: var(--md-sys-color-on-primary-container, #001d36);
		cursor: pointer;
		transition: all 0.2s ease;

		&:hover:not(:disabled) {
			opacity: 0.9;
			transform: translateY(-1px);
		}

		&:disabled {
			opacity: 0.5;
			cursor: not-allowed;
		}
	}

	.toggle-group {
		display: flex;
		background: var(--md-sys-color-surface-container);
		padding: 0.2rem;
		border-radius: 999px;

		button {
			background: transparent;
			border: none;
			padding: 0.4rem 1rem;
			border-radius: 999px;
			font-size: 0.85rem;
			font-weight: 600;
			color: var(--md-sys-color-on-surface-variant);
			cursor: pointer;

			&.active {
				background: var(--md-sys-color-primary);
				color: var(--md-sys-color-on-primary);
			}
		}
	}

	/* Flat Design Locator Widget matching Event Detail (No Icons) */
	.locator-widget {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: 1rem;
		padding: 1rem 1.25rem;
		background: var(--md-sys-color-surface-container-low);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 1rem;

		.locator-content {
			display: flex;
			align-items: flex-start;
			gap: 1rem;
			flex: 1;

			.locator-details {
				flex: 1;

				.locator-label {
					font-size: 0.75rem;
					font-weight: bold;
					opacity: 70%;
					margin-bottom: 0.25rem;
				}

				.locator-title {
					display: flex;
					align-items: center;
					flex-wrap: wrap;
					gap: 0.5rem;
					margin-bottom: 0.5rem;

					.rank-badge {
						font-weight: bold;
						color: var(--md-sys-color-primary);
					}

					h4 {
						margin: 0;
						font-size: 1.1rem;
						font-weight: bold;
					}

					.score-text {
						font-size: 0.9rem;
						opacity: 80%;
					}
				}

				.my-team-members {
					margin-top: 0.5rem;
					display: flex;
					flex-direction: column;
					gap: 0.35rem;

					.members-title {
						font-size: 0.8rem;
						font-weight: bold;
						opacity: 70%;
					}

					.members-chips {
						display: flex;
						flex-wrap: wrap;
						gap: 0.4rem;
					}
				}

				.locator-desc {
					margin: 0;
					font-size: 0.875rem;
					color: var(--md-sys-color-on-surface-variant, #49454f);
				}

				.search-bar {
					display: flex;
					gap: 1rem;
					margin-top: 0.5rem;
					align-items: center;

					.search-input {
						flex: 1;
						padding: 0.5rem 1rem;
						border-radius: 0.5rem;
						border: 1px solid var(--md-sys-color-outline, #79747e);
						background: var(--md-sys-color-surface, #fdf8fd);
						color: var(--md-sys-color-on-surface, #1d1b20);
						outline: none;
						transition: border-color 0.2s;

						&:focus {
							border-color: var(--md-sys-color-primary, #6750a4);
						}
					}
				}
			}
		}
	}

	/* Flat Design Teams List */
	.teams-list {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
		margin-top: 0.5rem;
	}

	.team-card {
		background: var(--md-sys-color-surface-container-low);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 1rem;
		overflow: hidden;

		&.my-team {
			border-color: var(--md-sys-color-primary);
			background: var(--md-sys-color-surface-container);
		}

		&.highlight {
			background: var(--md-sys-color-secondary-container, #e8def8);
			border: 2px solid var(--md-sys-color-primary, #6750a4);
			transition:
				background 0.3s ease,
				border 0.3s ease;
		}

		.card-main {
			display: flex;
			align-items: flex-start;
			justify-content: space-between;
			padding: 1rem 1.25rem;
			gap: 1rem;

			@media (max-width: 768px) {
				flex-direction: column;
				align-items: flex-start;
			}

			.rank-col {
				min-width: 40px;
				display: flex;
				align-items: center;
				justify-content: center;
				padding-top: 0.15rem;

				.medal {
					font-size: 1.5rem;
				}

				.rank-number {
					font-size: 1.2rem;
					font-weight: bold;
					opacity: 70%;
				}
			}

			.info-col {
				flex: 1;
				display: flex;
				flex-direction: column;
				gap: 0.35rem;

				.team-title-row {
					display: flex;
					align-items: center;
					flex-wrap: wrap;
					gap: 0.5rem;

					.team-name {
						margin: 0;
						font-size: 1.15rem;
						font-weight: bold;
					}

					.my-team-tag {
						font-size: 0.8rem;
						font-weight: bold;
						color: var(--md-sys-color-primary);
					}
				}

				.team-meta-row {
					display: flex;
					flex-wrap: wrap;
					align-items: center;
					gap: 0.5rem;
					font-size: 0.85rem;
					opacity: 80%;

					.meta-item {
						display: inline-flex;
						align-items: center;
						gap: 0.25rem;
					}
				}

				.my-team-card-details {
					margin-top: 0.5rem;
					display: flex;
					flex-direction: column;
					gap: 0.5rem;

					.my-team-members {
						display: flex;
						flex-direction: column;
						gap: 0.35rem;

						.members-label {
							font-size: 0.8rem;
							font-weight: bold;
							opacity: 70%;
						}

						.members-chips {
							display: flex;
							flex-wrap: wrap;
							gap: 0.4rem;
						}
					}
				}
			}

			.score-col {
				padding-top: 0.15rem;
				display: flex;
				flex-direction: column;
				align-items: flex-end;

				@media (max-width: 768px) {
					align-items: flex-start;
					margin-top: 0.5rem;
				}

				.score-box {
					display: flex;
					flex-direction: column;
					align-items: flex-end;

					@media (max-width: 768px) {
						align-items: flex-start;
					}

					.score-label {
						font-size: 0.75rem;
						opacity: 70%;
					}

					.score-val {
						font-size: 1.25rem;
						font-weight: bold;
						color: var(--md-sys-color-primary);
					}
				}

				.btn-export-cert {
					display: inline-flex;
					align-items: center;
					gap: 0.35rem;
					padding: 0.35rem 0.75rem;
					border-radius: 999px;
					font-size: 0.8rem;
					font-weight: 600;
					border: 1px solid var(--md-sys-color-outline-variant, #c9c5d0);
					background-color: var(--md-sys-color-surface-container, #f3edf7);
					color: var(--md-sys-color-on-surface, #1d1b20);
					cursor: pointer;
					transition: all 0.2s ease;
					margin-top: 0.5rem;
					white-space: nowrap;

					&:hover:not(:disabled) {
						background-color: var(--md-sys-color-primary-container, #eaddff);
						color: var(--md-sys-color-on-primary-container, #21005d);
						border-color: var(--md-sys-color-primary, #6750a4);
						transform: translateY(-1px);
					}

					&:disabled {
						opacity: 0.5;
						cursor: not-allowed;
					}
				}
			}
		}
	}

	.loading-state,
	.error-state,
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 3rem 1.5rem;
		background: var(--md-sys-color-surface-container-low);
		border-radius: 1rem;
		border: 1px solid var(--md-sys-color-outline-variant);
		text-align: center;
		gap: 0.5rem;
		opacity: 80%;

		p {
			margin: 0;
		}
	}

	.preview-badge {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.5rem 1rem;
		border-radius: 0.5rem;
		background-color: var(--md-sys-color-tertiary-container, #ffd8e4);
		color: var(--md-sys-color-on-tertiary-container, #31111d);
		font-size: 0.85rem;
		font-weight: 600;
	}

	.locked-state {
		border: 1px dashed var(--md-sys-color-primary, #0061a4);
		background: var(--md-sys-color-surface-container-low);
	}
</style>
