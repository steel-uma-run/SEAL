<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { theme } from "$lib/theme.svelte"
	import {
		ArrowLeft,
		Save,
		AlertCircle,
		FileText,
		MonitorPlay,
		Code,
		Presentation
	} from "@lucide/svelte"
	import { getAllCriteriaTemplates, gradeSubmission, getAllSubmissions, getSelfProfile, requestRegrade } from "$lib/api"
	import { goto } from "$app/navigation"

	let teamId = $derived($page.params.teamId)
	let submissionId = $derived($page.params.submissionId)

	let isLoading = $state(true)
	let isSubmitting = $state(false)
	let errorMessage = $state("")
	let successMessage = $state("")

	let criteriaTemplate: any = $state(null)
	let submission: any = $state(null)
	let hasGraded = $state(false)

	// Form state: map criteria_id -> { value: number, comment: string }
	let gradingData: Record<string, { value: number | null; comment: string }> = $state({})

	// Regrade request state
	let regradeReason = $state("")
	let isRequestingRegrade = $state(false)
	let regradeRequestSuccess = $state(false)

	function extractYoutubeId(url: string) {
		if (!url) return null
		const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/
		const match = url.match(regExp)
		return match && match[2].length === 11 ? match[2] : null
	}

	$effect(() => {
		if (teamId && submissionId) {
			loadData(teamId, submissionId)
		}
	})

	async function loadData(currentTeamId: string, currentSubmissionId: string) {
		try {
			// Fetch the submission based on teamId
			const { data: submissions, error: submissionsError } = await getAllSubmissions({
				path: { teamId: currentTeamId },
				throwOnError: false
			})

			if (submissionsError) {
				errorMessage = `Failed to load submissions: ${submissionsError?.message || JSON.stringify(submissionsError)}`
				isLoading = false
				return
			}

			if (submissions) {
				submission = submissions.find((s: any) => s.id === currentSubmissionId)
			}

			if (!submission) {
				errorMessage = "Submission not found or you don't have access."
				isLoading = false
				return
			}

			// Fetch criteria templates
			const { data: templates, error: templatesError } = await getAllCriteriaTemplates({ throwOnError: false })

			if (templatesError) {
				errorMessage = `Failed to load criteria templates: ${templatesError?.message || JSON.stringify(templatesError)}`
				isLoading = false
				return
			}

			const { data: profile } = await getSelfProfile({ throwOnError: false })

			if (templates && templates.length > 0) {
				// We use the first template as a workaround since backend doesn't link Event/Round to a specific template
				criteriaTemplate = templates[0]

				// Initialize grading form data
				if (criteriaTemplate.criteria) {
					criteriaTemplate.criteria.forEach((c: any) => {
						const existingScore = submission.scores?.find((s: any) => s.criteria_id === c.id && s.lecturer_id === profile?.id)
						if (existingScore) {
							hasGraded = true
							gradingData[c.id] = { value: existingScore.value, comment: existingScore.comment || "" }
						} else {
							gradingData[c.id] = { value: null, comment: "" }
						}
					})
				}
			} else {
				errorMessage =
					"No grading criteria templates found in the system. Please contact the coordinator."
			}
		} catch (error: any) {
			console.error("Failed to load data", error)
			errorMessage = (error && error.message) ? error.message : JSON.stringify(error) || "Failed to load grading details."
		} finally {
			isLoading = false
		}
	}

	// Calculate total score based on weights (only count valid scores between 0 and 10)
	let totalScore = $derived.by(() => {
		if (!criteriaTemplate || !criteriaTemplate.criteria) return 0
		let total = 0
		for (const c of criteriaTemplate.criteria) {
			const val = gradingData[c.id]?.value
			if (val !== null && val !== undefined && !isNaN(val) && val >= 0 && val <= 10) {
				total += (val * (c.weight || 0)) / 100
			}
		}
		return total
	})

	function handleScoreInput(criteriaId: string, event: Event) {
		const input = event.target as HTMLInputElement
		if (input.value === "") {
			gradingData[criteriaId].value = null
			return
		}
		const val = Number(input.value)
		if (isNaN(val) || val < 0 || val > 10) {
			gradingData[criteriaId].value = null
			input.value = ""
			errorMessage = "Score must be between 0 and 10. Invalid value was cleared."
		} else {
			gradingData[criteriaId].value = val
			if (errorMessage && errorMessage.includes("between 0 and 10")) {
				errorMessage = ""
			}
		}
	}


	// Check if any grade is below threshold (e.g., below 5) and requires a comment
	let requiresDocumentedReason = $derived.by(() => {
		if (!criteriaTemplate || !criteriaTemplate.criteria) return false
		for (const c of criteriaTemplate.criteria) {
			const val = gradingData[c.id]?.value
			const comment = gradingData[c.id]?.comment
			// BR-47: Required documented reason if score is below 5
			if (val !== null && val !== undefined && val < 5 && (!comment || comment.trim() === "")) {
				return true
			}
		}
		return false
	})

	// Check if any grade is out of range (< 0 or > 10)
	let hasInvalidScores = $derived.by(() => {
		if (!criteriaTemplate || !criteriaTemplate.criteria) return false
		for (const c of criteriaTemplate.criteria) {
			const val = gradingData[c.id]?.value
			if (val !== null && val !== undefined && (val < 0 || val > 10)) {
				return true
			}
		}
		return false
	})

	async function handleSubmit(e: Event) {
		e.preventDefault()
		errorMessage = ""
		successMessage = ""

		// Validation
		for (const c of criteriaTemplate.criteria) {
			const grade = gradingData[c.id]
			if (grade.value === null || grade.value === undefined || isNaN(Number(grade.value))) {
				errorMessage = `Please enter a valid score for ${c.name}`
				return
			}
			if (grade.value < 0 || grade.value > 10) {
				errorMessage = `Score for ${c.name} must be between 0 and 10.`
				return
			}
		}

		if (requiresDocumentedReason) {
			errorMessage = "Please provide a comment (documented reason) for any score below 5."
			return
		}

		isSubmitting = true

		try {
			// Prepare payload
			const body = Object.keys(gradingData).map((criteriaId) => ({
				criteria_id: criteriaId,
				value: Number(gradingData[criteriaId].value),
				comment: gradingData[criteriaId].comment || ""
			}))

			const { response, error } = await gradeSubmission({
				path: { submissionId },
				body: body,
				throwOnError: false
			})

			if (response?.ok) {
				successMessage = "Submission graded successfully!"
				setTimeout(() => {
					goto("/lecturer/grading")
				}, 1500)
			} else {
				const errorDetails = error || (await response?.json().catch(() => null))
				errorMessage = `Failed to submit grades: ${errorDetails?.detail || errorDetails?.message || response?.statusText}`
			}
		} catch (error: any) {
			console.error("Grading submission error", error)
			errorMessage = error.message || "An unexpected error occurred."
		} finally {
			isSubmitting = false
		}
	}

	async function handleRequestRegrade() {
		if (!regradeReason.trim()) {
			errorMessage = "Please enter a reason for requesting a re-grade."
			return
		}
		errorMessage = ""
		isRequestingRegrade = true
		try {
			const { response, error } = await requestRegrade({
				path: { submissionId },
				body: { reason: regradeReason }
			})
			if (response?.ok) {
				regradeRequestSuccess = true
				successMessage = "Re-grading request submitted successfully!"
			} else {
				const errorDetails = error || (await response?.json().catch(() => null))
				errorMessage = `Failed to request re-grade: ${errorDetails?.detail || errorDetails?.message || response?.statusText}`
			}
		} catch (err: any) {
			console.error("Failed to request regrade", err)
			errorMessage = err.message || "An unexpected error occurred."
		} finally {
			isRequestingRegrade = false
		}
	}
</script>

<svelte:head>
	<title>Grade Submission - SEAL</title>
</svelte:head>

<div class="grading-page" class:dark={theme.darkMode}>
	<a href="/lecturer/grading" class="back-link">
		<ArrowLeft class="link-icon" />
		Back to Grading List
	</a>

	<div class="page-header">
		<div>
			<h1 class="page-title">Grade Submission</h1>
			<p class="page-subtitle">
				ID: <span class="mono">{submissionId}</span>
			</p>
		</div>
		{#if criteriaTemplate}
			<div class="score-box">
				<p class="score-label">Total Score</p>
				<p class="score-value" class:pass={totalScore >= 5} class:fail={totalScore < 5}>
					{totalScore.toFixed(1)}<span class="score-max">/10</span>
				</p>
			</div>
		{/if}
	</div>

	{#if isLoading}
		<div class="spinner-wrap">
			<div class="spinner spinner--green"></div>
		</div>
	{:else if errorMessage && !submission}
		<div class="alert alert--error">{errorMessage}</div>
	{:else}
		<div class="split-layout">
			<!-- LEFT COLUMN: Submission Details -->
			<div class="submission-details">
				<div class="card stick-to-top">
					<h2 class="card-title">Project Details</h2>
					<p class="submission-title">{submission.title || "Untitled Project"}</p>

					{#if submission.description}
						<div class="description-box">
							<p>{submission.description}</p>
						</div>
					{/if}

					<div class="links-list">
						{#if submission.github_link}
							<a
								href={submission.github_link}
								target="_blank"
								rel="noopener noreferrer"
								class="resource-link github"
							>
								<Code class="icon-sm" />
								<span>GitHub Repository</span>
							</a>
						{/if}

						{#if submission.slide_link}
							<a
								href={submission.slide_link}
								target="_blank"
								rel="noopener noreferrer"
								class="resource-link slide"
							>
								<Presentation class="icon-sm" />
								<span>Presentation Slides</span>
							</a>
						{/if}
					</div>

					{#if submission.youtube_link}
						<div class="video-section">
							<h3 class="section-heading">Demo Video</h3>
							{#if extractYoutubeId(submission.youtube_link)}
								<div class="video-container">
									<iframe
										src="https://www.youtube.com/embed/{extractYoutubeId(submission.youtube_link)}"
										title="YouTube video player"
										frameborder="0"
										allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
										allowfullscreen
									>
									</iframe>
								</div>
							{:else}
								<a
									href={submission.youtube_link}
									target="_blank"
									rel="noopener noreferrer"
									class="resource-link youtube"
								>
									<MonitorPlay class="icon-sm" />
									<span>Watch Demo Video</span>
								</a>
							{/if}
						</div>
					{/if}
				</div>
			</div>

			<!-- RIGHT COLUMN: Grading Form -->
			<div class="grading-form-container">
				{#if !criteriaTemplate}
					<div class="alert alert--error">{errorMessage}</div>
				{:else}
					<form onsubmit={handleSubmit} class="grade-form">
						<div class="card">
							<h2 class="card-title">Evaluation Criteria</h2>
							<p class="card-desc">Please score each criteria from 0 to 10.</p>

							{#if errorMessage}
								<div class="alert alert--error alert--inline">
									<AlertCircle class="alert-icon" />
									<p class="alert-text">{errorMessage}</p>
								</div>
							{/if}

							{#if successMessage}
								<div class="alert alert--success">{successMessage}</div>
							{/if}

							<div class="criteria-list">
								{#each criteriaTemplate.criteria as c}
									<div class="criterion">
										<div class="criterion-head">
											<div>
												<h3 class="criterion-name">{c.name}</h3>
												<p class="criterion-weight">Weight: {c.weight}%</p>
											</div>
											<div class="score-input">
												<label class="field-label">Score (0-10)</label>
												<input
													type="number"
													min="0"
													max="10"
													step="0.1"
													value={gradingData[c.id].value ?? ""}
													oninput={(e) => handleScoreInput(c.id, e)}
													onchange={(e) => handleScoreInput(c.id, e)}
													class="input {gradingData[c.id].value !== null && gradingData[c.id].value !== undefined && (gradingData[c.id].value < 0 || gradingData[c.id].value > 10) ? 'input--error' : ''}"
													placeholder="e.g. 8.5"
													required
													disabled={hasGraded}
												/>
												{#if gradingData[c.id].value !== null && gradingData[c.id].value !== undefined && (gradingData[c.id].value < 0 || gradingData[c.id].value > 10)}
													<p class="field-error-text">Score must be between 0 and 10</p>
												{/if}
											</div>
										</div>
										<div>
											<label class="field-label">
												Comment / Feedback
												{#if gradingData[c.id].value !== null && gradingData[c.id].value < 5}
													<span class="field-required">(Required for scores below 5)</span>
												{/if}
											</label>
											<textarea
												bind:value={gradingData[c.id].comment}
												rows="2"
												class="input textarea"
												placeholder="Provide constructive feedback..."
												disabled={hasGraded}
											></textarea>
										</div>
									</div>
								{/each}
							</div>
						</div>

						{#if !hasGraded}
							<div class="form-actions">
								<button type="submit" disabled={isSubmitting || hasInvalidScores} class="btn-submit">
									{#if isSubmitting}
										<div class="spinner spinner--white"></div>
										Submitting...
									{:else}
										<Save class="btn-icon" />
										Submit Grades
									{/if}
								</button>
							</div>
						{/if}
					</form>

					{#if hasGraded}
						<div class="card regrade-card" style="margin-top: 1.5rem;">
							<h2 class="card-title">Request Re-grading</h2>
							<p class="card-desc">
								You have already graded this submission. To change your score, you must submit a request with a valid reason to the coordinator for approval.
							</p>

							{#if regradeRequestSuccess}
								<div class="alert alert--success">
									Re-grading request submitted successfully! Awaiting Coordinator approval.
								</div>
							{:else}
								<div class="regrade-field">
									<label class="field-label">Reason for Requesting Re-grade <span class="field-required">*</span></label>
									<textarea
										bind:value={regradeReason}
										rows="3"
										class="input textarea"
										placeholder="Specify why you want to change your scores..."
										required
									></textarea>
								</div>

								<div class="form-actions" style="margin-top: 1.25rem;">
									<button
										type="button"
										onclick={handleRequestRegrade}
										disabled={isRequestingRegrade || !regradeReason.trim()}
										class="btn-submit"
									>
										{#if isRequestingRegrade}
											<div class="spinner spinner--white"></div>
											Submitting...
										{:else}
											<Save class="btn-icon" />
											Submit Request
										{/if}
									</button>
								</div>
							{/if}
						</div>
					{/if}
				{/if}
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	// ---------- Design tokens ----------
	$max-width: 80rem; // max-w-7xl

	// Accent / status colors (theme-independent)
	$green-400: #4ade80;
	$green-500: #22c55e;
	$green-600: #16a34a;
	$green-700: #15803d;
	$red-500: #ef4444;
	$red-600: #dc2626;
	$gray-400: #9ca3af;

	// Brand colors
	$github: #24292e;
	$youtube: #ff0000;
	$slide: #f59e0b;

	// Alert palettes (theme-independent)
	$red-bg: #fef2f2; // red-50
	$red-border: #fecaca; // red-200
	$green-bg: #f0fdf4; // green-50
	$green-border: #bbf7d0; // green-200

	// ---------- Component ----------
	.grading-page {
		max-width: $max-width;
		margin: 0 auto;
		width: 100%;
		padding: 1rem;
		box-sizing: border-box;

		@media (min-width: 768px) {
			padding: 2rem;
		}

		// ---- Theme variables (light defaults) ----
		--text-strong: #1f2937; // gray-800
		--text-muted: #6b7280; // gray-500
		--text-criterion: #1f2937; // gray-800
		--bg-card: #ffffff; // bg-white
		--border-card: #e5e7eb; // gray-200
		--bg-criterion: #f9fafb; // gray-50
		--bg-input: #ffffff; // bg-white
		--border-input: #d1d5db; // gray-300
		--link-hover: #{$green-600};
		--accent-text: #{$green-600};

		// ---- Dark theme overrides ----
		&.dark {
			--text-strong: #f4f4f5; // zinc-100
			--text-muted: #a1a1aa; // zinc-400
			--text-criterion: #e4e4e7; // zinc-200
			--bg-card: #18181b; // zinc-900
			--border-card: #27272a; // zinc-800
			--bg-criterion: rgba(9, 9, 11, 0.5); // zinc-950/50
			--bg-input: #18181b; // zinc-900
			--border-input: #3f3f46; // zinc-700
			--link-hover: #{$green-400};
			--accent-text: #{$green-400};

			.card {
				box-shadow: none;
			}
		}

		// ---- Back link ----
		.back-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			margin-bottom: 1.5rem;
			font-weight: 500;
			color: var(--text-muted);
			transition: color 0.15s ease;
			text-decoration: none;

			&:hover {
				color: var(--link-hover);
			}
		}

		.link-icon {
			width: 1rem; // w-4
			height: 1rem; // h-4
		}

		// ---- Header ----
		.page-header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 2rem;
		}

		.page-title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			color: var(--text-strong);
			margin: 0;
		}

		.page-subtitle {
			font-size: 0.875rem; // text-sm
			color: var(--text-muted);
			margin: 0.25rem 0 0 0;
		}

		.mono {
			font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
			font-size: 0.75rem; // text-xs
		}

		// ---- Score box ----
		.score-box {
			text-align: right;
		}

		.score-label {
			font-size: 0.875rem;
			font-weight: 500;
			color: var(--text-muted);
			margin: 0 0 0.25rem 0;
		}

		.score-value {
			font-size: 1.875rem; // text-3xl
			font-weight: 700;
			margin: 0;

			&.pass {
				color: $green-500;
			}
			&.fail {
				color: $red-500;
			}
		}

		.score-max {
			font-size: 1.125rem; // text-lg
			color: $gray-400;
		}

		// ---- Split Layout ----
		.split-layout {
			display: grid;
			grid-template-columns: 1fr;
			gap: 2rem;
			align-items: start;

			@media (min-width: 1024px) {
				grid-template-columns: 1fr 1.2fr;
			}
		}

		.stick-to-top {
			position: sticky;
			top: 2rem;
		}

		// ---- Left Column (Details) ----
		.submission-title {
			font-size: 1.25rem;
			font-weight: 600;
			color: var(--text-strong);
			margin: 1rem 0;
		}

		.description-box {
			background: var(--bg-criterion);
			padding: 1rem;
			border-radius: 0.5rem;
			margin-bottom: 1.5rem;
			color: var(--text-muted);
			font-size: 0.875rem;
			line-height: 1.5;
			border: 1px solid var(--border-card);
		}

		.section-heading {
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--text-muted);
			text-transform: uppercase;
			letter-spacing: 0.05em;
			margin: 1.5rem 0 0.75rem 0;
		}

		.links-list {
			display: flex;
			flex-wrap: wrap;
			gap: 0.75rem;
			margin-bottom: 1.5rem;
		}

		.resource-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			padding: 0.5rem 1rem;
			border-radius: 0.5rem;
			font-size: 0.875rem;
			font-weight: 500;
			text-decoration: none;
			color: #ffffff;
			transition: opacity 0.2s;

			&:hover {
				opacity: 0.9;
			}

			&.github {
				background: $github;
			}
			&.youtube {
				background: $youtube;
			}
			&.slide {
				background: $slide;
			}

			.icon-sm {
				width: 1.25rem;
				height: 1.25rem;
			}
		}

		.video-container {
			position: relative;
			padding-bottom: 56.25%; /* 16:9 */
			height: 0;
			overflow: hidden;
			border-radius: 0.75rem;
			box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
			border: 1px solid var(--border-card);

			iframe {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
			}
		}

		// ---- Right Column (Form) ----
		.grade-form {
			display: flex;
			flex-direction: column;
			gap: 1.5rem;
		}

		// ---- Shared Card Style ----
		.card {
			padding: 1.5rem;
			border-radius: 1.5rem;
			border: 1px solid var(--border-card);
			background: var(--bg-card);
			box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
			box-sizing: border-box;
		}

		.card-title {
			font-size: 1.125rem; // text-lg
			font-weight: 700;
			margin: 0 0 0.25rem 0;
			color: var(--text-strong);
		}

		.card-desc {
			font-size: 0.875rem;
			margin: 0 0 1.5rem 0;
			color: var(--text-muted);
		}

		// ---- Criteria List ----
		.criteria-list {
			display: flex;
			flex-direction: column;
			gap: 1.5rem;
		}

		.criterion {
			padding: 1rem;
			border-radius: 0.75rem;
			border: 1px solid var(--border-card);
			background: var(--bg-criterion);
			box-sizing: border-box;
		}

		.criterion-head {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			gap: 1rem;
			margin-bottom: 1rem;

			@media (min-width: 768px) {
				flex-direction: row;
				align-items: flex-start;
			}
		}

		.criterion-name {
			font-weight: 700;
			font-size: 1rem; // text-md
			color: var(--text-criterion);
			margin: 0;
		}

		.criterion-weight {
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			margin: 0.25rem 0 0 0;
			color: var(--accent-text);
		}

		.score-input {
			width: 100%;
			flex-shrink: 0;

			@media (min-width: 768px) {
				width: 8rem;
			}
		}

		// ---- Form Controls ----
		.field-label {
			display: block;
			font-size: 0.75rem;
			font-weight: 500;
			margin-bottom: 0.25rem;
			color: var(--text-muted);
		}

		.field-required {
			color: $red-500;
			margin-left: 0.25rem;
		}

		.input {
			width: 100%;
			padding: 0.5rem 0.75rem;
			border: 1px solid var(--border-input);
			border-radius: 0.5rem;
			background: var(--bg-input);
			color: var(--text-strong);
			outline: none;
			box-sizing: border-box;

			&:focus {
				box-shadow: 0 0 0 2px #{$green-500};
			}

			&.textarea {
				resize: vertical;
				min-height: 40px;
			}
		}

		.input--error {
			border-color: $red-500 !important;
			background-color: #fef2f2 !important;

			&:focus {
				box-shadow: 0 0 0 2px #{$red-500} !important;
			}
		}

		.field-error-text {
			color: $red-500;
			font-size: 0.75rem;
			font-weight: 500;
			margin-top: 0.25rem;
		}

		// ---- Form Actions ----
		.form-actions {
			display: flex;
			justify-content: flex-end;
			padding-top: 1rem;
		}

		.btn-submit {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			padding: 0.75rem 1.5rem;
			background: $green-500;
			color: #ffffff;
			font-weight: 700;
			border: none;
			border-radius: 0.75rem;
			box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
			cursor: pointer;
			transition: all 0.15s ease;

			&:hover {
				background: $green-600;
			}

			&:disabled {
				opacity: 0.5;
				cursor: not-allowed;
			}
		}

		.btn-icon {
			width: 1.25rem;
			height: 1.25rem;
		}

		// ---- Spinner & Alerts ----
		.spinner-wrap {
			display: flex;
			justify-content: center;
			padding-block: 3rem;
		}

		.spinner {
			border-radius: 9999px;
			border: 2px solid transparent;
			animation: spin 1s linear infinite;

			&--green {
				width: 2rem;
				height: 2rem;
				border-top-color: $green-500;
				border-bottom-color: $green-500;
			}

			&--white {
				width: 1.25rem;
				height: 1.25rem;
				border-top-color: #ffffff;
				border-bottom-color: #ffffff;
			}
		}

		@keyframes spin {
			to {
				transform: rotate(360deg);
			}
		}

		.alert {
			padding: 1rem;
			border-radius: 0.75rem;
			border: 1px solid;
			box-sizing: border-box;

			&--error {
				background: $red-bg;
				color: $red-600;
				border-color: $red-border;
			}

			&--success {
				background: $green-bg;
				color: $green-700;
				border-color: $green-border;
				font-weight: 500;
			}

			&--inline {
				display: flex;
				align-items: flex-start;
				gap: 0.75rem;
				margin-bottom: 1.5rem;
			}
		}

		.alert-icon {
			width: 1.25rem;
			height: 1.25rem;
			flex-shrink: 0;
			margin-top: 0.125rem;
		}

		.alert-text {
			font-size: 0.875rem;
			font-weight: 500;
			margin: 0;
		}
	}
</style>
