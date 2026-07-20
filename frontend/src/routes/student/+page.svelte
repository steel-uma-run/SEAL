<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getInterestedParticipants,
		getRounds
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
	let activeRounds: any[] = $state([])
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
					// Prefer API
					const { data: eventsData, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: activeSeason.id },
						throwOnError: false
					})
					if (eventsRes?.ok && eventsData && eventsData.length > 0) {
						activeSeasonEvents = eventsData
					} else {
						// Fallback to local storage for mock events
						if (typeof window !== "undefined") {
							const key = `events_${activeSeason.id}`
							const stored = localStorage.getItem(key)
							if (stored) {
								const allLocalEvents = JSON.parse(stored)
								activeSeasonEvents = allLocalEvents.filter((e: any) => e.status === "FINALIZED")
							}
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
								path: { eventId: event.id },
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

					let roundsList: any[] = []
					if (joinedEvents.length > 0) {
						// Fetch rounds for the nearest event (or first joined event)
						const nearestEvent = joinedEvents
							.slice()
							.sort((a, b) => new Date(a.endTime).getTime() - new Date(b.endTime).getTime())[0]

						// Check local storage for mock rounds
						if (typeof window !== "undefined") {
							const localRounds = localStorage.getItem(`rounds_${nearestEvent.id}`)
							if (localRounds) {
								roundsList = JSON.parse(localRounds)
							}
						}

						if (roundsList.length === 0) {
							const { data: rounds } = await getRounds({
								path: { eventId: nearestEvent.id },
								throwOnError: false
							})
							if (rounds) {
								roundsList = rounds
							}
						}
					}
					activeRounds = roundsList
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

<div class="dashboard-page">
	{#if isLoading}
		<div class="loading-state">
			<div class="loading-state__spinner"></div>
		</div>
	{:else if errorMessage}
		<div class="error-banner">
			<h3 class="error-banner__title">Error loading dashboard</h3>
			<p class="error-banner__message">{errorMessage}</p>
		</div>
	{:else if profile}
		<!-- Top Header -->
		<header class="dashboard-header">
			<div class="dashboard-header__intro">
				<h1 class="dashboard-header__title">
					Welcome back, {profile?.name || "Student"}!
				</h1>
				<p class="dashboard-header__subtitle">Here is your project progress.</p>
			</div>
			<div class="dashboard-header__profile">
				<div class="user-meta">
					<p class="user-meta__name">
						{profile?.name || "Student"}
					</p>
					<p class="user-meta__role">
						{profile?.role}
					</p>
				</div>
				<div class="user-avatar">
					{profile?.name?.charAt(0).toUpperCase() || "S"}
				</div>
			</div>
		</header>

		<DashboardUI {profile} {seasons} {activeSeason} {joinedEvents} {activeRounds} />

		<!-- Joined Events Section -->
		<div class="events-panel">
			<div class="events-panel__header">
				<h2 class="events-panel__title">Joined Events</h2>
				<p class="events-panel__subtitle">
					Events you are currently registered for in this season.
				</p>
			</div>

			{#if joinedEvents.length === 0}
				<div class="empty-events">
					<svg class="empty-events__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="1.5"
							d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
						></path>
					</svg>
					<p class="empty-events__title">You haven't joined any events yet.</p>
					<p class="empty-events__desc">Select an active event below to join!</p>
				</div>
			{:else}
				<div class="events-grid">
					{#each joinedEvents as event}
						<div class="event-card">
							<div class="event-card__body">
								<div class="event-card__header">
									<h3 class="event-card__name">
										{event.name}
									</h3>
									<span class="event-card__badge"> Joined </span>
								</div>
								<p class="event-card__description">
									{event.description}
								</p>
							</div>
							<div class="event-card__footer">
								<span class="event-card__meta"> Active Season Event </span>
								<a href="/events/{event.id}" class="event-card__link"> View details &rarr; </a>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<ActiveSeasonEvents {activeSeason} events={activeSeasonEvents} />
	{/if}
</div>

<style lang="scss">
	// ============================================================================
	// Student Dashboard - SCSS Conversion
	// ============================================================================
	$bp-sm: 640px;
	$bp-md: 768px;
	$bp-lg: 1024px;

	// ----------------------------------------------------------------------------
	// Page Wrapper
	// p-6 md:p-10 max-w-[1600px] mx-auto w-full
	// ----------------------------------------------------------------------------
	.dashboard-page {
		max-width: 1600px;
		width: 100%;
	}

	// ----------------------------------------------------------------------------
	// Loading State
	// flex justify-center items-center h-[60vh]
	// animate-spin rounded-full h-12 w-12 border-t-2 border-b-2
	// ----------------------------------------------------------------------------
	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 60vh;

		&__spinner {
			width: 3rem; // h-12 w-12
			height: 3rem;
			border-radius: 9999px;
			border-top: 2px solid var(--md-primary);
			border-bottom: 2px solid var(--md-primary);
			border-left: 2px solid transparent;
			border-right: 2px solid transparent;
			animation: spin 1s linear infinite;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	// ----------------------------------------------------------------------------
	// Error Banner
	// bg-(--md-error-container) border-l-4 border-(--md-error) p-4 rounded-r mb-6
	// ----------------------------------------------------------------------------
	.error-banner {
		background-color: var(--md-error-container);
		border-left: 4px solid var(--md-error);
		padding: 1rem; // p-4
		border-radius: 0 0.5rem 0.5rem 0; // rounded-r
		margin-bottom: 1.5rem; // mb-6
		color: var(--md-on-error-container);

		&__title {
			font-size: 0.875rem; // text-sm
			font-weight: 700; // font-bold
		}

		&__message {
			font-size: 0.875rem;
			margin-top: 0.25rem; // mt-1
		}
	}

	// ----------------------------------------------------------------------------
	// Dashboard Header
	// flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6
	// ----------------------------------------------------------------------------
	.dashboard-header {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem; // mb-8
		border-bottom: 1px solid var(--md-outline-variant);
		padding-bottom: 1.5rem; // pb-6

		@media (min-width: $bp-md) {
			flex-direction: row;
			align-items: center;
		}

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 800; // font-extrabold
			letter-spacing: -0.025em; // tracking-tight
			color: var(--md-on-surface);

			@media (min-width: $bp-md) {
				font-size: 1.875rem; // md:text-3xl
			}
		}

		&__subtitle {
			margin-top: 0.25rem;
			font-size: 0.875rem; // text-sm
			color: var(--md-on-surface-variant);

			@media (min-width: $bp-md) {
				font-size: 1rem; // md:text-base
			}
		}

		&__profile {
			display: flex;
			align-items: center;
			gap: 1rem; // gap-4
			margin-top: 1rem; // mt-4

			@media (min-width: $bp-md) {
				margin-top: 0;
			}
		}
	}

	.user-meta {
		text-align: right;
		display: none; // hidden

		@media (min-width: $bp-sm) {
			display: block; // sm:block
		}

		&__name {
			font-weight: 700; // font-bold
			line-height: 1.25; // leading-tight
			color: var(--md-on-surface);
		}

		&__role {
			font-size: 0.75rem; // text-xs
			font-weight: 600; // font-semibold
			color: var(--md-primary);
			text-transform: uppercase; // uppercase
			letter-spacing: 0.05em; // tracking-wider
		}
	}

	.user-avatar {
		width: 3rem; // w-12
		height: 3rem; // h-12
		border-radius: 9999px; // rounded-full
		display: flex;
		align-items: center;
		justify-content: center;
		color: var(--md-on-primary-container);
		font-weight: 700; // font-bold
		font-size: 1.25rem; // text-xl
		background-color: var(--md-primary-container);
		border: 1px solid var(--md-outline-variant);
		flex-shrink: 0;
	}

	// ----------------------------------------------------------------------------
	// Events Panel
	// p-8 rounded-3xl border bg-(--md-surface-container-low) mb-8 transition-colors duration-300
	// ----------------------------------------------------------------------------
	.events-panel {
		padding: 2rem; // p-8
		border-radius: 1.5rem; // rounded-3xl
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-low);
		margin-bottom: 2rem; // mb-8
		transition:
			background-color 0.3s ease,
			color 0.3s ease;

		&__header {
			margin-bottom: 1.5rem; // mb-6
		}

		&__title {
			font-size: 1.25rem; // text-xl
			font-weight: 700; // font-bold
			color: var(--md-on-surface);
		}

		&__subtitle {
			font-size: 0.875rem; // text-sm
			margin-top: 0.25rem; // mt-1
			color: var(--md-on-surface-variant);
		}
	}

	// ----------------------------------------------------------------------------
	// Empty Events
	// text-center py-10 border border-dashed rounded-2xl
	// ----------------------------------------------------------------------------
	.empty-events {
		text-align: center;
		padding-top: 2.5rem; // py-10
		padding-bottom: 2.5rem;
		border: 1px dashed var(--md-outline-variant);
		border-radius: 1rem; // rounded-2xl
		background-color: var(--md-surface-container);

		&__icon {
			width: 3rem; // w-12 h-12
			height: 3rem;
			margin-left: auto;
			margin-right: auto;
			margin-bottom: 0.75rem; // mb-3
			color: var(--md-on-surface-variant);
			opacity: 0.6;
		}

		&__title {
			font-size: 1rem; // text-base
			font-weight: 500; // font-medium
			color: var(--md-on-surface);
		}

		&__desc {
			font-size: 0.75rem; // text-xs
			margin-top: 0.25rem; // mt-1
			color: var(--md-on-surface-variant);
		}
	}

	// ----------------------------------------------------------------------------
	// Events Grid
	// grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6
	// ----------------------------------------------------------------------------
	.events-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem; // gap-6

		@media (min-width: $bp-md) {
			grid-template-columns: repeat(2, 1fr);
		}

		@media (min-width: $bp-lg) {
			grid-template-columns: repeat(3, 1fr);
		}
	}

	// ----------------------------------------------------------------------------
	// Event Card
	// border rounded-2xl p-6 flex flex-col justify-between bg-(--md-surface-container)
	// hover:bg-(--md-surface-container-high) transition-all duration-300 shadow-sm
	// ----------------------------------------------------------------------------
	.event-card {
		border: 1px solid var(--md-outline-variant);
		border-radius: 1rem; // rounded-2xl
		padding: 1.5rem; // p-6
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		background-color: var(--md-surface-container);
		transition: all 0.3s ease;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); // shadow-sm

		&:hover {
			background-color: var(--md-surface-container-high);
		}

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem; // gap-4
			margin-bottom: 0.75rem; // mb-3
		}

		&__name {
			font-weight: 800; // font-extrabold
			font-size: 1.125rem; // text-lg
			color: var(--md-on-surface);
			// line-clamp-1
			display: -webkit-box;
			-webkit-line-clamp: 1;
			-webkit-box-orient: vertical;
			overflow: hidden;
			line-clamp: 1;
		}

		&__badge {
			display: inline-flex;
			padding: 0.125rem 0.625rem; // px-2.5 py-0.5
			border-radius: 9999px; // rounded-full
			font-size: 0.625rem; // text-[10px]
			font-weight: 700; // font-bold
			text-transform: uppercase;
			letter-spacing: 0.05em; // tracking-wider
			background-color: rgba(16, 185, 129, 0.1); // bg-emerald-500/10
			color: #10b981; // text-emerald-500
			border: 1px solid rgba(16, 185, 129, 0.2); // border-emerald-500/20
			flex-shrink: 0; // shrink-0
			white-space: nowrap;
		}

		&__description {
			font-size: 0.875rem; // text-sm
			color: var(--md-on-surface-variant);
			line-height: 1.625; // leading-relaxed
			margin-bottom: 1rem; // mb-4
			// line-clamp-2
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
			line-clamp: 2;
		}

		&__footer {
			padding-top: 1rem; // pt-4
			border-top: 1px solid var(--md-outline-variant);
			display: flex;
			justify-content: space-between;
			align-items: center;
		}

		&__meta {
			font-size: 0.75rem; // text-xs
			color: var(--md-on-surface-variant);
		}

		&__link {
			font-size: 0.75rem; // text-xs
			font-weight: 700; // font-bold
			color: var(--md-primary);
			cursor: pointer;
			text-decoration: none;

			&:hover {
				text-decoration: underline; // hover:underline
			}
		}
	}
</style>
