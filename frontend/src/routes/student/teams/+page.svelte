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

			// 2. Fetch all seasons to find the student's team
			const { data: seasons, response: seasonsRes } = await getAllSeasons({ throwOnError: false })
			if (seasonsRes?.ok && seasons) {
				let resolvedTeam: any = null
				let resolvedStudentUuid = null
				let resolvedParticipants: any[] = []

				// Loop through seasons and events to locate the student's registration
				for (const season of seasons) {
					const { data: events, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes?.ok && events) {
						for (const event of events) {
							// Get interested participants to find the student's student UUID and team ID
							const { data: participants, response: participantsRes } =
								await getInterestedParticipants({
									path: { seasonId: season.id, eventId: event.id },
									throwOnError: false
								})
							if (participantsRes?.ok && participants) {
								const currentParticipant = participants.find((p: any) => p.email === profile.email)
								if (currentParticipant) {
									resolvedStudentUuid = currentParticipant.id
									resolvedParticipants = participants

									if (currentParticipant.team_id) {
										// Fetch all teams of this event to find details
										const { data: teams, response: teamsRes } = await getAllTeamsOfEvents({
											path: { seasonId: season.id, eventId: event.id },
											throwOnError: false
										})
										if (teamsRes?.ok && teams) {
											const team = teams.find((t: any) => t.id === currentParticipant.team_id)
											if (team) {
												resolvedTeam = team as any
												const teamMembers = participants
													.filter((p: any) => p.team_id === team.id)
													.map((p: any) => ({
														id: p.id,
														name: p.name,
														email: p.email,
														student_id: p.studentId,
														role: p.id === team.leader_id ? "Leader" : "Member"
													}))
												resolvedTeam.members = teamMembers
											}
										}
									}
									break
								}
							}
						}
					}
					if (resolvedStudentUuid) break
				}

				studentUuid = resolvedStudentUuid
				myTeam = resolvedTeam
				allParticipants = resolvedParticipants
			}

			// 3. Get invites
			const { data: invitesData, response: invitesRes } = await getAllInvites({
				throwOnError: false
			})
			if (invitesRes?.ok && invitesData) {
				invites = invitesData.sort((a: any, b: any) => {
					if (a.status === "PENDING" && b.status !== "PENDING") return -1
					if (a.status !== "PENDING" && b.status === "PENDING") return 1
					return new Date(b.sent_at).getTime() - new Date(a.sent_at).getTime()
				})
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
				await loadData()
			} else {
				alert("Failed to accept invite: Please try again.")
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

	async function handleInviteMember(e: Event) {
		e.preventDefault()
		if (!inviteStudentId.trim()) return

		isInviting = true
		inviteMessage = ""
		inviteError = false

		try {
			const invitee = allParticipants.find(
				(p: any) =>
					p.studentId && p.studentId.trim().toLowerCase() === inviteStudentId.trim().toLowerCase()
			)

			if (!invitee) {
				inviteError = true
				inviteMessage =
					"Failed to send invite: Student not found or has not registered/marked interest in this event."
				isInviting = false
				return
			}

			const { response: res } = await inviteToTeam({
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
				inviteMessage =
					"Failed to send invite: User may already be in a team or has pending invites."
			}
		} catch (error) {
			inviteError = true
			inviteMessage = "Error sending invite."
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
			alert("Error: You are the team leader. You must transfer leadership to another member before leaving the team.")
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
					: 'bg-orange-50/50 border-orange-100 shadow-[0_4px_20px_rgba(234,88,12,0.05)]'}"
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
							class="font-semibold text-sm {studentUuid === myTeam.leader_id
								? 'text-orange-500'
								: 'text-green-500'}"
						>
							{studentUuid === myTeam.leader_id ? "Team Leader" : "Member"}
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
										<div
											class="w-10 h-10 rounded-full bg-gray-200 dark:bg-zinc-800 flex items-center justify-center text-gray-500 dark:text-zinc-400 font-semibold"
										>
											<Users class="w-5 h-5" />
										</div>
										<div>
											<p
												class="text-sm font-bold {theme.darkMode
													? 'text-zinc-300'
													: 'text-gray-700'}"
											>
												{member.name || "Unknown Member"}
											</p>
											<p class="text-xs text-gray-500 mt-0.5">
												{member.student_id || member.email || ""}
												{member.id === studentUuid ? "(You)" : ""}
											</p>
										</div>
									</div>
									{#if member.id === myTeam.leader_id}
										<span
											class="text-xs font-bold text-orange-500 bg-orange-100 dark:bg-orange-500/10 px-2 py-1 rounded"
											>Leader</span
										>
									{:else}
										<span
											class="text-xs font-semibold text-gray-500 bg-gray-100 dark:bg-zinc-800 px-2 py-1 rounded"
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
												>{invite.inviting_team_id || "Unknown"}</span
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
											{#if invite.status === "PENDING"}
												<span
													class="text-amber-700 dark:text-amber-400 bg-amber-100 dark:bg-amber-500/10 px-2 py-0.5 rounded-md border border-amber-200 dark:border-amber-500/20 font-semibold"
													>Pending</span
												>
											{:else if invite.status === "ACCEPTED"}
												<span
													class="text-green-600 dark:text-green-500 bg-green-50 dark:bg-green-950/30 px-2 py-0.5 rounded-md border border-green-200 dark:border-green-900/50"
													>Accepted</span
												>
											{:else if invite.status === "DECLINED"}
												<span
													class="text-red-600 dark:text-red-500 bg-red-50 dark:bg-red-950/30 px-2 py-0.5 rounded-md border border-red-200 dark:border-red-900/50"
													>Declined</span
												>
											{/if}
										</div>
									</div>
								</div>

								{#if invite.status === "PENDING"}
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
