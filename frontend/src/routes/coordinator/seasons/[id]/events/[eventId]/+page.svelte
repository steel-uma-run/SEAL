<script lang="ts">
	import { onMount } from "svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import { getEvent, getInterestedParticipants, getAllTeamsOfEvents, createTeam } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Clock, X } from "@lucide/svelte"

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
				const res = await getEvent({ path: { seasonId, eventId }, throwOnError: false })
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
					path: { seasonId, eventId },
					throwOnError: false
				}),
				getAllTeamsOfEvents({
					path: { seasonId, eventId },
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

	onMount(() => {
		fetchEventDetails()
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
								<th class="py-3 px-4">Team ID</th>
								<th class="py-3 px-4">Team Name</th>
								<th class="py-3 px-4">Leader Name</th>
								<th class="py-3 px-4">Status</th>
							</tr>
						</thead>
						<tbody class="text-sm">
							{#if teams.length > 0}
								{#each teams as team}
									<tr
										class="border-b border-(--md-outline-variant)/50 text-(--md-on-surface) hover:bg-(--md-surface-container-highest)/30"
									>
										<td class="py-3 px-4 font-mono text-xs font-semibold">
											#{team.id}
										</td>
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
