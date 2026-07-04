<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import { getSeason } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Calendar, Clock, Plus, X, Award, Edit2 } from "@lucide/svelte"

	// Route Param
	const seasonId = $page.params.id || ""

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

	function loadEvents() {
		if (typeof window !== "undefined") {
			const key = `events_${seasonId}`
			const stored = localStorage.getItem(key)
			if (stored) {
				events = JSON.parse(stored)
			} else {
				events = []
				localStorage.setItem(key, JSON.stringify(events))
			}
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
		eventTracks = [
			{ name: "", description: "" },
			{ name: "", description: "" }
		]
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

	function handleSaveEvent(e: Event) {
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

		// Validate tracks
		for (let i = 0; i < eventTracks.length; i++) {
			if (!eventTracks[i].name.trim() || !eventTracks[i].description.trim()) {
				eventMessage = `Track ${i + 1} name and description cannot be empty.`
				return
			}
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
				const newEvent = {
					id: crypto.randomUUID(),
					name: eventName,
					description: eventDescription,
					startTime: start.toISOString(),
					endTime: end.toISOString(),
					status: eventStatusState,
					tracks: eventTracks
				}
				events = [...events, newEvent]
			} else {
				events = events.map((evt) => {
					if (evt.id === editingEventId) {
						return {
							...evt,
							name: eventName,
							description: eventDescription,
							startTime: start.toISOString(),
							endTime: end.toISOString(),
							status: eventStatusState,
							tracks: eventTracks
						}
					}
					return evt
				})
			}

			if (typeof window !== "undefined") {
				localStorage.setItem(`events_${seasonId}`, JSON.stringify(events))
			}

			eventMessage = `Event ${modalMode === "create" ? "created" : "updated"} successfully!`
			setTimeout(() => {
				toggleEventModal()
			}, 1000)
		} catch (err) {
			eventMessage = "Failed to save event."
		} finally {
			isEventLoading = false
		}
	}

	onMount(() => {
		fetchSeasonDetails()
		loadEvents()
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
			class="flex items-center gap-2 bg-transparent text-sm font-semibold py-2 px-3 rounded-lg border transition-all cursor-pointer {theme.darkMode
				? 'border-zinc-800 text-zinc-300 hover:bg-zinc-800/30'
				: 'border-gray-200 text-gray-700 hover:bg-gray-50'}"
		>
			<ArrowLeft class="w-4 h-4" />
			Back to Seasons
		</button>
	</div>

	<!-- Loading & Error States -->
	{#if isLoadingSeason}
		<div class="flex justify-center items-center h-[50vh]">
			<div
				class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"
			></div>
		</div>
	{:else if seasonError}
		<div class="bg-red-50 dark:bg-red-950/20 border-l-4 border-red-500 p-4 rounded-r shadow-sm">
			<h3 class="text-sm font-semibold text-red-800 dark:text-red-400">Error loading season</h3>
			<p class="text-sm text-red-700 dark:text-red-300 mt-1">{seasonError}</p>
		</div>
	{:else if season}
		<!-- Main Content Grid -->
		<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
			<!-- Left Column: Season Meta Card -->
			<div class="lg:col-span-1 space-y-6">
				<div
					class="p-8 rounded-3xl border transition-all {theme.darkMode
						? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
						: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
				>
					<div class="flex items-center gap-3.5 mb-6">
						<div class="p-3.5 rounded-2xl bg-orange-500/10 text-orange-500">
							<Calendar class="w-6 h-6" />
						</div>
						<div>
							<span class="text-xs uppercase tracking-wider font-bold text-orange-500"
								>Hackathon Season</span
							>
							<h1
								class="text-2xl font-extrabold tracking-tight mt-0.5 {theme.darkMode
									? 'text-zinc-100'
									: 'text-gray-800'}"
							>
								{formatSemester(season.semester)}
								{season.year}
							</h1>
						</div>
					</div>

					<div
						class="space-y-4 border-t pt-6 {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}"
					>
						<div class="flex justify-between items-center text-sm">
							<span class="{theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} font-medium"
								>Academic Year</span
							>
							<span class="font-bold {theme.darkMode ? 'text-zinc-200' : 'text-gray-700'}"
								>{season.year}</span
							>
						</div>
						<div class="flex justify-between items-center text-sm">
							<span class="{theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} font-medium"
								>Semester term</span
							>
							<span class="font-bold {theme.darkMode ? 'text-zinc-200' : 'text-gray-700'}"
								>{season.semester}</span
							>
						</div>
					</div>
				</div>
			</div>

			<!-- Right Column: Events Management -->
			<div class="lg:col-span-2 space-y-6">
				<div
					class="p-8 rounded-3xl border transition-all {theme.darkMode
						? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
						: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
				>
					<div
						class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4"
					>
						<div>
							<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
								Season Events Schedule
							</h2>
							<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
								Manage and configure the specific rounds and checkpoints in this season.
							</p>
						</div>
						<button
							id="btn-new-event"
							onclick={openCreateModal}
							class="flex items-center gap-2 bg-[#f26f21] hover:bg-[#d85c14] text-white px-4 py-2 rounded-xl text-sm font-bold shadow-sm transition-all cursor-pointer border-0"
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
									class="p-6 rounded-2xl border transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4 {theme.darkMode
										? 'bg-zinc-950/40 border-zinc-800 hover:border-zinc-700'
										: 'bg-gray-50/50 border-gray-100 hover:border-gray-200'}"
								>
									<div class="space-y-2 flex-1">
										<div class="flex items-center gap-3">
											<h3
												class="text-base font-bold m-0 {theme.darkMode
													? 'text-zinc-100'
													: 'text-gray-800'}"
											>
												{event.name}
											</h3>
											<span
												class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {event.status ===
												'FINALIZED'
													? 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
													: 'bg-green-500/10 text-green-500 border border-green-500/20'}"
											>
												{event.status || "DRAFT"}
											</span>
										</div>
										<p
											class="text-xs m-0 leading-relaxed {theme.darkMode
												? 'text-zinc-400'
												: 'text-gray-500'}"
										>
											{event.description}
										</p>

										{#if event.tracks && event.tracks.length > 0}
											<div class="flex flex-wrap gap-1.5 mt-2">
												{#each event.tracks as track}
													<span
														class="inline-flex items-center px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider {theme.darkMode
															? 'bg-zinc-800 text-zinc-300 border border-zinc-700/50'
															: 'bg-zinc-100/80 text-zinc-700 border border-zinc-200/50'}"
														title={track.description}
													>
														{track.name}
													</span>
												{/each}
											</div>
										{/if}

										<div class="flex flex-wrap items-center gap-x-4 gap-y-1.5 pt-2">
											<div
												class="flex items-center gap-1.5 text-[11px] {theme.darkMode
													? 'text-zinc-500'
													: 'text-gray-400'}"
											>
												<Clock class="w-3.5 h-3.5 text-orange-500/80" />
												<span>Start: {formatDateTime(event.startTime)}</span>
											</div>
											<div
												class="flex items-center gap-1.5 text-[11px] {theme.darkMode
													? 'text-zinc-500'
													: 'text-gray-400'}"
											>
												<Clock class="w-3.5 h-3.5 text-red-500/80" />
												<span>End: {formatDateTime(event.endTime)}</span>
											</div>
										</div>
									</div>

									{#if event.status !== "FINALIZED"}
										<button
											onclick={() => openEditModal(event)}
											class="flex items-center gap-1.5 bg-transparent border border-orange-500 hover:bg-orange-500 text-orange-500 hover:text-white px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer"
										>
											<Edit2 class="w-3.5 h-3.5" />
											Edit
										</button>
									{/if}
								</div>
							{/each}
						{:else}
							<div
								class="text-center py-12 border border-dashed rounded-2xl {theme.darkMode
									? 'border-zinc-800 text-zinc-500'
									: 'border-gray-200 text-gray-400'}"
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
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
	>
		<div
			class="w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 text-zinc-100'
				: 'bg-white border-gray-100 text-gray-800'}"
		>
			<button
				onclick={toggleEventModal}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Calendar class="w-5 h-5 text-orange-500" />
				{modalMode === "create" ? "Create" : "Edit"} Event
			</h3>

			<form onsubmit={handleSaveEvent} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label
						id="lbl-event-name"
						class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						for="input-event-name">Event Name *</label
					>
					<input
						id="input-event-name"
						type="text"
						bind:value={eventName}
						required
						placeholder="Coding Round Phase"
						class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
							? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500'
							: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
					/>
				</div>

				<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
					<div class="space-y-1">
						<label
							id="lbl-start-time"
							class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
							for="input-start-time">Start Time *</label
						>
						<input
							id="input-start-time"
							type="datetime-local"
							bind:value={eventStartTime}
							required
							class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
								? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500'
								: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
						/>
					</div>
					<div class="space-y-1">
						<label
							id="lbl-end-time"
							class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
							for="input-end-time">End Time *</label
						>
						<input
							id="input-end-time"
							type="datetime-local"
							bind:value={eventEndTime}
							required
							class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
								? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500'
								: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
						/>
					</div>
				</div>

				<div class="space-y-1">
					<label
						id="lbl-description"
						class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						for="textarea-description">Description *</label
					>
					<textarea
						id="textarea-description"
						bind:value={eventDescription}
						rows="3"
						required
						placeholder="Briefly describe the round milestones, guidelines, and rules..."
						class="w-full rounded-xl border p-3 outline-none transition-all resize-none {theme.darkMode
							? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500'
							: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
					></textarea>
				</div>

				<!-- Tracks Section -->
				<div class="space-y-3 border-t pt-4 mt-2 {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
					<div class="flex items-center justify-between">
						<label class="text-sm font-bold {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
							Event Tracks ({eventTracks.length})
						</label>
						<button
							type="button"
							onclick={addTrack}
							class="text-[10px] flex items-center gap-1 bg-orange-500/10 hover:bg-orange-500/20 text-orange-500 py-1.5 px-3 rounded-lg border border-orange-500/20 transition-all font-bold cursor-pointer"
						>
							<Plus class="w-3 h-3" />
							Add Track
						</button>
					</div>

					<div class="space-y-3 max-h-[180px] overflow-y-auto pr-1">
						{#each eventTracks as track, index}
							<div class="p-3.5 rounded-xl border relative {theme.darkMode ? 'bg-zinc-950/30 border-zinc-800' : 'bg-gray-50/50 border-gray-100'}">
								{#if eventTracks.length > 1}
									<button
										type="button"
										onclick={() => removeTrack(index)}
										class="absolute top-2 right-2 p-1 rounded-md hover:bg-red-500/10 text-red-500 hover:text-red-600 transition-colors border-0 bg-transparent cursor-pointer"
										title="Remove Track"
									>
										<X class="w-3.5 h-3.5" />
									</button>
								{/if}
								
								<span class="text-[10px] font-bold uppercase tracking-wider text-orange-500 block mb-1.5">Track #{index + 1}</span>
								
								<div class="space-y-2">
									<input
										type="text"
										bind:value={track.name}
										required
										placeholder="Track Name (e.g. Cybersecurity)"
										class="w-full text-xs rounded-lg border p-2.5 outline-none transition-all {theme.darkMode
											? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-1 focus:ring-orange-500'
											: 'border-gray-200 bg-white focus:ring-1 focus:ring-orange-500'}"
									/>
									<textarea
										bind:value={track.description}
										required
										rows="2"
										placeholder="Track Description..."
										class="w-full text-xs rounded-lg border p-2.5 outline-none transition-all resize-none {theme.darkMode
											? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-1 focus:ring-orange-500'
											: 'border-gray-200 bg-white focus:ring-1 focus:ring-orange-500'}"
									></textarea>
								</div>
							</div>
						{/each}
					</div>
				</div>

				<div class="space-y-1">
					<label
						id="lbl-status"
						class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						for="select-status">Event Status *</label
					>
					<select
						id="select-status"
						bind:value={eventStatusState}
						required
						class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
							? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500'
							: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
					>
						<option value="DRAFT">DRAFT</option>
						<option value="FINALIZED">FINALIZED</option>
					</select>
				</div>

				{#if eventMessage}
					<div
						class="p-3 rounded-lg text-sm font-medium {eventMessage.includes('successfully')
							? theme.darkMode
								? 'bg-green-950/20 text-green-400'
								: 'bg-green-50 text-green-700'
							: theme.darkMode
								? 'bg-red-950/20 text-red-400'
								: 'bg-red-50 text-red-700'}"
					>
						{eventMessage}
					</div>
				{/if}

				<div class="flex gap-3 mt-4">
					<button
						type="button"
						onclick={toggleEventModal}
						class="w-1/2 rounded-xl border py-3 font-semibold transition-all cursor-pointer {theme.darkMode
							? 'border-zinc-800 bg-transparent text-zinc-300 hover:bg-zinc-800/20'
							: 'border-gray-250 bg-transparent text-gray-700 hover:bg-gray-50'}"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isEventLoading}
						class="w-1/2 bg-orange-500 hover:bg-orange-600 text-white rounded-xl py-3 font-semibold disabled:opacity-50 transition-all shadow-sm cursor-pointer border-0"
					>
						{isEventLoading ? "Saving..." : modalMode === "create" ? "Create" : "Save"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
