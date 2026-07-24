<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Users, ArrowLeft, Star, ExternalLink, ShieldAlert, CheckCircle2, Clock, CheckCircle } from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTracksOfEvent,
		getAllTeamsOfEvents,
		getAllSubmissions
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let lecturerProfile: any = $state(null)
	let allMentoredTracks: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")
	let activeTab: "past" | "ongoing" | "upcoming" = $state("ongoing")
	let selectedTeam: any = $state(null)

	function getSeasonCategory(season: any, currentInfo: any) {
		const s1 =
			season.year * 3 + (season.semester === "SPRING" ? 0 : season.semester === "SUMMER" ? 1 : 2)
		const s2 =
			currentInfo.year * 3 +
			(currentInfo.semester === "SPRING" ? 0 : currentInfo.semester === "SUMMER" ? 1 : 2)
		if (s1 < s2) return "past"
		if (s1 === s2) return "ongoing"
		return "upcoming"
	}

	let filteredTracks = $derived(
		allMentoredTracks.filter((track) => track.seasonCategory === activeTab)
	)

	onMount(async () => {
		try {
			const { data: profile } = await getSelfProfile()
			lecturerProfile = profile

			const { data: seasons } = await getAllSeasons()
			const currentInfo = getCurrentSeasonInfo()

			let mentoredTracks: any[] = []

			if (seasons) {
				for (let season of seasons) {
					const category = getSeasonCategory(season, currentInfo)
					const { data: events } = await getEventsInSeason({ path: { seasonId: season.id } })

					if (events) {
						for (let event of events) {
							const { data: tracks } = await getAllTracksOfEvent({ path: { eventId: event.id } })
							const { data: teams } = await getAllTeamsOfEvents({ path: { eventId: event.id } })

							if (tracks) {
								for (let track of tracks) {
									// Check if current lecturer is mentor
									const isMentor = track.mentors?.some(
										(m: any) => m.id === profile.id || m.email === profile.email
									)
									if (isMentor) {
										track.seasonCategory = category
										track.eventName = event.name
										track.seasonName = `${season.semester} ${season.year}`

										let trackTeams: any[] = []
										if (teams) {
											const eventMentoredTeams = teams.filter(
												(team: any) => team.track_id === track.id || team.trackId === track.id
											)
											for (let team of eventMentoredTeams) {
												const { data: submissions } = await getAllSubmissions({
													path: { teamId: team.id },
													throwOnError: false
												})
												team.submissions = submissions || []
												trackTeams.push(team)
											}
										}
										track.teams = trackTeams
										mentoredTracks.push(track)
									}
								}
							}
						}
					}
				}
			}

			allMentoredTracks = mentoredTracks
		} catch (err: any) {
			errorMessage = err.message || "Failed to load mentored teams."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>My Mentored Teams - SEAL</title>
</svelte:head>

<div class="mentor-page {theme.darkMode ? 'dark' : 'light'}">
	<a href="/lecturer" class="back-link">
		<ArrowLeft class="back-link__icon" />
		Back to Dashboard
	</a>

	{#if selectedTeam}
		<div class="team-detail-card">
			<div class="team-card__top">
				<div class="team-card__identity">
					<div class="team-card__badge-icon">
						<Star class="team-card__badge-svg" />
					</div>
					<div>
						<h3 class="team-card__eyebrow">
							MENTORED TEAM • {selectedTeam.eventName || 'EVENT'}
						</h3>
						<h2 class="team-card__name">{selectedTeam.name || "Untitled Team"}</h2>
					</div>
				</div>

				<div class="team-card__status-actions">
					{#if selectedTeam.status === "PENDING"}
						<span class="status-pill status-pill--pending">Status: Pending Approval</span>
					{:else if selectedTeam.status === "APPROVED"}
						<span class="status-pill status-pill--approved">Status: Approved</span>
					{/if}
				</div>
			</div>

			<div class="info-grid info-grid--2">
				<div class="info-card">
					<p class="info-card__label">Team ID</p>
					<p class="info-card__value info-card__value--mono">{selectedTeam.id}</p>
				</div>
				<div class="info-card">
					<p class="info-card__label">My Role</p>
					<p class="info-card__value info-card__value--orange">
						Mentor
					</p>
				</div>
			</div>

			<div class="info-grid info-grid--2">
				<div class="info-card">
					<p class="info-card__label">Track</p>
					<p class="info-card__value">{selectedTeam.trackName || "N/A"}</p>
				</div>
				<div class="info-card">
					<p class="info-card__label">Submissions</p>
					{#if selectedTeam.submissions && selectedTeam.submissions.length > 0}
						<div class="tag-list" style="flex-direction: column;">
							{#each selectedTeam.submissions as sub}
								<div style="background: rgba(139,92,246,0.1); padding: 0.5rem; border-radius: 0.5rem;">
									<p style="font-weight: 600; font-size: 0.8rem; margin-bottom: 0.25rem;">{sub.title}</p>
									<div style="display: flex; gap: 0.5rem;">
										{#if sub.github_link}<a href={sub.github_link} target="_blank" style="color: #6d28d9; font-size: 0.75rem; font-weight: bold; text-decoration: none;">GitHub</a>{/if}
										{#if sub.youtube_link}<a href={sub.youtube_link} target="_blank" style="color: #ef4444; font-size: 0.75rem; font-weight: bold; text-decoration: none;">YouTube</a>{/if}
										{#if sub.slide_link}<a href={sub.slide_link} target="_blank" style="color: #ea580c; font-size: 0.75rem; font-weight: bold; text-decoration: none;">Slides</a>{/if}
									</div>
								</div>
							{/each}
						</div>
					{:else}
						<p class="info-card__value">No submissions</p>
					{/if}
				</div>
			</div>

			<div class="members-section">
				<div class="members-section__header">
					<h3 class="members-section__title">Team Members ({selectedTeam.members?.length || 1}/5)</h3>
				</div>

				<div class="members-list">
					{#if selectedTeam.members && selectedTeam.members.length > 0}
						{#each selectedTeam.members as member}
							<div class="member-row">
								<div class="member-row__details">
									<div class="member-row__cell member-row__cell--name">
										<span class="member-row__name">{member.name || "Unknown Member"}</span>
									</div>
									<span class="member-row__sep">|</span>
									<div class="member-row__cell member-row__cell--id">
										{#if member.student_id}
											<span class="member-row__id-badge">{member.student_id}</span>
										{:else}
											<span class="member-row__none">NONE</span>
										{/if}
									</div>
									<span class="member-row__sep">|</span>
									<div class="member-row__cell member-row__cell--email">
										{member.email || "No Email"}
									</div>
									<span class="member-row__sep">|</span>
									<div class="member-row__cell member-row__cell--school">
										{#if member.is_external}
											<span class="member-row__school member-row__school--external"
												>{member.school_name || "External"}</span
											>
										{:else}
											<span class="member-row__school member-row__school--fpt"
												>FPT University</span
											>
										{/if}
									</div>
								</div>

								<div class="member-row__role">
									{#if member.role === "Leader" || member.id === selectedTeam.leader_id || member.id === selectedTeam.leaderId}
										<span class="role-badge role-badge--leader">Leader</span>
									{:else}
										<span class="role-badge role-badge--member">Member</span>
									{/if}
								</div>
							</div>
						{/each}
					{/if}
				</div>
				
				<div style="margin-top: 1rem; display: flex; justify-content: flex-end;">
					<button class="btn btn--secondary" onclick={() => (selectedTeam = null)}>
						Back to Teams
					</button>
				</div>
			</div>
		</div>
	{:else}
		<div class="assigned-tracks-container">
			<div class="page-header">
				<div class="header-icon">
					<Users class="w-6 h-6" />
				</div>
				<div>
					<h1 class="page-title">Mentored Teams</h1>
					<p class="page-subtitle">Teams you are assigned to mentor.</p>
				</div>
			</div>

			<div class="filters-row">
				<div class="tabs-container">
					<button
						class="tab-btn {activeTab === 'past' ? 'active' : ''}"
						onclick={() => (activeTab = "past")}
					>
						Past
					</button>
					<button
						class="tab-btn {activeTab === 'ongoing' ? 'active' : ''}"
						onclick={() => (activeTab = "ongoing")}
					>
						Ongoing
					</button>
					<button
						class="tab-btn {activeTab === 'upcoming' ? 'active' : ''}"
						onclick={() => (activeTab = "upcoming")}
					>
						Upcoming
					</button>
				</div>
			</div>

			{#if isLoading}
				<div class="loading-state">
					<div class="spinner"></div>
				</div>
			{:else if errorMessage}
				<div class="error-box">
					{errorMessage}
				</div>
			{:else if filteredTracks.length === 0}
				<div class="empty-state">
					<Users class="empty-icon" />
					<h3 class="empty-title">No Mentored Teams</h3>
					<p class="empty-text">
						You haven't been assigned as a mentor to any {activeTab} tracks.
					</p>
				</div>
			{:else}
				<div class="tracks-grid">
					{#each filteredTracks as track}
						<div class="track-card">
							<div class="track-top">
								<div class="track-heading-row">
									<h3 class="track-name">{track.name}</h3>
									<span class="track-role-badge badge-mentor">Mentor</span>
								</div>
								<p class="track-event">
									Event: <span>{track.eventName}</span>
								</p>
							</div>

							<div class="track-teams-container">
								{#if track.teams && track.teams.length > 0}
									<div class="teams-grid">
										{#each track.teams as team}
											<div class="team-card">
												<div class="sub-header">
													<h4 class="team-name">{team.name}</h4>
													{#if team.status === "PENDING"}
														<span class="status-badge pending">
															<Clock class="w-3 h-3" style="display:inline;" /> PENDING
														</span>
													{:else if team.status === "APPROVED"}
														<span class="status-badge approved">
															<CheckCircle class="w-3 h-3" style="display:inline;" /> APPROVED
														</span>
													{/if}
												</div>

												<div class="submission-area">
													<h4 class="submission-label">Team Info</h4>
													<div class="submission-card">
														<p class="submission-title">{team.members?.length || 1}/5 Members</p>
													</div>
												</div>

												<div class="grading-actions">
													<button
														class="grade-btn"
														onclick={() => {
															team.trackName = track.name;
															team.eventName = track.eventName;
															selectedTeam = team;
														}}
													>
														View Team Details
													</button>
												</div>
											</div>
										{/each}
									</div>
								{:else}
									<div class="no-teams-wrap">
										<p class="no-submissions">No teams assigned yet.</p>
									</div>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	{/if}
</div>

<style lang="scss">
	.mentor-page {
		max-width: 72rem;
		margin: 0 auto;
		width: 100%;
		padding: 1rem;
	}

	@media (min-width: 768px) {
		.mentor-page {
			padding: 2rem;
		}
	}

	.back-link {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 1.5rem;
		font-weight: 500;
		transition: color 0.2s ease;
		text-decoration: none;
		color: var(--md-on-surface-variant);
	}

	.back-link:hover {
		color: var(--md-primary);
	}

	.assigned-tracks-container {
		padding: 1.5rem;
		border-radius: 1.5rem;
		background: var(--md-surface-container-low);
		margin-bottom: 2rem;
		transition: background-color 0.3s ease;
	}

	@media (min-width: 768px) {
		.assigned-tracks-container {
			padding: 2rem;
		}
	}

	.page-header {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-bottom: 1.5rem;
	}

	.header-icon {
		padding: 0.75rem;
		border-radius: 0.75rem;
		background: rgb(249 115 22 / 0.1);
		color: rgb(249 115 22);
	}

	.page-title {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.page-subtitle {
		font-size: 0.875rem;
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);
	}

	.filters-row {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		margin-bottom: 2rem;
		border-bottom: 1px solid var(--md-outline-variant);
		padding-bottom: 1rem;
	}

	.tabs-container {
		display: flex;
		gap: 0.5rem;
	}

	.tab-btn {
		padding: 0.5rem 1rem;
		border-radius: 9999px;
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--md-on-surface-variant);
		background: transparent;
		border: 1px solid transparent;
		cursor: pointer;
		transition: all 0.2s ease;
	}

	.tab-btn:hover {
		background: var(--md-surface-container-high);
	}

	.tab-btn.active {
		background: var(--md-primary);
		color: var(--md-on-primary);
	}

	.loading-state {
		display: flex;
		justify-content: center;
		padding: 3rem 0;
	}

	.spinner {
		width: 2rem;
		height: 2rem;
		border-radius: 9999px;
		border: 2px solid transparent;
		border-top-color: rgb(249 115 22);
		border-bottom-color: rgb(249 115 22);
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.error-box {
		padding: 1rem;
		background: var(--md-error-container);
		color: var(--md-on-error-container);
		border-radius: 0.75rem;
	}

	.empty-state {
		text-align: center;
		padding: 4rem 1rem;
		border: 2px dashed var(--md-outline-variant);
		border-radius: 1rem;
		color: var(--md-on-surface-variant);
	}

	.empty-icon {
		width: 3rem;
		height: 3rem;
		margin: 0 auto 1rem;
		opacity: 0.5;
	}

	.empty-title {
		font-size: 1.125rem;
		line-height: 1.75rem;
		font-weight: 500;
		color: var(--md-on-surface);
	}

	.empty-text {
		font-size: 0.875rem;
		line-height: 1.25rem;
		margin-top: 0.25rem;
	}

	.tracks-grid {
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.track-card {
		border-radius: 1rem;
		padding: 1.5rem;
		background: var(--md-surface-container);
		box-shadow: 0 1px 2px rgb(0 0 0 / 0.05);
		transition: all 0.3s ease;
	}

	.track-top {
		margin-bottom: 1.5rem;
		border-bottom: 1px solid var(--md-outline-variant);
		padding-bottom: 1.5rem;
	}

	.track-heading-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 0.5rem;
		gap: 0.75rem;
	}

	.track-name {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.track-role-badge {
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		flex-shrink: 0;
	}

	.badge-judge {
		background: rgb(254 226 226);
		color: rgb(220 38 38);
	}
	.dark .badge-judge {
		background: rgb(220 38 38 / 0.2);
		color: rgb(252 165 165);
	}

	.track-event {
		font-size: 0.875rem;
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);
	}

	.track-event span {
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.track-teams-container {
		/* container for teams grid */
	}

	.teams-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem;
	}
	@media (min-width: 768px) {
		.teams-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}
	@media (min-width: 1024px) {
		.teams-grid {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}
	}

	.team-card {
		padding: 1.25rem;
		border-radius: 0.75rem;
		border: 1px solid var(--md-outline-variant);
		display: flex;
		flex-direction: column;
		background: var(--md-surface);
	}

	.team-track-header {
		margin-bottom: 0.75rem;
	}

	.track-heading-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 0.75rem;
		margin-bottom: 0.25rem;
	}

	.track-badge {
		font-size: 0.875rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.team-card-divider {
		height: 1px;
		background-color: var(--md-outline-variant);
		margin: 0.75rem 0;
	}

	.sub-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 0.75rem;
		margin-bottom: 1rem;
	}

	.team-name {
		font-size: 1.125rem;
		line-height: 1.75rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.status-badge {
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		flex-shrink: 0;
	}

	.status-badge.approved {
		background: rgb(220 252 231);
		color: rgb(21 128 61);
	}
	.dark .status-badge.approved {
		background: rgb(21 128 61 / 0.2);
		color: rgb(134 239 172);
	}

	.status-badge.pending {
		background: rgb(254 243 199);
		color: rgb(180 83 9);
	}
	.dark .status-badge.pending {
		background: rgb(180 83 9 / 0.2);
		color: rgb(253 230 138);
	}

	.submission-area {
		flex-grow: 1;
		margin-top: auto;
		padding-top: 1rem;
		border-top: 1px solid var(--md-outline-variant);
	}

	.submission-label {
		margin-bottom: 0.5rem;
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		color: var(--md-on-surface-variant);
	}

	.submission-card {
		padding: 0.75rem;
		border-radius: 0.5rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
		background: var(--md-surface-container);
		border: 1px solid var(--md-outline-variant);
	}

	.submission-title {
		margin-bottom: 0.25rem;
		font-weight: 500;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		color: var(--md-on-surface);
	}

	.submission-links {
		display: flex;
		gap: 0.75rem;
		margin-top: 0.5rem;
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
	}

	.submission-links a {
		color: rgb(249 115 22);
		text-decoration: none;
	}

	.submission-links a:hover {
		text-decoration: underline;
	}

	.submission-links .youtube {
		color: rgb(239 68 68);
	}

	.submission-links .slides {
		color: rgb(249 115 22);
	}

	.grading-actions {
		margin-top: 1.25rem;
	}

	.grade-btn {
		display: block;
		width: 100%;
		border: none;
		font-family: inherit;
		font-size: 1rem;
		cursor: pointer;
		text-align: center;
		padding: 0.625rem 1rem;
		background: var(--md-primary);
		color: var(--md-on-primary);
		border-radius: 0.5rem;
		font-weight: 600;
		text-decoration: none;
		transition: all 0.2s ease;
		box-sizing: border-box;
	}

	.grade-btn:hover {
		background: color-mix(in srgb, var(--md-primary) 80%, black);
	}

	.grade-btn.graded {
		background: rgb(249 115 22); /* Orange 500 */
		color: white;
	}

	.grade-btn.graded:hover {
		background: color-mix(in srgb, rgb(249 115 22) 80%, black);
	}

	.no-teams-wrap {
		text-align: center;
		padding: 2rem 0;
	}

	.no-submissions {
		font-size: 0.875rem;
		color: var(--md-on-surface-variant);
	}


	/* DETAIL VIEW STYLES */
	$bp-sm: 640px;
	$bp-md: 768px;

	.team-detail-card {
		margin-bottom: 2rem;
		padding: 1.5rem;
		border-radius: 1.5rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container);
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		transition: all 0.3s ease;

		@media (min-width: $bp-md) {
			padding: 2rem;
		}

		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 1rem;
			flex-wrap: wrap;
			margin-bottom: 1rem;
		}

		&__identity {
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}

		&__badge-icon {
			padding: 0.625rem;
			border-radius: 0.75rem;
			background: rgb(249 115 22); /* Orange 500 */
			color: #fff;
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		}
		&__badge-svg {
			width: 1.5rem;
			height: 1.5rem;
		}

		&__eyebrow {
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			color: rgb(234 88 12);
			.dark & {
				color: rgb(251 146 60);
			}
		}

		&__name {
			font-size: 1.5rem;
			font-weight: 700;
			color: var(--md-on-surface);
			@media (min-width: $bp-md) {
				font-size: 1.875rem;
			}
		}

		&__status-actions {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			flex-wrap: wrap;
		}
	}

	.status-pill {
		padding: 0.375rem 0.75rem;
		border-radius: 0.5rem;
		border-width: 1px;
		border-style: solid;
		font-weight: 700;
		font-size: 0.875rem;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		display: inline-flex;
		align-items: center;

		&--pending {
			color: #92400e;
			background: rgba(254, 243, 199, 0.7);
			border-color: #fde68a;
			.dark & {
				color: #fbbf24;
				background: rgba(245, 158, 11, 0.1);
				border-color: rgba(245, 158, 11, 0.2);
			}
		}
		&--approved {
			color: #065f46;
			background: rgba(209, 250, 229, 0.7);
			border-color: #a7f3d0;
			.dark & {
				color: #34d399;
				background: rgba(16, 185, 129, 0.1);
				border-color: rgba(16, 185, 129, 0.2);
			}
		}
	}

	.info-grid {
		display: grid;
		gap: 1rem;
		margin-top: 2rem;
		margin-bottom: 2rem;

		&--2 {
			grid-template-columns: 1fr;
			@media (min-width: $bp-md) {
				grid-template-columns: repeat(2, 1fr);
			}
		}
	}

	.info-card {
		padding: 1.25rem;
		border-radius: 1rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface);

		&__label {
			font-size: 0.75rem;
			color: var(--md-on-surface-variant);
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			margin-bottom: 0.5rem;
		}

		&__value {
			font-weight: 600;
			font-size: 0.875rem;
			color: var(--md-on-surface);
			&--mono {
				font-family: monospace;
				font-size: 0.875rem;
			}
			&--orange {
				color: rgb(249 115 22);
			}
		}
	}

	.members-section {
		border-top: 1px solid var(--md-outline-variant);
		padding-top: 2rem;

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 1rem;
		}
		&__title {
			font-size: 1.125rem;
			font-weight: 700;
			color: var(--md-on-surface);
		}
	}

	.members-list {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
		margin-bottom: 2rem;
	}

	.member-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 1rem;
		border-radius: 0.75rem;
		background: var(--md-surface-container-low);
		border: 1px solid var(--md-outline-variant);
		transition: all 0.2s ease;

		&:hover {
			border-color: var(--md-outline);
		}

		&__details {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 0.5rem;
		}

		&__cell {
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
			&--name {
				font-weight: 600;
				color: var(--md-on-surface);
			}
			&--email {
				/* default color */
			}
		}
		
		&__id-badge {
			font-family: monospace;
			background: var(--md-surface-container-high);
			padding: 0.125rem 0.375rem;
			border-radius: 0.25rem;
			color: var(--md-on-surface);
			font-weight: 600;
		}
		
		&__none {
			font-size: 0.75rem;
			font-style: italic;
			color: var(--md-on-surface-variant);
		}

		&__sep {
			color: var(--md-outline-variant);
		}

		&__school {
			&--fpt {
				color: rgb(234 88 12);
				font-weight: 500;
			}
			&--external {
				color: rgb(139 92 246);
				font-weight: 500;
			}
		}
	}
	
	.role-badge {
		font-size: 0.75rem;
		font-weight: 700;
		padding: 0.25rem 0.75rem;
		border-radius: 9999px;
		
		&--leader {
			background: rgb(255 237 213);
			color: rgb(194 65 12);
			.dark & {
				background: rgba(194, 65, 12, 0.2);
				color: rgb(251 146 60);
			}
		}
		
		&--member {
			background: var(--md-surface-container-high);
			color: var(--md-on-surface-variant);
		}
	}

	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		font-weight: 600;
		border-radius: 0.75rem;
		transition: all 0.2s ease;
		cursor: pointer;
		border: none;
		text-decoration: none;
		white-space: nowrap;
		padding: 0.625rem 1.25rem;

		&:disabled {
			opacity: 0.5;
			cursor: not-allowed;
		}
		&:active:not(:disabled) {
			transform: scale(0.95);
		}

		&--secondary {
			background: var(--md-surface-container-high);
			color: var(--md-on-surface);
			&:hover {
				background: var(--md-surface-container-highest);
			}
		}
	}

	.badge-mentor {
		background: rgb(255 237 213);
		color: rgb(194 65 12);
	}
	.dark .badge-mentor {
		background: rgba(194, 65, 12, 0.2);
		color: rgb(251 146 60);
	}
</style>

