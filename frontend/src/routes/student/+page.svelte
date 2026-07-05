<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile, getAllSeasons, getEventsInSeason } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"
	import DashboardUI from "$lib/components/student/DashboardUI.svelte"
	import ActiveSeasonEvents from "$lib/components/student/ActiveSeasonEvents.svelte"

	let profile: any = $state(null)
	let seasons: any[] = $state([])
	let activeSeason: any = $state(null)
	let activeSeasonEvents: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")

	async function loadData() {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (!profileRes?.ok || !profileData) {
				if (profileRes?.status === 401 || profileRes?.status === 403) {
					goto("/auth/login")
					return
				}
				throw new Error("Failed to load profile")
			}
			profile = profileData

			// Load seasons
			const { data: seasonsData, response: seasonsRes } = await getAllSeasons({
				throwOnError: false
			})
			if (seasonsRes?.ok) {
				seasons = seasonsData || []

				// Identify active season based on current date
				const currentInfo = getCurrentSeasonInfo()
				activeSeason = seasons.find(
					(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
				)

				// Fetch events for active season
				if (activeSeason) {
					// Check local storage for mock events (saving coordinators' actions locally)
					if (typeof window !== "undefined") {
						const key = `events_${activeSeason.id}`
						const stored = localStorage.getItem(key)
						if (stored) {
							const allLocalEvents = JSON.parse(stored)
							activeSeasonEvents = allLocalEvents.filter((e: any) => e.status === "FINALIZED")
						}
					}

					// Fallback to API if no local events are found
					if (activeSeasonEvents.length === 0) {
						const { data: eventsData, response: eventsRes } = await getEventsInSeason({
							path: { seasonId: activeSeason.id },
							throwOnError: false
						})
						if (eventsRes?.ok) {
							activeSeasonEvents = eventsData || []
						}
					}
				}
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
				class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"
			></div>
		</div>
	{:else if errorMessage}
		<div
			class="bg-(--md-error-container) border-l-4 border-(--md-error) p-4 rounded-r mb-6 text-(--md-on-error-container)"
		>
			<h3 class="text-sm font-bold">Error loading dashboard</h3>
			<p class="text-sm mt-1">{errorMessage}</p>
		</div>
	{:else if profile}
		<!-- Top Header -->
		<header
			class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
		>
			<div>
				<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
					Welcome back, {profile?.name || "Student"}!
				</h1>
				<p class="mt-1 text-sm md:text-base text-(--md-on-surface-variant)">
					Here is your project progress.
				</p>
			</div>

			<div class="flex items-center gap-4 mt-4 md:mt-0">
				<div class="text-right hidden sm:block">
					<p class="font-bold leading-tight text-(--md-on-surface)">
						{profile?.name || "Student"}
					</p>
					<p class="text-xs font-semibold text-(--md-primary) uppercase tracking-wider">
						{profile?.role}
					</p>
				</div>
				<div
					class="w-12 h-12 rounded-full flex items-center justify-center text-(--md-on-primary-container) font-bold text-xl bg-(--md-primary-container) border border-(--md-outline-variant)"
				>
					{profile?.name?.charAt(0).toUpperCase() || "S"}
				</div>
			</div>
		</header>

		<DashboardUI {profile} {seasons} {activeSeason} />
		<ActiveSeasonEvents {activeSeason} events={activeSeasonEvents} />
	{/if}
</div>
