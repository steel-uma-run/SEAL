<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Save, AlertCircle } from "lucide-svelte"
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
				errorMessage = "No grading criteria templates found in the system. Please contact the coordinator."
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
				const errorDetails = error || await response?.json().catch(() => null)
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

<div class="max-w-4xl mx-auto w-full p-4 md:p-8">
	<a
		href="/lecturer/grading"
		class="inline-flex items-center gap-2 transition-colors mb-6 font-medium {theme.darkMode ? 'text-zinc-400 hover:text-green-400' : 'text-gray-500 hover:text-green-600'}"
	>
		<ArrowLeft class="w-4 h-4" />
		Back to Grading List
	</a>

	<div class="flex items-center justify-between mb-8">
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Grade Submission</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				ID: <span class="font-mono text-xs">{submissionId}</span>
			</p>
		</div>
		
		{#if criteriaTemplate}
			<div class="text-right">
				<p class="text-sm font-medium {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Total Score</p>
				<p class="text-3xl font-bold {totalScore >= 50 ? 'text-green-500' : 'text-red-500'}">
					{totalScore.toFixed(1)}<span class="text-lg text-gray-400">/100</span>
				</p>
			</div>
		{/if}
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-green-500"></div>
		</div>
	{:else if !criteriaTemplate}
		<div class="p-4 bg-red-50 text-red-600 rounded-xl border border-red-200">
			{errorMessage}
		</div>
	{:else}
		<form onsubmit={handleSubmit} class="space-y-6">
			<div class="p-6 rounded-3xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-200 shadow-sm'}">
				<h2 class="text-lg font-bold mb-1 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					Evaluation Criteria
				</h2>
				<p class="text-sm mb-6 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
					Please score each criteria from 0 to 100.
				</p>
				
				{#if errorMessage}
					<div class="mb-6 p-4 bg-red-50 text-red-600 rounded-xl border border-red-200 flex items-start gap-3">
						<AlertCircle class="w-5 h-5 shrink-0 mt-0.5" />
						<p class="text-sm font-medium">{errorMessage}</p>
					</div>
				{/if}

				{#if successMessage}
					<div class="mb-6 p-4 bg-green-50 text-green-700 rounded-xl border border-green-200 font-medium">
						{successMessage}
					</div>
				{/if}

				<div class="space-y-6">
					{#each criteriaTemplate.criteria as c}
						<div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800' : 'bg-gray-50 border-gray-200'}">
							<div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
								<div>
									<h3 class="font-bold text-md {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
										{c.name}
									</h3>
									<p class="text-xs font-semibold uppercase tracking-wider mt-1 {theme.darkMode ? 'text-green-400' : 'text-green-600'}">
										Weight: {c.weight}%
									</p>
								</div>
								<div class="w-full md:w-32 shrink-0">
									<label class="block text-xs font-medium mb-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Score (0-100)</label>
									<input 
										type="number" 
										min="0" 
										max="100" 
										bind:value={gradingData[c.id].value}
										class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-green-500 outline-none {theme.darkMode ? 'bg-zinc-900 border-zinc-700 text-zinc-100' : 'bg-white border-gray-300'}"
										placeholder="e.g. 85"
										required
									/>
								</div>
							</div>
							
							<div>
								<label class="block text-xs font-medium mb-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
									Comment / Feedback
									{#if gradingData[c.id].value !== null && gradingData[c.id].value < 50}
										<span class="text-red-500 ml-1">(Required for scores below 50)</span>
									{/if}
								</label>
								<textarea 
									bind:value={gradingData[c.id].comment}
									rows="2"
									class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-green-500 outline-none resize-none {theme.darkMode ? 'bg-zinc-900 border-zinc-700 text-zinc-100' : 'bg-white border-gray-300'}"
									placeholder="Provide constructive feedback..."
								></textarea>
							</div>
						</div>
					{/each}
				</div>
			</div>

			<div class="flex justify-end pt-4">
				<button
					type="submit"
					disabled={isSubmitting}
					class="flex items-center gap-2 px-6 py-3 bg-green-500 hover:bg-green-600 text-white font-bold rounded-xl shadow-sm transition-all disabled:opacity-50"
				>
					{#if isSubmitting}
						<div class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></div>
						Submitting...
					{:else}
						<Save class="w-5 h-5" />
						Submit Grades
					{/if}
				</button>
			</div>
		</form>
	{/if}
</div>
