<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { getInvites, acceptInvite, declineInvite } from "$lib/api/invites"
	import { goto } from "$app/navigation"
	import { Bell, CheckCircle, XCircle, Clock, Users } from "@lucide/svelte"

	let invites = $state<any[]>([])
	let isLoading = $state(true)
	let errorMessage = $state("")
	let processingId = $state<string | null>(null)

	async function loadInvites() {
		try {
			const res = await getInvites()
			if (res.ok) {
				const data = await res.json()
				if (Array.isArray(data)) {
					// Sort pending first, then by date
					invites = data.sort((a, b) => {
						if (a.status === "PENDING" && b.status !== "PENDING") return -1
						if (a.status !== "PENDING" && b.status === "PENDING") return 1
						return new Date(b.sent_at).getTime() - new Date(a.sent_at).getTime()
					})
				}
			// } else if (res.status === 401 || res.status === 403) {
			// 	errorMessage = `Lỗi phân quyền hoặc token (Mã lỗi: ${res.status}). Có thể API chưa được backend hỗ trợ hoặc cấu hình sai.`
			// } else {
			// 	errorMessage = "Could not load notifications at this time."
			}
		} catch (error) {
			errorMessage = "Error connecting to the server."
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadInvites()
	})

	async function handleAccept(inviteId: string) {
		processingId = inviteId
		try {
			const res = await acceptInvite(inviteId)
			if (res.ok) {
				// Update local state
				const idx = invites.findIndex(i => i.id === inviteId)
				if (idx !== -1) invites[idx].status = "ACCEPTED"
			} else {
				alert("Failed to accept invite. Please try again.")
			}
		} catch (error) {
			alert("Error accepting invite.")
		} finally {
			processingId = null
		}
	}

	async function handleDecline(inviteId: string) {
		if (!confirm("Are you sure you want to decline this invite?")) return
		processingId = inviteId
		try {
			const res = await declineInvite(inviteId)
			if (res.ok) {
				// Update local state
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

	function formatDate(dateString: string) {
		return new Intl.DateTimeFormat('en-US', {
			month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
		}).format(new Date(dateString))
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
		<div class="p-3 rounded-xl {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
			<Users class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Teams</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Manage your team or pending invitations</p>
		</div>
	</div>

	<div class="mb-6 flex justify-end">
		<a href="/student/create-team" class="px-5 py-2.5 bg-[#f26f21] hover:bg-[#d85c14] text-white font-semibold rounded-xl shadow-sm transition-colors flex items-center gap-2">
			<Users class="w-5 h-5" />
			Create New Team
		</a>
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-orange-500"></div>
		</div>
	{:else if errorMessage}
		<div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm">
			<p class="text-sm text-red-700">{errorMessage}</p>
		</div>
	{:else if invites.length === 0}
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
									You have been invited to join Team <span class="font-mono font-bold text-orange-500">{invite.inviting_team_id?.substring(0,8) || "Unknown"}</span>.
								</p>
								<div class="flex items-center gap-4 mt-3 text-xs font-medium {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
									<span class="flex items-center gap-1.5"><Clock class="w-3.5 h-3.5" /> Sent: {formatDate(invite.sent_at)}</span>
									{#if invite.status === "PENDING"}
										<span class="text-yellow-600 dark:text-yellow-500 bg-yellow-50 dark:bg-yellow-950/30 px-2 py-0.5 rounded-md border border-yellow-200 dark:border-yellow-900/50">Pending</span>
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
</div>
