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
	let myMentoredTracks: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data: profile } = await getSelfProfile({ throwOnError: true })
			lecturerProfile = profile

			// Get current season
			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })
			const activeSeason = seasons?.find(
				(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
			)

			if (!activeSeason) {
				isLoading = false
				return
			}

			// Get events
			const { data: events } = await getEventsInSeason({
				path: { seasonId: activeSeason.id },
				throwOnError: true
			})

			if (!events || events.length === 0) {
				isLoading = false
				return
			}

			let allMentoredTracks: any[] = []

			// For each event, get tracks and teams
			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					// Filter tracks where this lecturer is a mentor
					const mentoredTracks = tracks.filter((t: any) => t.mentor_ids?.includes(profile.id))
					const mentoredTrackIds = mentoredTracks.map((t: any) => t.id)

					if (mentoredTrackIds.length > 0) {
						// Fetch all teams for this event
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

								// Fetch submissions for these teams
								for (let team of eventMentoredTeams) {
									const { data: submissions } = await getAllSubmissions({
										path: { teamId: team.id },
										throwOnError: false
									})
									team.submissions = submissions || []
									trackTeams.push(team)
								}
							}

							allMentoredTracks.push({
								...track,
								eventName: event.name,
								teams: trackTeams
							})
						}
					}
				}
			}

			myMentoredTracks = allMentoredTracks
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

	<div class="page-header">
		<div class="header-icon">
			<Users class="w-6 h-6" />
		</div>
		<div>
			<h1 class="page-title">Mentored Teams</h1>
			<p class="page-subtitle">Teams you are assigned to mentor this season.</p>
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
	{:else if myMentoredTracks.length === 0}
		<div class="empty-state">
			<Users class="empty-icon" />
			<h3 class="empty-title">No Mentored Tracks</h3>
			<p class="empty-text">
				You haven't been assigned as a mentor to any tracks in the active season.
			</p>
		</div>
	{:else}
		<div class="track-list">
			{#each myMentoredTracks as track}
				<div class="track-card">
					<div class="track-card-header">
						<div class="track-card-top">
							<h2 class="track-name">{track.name}</h2>
							<span class="mentor-badge">Mentor</span>
						</div>
						<p class="event-line">
							Event: <span>{track.eventName}</span>
						</p>
					</div>

					<div class="track-card-body">
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
	}

	.dark .back-link {
		color: rgb(161 161 170);
	}
	.dark .back-link:hover {
		color: rgb(251 146 60);
	}
	.light .back-link {
		color: rgb(107 114 128);
	}
	.light .back-link:hover {
		color: rgb(234 88 12);
	}

	.page-header {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-bottom: 2rem;
	}

	.header-icon {
		padding: 0.75rem;
		border-radius: 0.75rem;
	}

	.dark .header-icon {
		background: rgb(124 45 18 / 0.4);
		color: rgb(251 146 60);
	}
	.light .header-icon {
		background: rgb(255 237 213);
		color: rgb(234 88 12);
	}

	.page-title {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 700;
	}
	.dark .page-title {
		color: rgb(244 244 245);
	}
	.light .page-title {
		color: rgb(31 41 55);
	}

	.page-subtitle {
		font-size: 0.875rem;
		line-height: 1.25rem;
	}
	.dark .page-subtitle {
		color: rgb(161 161 170);
	}
	.light .page-subtitle {
		color: rgb(107 114 128);
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
		background: rgb(254 242 242);
		color: rgb(220 38 38);
		border: 1px solid rgb(254 202 202);
		border-radius: 0.75rem;
	}

	.empty-state {
		text-align: center;
		padding: 4rem 1rem;
		border: 2px dashed;
		border-radius: 1rem;
	}
	.dark .empty-state {
		border-color: rgb(39 39 42);
		color: rgb(113 113 122);
	}
	.light .empty-state {
		border-color: rgb(229 231 235);
		color: rgb(156 163 175);
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
	}

	.empty-text {
		font-size: 0.875rem;
		line-height: 1.25rem;
		margin-top: 0.25rem;
	}

	.track-list {
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.track-card {
		border-radius: 1rem;
		border: 1px solid;
		overflow: hidden;
		transition: all 0.2s ease;
	}
	.dark .track-card {
		background: rgb(24 24 27);
		border-color: rgb(39 39 42);
	}
	.light .track-card {
		background: white;
		border-color: rgb(229 231 235);
		box-shadow: 0 1px 2px rgb(0 0 0 / 0.05);
	}

	.track-card-header {
		padding: 1.5rem;
		border-bottom: 1px solid;
	}
	.dark .track-card-header {
		border-color: rgb(39 39 42);
	}
	.light .track-card-header {
		border-color: rgb(229 231 235);
	}

	.track-card-top,
	.team-card-top {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 0.75rem;
	}

	.track-name {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 700;
	}
	.dark .track-name,
	.dark .team-name {
		color: rgb(244 244 245);
	}
	.light .track-name,
	.light .team-name {
		color: rgb(17 24 39);
	}

	.mentor-badge,
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

	.mentor-badge {
		background: rgb(237 233 254);
		color: rgb(109 40 217);
	}

	.event-line {
		margin-top: 0.5rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
	}
	.dark .event-line {
		color: rgb(161 161 170);
	}
	.light .event-line {
		color: rgb(107 114 128);
	}
	.event-line span {
		font-weight: 700;
	}
	.dark .event-line span {
		color: rgb(212 212 216);
	}
	.light .event-line span {
		color: rgb(55 65 81);
	}

	.track-card-body {
		padding: 1.5rem;
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
		border: 1px solid;
		display: flex;
		flex-direction: column;
		height: 100%;
	}
	.dark .team-card {
		background: rgb(9 9 11 / 0.5);
		border-color: rgb(39 39 42);
	}
	.light .team-card {
		background: rgb(249 250 251);
		border-color: rgb(229 231 235);
	}

	.team-name {
		font-size: 1.125rem;
		line-height: 1.75rem;
		font-weight: 700;
	}

	.status-badge.approved {
		background: rgb(220 252 231);
		color: rgb(21 128 61);
	}
	.status-badge.pending {
		background: rgb(254 243 199);
		color: rgb(180 83 9);
	}

	.submission-area {
		flex-grow: 1;
		margin-top: 1rem;
	}

	.submission-label {
		margin-bottom: 0.5rem;
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
	}
	.dark .submission-label {
		color: rgb(113 113 122);
	}
	.light .submission-label {
		color: rgb(107 114 128);
	}

	.submission-card {
		padding: 0.75rem;
		border-radius: 0.5rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
	}
	.dark .submission-card {
		background: rgb(24 24 27);
		border: 1px solid rgb(39 39 42);
	}
	.light .submission-card {
		background: white;
		border: 1px solid rgb(229 231 235);
	}

	.submission-title {
		margin-bottom: 0.25rem;
		font-weight: 500;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.dark .submission-title {
		color: rgb(228 228 231);
	}
	.light .submission-title {
		color: rgb(31 41 55);
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
</style>
