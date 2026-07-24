<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import {
		ArrowLeft,
		Award,
		FileText,
		CheckCircle,
		Clock,
		History,
		X,
		ChevronRight,
		Users,
		RotateCcw
	} from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getAllSubmissions,
		getInterestedParticipants,
		getAllCriteriaTemplates
	} from "$lib/api"

	let profile: any = $state(null)
	let eventSubmissionsGroup: any[] = $state([])
	let criteriaTemplates: any[] = $state([])

	let isLoading = $state(true)
	let errorMessage = $state("")

	// Modal / History state
	let activeHistoryGroup: any = $state(null)

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			// Fetch criteria to match score criteria_id to name
			const { data: templates } = await getAllCriteriaTemplates({ throwOnError: false })
			if (templates) {
				criteriaTemplates = templates
			}

			// Get all seasons
			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			let groups: any[] = []

			if (seasons) {
				for (const season of seasons) {
					const { data: events } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (!events) continue

					for (const event of events) {
						// Find participant in event
						let hasJoined = false
						let me: any = null

						// 1. Check API
						const { data: participants } = await getInterestedParticipants({
							path: { eventId: event.id },
							throwOnError: false
						})

						if (participants) {
							const foundMe = participants.find((p: any) => p.email === profile.email)
							if (foundMe) {
								me = foundMe
								hasJoined = true
							}
						}

						if (hasJoined && me) {
							// Find team
							let team: any = null
							const { data: teams } = await getAllTeamsOfEvents({
								path: { eventId: event.id } as any,
								throwOnError: false
							})

							if (teams) {
								team = teams.find(
									(t: any) =>
										(me.team_ids && me.team_ids.includes(t.id)) ||
										t.leader_id === me.id ||
										t.leaderId === me.id ||
										(t.members && t.members.some((m: any) => m.id === me.id || m.email === me.email))
								)
							}

							let processedSubs: any[] = []
							if (team) {
								const { data: subs } = await getAllSubmissions({
									path: { teamId: team.id },
									throwOnError: false
								})

								if (subs && subs.length > 0) {
									processedSubs = subs.map((sub: any) => {
										let totalScore = 0
										if (sub.scores && sub.scores.length > 0) {
											const scoresByLecturer: Record<string, any[]> = {}
											sub.scores.forEach((score: any) => {
												const lId = score.lecturer_id || score.lecturerId
												if (!scoresByLecturer[lId]) scoresByLecturer[lId] = []
												scoresByLecturer[lId].push(score)
											})

											const lecturerTotals: number[] = []
											Object.values(scoresByLecturer).forEach((lecturerScores) => {
												let total = 0
												let totalWeight = 0
												lecturerScores.forEach((score: any) => {
													const cId = score.criteria_id || score.criteriaId
													const c = allCriteriaMap.get(cId)
													const weight = (c && c.weight) ? c.weight : 1
													total += score.value * weight
													totalWeight += weight
												})
												if (totalWeight > 0) {
													lecturerTotals.push(total / totalWeight)
												}
											})

											if (lecturerTotals.length > 0) {
												const sum = lecturerTotals.reduce((a, b) => a + b, 0)
												totalScore = sum / lecturerTotals.length
											}
										}

										return {
											...sub,
											total_score: totalScore,
											has_been_graded: sub.scores && sub.scores.length > 0
										}
									})

									// Sort by submitted_at descending (latest first)
									processedSubs.sort(
										(a, b) =>
											new Date(b.submitted_at || 0).getTime() -
											new Date(a.submitted_at || 0).getTime()
									)
								}
							}

							groups.push({
								eventId: event.id,
								eventName: event.name,
								eventDescription: event.description,
								seasonSemester: season.semester,
								seasonYear: season.year,
								team,
								latestSubmission: processedSubs[0] || null,
								historySubmissions: processedSubs.slice(1) || [],
								allSubmissions: processedSubs
							})
						}
					}
				}
			}

			eventSubmissionsGroup = groups
		} catch (error: any) {
			console.error("Failed to load results", error)
			errorMessage = error.message || "Failed to load results data."
		} finally {
			isLoading = false
		}
	})

	let allCriteriaMap = $derived.by(() => {
		const map = new Map<string, any>()
		criteriaTemplates.forEach((t: any) => {
			if (t.criteria) {
				t.criteria.forEach((c: any) => map.set(c.id, c))
			}
		})
		return map
	})

	function getCriteriaName(criteriaId: string) {
		const c = allCriteriaMap.get(criteriaId)
		return c ? c.name : "Criteria"
	}
</script>

<svelte:head>
	<title>Results & Progress - SEAL</title>
</svelte:head>

<div class="results-page {theme.darkMode ? 'results-page--dark' : ''}">
	<a href="/student" class="results-page__back-link">
		<ArrowLeft class="results-page__back-icon" />
		Back to Dashboard
	</a>

	<div class="results-page__header">
		<div class="results-page__header-icon-wrap">
			<Award class="results-page__header-icon" />
		</div>
		<div>
			<h1 class="results-page__title">Results & Progress</h1>
			<p class="results-page__subtitle">
				Track project submissions, grading feedback, and submission history per event.
			</p>
		</div>
	</div>

	{#if isLoading}
		<div class="results-page__loading">
			<div class="results-page__spinner"></div>
		</div>
	{:else if errorMessage}
		<div class="results-page__error">
			{errorMessage}
		</div>
	{:else if eventSubmissionsGroup.length === 0}
		<div class="results-page__empty">
			<FileText class="results-page__empty-icon" />
			<h3 class="results-page__empty-title">No Event Submissions Found</h3>
			<p class="results-page__empty-text">
				You haven't joined any active events or teams yet. Head over to the dashboard to join an event!
			</p>
			<a href="/student" class="btn btn--primary" style="margin-top: 1.25rem;">Go to Dashboard</a>
		</div>
	{:else}
		<div class="event-results-grid">
			{#each eventSubmissionsGroup as group}
				<div class="event-result-card">
					<div class="event-result-card__header">
						<div class="event-header-left">
							<div class="event-tag-badge">EVENT</div>
							<h2 class="event-title">{group.eventName}</h2>
							<p class="event-desc">{group.eventDescription || "No description provided."}</p>

							{#if group.team}
								<div class="team-badge">
									<Users class="badge-icon" />
									<span>Team: <strong>{group.team.name}</strong></span>
								</div>
							{/if}
						</div>

						<button
							type="button"
							class="btn-view-history-top"
							onclick={() => (activeHistoryGroup = group)}
						>
							<History class="btn-view-history-top__icon" />
							View History ({group.historySubmissions ? group.historySubmissions.length : 0}) &rarr;
						</button>
					</div>

					<div class="event-result-card__body">
						{#if !group.team}
							<div class="empty-sub-box">
								<p>You do not have an approved team for this event yet.</p>
							</div>
						{:else if !group.latestSubmission}
							<div class="empty-sub-box">
								<p>No project submitted for <strong>{group.team.name}</strong> yet.</p>
								<a href="/student/submit-project" class="btn btn--primary btn--sm" style="margin-top: 0.75rem;">
									Submit Project &rarr;
								</a>
							</div>
						{:else}
							{@const sub = group.latestSubmission}
							<div class="sub-detail-box">
								<div class="sub-detail-box__top">
									<div>
										<div class="sub-title-row">
											<h3 class="sub-title">{sub.title}</h3>
											<span class="attempt-tag">Latest (Attempt #{group.allSubmissions.length})</span>
										</div>
										<p class="sub-desc">{sub.description}</p>
										{#if sub.submitted_at}
											<div class="sub-date">
												<Clock class="icon-sm" />
												Submitted on {new Date(sub.submitted_at).toLocaleString()}
											</div>
										{/if}
									</div>

									<div class="score-pill-wrap">
										{#if sub.has_been_graded}
											<div class="score-box">
												<span class="score-label">Total Score</span>
												<div class="score-value-row">
													<span
														class="score-val {sub.total_score >= 5
															? 'score-val--pass'
															: 'score-val--fail'}"
													>
														{sub.total_score.toFixed(1)}
													</span>
													<span class="score-den">/10</span>
												</div>
											</div>
										{:else}
											<span class="badge-pending">Pending Review</span>
										{/if}
									</div>
								</div>

								{#if sub.has_been_graded && sub.scores}
									<div class="feedback-section">
										<h4 class="feedback-heading">Feedback & Scores</h4>
										<div class="feedback-grid">
											{#each sub.scores as score}
												<div class="feedback-card">
													<div class="feedback-card__text">
														<span class="criteria-title">{getCriteriaName(score.criteria_id)}</span>
														{#if score.comment}
															<p class="criteria-comment">"{score.comment}"</p>
														{/if}
													</div>
													<div class="criteria-score">{score.value}/10</div>
												</div>
											{/each}
										</div>
									</div>
								{/if}

								<div class="sub-card-actions">
									<a href="/student/submit-project" class="btn btn--resubmit">
										<RotateCcw class="btn__icon-sm" />
										Resubmit Project
									</a>
								</div>
							</div>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	{/if}

	<!-- Submission History Modal -->
	{#if activeHistoryGroup}
		<div class="modal-backdrop" onclick={() => (activeHistoryGroup = null)}>
			<div class="modal-card" onclick={(e) => e.stopPropagation()}>
				<div class="modal-card__header">
					<div>
						<div class="modal-tag">SUBMISSION HISTORY</div>
						<h2 class="modal-title">{activeHistoryGroup.eventName}</h2>
						<p class="modal-sub">Team: {activeHistoryGroup.team?.name}</p>
					</div>

					<div class="modal-header-right">
						<a href="/student/submit-project" class="btn btn--resubmit btn--sm">
							<RotateCcw class="btn__icon-sm" />
							Resubmit Project
						</a>
						<button
							type="button"
							class="btn-close"
							onclick={() => (activeHistoryGroup = null)}
						>
							<X />
						</button>
					</div>
				</div>

				<div class="modal-card__body">
					{#if activeHistoryGroup.historySubmissions.length === 0}
						<div class="modal-notice-box">
							<Clock class="notice-icon" />
							<div>
								<h4 class="notice-title">No Previous Attempts</h4>
								<p class="notice-desc">
									This is your team's initial project submission for <strong>{activeHistoryGroup.eventName}</strong>. Any future re-submissions will be recorded in this history list.
								</p>
							</div>
						</div>
					{/if}

					{#each activeHistoryGroup.allSubmissions as histSub, index}
						<div class="history-item-card {index === 0 ? 'history-item-card--latest' : ''}">
							<div class="history-item-header">
								<div>
									<div class="history-title-row">
										<h3 class="history-title">{histSub.title}</h3>
										<span class="history-attempt">
											Attempt #{activeHistoryGroup.allSubmissions.length - index}
											{index === 0 ? ' (Latest)' : ''}
										</span>
									</div>
									<p class="history-desc">{histSub.description}</p>
									{#if histSub.submitted_at}
										<div class="history-date">
											<Clock class="icon-sm" />
											Submitted on {new Date(histSub.submitted_at).toLocaleString()}
										</div>
									{/if}
								</div>

								<div class="history-score-wrap">
									{#if histSub.has_been_graded}
										<div class="score-value-row">
											<span class="score-val {histSub.total_score >= 5 ? 'score-val--pass' : 'score-val--fail'}">
												{histSub.total_score.toFixed(1)}
											</span>
											<span class="score-den">/10</span>
										</div>
									{:else}
										<span class="badge-pending">Pending Review</span>
									{/if}
								</div>
							</div>

							{#if histSub.has_been_graded && histSub.scores}
								<div class="history-scores-list">
									<h4 class="feedback-heading">Scores & Comments</h4>
									{#each histSub.scores as score}
										<div class="feedback-card">
											<div>
												<span class="criteria-title">{getCriteriaName(score.criteria_id)}</span>
												{#if score.comment}
													<p class="criteria-comment">"{score.comment}"</p>
												{/if}
											</div>
											<div class="criteria-score">{score.value}/10</div>
										</div>
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				</div>
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	$bp-md: 768px;

	.results-page {
		max-width: 64rem;
		width: 100%;
		margin-inline: auto;
		padding: 1.5rem;

		&__back-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			margin-bottom: 1.5rem;
			font-weight: 500;
			color: #6b7280;
			text-decoration: none;
			transition: color 0.2s ease;

			&:hover {
				color: #ea580c;
			}

			.results-page--dark & {
				color: #a1a1aa;
				&:hover {
					color: #fb923c;
				}
			}
		}

		&__back-icon {
			width: 1rem;
			height: 1rem;
		}

		&__header {
			display: flex;
			align-items: center;
			gap: 1rem;
			margin-bottom: 2rem;
		}

		&__header-icon-wrap {
			padding: 0.875rem;
			border-radius: 1rem;
			background: #ffedd5;
			color: #ea580c;

			.results-page--dark & {
				background: rgba(234, 88, 12, 0.15);
				color: #fb923c;
			}
		}

		&__header-icon {
			width: 1.75rem;
			height: 1.75rem;
		}

		&__title {
			font-size: 1.75rem;
			font-weight: 800;
			color: #111827;

			.results-page--dark & {
				color: #f4f4f5;
			}
		}

		&__subtitle {
			font-size: 0.875rem;
			color: #6b7280;

			.results-page--dark & {
				color: #a1a1aa;
			}
		}

		&__loading {
			display: flex;
			justify-content: center;
			padding: 4rem 0;
		}

		&__spinner {
			width: 2.5rem;
			height: 2.5rem;
			border-radius: 50%;
			border: 3px solid #e5e7eb;
			border-top-color: #ea580c;
			animation: spin 0.8s linear infinite;

			.results-page--dark & {
				border-color: #27272a;
				border-top-color: #fb923c;
			}
		}

		&__error {
			padding: 1.25rem;
			border-radius: 1rem;
			background: #fef2f2;
			border: 1px solid #fecaca;
			color: #dc2626;
		}

		&__empty {
			text-align: center;
			padding: 4rem 1.5rem;
			background: #ffffff;
			border: 2px dashed #e5e7eb;
			border-radius: 1.5rem;
			color: #6b7280;

			.results-page--dark & {
				background: #18181b;
				border-color: #27272a;
				color: #a1a1aa;
			}

			&-icon {
				width: 3rem;
				height: 3rem;
				color: #9ca3af;
				margin-bottom: 1rem;
			}

			&-title {
				font-size: 1.25rem;
				font-weight: 700;
				color: #111827;

				.results-page--dark & {
					color: #f4f4f5;
				}
			}

			&-text {
				font-size: 0.875rem;
				margin-top: 0.25rem;
			}
		}
	}

	.event-results-grid {
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.event-result-card {
		background: #ffffff;
		border: 1px solid #e5e7eb;
		border-radius: 1.5rem;
		overflow: hidden;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

		.results-page--dark & {
			background: #18181b;
			border-color: #27272a;
		}

		&__header {
			padding: 1.5rem;
			background: #f9fafb;
			border-bottom: 1px solid #e5e7eb;
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem;

			.results-page--dark & {
				background: #27272a;
				border-color: #3f3f46;
			}
		}

		&__body {
			padding: 1.5rem;
		}
	}

	.event-header-left {
		display: flex;
		flex-direction: column;
	}

	.btn-view-history-top {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		background: #ffedd5;
		color: #ea580c;
		border: 1px solid rgba(234, 88, 12, 0.2);
		padding: 0.5rem 0.875rem;
		border-radius: 0.75rem;
		font-size: 0.85rem;
		font-weight: 700;
		cursor: pointer;
		transition: all 0.2s ease;
		flex-shrink: 0;

		&:hover {
			background: #ea580c;
			color: #ffffff;
			border-color: #ea580c;
			transform: translateY(-1px);
			box-shadow: 0 4px 12px rgba(234, 88, 12, 0.25);
		}

		.results-page--dark & {
			background: rgba(234, 88, 12, 0.15);
			color: #fb923c;
			border-color: rgba(234, 88, 12, 0.3);

			&:hover {
				background: #ea580c;
				color: #ffffff;
				border-color: #ea580c;
			}
		}

		&__icon {
			width: 1rem;
			height: 1rem;
		}
	}

	.event-tag-badge {
		display: inline-block;
		font-size: 0.7rem;
		font-weight: 800;
		letter-spacing: 0.05em;
		text-transform: uppercase;
		color: #ea580c;
		background: rgba(234, 88, 12, 0.1);
		padding: 0.2rem 0.5rem;
		border-radius: 9999px;
		margin-bottom: 0.5rem;
	}

	.event-title {
		font-size: 1.35rem;
		font-weight: 800;
		color: #111827;

		.results-page--dark & {
			color: #f4f4f5;
		}
	}

	.event-desc {
		font-size: 0.875rem;
		color: #6b7280;
		margin-top: 0.25rem;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.team-badge {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.85rem;
		color: #374151;
		background: #ffffff;
		border: 1px solid #e5e7eb;
		padding: 0.375rem 0.75rem;
		border-radius: 0.75rem;
		margin-top: 0.75rem;

		.results-page--dark & {
			background: #18181b;
			border-color: #3f3f46;
			color: #d4d4d8;
		}

		.badge-icon {
			width: 1rem;
			height: 1rem;
			color: #ea580c;
		}
	}

	.sub-detail-box {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;

		&__top {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem;
		}
	}

	.sub-title-row {
		display: flex;
		align-items: center;
		gap: 0.75rem;

		.sub-title {
			font-size: 1.25rem;
			font-weight: 700;
			color: #111827;

			.results-page--dark & {
				color: #f4f4f5;
			}
		}
	}

	.attempt-tag {
		font-size: 0.75rem;
		font-weight: 700;
		background: #dbeafe;
		color: #1d4ed8;
		padding: 0.2rem 0.5rem;
		border-radius: 9999px;

		.results-page--dark & {
			background: rgba(29, 78, 216, 0.2);
			color: #60a5fa;
		}
	}

	.sub-desc {
		font-size: 0.875rem;
		color: #6b7280;
		margin-top: 0.25rem;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.sub-date {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.75rem;
		color: #9ca3af;
		margin-top: 0.5rem;

		.icon-sm {
			width: 0.875rem;
			height: 0.875rem;
		}
	}

	.score-box {
		text-align: right;

		.score-label {
			font-size: 0.75rem;
			font-weight: 600;
			color: #6b7280;
			text-transform: uppercase;

			.results-page--dark & {
				color: #a1a1aa;
			}
		}
	}

	.score-value-row {
		display: flex;
		align-items: baseline;
		gap: 0.25rem;
		justify-content: flex-end;
	}

	.score-val {
		font-size: 1.75rem;
		font-weight: 800;

		&--pass {
			color: #22c55e;
		}

		&--fail {
			color: #ef4444;
		}
	}

	.score-den {
		font-size: 0.875rem;
		font-weight: 700;
		color: #9ca3af;
	}

	.badge-pending {
		font-size: 0.85rem;
		font-weight: 700;
		background: #fffbeb;
		border: 1px solid #fde68a;
		color: #d97706;
		padding: 0.375rem 0.75rem;
		border-radius: 0.75rem;

		.results-page--dark & {
			background: rgba(217, 119, 6, 0.15);
			border-color: rgba(217, 119, 6, 0.3);
			color: #fbbf24;
		}
	}

	.feedback-section {
		border-top: 1px solid #e5e7eb;
		padding-top: 1rem;

		.results-page--dark & {
			border-color: #27272a;
		}
	}

	.feedback-heading {
		font-size: 0.8rem;
		font-weight: 800;
		text-transform: uppercase;
		letter-spacing: 0.05em;
		color: #6b7280;
		margin-bottom: 0.75rem;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.feedback-grid {
		display: flex;
		flex-direction: column;
		gap: 0.625rem;
	}

	.feedback-card {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0.75rem 1rem;
		background: #f9fafb;
		border-radius: 0.75rem;

		.results-page--dark & {
			background: #27272a;
		}

		&__text {
			display: flex;
			flex-direction: column;
		}
	}

	.criteria-title {
		font-size: 0.875rem;
		font-weight: 700;
		color: #111827;

		.results-page--dark & {
			color: #f4f4f5;
		}
	}

	.criteria-comment {
		font-size: 0.8rem;
		font-style: italic;
		color: #6b7280;
		margin-top: 0.15rem;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.criteria-score {
		font-size: 0.95rem;
		font-weight: 800;
		background: #ffffff;
		border: 1px solid #e5e7eb;
		padding: 0.25rem 0.625rem;
		border-radius: 0.5rem;
		color: #111827;

		.results-page--dark & {
			background: #18181b;
			border-color: #3f3f46;
			color: #f4f4f5;
		}
	}

	.card-footer-actions {
		display: flex;
		justify-content: flex-end;
		padding-top: 0.75rem;
		border-top: 1px solid #e5e7eb;

		.results-page--dark & {
			border-color: #27272a;
		}
	}

	.btn--history {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		background: #f3f4f6;
		color: #374151;
		border: none;
		padding: 0.5rem 1rem;
		border-radius: 0.75rem;
		font-size: 0.85rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s ease;

		&:hover {
			background: #ea580c;
			color: #ffffff;
		}

		.results-page--dark & {
			background: #27272a;
			color: #d4d4d8;

			&:hover {
				background: #ea580c;
				color: #ffffff;
			}
		}

		.btn__icon {
			width: 1rem;
			height: 1rem;
		}
	}

	.sub-card-actions {
		display: flex;
		justify-content: flex-end;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;

		.results-page--dark & {
			border-color: #27272a;
		}
	}

	.modal-header-right {
		display: flex;
		align-items: center;
		gap: 0.75rem;
	}

	.btn--resubmit {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		background: #f97316;
		color: #ffffff;
		border: none;
		padding: 0.5rem 1rem;
		border-radius: 0.75rem;
		font-size: 0.85rem;
		font-weight: 700;
		text-decoration: none;
		transition: all 0.2s ease;
		box-shadow: 0 2px 8px rgba(249, 115, 22, 0.25);

		&:hover {
			background: #ea580c;
			transform: translateY(-1px);
			box-shadow: 0 4px 12px rgba(234, 88, 12, 0.35);
			color: #ffffff;
		}

		&.btn--sm {
			padding: 0.375rem 0.75rem;
			font-size: 0.8rem;
		}

		.btn__icon-sm {
			width: 0.875rem;
			height: 0.875rem;
		}
	}

	.empty-sub-box {
		padding: 2rem;
		text-align: center;
		color: #6b7280;
		background: #f9fafb;
		border-radius: 1rem;

		.results-page--dark & {
			background: #27272a;
			color: #a1a1aa;
		}
	}

	// Modal styles
	.modal-backdrop {
		position: fixed;
		inset: 0;
		z-index: 9999;
		background: rgba(0, 0, 0, 0.7);
		backdrop-filter: blur(8px);
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 1.5rem;
	}

	.modal-card {
		background: #ffffff;
		border-radius: 1.5rem;
		max-width: 48rem;
		width: 100%;
		max-height: 85vh;
		display: flex;
		flex-direction: column;
		box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
		overflow: hidden;

		.results-page--dark & {
			background: #18181b;
			border: 1px solid #27272a;
		}

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			padding: 1.5rem;
			border-bottom: 1px solid #e5e7eb;

			.results-page--dark & {
				border-color: #27272a;
			}
		}

		&__body {
			padding: 1.5rem;
			overflow-y: auto;
			display: flex;
			flex-direction: column;
			gap: 1.25rem;
		}
	}

	.modal-notice-box {
		display: flex;
		align-items: flex-start;
		gap: 0.875rem;
		padding: 1rem 1.25rem;
		background: #eff6ff;
		border: 1px solid #bfdbfe;
		border-radius: 1rem;
		color: #1e40af;

		.results-page--dark & {
			background: rgba(30, 64, 175, 0.15);
			border-color: rgba(30, 64, 175, 0.3);
			color: #60a5fa;
		}

		.notice-icon {
			width: 1.25rem;
			height: 1.25rem;
			flex-shrink: 0;
			margin-top: 0.125rem;
		}

		.notice-title {
			font-size: 0.9rem;
			font-weight: 700;
			margin-bottom: 0.15rem;
		}

		.notice-desc {
			font-size: 0.8rem;
			line-height: 1.4;
		}
	}

	.modal-tag {
		font-size: 0.7rem;
		font-weight: 800;
		letter-spacing: 0.08em;
		color: #ea580c;
		margin-bottom: 0.25rem;
	}

	.modal-title {
		font-size: 1.5rem;
		font-weight: 800;
		color: #111827;

		.results-page--dark & {
			color: #f4f4f5;
		}
	}

	.modal-sub {
		font-size: 0.875rem;
		color: #6b7280;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.btn-close {
		background: none;
		border: none;
		color: #6b7280;
		cursor: pointer;
		padding: 0.5rem;
		border-radius: 0.5rem;
		transition: all 0.2s ease;

		&:hover {
			background: #f3f4f6;
			color: #111827;
		}

		.results-page--dark & {
			color: #a1a1aa;
			&:hover {
				background: #27272a;
				color: #f4f4f5;
			}
		}
	}

	.history-item-card {
		background: #f9fafb;
		border: 1px solid #e5e7eb;
		border-radius: 1.25rem;
		padding: 1.25rem;

		&--latest {
			border-color: #ea580c;
			background: #fff7ed;
		}

		.results-page--dark & {
			background: #27272a;
			border-color: #3f3f46;

			&--latest {
				background: rgba(234, 88, 12, 0.1);
				border-color: #ea580c;
			}
		}
	}

	.history-item-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 1rem;
	}

	.history-title-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.history-title {
		font-size: 1.125rem;
		font-weight: 700;
		color: #111827;

		.results-page--dark & {
			color: #f4f4f5;
		}
	}

	.history-attempt {
		font-size: 0.75rem;
		font-weight: 700;
		background: #e5e7eb;
		color: #374151;
		padding: 0.15rem 0.5rem;
		border-radius: 9999px;

		.results-page--dark & {
			background: #3f3f46;
			color: #f4f4f5;
		}
	}

	.history-desc {
		font-size: 0.85rem;
		color: #6b7280;
		margin-top: 0.25rem;

		.results-page--dark & {
			color: #a1a1aa;
		}
	}

	.history-date {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		font-size: 0.75rem;
		color: #9ca3af;
		margin-top: 0.5rem;
	}

	.history-scores-list {
		margin-top: 1rem;
		padding-top: 1rem;
		border-top: 1px solid #e5e7eb;

		.results-page--dark & {
			border-color: #3f3f46;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
