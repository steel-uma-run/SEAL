<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Users, ArrowLeft } from "@lucide/svelte"
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

	let filteredTracks = $derived(allMentoredTracks.filter((t) => t.category === activeTab))
	let totalTeams = $derived(
		filteredTracks.reduce((acc, track) => acc + (track.teams ? track.teams.length : 0), 0)
	)

	onMount(async () => {
		try {
			const { data: profile } = await getSelfProfile({ throwOnError: true })
			lecturerProfile = profile

			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })

			if (!seasons || seasons.length === 0) {
				isLoading = false
				return
			}

			let tempTracks: any[] = []

			for (const season of seasons) {
				const category = getSeasonCategory(season, currentInfo)
				const { data: events } = await getEventsInSeason({
					path: { seasonId: season.id },
					throwOnError: false
				})

				if (!events) continue

				for (const event of events) {
					const { data: tracks } = await getAllTracksOfEvent({
						path: { eventId: event.id },
						throwOnError: false
					})

					if (tracks) {
						const mentoredTracks = tracks.filter((t: any) =>
							t.mentors?.find((m: any) => m.id == profile.id)
						)

						if (mentoredTracks.length > 0) {
							const { data: teams } = await getAllTeamsOfEvents({
								path: { eventId: event.id } as any,
								throwOnError: false
							})

							for (let track of mentoredTracks) {
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

								tempTracks.push({
									...track,
									eventName: event.name,
									category,
									teams: trackTeams
								})
							}
						}
					}
				}
			}

			allMentoredTracks = tempTracks
		} catch (error: any) {
			console.error("Failed to load mentored teams", error)
			errorMessage = error.message || "Failed to load teams."
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
		<ArrowLeft class="w-4 h-4" />
		Back to Dashboard
	</a>

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
				<h3 class="empty-title">No Mentored Tracks</h3>
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
											<div class="team-card-top">
												<h3 class="team-name">{team.name}</h3>
												<span
													class="status-badge {team.status === 'APPROVED' ? 'approved' : 'pending'}"
												>
													{team.status}
												</span>
											</div>

											<div class="submission-area">
												<h4 class="submission-label">Latest Submission</h4>
												{#if team.submissions && team.submissions.length > 0}
													{@const latestSub = team.submissions[team.submissions.length - 1]}
													<div class="submission-card">
														<p class="submission-title">{latestSub.title}</p>
														<div class="submission-links">
															{#if latestSub.github_link}
																<a href={latestSub.github_link} target="_blank">GitHub</a>
															{/if}
															{#if latestSub.youtube_link}
																<a href={latestSub.youtube_link} target="_blank" class="youtube"
																	>YouTube</a
																>
															{/if}
															{#if latestSub.slide_link}
																<a href={latestSub.slide_link} target="_blank" class="slides"
																	>Slides</a
																>
															{/if}
														</div>
													</div>
												{:else}
													<p class="no-submissions">No submissions yet.</p>
												{/if}
											</div>
										</div>
									{/each}
								</div>
							{:else}
								<div class="no-teams-wrap">
									<p class="no-submissions">No teams assigned to this track yet.</p>
								</div>
							{/if}
						</div>
					</div>
				{/each}
			</div>
		{/if}
	</div>
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

	.badge-mentor {
		background: rgb(237 233 254);
		color: rgb(109 40 217);
	}
	.dark .badge-mentor {
		background: rgb(109 40 217 / 0.2);
		color: rgb(196 181 253);
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
		height: 100%;
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

	.track-role-badge {
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		flex-shrink: 0;
	}

	.badge-mentor {
		background: rgb(219 234 254);
		color: rgb(37 99 235);
	}
	.dark .badge-mentor {
		background: rgb(37 99 235 / 0.2);
		color: rgb(147 197 253);
	}

	.track-event {
		font-size: 0.75rem;
		color: var(--md-on-surface-variant);
	}

	.track-event span {
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.team-card-divider {
		height: 1px;
		background-color: var(--md-outline-variant);
		margin: 0.75rem 0;
	}

	.team-card-top {
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

	.members-area {
		margin-bottom: 1rem;
	}

	.members-list {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
	}

	.member-chip {
		font-size: 0.75rem;
		padding: 0.25rem 0.625rem;
		background: var(--md-surface-container-high);
		color: var(--md-on-surface);
		border-radius: 0.375rem;
		font-weight: 500;
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

	.no-teams-wrap {
		text-align: center;
		padding: 2rem 0;
	}

	.no-submissions {
		font-size: 0.875rem;
		color: var(--md-on-surface-variant);
	}
</style>
