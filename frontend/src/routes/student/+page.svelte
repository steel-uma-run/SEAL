<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getInterestedParticipants
	} from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"
	import DashboardUI from "$lib/components/student/DashboardUI.svelte"
	import ActiveSeasonEvents from "$lib/components/student/ActiveSeasonEvents.svelte"

	let profile: any = $state(null)
	let seasons: any[] = $state([])
	let activeSeason: any = $state(null)
	let activeSeasonEvents: any[] = $state([])
	let joinedEvents: any[] = $state([])
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

					// Fetch joined events
					let joinedList: any[] = []
					for (const event of activeSeasonEvents) {
						let hasJoined = false

						// 1. Check LocalStorage
						if (typeof window !== "undefined") {
							const localParts = localStorage.getItem(`participants_${event.id}`)
							if (localParts) {
								const parsed = JSON.parse(localParts)
								if (parsed.some((p: any) => p.email === profile.email)) {
									hasJoined = true
								}
							}
						}

						// 2. Check API if not found in local storage
						if (!hasJoined) {
							const { data: participants } = await getInterestedParticipants({
								path: { seasonId: activeSeason.id, eventId: event.id },
								throwOnError: false
							})
							if (participants && participants.some((p: any) => p.email === profile.email)) {
								hasJoined = true
							}
						}

						if (hasJoined) {
							joinedList.push(event)
						}
					}
					joinedEvents = joinedList
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

		<!-- Joined Events Section -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) mb-8 transition-colors duration-300"
		>
			<div class="mb-6">
				<h2 class="text-xl font-bold text-(--md-on-surface)">Joined Events</h2>
				<p class="text-sm mt-1 text-(--md-on-surface-variant)">
					Events you are currently registered for in this season.
				</p>
			</div>

			{#if joinedEvents.length === 0}
				<div
					class="text-center py-10 border border-dashed rounded-2xl border-(--md-outline-variant) bg-(--md-surface-container)"
				>
					<svg
						class="w-12 h-12 mx-auto mb-3 text-(--md-on-surface-variant) opacity-60"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="1.5"
							d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
						></path>
					</svg>
					<p class="text-base font-medium text-(--md-on-surface)">
						You haven't joined any events yet.
					</p>
					<p class="text-xs mt-1 text-(--md-on-surface-variant)">
						Select an active event below to join!
					</p>
				</div>
			{:else}
				<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
					{#each joinedEvents as event}
						<div
							class="border border-(--md-outline-variant) rounded-2xl p-6 flex flex-col justify-between bg-(--md-surface-container) hover:bg-(--md-surface-container-high) transition-all duration-300 shadow-sm"
						>
							<div>
								<div class="flex justify-between items-start gap-4 mb-3">
									<h3 class="font-extrabold text-lg text-(--md-on-surface line-clamp-1)">
										{event.name}
									</h3>
									<span
										class="inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 shrink-0"
									>
										Joined
									</span>
								</div>
								<p class="text-sm text-(--md-on-surface-variant) line-clamp-2 leading-relaxed mb-4">
									{event.description}
								</p>
							</div>

							<div
								class="pt-4 border-t border-(--md-outline-variant) flex justify-between items-center"
							>
								<span class="text-xs text-(--md-on-surface-variant)"> Active Season Event </span>
								<a
									href="/events/{event.id}"
									class="text-xs font-bold text-(--md-primary) hover:underline cursor-pointer"
								>
									View details &rarr;
								</a>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<ActiveSeasonEvents {activeSeason} events={activeSeasonEvents} />
	{/if}
</div>
