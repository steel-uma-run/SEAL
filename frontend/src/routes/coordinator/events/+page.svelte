<script lang="ts">
	import { onMount } from "svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import { Button, Tabs, TextFieldOutlined, TextFieldOutlinedMultiline } from "m3-svelte"
	import { ChevronLeft, ChevronRight, Pencil, X } from "@lucide/svelte"
	import { getAllSeasons, getEventsInSeason, updateEvent, finalizeEvent } from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let seasons = $state<any[]>([])
	let events = $state<any[]>([])
	let isLoading = $state(true)
	let activeTab = $state("all")

	async function loadData() {
		try {
			const seasonsResp = await getAllSeasons({ throwOnError: false })
			if (seasonsResp.response?.ok && seasonsResp.data) {
				seasons = seasonsResp.data

				let tempEvents: any[] = []
				for (const season of seasons) {
					const eventsResp = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsResp.response?.ok && eventsResp.data) {
						eventsResp.data.forEach((evt) => {
							tempEvents.push({
								...evt,
								seasonSemester: season.semester,
								seasonYear: season.year
							})
						})
					}
				}
				events = tempEvents
			}
		} catch (err) {
			console.error("Error loading events:", err)
		} finally {
			isLoading = false
		}
	}

	const filteredEvents = $derived.by(() => {
		const now = Date.now()
		return events.filter((event) => {
			if (activeTab === "all") return true
			if (activeTab === "draft") return event.status === "DRAFT"

			if (activeTab === "ongoing") {
				const currentSeason = getCurrentSeasonInfo()
				return (
					event.seasonSemester === currentSeason.semester && event.seasonYear === currentSeason.year
				)
			}

			if (activeTab === "completed") {
				const startIso = event.registration_start_time || event.start_time
				const startMs = startIso ? new Date(startIso).getTime() : 0
				const duration = event.registration_duration || 0
				const endMs = event.end_time
					? new Date(event.end_time).getTime()
					: startMs
						? startMs + duration
						: 0
				return event.status === "FINALIZED" && endMs > 0 && now > endMs
			}

			return true
		})
	})

	let currentPage = $state(0)
	const pageSize = 6

	const paginatedEvents = $derived.by(() => {
		const start = currentPage * pageSize
		return filteredEvents.slice(start, start + pageSize)
	})

	$effect(() => {
		activeTab
		currentPage = 0
	})

	let showEditModal = $state(false)
	let editingEventId = $state<string | null>(null)
	let editName = $state("")
	let editDescription = $state("")
	let editPrize = $state("")
	let isSaving = $state(false)
	let isFinalizing = $state(false)
	let modalError = $state("")

	function openEditModal(event: any) {
		editingEventId = event.id
		editName = event.name
		editDescription = event.description
		editPrize = event.prize || ""
		modalError = ""
		showEditModal = true
	}

	async function handleSaveEvent() {
		if (!editingEventId) return
		isSaving = true
		modalError = ""

		try {
			if (editName.trim().length === 0) {
				modalError = "Event name cannot be empty."
				isSaving = false
				return
			}

			const updateResp = await updateEvent({
				path: { eventId: editingEventId },
				body: {
					name: editName,
					description: editDescription,
					prize: editPrize
				},
				throwOnError: false
			})

			if (!updateResp.response?.ok) {
				throw new Error(updateResp.data?.detail || "Failed to update event details.")
			}

			await loadData()
			showEditModal = false
		} catch (err: any) {
			console.error("Error saving event:", err)
			modalError = err.message || "An unexpected error occurred."
		} finally {
			isSaving = false
		}
	}

	async function handleFinalizeEvent() {
		if (!editingEventId) return
		isFinalizing = true
		modalError = ""

		try {
			const finalizeResp = await finalizeEvent({
				path: { eventId: editingEventId },
				throwOnError: false
			})
			if (!finalizeResp.response?.ok) {
				throw new Error(finalizeResp.data?.detail || "Failed to finalize event.")
			}

			await loadData()
			showEditModal = false
		} catch (err: any) {
			console.error("Error finalizing event:", err)
			modalError = err.message || "An unexpected error occurred."
		} finally {
			isFinalizing = false
		}
	}

	onMount(() => {
		loadData()
	})
</script>

<svelte:head>
	<title>Events Management - SEAL</title>
</svelte:head>

<div class="header">
	<div>
		<h1>Events management</h1>
		<p>Manage all hackathon events, timelines, tracks, and configurations.</p>
	</div>

	<div>
		<Button href="/coordinator/events/create">Create event</Button>
	</div>
</div>

<Tabs
	bind:tab={activeTab}
	items={[
		{ value: "all", name: "All" },
		{ value: "ongoing", name: "Ongoing" },
		{ value: "draft", name: "Draft" },
		{ value: "completed", name: "Completed" }
	]}
/>

{#if isLoading}
	<div class="loading-state">
		<div class="loading-state__spinner"></div>
	</div>
{:else if filteredEvents.length === 0}
	<div class="kao">
		<KaomojiError kind="neutral" text="No events found." />
	</div>
{:else}
	<div class="event-grid">
		{#each paginatedEvents as event}
			<div class="event-card">
				<div class="event-card__header">
					<span class="badge {event.status.toLowerCase()}">{event.status}</span>
					<span class="season-label">{event.seasonSemester} {event.seasonYear}</span>
				</div>
				<h3 class="event-name">{event.name}</h3>
				<p class="event-desc">{event.description}</p>

				<div class="event-time">
					<span class="time-label">Registration Window:</span>
					<span class="time-val">
						{#if event.registration_start_time || event.start_time}
							{@const startMs = new Date(
								event.registration_start_time || event.start_time
							).getTime()}
							{@const endMs = event.end_time
								? new Date(event.end_time).getTime()
								: startMs + (event.registration_duration || 0)}
							{new Date(startMs).toLocaleDateString()} - {new Date(endMs).toLocaleDateString()}
						{:else}
							Not scheduled
						{/if}
					</span>
				</div>
				<div class="event-actions">
					<Button href="/coordinator/events/{event.id}" variant="tonal">Manage</Button>
					{#if event.status === "DRAFT"}
						<Button onclick={() => openEditModal(event)} variant="tonal">
							<Pencil style="width: 1rem; height: 1rem; margin-right: 0.25rem;" />
							Edit
						</Button>
					{/if}
					<Button href="/events/{event.id}" variant="text">Public Page</Button>
				</div>
			</div>
		{/each}
	</div>

	{#if filteredEvents.length > pageSize}
		<div class="pagination">
			<Button variant="tonal" disabled={currentPage === 0} onclick={() => currentPage--}>
				<ChevronLeft style="width: 1.25rem; height: 1.25rem" />
			</Button>

			<span class="pagination__info">
				Page {currentPage + 1} of {Math.ceil(filteredEvents.length / pageSize)}
			</span>

			<Button
				variant="tonal"
				disabled={(currentPage + 1) * pageSize >= filteredEvents.length}
				onclick={() => currentPage++}
			>
				<ChevronRight style="width: 1.25rem; height: 1.25rem" />
			</Button>
		</div>
	{/if}
{/if}

{#if showEditModal}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={() => (showEditModal = false)} class="modal-close">
				<X style="width: 1.5rem; height: 1.5rem" />
			</button>

			<h3 class="modal-title">Edit Event</h3>

			{#if modalError}
				<div class="modal-banner modal-error">
					<span>{modalError}</span>
				</div>
			{/if}

			<form
				onsubmit={(e) => {
					e.preventDefault()
					handleSaveEvent()
				}}
				class="modal-form"
			>
				<div class="field">
					<label for="event-name-input">Event Name *</label>
					<TextFieldOutlined id="event-name-input" required bind:value={editName} />
				</div>

				<div class="field">
					<label for="event-prize-input">Prize Pool *</label>
					<TextFieldOutlinedMultiline id="event-prize-input" required bind:value={editPrize} />
				</div>

				<div class="field">
					<label for="event-desc-input">Description *</label>
					<TextFieldOutlinedMultiline id="event-desc-input" required bind:value={editDescription} />
				</div>

				<div class="modal-actions">
					<Button
						type="button"
						variant="tonal"
						disabled={isFinalizing || isSaving}
						onclick={handleFinalizeEvent}
					>
						{#if isFinalizing}Finalizing...{:else}Finalize Event{/if}
					</Button>
					<Button type="button" variant="text" onclick={() => (showEditModal = false)}
						>Cancel</Button
					>
					<Button type="submit" disabled={isSaving || isFinalizing}>
						{#if isSaving}Saving...{:else}Save Changes{/if}
					</Button>
				</div>
			</form>
		</div>
	</div>
{/if}

<style lang="scss">
	* {
		margin: 0;
	}

	.header {
		display: flex;
		justify-content: space-between;
		margin-bottom: 2rem;
	}

	.kao {
		margin-top: 2rem;
	}

	.event-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 1.5rem;
		margin-top: 2rem;

		@media (max-width: 1024px) {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		@media (max-width: 640px) {
			grid-template-columns: minmax(0, 1fr);
		}
	}

	.pagination {
		display: flex;
		justify-content: center;
		align-items: center;
		gap: 1.5rem;
		margin-top: 2rem;
		margin-bottom: 2rem;

		&__info {
			font-size: 0.9rem;
			font-weight: 500;
			color: var(--md-sys-color-on-surface-variant);
		}
	}

	.event-card {
		background-color: var(--md-sys-color-surface-container);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 16px;
		padding: 1.5rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;
		min-width: 0;
		transition:
			transform 0.2s,
			box-shadow 0.2s;

		&:hover {
			transform: translateY(-2px);
			box-shadow: var(--md-sys-elevation-1);
		}

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: center;
		}
	}

	.event-name {
		font-size: 1.25rem;
		font-weight: 700;
		color: var(--md-sys-color-on-surface);
		word-wrap: break-word;
		overflow-wrap: break-word;
	}

	.event-desc {
		font-size: 0.9rem;
		color: var(--md-sys-color-on-surface-variant);
		flex-grow: 1;
		line-height: 1.4;
		display: -webkit-box;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
		overflow: hidden;
		word-wrap: break-word;
		overflow-wrap: break-word;
	}

	.event-time {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
		font-size: 0.85rem;
		border-top: 1px solid var(--md-sys-color-outline-variant);
		padding-top: 0.75rem;

		.time-label {
			font-weight: 600;
			color: var(--md-sys-color-on-surface-variant);
		}

		.time-val {
			color: var(--md-sys-color-primary);
			font-weight: 500;
		}
	}

	.event-actions {
		display: flex;
		gap: 0.75rem;
		margin-top: 0.5rem;
	}

	.badge {
		padding: 0.25rem 0.75rem;
		border-radius: 999px;
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;

		&.draft {
			background-color: var(--md-sys-color-secondary-container);
			color: var(--md-sys-color-on-secondary-container);
		}

		&.finalized {
			background-color: var(--md-sys-color-primary-container);
			color: var(--md-sys-color-on-primary-container);
		}
	}

	.season-label {
		font-size: 0.85rem;
		font-weight: 600;
		color: var(--md-sys-color-tertiary);
	}

	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		min-height: 200px;

		&__spinner {
			width: 2.5rem;
			height: 2.5rem;
			border-radius: 9999px;
			border-top: 2px solid var(--md-sys-color-primary);
			border-bottom: 2px solid var(--md-sys-color-primary);
			animation: spin 1s linear infinite;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
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
		font-family: Inter, sans-serif;
	}

	.modal {
		position: relative;
		width: 100%;
		max-width: 36rem;
		padding: 2rem;
		display: flex;
		flex-direction: column;
		max-height: 90vh;
		background: var(--md-sys-color-surface-container);
		border-radius: 16px;
		border: 1px solid var(--md-sys-color-outline-variant);
		box-shadow: var(--md-sys-elevation-3);
		color: var(--md-sys-color-on-surface);
	}

	.modal-close {
		position: absolute;
		top: 1rem;
		right: 1rem;
		padding: 0.25rem;
		border-radius: 0.5rem;
		color: var(--md-sys-color-on-surface-variant);
		border: 0;
		background: transparent;
		cursor: pointer;

		&:hover {
			background: var(--md-sys-color-surface-container-highest);
		}
	}

	.modal-title {
		margin-top: 0;
		margin-bottom: 1.5rem;
		font-size: 1.5rem;
		font-weight: 700;
		color: var(--md-sys-color-primary);
	}

	.modal-banner {
		padding: 0.75rem 1rem;
		border-radius: 8px;
		font-size: 0.875rem;
		margin-bottom: 1.25rem;
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-weight: 500;

		&.modal-error {
			background-color: var(--md-sys-color-error-container);
			color: var(--md-sys-color-on-error-container);
			border: 1px solid var(--md-sys-color-error);
		}
	}

	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;
		overflow-y: auto;
		padding-right: 0.5rem;
	}

	.field {
		display: flex;
		flex-direction: column;
		gap: 0.375rem;

		label {
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--md-sys-color-on-surface-variant);
		}
	}

	.date-picker-container {
		display: flex;
		gap: 1rem;
	}

	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		margin-top: 1.5rem;
		padding-top: 1.25rem;
		border-top: 1px dashed var(--md-sys-color-outline-variant);
	}
</style>
