<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Settings, Save, User, Lock, Mail } from "@lucide/svelte"
	import { getSelfProfile } from "$lib/api"
	import { goto } from "$app/navigation"

	let profile: any = $state(null)
	let isLoading = $state(true)
	let errorMessage = $state("")
	let successMessage = $state("")
	let isSaving = $state(false)

	// Form fields
	let name = $state("")
	let email = $state("")
	let currentPassword = $state("")
	let newPassword = $state("")
	let confirmPassword = $state("")

	onMount(async () => {
		try {
			const { data, response: res } = await getSelfProfile({ throwOnError: false })
			if (res?.ok && data) {
				profile = data
				name = data.name || ""
				email = data.email || ""
			} else {
				errorMessage = "Failed to load profile."
				goto("/auth/login")
			}
		} catch (error) {
			console.error("Error fetching profile:", error)
			errorMessage = "An error occurred while loading your profile."
		} finally {
			isLoading = false
		}
	})

	async function handleSave(e: Event) {
		e.preventDefault()
		errorMessage = ""
		successMessage = ""

		if (newPassword && newPassword !== confirmPassword) {
			errorMessage = "New passwords do not match."
			return
		}

		isSaving = true
		try {
			// Mocking update API as it doesn't exist yet
			await new Promise((resolve) => setTimeout(resolve, 800))

			// If we had an API, it would be called here
			// await updateProfile({ ... })

			successMessage = "Profile updated successfully! (Mocked)"
			currentPassword = ""
			newPassword = ""
			confirmPassword = ""
		} catch (error) {
			errorMessage = "Failed to update profile."
		} finally {
			isSaving = false
		}
	}
</script>

<div class="max-w-4xl mx-auto w-full p-4 md:p-8">
	<div class="flex items-center gap-3 mb-8">
		<div
			class="p-3 rounded-xl {theme.darkMode
				? 'bg-orange-950/40 text-orange-400'
				: 'bg-orange-100 text-orange-600'}"
		>
			<Settings class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Account Settings
			</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Update your personal information and password.
			</p>
		</div>
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-orange-500"></div>
		</div>
	{:else}
		<div
			class="rounded-2xl border transition-all shadow-sm {theme.darkMode
				? 'bg-zinc-900 border-zinc-800'
				: 'bg-white border-gray-100'}"
		>
			<div class="p-6 md:p-8">
				{#if errorMessage}
					<div class="mb-6 p-4 bg-red-50 text-red-600 rounded-xl border border-red-200 text-sm">
						{errorMessage}
					</div>
				{/if}
				{#if successMessage}
					<div
						class="mb-6 p-4 bg-green-50 text-green-700 rounded-xl border border-green-200 text-sm"
					>
						{successMessage}
					</div>
				{/if}

				<form onsubmit={handleSave} class="space-y-8">
					<!-- Personal Info Section -->
					<div>
						<h3
							class="text-lg font-bold mb-4 flex items-center gap-2 {theme.darkMode
								? 'text-zinc-100'
								: 'text-gray-800'}"
						>
							<User class="w-5 h-5 text-orange-500" />
							Personal Information
						</h3>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
							<div>
								<label
									class="block text-sm font-semibold mb-2 {theme.darkMode
										? 'text-zinc-300'
										: 'text-gray-700'}"
								>
									Full Name
								</label>
								<input
									type="text"
									bind:value={name}
									required
									class="w-full px-4 py-3 rounded-xl border focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all {theme.darkMode
										? 'bg-zinc-950 border-zinc-800 text-zinc-100'
										: 'bg-gray-50 border-gray-200 text-gray-900'}"
									placeholder="Your full name"
								/>
							</div>
							<div>
								<label
									class="block text-sm font-semibold mb-2 {theme.darkMode
										? 'text-zinc-300'
										: 'text-gray-700'}"
								>
									Email Address
								</label>
								<div class="relative">
									<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
										<Mail class="h-4 w-4 text-gray-400" />
									</div>
									<input
										type="email"
										value={email}
										disabled
										class="w-full pl-11 pr-4 py-3 rounded-xl border cursor-not-allowed opacity-70 {theme.darkMode
											? 'bg-zinc-950 border-zinc-800 text-zinc-100'
											: 'bg-gray-100 border-gray-200 text-gray-900'}"
									/>
								</div>
								<p class="text-xs mt-2 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
									Email address cannot be changed.
								</p>
							</div>
						</div>
					</div>

					<!-- Password Section -->
					<div class="pt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
						<h3
							class="text-lg font-bold mb-4 flex items-center gap-2 {theme.darkMode
								? 'text-zinc-100'
								: 'text-gray-800'}"
						>
							<Lock class="w-5 h-5 text-orange-500" />
							Change Password
						</h3>
						<div class="grid grid-cols-1 gap-6 max-w-md">
							<div>
								<label
									class="block text-sm font-semibold mb-2 {theme.darkMode
										? 'text-zinc-300'
										: 'text-gray-700'}"
								>
									Current Password
								</label>
								<input
									type="password"
									bind:value={currentPassword}
									class="w-full px-4 py-3 rounded-xl border focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all {theme.darkMode
										? 'bg-zinc-950 border-zinc-800 text-zinc-100'
										: 'bg-gray-50 border-gray-200 text-gray-900'}"
									placeholder="Leave blank to keep current"
								/>
							</div>
							<div>
								<label
									class="block text-sm font-semibold mb-2 {theme.darkMode
										? 'text-zinc-300'
										: 'text-gray-700'}"
								>
									New Password
								</label>
								<input
									type="password"
									bind:value={newPassword}
									class="w-full px-4 py-3 rounded-xl border focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all {theme.darkMode
										? 'bg-zinc-950 border-zinc-800 text-zinc-100'
										: 'bg-gray-50 border-gray-200 text-gray-900'}"
									placeholder="New password"
								/>
							</div>
							<div>
								<label
									class="block text-sm font-semibold mb-2 {theme.darkMode
										? 'text-zinc-300'
										: 'text-gray-700'}"
								>
									Confirm New Password
								</label>
								<input
									type="password"
									bind:value={confirmPassword}
									class="w-full px-4 py-3 rounded-xl border focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all {theme.darkMode
										? 'bg-zinc-950 border-zinc-800 text-zinc-100'
										: 'bg-gray-50 border-gray-200 text-gray-900'}"
									placeholder="Confirm new password"
								/>
							</div>
						</div>
					</div>

					<!-- Actions -->
					<div
						class="pt-6 border-t {theme.darkMode
							? 'border-zinc-800'
							: 'border-gray-100'} flex justify-end gap-4"
					>
						<button
							type="button"
							class="px-6 py-2.5 rounded-xl font-semibold transition-colors {theme.darkMode
								? 'bg-zinc-800 text-zinc-300 hover:bg-zinc-700'
								: 'bg-gray-100 text-gray-700 hover:bg-gray-200'}"
							onclick={() => {
								name = profile?.name || ""
								currentPassword = ""
								newPassword = ""
								confirmPassword = ""
							}}
						>
							Cancel
						</button>
						<button
							type="submit"
							disabled={isSaving}
							class="flex items-center gap-2 px-6 py-2.5 rounded-xl font-semibold text-white transition-colors bg-orange-500 hover:bg-orange-600 disabled:opacity-70 disabled:cursor-not-allowed shadow-sm"
						>
							{#if isSaving}
								<div
									class="animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white"
								></div>
								Saving...
							{:else}
								<Save class="w-4 h-4" />
								Save Changes
							{/if}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}
</div>
