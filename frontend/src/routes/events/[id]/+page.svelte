<script lang="ts">
	import { page } from "$app/stores"
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile, getAllSeasons, getEventsInSeason } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { formatFullDate } from "$lib/utils/formatters.js"
	
	let eventId = $page.params.id
	let eventDetail: any = $state(null)
	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({ throwOnError: false })
			if (!profileRes?.ok || !profileData) {
				goto("/auth/login")
				return
			}
			
			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			if (seasons) {
				for (const season of seasons) {
					const { data: events } = await getEventsInSeason({ path: { seasonId: season.id }, throwOnError: false })
					if (events) {
						const found = events.find((e: any) => e.id === eventId)
						if (found) {
							eventDetail = found
							break
						}
					}
				}
			}
			
			if (!eventDetail) {
				errorMessage = "Event not found."
			}
		} catch (err) {
			errorMessage = "Error loading event details."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>{eventDetail ? eventDetail.name : "Event Detail"} - SEAL</title>
</svelte:head>

<div class="max-w-4xl mx-auto w-full p-6 md:p-10">
	<button
		onclick={() => history.back()}
		class="inline-flex items-center gap-2 transition-colors mb-8 font-medium bg-transparent border-0 cursor-pointer
        {theme.darkMode ? 'text-zinc-400 hover:text-blue-400' : 'text-gray-500 hover:text-blue-600'}"
	>
		<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
		</svg>
		Back
	</button>

	{#if isLoading}
		<div class="flex justify-center py-20">
			<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
		</div>
	{:else if errorMessage}
		<div class="bg-red-50 text-red-600 p-6 rounded-2xl border border-red-200">
			<h2 class="text-lg font-bold">Error</h2>
			<p>{errorMessage}</p>
		</div>
	{:else if eventDetail}
		<div class="p-8 md:p-10 rounded-3xl transition-all border {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-xl' : 'bg-white border-gray-100 shadow-md'}">
			<div class="mb-8 border-b pb-8 {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
				<h1 class="text-3xl font-extrabold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					{eventDetail.name}
				</h1>
				<p class="mt-4 text-lg leading-relaxed {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
					{eventDetail.description}
				</p>
			</div>

			<div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
				<div class="p-5 rounded-2xl border {theme.darkMode ? 'bg-zinc-950 border-zinc-800' : 'bg-gray-50 border-gray-200'}">
					<p class="text-sm font-semibold uppercase tracking-wider mb-2 {theme.darkMode ? 'text-zinc-500' : 'text-gray-500'}">
						Event Start
					</p>
					<p class="font-bold text-lg {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
						{formatFullDate(eventDetail.start_time)}
					</p>
				</div>
				<div class="p-5 rounded-2xl border bg-blue-50/50 border-blue-100 dark:bg-blue-950/20 dark:border-blue-900/50">
					<p class="text-sm font-semibold uppercase tracking-wider mb-2 text-blue-600 dark:text-blue-400">
						Submission Deadline (BR-2)
					</p>
					<p class="font-bold text-lg text-blue-900 dark:text-blue-300 flex items-center gap-2">
						<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
						{formatFullDate(eventDetail.end_time)}
					</p>
					<p class="text-xs text-blue-600/80 dark:text-blue-400/80 mt-1">
						All work must be submitted by this accurate deadline pulled from the event configuration.
					</p>
				</div>
			</div>

			{#if eventDetail.tracks && eventDetail.tracks.length > 0}
				<div>
					<h3 class="text-xl font-bold mb-4 {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">Available Tracks</h3>
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each eventDetail.tracks as track}
							<div class="p-4 rounded-xl border {theme.darkMode ? 'border-zinc-800 bg-zinc-950' : 'border-gray-200 bg-white'}">
								<h4 class="font-bold text-md {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{track.name}</h4>
								<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">{track.description}</p>
							</div>
						{/each}
					</div>
				</div>
			{/if}
		</div>
	{/if}
</div>
