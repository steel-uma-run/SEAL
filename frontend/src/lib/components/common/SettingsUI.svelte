<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { getProfile } from "$lib/api/profile"
	import { onMount } from "svelte"
	import { 
		User, 
		Bell, 
		Moon, 
		Shield, 
		Save 
	} from "@lucide/svelte"

	let activeTab = $state('profile')
	let profile = $state<any>(null)
	let fullName = $state("")
	let isPublic = $state(true)
	
	let pushNotifications = $state(true)
	let emailAlerts = $state(false)

	let currentPassword = $state("")
	let newPassword = $state("")
	let confirmPassword = $state("")

	let isSaving = $state(false)
	let alertMessage = $state("")
	let alertType = $state<"success" | "error">("success")

	onMount(async () => {
		try {
			const res = await getProfile()
			if (res.ok) {
				profile = await res.json()
				fullName = profile.name || "Nguyễn Hùng Cường"
			} else {
				fullName = "Nguyễn Hùng Cường"
			}
		} catch (e) {
			console.error("Error loading settings profile:", e)
			fullName = "Nguyễn Hùng Cường"
		}
	})

	function triggerAlert(message: string, type: "success" | "error" = "success") {
		alertMessage = message
		alertType = type
		setTimeout(() => {
			alertMessage = ""
		}, 3000)
	}

	async function handleSaveProfile() {
		isSaving = true
		try {
			// Simulate saving changes
			await new Promise(resolve => setTimeout(resolve, 800))
			triggerAlert("Profile settings updated successfully!", "success")
		} catch (err) {
			triggerAlert("Failed to update profile settings.", "error")
		} finally {
			isSaving = false
		}
	}

	async function handleSaveSecurity(e: Event) {
		e.preventDefault()
		if (!currentPassword || !newPassword || !confirmPassword) {
			triggerAlert("Please fill in all password fields.", "error")
			return
		}
		if (newPassword !== confirmPassword) {
			triggerAlert("New password and confirm password do not match.", "error")
			return
		}
		isSaving = true
		try {
			// Simulate saving security settings
			await new Promise(resolve => setTimeout(resolve, 800))
			triggerAlert("Password updated successfully!", "success")
			currentPassword = ""
			newPassword = ""
			confirmPassword = ""
		} catch (err) {
			triggerAlert("Failed to update password.", "error")
		} finally {
			isSaving = false
		}
	}
</script>

<div class="p-4 md:p-6 max-w-5xl mx-auto w-full">
	<!-- Title Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-black tracking-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Settings</h1>
		<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Manage your account settings and preferences.</p>
	</div>

	<!-- Alert Toast -->
	{#if alertMessage}
		<div class="fixed bottom-5 right-5 z-[3000] p-4 rounded-xl shadow-lg border text-sm font-semibold transition-all duration-300 animate-slide-up
			{alertType === 'success' 
				? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-600 dark:text-emerald-400' 
				: 'bg-red-500/10 border-red-500/20 text-red-600 dark:text-red-400'}"
		>
			{alertMessage}
		</div>
	{/if}

	<div class="flex flex-col md:flex-row gap-8">
		<!-- Sidebar Navigation Tabs -->
		<nav class="w-full md:w-64 shrink-0 flex flex-col gap-1.5">
			<button 
				onclick={() => activeTab = 'profile'}
				class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-colors cursor-pointer text-left {activeTab === 'profile' ? (theme.darkMode ? 'bg-zinc-800 text-zinc-100' : 'bg-orange-50 text-[#ea580c] bg-orange-100/55') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200' : 'text-gray-600 hover:bg-gray-100/50 hover:text-gray-900')}"
			>
				<User class="w-4 h-4" /> Profile
			</button>
			<button 
				onclick={() => activeTab = 'appearance'}
				class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-colors cursor-pointer text-left {activeTab === 'appearance' ? (theme.darkMode ? 'bg-zinc-800 text-zinc-100' : 'bg-orange-50 text-[#ea580c] bg-orange-100/55') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200' : 'text-gray-600 hover:bg-gray-100/50 hover:text-gray-900')}"
			>
				<Moon class="w-4 h-4" /> Appearance
			</button>
			<button 
				onclick={() => activeTab = 'notifications'}
				class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-colors cursor-pointer text-left {activeTab === 'notifications' ? (theme.darkMode ? 'bg-zinc-800 text-zinc-100' : 'bg-orange-50 text-[#ea580c] bg-orange-100/55') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200' : 'text-gray-600 hover:bg-gray-100/50 hover:text-gray-900')}"
			>
				<Bell class="w-4 h-4" /> Notifications
			</button>
			<button 
				onclick={() => activeTab = 'security'}
				class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-colors cursor-pointer text-left {activeTab === 'security' ? (theme.darkMode ? 'bg-zinc-800 text-zinc-100' : 'bg-orange-50 text-[#ea580c] bg-orange-100/55') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200' : 'text-gray-600 hover:bg-gray-100/50 hover:text-gray-900')}"
			>
				<Shield class="w-4 h-4" /> Security
			</button>
		</nav>

		<!-- Content Panels -->
		<div class="flex-1 min-w-0">
			{#if activeTab === 'profile'}
				<div class="space-y-6 max-w-2xl">
					<div>
						<h2 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Profile Information</h2>
						<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Update your personal details here.</p>
					</div>
					
					<div class="space-y-4">
						<div class="grid gap-2">
							<label class="text-sm font-medium {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}" for="name">Full Name</label>
							<input 
								id="name" 
								type="text" 
								bind:value={fullName}
								class="w-full px-4 py-2.5 rounded-xl border text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 {theme.darkMode ? 'bg-zinc-900/50 border-zinc-700 text-zinc-100 focus:border-orange-500' : 'bg-white border-gray-200 text-gray-900 focus:border-orange-500'}"
							/>
						</div>

						<div class="flex items-center justify-between py-3 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
							<div>
								<p class="text-sm font-medium {theme.darkMode ? 'text-zinc-200' : 'text-gray-900'}">Public Profile</p>
								<p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Allow other coordinators to see your profile.</p>
							</div>
							<button 
								class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center justify-center rounded-full focus:outline-none group border-0 bg-transparent"
								onclick={() => isPublic = !isPublic}
								type="button"
							>
								<span class="sr-only">Toggle public profile</span>
								<span class="pointer-events-none absolute h-full w-full rounded-full transition-colors duration-200 ease-in-out {isPublic ? 'bg-[#ea580c]' : (theme.darkMode ? 'bg-zinc-700' : 'bg-gray-200')}"></span>
								<span class="pointer-events-none absolute left-0 inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {isPublic ? 'translate-x-4' : 'translate-x-1'}"></span>
							</button>
						</div>
					</div>

					<div class="pt-4 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'} flex justify-end">
						<button 
							onclick={handleSaveProfile}
							disabled={isSaving}
							class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold bg-[#ea580c] hover:bg-[#d04e0a] text-white transition-colors shadow-sm cursor-pointer disabled:opacity-50"
						>
							<Save class="w-4 h-4" /> {isSaving ? "Saving..." : "Save Changes"}
						</button>
					</div>
				</div>
			{/if}

			{#if activeTab === 'appearance'}
				<div class="space-y-6 max-w-2xl">
					<div>
						<h2 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Appearance</h2>
						<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Customize how the dashboard looks.</p>
					</div>

					<div class="space-y-4">
						<div class="flex items-center justify-between py-3 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
							<div>
								<p class="text-sm font-medium {theme.darkMode ? 'text-zinc-200' : 'text-gray-900'}">Dark Mode</p>
								<p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Switch the application layout theme between light and dark mode.</p>
							</div>
							<button 
								class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center justify-center rounded-full focus:outline-none group border-0 bg-transparent"
								onclick={() => theme.darkMode = !theme.darkMode}
								type="button"
							>
								<span class="sr-only">Toggle dark mode</span>
								<span class="pointer-events-none absolute h-full w-full rounded-full transition-colors duration-200 ease-in-out {theme.darkMode ? 'bg-[#ea580c]' : (theme.darkMode ? 'bg-zinc-700' : 'bg-gray-200')}"></span>
								<span class="pointer-events-none absolute left-0 inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {theme.darkMode ? 'translate-x-4' : 'translate-x-1'}"></span>
							</button>
						</div>
					</div>
				</div>
			{/if}

			{#if activeTab === 'notifications'}
				<div class="space-y-6 max-w-2xl">
					<div>
						<h2 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Notifications</h2>
						<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Configure your alerts and notification channels.</p>
					</div>

					<div class="space-y-4">
						<div class="flex items-center justify-between py-3 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
							<div>
								<p class="text-sm font-medium {theme.darkMode ? 'text-zinc-200' : 'text-gray-900'}">Push Notifications</p>
								<p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Receive browser push notifications for real-time team and season events.</p>
							</div>
							<button 
								class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center justify-center rounded-full focus:outline-none group border-0 bg-transparent"
								onclick={() => pushNotifications = !pushNotifications}
								type="button"
							>
								<span class="sr-only">Toggle push notifications</span>
								<span class="pointer-events-none absolute h-full w-full rounded-full transition-colors duration-200 ease-in-out {pushNotifications ? 'bg-[#ea580c]' : (theme.darkMode ? 'bg-zinc-700' : 'bg-gray-200')}"></span>
								<span class="pointer-events-none absolute left-0 inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {pushNotifications ? 'translate-x-4' : 'translate-x-1'}"></span>
							</button>
						</div>

						<div class="flex items-center justify-between py-3 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
							<div>
								<p class="text-sm font-medium {theme.darkMode ? 'text-zinc-200' : 'text-gray-900'}">Email Alerts</p>
								<p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Get email updates about submissions, announcements, and evaluations.</p>
							</div>
							<button 
								class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center justify-center rounded-full focus:outline-none group border-0 bg-transparent"
								onclick={() => emailAlerts = !emailAlerts}
								type="button"
							>
								<span class="sr-only">Toggle email alerts</span>
								<span class="pointer-events-none absolute h-full w-full rounded-full transition-colors duration-200 ease-in-out {emailAlerts ? 'bg-[#ea580c]' : (theme.darkMode ? 'bg-zinc-700' : 'bg-gray-200')}"></span>
								<span class="pointer-events-none absolute left-0 inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out {emailAlerts ? 'translate-x-4' : 'translate-x-1'}"></span>
							</button>
						</div>
					</div>

					<div class="pt-4 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'} flex justify-end">
						<button 
							onclick={handleSaveProfile}
							disabled={isSaving}
							class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold bg-[#ea580c] hover:bg-[#d04e0a] text-white transition-colors shadow-sm cursor-pointer disabled:opacity-50"
						>
							<Save class="w-4 h-4" /> {isSaving ? "Saving..." : "Save Preferences"}
						</button>
					</div>
				</div>
			{/if}

			{#if activeTab === 'security'}
				<div class="space-y-6 max-w-2xl">
					<div>
						<h2 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">Security</h2>
						<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Change your password and manage login protection.</p>
					</div>

					<form onsubmit={handleSaveSecurity} class="space-y-4">
						<div class="grid gap-2">
							<label class="text-sm font-medium {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}" for="curPassword">Current Password</label>
							<input 
								id="curPassword" 
								type="password" 
								bind:value={currentPassword}
								class="w-full px-4 py-2.5 rounded-xl border text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 {theme.darkMode ? 'bg-zinc-900/50 border-zinc-700 text-zinc-100 focus:border-orange-500' : 'bg-white border-gray-200 text-gray-900 focus:border-orange-500'}"
							/>
						</div>

						<div class="grid gap-2">
							<label class="text-sm font-medium {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}" for="newPassword">New Password</label>
							<input 
								id="newPassword" 
								type="password" 
								bind:value={newPassword}
								class="w-full px-4 py-2.5 rounded-xl border text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 {theme.darkMode ? 'bg-zinc-900/50 border-zinc-700 text-zinc-100 focus:border-orange-500' : 'bg-white border-gray-200 text-gray-900 focus:border-orange-500'}"
							/>
						</div>

						<div class="grid gap-2">
							<label class="text-sm font-medium {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}" for="confPassword">Confirm Password</label>
							<input 
								id="confPassword" 
								type="password" 
								bind:value={confirmPassword}
								class="w-full px-4 py-2.5 rounded-xl border text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 {theme.darkMode ? 'bg-zinc-900/50 border-zinc-700 text-zinc-100 focus:border-orange-500' : 'bg-white border-gray-200 text-gray-900 focus:border-orange-500'}"
							/>
						</div>

						<div class="pt-4 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'} flex justify-end">
							<button 
								type="submit"
								disabled={isSaving}
								class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold bg-[#ea580c] hover:bg-[#d04e0a] text-white transition-colors shadow-sm cursor-pointer disabled:opacity-50"
							>
								<Save class="w-4 h-4" /> {isSaving ? "Updating..." : "Update Password"}
							</button>
						</div>
					</form>
				</div>
			{/if}
		</div>
	</div>
</div>

<style>
	@keyframes slide-up {
		from {
			transform: translateY(20px);
			opacity: 0;
		}
		to {
			transform: translateY(0);
			opacity: 1;
		}
	}
	.animate-slide-up {
		animation: slide-up 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
	}
</style>
