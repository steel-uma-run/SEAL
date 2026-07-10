<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import { getSeason, getInterestedParticipants, getAllTeamsOfEvents, createTeam } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Calendar, Clock, Plus, X, Award, Edit2, Eye, Users } from "@lucide/svelte"

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

	// Event Details Modal State
	let showDetailModal = $state(false)
	let detailEvent = $state<any>(null)
	let isLoadingDetails = $state(false)
	let detailStudents = $state<any[]>([])
	let detailTeams = $state<any[]>([])
	let detailError = $state("")
	let activeTab = $state<"students" | "teams">("students")

	// Create Team Modal State
	let showCreateTeamModal = $state(false)
	let targetStudent = $state<any>(null)
	let teamNameInput = $state("")
	let teamDescriptionInput = $state("")
	let selectedMembers = $state<string[]>([])
	let isCreatingTeam = $state(false)
	let createTeamMessage = $state("")
	let createTeamError = $state(false)

	let activeTeamlessStudents = $derived(
		detailStudents.filter(
			(s) =>
				s.status === "ACTIVE" &&
				!(s.team_id || s.teamId) &&
				s.id !== (targetStudent ? targetStudent.id : null)
		)
	)

	function openCreateTeamModal(student: any) {
		targetStudent = student
		teamNameInput = `${student.fullName || student.name || "Student"}'s Team`
		teamDescriptionInput = `Team created by Coordinator for ${student.fullName || student.name || "Student"}`
		selectedMembers = []
		createTeamMessage = ""
		createTeamError = false
		showCreateTeamModal = true
	}

	async function handleCreateTeam(e: Event) {
		e.preventDefault()
		if (!teamNameInput.trim()) {
			createTeamMessage = "Team name is required."
			createTeamError = true
			return
		}

		isCreatingTeam = true
		createTeamMessage = ""
		createTeamError = false

		try {
			const { response, data, error } = await createTeam({
				body: {
					name: teamNameInput.trim(),
					description: teamDescriptionInput.trim(),
					event_id: detailEvent.id,
					leader_id: targetStudent.id,
					member_ids: selectedMembers
				},
				throwOnError: false
			})

			if (response?.ok && data) {
				showCreateTeamModal = false
				await openDetailModal(detailEvent)
				alert(`Team "${teamNameInput}" created successfully!`)
			} else {
				if (typeof window !== "undefined" && (!response || response.status === 404)) {
					const mockTeamId = crypto.randomUUID()
					const mockTeam = {
						id: mockTeamId,
						name: teamNameInput.trim(),
						description: teamDescriptionInput.trim(),
						status: "APPROVED",
						leader_id: targetStudent.id,
						leaderId: targetStudent.id,
						members: [targetStudent.id, ...selectedMembers]
					}

					const keyParts = `participants_${detailEvent.id}`
					const storedParts = localStorage.getItem(keyParts)
					if (storedParts) {
						let participants = JSON.parse(storedParts)
						participants = participants.map((p: any) => {
							if (p.id === targetStudent.id || selectedMembers.includes(p.id)) {
								return { ...p, team_id: mockTeamId, teamId: mockTeamId }
							}
							return p
						})
						localStorage.setItem(keyParts, JSON.stringify(participants))
					}

					const keyTeams = `teams_${detailEvent.id}`
					const storedTeams = localStorage.getItem(keyTeams)
					let teams = storedTeams ? JSON.parse(storedTeams) : []
					teams.push(mockTeam)
					localStorage.setItem(keyTeams, JSON.stringify(teams))

					showCreateTeamModal = false
					await openDetailModal(detailEvent)
					alert(`Team "${teamNameInput}" created locally for mock event!`)
				} else {
					const errBody = error as any
					createTeamMessage = errBody?.detail || errBody?.title || response?.statusText || "Failed to create team."
					createTeamError = true
				}
			}
		} catch (err: any) {
			console.error("Error creating team:", err)
			createTeamMessage = err.message || "An error occurred."
			createTeamError = true
		} finally {
			isCreatingTeam = false
		}
	}

	async function openDetailModal(eventItem: any) {
		detailEvent = eventItem
		showDetailModal = true
		isLoadingDetails = true
		detailError = ""
		detailStudents = []
		detailTeams = []
		activeTab = "students"

		try {
			const [participantsRes, teamsRes] = await Promise.all([
				getInterestedParticipants({
					path: { seasonId: seasonId, eventId: eventItem.id },
					throwOnError: false
				}),
				getAllTeamsOfEvents({
					path: { seasonId: seasonId, eventId: eventItem.id },
					throwOnError: false
				})
			])

			if (participantsRes.response?.ok && participantsRes.data) {
				detailStudents = participantsRes.data
			} else {
				// Fallback to local storage for mock events
				if (typeof window !== "undefined") {
					const localParts = localStorage.getItem(`participants_${eventItem.id}`)
					if (localParts) {
						detailStudents = JSON.parse(localParts)
					}
				}
			}

			if (teamsRes.response?.ok && teamsRes.data) {
				detailTeams = teamsRes.data
			} else {
				if (typeof window !== "undefined") {
					const localTeams = localStorage.getItem(`teams_${eventItem.id}`)
					if (localTeams) {
						detailTeams = JSON.parse(localTeams)
					} else {
						detailTeams = []
					}
				} else {
					detailTeams = []
				}
			}
		} catch (err: any) {
			console.error("Error loading event details:", err)
			detailError = err.message || "An error occurred while loading the event details."
		} finally {
			isLoadingDetails = false
		}
	}

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
					class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) transition-colors duration-300"
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
					class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) transition-colors duration-300"
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

										{#if event.tracks && event.tracks.length > 0}
											<div class="flex flex-wrap gap-1.5 mt-2">
												{#each event.tracks as track}
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
										<button
											onclick={() => openEditModal(event)}
											class="flex items-center gap-1.5 bg-transparent border border-(--md-outline) hover:bg-(--md-surface-container-highest) text-(--md-on-surface) px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer"
										>
											<Edit2 class="w-3.5 h-3.5" />
											Edit
										</button>
									{:else}
										<button
											onclick={() => openDetailModal(event)}
											class="flex items-center gap-1.5 bg-transparent border border-(--md-outline) hover:bg-(--md-surface-container-highest) text-(--md-on-surface) px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer"
										>
											<Eye class="w-3.5 h-3.5" />
											View Details
										</button>
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

				<!-- Tracks Section -->
				<div class="space-y-3 border-t pt-4 mt-2 border-(--md-outline-variant)">
					<div class="flex items-center justify-between">
						<label class="text-sm font-bold text-(--md-on-surface)">
							Event Tracks ({eventTracks.length})
						</label>
						<button
							type="button"
							onclick={addTrack}
							class="text-[10px] flex items-center gap-1 bg-(--md-primary-container) hover:bg-(--md-primary-container)/80 text-(--md-on-primary-container) py-1.5 px-3 rounded-lg border border-(--md-outline-variant) transition-all font-bold cursor-pointer"
						>
							<Plus class="w-3 h-3" />
							Add Track
						</button>
					</div>

					<div class="space-y-3 max-h-[180px] overflow-y-auto pr-1">
						{#each eventTracks as track, index}
							<div
								class="p-3.5 rounded-xl border relative bg-(--md-surface-container-low) border-(--md-outline-variant)"
							>
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

								<span
									class="text-[10px] font-bold uppercase tracking-wider text-(--md-primary) block mb-1.5"
									>Track #{index + 1}</span
								>

								<div class="space-y-2">
									<input
										type="text"
										bind:value={track.name}
										required
										placeholder="Track Name (e.g. Cybersecurity)"
										class="w-full text-xs rounded-lg border border-(--md-outline) p-2.5 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
									/>
									<textarea
										bind:value={track.description}
										required
										rows="2"
										placeholder="Track Description..."
										class="w-full text-xs rounded-lg border border-(--md-outline) p-2.5 outline-none transition-all resize-none bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
									></textarea>
								</div>
							</div>
						{/each}
					</div>
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

<!-- View Event Details Modal -->
{#if showDetailModal && detailEvent}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4"
	>
		<div
			class="w-full max-w-4xl max-h-[90vh] overflow-y-auto rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) p-8 relative transition-colors duration-300"
		>
			<button
				onclick={() => (showDetailModal = false)}
				class="absolute top-4 right-4 p-2 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-150/10 transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
				aria-label="Close"
			>
				<X class="w-5 h-5" />
			</button>

			<div class="mb-6">
				<div class="flex items-center gap-2 mb-2">
					<span
						class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
					>
						{detailEvent.status}
					</span>
				</div>
				<h2 class="text-2xl font-extrabold text-(--md-on-surface) m-0">
					{detailEvent.name}
				</h2>
				<p class="text-sm text-(--md-on-surface-variant) mt-2 leading-relaxed">
					{detailEvent.description || "No description provided."}
				</p>
				<div class="flex flex-wrap gap-4 mt-4 text-xs text-(--md-on-surface-variant)">
					<div class="flex items-center gap-1.5">
						<Clock class="w-4 h-4 text-(--md-primary)" />
						<span>Start: {formatDateTime(detailEvent.startTime)}</span>
					</div>
					<div class="flex items-center gap-1.5">
						<Clock class="w-4 h-4 text-(--md-error)" />
						<span>End: {formatDateTime(detailEvent.endTime)}</span>
					</div>
				</div>
			</div>

			<!-- Tabs -->
			<div class="flex border-b border-(--md-outline-variant) mb-6 gap-2">
				<button
					onclick={() => (activeTab = "students")}
					class="px-4 py-2 border-0 bg-transparent text-sm font-bold cursor-pointer transition-all border-b-2 {activeTab ===
					'students'
						? 'border-(--md-primary) text-(--md-primary)'
						: 'border-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface)'}"
				>
					Registered Students ({isLoadingDetails ? "..." : detailStudents.length})
				</button>
				<button
					onclick={() => (activeTab = "teams")}
					class="px-4 py-2 border-0 bg-transparent text-sm font-bold cursor-pointer transition-all border-b-2 {activeTab ===
					'teams'
						? 'border-(--md-primary) text-(--md-primary)'
						: 'border-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface)'}"
				>
					Teams ({isLoadingDetails ? "..." : detailTeams.length})
				</button>
			</div>

			<!-- Modal Content -->
			{#if isLoadingDetails}
				<div class="flex flex-col justify-center items-center py-12 gap-3">
					<div
						class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-(--md-primary)"
					></div>
					<span class="text-sm text-(--md-on-surface-variant)">Loading event data...</span>
				</div>
			{:else if detailError}
				<div
					class="bg-(--md-error-container) border border-(--md-error) p-4 rounded-xl text-(--md-on-error-container) text-sm"
				>
					<p class="font-bold">Cannot sync with database</p>
					<p class="mt-1">{detailError}</p>
				</div>
			{:else}
				{#if activeTab === "students"}
					<div class="overflow-x-auto">
						<table class="w-full text-left border-collapse">
							<thead>
								<tr
									class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
								>
									<th class="py-3 px-4">Student ID</th>
									<th class="py-3 px-4">Name</th>
									<th class="py-3 px-4">Email</th>
									<th class="py-3 px-4">Type</th>
									<th class="py-3 px-4">Team</th>
								</tr>
							</thead>
							<tbody class="text-sm">
								{#if detailStudents.length > 0}
									{#each detailStudents as student}
										<tr
											class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
										>
											<td class="py-3 px-4 font-mono text-xs font-semibold">
												{student.is_external
													? "NONE"
													: student.student_id || student.studentId || "N/A"}
											</td>
											<td class="py-3 px-4 font-bold"
												>{student.fullName || student.name || "N/A"}</td
											>
											<td class="py-3 px-4 text-xs">{student.email}</td>
											<td class="py-3 px-4 text-xs font-medium">
												{student.is_external ? "External" : "FPT"}
											</td>
											<td class="py-3 px-4 font-mono text-xs text-(--md-primary)">
												{#if student.team_id || student.teamId}
													#{student.team_id || student.teamId}
												{:else}
													<div class="flex items-center gap-2">
														<span class="text-zinc-500 italic">No Team</span>
														{#if student.status === 'ACTIVE'}
															<button
																onclick={() => openCreateTeamModal(student)}
																class="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-lg border border-(--md-primary) bg-transparent text-(--md-primary) hover:bg-(--md-primary-container) hover:text-(--md-on-primary-container) transition-all cursor-pointer"
															>
																Create Team
															</button>
														{/if}
													</div>
												{/if}
											</td>
										</tr>
									{/each}
								{:else}
									<tr>
										<td colspan="5" class="py-8 text-center text-(--md-on-surface-variant)">
											<p class="font-bold">No students registered for this event yet.</p>
										</td>
									</tr>
								{/if}
							</tbody>
						</table>
					</div>
				{:else if activeTab === "teams"}
					<div class="overflow-x-auto">
						<table class="w-full text-left border-collapse">
							<thead>
								<tr
									class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
								>
									<th class="py-3 px-4">Team ID</th>
									<th class="py-3 px-4">Team Name</th>
									<th class="py-3 px-4">Leader ID</th>
									<th class="py-3 px-4">Status</th>
								</tr>
							</thead>
							<tbody class="text-sm">
								{#if detailTeams.length > 0}
									{#each detailTeams as team}
										<tr
											class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
										>
											<td class="py-3 px-4 font-mono text-xs font-semibold">
												#{team.id}
											</td>
											<td class="py-3 px-4 font-bold">{team.name}</td>
											<td class="py-3 px-4 font-mono text-xs">{team.leader_id || team.leaderId}</td>
											<td class="py-3 px-4">
												<span
													class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {team.status ===
														'APPROVED' || team.status === 'ACTIVE'
														? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'
														: team.status === 'PENDING'
															? 'bg-amber-500/10 text-amber-500 border border-amber-500/20'
															: 'bg-rose-500/10 text-rose-500 border border-rose-500/20'}"
												>
													{team.status}
												</span>
											</td>
										</tr>
									{/each}
								{:else}
									<tr>
										<td colspan="4" class="py-8 text-center text-(--md-on-surface-variant)">
											<p class="font-bold">No teams created for this event yet.</p>
										</td>
									</tr>
								{/if}
							</tbody>
						</table>
					</div>
				{/if}
			{/if}

			<div class="flex justify-end mt-8 pt-4 border-t border-(--md-outline-variant)">
				<button
					onclick={() => (showDetailModal = false)}
					class="px-6 py-2.5 rounded-xl text-sm font-bold bg-(--md-surface-container-highest) hover:opacity-90 transition-all border border-(--md-outline) text-(--md-on-surface) cursor-pointer"
				>
					Close
				</button>
			</div>
		</div>
	</div>
{/if}

{#if showCreateTeamModal && targetStudent}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-md rounded-3xl border border-(--md-outline-variant) shadow-2xl p-8 bg-(--md-surface-container-low)"
		>
			<div class="flex justify-between items-center mb-6">
				<h3 class="text-xl font-extrabold text-(--md-on-surface)">
					Create Team
				</h3>
				<button
					onclick={() => (showCreateTeamModal = false)}
					class="p-1 rounded-full hover:bg-(--md-surface-container-high) border-0 bg-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer"
				>
					<X class="w-5 h-5" />
				</button>
			</div>

			<form onsubmit={handleCreateTeam} class="space-y-4">
				{#if createTeamMessage}
					<div
						class="p-3.5 rounded-xl border text-sm font-medium {createTeamError
							? 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)'
							: 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'}"
					>
						{createTeamMessage}
					</div>
				{/if}

				<div>
					<label for="new-team-leader" class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5">
						Team Leader
					</label>
					<input
						type="text"
						id="new-team-leader"
						value="{targetStudent.fullName || targetStudent.name} ({targetStudent.email})"
						disabled
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-container-high) text-(--md-on-surface-variant) outline-none opacity-80"
					/>
				</div>

				<div>
					<label for="new-team-name" class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5">
						Team Name *
					</label>
					<input
						type="text"
						id="new-team-name"
						bind:value={teamNameInput}
						placeholder="Enter team name"
						required
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
					/>
				</div>

				<div>
					<label for="new-team-desc" class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5">
						Description
					</label>
					<textarea
						id="new-team-desc"
						bind:value={teamDescriptionInput}
						placeholder="Enter team description"
						rows="2"
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all resize-none"
					></textarea>
				</div>

				{#if activeTeamlessStudents.length > 0}
					<div>
						<label class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5">
							Add Team Members (Active & Teamless)
						</label>
						<div class="max-h-36 overflow-y-auto border border-(--md-outline) rounded-xl p-3 bg-(--md-surface-bright) space-y-1.5">
							{#each activeTeamlessStudents as student}
								<label class="flex items-center gap-3 cursor-pointer p-1.5 rounded-lg hover:bg-(--md-surface-container-high) transition-colors">
									<input
										type="checkbox"
										value={student.id}
										checked={selectedMembers.includes(student.id)}
										onchange={(e) => {
											if (e.currentTarget.checked) {
												selectedMembers = [...selectedMembers, student.id];
											} else {
												selectedMembers = selectedMembers.filter(id => id !== student.id);
											}
										}}
										class="h-4.5 w-4.5 rounded border-(--md-outline) bg-transparent checked:bg-(--md-primary) checked:border-(--md-primary) accent-(--md-primary)"
									/>
									<div class="text-xs">
										<p class="font-bold text-(--md-on-surface)">
											{student.fullName || student.name}
										</p>
										<p class="text-(--md-on-surface-variant) mt-0.5">
											{student.is_external ? "External" : "FPT"} &bull; {student.email}
										</p>
									</div>
								</label>
							{/each}
						</div>
					</div>
				{/if}

				<div class="flex justify-end gap-3 pt-4 border-t border-(--md-outline-variant)">
					<button
						type="button"
						onclick={() => (showCreateTeamModal = false)}
						class="px-5 py-2.5 rounded-xl text-sm font-semibold border border-(--md-outline) bg-transparent text-(--md-on-surface) hover:bg-(--md-surface-container-high) transition-all cursor-pointer"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isCreatingTeam}
						class="px-5 py-2.5 rounded-xl text-sm font-bold bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 transition-all cursor-pointer disabled:opacity-50"
					>
						{isCreatingTeam ? "Creating..." : "Create Team"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
