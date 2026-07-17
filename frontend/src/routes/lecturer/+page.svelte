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
	let assignedEvents: any[] = $state([])

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
			let allMentoredTeams: any[] = []
			let trackNameMap: Record<string, string> = {}

			for (const event of events) {
				const { data: tracks } = await getAllTracksOfEvent({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (tracks) {
					const mentoredTrackIds = tracks.filter((t: any) => t.mentor_ids?.includes(profile.id)).map((t: any) => t.id)
					const judgingTrackIds = tracks.filter((t: any) => t.judge_ids?.includes(profile.id)).map((t: any) => t.id)
					
					tracks.forEach((t: any) => { trackNameMap[t.id] = t.name })

					if (mentoredTrackIds.length > 0 || judgingTrackIds.length > 0) {
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							// For mentored teams
							const eventMentoredTeams = teams.filter((t: any) => mentoredTrackIds.includes(t.track_id))
							mentoredTeams += eventMentoredTeams.length
							
							for (let team of eventMentoredTeams) {
								const { data: subs } = await getAllSubmissions({
									path: { teamId: team.id },
									throwOnError: false
								})
								team.submissions = subs || []
								team.trackName = trackNameMap[team.track_id] || 'Unknown Track'
								team.eventName = event.name
								allMentoredTeams.push(team)
							}

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

			assignedEvents = allMentoredTeams
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

		<!-- Mentored Teams Section -->
		<div class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) mb-8 transition-colors duration-300">
			<div class="mb-6">
				<h2 class="text-xl font-bold text-(--md-on-surface)">My Mentored Teams</h2>
				<p class="text-sm mt-1 text-(--md-on-surface-variant)">
					Teams you are assigned to mentor this season.
				</p>
			</div>

			{#if assignedEvents.length === 0}
				<div class="text-center py-10 border border-dashed rounded-2xl border-(--md-outline-variant) bg-(--md-surface-container)">
					<Users class="w-12 h-12 mx-auto mb-3 text-(--md-on-surface-variant) opacity-60" />
					<p class="text-base font-medium text-(--md-on-surface)">
						You haven't been assigned to mentor any teams yet.
					</p>
				</div>
			{:else}
				<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
					{#each assignedEvents as team}
						<div class="border border-(--md-outline-variant) rounded-2xl p-6 flex flex-col justify-between bg-(--md-surface-container) hover:bg-(--md-surface-container-high) transition-all duration-300 shadow-sm h-full">
							<div class="mb-4">
								<div class="flex justify-between items-start mb-2">
									<h3 class="font-extrabold text-lg text-(--md-on-surface line-clamp-1)">
										{team.name}
									</h3>
									<span class="inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {team.status === 'APPROVED' ? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20' : 'bg-amber-500/10 text-amber-500 border border-amber-500/20'} shrink-0">
										{team.status}
									</span>
								</div>
								<p class="text-xs font-semibold text-(--md-primary) mb-1">
									{team.trackName}
								</p>
								<p class="text-xs text-(--md-on-surface-variant) mb-4">
									Event: {team.eventName}
								</p>
							</div>

							<div class="flex-grow pt-4 border-t border-(--md-outline-variant)">
								<h4 class="text-xs text-(--md-on-surface-variant) mb-2 uppercase font-bold tracking-wider">Latest Submission</h4>
								{#if team.submissions && team.submissions.length > 0}
									{@const latestSub = team.submissions[team.submissions.length - 1]}
									<div class="p-3 rounded-xl bg-(--md-surface-container-highest)">
										<p class="font-medium text-sm text-(--md-on-surface) mb-2 truncate">{latestSub.title}</p>
										<div class="flex gap-4 mt-2 text-xs font-bold text-(--md-primary)">
											{#if latestSub.github_link}
												<a href={latestSub.github_link} target="_blank" class="hover:underline flex items-center gap-1">GitHub</a>
											{/if}
											{#if latestSub.youtube_link}
												<a href={latestSub.youtube_link} target="_blank" class="hover:underline text-red-500 flex items-center gap-1">YouTube</a>
											{/if}
											{#if latestSub.slide_link}
												<a href={latestSub.slide_link} target="_blank" class="hover:underline text-orange-500 flex items-center gap-1">Slides</a>
											{/if}
										</div>
									</div>
								{:else}
									<p class="text-xs italic text-(--md-on-surface-variant) opacity-75">No submissions yet.</p>
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
