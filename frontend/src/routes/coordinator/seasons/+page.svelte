<script lang="ts">
	import { onMount } from "svelte"
	import { getAllSeasons } from "$lib/api"
	import ManageSeason from "$lib/components/coordinator/ManageSeason.svelte"

	let seasons: any[] = $state([])
	let seasonsError = $state("")
	let isLoading = $state(true)

	async function fetchSeasons() {
		try {
			seasonsError = ""
			const { data, response: seasonsRes } = await getAllSeasons({ throwOnError: false })
			if (seasonsRes?.ok) {
				seasons = data || []
			} else {
				console.error("Failed to fetch seasons:", seasonsRes?.status)
				seasonsError = `Failed to load seasons (${seasonsRes?.status || "Unknown"}).`
			}
		} catch (err) {
			console.error("Error fetching seasons:", err)
			seasonsError = "Cannot connect to server."
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		fetchSeasons()
	})
</script>

<svelte:head>
	<title>Season Management - SEAL</title>
</svelte:head>

{#if isLoading}
	<div class="flex justify-center items-center h-[60vh]">
		<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"></div>
	</div>
{:else if seasonsError && seasons.length === 0}
	<div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm m-6">
		<h3 class="text-sm font-medium text-red-800">Error loading seasons</h3>
		<p class="text-sm text-red-700 mt-1">{seasonsError}</p>
	</div>
{:else}
	<ManageSeason {seasons} refreshSeasons={fetchSeasons} />
{/if}
