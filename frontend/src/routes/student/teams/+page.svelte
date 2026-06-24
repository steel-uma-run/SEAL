<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { getInvites, acceptInvite, declineInvite } from "$lib/api/invites"
	import { getMyTeam, inviteMember } from "$lib/api/teams"
	import { getProfile } from "$lib/api/profile"
	import { goto } from "$app/navigation"
	import { Bell, CheckCircle, XCircle, Clock, Users, Star, UserPlus, LogOut, ArrowRightLeft } from "@lucide/svelte"

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

	async function loadData() {
		try {
			const profileRes = await getProfile()
			if (profileRes.ok) {
				profile = await profileRes.json()
			}

			const teamRes = await getMyTeam()
			if (teamRes.ok) {
				myTeam = await teamRes.json()
			} else {
				myTeam = null
			}

			const res = await getInvites()
			if (res.ok) {
				const data = await res.json()
				if (Array.isArray(data)) {
					invites = data.sort((a, b) => {
						if (a.status === "PENDING" && b.status !== "PENDING") return -1
						if (a.status !== "PENDING" && b.status === "PENDING") return 1
						return new Date(b.sent_at).getTime() - new Date(a.sent_at).getTime()
					})
				}
			}
		} catch (error) {
			errorMessage = "Error connecting to the server."
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
			const res = await acceptInvite(inviteId)
			if (res.ok) {
				await loadData()
			} else {
				const err = await res.text()
				alert(`Failed to accept invite: ${err || "Please try again."}`)
			}
		} catch (error) {
			alert("Error accepting invite.")
		} finally {
			processingId = null
		}
	}

	async function handleDecline(inviteId: string) {
		if (!confirm("Are you sure you want to decline this invite? This action is irreversible.")) return
		processingId = inviteId
		try {
			const res = await declineInvite(inviteId)
			if (res.ok) {
				const idx = invites.findIndex(i => i.id === inviteId)
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
			const res = await inviteMember(myTeam.id, inviteStudentId.trim())
			if (res.ok) {
				inviteMessage = "Invitation sent successfully!"
				inviteStudentId = ""
			} else {
				const errText = await res.text()
				inviteError = true
				inviteMessage = `Failed to send invite: ${errText || "User may not exist or is already in a team."}`
			}
		} catch (error) {
			inviteError = true
			inviteMessage = "Error sending invite."
		} finally {
			isInviting = false
		}
	}

	function formatDate(dateString: string) {
		return new Intl.DateTimeFormat('en-US', {
			month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
		}).format(new Date(dateString))
	}

	function mockAction(actionName: string) {
		alert(`The "${actionName}" action is not currently supported by the backend API.`)
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
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
		</svg>
		Back to Dashboard
	</a>

	<div class="flex items-center gap-3 mb-8">
		<div class="p-3 rounded-xl {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
			<Users class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Teams</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Manage your team or pending invitations</p>
		</div>
	</div>

	{#if !myTeam && !isLoading}
		<div class="mb-6 flex justify-end">
			<a href="/student/create-team" class="px-5 py-2.5 bg-[#f26f21] hover:bg-[#d85c14] text-white font-semibold rounded-xl shadow-sm transition-colors flex items-center gap-2">
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
			<div class="mb-8 p-6 md:p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-orange-900/50 shadow-[0_4px_30px_rgba(234,88,12,0.1)]' : 'bg-orange-50/50 border-orange-100 shadow-[0_4px_20px_rgba(234,88,12,0.05)]'}">
				<div class="flex items-center justify-between gap-4 flex-wrap mb-4">
					<div class="flex items-center gap-3">
						<div class="p-2.5 rounded-xl bg-orange-500 text-white shadow-sm">
							<Star class="w-6 h-6" />
						</div>
						<div>
							<h3 class="text-xs font-semibold uppercase tracking-wider text-orange-600 dark:text-orange-400">My Current Team</h3>
							<h2 class="text-2xl md:text-3xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">{myTeam.name || "Untitled Team"}</h2>
						</div>
					</div>
					<div class="flex items-center gap-3">
						{#if myTeam.status === "PENDING"}
							<span class="text-amber-700 dark:text-amber-400 bg-amber-100 dark:bg-amber-500/10 px-3 py-1 rounded-lg border border-amber-200 dark:border-amber-500/20 font-semibold text-sm">Status: Pending Approval</span>
						{:else if myTeam.status === "APPROVED"}
							<span class="text-green-600 dark:text-green-500 bg-green-50 dark:bg-green-950/30 px-3 py-1 rounded-lg border border-green-200 dark:border-green-900/50 font-semibold text-sm">Status: Approved</span>
						{/if}
					</div>
				</div>
				
				<div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-8 mb-8">
					<div class="p-5 rounded-2xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800' : 'bg-white border-gray-100'}">
						<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-2">Team ID</p>
						<p class="font-mono text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">{myTeam.id}</p>
					</div>
					<div class="p-5 rounded-2xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800' : 'bg-white border-gray-100'}">
						<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-2">My Role</p>
						<p class="font-semibold text-sm {profile?.id === myTeam.leader_id ? 'text-orange-500' : 'text-green-500'}">
							{profile?.id === myTeam.leader_id ? 'Team Leader' : 'Member'}
						</p>
					</div>
				</div>

				<div class="border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-8">
					<div class="flex justify-between items-center mb-4">
						<h3 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Team Members ({myTeam.members?.length || 1}/5)</h3>
					</div>
					
					<div class="space-y-3 mb-8">
						{#if myTeam.members && myTeam.members.length > 0}
							{#each myTeam.members as memberId}
								<div class="flex items-center justify-between p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800' : 'bg-white border-gray-100'}">
									<div class="flex items-center gap-3">
										<div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-zinc-800 flex items-center justify-center text-gray-500 dark:text-zinc-400 font-semibold">
											<Users class="w-5 h-5" />
										</div>
										<div>
											<p class="font-mono text-sm {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">{memberId}</p>
											{#if memberId === profile?.id}
												<p class="text-xs text-gray-500 mt-0.5">(You)</p>
											{/if}
										</div>
									</div>
									{#if memberId === myTeam.leader_id}
										<span class="text-xs font-bold text-orange-500 bg-orange-100 dark:bg-orange-500/10 px-2 py-1 rounded">Leader</span>
									{:else}
										<span class="text-xs font-semibold text-gray-500 bg-gray-100 dark:bg-zinc-800 px-2 py-1 rounded">Member</span>
									{/if}
								</div>
							{/each}
						{:else}
							<!-- Fallback if members array is missing but we know they are in the team -->
							<div class="flex items-center justify-between p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800' : 'bg-white border-gray-100'}">
								<div class="flex items-center gap-3">
									<div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-zinc-800 flex items-center justify-center text-gray-500 dark:text-zinc-400 font-semibold">
										<Users class="w-5 h-5" />
									</div>
									<div>
										<p class="font-mono text-sm {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">{profile?.id}</p>
										<p class="text-xs text-gray-500 mt-0.5">(You)</p>
									</div>
								</div>
								<span class="text-xs font-bold text-orange-500 bg-orange-100 dark:bg-orange-500/10 px-2 py-1 rounded">Leader</span>
							</div>
						{/if}
					</div>

					{#if profile?.id === myTeam.leader_id}
						<div class="p-6 rounded-2xl border {theme.darkMode ? 'bg-zinc-950/80 border-zinc-800' : 'bg-gray-50 border-gray-200'}">
							<h4 class="font-bold mb-2 flex items-center gap-2 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
								<UserPlus class="w-5 h-5 text-orange-500" />
								Invite New Member
							</h4>
							<p class="text-sm mb-4 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Enter a student ID to invite them to your team. Teams can have up to 5 members.</p>
							
							{#if (myTeam.members?.length || 1) >= 5}
								<div class="p-3 bg-amber-50 dark:bg-amber-950/30 text-amber-600 dark:text-amber-400 rounded-xl text-sm font-medium border border-amber-200 dark:border-amber-900/50">
									Your team has reached the maximum size limit (5 members).
								</div>
							{:else}
								<form onsubmit={handleInviteMember} class="flex gap-3">
									<input 
										type="text" 
										bind:value={inviteStudentId} 
										placeholder="Student UUID..." 
										required
										class="flex-1 rounded-xl p-3 border focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-700 text-zinc-100' : 'bg-white border-gray-300'}"
									/>
									<button 
										type="submit" 
										disabled={isInviting || !inviteStudentId.trim()}
										class="px-5 py-3 bg-orange-500 hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors disabled:opacity-50 min-w-[120px]"
									>
										{isInviting ? 'Sending...' : 'Send Invite'}
									</button>
								</form>
								{#if inviteMessage}
									<p class="mt-3 text-sm font-medium {inviteError ? 'text-red-500' : 'text-green-500'}">{inviteMessage}</p>
								{/if}
							{/if}
						</div>

						<div class="mt-8 flex gap-4 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
							<button onclick={() => mockAction('Transfer Leadership')} class="flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg border transition-colors {theme.darkMode ? 'border-zinc-700 text-zinc-300 hover:bg-zinc-800' : 'border-gray-300 text-gray-700 hover:bg-gray-100'}">
								<ArrowRightLeft class="w-4 h-4" />
								Transfer Leadership
							</button>
						</div>
					{/if}

					<div class="mt-4 flex gap-4">
						<button onclick={() => mockAction('Leave Team')} class="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-red-500 rounded-lg hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors">
							<LogOut class="w-4 h-4" />
							Leave Team
						</button>
					</div>
				</div>
			</div>
		{:else}
			{#if invites.length === 0}
				<div class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode ? 'border-zinc-800 text-zinc-500' : 'border-gray-200 text-gray-400'}">
					<Users class="w-12 h-12 mx-auto mb-4 opacity-50" />
					<h3 class="text-lg font-medium">No Pending Invitations</h3>
					<p class="text-sm mt-1">You haven't received any team invitations yet.</p>
				</div>
			{:else}
				<div class="space-y-4">
					{#each invites as invite}
						<div class="p-5 rounded-2xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 hover:border-zinc-700' : 'bg-white border-gray-100 shadow-sm hover:shadow-md'}">
							<div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
								<div class="flex items-start gap-4">
									<div class="p-2.5 rounded-full mt-1 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
										<Users class="w-5 h-5" />
									</div>
									<div>
										<h4 class="font-semibold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">
											Team Invitation
										</h4>
										<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
											You have been invited to join Team <span class="font-mono font-bold text-orange-500">{invite.team_id || "Unknown"}</span>.
										</p>
										<div class="flex items-center gap-4 mt-3 text-xs font-medium {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
											<span class="flex items-center gap-1.5"><Clock class="w-3.5 h-3.5" /> Sent: {formatDate(invite.sent_at)}</span>
											{#if invite.status === "PENDING"}
												<span class="text-amber-700 dark:text-amber-400 bg-amber-100 dark:bg-amber-500/10 px-2 py-0.5 rounded-md border border-amber-200 dark:border-amber-500/20 font-semibold">Pending</span>
											{:else if invite.status === "ACCEPTED"}
												<span class="text-green-600 dark:text-green-500 bg-green-50 dark:bg-green-950/30 px-2 py-0.5 rounded-md border border-green-200 dark:border-green-900/50">Accepted</span>
											{:else if invite.status === "DECLINED"}
												<span class="text-red-600 dark:text-red-500 bg-red-50 dark:bg-red-950/30 px-2 py-0.5 rounded-md border border-red-200 dark:border-red-900/50">Declined</span>
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
											{processingId === invite.id ? 'Processing...' : 'Accept'}
										</button>
										<button 
											onclick={() => handleDecline(invite.id)}
											disabled={processingId === invite.id}
											class="flex items-center gap-1.5 px-4 py-2 border {theme.darkMode ? 'border-zinc-700 hover:bg-zinc-800 text-zinc-300' : 'border-gray-200 hover:bg-gray-50 text-gray-600'} font-medium text-sm rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
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

