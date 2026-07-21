<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Award, FileText, CheckCircle, Clock } from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getAllSubmissions,
		getInterestedParticipants,
		getAllCriteriaTemplates
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let profile: any = $state(null)
	let myTeam: any = $state(null)
	let submissions: any[] = $state([])
	let criteriaTemplates: any[] = $state([])

	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			// Fetch criteria to match score criteria_id to name
			const { data: templates } = await getAllCriteriaTemplates({ throwOnError: false })
			if (templates) {
				criteriaTemplates = templates
			}

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

			// Find student's team
			let resolvedTeam = null
			for (const event of events) {
				const { data: participants } = await getInterestedParticipants({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (participants) {
					const me = participants.find((p: any) => p.email === profile.email)
					if (me && me.team_ids && me.team_ids.length > 0) {
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							const team = teams.find((t: any) => me.team_ids.includes(t.id))
							if (team) {
								resolvedTeam = team
								break
							}
						}
					}
				}
			}

			myTeam = resolvedTeam

			if (myTeam) {
				// Fetch submissions
				const { data: subs } = await getAllSubmissions({
					path: { teamId: myTeam.id },
					throwOnError: false
				})

				if (subs) {
					// Process scores to aggregate total
					submissions = subs.map((sub: any) => {
						let totalScore = 0
						if (sub.scores && sub.scores.length > 0) {
							// Find matching criteria weight
							let template = criteriaTemplates.length > 0 ? criteriaTemplates[0] : null
							sub.scores.forEach((score: any) => {
								let weight = 0
								if (template && template.criteria) {
									const c = template.criteria.find((c: any) => c.id === score.criteria_id)
									if (c) weight = c.weight
								}
								totalScore += (score.value * weight) / 100
							})
						}

						return {
							...sub,
							total_score: totalScore,
							has_been_graded: sub.scores && sub.scores.length > 0
						}
					})

					// Sort by submitted_at descending
					submissions.sort(
						(a, b) =>
							new Date(b.submitted_at || 0).getTime() - new Date(a.submitted_at || 0).getTime()
					)
				}
			}
		} catch (error: any) {
			console.error("Failed to load results", error)
			errorMessage = error.message || "Failed to load results data."
		} finally {
			isLoading = false
		}
	})

	function getCriteriaName(criteriaId: string) {
		if (criteriaTemplates.length > 0 && criteriaTemplates[0].criteria) {
			const c = criteriaTemplates[0].criteria.find((c: any) => c.id === criteriaId)
			return c ? c.name : "Unknown Criteria"
		}
		return "Criteria"
	}
</script>

<svelte:head>
	<title>My Results - SEAL</title>
</svelte:head>

<div class="results-page">
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
			<p class="results-page__subtitle">View your team's submissions and grading feedback.</p>
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
	{:else if !myTeam}
		<div class="results-page__empty">
			<FileText class="results-page__empty-icon" />
			<h3 class="results-page__empty-title">No Team Found</h3>
			<p class="results-page__empty-text">
				You are not part of any approved team in the active season. Please create or join a team
				first.
			</p>
		</div>
	{:else if submissions.length === 0}
		<div class="results-page__empty">
			<FileText class="results-page__empty-icon" />
			<h3 class="results-page__empty-title">No Submissions Yet</h3>
			<p class="results-page__empty-text">
				Your team "{myTeam.name}" has not submitted any projects.
			</p>
		</div>
	{:else}
		<div class="results-page__list">
			{#each submissions as sub, index}
				<div class="results-page__card">
					<div class="results-page__card-header">
						<div>
							<div class="results-page__card-title-row">
								<h2 class="results-page__card-title">{sub.title}</h2>
								<span class="results-page__attempt-badge">
									Attempt {submissions.length - index}
								</span>
							</div>
							<p class="results-page__card-desc">{sub.description}</p>
							{#if sub.submitted_at}
								<div class="results-page__card-date">
									<Clock class="results-page__clock-icon" />
									Submitted on {new Date(sub.submitted_at).toLocaleString()}
								</div>
							{/if}
						</div>

						<div class="results-page__score-area">
							{#if sub.has_been_graded}
								<p class="results-page__score-label">Total Score</p>
								<div class="results-page__score-row">
									<span
										class="results-page__score-value"
										class:results-page__score-value--pass={sub.total_score >= 50}
										class:results-page__score-value--fail={sub.total_score < 50}
									>
										{sub.total_score.toFixed(1)}
									</span>
									<span class="results-page__score-denominator">/100</span>
								</div>
							{:else}
								<div class="results-page__pending">
									<span class="results-page__pending-badge">Pending Review</span>
									<p class="results-page__pending-note">Awaiting grades from judges</p>
								</div>
							{/if}
						</div>
					</div>

					{#if sub.has_been_graded && sub.scores}
						<div>
							<h3 class="results-page__feedback-title">Feedback & Scores</h3>
							<div class="results-page__feedback-list">
								{#each sub.scores as score}
									<div class="results-page__feedback-item">
										<div>
											<p class="results-page__feedback-criteria">
												{getCriteriaName(score.criteria_id)}
											</p>
											{#if score.comment}
												<p class="results-page__feedback-comment">"{score.comment}"</p>
											{/if}
										</div>
										<div class="results-page__feedback-score-wrap">
											<span class="results-page__feedback-score">{score.value}</span>
										</div>
									</div>
								{/each}
							</div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	{/if}
</div>

<style lang="scss">
	// ============================================================
	// Results & Progress Page — SCSS
	// ============================================================
	// Dark mode: add `.dark` to a common ancestor (e.g. <body>).
	//   <svelte:body class:dark={theme.darkMode} />
	// ============================================================

	// ── Colour tokens ──────────────────────────────────────────────
	$white: #ffffff;
	$gray-50: #f9fafb;
	$gray-100: #f3f4f6;
	$gray-200: #e5e7eb;
	$gray-400: #9ca3af;
	$gray-500: #6b7280;
	$gray-600: #4b5563;
	$gray-700: #374151;
	$gray-800: #1f2937;
	$gray-900: #111827;

	$zinc-100: #f4f4f5;
	$zinc-200: #e4e4e7;
	$zinc-300: #d4d4d8;
	$zinc-400: #a1a1aa;
	$zinc-500: #71717a;
	$zinc-600: #52525b;
	$zinc-700: #3f3f46;
	$zinc-800: #27272a;
	$zinc-900: #18181b;
	$zinc-950: #09090b;

	$orange-50: #fff7ed;
	$orange-100: #ffedd5;
	$orange-400: #fb923c;
	$orange-500: #f97316;
	$orange-600: #ea580c;
	$orange-900: #7c2d12;
	$orange-950: #431407;

	$red-50: #fef2f2;
	$red-200: #fecaca;
	$red-500: #ef4444;
	$red-600: #dc2626;

	$green-500: #22c55e;

	$amber-50: #fffbeb;
	$amber-200: #fde68a;
	$amber-400: #fbbf24;
	$amber-600: #d97706;
	$amber-900: #78350f;
	$amber-950: #451a03;

	// ── Misc tokens ────────────────────────────────────────────────
	$radius-lg: 0.5rem; // 8px
	$radius-xl: 0.75rem; // 12px
	$radius-2xl: 1rem; // 16px
	$radius-3xl: 1.5rem; // 24px
	$radius-full: 9999px;

	$transition-colors: color, background-color, border-color, text-decoration-color, fill, stroke;
	$transition-duration: 150ms;
	$transition-ease: cubic-bezier(0.4, 0, 0.2, 1);

	$shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);

	// ── Spinner keyframes ──────────────────────────────────────────
	@keyframes results-spin {
		to {
			transform: rotate(360deg);
		}
	}

	// ============================================================
	// Block: .results-page
	// ============================================================
	.results-page {
		max-width: 56rem; // max-w-4xl
		width: 100%;
		margin-inline: auto;
		padding: 1rem;

		@media (min-width: 768px) {
			padding: 2rem;
		}

		// ── Back link ──────────────────────────────────────────────
		&__back-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			margin-bottom: 2rem;
			font-weight: 500;
			color: $gray-500;
			text-decoration: none;
			transition: $transition-colors $transition-duration $transition-ease;

			&:hover {
				color: $orange-600;
			}

			.dark & {
				color: $zinc-400;

				&:hover {
					color: $orange-400;
				}
			}
		}

		&__back-icon {
			width: 1rem;
			height: 1rem;
		}

		// ── Page header ────────────────────────────────────────────
		&__header {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			margin-bottom: 2rem;
		}

		&__header-icon-wrap {
			padding: 0.75rem;
			border-radius: $radius-xl;
			background: $orange-100;
			color: $orange-600;

			.dark & {
				background: rgb($orange-950, 0.4);
				color: $orange-400;
			}
		}

		&__header-icon {
			width: 1.5rem;
			height: 1.5rem;
		}

		&__title {
			font-size: 1.5rem;
			line-height: 2rem;
			font-weight: 700;
			color: $gray-800;

			.dark & {
				color: $zinc-100;
			}
		}

		&__subtitle {
			font-size: 0.875rem;
			line-height: 1.25rem;
			color: $gray-500;

			.dark & {
				color: $zinc-400;
			}
		}

		// ── Loading spinner ────────────────────────────────────────
		&__loading {
			display: flex;
			justify-content: center;
			padding-block: 3rem;
		}

		&__spinner {
			width: 2rem;
			height: 2rem;
			border-radius: $radius-full;
			border: 2px solid transparent;
			border-top-color: $orange-500;
			border-bottom-color: $orange-500;
			animation: results-spin 1s linear infinite;
		}

		// ── Error message ──────────────────────────────────────────
		&__error {
			padding: 1rem;
			border-radius: $radius-xl;
			border: 1px solid $red-200;
			background: $red-50;
			color: $red-600;
		}

		// ── Empty state (no team / no submissions) ─────────────────
		&__empty {
			text-align: center;
			padding-block: 4rem;
			border: 2px dashed $gray-200;
			border-radius: $radius-2xl;
			color: $gray-400;

			.dark & {
				border-color: $zinc-800;
				color: $zinc-500;
			}
		}

		&__empty-icon {
			width: 3rem;
			height: 3rem;
			margin-inline: auto;
			margin-bottom: 1rem;
			opacity: 0.5;
		}

		&__empty-title {
			font-size: 1.125rem;
			line-height: 1.75rem;
			font-weight: 500;
		}

		&__empty-text {
			font-size: 0.875rem;
			line-height: 1.25rem;
			margin-top: 0.25rem;
		}

		// ── Submissions list ───────────────────────────────────────
		&__list {
			display: flex;
			flex-direction: column;
			gap: 2rem;
		}

		// ── Submission card ────────────────────────────────────────
		&__card {
			padding: 1.5rem;
			border-radius: $radius-3xl;
			border: 1px solid $gray-200;
			background: $white;
			box-shadow: $shadow-sm;

			.dark & {
				background: $zinc-900;
				border-color: $zinc-800;
				box-shadow: none;
			}
		}

		// Card header (title + score area)
		&__card-header {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem;
			margin-bottom: 1.5rem;
			padding-bottom: 1.5rem;
			border-bottom: 1px solid $gray-100;

			@media (min-width: 768px) {
				flex-direction: row;
			}

			.dark & {
				border-bottom-color: $zinc-800;
			}
		}

		&__card-title-row {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			margin-bottom: 0.5rem;
		}

		&__card-title {
			font-size: 1.25rem;
			line-height: 1.75rem;
			font-weight: 700;
			color: $gray-900;

			.dark & {
				color: $zinc-100;
			}
		}

		&__attempt-badge {
			display: inline-block;
			font-size: 0.75rem;
			line-height: 1rem;
			font-weight: 600;
			padding: 0.25rem 0.625rem;
			border-radius: $radius-full;
			background: $gray-100;
			color: $gray-600;

			.dark & {
				background: $zinc-800;
				color: $zinc-300;
			}
		}

		&__card-desc {
			font-size: 0.875rem;
			line-height: 1.25rem;
			color: $gray-600;

			.dark & {
				color: $zinc-400;
			}
		}

		&__card-date {
			display: flex;
			align-items: center;
			gap: 0.25rem;
			margin-top: 0.75rem;
			font-size: 0.75rem;
			line-height: 1rem;
			color: $gray-400;

			.dark & {
				color: $zinc-500;
			}
		}

		&__clock-icon {
			width: 0.75rem;
			height: 0.75rem;
		}

		// ── Score area (right side of card header) ─────────────────
		&__score-area {
			flex-shrink: 0;
			text-align: right;
		}

		&__score-label {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 500;
			margin-bottom: 0.25rem;
			color: $gray-500;

			.dark & {
				color: $zinc-400;
			}
		}

		&__score-row {
			display: flex;
			align-items: baseline;
			gap: 0.25rem;
			justify-content: flex-end;
		}

		&__score-value {
			font-size: 1.875rem;
			line-height: 2.25rem;
			font-weight: 800;

			&--pass {
				color: $green-500;
			}

			&--fail {
				color: $red-500;
			}
		}

		&__score-denominator {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 700;
			color: $gray-400;

			.dark & {
				color: $zinc-500;
			}
		}

		// ── Pending review badge ───────────────────────────────────
		&__pending {
			display: inline-flex;
			flex-direction: column;
			align-items: flex-end;
		}

		&__pending-badge {
			display: inline-block;
			padding: 0.375rem 0.75rem;
			border-radius: $radius-lg;
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 500;
			border: 1px solid $amber-200;
			background: $amber-50;
			color: $amber-600;

			.dark & {
				border-color: rgb($amber-900, 0.5);
				background: rgb($amber-950, 0.3);
				color: $amber-400;
			}
		}

		&__pending-note {
			font-size: 0.75rem;
			line-height: 1rem;
			margin-top: 0.5rem;
			color: $gray-400;

			.dark & {
				color: $zinc-500;
			}
		}

		// ── Feedback & Scores section ──────────────────────────────
		&__feedback-title {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			margin-bottom: 1rem;
			color: $gray-700;

			.dark & {
				color: $zinc-300;
			}
		}

		&__feedback-list {
			display: flex;
			flex-direction: column;
			gap: 0.75rem;
		}

		&__feedback-item {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			gap: 1rem;
			padding: 1rem;
			border-radius: $radius-xl;
			background: $gray-50;

			@media (min-width: 768px) {
				flex-direction: row;
			}

			.dark & {
				background: rgb($zinc-950, 0.5);
			}
		}

		&__feedback-criteria {
			font-weight: 600;
			color: $gray-800;

			.dark & {
				color: $zinc-200;
			}
		}

		&__feedback-comment {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-style: italic;
			margin-top: 0.25rem;
			color: $gray-600;

			.dark & {
				color: $zinc-400;
			}
		}

		&__feedback-score-wrap {
			flex-shrink: 0;
		}

		&__feedback-score {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			min-width: 3rem;
			padding: 0.25rem 0.5rem;
			border-radius: $radius-lg;
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 700;
			background: $white;
			color: $gray-900;
			box-shadow: $shadow-sm;
			border: 1px solid $gray-200;

			.dark & {
				background: $zinc-800;
				color: $zinc-100;
				box-shadow: none;
				border-color: transparent;
			}
		}
	}
</style>
