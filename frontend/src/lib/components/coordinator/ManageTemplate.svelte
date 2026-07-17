<script lang="ts">
	import { onMount } from "svelte"
	import { getAllCriteriaTemplates, createCriteriaTemplate } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import {
		Plus,
		X,
		Search,
		Trash2,
		ChevronDown,
		ChevronUp,
		ClipboardList,
		AlertCircle,
		CheckCircle2
	} from "@lucide/svelte"

	// 1. DATA LOADER & STATES
	let templatesList = $state<any[]>([])
	let isLoading = $state(true)
	let searchQuery = $state("")
	let expandedTemplateId = $state<string | null>(null)

	// Helper function to generate UUID
	function generateId() {
		if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
			return crypto.randomUUID()
		}
		return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
			const r = (Math.random() * 16) | 0
			const v = c === "x" ? r : (r & 0x3) | 0x8
			return v.toString(16)
		})
	}

	async function loadTemplates() {
		isLoading = true
		try {
			const { data, response } = await getAllCriteriaTemplates({ throwOnError: false })
			if (response?.ok && data) {
				templatesList = data
			}
		} catch (err) {
			console.error("Failed to load templates:", err)
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadTemplates()
	})

	// 2. SEARCH & FILTER
	let filteredTemplates = $derived(
		templatesList.filter((template) => {
			const q = searchQuery.trim().toLowerCase()
			if (!q) return true
			const descMatch = (template.description || "").toLowerCase().includes(q)
			const criteriaMatch =
				template.criteria &&
				template.criteria.some((c: any) => (c.name || "").toLowerCase().includes(q))
			return descMatch || criteriaMatch
		})
	)

	// 3. CREATE MODAL STATES
	let showModal = $state(false)
	let modalLoading = $state(false)
	let modalMessage = $state("")
	let modalError = $state(false)

	let templateDescription = $state("")
	interface CriteriaFormItem {
		id: string
		name: string
		weight: number
	}
	let criteriaItems = $state<CriteriaFormItem[]>([{ id: generateId(), name: "", weight: 0 }])

	// Derived calculations
	let totalWeight = $derived(
		criteriaItems.reduce((sum, item) => sum + (Number(item.weight) || 0), 0)
	)

	let isFormValid = $derived(
		templateDescription.trim().length > 0 &&
			criteriaItems.length > 0 &&
			criteriaItems.every((item) => item.name.trim().length > 0 && Number(item.weight) > 0) &&
			totalWeight === 100
	)

	function openModal() {
		templateDescription = ""
		criteriaItems = [{ id: generateId(), name: "", weight: 0 }]
		modalMessage = ""
		modalError = false
		showModal = true
	}

	function closeModal() {
		showModal = false
	}

	function addCriteriaRow() {
		criteriaItems.push({
			id: generateId(),
			name: "",
			weight: 0
		})
	}

	function removeCriteriaRow(id: string) {
		if (criteriaItems.length > 1) {
			criteriaItems = criteriaItems.filter((item) => item.id !== id)
		}
	}

	function toggleExpand(id: string) {
		expandedTemplateId = expandedTemplateId === id ? null : id
	}

	async function handleSubmit(e: Event) {
		e.preventDefault()
		if (!isFormValid) return

		modalLoading = true
		modalMessage = ""
		modalError = false

		try {
			const { response, data } = await createCriteriaTemplate({
				body: {
					description: templateDescription,
					criteria: criteriaItems.map((item) => ({
						id: item.id,
						name: item.name,
						weight: Number(item.weight)
					}))
				},
				throwOnError: false
			})

			if (response?.ok) {
				modalError = false
				modalMessage = "Template created successfully!"
				setTimeout(() => {
					showModal = false
					loadTemplates()
				}, 1500)
			} else {
				modalError = true
				modalMessage = response?.statusText || "Failed to create template."
			}
		} catch (err: any) {
			modalError = true
			modalMessage = err.message || "An unexpected error occurred."
		} finally {
			modalLoading = false
		}
	}
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full font-sans text-(--md-on-surface)">
	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Template Management
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Prepare and manage criteria templates for grading rounds.
			</p>
		</div>
		<div class="flex items-center gap-3 mt-4 md:mt-0">
			<button
				onclick={openModal}
				class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-5 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer border-0 shadow-none"
			>
				<Plus class="w-4 h-4" />
				New Template
			</button>
		</div>
	</header>

	<!-- Main Card Content -->
	<div
		class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-all shadow-none"
	>
		<!-- Search and stats bar -->
		<div
			class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6 pb-6 border-b border-(--md-outline-variant)"
		>
			<!-- Search bar -->
			<div class="relative w-full sm:w-80">
				<span
					class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-(--md-on-surface-variant)"
				>
					<Search class="w-4 h-4" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search templates or criteria..."
					class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-(--md-outline) bg-(--md-surface-container-low) text-(--md-on-surface) placeholder-(--md-on-surface-variant)/60 focus:ring-2 focus:ring-(--md-primary) outline-none transition-all text-sm shadow-none"
				/>
				{#if searchQuery}
					<button
						type="button"
						onclick={() => (searchQuery = "")}
						class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-md text-xs font-bold text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer border-0 bg-transparent"
					>
						Clear
					</button>
				{/if}
			</div>

			<!-- Stats -->
			<div class="text-xs font-semibold text-(--md-on-surface-variant)">
				Showing <strong class="text-(--md-primary) font-black">{filteredTemplates.length}</strong>
				of {templatesList.length} templates
			</div>
		</div>

		<!-- List of Templates -->
		<div class="overflow-x-auto">
			<table class="w-full text-left border-collapse min-w-[800px]">
				<thead>
					<tr
						class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
					>
						<th class="py-3.5 px-4">Template Description</th>
						<th class="py-3.5 px-4">Criteria Categories</th>
						<th class="py-3.5 px-4">Total Weight</th>
						<th class="py-3.5 px-4">Actions</th>
					</tr>
				</thead>
				<tbody class="text-sm">
					{#if isLoading}
						<tr>
							<td colspan="4" class="py-16 text-center">
								<div class="flex flex-col items-center justify-center gap-3">
									<div
										class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-(--md-primary)"
									></div>
									<span class="text-sm text-(--md-on-surface-variant)"
										>Loading templates data...</span
									>
								</div>
							</td>
						</tr>
					{:else}
						{#if filteredTemplates.length === 0}
							<tr>
								<td colspan="4" class="py-16 text-center">
									<div class="max-w-xs mx-auto space-y-2">
										<p class="text-3xl">🔍</p>
										<p class="font-bold text-(--md-on-surface)">No templates found</p>
										<p class="text-xs text-(--md-on-surface-variant)">
											We couldn't find any templates matching <strong class="text-(--md-primary)"
												>"{searchQuery}"</strong
											>.
										</p>
									</div>
								</td>
							</tr>
						{/if}
					{/if}

					{#each filteredTemplates as template (template.id)}
						{@const isExpanded = expandedTemplateId === template.id}
						<tr
							class="border-b border-(--md-outline-variant)/50 transition-colors hover:bg-(--md-surface-container-highest)/30 text-(--md-on-surface)"
						>
							<td class="py-4 px-4 font-bold">{template.description}</td>
							<td class="py-4 px-4 text-xs font-semibold text-(--md-on-surface-variant)">
								{template.criteria ? template.criteria.length : 0} categories
							</td>
							<td class="py-4 px-4">
								<span
									class="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 shadow-none"
								>
									100% Weight
								</span>
							</td>
							<td class="py-4 px-4">
								<button
									onclick={() => toggleExpand(template.id)}
									class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) hover:bg-(--md-surface-container-high) shadow-none"
								>
									{#if isExpanded}
										<ChevronUp class="w-3.5 h-3.5" /> Hide Details
									{:else}
										<ChevronDown class="w-3.5 h-3.5" /> View Details
									{/if}
								</button>
							</td>
						</tr>
						{#if isExpanded}
							<tr>
								<td
									colspan="4"
									class="py-4 px-8 bg-(--md-surface-container-low)/30 border-b border-(--md-outline-variant)/50"
								>
									<div class="overflow-x-auto">
										<table class="w-full text-left border-collapse">
											<thead>
												<tr
													class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
												>
													<th class="py-2 px-3">Criteria Category</th>
													<th class="py-2 px-3 text-right">Weight</th>
												</tr>
											</thead>
											<tbody class="text-sm">
												{#each template.criteria as item}
													<tr
														class="border-b border-(--md-outline-variant) last:border-0 text-(--md-on-surface)"
													>
														<td class="py-2.5 px-3 font-semibold">{item.name}</td>
														<td class="py-2.5 px-3 text-right font-bold text-(--md-primary)"
															>{item.weight}%</td
														>
													</tr>
												{/each}
											</tbody>
										</table>
									</div>
								</td>
							</tr>
						{/if}
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- Create Template Modal -->
{#if showModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4 font-sans text-(--md-on-surface)"
	>
		<div
			class="w-full max-w-2xl rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) p-8 relative transition-all shadow-none flex flex-col max-h-[90vh]"
		>
			<!-- Close button -->
			<button
				onclick={closeModal}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-highest) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>

			<h3 class="text-xl font-bold mb-4 flex items-center gap-2">
				<ClipboardList class="w-5 h-5 text-(--md-primary)" />
				Create Criteria Template
			</h3>

			<!-- Modal Message Banner -->
			{#if modalMessage}
				<div
					class="mb-4 p-3.5 rounded-xl text-sm flex items-start gap-2.5 border {modalError
						? 'bg-rose-500/10 text-rose-500 border-rose-500/20'
						: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20'}"
				>
					{#if modalError}
						<AlertCircle class="w-5 h-5 shrink-0" />
					{:else}
						<CheckCircle2 class="w-5 h-5 shrink-0" />
					{/if}
					<span>{modalMessage}</span>
				</div>
			{/if}

			<form onsubmit={handleSubmit} class="flex flex-col gap-4 overflow-y-auto pr-1 flex-1">
				<!-- Template Description Name -->
				<div class="space-y-1.5">
					<label
						for="template-desc-input"
						class="text-sm font-semibold text-(--md-on-surface-variant)"
						>Template Name/Description *</label
					>
					<input
						id="template-desc-input"
						type="text"
						bind:value={templateDescription}
						placeholder="e.g., Hackathon Round 1 Presentation Template"
						required
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
					/>
				</div>

				<!-- Criteria Rows Section -->
				<div class="space-y-2">
					<div class="flex justify-between items-center">
						<span class="text-sm font-semibold text-(--md-on-surface-variant)">Criteria List *</span
						>
						<button
							type="button"
							onclick={addCriteriaRow}
							class="text-xs font-bold text-(--md-primary) hover:underline bg-transparent border-0 cursor-pointer"
						>
							+ Add Row
						</button>
					</div>

					<div class="space-y-2 max-h-[40vh] overflow-y-auto pr-1">
						{#each criteriaItems as item, index (item.id)}
							<div class="flex gap-3 items-center">
								<!-- Criteria name -->
								<div class="flex-1">
									<input
										type="text"
										bind:value={item.name}
										placeholder="Criteria Category Name"
										required
										class="w-full rounded-xl border border-(--md-outline) p-2.5 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) text-sm"
									/>
								</div>

								<!-- Criteria weight -->
								<div class="w-28 flex items-center gap-1.5">
									<input
										type="number"
										bind:value={item.weight}
										placeholder="Weight"
										min="1"
										max="100"
										required
										class="w-full rounded-xl border border-(--md-outline) p-2.5 outline-none text-center bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) text-sm"
									/>
									<span class="text-sm text-(--md-on-surface-variant)">%</span>
								</div>

								<!-- Delete button -->
								<button
									type="button"
									onclick={() => removeCriteriaRow(item.id)}
									disabled={criteriaItems.length <= 1}
									class="p-2 rounded-xl hover:bg-(--md-surface-container-highest) transition-colors text-rose-500 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer border-0 bg-transparent"
								>
									<Trash2 class="w-4 h-4" />
								</button>
							</div>
						{/each}
					</div>
				</div>

				<!-- Live Weight Validation Indicator -->
				<div
					class="mt-2 p-4 rounded-2xl bg-(--md-surface-container-low) border border-(--md-outline-variant) flex justify-between items-center"
				>
					<div class="text-sm font-semibold text-(--md-on-surface)">
						Total Weight:
						<span
							class={totalWeight === 100
								? "text-emerald-500 font-bold"
								: "text-amber-500 font-bold"}
						>
							{totalWeight}%
						</span>
					</div>
					<div class="text-xs text-(--md-on-surface-variant)">
						{#if totalWeight === 100}
							<span class="text-emerald-500 font-bold flex items-center gap-1">
								✓ Weights sum to 100%
							</span>
						{:else if totalWeight < 100}
							<span>Need {100 - totalWeight}% more</span>
						{:else}
							<span class="text-rose-500 font-semibold">Exceeds 100% by {totalWeight - 100}%</span>
						{/if}
					</div>
				</div>

				<!-- Action Buttons -->
				<div class="flex justify-end gap-3 mt-4 pt-4 border-t border-(--md-outline-variant)">
					<button
						type="button"
						onclick={closeModal}
						class="px-5 py-2.5 rounded-xl text-sm font-bold bg-transparent text-(--md-on-surface-variant) hover:bg-(--md-surface-container-highest) transition-colors cursor-pointer border-0"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={!isFormValid || modalLoading}
						class="flex items-center justify-center gap-2 bg-(--md-primary) disabled:opacity-50 disabled:cursor-not-allowed text-(--md-on-primary) px-6 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer border-0"
					>
						{#if modalLoading}
							<div
								class="animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-current"
							></div>
						{/if}
						Save Template
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
