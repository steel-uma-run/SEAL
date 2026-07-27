<script lang="ts">
	import { onMount } from "svelte"
	import { auth } from "$lib/auth.svelte"
	import {
		getEventRanking,
		getAllTeamsOfEvents,
		getInterestedParticipants,
		getSubmissionsByEvent
	} from "$lib/api"
	import { Button, Chip } from "m3-svelte"
	import { Trophy } from "@lucide/svelte"

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

	// Filter mode: "TOP_10" or "ALL"
	let filterMode = $state<"TOP_10" | "ALL">("TOP_10")

	// Reactive check if logged in user has authorized role
	let isAuthorizedUser = $derived(
		!!auth.value && ["STUDENT", "LECTURER", "COORDINATOR"].includes(auth.value.role)
	)

	// Leaderboard only displays teams that HAVE BEEN GRADED (computedScore !== null)
	let allProcessedTeams = $derived.by(() => {
		const graded = allParticipatingTeams.filter(
			(t) => t.computedScore !== null && typeof t.computedScore === "number"
		)
		return graded
			.sort((a, b) => b.computedScore - a.computedScore)
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

	// Displayed teams based on filterMode and auth
	let displayedTeams = $derived.by(() => {
		if (!isAuthorizedUser || filterMode === "TOP_10") {
			return allProcessedTeams.slice(0, 10)
		}
		return allProcessedTeams
	})

	function scrollToMyTeam() {
		if (!myTeamInfo || myTeamInfo.rank === null) return
		if (myTeamInfo.rank > 10 && filterMode === "TOP_10") {
			filterMode = "ALL"
		}
		setTimeout(() => {
			const el = document.getElementById(`team-card-${myTeamInfo?.team.id}`)
			if (el) {
				el.scrollIntoView({ behavior: "smooth", block: "center" })
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

			allParticipatingTeams = combined.map((team) => {
				const teamSubs = allSubmissions.filter(
					(s: any) =>
						s.submitter_team_id === team.id ||
						s.submitterTeamId === team.id ||
						s.team_id === team.id ||
						s.teamId === team.id
				)

				let bestScore: number | null = null
				if (typeof team.score === "number") bestScore = team.score
				else if (typeof team.avg_score === "number") bestScore = team.avg_score
				else if (typeof team.avgScore === "number") bestScore = team.avgScore

				if (teamSubs.length > 0) {
					for (const s of teamSubs) {
						const sc = s.avg_score ?? s.avgScore ?? s.total_score ?? s.totalScore
						if (typeof sc === "number") {
							if (bestScore === null || sc > bestScore) bestScore = sc
						}
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

		{#if isAuthorizedUser && allProcessedTeams.length > 0}
			<div class="toggle-group">
				<button
					type="button"
					class:active={filterMode === "TOP_10"}
					onclick={() => (filterMode = "TOP_10")}
				>
					Top 10
				</button>
				<button
					type="button"
					class:active={filterMode === "ALL"}
					onclick={() => (filterMode = "ALL")}
				>
					All Graded ({allProcessedTeams.length})
				</button>
			</div>
		{/if}
	</div>

	<p class="subtitle">
		{#if !isAuthorizedUser}
			Showing Top 10 Graded Teams • Log in to view all rankings and your team position
		{:else}
			Showing {filterMode === "TOP_10" ? "Top 10" : "All"} Graded Teams ({displayedTeams.length} of {allProcessedTeams.length})
		{/if}
	</p>

	<!-- Personal Locator Widget (Flat Design, No Icons) -->
	{#if isAuthorizedUser && !isLoading}
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
								<Chip variant="filter" selected={true}>{myTeamInfo.team.leaderName} (Leader)</Chip>
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
					font-size: 0.9rem;
					opacity: 70%;
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
</style>
