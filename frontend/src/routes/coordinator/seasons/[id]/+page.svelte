<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import {
		getSeason,
		getEventsInSeason,
		createEvent,
		createTrack,
		updateEvent,
		finalizeEvent,
		getAllTracksOfEvent,
		openRegistration,
		closeRegistration
	} from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Calendar, Clock, Plus, X, Award, Edit2, Eye, Users } from "@lucide/svelte"

	// Route Param
	let seasonId = $derived($page.params.id || "")

	// Season Details State
	let season = $state<any>(null)
	let isLoadingSeason = $state(true)
	let seasonError = $state("")

	// Events State (Mock Data in LocalStorage)
	let events = $state<any[]>([])
	let showEventModal = $state(false)
	let isEventLoading = $state(false)
	let eventMessage = $state("")

	// Modal Mode: 'create' | 'edit'
	let modalMode = $state<"create" | "edit">("create")
	let editingEventId = $state<string | null>(null)

	// Create/Edit Event Form State
	let eventName = $state("")
	let eventDescription = $state("")
	let eventStartTime = $state("")
	let eventEndTime = $state("")
	let eventStatusState = $state("DRAFT")
	let eventTracks = $state<{ name: string; description: string }[]>([])

	function addTrack() {
		eventTracks = [...eventTracks, { name: "", description: "" }]
	}

	function removeTrack(index: number) {
		if (eventTracks.length > 1) {
			eventTracks = eventTracks.filter((_, i) => i !== index)
		} else {
			alert("An event must have at least one track.")
		}
	}

	// Helpers
	function formatSemester(semester: string) {
		if (!semester) return ""
		return semester.charAt(0).toUpperCase() + semester.slice(1).toLowerCase()
	}

	function formatDateTime(isoString: string) {
		if (!isoString) return "N/A"
		try {
			const d = new Date(isoString)
			return d.toLocaleString(undefined, {
				dateStyle: "medium",
				timeStyle: "short"
			})
		} catch (e) {
			return isoString
		}
	}

	async function fetchSeasonDetails() {
		try {
			isLoadingSeason = true
			seasonError = ""
			const { data, response: res } = await getSeason({ path: { seasonId }, throwOnError: false })
			if (res?.ok && data) {
				season = data
			} else {
				seasonError = `Failed to load season details (${res?.status || "Unknown"}).`
			}
		} catch (err) {
			console.error("Error loading season:", err)
			seasonError = "Cannot connect to server to load season details."
		} finally {
			isLoadingSeason = false
		}
	}

	async function loadEvents() {
		try {
			const { data, response: res } = await getEventsInSeason({
				path: { seasonId },
				throwOnError: false
			})
			if (res?.ok && data) {
				const fetchedEvents = []
				for (const eventItem of data) {
					let tracks: any[] = []
					try {
						const trackRes = await getAllTracksOfEvent({
							path: { eventId: eventItem.id },
							throwOnError: false
						})
						if (trackRes.response?.ok && trackRes.data) {
							tracks = trackRes.data
						}
					} catch (e) {
						console.error("Error loading tracks for event", eventItem.id, e)
					}
					fetchedEvents.push({
						...eventItem,
						startTime: eventItem.start_time,
						endTime: eventItem.end_time,
						tracks: tracks
					})
				}
				events = fetchedEvents
			} else {
				events = []
			}
		} catch (err) {
			console.error("Error loading events:", err)
			events = []
		}
	}

	function openCreateModal() {
		modalMode = "create"
		editingEventId = null
		eventName = ""
		eventDescription = ""
		eventStartTime = ""
		eventEndTime = ""
		eventStatusState = "DRAFT"
		eventTracks = []
		showEventModal = true
		eventMessage = ""
	}

	function openEditModal(eventItem: any) {
		modalMode = "edit"
		editingEventId = eventItem.id
		eventName = eventItem.name
		eventDescription = eventItem.description
		// YYYY-MM-DDTHH:MM datetime-local format support
		eventStartTime = eventItem.startTime ? eventItem.startTime.substring(0, 16) : ""
		eventEndTime = eventItem.endTime ? eventItem.endTime.substring(0, 16) : ""
		eventStatusState = eventItem.status || "DRAFT"
		eventTracks = eventItem.tracks ? JSON.parse(JSON.stringify(eventItem.tracks)) : []
		showEventModal = true
		eventMessage = ""
	}

	function toggleEventModal() {
		showEventModal = !showEventModal
		eventMessage = ""
	}

	async function handleSaveEvent(e: Event) {
		e.preventDefault()

		if (!eventName.trim() || !eventDescription.trim() || !eventStartTime || !eventEndTime) {
			eventMessage = "All fields marked with * are required."
			return
		}

		const start = new Date(eventStartTime)
		const end = new Date(eventEndTime)

		if (start >= end) {
			eventMessage = "Start time must be before end time."
			return
		}

		// Prompt confirmation if status is being set to FINALIZED
		if (eventStatusState === "FINALIZED") {
			const confirmed = confirm(
				"Are you sure you want to finalize this event? Once finalized, this event is locked and you will not be able to edit it again."
			)
			if (!confirmed) {
				return
			}
		}

		isEventLoading = true
		eventMessage = ""

		try {
			if (modalMode === "create") {
				const { data: createdEvent, response: createRes } = await createEvent({
					body: {
						season_id: seasonId,
						name: eventName,
						description: eventDescription,
						start_time: start.toISOString(),
						end_time: end.toISOString()
					},
					throwOnError: false
				})

				if (createRes?.ok && createdEvent) {
					// Create tracks
					if (eventTracks && eventTracks.length > 0) {
						for (const track of eventTracks) {
							if (track.name && track.name.trim() !== "") {
								await createTrack({
									path: { eventId: createdEvent.id },
									body: {
										name: track.name.trim(),
										description: track.description.trim()
									},
									throwOnError: false
								})
							}
						}
					}

					// Finalize event if selected
					if (eventStatusState === "FINALIZED") {
						await finalizeEvent({
							path: { eventId: createdEvent.id },
							throwOnError: false
						})
					}

					eventMessage = "Event created successfully!"
				} else {
					eventMessage = "Failed to create event on server."
				}
			} else {
				// Update
				const { data: updatedEvent, response: updateRes } = await updateEvent({
					path: { eventId: editingEventId || "" },
					body: {
						name: eventName,
						description: eventDescription,
						start_time: start.toISOString(),
						end_time: end.toISOString()
					},
					throwOnError: false
				})

				if (updateRes?.ok && updatedEvent) {
					if (eventStatusState === "FINALIZED" && updatedEvent.status !== "FINALIZED") {
						await finalizeEvent({
							path: { eventId: editingEventId || "" },
							throwOnError: false
						})
					}
					eventMessage = "Event updated successfully!"
				} else {
					eventMessage = "Failed to update event on server."
				}
			}

			await loadEvents()

			setTimeout(() => {
				toggleEventModal()
			}, 1000)
		} catch (err) {
			console.error(err)
			eventMessage = "Failed to save event."
		} finally {
			isEventLoading = false
		}
	}

	async function handleOpenRegistration(eventId: string) {
		try {
			const { response } = await openRegistration({
				path: { eventId },
				throwOnError: false
			})
			if (response?.ok) {
				await loadEvents()
			} else {
				alert("Failed to open registration. Ensure the event is finalized.")
			}
		} catch (err) {
			console.error(err)
			alert("An error occurred while opening registration.")
		}
	}

	async function handleCloseRegistration(eventId: string) {
		try {
			const { response } = await closeRegistration({
				path: { eventId },
				throwOnError: false
			})
			if (response?.ok) {
				await loadEvents()
			} else {
				alert("Failed to close registration.")
			}
		} catch (err) {
			console.error(err)
			alert("An error occurred while closing registration.")
		}
	}

	$effect(() => {
		if (seasonId) {
			fetchSeasonDetails()
			loadEvents()
		}
	})
</script>

<svelte:head>
	<title
		>{season ? `${formatSemester(season.semester)} ${season.year}` : "Season Detail"} - SEAL</title
	>
</svelte:head>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Navigation & Actions Header -->
	<div class="mb-6 flex items-center justify-between">
		<button
			onclick={() => goto("/coordinator/seasons")}
			class="flex items-center gap-2 bg-transparent text-sm font-semibold py-2 px-3 rounded-lg border border-(--md-outline) text-(--md-on-surface-variant) hover:bg-(--md-surface-container-high) transition-all cursor-pointer"
		>
			<ArrowLeft class="w-4 h-4" />
			Back to Seasons
		</button>
	</div>

	<!-- Loading & Error States -->
	{#if isLoadingSeason}
		<div class="flex justify-center items-center h-[50vh]">
			<div
				class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"
			></div>
		</div>
	{:else if seasonError}
		<div
			class="bg-(--md-error-container) border-l-4 border-(--md-error) p-4 rounded-r text-(--md-on-error-container)"
		>
			<h3 class="text-sm font-bold">Error loading season</h3>
			<p class="text-sm mt-1">{seasonError}</p>
		</div>
	{:else if season}
		<!-- Main Content Grid -->
		<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
			<!-- Left Column: Season Meta Card -->
			<div class="lg:col-span-1 space-y-6">
				<div
					class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
				>
					<div class="flex items-center gap-3.5 mb-6">
						<div
							class="p-3.5 rounded-2xl bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
						>
							<Calendar class="w-6 h-6" />
						</div>
						<div>
							<span class="text-xs uppercase tracking-wider font-bold text-(--md-primary)"
								>Hackathon Season</span
							>
							<h1 class="text-2xl font-extrabold tracking-tight mt-0.5 text-(--md-on-surface)">
								{formatSemester(season.semester)}
								{season.year}
							</h1>
						</div>
					</div>

					<div class="space-y-4 border-t pt-6 border-(--md-outline-variant)">
						<div class="flex justify-between items-center text-sm">
							<span class="text-(--md-on-surface-variant) font-medium">Academic Year</span>
							<span class="font-bold text-(--md-on-surface)">{season.year}</span>
						</div>
						<div class="flex justify-between items-center text-sm">
							<span class="text-(--md-on-surface-variant) font-medium">Semester term</span>
							<span class="font-bold text-(--md-on-surface)">{season.semester}</span>
						</div>
					</div>
				</div>
			</div>

			<!-- Right Column: Events Management -->
			<div class="lg:col-span-2 space-y-6">
				<div
					class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
				>
					<div
						class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4"
					>
						<div>
							<h2 class="text-xl font-bold text-(--md-on-surface)">Season Events Schedule</h2>
							<p class="text-sm mt-1 text-(--md-on-surface-variant)">
								Manage and configure the specific rounds and checkpoints in this season.
							</p>
						</div>
						<button
							id="btn-new-event"
							onclick={openCreateModal}
							class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-4 py-2 rounded-xl text-sm font-bold transition-all cursor-pointer border-0"
						>
							<Plus class="w-4 h-4" />
							Add Event
						</button>
					</div>

					<!-- Events List -->
					<div class="space-y-6">
						{#if events.length > 0}
							{#each events as event}
								<div
									class="p-6 rounded-2xl border transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-(--md-surface-container-low) hover:bg-zinc-800/5 hover:dark:bg-zinc-100/5 border-(--md-outline-variant)"
								>
									<div class="space-y-2 flex-1">
										<div class="flex items-center gap-3">
											<h3 class="text-base font-bold m-0 text-(--md-on-surface)">
												{event.name}
											</h3>
											<span
												class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {event.status ===
												'FINALIZED'
													? 'bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)'
													: 'bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-outline-variant)'}"
											>
												{event.status || "DRAFT"}
											</span>
										</div>
										<p class="text-xs m-0 leading-relaxed text-(--md-on-surface-variant)">
											{event.description}
										</p>

										{#if event.tracks && event.tracks.filter((t: any) => t.name && t.name.trim() !== "").length > 0}
											<div class="flex flex-wrap gap-1.5 mt-2">
												{#each event.tracks.filter((t: any) => t.name && t.name.trim() !== "") as track}
													<span
														class="inline-flex items-center px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider bg-(--md-surface-container-high) text-(--md-on-surface-variant) border border-(--md-outline-variant)"
														title={track.description}
													>
														{track.name}
													</span>
												{/each}
											</div>
										{/if}

										<div class="flex flex-wrap items-center gap-x-4 gap-y-1.5 pt-2">
											<div
												class="flex items-center gap-1.5 text-[11px] text-(--md-on-surface-variant)"
											>
												<Clock class="w-3.5 h-3.5 text-(--md-primary)" />
												<span>Start: {formatDateTime(event.startTime)}</span>
											</div>
											<div
												class="flex items-center gap-1.5 text-[11px] text-(--md-on-surface-variant)"
											>
												<Clock class="w-3.5 h-3.5 text-(--md-error)" />
												<span>End: {formatDateTime(event.endTime)}</span>
											</div>
										</div>
									</div>

									{#if event.status !== "FINALIZED"}
										<div class="flex items-center gap-2">
											<button
												onclick={() => openEditModal(event)}
												class="flex items-center gap-1.5 bg-transparent border border-(--md-outline) hover:bg-(--md-surface-container-highest) text-(--md-on-surface) px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer"
											>
												<Edit2 class="w-3.5 h-3.5" />
												Edit
											</button>
											<a
												href="/coordinator/seasons/{seasonId}/events/{event.id}"
												class="flex items-center gap-1.5 bg-transparent border border-(--md-outline) hover:bg-(--md-surface-container-highest) text-(--md-on-surface) px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer no-underline"
											>
												<Eye class="w-3.5 h-3.5" />
												View Details
											</a>
										</div>
									{:else}
										<div class="flex items-center gap-2">
											{#if event.open_for_registration}
												<button
													onclick={() => handleCloseRegistration(event.id)}
													class="flex items-center gap-1.5 bg-red-600 hover:bg-red-700 text-white px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer border-0"
												>
													<X class="w-3.5 h-3.5" />
													Close Registration
												</button>
											{:else}
												<button
													onclick={() => handleOpenRegistration(event.id)}
													class="flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer border-0"
												>
													<Plus class="w-3.5 h-3.5" />
													Open Registration
												</button>
											{/if}
											<a
												href="/coordinator/seasons/{seasonId}/events/{event.id}"
												class="flex items-center gap-1.5 bg-transparent border border-(--md-outline) hover:bg-(--md-surface-container-highest) text-(--md-on-surface) px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer no-underline"
											>
												<Eye class="w-3.5 h-3.5" />
												View Details
											</a>
										</div>
									{/if}
								</div>
							{/each}
						{:else}
							<div
								class="text-center py-12 border border-dashed rounded-2xl border-(--md-outline-variant) text-(--md-on-surface-variant)"
							>
								<Award class="w-12 h-12 mx-auto opacity-30 mb-3" />
								<p class="text-sm font-semibold">No events created for this season yet</p>
								<p class="text-xs mt-1">Get started by creating your first milestone event.</p>
							</div>
						{/if}
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>

<!-- Create/Edit Event Modal -->
{#if showEventModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4"
	>
		<div
			class="w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) p-8 relative transition-colors duration-300"
		>
			<button
				onclick={toggleEventModal}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-highest) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Calendar class="w-5 h-5 text-(--md-primary)" />
				{modalMode === "create" ? "Create" : "Edit"} Event
			</h3>

			<form onsubmit={handleSaveEvent} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label
						id="lbl-event-name"
						class="text-sm font-semibold text-(--md-on-surface-variant)"
						for="input-event-name">Event Name *</label
					>
					<input
						id="input-event-name"
						type="text"
						bind:value={eventName}
						required
						placeholder="Coding Round Phase"
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) placeholder-(--md-on-surface-variant) opacity-70 focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
					/>
				</div>

				<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
					<div class="space-y-1">
						<label
							id="lbl-start-time"
							class="text-sm font-semibold text-(--md-on-surface-variant)"
							for="input-start-time">Start Time *</label
						>
						<input
							id="input-start-time"
							type="datetime-local"
							bind:value={eventStartTime}
							required
							class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
						/>
					</div>
					<div class="space-y-1">
						<label
							id="lbl-end-time"
							class="text-sm font-semibold text-(--md-on-surface-variant)"
							for="input-end-time">End Time *</label
						>
						<input
							id="input-end-time"
							type="datetime-local"
							bind:value={eventEndTime}
							required
							class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
						/>
					</div>
				</div>

				<div class="space-y-1">
					<label
						id="lbl-description"
						class="text-sm font-semibold text-(--md-on-surface-variant)"
						for="textarea-description">Description *</label
					>
					<textarea
						id="textarea-description"
						bind:value={eventDescription}
						rows="3"
						required
						placeholder="Briefly describe the round milestones, guidelines, and rules..."
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all resize-none bg-(--md-surface-container-highest) text-(--md-on-surface) placeholder-(--md-on-surface-variant) opacity-70 focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
					></textarea>
				</div>

				<div class="space-y-1">
					<label
						id="lbl-status"
						class="text-sm font-semibold text-(--md-on-surface-variant)"
						for="select-status">Event Status *</label
					>
					<select
						id="select-status"
						bind:value={eventStatusState}
						required
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary) cursor-pointer"
					>
						<option value="DRAFT">DRAFT</option>
						<option value="FINALIZED">FINALIZED</option>
					</select>
				</div>

				{#if eventMessage}
					<div
						class="p-3 rounded-lg text-sm font-medium border {eventMessage.includes('successfully')
							? 'bg-(--md-secondary-container) text-(--md-on-secondary-container) border-(--md-outline-variant)'
							: 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)'}"
					>
						{eventMessage}
					</div>
				{/if}

				<div class="flex gap-3 mt-4">
					<button
						type="button"
						onclick={toggleEventModal}
						class="w-1/2 rounded-xl border border-(--md-outline) py-3 font-semibold transition-all cursor-pointer bg-transparent text-(--md-on-surface-variant) hover:bg-(--md-surface-container-highest)"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isEventLoading}
						class="w-1/2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) rounded-xl py-3 font-semibold disabled:opacity-50 transition-all cursor-pointer border-0"
					>
						{isEventLoading ? "Saving..." : modalMode === "create" ? "Create" : "Save"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
