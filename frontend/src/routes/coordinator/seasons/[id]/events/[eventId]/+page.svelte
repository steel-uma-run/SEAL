<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import {
		getEvent,
		getInterestedParticipants,
		getAllTeamsOfEvents,
		createTeam,
		getAllAccounts,
		assignMentor,
		assignJudge,
		assignTeamToTrack,
		createTrack,
		getAllTracksOfEvent,
		createRound,
		getRounds
	} from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Clock, X, Plus } from "@lucide/svelte"

	// Route Params
	const seasonId = $page.params.id || ""
	const eventId = $page.params.eventId || ""

	// Event Details State
	let event = $state<any>(null)
	let isLoading = $state(true)
	let error = $state("")

	// Students & Teams List State
	let students = $state<any[]>([])
	let teams = $state<any[]>([])
	let activeTab = $state<"students" | "teams" | "rounds">("students")

	// Create Team Modal State
	let showCreateTeamModal = $state(false)
	let targetStudent = $state<any>(null)
	let teamNameInput = $state("")
	let teamDescriptionInput = $state("")
	let selectedMembers = $state<string[]>([])
	let isCreatingTeam = $state(false)
	let createTeamMessage = $state("")
	let createTeamError = $state(false)

	// Lecturers List for Assignment
	let lecturers = $state<any[]>([])

	// Event Tracks list
	let eventTracks = $state<any[]>([])

	// Create Track Modal State
	let showCreateTrackModal = $state(false)
	let newTrackName = $state("")
	let newTrackDescription = $state("")
	let selectedTeamsForTrack = $state<string[]>([])
	let selectedMentorForTrack = $state<string>("")
	let selectedJudgesForTrack = $state<string[]>([])
	let isCreatingTrack = $state(false)
	let createTrackMessage = $state("")
	let createTrackError = $state(false)

	// Event Rounds list
	let eventRounds = $state<any[]>([])

	// Create Round Modal State
	let showCreateRoundModal = $state(false)
	let newRoundName = $state("")
	let newRoundDescription = $state("")
	let newRoundStartTime = $state("")
	let newRoundEndTime = $state("")
	let isCreatingRound = $state(false)
	let createRoundMessage = $state("")
	let createRoundError = $state(false)

	// Computed: Active teamless students for selection
	let activeTeamlessStudents = $derived(
		students.filter(
			(s) =>
				s.status === "ACTIVE" &&
				!(s.team_id || s.teamId) &&
				s.id !== (targetStudent ? targetStudent.id : null)
		)
	)

	function getTeamName(teamId: string) {
		const team = teams.find((t) => t.id === teamId)
		return team ? team.name : `#${teamId}`
	}

	function getStudentName(studentId: string) {
		const student = students.find((s) => s.id === studentId)
		return student ? student.fullName || student.name : studentId
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

	async function fetchEventDetails() {
		isLoading = true
		error = ""
		try {
			// 1. Fetch Event Details
			let eventItem = null
			try {
				const res = await getEvent({ path: { eventId }, throwOnError: false })
				if (res.response?.ok && res.data) {
					eventItem = res.data
				}
			} catch (err) {
				console.error("Error fetching event via API:", err)
			}

			if (!eventItem && typeof window !== "undefined") {
				// Fallback to local storage
				const storedEvents = localStorage.getItem(`events_${seasonId}`)
				if (storedEvents) {
					const eventsList = JSON.parse(storedEvents)
					eventItem = eventsList.find((e: any) => e.id === eventId)
				}
			}

			if (!eventItem) {
				error = "Event details not found."
				return
			}
			event = eventItem

			// 2. Fetch interested participants and teams
			const [participantsRes, teamsRes] = await Promise.all([
				getInterestedParticipants({
					path: { eventId },
					throwOnError: false
				}),
				getAllTeamsOfEvents({
					path: { eventId },
					throwOnError: false
				})
			])

			if (participantsRes.response?.ok && participantsRes.data) {
				students = participantsRes.data
			} else if (typeof window !== "undefined") {
				const localParts = localStorage.getItem(`participants_${eventId}`)
				if (localParts) {
					students = JSON.parse(localParts)
				}
			}

			if (teamsRes.response?.ok && teamsRes.data) {
				teams = teamsRes.data
			} else if (typeof window !== "undefined") {
				const localTeams = localStorage.getItem(`teams_${eventId}`)
				if (localTeams) {
					teams = JSON.parse(localTeams)
				}
			}
		} catch (err: any) {
			console.error("Error loading event details:", err)
			error = err.message || "An error occurred while loading the event details."
		} finally {
			isLoading = false
		}
	}

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
			const {
				response,
				data,
				error: apiErr
			} = await createTeam({
				body: {
					name: teamNameInput.trim(),
					description: teamDescriptionInput.trim(),
					event_id: eventId,
					leader_id: targetStudent.id,
					member_ids: selectedMembers
				},
				throwOnError: false
			})

			if (response?.ok && data) {
				showCreateTeamModal = false
				await fetchEventDetails()
				alert(`Team "${teamNameInput}" created successfully!`)
			} else {
				if (typeof window !== "undefined") {
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

					const keyParts = `participants_${eventId}`
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

					const keyTeams = `teams_${eventId}`
					const storedTeams = localStorage.getItem(keyTeams)
					let localTeams = storedTeams ? JSON.parse(storedTeams) : []
					localTeams.push(mockTeam)
					localStorage.setItem(keyTeams, JSON.stringify(localTeams))

					showCreateTeamModal = false
					await fetchEventDetails()
					alert(`Team "${teamNameInput}" created locally for mock event!`)
				} else {
					const errBody = apiErr as any
					createTeamMessage =
						errBody?.detail || errBody?.title || response?.statusText || "Failed to create team."
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

	async function loadLecturers() {
		try {
			const { data, response } = await getAllAccounts({ throwOnError: false })
			if (response?.ok && data) {
				lecturers = data.filter((u: any) => u.role === "LECTURER" || u.role === "Lecturer")
			} else {
				lecturers = [
					{ id: "lecturer-1", name: "Nguyễn Văn A", email: "nva@fpt.edu.vn" },
					{ id: "lecturer-2", name: "Trần Thị B", email: "ttb@fpt.edu.vn" },
					{ id: "lecturer-3", name: "Lê Văn C", email: "lvc@fpt.edu.vn" },
					{ id: "lecturer-4", name: "David Miller", email: "david@fpt.edu.vn" }
				]
			}
		} catch (err) {
			console.error("Error loading lecturers:", err)
			lecturers = [
				{ id: "lecturer-1", name: "Nguyễn Văn A", email: "nva@fpt.edu.vn" },
				{ id: "lecturer-2", name: "Trần Thị B", email: "ttb@fpt.edu.vn" },
				{ id: "lecturer-3", name: "Lê Văn C", email: "lvc@fpt.edu.vn" },
				{ id: "lecturer-4", name: "David Miller", email: "david@fpt.edu.vn" }
			]
		}
	}

	async function loadEventTracks() {
		try {
			const { data, response } = await getAllTracksOfEvent({
				path: { eventId },
				throwOnError: false
			})
			if (response?.ok && data && data.length > 0) {
				eventTracks = data
				return
			}
		} catch (err) {
			console.error("Error loading event tracks from API:", err)
		}

		// Fallback to local storage event tracks
		if (event && event.tracks && event.tracks.length > 0) {
			eventTracks = event.tracks
				.filter((t: any) => t.name && t.name.trim() !== "")
				.map((t: any, idx: number) => ({
					id: t.id || t.name || `track-${idx + 1}`,
					name: t.name,
					description: t.description || "",
					mentor_ids: t.mentor_ids || t.mentorIds || [],
					judge_ids: t.judge_ids || t.judgeIds || []
				}))
		} else {
			eventTracks = []
		}
	}

	function getTrackName(trackId: string) {
		const track = eventTracks.find((t) => t.id === trackId || t.name === trackId)
		return track ? track.name : trackId
	}

	async function loadEventRounds() {
		try {
			const { data, response } = await getRounds({
				path: { eventId },
				throwOnError: false
			})
			if (response?.ok && data && data.length > 0) {
				eventRounds = data
				return
			}
		} catch (err) {
			console.error("Error loading event rounds from API:", err)
		}

		// Fallback to local storage
		let localRounds: any[] = []
		if (typeof window !== "undefined") {
			const storedRounds = localStorage.getItem(`rounds_${eventId}`)
			if (storedRounds) {
				localRounds = JSON.parse(storedRounds)
			}
		}
		eventRounds = localRounds
	}

	function openCreateRoundModal() {
		newRoundName = ""
		newRoundDescription = ""
		newRoundStartTime = ""
		newRoundEndTime = ""
		createRoundMessage = ""
		createRoundError = false
		showCreateRoundModal = true
	}

	async function handleCreateRound(e: Event) {
		e.preventDefault()
		if (
			!newRoundName.trim() ||
			!newRoundDescription.trim() ||
			!newRoundStartTime ||
			!newRoundEndTime
		) {
			createRoundMessage = "All fields are required."
			createRoundError = true
			return
		}

		const start = new Date(newRoundStartTime)
		const end = new Date(newRoundEndTime)

		if (start >= end) {
			createRoundMessage = "Start time must be before end time."
			createRoundError = true
			return
		}

		// Verify within event dates
		if (event) {
			const eventStart = new Date(event.startTime || event.start_time)
			const eventEnd = new Date(event.endTime || event.end_time)
			if (start < eventStart || end > eventEnd) {
				createRoundMessage = "Round timeframe must be strictly within the Event's timeframe."
				createRoundError = true
				return
			}
		}

		isCreatingRound = true
		createRoundMessage = ""
		createRoundError = false

		try {
			const { response, data: roundData } = await createRound({
				path: { eventId },
				body: {
					name: newRoundName.trim(),
					description: newRoundDescription.trim(),
					start_time: start.toISOString(),
					end_time: end.toISOString()
				},
				throwOnError: false
			})

			if (response?.ok && roundData) {
				showCreateRoundModal = false
				await loadEventRounds()
				alert(`Round "${newRoundName}" created successfully!`)
			} else {
				// Local storage mock fallback
				const mockRound = {
					id: crypto.randomUUID(),
					name: newRoundName.trim(),
					description: newRoundDescription.trim(),
					startTime: start.toISOString(),
					endTime: end.toISOString(),
					eventId
				}
				eventRounds = [...eventRounds, mockRound]
				if (typeof window !== "undefined") {
					localStorage.setItem(`rounds_${eventId}`, JSON.stringify(eventRounds))
				}
				showCreateRoundModal = false
				alert(`Round "${newRoundName}" created locally for mock event!`)
			}
		} catch (err: any) {
			createRoundMessage = err.message || "An error occurred."
			createRoundError = true
		} finally {
			isCreatingRound = false
		}
	}

	function openCreateTrackModal() {
		newTrackName = ""
		newTrackDescription = ""
		selectedTeamsForTrack = []
		selectedMentorForTrack = ""
		selectedJudgesForTrack = []
		createTrackMessage = ""
		createTrackError = false
		showCreateTrackModal = true
	}

	async function handleCreateTrack(e: Event) {
		e.preventDefault()
		if (!newTrackName.trim() || !newTrackDescription.trim()) {
			createTrackMessage = "Track name and description are required."
			createTrackError = true
			return
		}

		if (
			selectedJudgesForTrack.length > 0 &&
			selectedJudgesForTrack.length !== 2 &&
			selectedJudgesForTrack.length !== 3
		) {
			createTrackMessage = "You must assign either 2 or 3 judges (or none)."
			createTrackError = true
			return
		}

		isCreatingTrack = true
		createTrackMessage = ""
		createTrackError = false

		try {
			// 1. Create the Track
			const {
				response,
				data: newTrack,
				error: apiErr
			} = await createTrack({
				path: { eventId },
				body: {
					name: newTrackName.trim(),
					description: newTrackDescription.trim()
				},
				throwOnError: false
			})

			if (response?.ok && newTrack) {
				const trackId = newTrack.id

				// 2. Assign Mentor if selected
				if (selectedMentorForTrack) {
					await assignMentor({
						path: { trackId },
						body: { mentor_id: selectedMentorForTrack },
						throwOnError: false
					})
				}

				// 3. Assign Judges if selected
				for (const judgeId of selectedJudgesForTrack) {
					await assignJudge({
						path: { trackId },
						body: { judge_id: judgeId },
						throwOnError: false
					})
				}

				// 4. Assign Teams if selected
				for (const teamId of selectedTeamsForTrack) {
					await assignTeamToTrack({
						path: { trackId },
						body: { team_id: teamId },
						throwOnError: false
					})
				}

				showCreateTrackModal = false
				await fetchEventDetails()
				await loadEventTracks()
				alert(`Track "${newTrackName}" and assignments saved successfully!`)
			} else {
				// Local storage mock fallback
				if (typeof window !== "undefined") {
					const trackId = crypto.randomUUID()
					const mockTrack = {
						id: trackId,
						name: newTrackName.trim(),
						description: newTrackDescription.trim(),
						event_id: eventId,
						mentor_ids: selectedMentorForTrack ? [selectedMentorForTrack] : [],
						judge_ids: selectedJudgesForTrack
					}

					// Update events list
					const key = `events_${seasonId}`
					const stored = localStorage.getItem(key)
					if (stored) {
						let localEvents = JSON.parse(stored)
						localEvents = localEvents.map((evt: any) => {
							if (evt.id === eventId) {
								const existingTracks = evt.tracks || []
								return { ...evt, tracks: [...existingTracks, mockTrack] }
							}
							return evt
						})
						localStorage.setItem(key, JSON.stringify(localEvents))
					}

					// Assign teams to this track in localStorage
					if (selectedTeamsForTrack.length > 0) {
						const keyTeams = `teams_${eventId}`
						const storedTeams = localStorage.getItem(keyTeams)
						if (storedTeams) {
							let localTeams = JSON.parse(storedTeams)
							localTeams = localTeams.map((t: any) => {
								if (selectedTeamsForTrack.includes(t.id)) {
									return { ...t, track_id: trackId, trackId: trackId }
								}
								return t
							})
							localStorage.setItem(keyTeams, JSON.stringify(localTeams))
						}
					}

					showCreateTrackModal = false
					await fetchEventDetails()
					await loadEventTracks()
					alert(`Track and assignments saved locally for mock event!`)
				} else {
					const errBody = apiErr as any
					createTrackMessage = errBody?.detail || response?.statusText || "Failed to create track."
					createTrackError = true
				}
			}
		} catch (err: any) {
			console.error("Error creating track and assignments:", err)
			createTrackMessage = err.message || "An error occurred."
			createTrackError = true
		} finally {
			isCreatingTrack = false
		}
	}

	function getLecturerName(lecturerId: string) {
		const lec = lecturers.find((l) => l.id === lecturerId)
		return lec ? lec.fullName || lec.name : lecturerId
	}

	onMount(async () => {
		await fetchEventDetails()
		await loadLecturers()
		await loadEventTracks()
		await loadEventRounds()
	})
</script>

<div class="mb-6">
	<button
		onclick={() => goto(`/coordinator/seasons/${seasonId}`)}
		class="flex items-center gap-2 bg-transparent text-sm font-semibold py-2 px-3 rounded-lg border border-(--md-outline) text-(--md-on-surface-variant) hover:bg-(--md-surface-container-high) transition-all cursor-pointer"
	>
		<ArrowLeft class="w-4 h-4" />
		Back to Season Details
	</button>
</div>

{#if isLoading}
	<div class="flex justify-center items-center h-[50vh]">
		<div
			class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"
		></div>
	</div>
{:else if error}
	<div
		class="bg-(--md-error-container) border border-(--md-error) p-4 rounded-xl text-(--md-on-error-container) text-sm"
	>
		<p class="font-bold">Error loading event</p>
		<p class="mt-1">{error}</p>
	</div>
{:else if event}
	<div class="space-y-6">
		<!-- Event Header Card -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
		>
			<div class="mb-6">
				<div class="flex items-center gap-2 mb-2">
					<span
						class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
					>
						{event.status}
					</span>
				</div>
				<h1 class="text-3xl font-extrabold text-(--md-on-surface) m-0">
					{event.name}
				</h1>
				<p class="text-base text-(--md-on-surface-variant) mt-3 leading-relaxed max-w-3xl">
					{event.description || "No description provided."}
				</p>
				{#if eventTracks.length > 0}
					<div class="flex items-center gap-2 mt-4 flex-wrap">
						<span class="text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider"
							>Tracks:</span
						>
						{#each eventTracks as track}
							<span
								class="inline-flex items-center px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider bg-(--md-surface-container-high) text-(--md-on-surface-variant) border border-(--md-outline-variant)"
								title={track.description}
							>
								{track.name}
							</span>
						{/each}
					</div>
				{/if}
				<div class="flex flex-wrap gap-6 mt-6 text-sm text-(--md-on-surface-variant)">
					<div class="flex items-center gap-2">
						<Clock class="w-4 h-4 text-(--md-primary)" />
						<span><strong>Start:</strong> {formatDateTime(event.startTime)}</span>
					</div>
					<div class="flex items-center gap-2">
						<Clock class="w-4 h-4 text-(--md-error)" />
						<span><strong>End:</strong> {formatDateTime(event.endTime)}</span>
					</div>
				</div>
			</div>
		</div>

		<!-- Event Tracks and Experts Management Card -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
		>
			<div class="flex justify-between items-center mb-6">
				<div>
					<h2 class="text-xl font-bold text-(--md-on-surface) m-0">Event Tracks & Experts</h2>
					<p class="text-sm text-(--md-on-surface-variant) mt-1">
						Manage and configure track details, assigned mentors, judges, and teams.
					</p>
				</div>
				<button
					onclick={openCreateTrackModal}
					class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-4 py-2 rounded-xl text-sm font-bold transition-all cursor-pointer border-0"
				>
					<Plus class="w-4 h-4" />
					Create Track
				</button>
			</div>

			{#if eventTracks.length > 0}
				<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
					{#each eventTracks as track}
						<div
							class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) space-y-4"
						>
							<div>
								<h3 class="text-base font-bold text-(--md-primary) m-0">{track.name}</h3>
								<p class="text-xs text-(--md-on-surface-variant) mt-1 leading-relaxed">
									{track.description}
								</p>
							</div>

							<div class="space-y-2 border-t pt-3 border-(--md-outline-variant)">
								<div>
									<span
										class="text-[10px] uppercase font-bold tracking-wider text-(--md-on-surface-variant)"
										>Mentor</span
									>
									<div class="mt-1">
										{#if track.mentor_ids && track.mentor_ids.length > 0}
											{#each track.mentor_ids as mentorId}
												<span
													class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-violet-500/10 text-violet-500 font-medium"
												>
													{getLecturerName(mentorId)}
												</span>
											{/each}
										{:else}
											<span class="text-xs text-zinc-500 italic">No mentor assigned</span>
										{/if}
									</div>
								</div>

								<div>
									<span
										class="text-[10px] uppercase font-bold tracking-wider text-(--md-on-surface-variant)"
										>Judges</span
									>
									<div class="mt-1 flex flex-wrap gap-1">
										{#if track.judge_ids && track.judge_ids.length > 0}
											{#each track.judge_ids as judgeId}
												<span
													class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-sky-500/10 text-sky-500 font-medium"
												>
													{getLecturerName(judgeId)}
												</span>
											{/each}
										{:else}
											<span class="text-xs text-zinc-500 italic">No judges assigned</span>
										{/if}
									</div>
								</div>

								<div>
									<span
										class="text-[10px] uppercase font-bold tracking-wider text-(--md-on-surface-variant)"
										>Assigned Teams</span
									>
									<div class="mt-1 flex flex-wrap gap-1">
										{#if teams.filter((t) => t.track_id === track.id || t.trackId === track.id).length > 0}
											{#each teams.filter((t) => t.track_id === track.id || t.trackId === track.id) as team}
												<span
													class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-emerald-500/10 text-emerald-500 font-medium"
												>
													{team.name}
												</span>
											{/each}
										{:else}
											<span class="text-xs text-zinc-500 italic">No teams assigned</span>
										{/if}
									</div>
								</div>
							</div>
						</div>
					{/each}
				</div>
			{:else}
				<div
					class="text-center py-8 border border-dashed rounded-2xl border-(--md-outline-variant) text-(--md-on-surface-variant)"
				>
					<p class="text-sm font-semibold">No tracks created for this event yet</p>
					<p class="text-xs mt-1">
						Get started by creating your first track and assigning components.
					</p>
				</div>
			{/if}
		</div>

		<!-- Event Content Tabs Card -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
		>
			<!-- Tabs -->
			<div class="flex border-b border-(--md-outline-variant) mb-6 gap-2">
				<button
					onclick={() => (activeTab = "students")}
					class="px-4 py-3 border-0 bg-transparent text-sm font-bold cursor-pointer transition-all border-b-2 {activeTab ===
					'students'
						? 'border-(--md-primary) text-(--md-primary)'
						: 'border-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface)'}"
				>
					Registered Students ({students.length})
				</button>
				<button
					onclick={() => (activeTab = "teams")}
					class="px-4 py-3 border-0 bg-transparent text-sm font-bold cursor-pointer transition-all border-b-2 {activeTab ===
					'teams'
						? 'border-(--md-primary) text-(--md-primary)'
						: 'border-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface)'}"
				>
					Teams ({teams.length})
				</button>
				<button
					onclick={() => (activeTab = "rounds")}
					class="px-4 py-3 border-0 bg-transparent text-sm font-bold cursor-pointer transition-all border-b-2 {activeTab ===
					'rounds'
						? 'border-(--md-primary) text-(--md-primary)'
						: 'border-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface)'}"
				>
					Rounds ({eventRounds.length})
				</button>
			</div>

			<!-- Tab Panels -->
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
							{#if students.length > 0}
								{#each students as student}
									<tr
										class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
									>
										<td class="py-3 px-4 font-mono text-xs font-semibold">
											{student.is_external
												? "NONE"
												: student.student_id || student.studentId || "N/A"}
										</td>
										<td class="py-3 px-4 font-bold">
											{student.fullName || student.name || "N/A"}
										</td>
										<td class="py-3 px-4 text-xs">{student.email}</td>
										<td class="py-3 px-4 text-xs font-medium">
											{student.is_external ? "External" : "FPT"}
										</td>
										<td class="py-3 px-4 text-xs font-semibold text-(--md-primary)">
											{#if student.team_id || student.teamId}
												{getTeamName(student.team_id || student.teamId)}
											{:else}
												<div class="flex items-center gap-2">
													<span class="text-zinc-500 italic">No Team</span>
													{#if student.status === "ACTIVE"}
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
								<th class="py-3 px-4">Team Name</th>
								<th class="py-3 px-4">Leader Name</th>
								<th class="py-3 px-4">Status</th>
								<th class="py-3 px-4">Track</th>
								<th class="py-3 px-4">Mentor</th>
								<th class="py-3 px-4">Judges</th>
							</tr>
						</thead>
						<tbody class="text-sm">
							{#if teams.length > 0}
								{#each teams as team}
									{@const track = eventTracks.find(
										(t) =>
											t.id === (team.track_id || team.trackId) ||
											t.name === (team.track_id || team.trackId)
									)}
									<tr
										class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
									>
										<td class="py-3 px-4 font-bold">{team.name}</td>
										<td class="py-3 px-4 text-xs font-semibold"
											>{getStudentName(team.leader_id || team.leaderId)}</td
										>
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
										<td class="py-3 px-4 text-xs font-semibold">
											{#if team.track_id || team.trackId}
												<span
													class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-teal-500/10 text-teal-500 border border-teal-500/20"
												>
													{getTrackName(team.track_id || team.trackId)}
												</span>
											{:else}
												<span class="text-zinc-500 italic">Unassigned</span>
											{/if}
										</td>
										<td class="py-3 px-4 text-xs">
											{#if track && track.mentor_ids && track.mentor_ids.length > 0}
												<div class="flex flex-wrap gap-1">
													{#each track.mentor_ids as mentorId}
														<span
															class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-violet-500/10 text-violet-500 border border-violet-500/20"
														>
															{getLecturerName(mentorId)}
														</span>
													{/each}
												</div>
											{:else}
												<span class="text-zinc-500 italic">Unassigned</span>
											{/if}
										</td>
										<td class="py-3 px-4 text-xs">
											{#if track && track.judge_ids && track.judge_ids.length > 0}
												<div class="flex flex-wrap gap-1">
													{#each track.judge_ids as judgeId}
														<span
															class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-sky-500/10 text-sky-500 border border-sky-500/20"
														>
															{getLecturerName(judgeId)}
														</span>
													{/each}
												</div>
											{:else}
												<span class="text-zinc-500 italic">Unassigned</span>
											{/if}
										</td>
									</tr>
								{/each}
							{:else}
								<tr>
									<td colspan="6" class="py-8 text-center text-(--md-on-surface-variant)">
										<p class="font-bold">No teams created for this event yet.</p>
									</td>
								</tr>
							{/if}
						</tbody>
					</table>
				</div>
			{:else if activeTab === "rounds"}
				<div class="space-y-6">
					<div class="flex justify-between items-center">
						<h3 class="text-base font-bold text-(--md-on-surface) m-0">Event Rounds</h3>
						<button
							onclick={openCreateRoundModal}
							class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-4 py-2 rounded-xl text-sm font-bold transition-all cursor-pointer border-0"
						>
							<Plus class="w-4 h-4" />
							Create Round
						</button>
					</div>

					{#if eventRounds.length > 0}
						<div class="overflow-x-auto">
							<table class="w-full text-left border-collapse">
								<thead>
									<tr
										class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
									>
										<th class="py-3 px-4">Round Name</th>
										<th class="py-3 px-4">Description</th>
										<th class="py-3 px-4">Start Time</th>
										<th class="py-3 px-4">End Time</th>
									</tr>
								</thead>
								<tbody class="text-sm">
									{#each eventRounds as round}
										<tr
											class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
										>
											<td class="py-3 px-4 font-bold">{round.name}</td>
											<td class="py-3 px-4 text-xs text-(--md-on-surface-variant)"
												>{round.description}</td
											>
											<td class="py-3 px-4 text-xs"
												>{formatDateTime(round.startTime || round.start_time)}</td
											>
											<td class="py-3 px-4 text-xs"
												>{formatDateTime(round.endTime || round.end_time)}</td
											>
										</tr>
									{/each}
								</tbody>
							</table>
						</div>
					{:else}
						<div
							class="text-center py-8 border border-dashed rounded-2xl border-(--md-outline-variant) text-(--md-on-surface-variant)"
						>
							<p class="font-bold">No rounds created for this event yet.</p>
						</div>
					{/if}
				</div>
			{/if}
		</div>
	</div>
{/if}

{#if showCreateTeamModal && targetStudent}
	<div
		class="fixed inset-0 z-[3000] flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-md rounded-3xl border border-(--md-outline-variant) shadow-2xl p-8 bg-(--md-surface-container-low)"
		>
			<div class="flex justify-between items-center mb-6">
				<h3 class="text-xl font-extrabold text-(--md-on-surface)">Create Team</h3>
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
					<label
						for="new-team-leader"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
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
					<label
						for="new-team-name"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
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
					<label
						for="new-team-desc"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
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
						<label
							class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
						>
							Add Team Members (Active & Teamless)
						</label>
						<div
							class="max-h-36 overflow-y-auto border border-(--md-outline) rounded-xl p-3 bg-(--md-surface-bright) space-y-1.5"
						>
							{#each activeTeamlessStudents as student}
								<label
									class="flex items-center gap-3 cursor-pointer p-1.5 rounded-lg hover:bg-(--md-surface-container-high) transition-colors"
								>
									<input
										type="checkbox"
										value={student.id}
										checked={selectedMembers.includes(student.id)}
										onchange={(e) => {
											if (e.currentTarget.checked) {
												selectedMembers = [...selectedMembers, student.id]
											} else {
												selectedMembers = selectedMembers.filter((id) => id !== student.id)
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

{#if showCreateTrackModal}
	<div
		class="fixed inset-0 z-[3000] flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-md rounded-3xl border border-(--md-outline-variant) shadow-2xl p-8 bg-(--md-surface-container-low) max-h-[90vh] overflow-y-auto"
		>
			<div class="flex justify-between items-center mb-6">
				<h3 class="text-xl font-extrabold text-(--md-on-surface)">Create Track</h3>
				<button
					onclick={() => (showCreateTrackModal = false)}
					class="p-1 rounded-full hover:bg-(--md-surface-container-high) border-0 bg-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer"
				>
					<X class="w-5 h-5" />
				</button>
			</div>

			<form onsubmit={handleCreateTrack} class="space-y-4">
				{#if createTrackMessage}
					<div
						class="p-3.5 rounded-xl border text-sm font-medium {createTrackError
							? 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)'
							: 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'}"
					>
						{createTrackMessage}
					</div>
				{/if}

				<div>
					<label
						for="new-track-name"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Track Name *
					</label>
					<input
						type="text"
						id="new-track-name"
						bind:value={newTrackName}
						placeholder="Cybersecurity"
						required
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
					/>
				</div>

				<div>
					<label
						for="new-track-desc"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Description *
					</label>
					<textarea
						id="new-track-desc"
						bind:value={newTrackDescription}
						placeholder="Enter track description..."
						required
						rows="2"
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all resize-none"
					></textarea>
				</div>

				<!-- Mentor Selection -->
				<div>
					<label
						for="new-track-mentor"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Select Mentor
					</label>
					<select
						id="new-track-mentor"
						bind:value={selectedMentorForTrack}
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all cursor-pointer"
					>
						<option value="">-- None / Unassigned --</option>
						{#each lecturers.filter((l) => !selectedJudgesForTrack.includes(l.id)) as lecturer}
							<option value={lecturer.id}
								>{lecturer.fullName || lecturer.name} ({lecturer.email})</option
							>
						{/each}
					</select>
				</div>

				<!-- Judges Selection -->
				<div>
					<label
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Select Judges (2 or 3 required)
					</label>
					<div
						class="max-h-32 overflow-y-auto border border-(--md-outline) rounded-xl p-3 bg-(--md-surface-bright) space-y-1.5"
					>
						{#each lecturers.filter((l) => l.id !== selectedMentorForTrack) as lecturer}
							<label
								class="flex items-center gap-3 cursor-pointer p-1.5 rounded-lg hover:bg-(--md-surface-container-high) transition-colors"
							>
								<input
									type="checkbox"
									value={lecturer.id}
									checked={selectedJudgesForTrack.includes(lecturer.id)}
									onchange={(e) => {
										if (e.currentTarget.checked) {
											selectedJudgesForTrack = [...selectedJudgesForTrack, lecturer.id]
										} else {
											selectedJudgesForTrack = selectedJudgesForTrack.filter(
												(id) => id !== lecturer.id
											)
										}
									}}
									class="h-4.5 w-4.5 rounded border-(--md-outline) bg-transparent checked:bg-(--md-primary) checked:border-(--md-primary) accent-(--md-primary)"
								/>
								<div class="text-xs">
									<p class="font-bold text-(--md-on-surface)">
										{lecturer.fullName || lecturer.name}
									</p>
									<p class="text-(--md-on-surface-variant) mt-0.5">
										{lecturer.email}
									</p>
								</div>
							</label>
						{/each}
					</div>
					<p class="text-[10px] text-zinc-500 mt-1 italic">
						Selected: {selectedJudgesForTrack.length} judge(s)
					</p>
				</div>

				<!-- Teams Selection -->
				{#if teams.filter((t: any) => !t.track_id && !t.trackId).length > 0}
					<div>
						<label
							class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
						>
							Assign Teams
						</label>
						<div
							class="max-h-32 overflow-y-auto border border-(--md-outline) rounded-xl p-3 bg-(--md-surface-bright) space-y-1.5"
						>
							{#each teams.filter((t: any) => !t.track_id && !t.trackId) as team}
								<label
									class="flex items-center gap-3 cursor-pointer p-1.5 rounded-lg hover:bg-(--md-surface-container-high) transition-colors"
								>
									<input
										type="checkbox"
										value={team.id}
										checked={selectedTeamsForTrack.includes(team.id)}
										onchange={(e) => {
											if (e.currentTarget.checked) {
												selectedTeamsForTrack = [...selectedTeamsForTrack, team.id]
											} else {
												selectedTeamsForTrack = selectedTeamsForTrack.filter((id) => id !== team.id)
											}
										}}
										class="h-4.5 w-4.5 rounded border-(--md-outline) bg-transparent checked:bg-(--md-primary) checked:border-(--md-primary) accent-(--md-primary)"
									/>
									<div class="text-xs">
										<p class="font-bold text-(--md-on-surface)">{team.name}</p>
										<p class="text-(--md-on-surface-variant) mt-0.5">
											Leader: {getStudentName(team.leader_id || team.leaderId)}
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
						onclick={() => (showCreateTrackModal = false)}
						class="px-5 py-2.5 rounded-xl text-sm font-semibold border border-(--md-outline) bg-transparent text-(--md-on-surface) hover:bg-(--md-surface-container-high) transition-all cursor-pointer"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isCreatingTrack}
						class="px-5 py-2.5 rounded-xl text-sm font-bold bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 transition-all cursor-pointer disabled:opacity-50"
					>
						{isCreatingTrack ? "Creating..." : "Create Track"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if showCreateRoundModal}
	<div
		class="fixed inset-0 z-[3000] flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-md rounded-3xl border border-(--md-outline-variant) shadow-2xl p-8 bg-(--md-surface-container-low)"
		>
			<div class="flex justify-between items-center mb-6">
				<h3 class="text-xl font-extrabold text-(--md-on-surface)">Create Round</h3>
				<button
					onclick={() => (showCreateRoundModal = false)}
					class="p-1 rounded-full hover:bg-(--md-surface-container-high) border-0 bg-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer"
				>
					<X class="w-5 h-5" />
				</button>
			</div>

			<form onsubmit={handleCreateRound} class="space-y-4">
				{#if createRoundMessage}
					<div
						class="p-3.5 rounded-xl border text-sm font-medium {createRoundError
							? 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)'
							: 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'}"
					>
						{createRoundMessage}
					</div>
				{/if}

				<div>
					<label
						for="new-round-name"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Round Name *
					</label>
					<input
						type="text"
						id="new-round-name"
						bind:value={newRoundName}
						placeholder="Enter round name (e.g. Group Stage)"
						required
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
					/>
				</div>

				<div>
					<label
						for="new-round-description"
						class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
					>
						Description *
					</label>
					<textarea
						id="new-round-description"
						bind:value={newRoundDescription}
						placeholder="Enter round instructions"
						rows="3"
						required
						class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all resize-none"
					></textarea>
				</div>

				<div class="grid grid-cols-2 gap-4">
					<div>
						<label
							for="new-round-start"
							class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
						>
							Start Time *
						</label>
						<input
							type="datetime-local"
							id="new-round-start"
							bind:value={newRoundStartTime}
							required
							class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
						/>
					</div>

					<div>
						<label
							for="new-round-end"
							class="block text-xs font-bold text-(--md-on-surface-variant) uppercase tracking-wider mb-1.5"
						>
							End Time *
						</label>
						<input
							type="datetime-local"
							id="new-round-end"
							bind:value={newRoundEndTime}
							required
							class="w-full rounded-xl p-3 border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
						/>
					</div>
				</div>

				<div class="flex justify-end gap-3 pt-4 border-t border-(--md-outline-variant)">
					<button
						type="button"
						onclick={() => (showCreateRoundModal = false)}
						class="px-5 py-2.5 rounded-xl text-sm font-semibold border border-(--md-outline) bg-transparent text-(--md-on-surface) hover:bg-(--md-surface-container-high) transition-all cursor-pointer"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isCreatingRound}
						class="px-5 py-2.5 rounded-xl text-sm font-bold bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 transition-all cursor-pointer disabled:opacity-50"
					>
						{isCreatingRound ? "Creating..." : "Create"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
