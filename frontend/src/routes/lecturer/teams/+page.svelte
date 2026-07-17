<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Users, ArrowLeft } from "@lucide/svelte"
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
	let myTeams: any[] = $state([])
	let isLoading = $state(true)
	let errorMessage = $state("")

	// Track mapping: track_id -> track_name
	let teamTrackMap: Record<string, string> = $state({})

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

			let allMentoredTeams: any[] = []
			let trackNameMap: Record<string, string> = {}

			// For each event, get tracks and teams
			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					// Filter tracks where this lecturer is a mentor
					const mentoredTracks = tracks.filter((t: any) => t.mentor_ids?.includes(profile.id))
					const mentoredTrackIds = mentoredTracks.map((t: any) => t.id)

					mentoredTracks.forEach((t: any) => {
						trackNameMap[t.id] = t.name
					})

					if (mentoredTrackIds.length > 0) {
						// Fetch all teams for this event
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							// Filter teams belonging to the mentored tracks
							const eventMentoredTeams = teams.filter((team: any) =>
								mentoredTrackIds.includes(team.track_id)
							)

							// Fetch submissions for these teams
							for (let team of eventMentoredTeams) {
								const { data: submissions } = await getAllSubmissions({
									path: { teamId: team.id },
									throwOnError: false
								})
								team.submissions = submissions || []
								allMentoredTeams.push(team)
							}
						}
					}
				}
			}

			myTeams = allMentoredTeams
			teamTrackMap = trackNameMap
		} catch (error: any) {
			console.error("Failed to load mentored teams", error)
			errorMessage = error.message || "Failed to load teams."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>My Mentored Teams - SEAL</title>
</svelte:head>

<div class="max-w-6xl mx-auto w-full p-4 md:p-8">
	<a
		href="/lecturer"
		class="inline-flex items-center gap-2 transition-colors mb-6 font-medium {theme.darkMode
			? 'text-zinc-400 hover:text-blue-400'
			: 'text-gray-500 hover:text-blue-600'}"
	>
		<ArrowLeft class="w-4 h-4" />
		Back to Dashboard
	</a>

	<div class="flex items-center gap-3 mb-8">
		<div
			class="p-3 rounded-xl {theme.darkMode
				? 'bg-blue-950/40 text-blue-400'
				: 'bg-blue-100 text-blue-600'}"
		>
			<Users class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Mentored Teams
			</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Teams you are assigned to mentor this season.
			</p>
		</div>
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
		</div>
	{:else if errorMessage}
		<div class="p-4 bg-red-50 text-red-600 rounded-xl border border-red-200">
			{errorMessage}
		</div>
	{:else if myTeams.length === 0}
		<div
			class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode
				? 'border-zinc-800 text-zinc-500'
				: 'border-gray-200 text-gray-400'}"
		>
			<Users class="w-12 h-12 mx-auto mb-4 opacity-50" />
			<h3 class="text-lg font-medium">No Mentored Teams</h3>
			<p class="text-sm mt-1">
				You haven't been assigned as a mentor to any teams in the active season.
			</p>
		</div>
	{:else}
		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
			{#each myTeams as team}
				<div
					class="p-6 rounded-2xl border transition-all flex flex-col h-full {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-200 shadow-sm'}"
				>
					<div class="mb-4">
						<div class="flex justify-between items-start mb-2">
							<h3 class="font-bold text-xl {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">
								{team.name}
							</h3>
							<span
								class="text-xs font-semibold px-2.5 py-1 rounded-full {team.status === 'APPROVED'
									? 'bg-green-100 text-green-700'
									: 'bg-amber-100 text-amber-700'}"
							>
								{team.status}
							</span>
						</div>
						<p class="text-xs font-medium {theme.darkMode ? 'text-blue-400' : 'text-blue-600'}">
							Track: {teamTrackMap[team.track_id] || "Unknown Track"}
						</p>
					</div>

					<div class="flex-grow">
						<h4
							class="text-sm font-semibold mb-2 {theme.darkMode
								? 'text-zinc-300'
								: 'text-gray-700'}"
						>
							Latest Submission:
						</h4>
						{#if team.submissions && team.submissions.length > 0}
							{@const latestSub = team.submissions[team.submissions.length - 1]}
							<div
								class="p-3 rounded-lg text-sm {theme.darkMode ? 'bg-zinc-950/50' : 'bg-gray-50'}"
							>
								<p
									class="font-medium mb-1 truncate {theme.darkMode
										? 'text-zinc-200'
										: 'text-gray-800'}"
								>
									{latestSub.title}
								</p>
								<div class="flex gap-3 mt-2 text-blue-500">
									{#if latestSub.github_link}
										<a href={latestSub.github_link} target="_blank" class="hover:underline"
											>GitHub</a
										>
									{/if}
									{#if latestSub.youtube_link}
										<a
											href={latestSub.youtube_link}
											target="_blank"
											class="hover:underline text-red-500">YouTube</a
										>
									{/if}
									{#if latestSub.slide_link}
										<a
											href={latestSub.slide_link}
											target="_blank"
											class="hover:underline text-orange-500">Slides</a
										>
									{/if}
								</div>
							</div>
						{:else}
							<p class="text-sm italic {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
								No submissions yet.
							</p>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
