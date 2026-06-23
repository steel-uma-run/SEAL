<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { User, Mail } from "@lucide/svelte"

	let { profile } = $props<{ profile: any }>()
</script>

<div class="max-w-4xl mx-auto w-full p-4">
	<div
		class="w-full rounded-2xl border p-8 relative transition-all shadow-xl {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 text-zinc-100'
			: 'bg-white border-gray-100 text-gray-800'}"
	>
		<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
			<User class="w-5 h-5 text-[#ea580c]" />
			My Profile
		</h3>

		<div class="space-y-4">
			<div
				class="flex items-center gap-4 p-4 rounded-xl {theme.darkMode
					? 'bg-zinc-950'
					: 'bg-gray-50'}"
			>
				<div
					class="w-16 h-16 rounded-full flex items-center justify-center text-3xl font-bold text-[#ea580c] {theme.darkMode
						? 'bg-orange-950/40'
						: 'bg-orange-100'}"
				>
					{profile.name ? profile.name.charAt(0).toUpperCase() : "U"}
				</div>
				<div class="break-all">
					<h4 class="font-extrabold text-xl">{profile.name || "Unknown User"}</h4>
					<p class="text-sm font-mono mt-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
						UUID: {profile.id}
					</p>
				</div>
			</div>

			<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">System Role</p>
					<p class="font-bold text-sm mt-1 text-blue-500">{profile.role}</p>
				</div>
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Status</p>
					<p class="font-bold text-sm mt-1 text-green-500">ACTIVE</p>
				</div>
			</div>

			<div
				class="p-4 rounded-xl border {theme.darkMode
					? 'bg-zinc-900 border-zinc-800'
					: 'bg-white border-gray-100'}"
			>
				<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1">
					<Mail class="w-3.5 h-3.5" />
					Email Address
				</p>
				<p class="font-bold text-sm mt-1 text-[#ea580c]">{profile.email}</p>
			</div>
			
			<!-- ROLE BASED LOGIC UI HERE -->
			{#if profile.role === "STUDENT"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Student Information</h4>
					<div class="p-6 rounded-xl border border-dashed {theme.darkMode ? 'bg-zinc-950/50 border-zinc-700' : 'bg-gray-50 border-gray-300'}">
						<p class="text-center text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
							No specific student data available yet. Please join a team or register for an event.
						</p>
					</div>
				</div>
			{:else if profile.role === "COORDINATOR"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Coordinator Tools</h4>
					<div class="grid grid-cols-2 gap-4">
						<button class="p-3 text-sm font-semibold rounded-lg bg-orange-500 text-white hover:bg-orange-600 transition-colors cursor-pointer border-0">
							Manage Users
						</button>
						<button class="p-3 text-sm font-semibold rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition-colors cursor-pointer border-0">
							System Settings
						</button>
					</div>
				</div>
			{:else if profile.role === "JUDGE" || profile.role === "MENTOR"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Expert Information</h4>
					<div class="p-6 rounded-xl border border-dashed {theme.darkMode ? 'bg-zinc-950/50 border-zinc-700' : 'bg-gray-50 border-gray-300'}">
						<p class="text-center text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
							You are currently assigned as an expert. Awaiting track assignments.
						</p>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
