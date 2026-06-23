<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getProfile } from "$lib/api/profile"
	import { getSeasons } from "$lib/api/seasons"
	import { theme } from "$lib/theme.svelte"
	import DashboardUI from "$lib/components/student/DashboardUI.svelte"
	import StudentPanel from "$lib/components/student/StudentPanel.svelte"

	let profile: any = $state(null)
	let seasons: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")

	async function loadData() {
		try {
			const profileRes = await getProfile()
			if (!profileRes.ok) {
				if (profileRes.status === 401 || profileRes.status === 403) {
					goto("/auth/login")
					return
				}
				throw new Error("Failed to load profile")
			}
			profile = await profileRes.json()

			// Load seasons
			const seasonsRes = await getSeasons()
			if (seasonsRes.ok) {
				seasons = await seasonsRes.json()
			}
		} catch (err: any) {
			errorMessage = err.message || "An error occurred while loading the dashboard."
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadData()
	})
</script>

<svelte:head>
	<title>Student Dashboard - SEAL</title>
</svelte:head>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	{#if isLoading}
		<div class="flex justify-center items-center h-[60vh]">
			<div
				class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"
			></div>
		</div>
	{:else if errorMessage}
		<div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm mb-6">
			<h3 class="text-sm font-medium text-red-800">Error loading dashboard</h3>
			<p class="text-sm text-red-700 mt-1">{errorMessage}</p>
		</div>
	{:else if profile}
		<!-- Top Header -->
		<header
			class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 {theme.darkMode
				? 'border-zinc-800'
				: 'border-gray-100'}"
		>
			<div>
				<h1
					class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode
						? 'text-zinc-100'
						: 'text-gray-800'}"
				>
					Welcome back, {profile.name}!
				</h1>
				<p class="mt-1 text-sm md:text-base {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
					Here is your project progress.
				</p>
			</div>

			<div class="flex items-center gap-4 mt-4 md:mt-0">
				<div class="text-right hidden sm:block">
					<p class="font-bold leading-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
						{profile.name}
					</p>
					<p class="text-xs font-semibold text-[#ea580c] uppercase tracking-wider">
						{profile.role}
					</p>
				</div>
				<div
					class="w-12 h-12 rounded-full flex items-center justify-center text-[#ea580c] font-bold text-xl shadow-sm {theme.darkMode
						? 'bg-orange-950/40 border border-orange-900/50'
						: 'bg-[#ffedd5] border border-[#fed7aa]'}"
				>
					{profile.name.charAt(0).toUpperCase()}
				</div>
			</div>
		</header>

		<DashboardUI {profile} {seasons} />
		<StudentPanel />
	{/if}
</div>
