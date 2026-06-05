<script lang="ts">
	import { onMount } from "svelte"
	import { authState } from "$lib/features/auth/auth.svelte"
	import ThemeToggle from "$lib/components/ThemeToggle.svelte"

	let { children } = $props()

	let isMenuOpen = $state(false)
	let isProfileModalOpen = $state(false)
	let currentUser = $derived(authState.user)

	function toggleMenu() {
		isMenuOpen = !isMenuOpen
	}

	function handleLogout() {
		authState.logout()
		window.location.href = "/"
	}
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300">
	<header
		class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 shadow-sm sticky top-0 z-40"
	>
		<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
			<div class="flex items-center gap-8">
				<a href="/dashboard" class="flex flex-col items-center hover:opacity-80 transition-opacity">
					<div class="flex items-center">
						<img
							src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
							alt="FPT Logo"
							class="h-6 w-auto"
						/>
						<span class="ml-1 text-xs font-semibold text-[#002f6c] dark:text-blue-400"
							>Education</span
						>
					</div>
					<span
						class="text-orange-600 dark:text-orange-500 font-extrabold text-sm tracking-wider uppercase"
						>FPT University</span
					>
				</a>
			</div>

			<div class="flex items-center space-x-4">
				<ThemeToggle />

				<div class="relative">
					<button
						onclick={toggleMenu}
						class="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors focus:outline-none focus:ring-2 focus:ring-orange-500"
					>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="h-6 w-6 text-gray-700 dark:text-white"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
							/>
						</svg>
					</button>

					{#if isMenuOpen}
						<div
							class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 py-1 z-50 overflow-hidden"
						>
							<button
								onclick={() => {
									isProfileModalOpen = true
									isMenuOpen = false
								}}
								class="w-full text-left block px-4 py-3 text-sm text-gray-700 dark:text-white hover:bg-orange-50 dark:hover:bg-gray-700 hover:text-orange-600 dark:hover:text-orange-400 transition-colors font-medium"
							>
								Hồ sơ cá nhân
							</button>
							<button
								onclick={handleLogout}
								class="w-full text-left block px-4 py-3 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors font-medium"
							>
								Đăng xuất
							</button>
						</div>
						<!-- svelte-ignore a11y_click_events_have_key_events -->
						<!-- svelte-ignore a11y_interactive_supports_focus -->
						<div
							class="fixed inset-0 z-40"
							onclick={() => (isMenuOpen = false)}
							role="button"
							aria-label="Đóng menu"
						></div>
					{/if}
				</div>
			</div>
		</div>
	</header>

	<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
		{@render children()}
	</main>

	{#if isProfileModalOpen && currentUser}
		<div
			class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 dark:bg-black/70 backdrop-blur-sm"
		>
			<div
				class="bg-white dark:bg-gray-800 rounded-2xl p-8 max-w-md w-full shadow-2xl relative border border-gray-200 dark:border-gray-700"
			>
				<button
					onclick={() => (isProfileModalOpen = false)}
					class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						></path></svg
					>
				</button>
				<div class="text-center mb-6">
					<div
						class="w-24 h-24 bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 rounded-full flex items-center justify-center text-4xl font-bold mx-auto mb-4 border-2 border-orange-200 dark:border-orange-800"
					>
						{currentUser.full_name?.charAt(0) || "U"}
					</div>
					<h2 class="text-2xl font-bold text-gray-900 dark:text-white">{currentUser.full_name}</h2>
					<p class="text-gray-500 dark:text-white mt-1">{currentUser.email}</p>
				</div>
				<div class="space-y-4">
					<div
						class="flex justify-between items-center py-3 border-b border-gray-100 dark:border-gray-700"
					>
						<span class="text-gray-600 dark:text-white">Mã sinh viên</span>
						<span class="font-bold text-gray-900 dark:text-white">{currentUser.student_code}</span>
					</div>
					<div
						class="flex justify-between items-center py-3 border-b border-gray-100 dark:border-gray-700"
					>
						<span class="text-gray-600 dark:text-white">Loại</span>
						<span
							class="font-bold text-gray-900 dark:text-white px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-lg"
							>{currentUser.student_type}</span
						>
					</div>
				</div>
				<div class="mt-8">
					<button
						onclick={() => (isProfileModalOpen = false)}
						class="w-full py-3 px-4 bg-orange-600 hover:bg-orange-700 text-white rounded-xl font-bold transition-colors"
					>
						Đóng
					</button>
				</div>
			</div>
		</div>
	{/if}
</div>
