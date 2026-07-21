<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Save, AlertCircle, Castle } from "@lucide/svelte"
	import { getAllCriteriaTemplates, gradeSubmission } from "$lib/api"
	import { goto } from "$app/navigation"

	let submissionId = $page.params.submissionId
	let isLoading = $state(true)
	let isSubmitting = $state(false)
	let errorMessage = $state("")
	let successMessage = $state("")

	let criteriaTemplate: any = $state(null)

	// Form state: map criteria_id -> { value: number, comment: string }
	let gradingData: Record<string, { value: number | null; comment: string }> = $state({})

	onMount(async () => {
		try {
			// Fetch criteria templates
			const { data: templates } = await getAllCriteriaTemplates({ throwOnError: true })

			if (templates && templates.length > 0) {
				// We use the first template as a workaround since backend doesn't link Event/Round to a specific template
				criteriaTemplate = templates[0]

				// Initialize grading form data
				if (criteriaTemplate.criteria) {
					criteriaTemplate.criteria.forEach((c: any) => {
						gradingData[c.id] = { value: null, comment: "" }
					})
				}
			} else {
				errorMessage =
					"No grading criteria templates found in the system. Please contact the coordinator."
			}
		} catch (error: any) {
			console.error("Failed to load criteria templates", error)
			errorMessage = error.message || "Failed to load grading criteria."
		} finally {
			isLoading = false
		}
	})

	// Calculate total score based on weights
	let totalScore = $derived.by(() => {
		if (!criteriaTemplate || !criteriaTemplate.criteria) return 0
		let total = 0
		for (const c of criteriaTemplate.criteria) {
			const val = gradingData[c.id]?.value || 0
			// Assuming value is out of 100, score = (value * weight) / 100
			// Wait, the API spec says `value: integer`, it doesn't specify if it's out of 100 or what.
			// Usually, if weight is percentage (e.g. 20, 30), total score = sum(value * weight) / 100
			total += (val * (c.weight || 0)) / 100
		}
		return total
	})

	// Check if any grade is below threshold (e.g., below 50) and requires a comment
	let requiresDocumentedReason = $derived.by(() => {
		if (!criteriaTemplate || !criteriaTemplate.criteria) return false
		for (const c of criteriaTemplate.criteria) {
			const val = gradingData[c.id]?.value
			const comment = gradingData[c.id]?.comment
			// BR-47: Required documented reason if score is below a certain threshold, let's say 50/100
			if (val !== null && val < 50 && (!comment || comment.trim() === "")) {
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
			if (grade.value === null) {
				errorMessage = `Please enter a score for ${c.name}`
				return
			}
			if (grade.value < 0 || grade.value > 100) {
				errorMessage = `Score for ${c.name} must be between 0 and 100`
				return
			}
		}

		if (requiresDocumentedReason) {
			errorMessage = "Please provide a comment (documented reason) for any score below 50."
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
				<p class="score-value" class:pass={totalScore >= 50} class:fail={totalScore < 50}>
					{totalScore.toFixed(1)}<span class="score-max">/100</span>
				</p>
			</div>
		{/if}
	</div>

	{#if isLoading}
		<div class="spinner-wrap">
			<div class="spinner spinner--green"></div>
		</div>
	{:else if !criteriaTemplate}
		<div class="alert alert--error">{errorMessage}</div>
	{:else}
		<form onsubmit={handleSubmit} class="grade-form">
			<div class="card">
				<h2 class="card-title">Evaluation Criteria</h2>
				<p class="card-desc">Please score each criteria from 0 to 100.</p>

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
									<label class="field-label">Score (0-100)</label>
									<input
										type="number"
										min="0"
										max="100"
										bind:value={gradingData[c.id].value}
										class="input"
										placeholder="e.g. 85"
										required
									/>
								</div>
							</div>
							<div>
								<label class="field-label">
									Comment / Feedback
									{#if gradingData[c.id].value !== null && gradingData[c.id].value < 50}
										<span class="field-required">(Required for scores below 50)</span>
									{/if}
								</label>
								<textarea
									bind:value={gradingData[c.id].comment}
									rows="2"
									class="input textarea"
									placeholder="Provide constructive feedback..."
								></textarea>
							</div>
						</div>
					{/each}
				</div>
			</div>

			<div class="form-actions">
				<button type="submit" disabled={isSubmitting} class="btn-submit">
					{#if isSubmitting}
						<div class="spinner spinner--white"></div>
						Submitting...
					{:else}
						<Save class="btn-icon" />
						Submit Grades
					{/if}
				</button>
			</div>
		</form>
	{/if}
</div>

<style lang="scss">
	// ---------- Design tokens ----------
	$max-width: 56rem; // max-w-4xl

	// Accent / status colors (theme-independent)
	$green-400: #4ade80;
	$green-500: #22c55e;
	$green-600: #16a34a;
	$green-700: #15803d;
	$red-500: #ef4444;
	$red-600: #dc2626;
	$gray-400: #9ca3af;

	// Alert palettes (theme-independent)
	$red-bg: #fef2f2; // red-50
	$red-border: #fecaca; // red-200
	$green-bg: #f0fdf4; // green-50
	$green-border: #bbf7d0; // green-200

	// ---------- Component ----------
	.grading-page {
		// max-w-4xl mx-auto w-full p-4 (md:p-8)
		max-width: $max-width;
		margin-inline: auto;
		width: 100%;
		padding: 1rem;

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
			} // shadow-sm is light-only
		}

		// ---- Back link ----
		// inline-flex items-center gap-2 transition-colors mb-6 font-medium
		// + text-gray-500/zinc-400, hover green-600/green-400
		.back-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			margin-bottom: 1.5rem;
			font-weight: 500;
			color: var(--text-muted);
			transition: color 0.15s ease;

			&:hover {
				color: var(--link-hover);
			}
		}

		.link-icon {
			width: 1rem; // w-4
			height: 1rem; // h-4
		}

		// ---- Header ----
		// flex items-center justify-between mb-8
		.page-header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 2rem;
		}

		// h1: text-2xl font-bold + text-gray-800/zinc-100
		.page-title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			color: var(--text-strong);
		}

		// p: text-sm + text-gray-500/zinc-400
		.page-subtitle {
			font-size: 0.875rem; // text-sm
			color: var(--text-muted);
		}

		// span: font-mono text-xs
		.mono {
			font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
			font-size: 0.75rem; // text-xs
		}

		// ---- Score box ----
		.score-box {
			text-align: right;
		} // text-right

		.score-label {
			font-size: 0.875rem;
			font-weight: 500;
			color: var(--text-muted);
		}

		// text-3xl font-bold + green-500/red-500
		.score-value {
			font-size: 1.875rem; // text-3xl
			font-weight: 700;

			&.pass {
				color: $green-500;
			}
			&.fail {
				color: $red-500;
			}
		}

		// text-lg text-gray-400
		.score-max {
			font-size: 1.125rem; // text-lg
			color: $gray-400;
		}

		// ---- Loading spinner ----
		// flex justify-center py-12
		.spinner-wrap {
			display: flex;
			justify-content: center;
			padding-block: 3rem; // py-12
		}

		// animate-spin rounded-full border-t-2 border-b-2
		.spinner {
			border-radius: 9999px;
			border: 2px solid transparent;
			animation: spin 1s linear infinite;

			&--green {
				width: 2rem; // h-8 / w-8
				height: 2rem;
				border-top-color: $green-500;
				border-bottom-color: $green-500;
			}

			&--white {
				width: 1.25rem; // h-5 / w-5
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

		// ---- Alerts ----
		// p-4 rounded-xl border + bg/text/border
		.alert {
			padding: 1rem;
			border-radius: 0.75rem; // rounded-xl
			border: 1px solid;

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

			// mb-6 flex items-start gap-3
			&--inline {
				display: flex;
				align-items: flex-start;
				gap: 0.75rem;
				margin-bottom: 1.5rem;
			}
		}

		.alert-icon {
			width: 1.25rem; // w-5
			height: 1.25rem; // h-5
			flex-shrink: 0; // shrink-0
			margin-top: 0.125rem; // mt-0.5
		}

		.alert-text {
			font-size: 0.875rem;
			font-weight: 500;
		}

		// ---- Form ----
		// space-y-6
		.grade-form {
			display: flex;
			flex-direction: column;
			gap: 1.5rem;
		}

		// p-6 rounded-3xl border + bg/border/shadow-sm (light only)
		.card {
			padding: 1.5rem; // p-6
			border-radius: 1.5rem; // rounded-3xl
			border: 1px solid var(--border-card);
			background: var(--bg-card);
			box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); // shadow-sm
		}

		// h2: text-lg font-bold mb-1 + text-gray-800/zinc-100
		.card-title {
			font-size: 1.125rem; // text-lg
			font-weight: 700;
			margin-bottom: 0.25rem;
			color: var(--text-strong);
		}

		// p: text-sm mb-6 + text-gray-500/zinc-400
		.card-desc {
			font-size: 0.875rem;
			margin-bottom: 1.5rem;
			color: var(--text-muted);
		}

		// space-y-6
		.criteria-list {
			display: flex;
			flex-direction: column;
			gap: 1.5rem;
		}

		// p-4 rounded-xl border + bg/border
		.criterion {
			padding: 1rem;
			border-radius: 0.75rem; // rounded-xl
			border: 1px solid var(--border-card);
			background: var(--bg-criterion);
		}

		// flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4
		.criterion-head {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			gap: 1rem;
			margin-bottom: 1rem;

			@media (min-width: 768px) {
				flex-direction: row;
				align-items: center;
			}
		}

		// font-bold text-md + text-gray-800/zinc-200
		.criterion-name {
			font-weight: 700;
			font-size: 1rem; // text-md
			color: var(--text-criterion);
		}

		// text-xs font-semibold uppercase tracking-wider mt-1 + green-600/400
		.criterion-weight {
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em; // tracking-wider
			margin-top: 0.25rem;
			color: var(--accent-text);
		}

		// w-full md:w-32 shrink-0
		.score-input {
			width: 100%;
			flex-shrink: 0;

			@media (min-width: 768px) {
				width: 8rem; // w-32
			}
		}

		// block text-xs font-medium mb-1 + text-gray-500/zinc-400
		.field-label {
			display: block;
			font-size: 0.75rem;
			font-weight: 500;
			margin-bottom: 0.25rem;
			color: var(--text-muted);
		}

		// text-red-500 ml-1
		.field-required {
			color: $red-500;
			margin-left: 0.25rem;
		}

		// w-full px-3 py-2 border rounded-lg outline-none
		// focus:ring-2 focus:ring-green-500 + bg/border/text
		.input {
			width: 100%;
			padding: 0.5rem 0.75rem; // py-2 px-3
			border: 1px solid var(--border-input);
			border-radius: 0.5rem; // rounded-lg
			background: var(--bg-input);
			color: var(--text-strong);
			outline: none;

			&:focus {
				box-shadow: 0 0 0 2px #{$green-500}; // ring-2 ring-green-500
			}

			&.textarea {
				resize: none;
			} // resize-none
		}

		// ---- Form actions / submit ----
		// flex justify-end pt-4
		.form-actions {
			display: flex;
			justify-content: flex-end;
			padding-top: 1rem; // pt-4
		}

		// flex items-center gap-2 px-6 py-3 bg-green-500 hover:bg-green-600
		// text-white font-bold rounded-xl shadow-sm transition-all disabled:opacity-50
		.btn-submit {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			padding: 0.75rem 1.5rem; // py-3 px-6
			background: $green-500;
			color: #ffffff;
			font-weight: 700;
			border: none;
			border-radius: 0.75rem; // rounded-xl
			box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); // shadow-sm
			cursor: pointer;
			transition: all 0.15s ease;

			&:hover {
				background: $green-600;
			}

			&:disabled {
				opacity: 0.5; // disabled:opacity-50
				cursor: not-allowed;
			}
		}

		.btn-icon {
			width: 1.25rem; // w-5
			height: 1.25rem; // h-5
		}
	}
</style>
