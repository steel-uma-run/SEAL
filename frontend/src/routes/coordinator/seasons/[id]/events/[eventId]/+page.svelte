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
		createTrack,
		getAllTracksOfEvent,
		createRound,
		getRounds,
		updateTrack,
		getAllCriteriaTemplates,
		assignCriteria
	} from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Clock, X, Plus } from "@lucide/svelte"

	// Route Params
	let seasonId = $derived($page.params.id || "")
	let eventId = $derived($page.params.eventId || "")

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
	let selectedMentorsForTrack = $state<string[]>([])
	let selectedJudgesForTrack = $state<string[]>([])
	let isCreatingTrack = $state(false)
	let createTrackMessage = $state("")
	let createTrackError = $state(false)

	// Edit Track Modal State
	let showEditTrackModal = $state(false)
	let editingTrackId = $state("")
	let editTrackName = $state("")
	let editTrackDescription = $state("")
	let selectedMentorsForEditTrack = $state<string[]>([])
	let selectedJudgesForEditTrack = $state<string[]>([])
	let selectedTeamsForEditTrack = $state<string[]>([])
	let isSavingEditTrack = $state(false)
	let editTrackMessage = $state("")
	let editTrackError = $state(false)

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

	// Assign Criteria Modal State
	let showAssignCriteriaModal = $state(false)
	let assigningRound = $state<any>(null)
	let criteriaTemplates = $state<any[]>([])
	let selectedTemplateId = $state("")
	let isAssigningCriteria = $state(false)
	let assignCriteriaMessage = $state("")
	let assignCriteriaError = $state(false)

	$effect(() => {
		if (selectedTemplateId) {
			assignCriteriaMessage = ""
			assignCriteriaError = false
		}
	})

	// Computed: Active teamless students for selection
	let activeTeamlessStudents = $derived(
		students.filter(
			(s) =>
				s.status === "ACTIVE" &&
				!getStudentTeamId(s) &&
				s.id !== (targetStudent ? targetStudent.id : null)
		)
	)

	function getTeamName(teamId: string) {
		const team = teams.find((t) => t.id === teamId)
		return team ? team.name : `#${teamId}`
	}

	function getStudentTeamId(student: any) {
		if (student.team_ids && student.team_ids.length > 0) {
			const team = teams.find((t) => student.team_ids.includes(t.id))
			if (team) return team.id
		}
		return null
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

			// Try API — with one automatic retry on failure to handle transient errors
			for (let attempt = 0; attempt < 2 && !eventItem; attempt++) {
				try {
					const res = await getEvent({ path: { eventId }, throwOnError: false })
					if (res.response?.ok && res.data) {
						eventItem = res.data
					} else if (attempt === 0) {
						// Small delay before retry
						await new Promise((r) => setTimeout(r, 300))
					}
				} catch (err) {
					console.error(`Error fetching event via API (attempt ${attempt + 1}):`, err)
					if (attempt === 0) {
						await new Promise((r) => setTimeout(r, 300))
					}
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
			} else {
				students = []
			}

			if (teamsRes.response?.ok && teamsRes.data) {
				teams = teamsRes.data
			} else {
				teams = []
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
				const errBody = apiErr as any
				createTeamMessage =
					errBody?.detail || errBody?.title || response?.statusText || "Failed to create team."
				createTeamError = true
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
			if (response?.ok && data) {
				eventTracks = data
			} else {
				eventTracks = []
			}
		} catch (err) {
			console.error("Error loading event tracks from API:", err)
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
			if (response?.ok && data) {
				eventRounds = data
			} else {
				eventRounds = []
			}
		} catch (err) {
			console.error("Error loading event rounds from API:", err)
			eventRounds = []
		}
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

	async function openAssignCriteriaModal(round: any) {
		assigningRound = round
		selectedTemplateId = ""
		assignCriteriaMessage = ""
		assignCriteriaError = false
		criteriaTemplates = []
		showAssignCriteriaModal = true
		try {
			const { data } = await getAllCriteriaTemplates({ throwOnError: false })
			if (data) criteriaTemplates = data
		} catch {
			// silently fail, user will see empty list
		}
	}

	async function handleAssignCriteria() {
		if (!selectedTemplateId) {
			assignCriteriaMessage = "Please select a criteria template."
			assignCriteriaError = true
			return
		}
		const template = criteriaTemplates.find((t) => t.id === selectedTemplateId)
		if (!template || !template.criteria?.length) {
			assignCriteriaMessage = "Selected template has no criteria."
			assignCriteriaError = true
			return
		}
		isAssigningCriteria = true
		assignCriteriaMessage = ""
		assignCriteriaError = false
		try {
			const criteriaIds = template.criteria.map((c: any) => c.id)
			const { response } = await assignCriteria({
				path: { roundId: assigningRound.id },
				body: criteriaIds,
				throwOnError: false
			})
			if (response?.ok) {
				showAssignCriteriaModal = false
				await loadEventRounds()
				alert(`Criteria assigned to round "${assigningRound.name}" successfully!`)
			} else {
				assignCriteriaMessage = "Failed to assign criteria. Please try again."
				assignCriteriaError = true
			}
		} catch (err: any) {
			assignCriteriaMessage = err.message || "An error occurred."
			assignCriteriaError = true
		} finally {
			isAssigningCriteria = false
		}
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
			const eventEnd = new Date(event.endTime || event.end_time)
			if (start <= eventEnd) {
				createRoundMessage = "Round timeframe must start after the registration period ends."
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
				createRoundMessage = "Failed to create round on server."
				createRoundError = true
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
		selectedMentorsForTrack = []
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

		if (selectedMentorsForTrack.length > 3) {
			createTrackMessage = "You can assign at most 3 mentors."
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

				// 2. Assign Mentors, Judges, and Teams if selected
				await updateTrack({
					path: { trackId },
					body: {
						name: newTrackName.trim(),
						description: newTrackDescription.trim(),
						mentor_ids: selectedMentorsForTrack,
						judge_ids: selectedJudgesForTrack,
						team_ids: selectedTeamsForTrack
					},
					throwOnError: false
				})

				showCreateTrackModal = false
				await fetchEventDetails()
				await loadEventTracks()
				alert(`Track "${newTrackName}" and assignments saved successfully!`)
			} else {
				const errBody = apiErr as any
				createTrackMessage = errBody?.detail || response?.statusText || "Failed to create track."
				createTrackError = true
			}
		} catch (err: any) {
			console.error("Error creating track and assignments:", err)
			createTrackMessage = err.message || "An error occurred."
			createTrackError = true
		} finally {
			isCreatingTrack = false
		}
	}

	function openEditTrackModal(track: any) {
		editingTrackId = track.id || ""
		editTrackName = track.name || ""
		editTrackDescription = track.description || ""

		const mentors = track.mentor_ids || track.mentorIds || []
		selectedMentorsForEditTrack = [...mentors]

		selectedJudgesForEditTrack = [...(track.judge_ids || track.judgeIds || [])]

		selectedTeamsForEditTrack = teams
			.filter((t) => t.track_id === track.id || t.trackId === track.id)
			.map((t) => t.id)

		editTrackMessage = ""
		editTrackError = false
		showEditTrackModal = true
	}

	async function handleUpdateTrack(e: Event) {
		e.preventDefault()
		if (!editTrackName.trim() || !editTrackDescription.trim()) {
			editTrackMessage = "Track name and description are required."
			editTrackError = true
			return
		}

		if (selectedMentorsForEditTrack.length > 3) {
			editTrackMessage = "You can assign at most 3 mentors."
			editTrackError = true
			return
		}

		if (
			selectedJudgesForEditTrack.length > 0 &&
			selectedJudgesForEditTrack.length !== 2 &&
			selectedJudgesForEditTrack.length !== 3
		) {
			editTrackMessage = "You must assign either 2 or 3 judges (or none)."
			editTrackError = true
			return
		}

		isSavingEditTrack = true
		editTrackMessage = ""
		editTrackError = false

		try {
			const {
				response,
				data: updatedTrack,
				error: apiErr
			} = await updateTrack({
				path: { trackId: editingTrackId },
				body: {
					name: editTrackName.trim(),
					description: editTrackDescription.trim(),
					mentor_ids: selectedMentorsForEditTrack,
					judge_ids: selectedJudgesForEditTrack,
					team_ids: selectedTeamsForEditTrack
				},
				throwOnError: false
			})

			if (response?.ok && updatedTrack) {
				showEditTrackModal = false
				await fetchEventDetails()
				await loadEventTracks()
				alert(`Track "${editTrackName}" updated successfully!`)
			} else {
				const errBody = apiErr as any
				editTrackMessage = errBody?.detail || response?.statusText || "Failed to update track."
				editTrackError = true
			}
		} catch (err: any) {
			console.error("Error updating track:", err)
			editTrackMessage = err.message || "An error occurred."
			editTrackError = true
		} finally {
			isSavingEditTrack = false
		}
	}

	function getLecturerName(lecturerId: string) {
		const lec = lecturers.find((l) => l.id === lecturerId)
		return lec ? lec.fullName || lec.name : lecturerId
	}

	$effect(() => {
		if (seasonId && eventId) {
			fetchEventDetails()
			loadLecturers()
			loadEventTracks()
			loadEventRounds()
		}
	})
</script>

<div class="coordinator-event-details">
	<!-- Top Action Bar -->
	<div class="action-bar">
		<button onclick={() => goto("/coordinator/seasons")} class="btn btn-outline back-btn">
			<ArrowLeft class="icon" />
			Back to Season Details
		</button>
	</div>

	{#if isLoading}
		<div class="loading-container">
			<div class="spinner"></div>
		</div>
	{:else if error}
		<div class="alert alert-error">
			<p class="alert-title">Error loading event</p>
			<p class="alert-desc">{error}</p>
		</div>
	{:else if event}
		<div class="content-wrapper">
			<!-- Event Header Card -->
			<div class="md3-card header-card">
				<div class="header-status">
					<span class="badge badge-primary">{event.status}</span>
				</div>

				<h1 class="event-title">{event.name}</h1>
				<p class="event-desc">{event.description || "No description provided."}</p>

				{#if eventTracks.length > 0}
					<div class="event-tracks-info">
						<span class="info-label">Tracks:</span>
						<div class="chip-group">
							{#each eventTracks as track}
								<span class="chip chip-surface" title={track.description}>
									{track.name}
								</span>
							{/each}
						</div>
					</div>
				{/if}

				<div class="event-time-info">
					<div class="time-item">
						<Clock class="icon text-primary" />
						<span
							><strong>Start:</strong> {formatDateTime(event.startTime || event.start_time)}</span
						>
					</div>
					<div class="time-item">
						<Clock class="icon text-error" />
						<span><strong>End:</strong> {formatDateTime(event.endTime || event.end_time)}</span>
					</div>
				</div>
			</div>

			<!-- Event Tracks and Experts Management Card -->
			<div class="md3-card section-card">
				<div class="section-header">
					<div class="section-title-group">
						<h2 class="section-title">Tracks</h2>
						<p class="section-desc">
							Manage and configure track details, assigned mentors, judges, and teams.
						</p>
					</div>
					{#if event.status?.toUpperCase() === "DRAFT"}
						<button onclick={openCreateTrackModal} class="btn btn-filled">
							<Plus class="icon" />
							Create Track
						</button>
					{/if}
				</div>

				{#if eventTracks.length > 0}
					<div class="track-grid">
						{#each eventTracks as track}
							<div class="track-card">
								<div class="track-card-header">
									<div class="track-title-wrapper">
										<h3 class="track-name">{track.name}</h3>
										<p class="track-desc">{track.description}</p>
									</div>
									<button
										type="button"
										onclick={() => openEditTrackModal(track)}
										class="btn btn-text btn-small"
									>
										Assign to track
									</button>
								</div>

								<div class="track-card-content">
									<div class="meta-group">
										<span class="meta-label">Mentor</span>
										<div class="chip-group small">
											{#if track.mentor_ids && track.mentor_ids.length > 0}
												{#each track.mentor_ids as mentorId}
													<span class="chip chip-mentor">{getLecturerName(mentorId)}</span>
												{/each}
											{:else}
												<span class="empty-text">No mentor assigned</span>
											{/if}
										</div>
									</div>

									<div class="meta-group">
										<span class="meta-label">Judges</span>
										<div class="chip-group small">
											{#if track.judge_ids && track.judge_ids.length > 0}
												{#each track.judge_ids as judgeId}
													<span class="chip chip-judge">{getLecturerName(judgeId)}</span>
												{/each}
											{:else}
												<span class="empty-text">No judges assigned</span>
											{/if}
										</div>
									</div>

									<div class="meta-group">
										<span class="meta-label">Assigned Teams</span>
										<div class="chip-group small">
											{#if teams.filter((t) => t.track_id === track.id || t.trackId === track.id).length > 0}
												{#each teams.filter((t) => t.track_id === track.id || t.trackId === track.id) as team}
													<span class="chip chip-team">{team.name}</span>
												{/each}
											{:else}
												<span class="empty-text">No teams assigned</span>
											{/if}
										</div>
									</div>
								</div>
							</div>
						{/each}
					</div>
				{:else}
					<div class="empty-state">
						<p class="empty-title">No tracks created for this event yet</p>
						<p class="empty-desc">
							Get started by creating your first track and assigning components.
						</p>
					</div>
				{/if}
			</div>

			<!-- Event Content Tabs Card -->
			<div class="md3-card tabs-card">
				<div class="tab-list">
					<button
						onclick={() => (activeTab = "students")}
						class="tab-btn {activeTab === 'students' ? 'active' : ''}"
					>
						Registered Students ({students.length})
					</button>
					<button
						onclick={() => (activeTab = "teams")}
						class="tab-btn {activeTab === 'teams' ? 'active' : ''}"
					>
						Teams ({teams.length})
					</button>
					<button
						onclick={() => (activeTab = "rounds")}
						class="tab-btn {activeTab === 'rounds' ? 'active' : ''}"
					>
						Rounds ({eventRounds.length})
					</button>
				</div>

				<div class="tab-content">
					{#if activeTab === "students"}
						<div class="table-container">
							<table class="data-table">
								<thead>
									<tr>
										<th>Student ID</th>
										<th>Name</th>
										<th>Email</th>
										<th>Type</th>
										<th>Team</th>
									</tr>
								</thead>
								<tbody>
									{#if students.length > 0}
										{#each students as student}
											<tr>
												<td class="font-mono"
													>{student.student_id || student.studentId || "NONE"}</td
												>
												<td class="font-bold">{student.fullName || student.name || "N/A"}</td>
												<td>{student.email}</td>
												<td
													>{student.is_external
														? student.school_name || student.schoolName || "External"
														: "FPT"}</td
												>
												<td>
													{#if getStudentTeamId(student)}
														<span class="text-primary font-bold"
															>{getTeamName(getStudentTeamId(student))}</span
														>
													{:else}
														<div class="action-cell">
															<span class="empty-text">No Team</span>
															{#if student.status === "ACTIVE"}
																<button
																	onclick={() => openCreateTeamModal(student)}
																	class="btn btn-outline btn-tiny"
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
											<td colspan="5" class="text-center empty-cell"
												>No students registered for this event yet.</td
											>
										</tr>
									{/if}
								</tbody>
							</table>
						</div>
					{:else if activeTab === "teams"}
						<div class="table-container">
							<table class="data-table">
								<thead>
									<tr>
										<th>Team Name</th>
										<th>Leader Name</th>
										<th>Status</th>
										<th>Track</th>
										<th>Mentor</th>
										<th>Judges</th>
									</tr>
								</thead>
								<tbody>
									{#if teams.length > 0}
										{#each teams as team}
											{@const track = eventTracks.find(
												(t) =>
													t.id === (team.track_id || team.trackId) ||
													t.name === (team.track_id || team.trackId)
											)}
											<tr>
												<td class="font-bold">{team.name}</td>
												<td class="font-medium"
													>{getStudentName(team.leader_id || team.leaderId)}</td
												>
												<td>
													<span
														class="badge {team.status === 'APPROVED' || team.status === 'ACTIVE'
															? 'badge-success'
															: team.status === 'PENDING'
																? 'badge-warning'
																: 'badge-error'}"
													>
														{team.status}
													</span>
												</td>
												<td>
													{#if team.track_id || team.trackId}
														<span class="badge badge-track"
															>{getTrackName(team.track_id || team.trackId)}</span
														>
													{:else}
														<span class="empty-text">Unassigned</span>
													{/if}
												</td>
												<td>
													{#if track && track.mentor_ids && track.mentor_ids.length > 0}
														<div class="chip-group small inline">
															{#each track.mentor_ids as mentorId}
																<span class="chip chip-mentor">{getLecturerName(mentorId)}</span>
															{/each}
														</div>
													{:else}
														<span class="empty-text">Unassigned</span>
													{/if}
												</td>
												<td>
													{#if track && track.judge_ids && track.judge_ids.length > 0}
														<div class="chip-group small inline">
															{#each track.judge_ids as judgeId}
																<span class="chip chip-judge">{getLecturerName(judgeId)}</span>
															{/each}
														</div>
													{:else}
														<span class="empty-text">Unassigned</span>
													{/if}
												</td>
											</tr>
										{/each}
									{:else}
										<tr>
											<td colspan="6" class="text-center empty-cell"
												>No teams created for this event yet.</td
											>
										</tr>
									{/if}
								</tbody>
							</table>
						</div>
					{:else if activeTab === "rounds"}
						{#if event.status?.toUpperCase() === "DRAFT"}
							<div class="tab-top-actions">
								<button onclick={openCreateRoundModal} class="btn btn-filled">
									<Plus class="icon" /> Create Round
								</button>
							</div>
						{/if}

						{#if eventRounds.length > 0}
							<div class="table-container">
								<table class="data-table">
									<thead>
										<tr>
											<th>Round Name</th>
											<th>Description</th>
											<th>Start Time</th>
											<th>End Time</th>
											<th>Criteria</th>
											{#if event.status?.toUpperCase() === "DRAFT"}
												<th>Actions</th>
											{/if}
										</tr>
									</thead>
									<tbody>
										{#each eventRounds as round}
											<tr>
												<td class="font-bold">{round.name}</td>
												<!-- CẬP NHẬT Ở ĐÂY: Thêm class truncate-text và title -->
												<td class="text-muted wrap-text">
													{round.description}
												</td>
												<td>{formatDateTime(round.startTime || round.start_time)}</td>
												<td>{formatDateTime(round.endTime || round.end_time)}</td>
												<td>
													{#if round.criteria && round.criteria.length > 0}
														<div class="chip-group small vertical">
															{#each round.criteria as criterion}
																<span class="chip chip-primary">
																	{criterion.name}
																	<span class="opacity-60">({criterion.weight}%)</span>
																</span>
															{/each}
														</div>
													{:else}
														<span class="empty-text">None assigned</span>
													{/if}
												</td>
												{#if event.status?.toUpperCase() === "DRAFT"}
													<td>
														<div
															style="display: flex; flex-direction: column; gap: 1rem; align-items: center;"
														>
															<button
																onclick={() => openAssignCriteriaModal(round)}
																class="btn btn-tonal btn-small"
															>
																{round.criteria && round.criteria.length > 0
																	? "Reassign"
																	: "Assign Criteria"}
															</button>
														</div>
													</td>
												{/if}
											</tr>
										{/each}
									</tbody>
								</table>
							</div>
						{:else}
							<div class="empty-state">
								<p class="empty-title">No rounds created for this event yet.</p>
							</div>
						{/if}
					{/if}
				</div>
			</div>
		</div>
	{/if}

	<!-- Modals (Create Team, Track, Round, Assign Criteria) -->
	<!-- The structure below applies to all modals to keep them consistent -->

	{#if showCreateTeamModal && targetStudent}
		<div class="modal-overlay">
			<div class="modal-surface">
				<div class="modal-header">
					<h3>Create Team</h3>
					<button onclick={() => (showCreateTeamModal = false)} class="btn-icon">
						<X class="icon" />
					</button>
				</div>
				<form onsubmit={handleCreateTeam} class="modal-form">
					{#if createTeamMessage}
						<div class="alert {createTeamError ? 'alert-error' : 'alert-success'}">
							{createTeamMessage}
						</div>
					{/if}
					<div class="form-group">
						<label>Team Leader</label>
						<input
							type="text"
							value="{targetStudent.fullName || targetStudent.name} ({targetStudent.email})"
							disabled
						/>
					</div>
					<div class="form-group">
						<label>Team Name *</label>
						<input type="text" bind:value={teamNameInput} placeholder="Enter team name" required />
					</div>
					<div class="form-group">
						<label>Description</label>
						<textarea
							bind:value={teamDescriptionInput}
							placeholder="Enter team description"
							rows="2"
						></textarea>
					</div>
					{#if activeTeamlessStudents.length > 0}
						<div class="form-group">
							<label>Add Team Members</label>
							<div class="selection-list">
								{#each activeTeamlessStudents as student}
									<label class="selection-item">
										<input
											type="checkbox"
											value={student.id}
											checked={selectedMembers.includes(student.id)}
											onchange={(e) => {
												if (e.currentTarget.checked)
													selectedMembers = [...selectedMembers, student.id]
												else selectedMembers = selectedMembers.filter((id) => id !== student.id)
											}}
										/>
										<div class="selection-info">
											<p class="name">{student.fullName || student.name}</p>
											<p class="desc">
												{student.is_external
													? student.school_name || student.schoolName || "External"
													: "FPT"} &bull; {student.email}
											</p>
										</div>
									</label>
								{/each}
							</div>
						</div>
					{/if}
					<div class="modal-actions">
						<button type="button" onclick={() => (showCreateTeamModal = false)} class="btn btn-text"
							>Cancel</button
						>
						<button type="submit" disabled={isCreatingTeam} class="btn btn-filled">
							{isCreatingTeam ? "Creating..." : "Create Team"}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	{#if showCreateTrackModal}
		<div class="modal-overlay">
			<div class="modal-surface large">
				<div class="modal-header">
					<h3>Create Track</h3>
					<button onclick={() => (showCreateTrackModal = false)} class="btn-icon"
						><X class="icon" /></button
					>
				</div>
				<form onsubmit={handleCreateTrack} class="modal-form">
					{#if createTrackMessage}
						<div class="alert {createTrackError ? 'alert-error' : 'alert-success'}">
							{createTrackMessage}
						</div>
					{/if}
					<div class="form-group">
						<label>Track Name *</label>
						<input type="text" bind:value={newTrackName} placeholder="Cybersecurity" required />
					</div>
					<div class="form-group">
						<label>Description *</label>
						<textarea
							bind:value={newTrackDescription}
							placeholder="Enter track description..."
							required
							rows="2"
						></textarea>
					</div>

					<div class="form-group">
						<label>Select Mentors (maximum 3)</label>
						<div class="selection-list">
							{#each lecturers.filter((l) => !selectedJudgesForTrack.includes(l.id)) as lecturer}
								<label class="selection-item">
									<input
										type="checkbox"
										value={lecturer.id}
										checked={selectedMentorsForTrack.includes(lecturer.id)}
										onchange={(e) => {
											if (e.currentTarget.checked)
												selectedMentorsForTrack = [...selectedMentorsForTrack, lecturer.id]
											else
												selectedMentorsForTrack = selectedMentorsForTrack.filter(
													(id) => id !== lecturer.id
												)
										}}
									/>
									<div class="selection-info">
										<p class="name">{lecturer.fullName || lecturer.name}</p>
										<p class="desc">{lecturer.email}</p>
									</div>
								</label>
							{/each}
						</div>
						<p class="helper-text">Selected: {selectedMentorsForTrack.length} mentor(s)</p>
					</div>

					<div class="form-group">
						<label>Select Judges (2 or 3 required)</label>
						<div class="selection-list">
							{#each lecturers.filter((l) => !selectedMentorsForTrack.includes(l.id)) as lecturer}
								<label class="selection-item">
									<input
										type="checkbox"
										value={lecturer.id}
										checked={selectedJudgesForTrack.includes(lecturer.id)}
										onchange={(e) => {
											if (e.currentTarget.checked)
												selectedJudgesForTrack = [...selectedJudgesForTrack, lecturer.id]
											else
												selectedJudgesForTrack = selectedJudgesForTrack.filter(
													(id) => id !== lecturer.id
												)
										}}
									/>
									<div class="selection-info">
										<p class="name">{lecturer.fullName || lecturer.name}</p>
										<p class="desc">{lecturer.email}</p>
									</div>
								</label>
							{/each}
						</div>
						<p class="helper-text">Selected: {selectedJudgesForTrack.length} judge(s)</p>
					</div>

					{#if teams.filter((t: any) => !t.track_id && !t.trackId).length > 0}
						<div class="form-group">
							<label>Assign Teams</label>
							<div class="selection-list">
								{#each teams.filter((t: any) => !t.track_id && !t.trackId) as team}
									<label class="selection-item">
										<input
											type="checkbox"
											value={team.id}
											checked={selectedTeamsForTrack.includes(team.id)}
											onchange={(e) => {
												if (e.currentTarget.checked)
													selectedTeamsForTrack = [...selectedTeamsForTrack, team.id]
												else
													selectedTeamsForTrack = selectedTeamsForTrack.filter(
														(id) => id !== team.id
													)
											}}
										/>
										<div class="selection-info">
											<p class="name">{team.name}</p>
											<p class="desc">Leader: {getStudentName(team.leader_id || team.leaderId)}</p>
										</div>
									</label>
								{/each}
							</div>
						</div>
					{/if}

					<div class="modal-actions">
						<button
							type="button"
							onclick={() => (showCreateTrackModal = false)}
							class="btn btn-text">Cancel</button
						>
						<button type="submit" disabled={isCreatingTrack} class="btn btn-filled">
							{isCreatingTrack ? "Creating..." : "Create Track"}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	{#if showEditTrackModal}
		<div class="modal-overlay">
			<div class="modal-surface large">
				<div class="modal-header">
					<h3>Edit Track</h3>
					<button onclick={() => (showEditTrackModal = false)} class="btn-icon"
						><X class="icon" /></button
					>
				</div>
				<form onsubmit={handleUpdateTrack} class="modal-form">
					{#if editTrackMessage}
						<div class="alert {editTrackError ? 'alert-error' : 'alert-success'}">
							{editTrackMessage}
						</div>
					{/if}
					<div class="form-group">
						<label>Track Name *</label>
						<input type="text" bind:value={editTrackName} required />
					</div>
					<div class="form-group">
						<label>Description *</label>
						<textarea bind:value={editTrackDescription} required rows="2"></textarea>
					</div>

					<div class="form-group">
						<label>Select Mentors (maximum 3)</label>
						<div class="selection-list">
							{#each lecturers.filter((l) => !selectedJudgesForEditTrack.includes(l.id)) as lecturer}
								<label class="selection-item">
									<input
										type="checkbox"
										value={lecturer.id}
										checked={selectedMentorsForEditTrack.includes(lecturer.id)}
										onchange={(e) => {
											if (e.currentTarget.checked)
												selectedMentorsForEditTrack = [...selectedMentorsForEditTrack, lecturer.id]
											else
												selectedMentorsForEditTrack = selectedMentorsForEditTrack.filter(
													(id) => id !== lecturer.id
												)
										}}
									/>
									<div class="selection-info">
										<p class="name">{lecturer.fullName || lecturer.name}</p>
										<p class="desc">{lecturer.email}</p>
									</div>
								</label>
							{/each}
						</div>
						<p class="helper-text">Selected: {selectedMentorsForEditTrack.length} mentor(s)</p>
					</div>

					<div class="form-group">
						<label>Select Judges (2 or 3 required)</label>
						<div class="selection-list">
							{#each lecturers.filter((l) => !selectedMentorsForEditTrack.includes(l.id)) as lecturer}
								<label class="selection-item">
									<input
										type="checkbox"
										value={lecturer.id}
										checked={selectedJudgesForEditTrack.includes(lecturer.id)}
										onchange={(e) => {
											if (e.currentTarget.checked)
												selectedJudgesForEditTrack = [...selectedJudgesForEditTrack, lecturer.id]
											else
												selectedJudgesForEditTrack = selectedJudgesForEditTrack.filter(
													(id) => id !== lecturer.id
												)
										}}
									/>
									<div class="selection-info">
										<p class="name">{lecturer.fullName || lecturer.name}</p>
										<p class="desc">{lecturer.email}</p>
									</div>
								</label>
							{/each}
						</div>
						<p class="helper-text">Selected: {selectedJudgesForEditTrack.length} judge(s)</p>
					</div>

					<div class="form-group">
						<label>Assign Teams</label>
						<div class="selection-list">
							{#each teams as team}
								<label class="selection-item">
									<input
										type="checkbox"
										value={team.id}
										checked={selectedTeamsForEditTrack.includes(team.id)}
										onchange={(e) => {
											if (e.currentTarget.checked)
												selectedTeamsForEditTrack = [...selectedTeamsForEditTrack, team.id]
											else
												selectedTeamsForEditTrack = selectedTeamsForEditTrack.filter(
													(id) => id !== team.id
												)
										}}
									/>
									<div class="selection-info">
										<p class="name">{team.name}</p>
										<p class="desc">
											Leader: {getStudentName(team.leader_id || team.leaderId)}
											{#if (team.track_id && team.track_id !== editingTrackId) || (team.trackId && team.trackId !== editingTrackId)}
												<span class="text-warning ml-1">(Currently in another track)</span>
											{/if}
										</p>
									</div>
								</label>
							{/each}
						</div>
					</div>

					<div class="modal-actions">
						<button type="button" onclick={() => (showEditTrackModal = false)} class="btn btn-text"
							>Cancel</button
						>
						<button type="submit" disabled={isSavingEditTrack} class="btn btn-filled">
							{isSavingEditTrack ? "Saving..." : "Save Changes"}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	{#if showCreateRoundModal}
		<div class="modal-overlay">
			<div class="modal-surface">
				<div class="modal-header">
					<h3>Create Round</h3>
					<button onclick={() => (showCreateRoundModal = false)} class="btn-icon"
						><X class="icon" /></button
					>
				</div>
				<form onsubmit={handleCreateRound} class="modal-form">
					{#if createRoundMessage}
						<div class="alert {createRoundError ? 'alert-error' : 'alert-success'}">
							{createRoundMessage}
						</div>
					{/if}
					<div class="form-group">
						<label>Round Name *</label>
						<input type="text" bind:value={newRoundName} placeholder="Enter round name" required />
					</div>
					<div class="form-group">
						<label>Description *</label>
						<textarea
							bind:value={newRoundDescription}
							placeholder="Enter round instructions"
							rows="3"
							required
						></textarea>
					</div>
					<div class="form-row">
						<div class="form-group">
							<label>Start Time *</label>
							<input type="datetime-local" bind:value={newRoundStartTime} required />
						</div>
						<div class="form-group">
							<label>End Time *</label>
							<input type="datetime-local" bind:value={newRoundEndTime} required />
						</div>
					</div>
					<div class="modal-actions">
						<button
							type="button"
							onclick={() => (showCreateRoundModal = false)}
							class="btn btn-text">Cancel</button
						>
						<button type="submit" disabled={isCreatingRound} class="btn btn-filled">
							{isCreatingRound ? "Creating..." : "Create"}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}

	{#if showAssignCriteriaModal && assigningRound}
		<div class="modal-overlay">
			<div class="modal-surface">
				<div class="modal-header">
					<div>
						<h3>Assign Criteria</h3>
						<p class="subtitle">Round: <span>{assigningRound.name}</span></p>
					</div>
					<button onclick={() => (showAssignCriteriaModal = false)} class="btn-icon"
						><X class="icon" /></button
					>
				</div>
				{#if assignCriteriaMessage}
					<div class="alert {assignCriteriaError ? 'alert-error' : 'alert-success'} mb-4">
						{assignCriteriaMessage}
					</div>
				{/if}
				<div class="modal-form">
					<div class="form-group">
						<label>Select Template *</label>
						{#if criteriaTemplates.length === 0}
							<p class="helper-text italic py-2">
								Loading templates… or none available. Create templates in Template Management first.
							</p>
						{:else}
							<select bind:value={selectedTemplateId}>
								<option value="">-- Choose a template --</option>
								{#each criteriaTemplates as template}
									<option value={template.id}>{template.description}</option>
								{/each}
							</select>
						{/if}
					</div>

					{#if selectedTemplateId}
						{@const previewTemplate = criteriaTemplates.find((t) => t.id === selectedTemplateId)}
						{#if previewTemplate?.criteria?.length > 0}
							<div class="preview-container">
								<div class="preview-header">Criteria Preview</div>
								<div class="preview-list">
									{#each previewTemplate.criteria as criterion}
										<div class="preview-item">
											<span class="preview-name">{criterion.name}</span>
											<span class="preview-weight">{criterion.weight}%</span>
										</div>
									{/each}
								</div>
							</div>
						{/if}
					{/if}

					<div class="modal-actions">
						<button
							type="button"
							onclick={() => (showAssignCriteriaModal = false)}
							class="btn btn-text">Cancel</button
						>
						<button
							type="button"
							onclick={handleAssignCriteria}
							disabled={isAssigningCriteria || !selectedTemplateId}
							class="btn btn-filled"
						>
							{isAssigningCriteria ? "Assigning..." : "Confirm"}
						</button>
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	/* Core MD3 Container & Layout */
	.coordinator-event-details {
		font-family: "Inter", system-ui, sans-serif;
		padding: 2rem;
		width: 80%;
		max-width: 1440px; /* Thông số khuyên dùng: Rộng rãi nhưng được kiểm soát */
		margin: 0 auto; /* Căn giữa màn hình */
		box-sizing: border-box;
		color: var(--md-sys-color-on-surface);
		background-color: var(--md-sys-color-surface);
		min-height: 100vh;

		* {
			box-sizing: border-box;
		}
	}

	.content-wrapper {
		display: flex;
		flex-direction: column;
		gap: 2rem;
		margin-top: 1.5rem;
	}

	/* Common MD3 Card Component */
	.md3-card {
		background-color: var(--md-sys-color-surface-container-lowest);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 24px;
		padding: 2rem;
		box-shadow:
			0px 1px 3px 0px rgba(0, 0, 0, 0.1),
			0px 1px 2px 0px rgba(0, 0, 0, 0.06);
		transition: box-shadow 0.2s ease-in-out;

		&:hover {
			box-shadow:
				0px 4px 6px -1px rgba(0, 0, 0, 0.1),
				0px 2px 4px -1px rgba(0, 0, 0, 0.06);
		}
	}

	/* Header Specific Styling */
	.header-card {
		background: linear-gradient(
			135deg,
			var(--md-sys-color-primary-container),
			var(--md-sys-color-secondary-container)
		);
		border: none;
		color: var(--md-sys-color-on-primary-container);

		.header-status {
			margin-bottom: 1.5rem;
		}

		.event-title {
			font-size: 2.25rem;
			font-weight: 800;
			margin: 0 0 1rem 0;
			letter-spacing: -0.02em;
			color: var(--md-sys-color-on-primary-container);
		}

		.event-desc {
			font-size: 1rem;
			line-height: 1.6;
			margin: 0 0 1.5rem 0;
			max-width: 800px;
			opacity: 0.9;
		}

		.event-tracks-info {
			display: flex;
			align-items: center;
			gap: 1rem;
			flex-wrap: wrap;
			margin-bottom: 1.5rem;

			.info-label {
				font-size: 0.75rem;
				font-weight: 700;
				text-transform: uppercase;
				letter-spacing: 0.05em;
				opacity: 0.8;
			}
		}

		.event-time-info {
			display: flex;
			gap: 2rem;
			flex-wrap: wrap;
			padding-top: 1.5rem;
			border-top: 1px solid rgba(0, 0, 0, 0.1);

			.time-item {
				display: flex;
				align-items: center;
				gap: 0.5rem;
				font-size: 0.875rem;

				.icon {
					width: 1.25rem;
					height: 1.25rem;
				}
				.text-primary {
					color: var(--md-sys-color-primary);
				}
				.text-error {
					color: var(--md-sys-color-error);
				}
			}
		}
	}

	/* Section & Tracks Grid */
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 1.5rem;

		&.minimal {
			align-items: center;
		}

		.section-title {
			font-size: 1.5rem;
			font-weight: 700;
			margin: 0 0 0.5rem 0;
			color: var(--md-sys-color-on-surface);
		}

		.section-desc {
			font-size: 0.875rem;
			color: var(--md-sys-color-on-surface-variant);
			margin: 0;
		}
	}

	.track-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
		gap: 1.5rem;
	}

	.track-card {
		background-color: var(--md-sys-color-surface-container-low);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 16px;
		padding: 1.5rem;
		display: flex;
		flex-direction: column;
		gap: 1.25rem;

		.track-card-header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem;

			.track-title-wrapper {
				flex: 1;
				.track-name {
					font-size: 1.125rem;
					font-weight: 700;
					color: var(--md-sys-color-primary);
					margin: 0 0 0.5rem 0;
				}
				.track-desc {
					font-size: 0.8125rem;
					color: var(--md-sys-color-on-surface-variant);
					margin: 0;
					line-height: 1.5;
				}
			}
		}

		.track-card-content {
			display: flex;
			flex-direction: column;
			gap: 1rem;
			padding-top: 1rem;
			border-top: 1px solid var(--md-sys-color-outline-variant);

			.meta-group {
				display: flex;
				flex-direction: column;
				gap: 0.5rem;

				.meta-label {
					font-size: 0.65rem;
					font-weight: 700;
					text-transform: uppercase;
					letter-spacing: 0.05em;
					color: var(--md-sys-color-on-surface-variant);
				}
			}
		}
	}

	/* Tabs & Tables */
	/* Chỉ giữ 1 class tab-top-actions này */
	.tab-top-actions {
		display: flex;
		justify-content: flex-end;
		align-items: center;
		margin-bottom: 1.5rem;
	}

	.wrap-text {
		white-space: normal; /* Cho phép chữ rớt xuống dòng */
		word-wrap: break-word; /* Bẻ dòng kể cả khi có từ dính liền quá dài */
		overflow-wrap: break-word;
		line-height: 1.5; /* Căn chỉnh khoảng cách giữa các dòng cho dễ đọc */
	}

	.data-table {
		width: 100%;
		table-layout: fixed;
		border-collapse: collapse;
		font-size: 0.875rem;

		th {
			background-color: var(--md-sys-color-surface-container-low);
			color: var(--md-sys-color-on-surface-variant);
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			padding: 1rem;
			text-align: left;
			border-bottom: 1px solid var(--md-sys-color-outline-variant);
			/* THÊM MỚI: Ép tiêu đề luôn nằm trên 1 dòng */
			white-space: nowrap;
		}

		td {
			padding: 1rem;
			color: var(--md-sys-color-on-surface);
			border-bottom: 1px solid var(--md-sys-color-outline-variant);
			vertical-align: middle;
		}

		tbody tr {
			transition: background-color 0.2s;
			&:hover {
				background-color: var(--md-sys-color-surface-container-lowest);
			}
			&:last-child td {
				border-bottom: none;
			}
		}

		.action-cell {
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}
	}

	.tabs-card {
		padding: 0;
		overflow: hidden;
	}

	.tab-list {
		display: flex;
		border-bottom: 1px solid var(--md-sys-color-outline-variant);
		background-color: var(--md-sys-color-surface-container-lowest);
		overflow-x: auto;

		.tab-btn {
			padding: 1.25rem 1.5rem;
			background: none;
			border: none;
			border-bottom: 2px solid transparent;
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--md-sys-color-on-surface-variant);
			cursor: pointer;
			transition: all 0.2s;
			white-space: nowrap;

			&:hover {
				color: var(--md-sys-color-on-surface);
				background-color: var(--md-sys-color-surface-container-low);
			}
			&.active {
				color: var(--md-sys-color-primary);
				border-bottom-color: var(--md-sys-color-primary);
			}
		}
	}

	.tab-content {
		padding: 1.5rem;
	}

	.table-container {
		width: 100%;
		overflow-x: auto;
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 12px;
	}

	.data-table {
		width: 100%;
		border-collapse: collapse;
		font-size: 0.875rem;

		th {
			background-color: var(--md-sys-color-surface-container-low);
			color: var(--md-sys-color-on-surface-variant);
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			padding: 1rem;
			text-align: left;
			border-bottom: 1px solid var(--md-sys-color-outline-variant);
		}

		td {
			padding: 1rem;
			color: var(--md-sys-color-on-surface);
			border-bottom: 1px solid var(--md-sys-color-outline-variant);
			vertical-align: middle;
		}

		tbody tr {
			transition: background-color 0.2s;
			&:hover {
				background-color: var(--md-sys-color-surface-container-lowest);
			}
			&:last-child td {
				border-bottom: none;
			}
		}

		.action-cell {
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}
	}

	/* Modals */
	.modal-overlay {
		position: fixed;
		inset: 0;
		z-index: 3000;
		background-color: rgba(0, 0, 0, 0.6);
		backdrop-filter: blur(4px);
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 1rem;
	}

	.modal-surface {
		background-color: var(--md-sys-color-surface-container-low);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 28px;
		padding: 2rem;
		width: 100%;
		max-width: 480px;
		max-height: 90vh;
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
		overflow-y: auto;
		box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.2);

		&.large {
			max-width: 600px;
		}

		.modal-header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;

			h3 {
				font-size: 1.25rem;
				font-weight: 800;
				margin: 0;
				color: var(--md-sys-color-on-surface);
			}
			.subtitle {
				font-size: 0.75rem;
				color: var(--md-sys-color-on-surface-variant);
				margin: 0.25rem 0 0 0;
				span {
					font-weight: 600;
				}
			}
		}
	}

	/* Forms */
	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;
	}

	.form-row {
		display: flex;
		flex-direction: column;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;

		label {
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			color: var(--md-sys-color-on-surface-variant);
		}

		input[type="text"],
		input[type="datetime-local"],
		select,
		textarea {
			width: 100%;
			padding: 0.75rem 1rem;
			background-color: var(--md-sys-color-surface-container-high);
			border: 1px solid var(--md-sys-color-outline);
			border-radius: 12px;
			color: var(--md-sys-color-on-surface);
			font-family: inherit;
			font-size: 0.875rem;
			transition:
				border-color 0.2s,
				box-shadow 0.2s;
			outline: none;

			&:focus {
				border-color: var(--md-sys-color-primary);
				box-shadow: 0 0 0 2px rgba(var(--md-sys-color-primary-rgb), 0.2);
			}
			&:disabled {
				opacity: 0.6;
				cursor: not-allowed;
			}
		}

		.helper-text {
			font-size: 0.7rem;
			color: var(--md-sys-color-on-surface-variant);
			margin: 0;
		}
	}

	.selection-list {
		max-height: 12rem;
		overflow-y: auto;
		background-color: var(--md-sys-color-surface-container-high);
		border: 1px solid var(--md-sys-color-outline);
		border-radius: 12px;
		padding: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 0.25rem;

		.selection-item {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			padding: 0.5rem;
			border-radius: 8px;
			cursor: pointer;
			transition: background-color 0.2s;
			text-transform: none;
			letter-spacing: normal;
			font-weight: normal;

			&:hover {
				background-color: var(--md-sys-color-surface-container-highest);
			}

			input[type="checkbox"] {
				width: 1.125rem;
				height: 1.125rem;
				accent-color: var(--md-sys-color-primary);
				cursor: pointer;
			}

			.selection-info {
				.name {
					font-size: 0.875rem;
					font-weight: 600;
					color: var(--md-sys-color-on-surface);
					margin: 0;
				}
				.desc {
					font-size: 0.75rem;
					color: var(--md-sys-color-on-surface-variant);
					margin: 0.125rem 0 0 0;
				}
			}
		}
	}

	.modal-actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		padding-top: 1rem;
		border-top: 1px solid var(--md-sys-color-outline-variant);
		margin-top: 0.5rem;
	}

	/* Buttons */
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		font-family: inherit;
		font-size: 0.875rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
		border-radius: 12px;

		&:disabled {
			opacity: 0.5;
			cursor: not-allowed;
		}

		.icon {
			width: 1.25rem;
			height: 1.25rem;
		}
	}

	.btn-filled {
		background-color: var(--md-sys-color-primary);
		color: var(--md-sys-color-on-primary);
		border: none;
		padding: 0.625rem 1.25rem;
		&:hover:not(:disabled) {
			background-color: var(--md-sys-color-primary-container);
			color: var(--md-sys-color-on-primary-container);
		}
	}

	.btn-outline {
		background-color: transparent;
		color: var(--md-sys-color-primary);
		border: 1px solid var(--md-sys-color-outline);
		padding: 0.625rem 1.25rem;
		&:hover:not(:disabled) {
			background-color: var(--md-sys-color-surface-container-high);
			border-color: var(--md-sys-color-primary);
		}
		&.back-btn {
			color: var(--md-sys-color-on-surface-variant);
			border-color: var(--md-sys-color-outline);
		}
	}

	.btn-text {
		background-color: transparent;
		color: var(--md-sys-color-primary);
		border: 1px solid transparent;
		padding: 0.625rem 1rem;
		&:hover:not(:disabled) {
			background-color: var(--md-sys-color-surface-container-high);
		}
	}

	.btn-tonal {
		background-color: var(--md-sys-color-secondary-container);
		color: var(--md-sys-color-on-secondary-container);
		border: none;
		padding: 0.625rem 1rem;
		&:hover:not(:disabled) {
			opacity: 0.9;
		}
	}

	.btn-small {
		padding: 0.375rem 0.75rem;
		font-size: 0.75rem;
		border-radius: 8px;
		border: 1px solid var(--md-sys-color-outline);
	}

	.btn-tiny {
		padding: 0.25rem 0.625rem;
		font-size: 0.7rem;
		border-radius: 6px;
	}

	.btn-icon {
		background: transparent;
		border: none;
		color: var(--md-sys-color-on-surface-variant);
		padding: 0.5rem;
		border-radius: 50%;
		cursor: pointer;
		display: flex;
		align-items: center;
		justify-content: center;
		&:hover {
			background-color: var(--md-sys-color-surface-container-high);
			color: var(--md-sys-color-on-surface);
		}
	}

	/* Chips & Badges */
	.badge {
		display: inline-block;
		padding: 0.25rem 0.75rem;
		border-radius: 99px;
		font-size: 0.7rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;

		&.badge-primary {
			background-color: var(--md-sys-color-primary);
			color: var(--md-sys-color-on-primary);
		}
		&.badge-success {
			background-color: rgba(34, 197, 94, 0.15);
			color: #16a34a;
			border: 1px solid rgba(34, 197, 94, 0.3);
		}
		&.badge-warning {
			background-color: rgba(245, 158, 11, 0.15);
			color: #d97706;
			border: 1px solid rgba(245, 158, 11, 0.3);
		}
		&.badge-error {
			background-color: rgba(239, 68, 68, 0.15);
			color: #dc2626;
			border: 1px solid rgba(239, 68, 68, 0.3);
		}
		&.badge-track {
			background-color: rgba(20, 184, 166, 0.15);
			color: #0d9488;
			border: 1px solid rgba(20, 184, 166, 0.3);
		}
	}

	.chip-group {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;

		&.small .chip {
			font-size: 0.65rem;
			padding: 0.125rem 0.5rem;
		}
		&.inline {
			display: inline-flex;
		}
		&.vertical {
			flex-direction: column;
			align-items: flex-start;
		}
	}

	.chip {
		display: inline-flex;
		align-items: center;
		padding: 0.25rem 0.75rem;
		border-radius: 8px;
		font-size: 0.75rem;
		font-weight: 600;
		border: 1px solid transparent;

		&.chip-surface {
			background-color: rgba(255, 255, 255, 0.2);
			color: var(--md-sys-color-on-primary-container);
			border-color: rgba(255, 255, 255, 0.3);
		}
		&.chip-primary {
			background-color: rgba(var(--md-sys-color-primary-rgb), 0.1);
			color: var(--md-sys-color-primary);
			border-color: rgba(var(--md-sys-color-primary-rgb), 0.2);
		}
		&.chip-mentor {
			background-color: rgba(139, 92, 246, 0.15);
			color: #7c3aed;
			border-color: rgba(139, 92, 246, 0.3);
		}
		&.chip-judge {
			background-color: rgba(14, 165, 233, 0.15);
			color: #0284c7;
			border-color: rgba(14, 165, 233, 0.3);
		}
		&.chip-team {
			background-color: rgba(16, 185, 129, 0.15);
			color: #059669;
			border-color: rgba(16, 185, 129, 0.3);
		}
	}

	/* Alerts & Empty States */
	.alert {
		padding: 1rem;
		border-radius: 12px;
		border: 1px solid;
		font-size: 0.875rem;

		.alert-title {
			font-weight: 700;
			margin: 0 0 0.25rem 0;
		}
		.alert-desc {
			margin: 0;
		}

		&.alert-error {
			background-color: var(--md-sys-color-error-container);
			color: var(--md-sys-color-on-error-container);
			border-color: var(--md-sys-color-error);
		}
		&.alert-success {
			background-color: rgba(16, 185, 129, 0.1);
			color: #059669;
			border-color: rgba(16, 185, 129, 0.3);
		}
	}

	.empty-state {
		padding: 3rem 2rem;
		text-align: center;
		border: 2px dashed var(--md-sys-color-outline-variant);
		border-radius: 16px;

		.empty-title {
			font-size: 1rem;
			font-weight: 700;
			color: var(--md-sys-color-on-surface);
			margin: 0 0 0.5rem 0;
		}
		.empty-desc {
			font-size: 0.875rem;
			color: var(--md-sys-color-on-surface-variant);
			margin: 0;
		}
	}

	.empty-text {
		font-size: 0.75rem;
		color: var(--md-sys-color-on-surface-variant);
		font-style: italic;
	}

	/* Helper Utilities */
	.font-mono {
		font-family: monospace;
		font-size: 0.8em;
	}
	.font-bold {
		font-weight: 700;
	}
	.font-medium {
		font-weight: 500;
	}
	.text-center {
		text-align: center;
	}
	.text-primary {
		color: var(--md-sys-color-primary);
	}
	.text-warning {
		color: #d97706;
		font-weight: 700;
	}
	.text-muted {
		color: var(--md-sys-color-on-surface-variant);
	}
	.mb-4 {
		margin-bottom: 1rem;
	}
	.py-2 {
		padding-top: 0.5rem;
		padding-bottom: 0.5rem;
	}
	.italic {
		font-style: italic;
	}
	.opacity-60 {
		opacity: 0.6;
	}

	/* Criteria Preview Box */
	.preview-container {
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 12px;
		overflow: hidden;

		.preview-header {
			background-color: var(--md-sys-color-surface-container);
			padding: 0.75rem 1rem;
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			color: var(--md-sys-color-on-surface-variant);
			border-bottom: 1px solid var(--md-sys-color-outline-variant);
		}

		.preview-list {
			display: flex;
			flex-direction: column;
		}

		.preview-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0.75rem 1rem;
			border-bottom: 1px solid var(--md-sys-color-outline-variant);

			&:last-child {
				border-bottom: none;
			}

			.preview-name {
				font-size: 0.875rem;
				font-weight: 500;
				color: var(--md-sys-color-on-surface);
			}
			.preview-weight {
				font-size: 0.75rem;
				font-weight: 700;
				background-color: rgba(var(--md-sys-color-primary-rgb), 0.1);
				color: var(--md-sys-color-primary);
				padding: 0.25rem 0.625rem;
				border-radius: 99px;
			}
		}
	}

	/* Loading Spinner */
	.loading-container {
		display: flex;
		justify-content: center;
		align-items: center;
		min-height: 50vh;
	}
	.spinner {
		width: 3rem;
		height: 3rem;
		border: 3px solid transparent;
		border-top-color: var(--md-sys-color-primary);
		border-bottom-color: var(--md-sys-color-primary);
		border-radius: 50%;
		animation: spin 1s linear infinite;
	}
	@keyframes spin {
		100% {
			transform: rotate(360deg);
		}
	}
</style>
