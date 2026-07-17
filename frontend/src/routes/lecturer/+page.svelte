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
	import { getCurrentSeasonInfo, formatSeasonName } from "$lib/utils/seasons"
	import ActiveSeasonEvents from "$lib/components/student/ActiveSeasonEvents.svelte"

	let profile: any = $state(null)
	let stats = $state({
		mentoredTeamsCount: 0,
		submissionsToGradeCount: 0
	})
	let isLoading = $state(true)
	let activeSeason: any = $state(null)
	let activeSeasonEvents: any[] = $state([])
	let assignedTracks: any[] = $state([])

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })
			activeSeason = seasons?.find(
				(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
			)

			if (!activeSeason) return

			const { data: events } = await getEventsInSeason({
				path: { seasonId: activeSeason.id },
				throwOnError: true
			})

			if (!events || events.length === 0) return
			activeSeasonEvents = events

			let mentoredTeams = 0
			let submissionsToGrade = 0
			let allAssignedTracks: any[] = []

			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					for (const track of tracks) {
						let isMentor = track.mentor_ids?.includes(profile.id)
						let isJudge = track.judge_ids?.includes(profile.id)
						
						if (isMentor || isJudge) {
							let trackTeams: any[] = []
							
							const { data: teams } = await getAllTeamsOfEvents({
								path: { eventId: event.id } as any,
								throwOnError: false
							})

							if (teams) {
								trackTeams = teams.filter((t: any) => t.track_id === track.id || t.trackId === track.id)
							}

							if (isMentor) {
								mentoredTeams += trackTeams.length
							}

							if (isJudge) {
								for (const team of trackTeams) {
									const { data: subs } = await getAllSubmissions({
										path: { teamId: team.id },
										throwOnError: false
									})
									if (subs) submissionsToGrade += subs.length
								}
							}

							allAssignedTracks.push({
								...track,
								eventName: event.name,
								role: isMentor && isJudge ? "Mentor & Judge" : isMentor ? "Mentor" : "Judge",
								teams: trackTeams
							})
						}
					}
				}
			}

			assignedTracks = allAssignedTracks
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

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	{#if isLoading}
		<div class="flex justify-center items-center h-[60vh]">
			<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"></div>
		</div>
	{:else if profile}
		<!-- Top Header -->
		<header
			class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
		>
			<div>
				<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
					Welcome back, {profile?.fullName || profile?.name || 'Lecturer'}!
				</h1>
				<p class="mt-1 text-sm md:text-base text-(--md-on-surface-variant)">
					Here is an overview of your responsibilities for the active season.
				</p>
			</div>

			<div class="flex items-center gap-4 mt-4 md:mt-0">
				<div class="text-right hidden sm:block">
					<p class="font-bold leading-tight text-(--md-on-surface)">
						{profile?.fullName || profile?.name || 'Lecturer'}
					</p>
					<p class="text-xs font-semibold text-(--md-primary) uppercase tracking-wider">
						{profile?.role || 'LECTURER'}
					</p>
				</div>
				<div
					class="w-12 h-12 rounded-full flex items-center justify-center text-(--md-on-primary-container) font-bold text-xl bg-(--md-primary-container) border border-(--md-outline-variant)"
				>
					{(profile?.fullName || profile?.name || 'L').charAt(0).toUpperCase()}
				</div>
			</div>
		</header>

		<!-- Overview Cards -->
		<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6 mb-8">
			<!-- Current Season (Always visible) -->
			<div class="rounded-2xl p-6 border border-(--md-outline-variant)/50 bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all duration-300 hover:bg-(--md-surface-container-low)">
				<div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-orange-500/10 text-orange-500">
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path>
					</svg>
				</div>
				<div>
					<p class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
						Current Season
					</p>
					<h3 class="text-xl font-bold text-(--md-on-surface)">
						{activeSeason ? formatSeasonName(activeSeason) : "---"}
					</h3>
				</div>
			</div>

			<!-- Mentor Card -->
			<div class="rounded-2xl p-6 border border-(--md-outline-variant)/50 bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all duration-300 hover:bg-(--md-surface-container-low)">
				<div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-orange-500/10 text-orange-500">
					<Users class="w-6 h-6" />
				</div>
				<div>
					<p class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
						Mentored Teams
					</p>
					<h3 class="text-xl font-bold text-(--md-on-surface)">
						{stats.mentoredTeamsCount}
					</h3>
				</div>
			</div>

			<!-- Judge Card -->
			<div class="rounded-2xl p-6 border border-(--md-outline-variant)/50 bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all duration-300 hover:bg-(--md-surface-container-low)">
				<div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-orange-500/10 text-orange-500">
					<FileText class="w-6 h-6" />
				</div>
				<div>
					<p class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
						Submissions to Grade
					</p>
					<h3 class="text-xl font-bold text-(--md-on-surface)">
						{stats.submissionsToGradeCount}
					</h3>
				</div>
			</div>
		</div>

		<!-- Assigned Tracks Section -->
		<div class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) mb-8 transition-colors duration-300">
			<div class="mb-6">
				<h2 class="text-xl font-bold text-(--md-on-surface)">My Assigned Tracks</h2>
				<p class="text-sm mt-1 text-(--md-on-surface-variant)">
					Tracks you are assigned to mentor or judge this season.
				</p>
			</div>

			{#if assignedTracks.length === 0}
				<div class="text-center py-10 border border-dashed rounded-2xl border-(--md-outline-variant) bg-(--md-surface-container)">
					<FileText class="w-12 h-12 mx-auto mb-3 text-(--md-on-surface-variant) opacity-60" />
					<p class="text-base font-medium text-(--md-on-surface)">
						You haven't been assigned to any tracks yet.
					</p>
				</div>
			{:else}
				<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
					{#each assignedTracks as track}
						<div class="border border-(--md-outline-variant) rounded-2xl p-6 flex flex-col justify-between bg-(--md-surface-container) hover:bg-(--md-surface-container-high) transition-all duration-300 shadow-sm h-full">
							<div class="mb-4">
								<div class="flex justify-between items-start mb-2">
									<h3 class="font-extrabold text-lg text-(--md-on-surface line-clamp-1)">
										{track.name}
									</h3>
									<span class="inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {track.role === 'Mentor & Judge' ? 'bg-indigo-500/10 text-indigo-500 border border-indigo-500/20' : track.role === 'Mentor' ? 'bg-violet-500/10 text-violet-500 border border-violet-500/20' : 'bg-sky-500/10 text-sky-500 border border-sky-500/20'} shrink-0">
										{track.role}
									</span>
								</div>
								<p class="text-xs text-(--md-on-surface-variant) mb-4">
									Event: <span class="font-bold text-(--md-primary)">{track.eventName}</span>
								</p>
							</div>

							<div class="flex-grow pt-4 border-t border-(--md-outline-variant)">
								<h4 class="text-xs text-(--md-on-surface-variant) mb-2 uppercase font-bold tracking-wider">Assigned Teams</h4>
								{#if track.teams && track.teams.length > 0}
									<div class="flex flex-wrap gap-1">
										{#each track.teams as team}
											<span class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-emerald-500/10 text-emerald-500 font-medium">
												{team.name}
											</span>
										{/each}
									</div>
								{:else}
									<p class="text-xs italic text-(--md-on-surface-variant) opacity-75">No teams assigned yet.</p>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<ActiveSeasonEvents {activeSeason} events={activeSeasonEvents} />
	{/if}
</div>
