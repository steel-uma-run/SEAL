<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Users, FileText, CheckCircle, Clock } from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTracksOfEvent,
		getAllTeamsOfEvents,
		getAllSubmissions
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let profile: any = $state(null)
	let stats = $state({
		mentoredTeamsCount: 0,
		submissionsToGradeCount: 0
	})
	let isLoading = $state(true)

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })
			const activeSeason = seasons?.find(
				(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
			)

			if (!activeSeason) return

			const { data: events } = await getEventsInSeason({
				path: { seasonId: activeSeason.id },
				throwOnError: true
			})

			if (!events || events.length === 0) return

			let mentoredTeams = 0
			let submissionsToGrade = 0

			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					const mentoredTrackIds = tracks
						.filter((t: any) => t.mentor_ids?.includes(profile.id))
						.map((t: any) => t.id)
					const judgingTrackIds = tracks
						.filter((t: any) => t.judge_ids?.includes(profile.id))
						.map((t: any) => t.id)

					if (mentoredTrackIds.length > 0 || judgingTrackIds.length > 0) {
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							// Count mentored teams
							mentoredTeams += teams.filter((t: any) =>
								mentoredTrackIds.includes(t.track_id)
							).length

							// Count submissions for judging tracks
							const judgingTeams = teams.filter((t: any) => judgingTrackIds.includes(t.track_id))
							for (const team of judgingTeams) {
								const { data: subs } = await getAllSubmissions({
									path: { teamId: team.id },
									throwOnError: false
								})
								if (subs) submissionsToGrade += subs.length
							}
						}
					}
				}
			}

			stats = {
				mentoredTeamsCount: mentoredTeams,
				submissionsToGradeCount: submissionsToGrade
			}
		} catch (error) {
			console.error("Failed to load dashboard stats", error)
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>Lecturer Dashboard - SEAL</title>
</svelte:head>

<div class="max-w-6xl mx-auto w-full p-4 md:p-8">
	<div
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1
				class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode
					? 'text-zinc-100'
					: 'text-gray-900'}"
			>
				Welcome, {profile?.fullName || profile?.name || "Lecturer"}
			</h1>
			<p class="mt-1 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Here is an overview of your responsibilities for the active season.
			</p>
		</div>
	</div>

	{#if isLoading}
		<div class="flex justify-center py-12">
			<div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
		</div>
	{:else}
		<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
			<!-- Mentor Card -->
			<div
				class="p-6 rounded-3xl border transition-all flex flex-col {theme.darkMode
					? 'bg-zinc-900 border-zinc-800 hover:border-blue-900/50'
					: 'bg-white border-gray-200 shadow-sm hover:border-blue-300'}"
			>
				<div class="flex items-center gap-4 mb-4">
					<div
						class="p-3 rounded-2xl {theme.darkMode
							? 'bg-blue-950/50 text-blue-400'
							: 'bg-blue-100 text-blue-600'}"
					>
						<Users class="w-8 h-8" />
					</div>
					<div>
						<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
							Mentored Teams
						</h2>
						<p class="text-sm font-semibold {theme.darkMode ? 'text-blue-400' : 'text-blue-600'}">
							Mentor Role
						</p>
					</div>
				</div>
				<p class="text-sm mb-6 flex-grow {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
					You are currently assigned to mentor <span
						class="font-bold text-lg {theme.darkMode ? 'text-zinc-200' : 'text-black'}"
						>{stats.mentoredTeamsCount}</span
					> team(s). View their progress and submissions.
				</p>
				<a
					href="/lecturer/teams"
					class="w-full py-3 text-center rounded-xl font-semibold transition-colors {theme.darkMode
						? 'bg-blue-600 text-white hover:bg-blue-500'
						: 'bg-blue-600 text-white hover:bg-blue-700'}"
				>
					View My Teams
				</a>
			</div>

			<!-- Judge Card -->
			<div
				class="p-6 rounded-3xl border transition-all flex flex-col {theme.darkMode
					? 'bg-zinc-900 border-zinc-800 hover:border-green-900/50'
					: 'bg-white border-gray-200 shadow-sm hover:border-green-300'}"
			>
				<div class="flex items-center gap-4 mb-4">
					<div
						class="p-3 rounded-2xl {theme.darkMode
							? 'bg-green-950/50 text-green-400'
							: 'bg-green-100 text-green-600'}"
					>
						<FileText class="w-8 h-8" />
					</div>
					<div>
						<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
							Submissions to Grade
						</h2>
						<p class="text-sm font-semibold {theme.darkMode ? 'text-green-400' : 'text-green-600'}">
							Judge Role
						</p>
					</div>
				</div>
				<p class="text-sm mb-6 flex-grow {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
					There are <span
						class="font-bold text-lg {theme.darkMode ? 'text-zinc-200' : 'text-black'}"
						>{stats.submissionsToGradeCount}</span
					> submission(s) awaiting your evaluation in the tracks you judge.
				</p>
				<a
					href="/lecturer/grading"
					class="w-full py-3 text-center rounded-xl font-semibold transition-colors {theme.darkMode
						? 'bg-green-600 text-white hover:bg-green-500'
						: 'bg-green-600 text-white hover:bg-green-700'}"
				>
					View Grading List
				</a>
			</div>
		</div>
	{/if}
</div>
