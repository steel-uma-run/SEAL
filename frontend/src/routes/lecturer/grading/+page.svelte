<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { FileText, ArrowLeft, CheckCircle, Clock } from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTracksOfEvent,
		getAllTeamsOfEvents,
		getAllSubmissions
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let lecturerProfile: any = $state(null)
	let judgingSubmissions: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data: profile } = await getSelfProfile({ throwOnError: true })
			lecturerProfile = profile

			// Get current season
			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })
			const activeSeason = seasons?.find(
				(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
			)

			if (!activeSeason) {
				isLoading = false
				return
			}

			// Get events
			const { data: events } = await getEventsInSeason({
				path: { seasonId: activeSeason.id },
				throwOnError: true
			})

			if (!events || events.length === 0) {
				isLoading = false
				return
			}

			let allSubmissionsForJudging: any[] = []

			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					// Filter tracks where this lecturer is a judge
					const judgingTracks = tracks.filter((t: any) => t.judge_ids?.includes(profile.id))
					const judgingTrackIds = judgingTracks.map((t: any) => t.id)
					
					if (judgingTrackIds.length > 0) {
						// Fetch teams
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							const eventJudgingTeams = teams.filter((team: any) =>
								judgingTrackIds.includes(team.track_id)
							)

							// Fetch submissions
							for (let team of eventJudgingTeams) {
								const trackName = judgingTracks.find((t: any) => t.id === team.track_id)?.name
								const { data: submissions } = await getAllSubmissions({
									path: { teamId: team.id },
									throwOnError: false
								})
								
								if (submissions) {
									submissions.forEach((sub: any) => {
										allSubmissionsForJudging.push({
											...sub,
											team_name: team.name,
											team_id: team.id,
											track_name: trackName,
											event_name: event.name
										})
									})
								}
							}
						}
					}
				}
			}

			judgingSubmissions = allSubmissionsForJudging.sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
		} catch (error: any) {
			console.error("Failed to load submissions", error)
			errorMessage = error.message || "Failed to load submissions for judging."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>Submissions to Grade - SEAL</title>
</svelte:head>

<div class="max-w-6xl mx-auto w-full p-4 md:p-8">
	<a
		href="/lecturer"
		class="inline-flex items-center gap-2 transition-colors mb-6 font-medium {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-500 hover:text-orange-600'}"
	>
		<ArrowLeft class="w-4 h-4" />
		Back to Dashboard
	</a>

	<div class="flex items-center gap-3 mb-8">
		<div class="p-3 rounded-xl {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
			<FileText class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Grading</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Submissions from teams you are assigned to judge.
			</p>
		</div>
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-orange-500"></div>
		</div>
	{:else if errorMessage}
		<div class="p-4 bg-red-50 text-red-600 rounded-xl border border-red-200">
			{errorMessage}
		</div>
	{:else if judgingSubmissions.length === 0}
		<div class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode ? 'border-zinc-800 text-zinc-500' : 'border-gray-200 text-gray-400'}">
			<FileText class="w-12 h-12 mx-auto mb-4 opacity-50" />
			<h3 class="text-lg font-medium">No Submissions</h3>
			<p class="text-sm mt-1">There are no submissions available for you to grade right now.</p>
		</div>
	{:else}
		<div class="grid grid-cols-1 gap-4">
			{#each judgingSubmissions as sub}
				<div class="p-5 rounded-2xl border transition-all flex flex-col md:flex-row md:items-center justify-between gap-4 {theme.darkMode ? 'bg-zinc-900 border-zinc-800 hover:border-zinc-700' : 'bg-white border-gray-200 shadow-sm hover:shadow-md'}">
					<div class="flex-grow">
						<div class="flex items-center gap-2 mb-1">
							<h3 class="font-bold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">
								{sub.team_name}
							</h3>
							<span class="text-xs font-semibold px-2 py-0.5 rounded-full {theme.darkMode ? 'bg-zinc-800 text-zinc-300' : 'bg-gray-100 text-gray-700'}">
								{sub.track_name}
							</span>
						</div>
						<p class="font-medium mb-2 {theme.darkMode ? 'text-orange-400' : 'text-orange-600'}">{sub.title}</p>
						<p class="text-sm line-clamp-2 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">{sub.description}</p>
					</div>
					
					<div class="flex items-center gap-4 shrink-0">
						<!-- Basic logic for check if already graded isn't present in API response yet, assuming all need grading or user can re-grade -->
						<a 
							href="/lecturer/grading/{sub.id}"
							class="px-5 py-2.5 bg-orange-500 hover:bg-orange-600 text-white font-semibold rounded-xl transition-colors text-sm shadow-sm"
						>
							Grade Submission
						</a>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
