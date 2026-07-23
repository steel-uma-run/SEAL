<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { FileText, ArrowLeft, CheckCircle, Clock } from "@lucide/svelte"
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
	let allJudgingTracks: any[] = $state([])
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

	let filteredTracks = $derived(allJudgingTracks.filter((t) => t.category === activeTab))
	let totalSubmissions = $derived(
		filteredTracks.reduce(
			(acc, track) => acc + (track.submissions ? track.submissions.length : 0),
			0
		)
	)

	onMount(async () => {
		try {
			const { data: profile, error: profileError } = await getSelfProfile({ throwOnError: false })
			if (profileError) {
				window.location.href = "/auth/login"
				return
			}
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
						const judgingTracks = tracks.filter((t: any) =>
							t.judges?.find((pred: any) => pred.id == profile.id)
						)

						if (judgingTracks.length > 0) {
							const { data: teams } = await getAllTeamsOfEvents({
								path: { eventId: event.id } as any,
								throwOnError: false
							})

							for (let track of judgingTracks) {
								let trackSubmissions: any[] = []

								if (teams) {
									const eventJudgingTeams = teams.filter(
										(team: any) => team.track_id === track.id || team.trackId === track.id
									)

									for (let team of eventJudgingTeams) {
										const { data: submissions } = await getAllSubmissions({
											path: { teamId: team.id },
											throwOnError: false
										})

										if (submissions) {
											submissions.forEach((sub: any) => {
												const hasGraded = sub.scores && sub.scores.some((s: any) => s.lecturer_id === lecturerProfile?.id)
												trackSubmissions.push({
													...sub,
													status: hasGraded ? "GRADED" : "PENDING",
													team_name: team.name,
													team_id: team.id
												})
											})
										}
									}
								}

								trackSubmissions.sort(
									(a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
								)

								tempTracks.push({
									...track,
									eventName: event.name,
									category,
									submissions: trackSubmissions
								})
							}
						}
					}
				}
			}

			allJudgingTracks = tempTracks
		} catch (error: any) {
			console.error("Failed to load submissions", error)
			errorMessage = error.message || "Failed to load submissions for judging."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>Submissions to Grade - SEAL</title>
</svelte:head>

<div class="mentor-page {theme.darkMode ? 'dark' : 'light'}">
	<a href="/lecturer" class="back-link">
		<ArrowLeft class="w-4 h-4" />
		Back to Dashboard
	</a>

	<div class="assigned-tracks-container">
		<div class="page-header">
			<div class="header-icon">
				<FileText class="w-6 h-6" />
			</div>
			<div>
				<h1 class="page-title">Grading</h1>
				<p class="page-subtitle">Submissions from teams you are assigned to judge.</p>
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
				<FileText class="empty-icon" />
				<h3 class="empty-title">No Judging Tracks</h3>
				<p class="empty-text">
					You haven't been assigned as a judge to any {activeTab} tracks.
				</p>
			</div>
		{:else}
			<div class="tracks-grid">
				{#each filteredTracks as track}
					<div class="track-card">
						<div class="track-top">
							<div class="track-heading-row">
								<h3 class="track-name">{track.name}</h3>
								<span class="track-role-badge badge-judge">Judge</span>
							</div>
							<p class="track-event">
								Event: <span>{track.eventName}</span>
							</p>
						</div>

						<div class="track-teams-container">
							{#if track.submissions && track.submissions.length > 0}
								<div class="teams-grid">
									{#each track.submissions as sub}
										<div class="team-card">
											<div class="sub-header">
												<h4 class="team-name">{sub.team_name}</h4>
												{#if sub.status === "GRADED"}
													<span class="status-badge approved">
														<CheckCircle class="w-3 h-3" style="display:inline;" /> {sub.total_score !== undefined && sub.total_score !== null ? (Number(sub.total_score) / 10).toFixed(2) + '/10' : "Graded"}
													</span>
												{:else}
													<span class="status-badge pending">
														<Clock class="w-3 h-3" style="display:inline;" /> Pending
													</span>
												{/if}
											</div>

											<div class="submission-area">
												<h4 class="submission-label">Submission</h4>
												<div class="submission-card">
													<p class="submission-title">{sub.title}</p>
													<div class="submission-links">
														{#if sub.github_link}
															<a href={sub.github_link} target="_blank">GitHub</a>
														{/if}
														{#if sub.youtube_link}
															<a href={sub.youtube_link} target="_blank" class="youtube">YouTube</a>
														{/if}
														{#if sub.slide_link}
															<a href={sub.slide_link} target="_blank" class="slides">Slides</a>
														{/if}
													</div>
												</div>
											</div>

											<div class="grading-actions">
												<a
													href="/lecturer/grading/{sub.team_id}/{sub.id}"
													class="grade-btn {sub.status === 'GRADED' ? 'graded' : ''}"
												>
													{sub.status === "GRADED" ? "Review Grade" : "Grade Now"}
												</a>
											</div>
										</div>
									{/each}
								</div>
							{:else}
								<div class="no-teams-wrap">
									<p class="no-submissions">No submissions yet.</p>
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
</style>
