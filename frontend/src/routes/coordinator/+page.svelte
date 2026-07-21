<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import {
		getSelfProfile,
		getAllSeasons,
		getAllAccounts,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getAllTracksOfEvent,
		getUnapprovedStudents
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"
	import DashboardUI from "$lib/components/coordinator/DashboardUI.svelte"

	let profile: any = $state(null)
	let seasonsCount = $state(0)
	let activeParticipantsCount = $state(0)
	let totalLecturersCount = $state(0)
	let totalTeamsCount = $state(0)
	let pendingTeamsCount = $state(0)
	let pendingStudentsCount = $state(0)
	let unassignedJudgesCount = $state(0)
	let activeEvents: any[] = $state([])
	let currentSeason: any = $state(null)
	let isLoading = $state(true)
	let errorMessage = $state("")

	async function loadData() {
		try {
			const { data, response: profileRes } = await getSelfProfile({ throwOnError: false })
			if (!profileRes?.ok || !data) {
				if (profileRes?.status === 401 || profileRes?.status === 403) {
					return
				}
				throw new Error("Failed to load profile")
			}
			profile = data

			const { data: accountsData, response: accountsRes } = await getAllAccounts({
				throwOnError: false
			})
			let lecturersList: any[] = []
			if (accountsRes?.ok && accountsData) {
				activeParticipantsCount = accountsData.filter(
					(u: any) => u.role === "STUDENT" && u.status === "ACTIVE"
				).length
				totalLecturersCount = accountsData.filter((u: any) => u.role === "LECTURER").length
				lecturersList = accountsData.filter((u: any) => u.role === "LECTURER")
			}

			const { data: unapprovedData, response: unapprovedRes } = await getUnapprovedStudents({
				throwOnError: false
			})
			if (unapprovedRes?.ok && unapprovedData) {
				pendingStudentsCount = unapprovedData.length
			}

			const { data: seasonsData, response: seasonsRes } = await getAllSeasons({
				throwOnError: false
			})

			let tempActiveEvents: any[] = []
			let tempTotalTeamsCount = 0
			let tempPendingTeamsCount = 0
			let assignedJudgesSet = new Set<string>()

			const currentInfo = getCurrentSeasonInfo()

			if (seasonsRes?.ok && seasonsData) {
				seasonsCount = seasonsData.length

				currentSeason = seasonsData.find(
					(s: any) => s.semester === currentInfo.semester && s.year === currentInfo.year
				)

				for (const season of seasonsData) {
					let seasonEvents: any[] = []
					const { data: eventsData, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes?.ok && eventsData && eventsData.length > 0) {
						seasonEvents = eventsData
					} else if (typeof window !== "undefined") {
						const stored = localStorage.getItem(`events_${season.id}`)
						if (stored) {
							seasonEvents = JSON.parse(stored)
						}
					}

					for (const evt of seasonEvents) {
						// Retrieve teams for this event (check localStorage first)
						let eventTeams: any[] = []
						if (typeof window !== "undefined") {
							const storedTeams = localStorage.getItem(`teams_${evt.id}`)
							if (storedTeams) {
								eventTeams = JSON.parse(storedTeams)
							}
						}
						if (eventTeams.length === 0) {
							const { data: teamsData, response: teamsRes } = await getAllTeamsOfEvents({
								path: { eventId: evt.id },
								throwOnError: false
							})
							if (teamsRes?.ok && teamsData) {
								eventTeams = teamsData
							}
						}

						let eventTeamsCount = eventTeams.length
						tempTotalTeamsCount += eventTeamsCount
						tempPendingTeamsCount += eventTeams.filter((t: any) => t.status === "PENDING").length

						// Fetch tracks to check judge assignments
						const { data: tracksData, response: tracksRes } = await getAllTracksOfEvent({
							path: { eventId: evt.id },
							throwOnError: false
						})
						if (tracksRes?.ok && tracksData) {
							for (const track of tracksData) {
								if (track.judge_ids) {
									track.judge_ids.forEach((jId: string) => assignedJudgesSet.add(jId))
								}
							}
						}

						// Only show finalized events in the current season
						if (evt.status === "FINALIZED" && currentSeason && season.id === currentSeason.id) {
							tempActiveEvents.push({
								...evt,
								seasonId: season.id,
								seasonSemester: season.semester,
								seasonYear: season.year,
								teamsCount: eventTeamsCount
							})
						}
					}
				}
			}

			totalTeamsCount = tempTotalTeamsCount
			pendingTeamsCount = tempPendingTeamsCount
			activeEvents = tempActiveEvents
			unassignedJudgesCount = lecturersList.filter((l: any) => !assignedJudgesSet.has(l.id)).length
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
	<title>Coordinator Dashboard - SEAL</title>
</svelte:head>

{#if isLoading}
	<div class="loading-state">
		<div class="loading-state__spinner"></div>
	</div>
{:else}
	{#if errorMessage}
		<div class="error-banner">
			<h3 class="error-banner__title">Error loading dashboard</h3>
			<p class="error-banner__message">{errorMessage}</p>
		</div>
	{:else if profile}
		<DashboardUI
			{profile}
			{seasonsCount}
			{activeParticipantsCount}
			{totalLecturersCount}
			{totalTeamsCount}
			{pendingTeamsCount}
			{pendingStudentsCount}
			{unassignedJudgesCount}
			{activeEvents}
			{currentSeason}
		/>
	{/if}
{/if}

<style lang="scss">
	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 60vh;
	}

	.loading-state__spinner {
		width: 3rem;
		height: 3rem;
		border-radius: 9999px;
		border-top: 2px solid var(--md-primary);
		border-bottom: 2px solid var(--md-primary);
		animation: spin 1s linear infinite;
	}

	.error-banner {
		margin: 1.5rem;
		padding: 1rem;
		border-left: 4px solid var(--md-error);
		border-radius: 0 0.25rem 0.25rem 0;
		background: var(--md-error-container);
		color: var(--md-on-error-container);
	}

	.error-banner__title {
		font-size: 0.875rem;
		font-weight: 700;
	}

	.error-banner__message {
		margin-top: 0.25rem;
		font-size: 0.875rem;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
