<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import {
		getSelfProfile,
		getAllInvites,
		acceptInvite,
		declineInvite,
		inviteToTeam,
		getAllSeasons,
		getEventsInSeason,
		getInterestedParticipants,
		getAllTeamsOfEvents
	} from "$lib/api"
	import { goto } from "$app/navigation"
	import {
		Bell,
		CheckCircle,
		XCircle,
		Clock,
		Users,
		Star,
		UserPlus,
		LogOut,
		ArrowRightLeft
	} from "@lucide/svelte"

	let profile = $state<any>(null)
	let invites = $state<any[]>([])
	let myTeam = $state<any>(null)
	let isLoading = $state(true)
	let errorMessage = $state("")
	let processingId = $state<string | null>(null)

	let inviteStudentId = $state("")
	let isInviting = $state(false)
	let inviteMessage = $state("")
	let inviteError = $state(false)

	let studentUuid = $state<string | null>(null)
	let allParticipants = $state<any[]>([])
	let availableStudents = $state<any[]>([])
	let globalTeamsCache: any[] = []

	async function loadData() {
		try {
			// 1. Get profile
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (profileRes?.ok && profileData) {
				profile = profileData
			} else {
				goto("/auth/login")
				return
			}

			// Initialize team and invites to default values in case search yields nothing
			myTeam = null
			invites = []
			
			globalTeamsCache = []

			// 2. Fetch all seasons to find the student's team
			const { data: seasons, response: seasonsRes } = await getAllSeasons({ throwOnError: false })
			if (seasonsRes?.ok && seasons) {
				let resolvedTeam: any = null
				let resolvedStudentUuid = null
				let resolvedParticipants: any[] = []

				// Loop through seasons and events to locate the student's registration
				for (const season of seasons) {
					let allSeasonEvents: any[] = []

					// 2. Fetch from API
					const { data: events, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes?.ok && events) allSeasonEvents = events

					for (const event of allSeasonEvents) {
						let participantsList: any[] = []

						// 1. Check API for participants
						const { data: participants, response: participantsRes } =
							await getInterestedParticipants({
								path: { eventId: event.id },
								throwOnError: false
							})
						if (participantsRes?.ok && participants && participants.length > 0) {
							participantsList = participants
						}

						if (participantsList.length > 0) {
							const currentParticipant = participantsList.find(
								(p: any) => p.email === profile.email
							)
							if (currentParticipant) {
								resolvedStudentUuid = currentParticipant.id
								resolvedParticipants = participantsList

								let eventTeamsList: any[] = []
								// Always fetch teams to resolve invite team names
								const { data: teams, response: teamsRes } = await getAllTeamsOfEvents({
									path: { eventId: event.id } as any,
									throwOnError: false
								})
								if (teamsRes?.ok && teams && teams.length > 0) {
									eventTeamsList = teams
									globalTeamsCache.push(...teams)
								}
								// Store this globally or on the invite processing step so invites can use it
								// Since this is per-event, and invites are global, we will just use it to resolve resolvedTeam if they have one
								// But to map invite names, we should store a global map of teamId -> teamName
								eventTeamsList.forEach((t: any) => {
									if (typeof window !== "undefined") {
										// cache in session storage or just map it
									}
								})

								if ((currentParticipant.team_ids && currentParticipant.team_ids.length > 0) || eventTeamsList.some((t: any) => t.leader_id === currentParticipant.id || t.leaderId === currentParticipant.id)) {
									if (eventTeamsList.length > 0) {
										const team = eventTeamsList.find((t: any) => (currentParticipant.team_ids && currentParticipant.team_ids.includes(t.id)) || t.leader_id === currentParticipant.id || t.leaderId === currentParticipant.id)
										if (team) {
											resolvedTeam = team as any
											const teamMembers = participantsList
												.filter((p: any) => (p.team_ids && p.team_ids.includes(team.id)) || team.leader_id === p.id || team.leaderId === p.id)
												.map((p: any) => ({
													id: p.id,
													name: p.fullName || p.full_name || p.name || "Unknown",
													email: p.email,
													student_id: p.studentId || p.student_id || "",
													is_external: p.isExternal || p.is_external || false,
													role:
														p.id === team.leader_id || p.id === team.leaderId ? "Leader" : "Member"
												}))
											resolvedTeam.members = teamMembers
										}
									}
								}
								break
							}
						}
					}
					if (resolvedStudentUuid) break
				}

				studentUuid = resolvedStudentUuid
				myTeam = resolvedTeam
				allParticipants = resolvedParticipants
				availableStudents = resolvedParticipants.filter(
					(p: any) => !(p.team_ids && p.team_ids.length > 0) && p.id !== resolvedStudentUuid
				)
			}

			// 3. Get invites
			let invitesList: any[] = []
			const { data: invitesData, response: invitesRes } = await getAllInvites({
				throwOnError: false
			})
			if (invitesRes?.ok && invitesData) {
				invitesList = invitesData
			}

			if (invitesList.length > 0) {
				invites = invitesList.sort((a: any, b: any) => {
					if (a.status === "PENDING" && b.status !== "PENDING") return -1
					if (a.status !== "PENDING" && b.status === "PENDING") return 1
					return new Date(b.sent_at).getTime() - new Date(a.sent_at).getTime()
				})
			}

			// Fallback: If myTeam is null, but we have an ACCEPTED invite (fixes F5 refresh issue)
			if (!myTeam && invites.length > 0) {
				const acceptedInvite = invites.find((i: any) => i.status === "ACCEPTED")
				if (acceptedInvite && allParticipants.length > 0) {
					const targetTeamId = acceptedInvite.inviting_team_id
					const cachedTeam = globalTeamsCache.find((t: any) => t.id === targetTeamId)
					let fallbackTeam = cachedTeam || acceptedInvite.team || {
						id: targetTeamId,
						name: targetTeamId,
						status: "APPROVED"
					}

					const existingMembers = allParticipants
						.filter((p: any) => p.team_ids && p.team_ids.includes(targetTeamId))
						.map((p: any) => ({
							id: p.id,
							name: p.fullName || p.full_name || p.name || "Unknown",
							email: p.email,
							student_id: p.studentId || p.student_id || "",
							is_external: p.isExternal || p.is_external || false,
							role:
								p.id === fallbackTeam.leader_id || p.id === fallbackTeam.leaderId
									? "Leader"
									: "Member"
						}))

					const me = {
						id: profile.id,
						name: profile.fullName || profile.full_name || profile.name || "Me",
						email: profile.email,
						role: "Member"
					}

					if (!existingMembers.some((m: any) => m.id === me.id || m.email === me.email)) {
						fallbackTeam.members = [...existingMembers, me]
					} else {
						fallbackTeam.members = existingMembers
					}

					myTeam = fallbackTeam
				}
			}
		} catch (error) {
			errorMessage = "Error connecting to the server."
			console.error("loadData error:", error)
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadData()
	})

	async function handleAccept(inviteId: string) {
		processingId = inviteId
		try {
			const { response: res } = await acceptInvite({
				path: { inviteId },
				throwOnError: false
			})
			if (res?.ok) {
				// Manually extract team from invite just in case loadData fails to fetch it (due to GET cache)
				const acceptedInvite = invites.find((i) => i.id === inviteId)
				await loadData()
				if (!myTeam && acceptedInvite) {
					// Find the team in the teams list we just fetched (if it was cached) or from the invite
					const targetTeamId = acceptedInvite.inviting_team_id
					const cachedTeam = globalTeamsCache.find((t: any) => t.id === targetTeamId)
					let fallbackTeam = cachedTeam || acceptedInvite.team || { id: targetTeamId, name: targetTeamId }

					// Find existing members in allParticipants
					const existingMembers = allParticipants
						.filter((p: any) => p.team_ids && p.team_ids.includes(targetTeamId))
						.map((p: any) => ({
							id: p.id,
							name: p.fullName || p.full_name || p.name || "Unknown",
							email: p.email,
							student_id: p.studentId || p.student_id || "",
							is_external: p.isExternal || p.is_external || false,
							role:
								p.id === fallbackTeam.leader_id || p.id === fallbackTeam.leaderId
									? "Leader"
									: "Member"
						}))

					// Add the current user
					const me = {
						id: profile.id,
						name: profile.fullName || "Me",
						email: profile.email,
						role: "Member"
					}

					fallbackTeam.members = [...existingMembers, me]
					myTeam = fallbackTeam
				}
			} else {
				errorMessage = "Failed to accept invite: " + (res?.statusText || "Unknown error")
			}
		} catch (error) {
			alert("Error accepting invite.")
		} finally {
			processingId = null
		}
	}

	async function handleDecline(inviteId: string) {
		if (!confirm("Are you sure you want to decline this invite? This action is irreversible."))
			return
		processingId = inviteId
		try {
			const { response: res } = await declineInvite({
				path: { inviteId },
				throwOnError: false
			})
			if (res?.ok) {
				const idx = invites.findIndex((i) => i.id === inviteId)
				if (idx !== -1) invites[idx].status = "DECLINED"
			} else {
				alert("Failed to decline invite.")
			}
		} catch (error) {
			alert("Error declining invite.")
		} finally {
			processingId = null
		}
	}

	async function handleInviteMember(e?: Event) {
		if (e) e.preventDefault()
		if (!inviteStudentId.trim()) return

		isInviting = true
		inviteMessage = ""
		inviteError = false

		try {
			const invitee = allParticipants.find(
				(p: any) =>
					(p.studentId &&
						p.studentId.trim().toLowerCase() === inviteStudentId.trim().toLowerCase()) ||
					(p.student_id &&
						p.student_id.trim().toLowerCase() === inviteStudentId.trim().toLowerCase()) ||
					(p.email && p.email.trim().toLowerCase() === inviteStudentId.trim().toLowerCase())
			)

			if (!invitee) {
				inviteError = true
				inviteMessage =
					"Failed to send invite: Student not found or has not registered/marked interest in this event."
				isInviting = false
				return
			}

			const { response: res, error } = await inviteToTeam({
				path: {
					teamId: myTeam.id,
					studentId: invitee.id
				},
				throwOnError: false
			})
			if (res?.ok) {
				inviteMessage = "Invitation sent successfully!"
				inviteStudentId = ""
			} else {
				inviteError = true
				const errorDetails = error || (await res?.json().catch(() => null))
				inviteMessage = `Failed to send invite: ${errorDetails?.detail || errorDetails?.message || errorDetails?.title || res?.statusText || "User may already be in a team or has pending invites."}`
			}
		} catch (err: any) {
			inviteError = true
			inviteMessage = `Error sending invite: ${err?.message || "Unknown error"}`
		} finally {
			isInviting = false
		}
	}

	function formatDate(dateString: string) {
		return new Intl.DateTimeFormat("en-US", {
			month: "short",
			day: "numeric",
			hour: "2-digit",
			minute: "2-digit"
		}).format(new Date(dateString))
	}

	function handleLeaveTeam() {
		// BR-33: Leader cannot leave without transferring leadership
		if (myTeam && studentUuid === myTeam.leader_id) {
			alert(
				"Error: You are the team leader. You must transfer leadership to another member before leaving the team."
			)
			return
		}

		// BR-32: Cannot leave team after season has officially begun (ACTIVE status)
		// Assuming we can check season status if we had it, but for now we enforce the rule in UI
		if (myTeam && myTeam.status === "ACTIVE") {
			alert("Error: You cannot leave the team after the season has officially begun.")
			return
		}

		alert(`The "Leave Team" action is not currently supported by the backend API.`)
	}

	function handleTransferLeadership() {
		alert(`The "Transfer Leadership" action is not currently supported by the backend API.`)
	}
</script>

<svelte:head>
	<title>Teams - Student</title>
</svelte:head>

<div class="max-w-4xl mx-auto w-full p-4 md:p-8">
	<a
		href="/student"
		class="inline-flex items-center gap-2 transition-colors mb-8 font-medium
        {theme.darkMode
			? 'text-zinc-400 hover:text-orange-400'
			: 'text-gray-500 hover:text-orange-600'}"
	>
		<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path>
		</svg>
		Back to Dashboard
	</a>

	<div class="flex items-center gap-3 mb-8">
		<div
			class="p-3 rounded-xl {theme.darkMode
				? 'bg-orange-950/40 text-orange-400'
				: 'bg-orange-100 text-orange-600'}"
		>
			<Users class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Teams</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Manage your team or pending invitations
			</p>
		</div>
	</div>

	{#if !myTeam && !isLoading}
		<div class="mb-6 flex justify-end">
			<a
				href="/student/create-team"
				class="px-5 py-2.5 bg-[#f26f21] hover:bg-[#d85c14] text-white font-semibold rounded-xl shadow-sm transition-colors flex items-center gap-2"
			>
				<Users class="w-5 h-5" />
				Create New Team
			</a>
		</div>
	{/if}

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-orange-500"></div>
		</div>
	{:else if errorMessage}
		<div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm">
			<p class="text-sm text-red-700">{errorMessage}</p>
		</div>
	{:else}
		{#if myTeam}
			<div
				class="mb-8 p-6 md:p-8 rounded-3xl border transition-all {theme.darkMode
					? 'bg-zinc-900 border-orange-900/50 shadow-[0_4px_30px_rgba(234,88,12,0.1)]'
					: 'bg-white border-gray-200 shadow-sm'}"
			>
				<div class="flex items-center justify-between gap-4 flex-wrap mb-4">
					<div class="flex items-center gap-3">
						<div class="p-2.5 rounded-xl bg-orange-500 text-white shadow-sm">
							<Star class="w-6 h-6" />
						</div>
						<div>
							<h3
								class="text-xs font-semibold uppercase tracking-wider text-orange-600 dark:text-orange-400"
							>
								My Current Team
							</h3>
							<h2
								class="text-2xl md:text-3xl font-bold {theme.darkMode
									? 'text-zinc-100'
									: 'text-gray-900'}"
							>
								{myTeam.name || "Untitled Team"}
							</h2>
						</div>
					</div>
					<div class="flex items-center gap-3">
						{#if myTeam.status === "PENDING"}
							<span
								class="text-amber-700 dark:text-amber-400 bg-amber-100 dark:bg-amber-500/10 px-3 py-1 rounded-lg border border-amber-200 dark:border-amber-500/20 font-semibold text-sm"
								>Status: Pending Approval</span
							>
						{:else if myTeam.status === "APPROVED"}
							<span
								class="text-green-600 dark:text-green-500 bg-green-50 dark:bg-green-950/30 px-3 py-1 rounded-lg border border-green-200 dark:border-green-900/50 font-semibold text-sm"
								>Status: Approved</span
							>
						{/if}
						{#if myTeam.leader_id === profile.id}
							<a
								href="/student/submit-project"
								class="px-4 py-1.5 bg-[#f26f21] hover:bg-[#d85c14] text-white rounded-lg font-bold text-sm shadow-sm transition-colors flex items-center gap-2"
							>
								<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
									></path></svg
								>
								Submission
							</a>
						{/if}
					</div>
				</div>

				<div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8 mb-8">
					<div
						class="p-5 rounded-2xl border {theme.darkMode
							? 'bg-zinc-950/50 border-zinc-800'
							: 'bg-white border-gray-100'}"
					>
						<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-2">Team ID</p>
						<p
							class="font-mono text-sm font-semibold {theme.darkMode
								? 'text-zinc-300'
								: 'text-gray-700'}"
						>
							{myTeam.id}
						</p>
					</div>
					<div
						class="p-5 rounded-2xl border {theme.darkMode
							? 'bg-zinc-950/50 border-zinc-800'
							: 'bg-white border-gray-100'}"
					>
						<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-2">My Role</p>
						<p
							class="font-semibold text-sm {studentUuid === myTeam.leader_id ||
							studentUuid === myTeam.leaderId
								? 'text-orange-500'
								: 'text-green-500'}"
						>
							{studentUuid === myTeam.leader_id || studentUuid === myTeam.leaderId
								? "Team Leader"
								: "Member"}
						</p>
					</div>
				</div>

				<div class="border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-8">
					<div class="flex justify-between items-center mb-4">
						<h3 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
							Team Members ({myTeam.members?.length || 1}/5)
						</h3>
					</div>

					<div class="space-y-3 mb-8">
						{#if myTeam.members && myTeam.members.length > 0}
							{#each myTeam.members as member}
								<div
									class="flex items-center justify-between p-4 rounded-xl border {theme.darkMode
										? 'bg-zinc-950/50 border-zinc-800'
										: 'bg-white border-gray-100'}"
								>
									<div class="flex items-center gap-3">
										<div>
											<p
												class="text-sm font-bold {theme.darkMode
													? 'text-zinc-300'
													: 'text-gray-700'}"
											>
												{member.name || "Unknown Member"}
												{#if member.id === studentUuid}
													<span class="text-xs font-medium text-gray-400 ml-1">(You)</span>
												{/if}
											</p>
											<p class="text-xs text-gray-500 mt-0.5">
												{member.email || "No Email"}
												{member.is_external ? " (External)" : ""}
											</p>
										</div>
									</div>
									{#if member.role === "Leader" || member.id === myTeam.leader_id || member.id === myTeam.leaderId}
										<span
											class="text-xs font-bold text-orange-500 bg-orange-100 dark:bg-orange-500/10 px-2 py-1 rounded"
											>Leader</span
										>
									{:else}
										<span
											class="text-xs font-semibold text-black bg-white border border-gray-300 dark:text-white dark:bg-black dark:border-zinc-700 px-2 py-1 rounded"
											>Member</span
										>
									{/if}
								</div>
							{/each}
						{/if}
					</div>

					{#if studentUuid === myTeam.leader_id}
						<div
							class="p-6 rounded-2xl border {theme.darkMode
								? 'bg-zinc-950/80 border-zinc-800'
								: 'bg-gray-50 border-gray-200'}"
						>
							<h4
								class="font-bold mb-2 flex items-center gap-2 {theme.darkMode
									? 'text-zinc-100'
									: 'text-gray-800'}"
							>
								<UserPlus class="w-5 h-5 text-orange-500" />
								Invite New Member
							</h4>
							<p class="text-sm mb-4 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
								Enter a student ID to invite them to your team. Teams can have up to 5 members.
							</p>

							{#if (myTeam.members?.length || 1) >= 5}
								<div
									class="p-3 bg-amber-50 dark:bg-amber-950/30 text-amber-600 dark:text-amber-400 rounded-xl text-sm font-medium border border-amber-200 dark:border-amber-900/50"
								>
									Your team has reached the maximum size limit (5 members).
								</div>
							{:else}
								<form onsubmit={handleInviteMember} class="flex gap-3">
									<input
										type="text"
										bind:value={inviteStudentId}
										placeholder="Enter student ID (e.g. SE160123)..."
										required
										class="flex-1 rounded-xl p-3 border focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all {theme.darkMode
											? 'bg-zinc-900 border-zinc-700 text-zinc-100'
											: 'bg-white border-gray-300'}"
									/>
									<button
										type="submit"
										disabled={isInviting || !inviteStudentId.trim()}
										class="px-5 py-3 bg-orange-500 hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors disabled:opacity-50 min-w-[120px]"
									>
										{isInviting ? "Sending..." : "Send Invite"}
									</button>
								</form>
								{#if inviteMessage}
									<p
										class="mt-3 text-sm font-medium {inviteError
											? 'text-red-500'
											: 'text-green-500'}"
									>
										{inviteMessage}
									</p>
								{/if}

								<div
									class="mt-6 border-t {theme.darkMode
										? 'border-zinc-800'
										: 'border-gray-200'} pt-4"
								>
									<h5
										class="text-sm font-bold mb-3 flex items-center gap-2 {theme.darkMode
											? 'text-zinc-200'
											: 'text-gray-700'}"
									>
										Students Without a Team ({availableStudents.length})
									</h5>
									{#if availableStudents.length > 0}
										<div class="max-h-60 overflow-y-auto pr-2 space-y-2 custom-scrollbar">
											{#each availableStudents as student}
												<div
													class="flex items-center justify-between p-3 rounded-xl border {theme.darkMode
														? 'bg-zinc-900 border-zinc-800 hover:border-zinc-700'
														: 'bg-white border-gray-100 hover:border-orange-200'} transition-colors"
												>
													<div>
														<p
															class="text-sm font-bold {theme.darkMode
																? 'text-zinc-200'
																: 'text-gray-800'}"
														>
															{student.fullName || student.full_name || student.name || "Unknown"}
														</p>
														<p class="text-xs text-gray-500 mt-0.5">
															{student.studentId || student.student_id || student.email || "No ID"}
														</p>
													</div>
													<button
														onclick={() => {
															inviteStudentId =
																student.studentId || student.student_id || student.email || ""
															handleInviteMember()
														}}
														class="text-xs font-bold px-4 py-2 rounded-lg bg-orange-100 text-orange-600 hover:bg-orange-200 dark:bg-orange-900/30 dark:text-orange-400 dark:hover:bg-orange-900/50 transition-colors"
													>
														Invite
													</button>
												</div>
											{/each}
										</div>
									{:else}
										<div
											class="p-4 text-center rounded-xl border border-dashed {theme.darkMode
												? 'bg-zinc-950/50 border-zinc-800 text-zinc-400'
												: 'bg-gray-50 border-gray-200 text-gray-500'}"
										>
											<p class="text-sm">
												There are no available students without a team to invite right now.
											</p>
										</div>
									{/if}
								</div>
							{/if}
						</div>

						<div
							class="mt-8 flex gap-4 border-t {theme.darkMode
								? 'border-zinc-800'
								: 'border-gray-200'} pt-6"
						>
							<button
								onclick={handleTransferLeadership}
								class="flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg border transition-colors {theme.darkMode
									? 'border-zinc-700 text-zinc-300 hover:bg-zinc-800'
									: 'border-gray-300 text-gray-700 hover:bg-gray-100'}"
							>
								<ArrowRightLeft class="w-4 h-4" />
								Transfer Leadership
							</button>
						</div>
					{/if}

					<div class="mt-4 flex gap-4">
						<button
							onclick={handleLeaveTeam}
							class="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-red-500 rounded-lg hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
						>
							<LogOut class="w-4 h-4" />
							Leave Team
						</button>
					</div>
				</div>
			</div>
		{:else}
			{#if invites.length === 0}
				<div
					class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode
						? 'border-zinc-800 text-zinc-500'
						: 'border-gray-200 text-gray-400'}"
				>
					<Users class="w-12 h-12 mx-auto mb-4 opacity-50" />
					<h3 class="text-lg font-medium">No Pending Invitations</h3>
					<p class="text-sm mt-1">You haven't received any team invitations yet.</p>
				</div>
			{:else}
				<div class="space-y-4">
					{#each invites as invite}
						<div
							class="p-5 rounded-2xl border transition-all {theme.darkMode
								? 'bg-zinc-900 border-zinc-800 hover:border-zinc-700'
								: 'bg-white border-gray-100 shadow-sm hover:shadow-md'}"
						>
							<div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
								<div class="flex items-start gap-4">
									<div
										class="p-2.5 rounded-full mt-1 {theme.darkMode
											? 'bg-blue-950/40 text-blue-400'
											: 'bg-blue-50 text-blue-600'}"
									>
										<Users class="w-5 h-5" />
									</div>
									<div>
										<h4
											class="font-semibold text-lg {theme.darkMode
												? 'text-zinc-100'
												: 'text-gray-900'}"
										>
											Team Invitation
										</h4>
										<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
											You have been invited to join Team <span
												class="font-mono font-bold text-orange-500"
												>{invite.team?.name || invite.inviting_team_id || "Unknown"}</span
											>.
										</p>
										<div
											class="flex items-center gap-4 mt-3 text-xs font-medium {theme.darkMode
												? 'text-zinc-500'
												: 'text-gray-400'}"
										>
											<span class="flex items-center gap-1.5"
												><Clock class="w-3.5 h-3.5" /> Sent: {formatDate(invite.sent_at)}</span
											>
											{#if invite.status?.toUpperCase() === "PENDING"}
												<span
													class="text-amber-700 dark:text-amber-400 bg-amber-100 dark:bg-amber-500/10 px-2 py-0.5 rounded-md border border-amber-200 dark:border-amber-500/20 font-semibold"
													>Pending</span
												>
											{:else if invite.status?.toUpperCase() === "ACCEPTED"}
												<span
													class="text-green-600 dark:text-green-500 bg-green-50 dark:bg-green-950/30 px-2 py-0.5 rounded-md border border-green-200 dark:border-green-900/50"
													>Accepted</span
												>
											{:else if invite.status?.toUpperCase() === "DECLINED"}
												<span
													class="text-red-600 dark:text-red-500 bg-red-50 dark:bg-red-950/30 px-2 py-0.5 rounded-md border border-red-200 dark:border-red-900/50"
													>Declined</span
												>
											{/if}
										</div>
									</div>
								</div>

								{#if invite.status?.toUpperCase() === "PENDING"}
									<div class="flex items-center gap-3 self-start sm:self-center ml-14 sm:ml-0">
										<button
											onclick={() => handleAccept(invite.id)}
											disabled={processingId === invite.id}
											class="flex items-center gap-1.5 px-4 py-2 bg-green-500 hover:bg-green-600 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50 border-0 cursor-pointer"
										>
											<CheckCircle class="w-4 h-4" />
											{processingId === invite.id ? "Processing..." : "Accept"}
										</button>
										<button
											onclick={() => handleDecline(invite.id)}
											disabled={processingId === invite.id}
											class="flex items-center gap-1.5 px-4 py-2 border {theme.darkMode
												? 'border-zinc-700 hover:bg-zinc-800 text-zinc-300'
												: 'border-gray-200 hover:bg-gray-50 text-gray-600'} font-medium text-sm rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
										>
											<XCircle class="w-4 h-4" />
											Decline
										</button>
									</div>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		{/if}
	{/if}
</div>
