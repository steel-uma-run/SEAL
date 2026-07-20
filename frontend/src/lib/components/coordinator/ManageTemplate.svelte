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

<div class="template-management">
	<header class="page-header">
		<div>
			<h1>Template Management</h1>
			<p>Prepare and manage criteria templates for grading rounds.</p>
		</div>

		<div class="header-actions">
			<button onclick={openModal} class="primary-btn">
				<Plus class="icon-sm" />
				New Template
			</button>
		</div>
	</header>

	<div class="main-card">
		<div class="toolbar">
			<div class="search-wrap">
				<span class="search-icon">
					<Search class="icon-sm" />
				</span>

				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search templates or criteria..."
					class="search-input"
				/>

				{#if searchQuery}
					<button type="button" onclick={() => (searchQuery = "")} class="clear-btn">
						Clear
					</button>
				{/if}
			</div>

			<div class="stats">
				Showing <strong>{filteredTemplates.length}</strong> of {templatesList.length} templates
			</div>
		</div>

		<div class="table-wrap">
			<table class="templates-table">
				<thead>
					<tr>
						<th>Template Description</th>
						<th>Criteria Categories</th>
						<th>Total Weight</th>
						<th>Actions</th>
					</tr>
				</thead>

				<tbody>
					{#if isLoading}
						<tr>
							<td colspan="4" class="empty-cell">
								<div class="loading-state">
									<div class="spinner"></div>
									<span>Loading templates data...</span>
								</div>
							</td>
						</tr>
					{:else}
						{#if filteredTemplates.length === 0}
							<tr>
								<td colspan="4" class="empty-cell">
									<div class="empty-state">
										<p class="emoji">🔍</p>
										<p class="empty-title">No templates found</p>
										<p class="empty-text">
											We couldn't find any templates matching <strong>"{searchQuery}"</strong>.
										</p>
									</div>
								</td>
							</tr>
						{/if}
					{/if}

					{#each filteredTemplates as template (template.id)}
						{@const isExpanded = expandedTemplateId === template.id}
						<tr class="template-row">
							<td class="cell-bold">{template.description}</td>
							<td class="cell-muted">
								{template.criteria ? template.criteria.length : 0} categories
							</td>
							<td>
								<span class="weight-pill">100% Weight</span>
							</td>
							<td>
								<button onclick={() => toggleExpand(template.id)} class="details-btn">
									{#if isExpanded}
										<ChevronUp class="icon-xs" /> Hide Details
									{:else}
										<ChevronDown class="icon-xs" /> View Details
									{/if}
								</button>
							</td>
						</tr>

						{#if isExpanded}
							<tr>
								<td colspan="4" class="expanded-cell">
									<div class="table-wrap nested">
										<table class="nested-table">
											<thead>
												<tr>
													<th>Criteria Category</th>
													<th class="text-right">Weight</th>
												</tr>
											</thead>
											<tbody>
												{#each template.criteria as item}
													<tr>
														<td class="cell-semibold">{item.name}</td>
														<td class="text-right cell-primary">{item.weight}%</td>
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

{#if showModal}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={closeModal} class="modal-close">
				<X class="icon-md" />
			</button>

			<h3 class="modal-title">
				<ClipboardList class="icon-md icon-primary" />
				Create Criteria Template
			</h3>

			{#if modalMessage}
				<div class:modal-error={modalError} class:modal-success={!modalError} class="modal-banner">
					{#if modalError}
						<AlertCircle class="icon-md shrink-0" />
					{:else}
						<CheckCircle2 class="icon-md shrink-0" />
					{/if}
					<span>{modalMessage}</span>
				</div>
			{/if}

			<form onsubmit={handleSubmit} class="modal-form">
				<div class="field">
					<label for="template-desc-input">Template Name/Description *</label>
					<input
						id="template-desc-input"
						type="text"
						bind:value={templateDescription}
						placeholder="e.g., Hackathon Round 1 Presentation Template"
						required
						class="input"
					/>
				</div>

				<div class="field">
					<div class="field-header">
						<span>Criteria List *</span>
						<button type="button" onclick={addCriteriaRow} class="add-row-btn">+ Add Row</button>
					</div>

					<div class="criteria-list">
						{#each criteriaItems as item, index (item.id)}
							<div class="criteria-row">
								<div class="criteria-name">
									<input
										type="text"
										bind:value={item.name}
										placeholder="Criteria Category Name"
										required
										class="input small"
									/>
								</div>

								<div class="criteria-weight">
									<input
										type="number"
										bind:value={item.weight}
										placeholder="Weight"
										min="1"
										max="100"
										required
										class="input small center"
									/>
									<span>%</span>
								</div>

								<button
									type="button"
									onclick={() => removeCriteriaRow(item.id)}
									disabled={criteriaItems.length <= 1}
									class="icon-btn danger"
								>
									<Trash2 class="icon-sm" />
								</button>
							</div>
						{/each}
					</div>
				</div>

				<div class="weight-status">
					<div class="weight-total">
						Total Weight:
						<span class:good={totalWeight === 100} class:warn={totalWeight !== 100}>
							{totalWeight}%
						</span>
					</div>

					<div class="weight-note">
						{#if totalWeight === 100}
							<span class="good-text">✓ Weights sum to 100%</span>
						{:else if totalWeight < 100}
							<span>Need {100 - totalWeight}% more</span>
						{:else}
							<span class="bad-text">Exceeds 100% by {totalWeight - 100}%</span>
						{/if}
					</div>
				</div>

				<div class="modal-actions">
					<button type="button" onclick={closeModal} class="secondary-btn">Cancel</button>
					<button type="submit" disabled={!isFormValid || modalLoading} class="primary-btn">
						{#if modalLoading}
							<div class="spinner small"></div>
						{/if}
						Save Template
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<style lang="scss">
	.page-header {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		gap: 1rem;
		margin-bottom: 2rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--md-outline-variant);

		@media (min-width: 768px) {
			flex-direction: row;
			align-items: center;
		}
		h1 {
			margin: 0;
			font-size: 1.5rem;
			font-weight: 800;
			letter-spacing: -0.025em;

			@media (min-width: 768px) {
				font-size: 1.875rem;
			}
		}

		p {
			margin: 0.25rem 0 0;
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
		}
	}

	.header-actions {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-top: 1rem;

		@media (min-width: 768px) {
			margin-top: 0;
		}
	}

	.main-card,
	.modal {
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container-lowest);
		border-radius: 1.5rem;
		box-shadow: none;
		transition: all 0.2s;
	}

	.main-card {
		padding: 2rem;
	}

	.toolbar {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: center;
		gap: 1rem;
		margin-bottom: 1.5rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--md-outline-variant);

		@media (min-width: 640px) {
			flex-direction: row;
		}
	}

	.search-wrap {
		position: relative;
		width: 100%;

		@media (min-width: 640px) {
			width: 20rem;
		}
	}

	.search-icon {
		position: absolute;
		inset: 0 auto 0 0;
		padding-left: 0.75rem;
		display: flex;
		align-items: center;
		pointer-events: none;
		color: var(--md-on-surface-variant);
	}

	.search-input,
	.input {
		width: 100%;
		border: 1px solid var(--md-outline);
		background: var(--md-surface-container-low);
		color: var(--md-on-surface);
		outline: none;
		transition: all 0.2s;
	}

	.search-input {
		padding: 0.625rem 1rem 0.625rem 2.5rem;
		border-radius: 0.75rem;
		font-size: 0.875rem;
		box-shadow: none;

		&::placeholder {
			color: color-mix(in srgb, var(--md-on-surface-variant) 60%, transparent);
		}
	}

	.clear-btn,
	.add-row-btn,
	.secondary-btn,
	.primary-btn,
	.details-btn,
	.icon-btn,
	.modal-close {
		border: 0;
		background: transparent;
		cursor: pointer;
	}

	.clear-btn {
		position: absolute;
		right: 0.75rem;
		top: 50%;
		transform: translateY(-50%);
		padding: 0.25rem;
		border-radius: 0.375rem;
		font-size: 0.75rem;
		font-weight: 700;
		color: var(--md-on-surface-variant);
	}

	.stats {
		font-size: 0.75rem;
		font-weight: 600;
		color: var(--md-on-surface-variant);

		strong {
			font-weight: 900;
			color: var(--md-primary);
		}
	}

	.table-wrap {
		overflow-x: auto;

		&.nested {
			border: 0;
		}
	}

	.templates-table,
	.nested-table {
		width: 100%;
		border-collapse: collapse;
		text-align: left;
	}

	.templates-table {
		min-width: 800px;

		thead tr,
		.nested-table thead tr {
			border-bottom: 1px solid var(--md-outline-variant);
			color: var(--md-on-surface-variant);
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.08em;
		}

		th,
		td {
			padding: 0.875rem 1rem;
		}
	}

	.template-row {
		border-bottom: 1px solid color-mix(in srgb, var(--md-outline-variant) 50%, transparent);
		color: var(--md-on-surface);
		transition: background-color 0.2s;

		&:hover {
			background: color-mix(in srgb, var(--md-surface-container-highest) 30%, transparent);
		}
	}

	.cell-bold,
	.cell-semibold {
		font-weight: 700;
	}

	.cell-muted {
		font-size: 0.75rem;
		font-weight: 600;
		color: var(--md-on-surface-variant);
	}

	.weight-pill {
		display: inline-block;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		font-size: 0.75rem;
		font-weight: 600;
		background: rgba(16, 185, 129, 0.1);
		color: #10b981;
		border: 1px solid rgba(16, 185, 129, 0.2);
	}

	.details-btn,
	.primary-btn,
	.secondary-btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		border-radius: 0.75rem;
		font-size: 0.875rem;
		font-weight: 700;
		transition: all 0.2s;
	}

	.details-btn {
		padding: 0.375rem 0.75rem;
		font-size: 0.75rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container-low);
		color: var(--md-on-surface);

		&:hover {
			background: var(--md-surface-container-high);
		}
	}

	.expanded-cell {
		padding: 1rem 2rem;
		background: color-mix(in srgb, var(--md-surface-container-low) 30%, transparent);
		border-bottom: 1px solid color-mix(in srgb, var(--md-outline-variant) 50%, transparent);
	}

	.nested-table {
		thead tr {
			font-size: 0.75rem;
		}
		td,
		th {
			padding: 0.625rem 0.75rem;
			border-bottom: 1px solid var(--md-outline-variant);
		}
		tr:last-child td {
			border-bottom: 0;
		}
	}

	.text-right {
		text-align: right;
	}

	.cell-primary {
		font-weight: 700;
		color: var(--md-primary);
	}

	.empty-cell {
		padding: 4rem 1rem;
		text-align: center;
	}

	.loading-state,
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 0.75rem;
	}

	.empty-state {
		max-width: 20rem;
		margin: 0 auto;
		gap: 0.5rem;
	}

	.emoji {
		font-size: 1.875rem;
		margin: 0;
	}

	.empty-title {
		font-weight: 700;
		margin: 0;
	}

	.empty-text,
	.loading-state span {
		font-size: 0.875rem;
		color: var(--md-on-surface-variant);
		margin: 0;
	}

	.modal-overlay {
		position: fixed;
		inset: 0;
		z-index: 2000;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 1rem;
		background: rgba(0, 0, 0, 0.4);
		backdrop-filter: blur(2px);
		font-family: sans-serif;
		color: var(--md-on-surface);
	}

	.modal {
		position: relative;
		width: 100%;
		max-width: 42rem;
		padding: 2rem;
		display: flex;
		flex-direction: column;
		max-height: 90vh;
		background: var(--md-surface-container-low);
	}

	.modal-close {
		position: absolute;
		top: 1rem;
		right: 1rem;
		padding: 0.25rem;
		border-radius: 0.5rem;
		color: var(--md-on-surface-variant);

		&:hover {
			background: var(--md-surface-container-highest);
		}
	}

	.modal-title {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin: 0 0 1rem;
		font-size: 1.25rem;
		font-weight: 700;
	}

	.icon-sm {
		width: 1rem;
		height: 1rem;
	}

	.icon-md {
		width: 1.25rem;
		height: 1.25rem;
	}

	.icon-xs {
		width: 0.875rem;
		height: 0.875rem;
	}

	.icon-primary {
		color: var(--md-primary);
	}

	.modal-banner {
		display: flex;
		align-items: flex-start;
		gap: 0.625rem;
		margin-bottom: 1rem;
		padding: 0.875rem;
		border: 1px solid;
		border-radius: 0.75rem;
		font-size: 0.875rem;
	}

	.modal-error {
		background: rgba(244, 63, 94, 0.1);
		color: #f43f5e;
		border-color: rgba(244, 63, 94, 0.2);
	}

	.modal-success {
		background: rgba(16, 185, 129, 0.1);
		color: #10b981;
		border-color: rgba(16, 185, 129, 0.2);
	}

	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		overflow-y: auto;
		padding-right: 0.25rem;
		flex: 1;
	}

	.field {
		display: grid;
		gap: 0.375rem;

		label,
		span {
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--md-on-surface-variant);
		}
	}

	.field-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.add-row-btn {
		font-size: 0.75rem;
		font-weight: 700;
		color: var(--md-primary);

		&:hover {
			text-decoration: underline;
		}
	}

	.input {
		padding: 0.75rem;
		border-radius: 0.75rem;
		background: var(--md-surface-container-highest);

		&.small {
			padding: 0.625rem;
			font-size: 0.875rem;
		}

		&.center {
			text-align: center;
		}
	}

	.criteria-list {
		display: grid;
		gap: 0.5rem;
		max-height: 40vh;
		overflow-y: auto;
		padding-right: 0.25rem;
	}

	.criteria-row {
		display: flex;
		align-items: center;
		gap: 0.75rem;
	}

	.criteria-name {
		flex: 1;
	}

	.criteria-weight {
		width: 7rem;
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	.icon-btn {
		padding: 0.5rem;
		border-radius: 0.75rem;
		background: transparent;

		&:hover {
			background: var(--md-surface-container-highest);
		}
		&.danger {
			color: #f43f5e;
		}
		&:disabled {
			opacity: 0.3;
			cursor: not-allowed;
		}
	}

	.weight-status {
		margin-top: 0.5rem;
		padding: 1rem;
		border-radius: 1rem;
		background: var(--md-surface-container-low);
		border: 1px solid var(--md-outline-variant);
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.weight-total {
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--md-on-surface);
	}

	.good {
		color: #10b981;
		font-weight: 700;
	}

	.warn {
		color: #f59e0b;
		font-weight: 700;
	}

	.good-text {
		color: #10b981;
		font-weight: 700;
	}

	.bad-text {
		color: #f43f5e;
		font-weight: 600;
	}

	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1rem;
		padding-top: 1rem;
		border-top: 1px solid var(--md-outline-variant);
	}

	.secondary-btn {
		padding: 0.625rem 1.25rem;
		color: var(--md-on-surface-variant);

		&:hover {
			background: var(--md-surface-container-highest);
		}
	}

	.primary-btn {
		padding: 0.625rem 1.5rem;
		background: var(--md-primary);
		color: var(--md-on-primary);
		border: 0;
		box-shadow: none;

		&:hover {
			opacity: 0.9;
		}

		&:disabled {
			opacity: 0.5;
			cursor: not-allowed;
		}
	}

	.spinner {
		width: 2.5rem;
		height: 2.5rem;
		border-radius: 9999px;
		border-top: 2px solid var(--md-primary);
		border-bottom: 2px solid var(--md-primary);
		animation: spin 1s linear infinite;

		&.small {
			width: 1rem;
			height: 1rem;
			border-color: currentColor;
			border-top-width: 2px;
			border-bottom-width: 2px;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
