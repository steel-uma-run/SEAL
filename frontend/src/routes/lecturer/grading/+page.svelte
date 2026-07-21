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
	let myJudgingTracks: any[] = $state([])
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

			let allJudgingTracks: any[] = []

			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					// Filter tracks where this lecturer is a judge
					const judgingTracks = tracks.filter((t: any) =>
						t.judges?.find((pred) => pred.id === profile.id)
					)
					const judgingTrackIds = judgingTracks.map((t: any) => t.id)

					if (judgingTrackIds.length > 0) {
						// Fetch teams
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

								// Fetch submissions
								for (let team of eventJudgingTeams) {
									const { data: submissions } = await getAllSubmissions({
										path: { teamId: team.id },
										throwOnError: false
									})

									if (submissions) {
										submissions.forEach((sub: any) => {
											trackSubmissions.push({
												...sub,
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

							allJudgingTracks.push({
								...track,
								eventName: event.name,
								submissions: trackSubmissions
							})
						}
					}
				}
			}

			myJudgingTracks = allJudgingTracks
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

<div class="grading-page">
	<a href="/lecturer" class:dark={theme.darkMode} class="back-link">
		<ArrowLeft class="icon-sm" />
		Back to Dashboard
	</a>

	<div class="page-header">
		<div class:dark={theme.darkMode} class="header-icon">
			<FileText class="icon-md" />
		</div>
		<div>
			<h1 class:dark={theme.darkMode}>Grading</h1>
			<p class:dark={theme.darkMode}>Submissions from teams you are assigned to judge.</p>
		</div>
	</div>

	{#if isLoading}
		<div class="loading-wrap">
			<div class="spinner"></div>
		</div>
	{:else if errorMessage}
		<div class="error-box">{errorMessage}</div>
	{:else if myJudgingTracks.length === 0}
		<div class:dark={theme.darkMode} class="empty-state">
			<FileText class="icon-lg" />
			<h3>No Judging Tracks</h3>
			<p>You haven't been assigned as a judge to any tracks in the active season.</p>
		</div>
	{:else}
		<div class="tracks-list">
			{#each myJudgingTracks as track}
				<div class:dark={theme.darkMode} class="track-card">
					<div class:dark={theme.darkMode} class="track-card__header">
						<div class="track-card__title-row">
							<h2 class:dark={theme.darkMode}>{track.name}</h2>
							<span class="judge-badge">Judge</span>
						</div>
						<p class:dark={theme.darkMode}>
							Event: <span class:dark={theme.darkMode}>{track.eventName}</span>
						</p>
					</div>

					<div class="track-card__body">
						{#if track.submissions && track.submissions.length > 0}
							<div class="submissions-grid">
								{#each track.submissions as sub}
									<div class:dark={theme.darkMode} class="submission-card">
										<div class="submission-card__content">
											<div class="submission-card__title-row">
												<h3 class:dark={theme.darkMode}>{sub.team_name}</h3>
											</div>
											<p class:dark={theme.darkMode} class="submission-title">{sub.title}</p>
											<p class:dark={theme.darkMode} class="submission-description">
												{sub.description}
											</p>
										</div>

										<div class="submission-card__actions">
											<a href="/lecturer/grading/{sub.team_id}/{sub.id}" class="grade-btn">
												Grade Submission
											</a>
										</div>
									</div>
								{/each}
							</div>
						{:else}
							<div class="track-empty">
								<p class:dark={theme.darkMode}>No submissions to grade for this track yet.</p>
							</div>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>

<style lang="scss">
	.grading-page {
		width: 100%;
		max-width: 72rem;
		margin: 0 auto;
		padding: 1rem;

		@media (min-width: 768px) {
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
		color: #6b7280;

		&:hover {
			color: #ea580c;
		}

		&.dark {
			color: #a1a1aa;

			&:hover {
				color: #fb923c;
			}
		}
	}

	.page-header {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-bottom: 2rem;

		h1 {
			font-size: 1.5rem;
			line-height: 2rem;
			font-weight: 700;
			color: #1f2937;

			&.dark {
				color: #f4f4f5;
			}
		}

		p {
			font-size: 0.875rem;
			line-height: 1.25rem;
			color: #6b7280;

			&.dark {
				color: #a1a1aa;
			}
		}
	}

	.header-icon {
		padding: 0.75rem;
		border-radius: 0.75rem;
		background: #ffedd5;
		color: #ea580c;

		&.dark {
			background: rgba(69, 26, 3, 0.4);
			color: #fb923c;
		}
	}

	.track-card,
	.submission-card,
	.empty-state {
		border-radius: 1rem;
	}

	.loading-wrap {
		display: flex;
		justify-content: center;
		padding: 3rem 0;
	}

	.spinner {
		width: 2rem;
		height: 2rem;
		border: 2px solid transparent;
		border-top-color: #f97316;
		border-bottom-color: #f97316;
		border-radius: 9999px;
		animation: spin 1s linear infinite;
	}

	.error-box {
		padding: 1rem;
		background: #fef2f2;
		color: #dc2626;
		border: 1px solid #fecaca;
		border-radius: 0.75rem;
	}

	.empty-state {
		text-align: center;
		padding: 4rem 1rem;
		border: 2px dashed #e5e7eb;
		color: #9ca3af;

		&.dark {
			border-color: #27272a;
			color: #71717a;
		}

		h3 {
			font-size: 1.125rem;
			font-weight: 500;
			margin-top: 0.5rem;
		}

		p {
			font-size: 0.875rem;
			margin-top: 0.25rem;
		}
	}

	.tracks-list {
		display: grid;
		gap: 2rem;
	}

	.track-card {
		overflow: hidden;
		border: 1px solid #e5e7eb;
		background: #fff;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

		&.dark {
			border-color: #27272a;
			background: #18181b;
		}
	}

	.track-card__header {
		padding: 1.5rem;
		border-bottom: 1px solid #e5e7eb;

		&.dark {
			border-bottom-color: #27272a;
		}

		p {
			font-size: 0.875rem;
			color: #6b7280;

			&.dark {
				color: #a1a1aa;
			}

			span {
				font-weight: 700;
				color: #374151;

				&.dark {
					color: #d4d4d8;
				}
			}
		}
	}

	.track-card__title-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 1rem;
		margin-bottom: 0.5rem;
	}

	.track-card h2 {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 700;
		color: #111827;

		&.dark {
			color: #f4f4f5;
		}
	}

	.judge-badge {
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.08em;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		background: #e0f2fe;
		color: #0369a1;
	}

	.track-card__body {
		padding: 1.5rem;
	}

	.submissions-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1rem;
	}

	.submission-card {
		padding: 1.25rem;
		border: 1px solid #e5e7eb;
		background: #f9fafb;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
		display: flex;
		flex-direction: column;
		gap: 1rem;
		transition: all 0.2s ease;
		justify-content: space-between;

		@media (min-width: 768px) {
			flex-direction: row;
			align-items: center;
		}

		&:hover {
			box-shadow: 0 4px 6px rgba(0, 0, 0, 0.08);
		}

		&.dark {
			background: rgba(9, 9, 11, 0.5);
			border-color: #27272a;

			&:hover {
				border-color: #3f3f46;
			}
		}
	}

	.submission-card__content {
		flex: 1 1 auto;
	}

	.submission-card__title-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 0.25rem;
	}

	.submission-card h3 {
		font-size: 1.125rem;
		font-weight: 700;
		color: #111827;

		&.dark {
			color: #f4f4f5;
		}
	}

	.submission-title {
		font-weight: 500;
		margin-bottom: 0.5rem;
		color: #ea580c;

		&.dark {
			color: #fb923c;
		}
	}

	.submission-description {
		font-size: 0.875rem;
		color: #6b7280;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;

		&.dark {
			color: #a1a1aa;
		}
	}

	.submission-card__actions {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.grade-btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 0.625rem 1.25rem;
		border-radius: 0.75rem;
		background: #f97316;
		color: #fff;
		font-size: 0.875rem;
		font-weight: 600;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		transition: background-color 0.2s ease;

		&:hover {
			background: #ea580c;
		}
	}

	.track-empty {
		text-align: center;
		padding: 2rem 0;

		p {
			font-size: 0.875rem;
			font-style: italic;
			color: #9ca3af;

			&.dark {
				color: #71717a;
			}
		}
	}

	.icon-sm {
		width: 1rem;
		height: 1rem;
	}

	.icon-md {
		width: 1.5rem;
		height: 1.5rem;
	}

	.icon-lg {
		width: 3rem;
		height: 3rem;
		margin: 0 auto 1rem;
		opacity: 0.5;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
