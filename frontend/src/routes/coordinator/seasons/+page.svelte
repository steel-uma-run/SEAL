<script lang="ts">
	import { onMount } from "svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import { Button, Tabs, TextFieldOutlined, TextFieldOutlinedMultiline } from "m3-svelte"
	import { ChevronLeft, ChevronRight } from "@lucide/svelte"
	import { getAllSeasons, getEventsInSeason, updateEvent, finalizeEvent } from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"
	import DateTimePicker from "$lib/components/DateTimePicker.svelte"

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
				const end = new Date(event.end_time).getTime()
				return event.status === "FINALIZED" && now > end
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

	// Edit Event State
	let showEditModal = $state(false)
	let editEventId = $state("")
	let editEventName = $state("")
	let editEventPrize = $state("")
	let editEventDescription = $state("")
	let editEventStartTime = $state<Date | undefined>(undefined)
	let editEventEndTime = $state<Date | undefined>(undefined)
	let editErrors = $state<any>({})
	let isSaving = $state(false)
	let shouldFinalize = $state(false)
	let editEventStatus = $state("")

	function openEditModal(event: any) {
		editEventId = event.id
		editEventName = event.name
		editEventDescription = event.description || ""
		editEventPrize = event.price || ""
		editEventStartTime =
			event.start_time || event.startTime
				? new Date(event.start_time || event.startTime)
				: undefined
		editEventEndTime =
			event.end_time || event.endTime ? new Date(event.end_time || event.endTime) : undefined
		editEventStatus = event.status || ""
		shouldFinalize = false
		editErrors = {}
		showEditModal = true
	}

	async function handleSaveEvent(e: Event) {
		e.preventDefault()
		editErrors = {}

		if (!editEventName.trim()) {
			editErrors.name = "Event name cannot be empty"
			return
		}
		if (!editEventStartTime) {
			editErrors.startTime = "Start time is required"
			return
		}
		if (!editEventEndTime) {
			editErrors.endTime = "End time is required"
			return
		}
		if (editEventStartTime >= editEventEndTime) {
			editErrors.startTime = "Start time must be before end time"
			return
		}

		isSaving = true
		try {
			// 1. Update event details
			const { response, error: apiErr } = await updateEvent({
				path: { eventId: editEventId },
				body: {
					name: editEventName,
					description: editEventDescription,
					price: editEventPrize,
					start_time: editEventStartTime.toISOString(),
					end_time: editEventEndTime.toISOString()
				},
				throwOnError: false
			})

			if (!response?.ok) {
				const errBody = apiErr as any
				editErrors.generic = errBody?.detail || response?.statusText || "Failed to update event"
				return
			}

			// 2. Finalize event if checked
			if (shouldFinalize) {
				const finalizeResp = await finalizeEvent({
					path: { eventId: editEventId },
					throwOnError: false
				})
				if (!finalizeResp.response?.ok) {
					const errBody = finalizeResp.error as any
					editErrors.generic =
						errBody?.detail || finalizeResp.response?.statusText || "Failed to finalize event"
					return
				}
			}

			showEditModal = false
			await loadData()
			alert(
				shouldFinalize ? "Event updated and finalized successfully!" : "Event updated successfully!"
			)
		} catch (err: any) {
			console.error("Error updating event:", err)
			editErrors.generic = err.message || "An error occurred"
		} finally {
			isSaving = false
		}
	}

	onMount(() => {
		loadData()
	})
</script>

<svelte:head>
	<title>Season Management - SEAL</title>
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
						{new Date(event.start_time).toLocaleDateString()} - {new Date(
							event.end_time
						).toLocaleDateString()}
					</span>
				</div>

				<div class="event-actions">
					<Button href="/coordinator/seasons/{event.season_id}/events/{event.id}" variant="tonal"
						>Manage</Button
					>
					<Button
						onclick={() => openEditModal(event)}
						variant="tonal"
						disabled={event.status?.toUpperCase() === "FINALIZED"}>Edit</Button
					>
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
		<div class="modal-surface">
			<div class="modal-header">
				<h3>Edit Event Information</h3>
				<button onclick={() => (showEditModal = false)} class="btn-icon">
					<svg
						style="width: 1.5rem; height: 1.5rem"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						></path>
					</svg>
				</button>
			</div>

			<form onsubmit={handleSaveEvent} class="modal-form">
				{#if editErrors.generic}
					<div class="alert alert-error">
						{editErrors.generic}
					</div>
				{/if}

				<div class="form-group">
					<p class="form-label">Event Name *</p>
					<TextFieldOutlined required minlength={1} label="" bind:value={editEventName} />
					{#if editErrors.name}
						<span class="error">{editErrors.name}</span>
					{/if}
				</div>

				<div class="form-group">
					<p class="form-label">Prize Pool</p>
					<TextFieldOutlined required minlength={1} label="" bind:value={editEventPrize} />
				</div>

				<div class="form-group">
					<p class="form-label">Registration Start *</p>
					<div class="date-picker-container">
						<DateTimePicker bind:value={editEventStartTime} />
					</div>
					{#if editErrors.startTime}
						<span class="error">{editErrors.startTime}</span>
					{/if}
				</div>

				<div class="form-group">
					<p class="form-label">Registration End *</p>
					<div class="date-picker-container">
						<DateTimePicker bind:value={editEventEndTime} />
					</div>
					{#if editErrors.endTime}
						<span class="error">{editErrors.endTime}</span>
					{/if}
				</div>

				<div class="form-group">
					<p class="form-label">Description</p>
					<TextFieldOutlinedMultiline
						required
						minlength={1}
						label=""
						bind:value={editEventDescription}
					/>
				</div>

				{#if editEventStatus?.toUpperCase() === "DRAFT"}
					<div class="form-group finalize-group">
						<label class="checkbox-label">
							<input type="checkbox" bind:checked={shouldFinalize} />
							<span>Finalize Event (Locks editing and publishes to students)</span>
						</label>
					</div>
				{/if}

				<div class="modal-actions">
					<Button type="button" variant="text" onclick={() => (showEditModal = false)}
						>Cancel</Button
					>
					<Button type="submit" disabled={isSaving}>
						{isSaving ? "Saving..." : "Save Changes"}
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
		color: var(--md-sys-color-on-surface);
	}

	.modal-surface {
		border: 1px solid var(--md-sys-color-outline-variant);
		background: var(--md-sys-color-surface-container-low);
		border-radius: 24px;
		width: 100%;
		max-width: 40rem;
		padding: 2rem;
		display: flex;
		flex-direction: column;
		max-height: 90vh;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;

		h3 {
			margin: 0;
			font-size: 1.25rem;
			font-weight: 700;
		}
	}

	.btn-icon {
		border: 0;
		background: transparent;
		cursor: pointer;
		padding: 0.25rem;
		border-radius: 0.5rem;
		color: var(--md-sys-color-on-surface-variant);
		display: flex;
		align-items: center;
		justify-content: center;

		&:hover {
			background: var(--md-sys-color-surface-container-highest);
		}
	}

	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;
		overflow-y: auto;
		flex: 1;
		padding-right: 0.25rem;
		text-align: left;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.375rem;

		.form-label {
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--md-sys-color-on-surface-variant);
			margin: 0;
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
		margin-top: 1rem;
		padding-top: 1rem;
		border-top: 1px solid var(--md-sys-color-outline-variant);
	}

	.error {
		color: var(--md-sys-color-error);
		font-size: 0.8rem;
		margin-top: 0.25rem;
	}

	.alert {
		padding: 0.75rem;
		border-radius: 0.5rem;
		font-size: 0.85rem;

		&-error {
			background: rgba(244, 63, 94, 0.1);
			color: var(--md-sys-color-error);
			border: 1px solid rgba(244, 63, 94, 0.2);
		}
	}

	.checkbox-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
		font-size: 0.9rem;
		font-weight: 500;
		color: var(--md-sys-color-primary);

		input[type="checkbox"] {
			width: 1.15rem;
			height: 1.15rem;
			cursor: pointer;
			accent-color: var(--md-sys-color-primary);
		}
	}

	.finalize-group {
		margin-top: 0.5rem;
		padding: 0.75rem;
		border: 1.5px dashed var(--md-sys-color-primary);
		border-radius: 12px;
		background: color-mix(in srgb, var(--md-sys-color-primary) 5%, transparent);
	}
</style>
