<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { ArrowLeft, Award, FileText, CheckCircle, Clock } from "lucide-svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getAllSubmissions,
		getInterestedParticipants,
		getAllCriteriaTemplates
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let profile: any = $state(null)
	let myTeam: any = $state(null)
	let submissions: any[] = $state([])
	let criteriaTemplates: any[] = $state([])
	
	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			// Fetch criteria to match score criteria_id to name
			const { data: templates } = await getAllCriteriaTemplates({ throwOnError: false })
			if (templates) {
				criteriaTemplates = templates
			}

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

			// Find student's team
			let resolvedTeam = null
			for (const event of events) {
				const { data: participants } = await getInterestedParticipants({
					path: { eventId: event.id },
					throwOnError: false
				})

				if (participants) {
					const me = participants.find((p: any) => p.email === profile.email)
					if (me && (me.team_id || me.teamId)) {
						const teamId = me.team_id || me.teamId
						
						const { data: teams } = await getAllTeamsOfEvents({
							path: { eventId: event.id } as any,
							throwOnError: false
						})

						if (teams) {
							const team = teams.find((t: any) => t.id === teamId)
							if (team) {
								resolvedTeam = team
								break
							}
						}
					}
				}
			}

			myTeam = resolvedTeam

			if (myTeam) {
				// Fetch submissions
				const { data: subs } = await getAllSubmissions({
					path: { teamId: myTeam.id },
					throwOnError: false
				})

				if (subs) {
					// Process scores to aggregate total
					submissions = subs.map((sub: any) => {
						let totalScore = 0
						if (sub.scores && sub.scores.length > 0) {
							// Find matching criteria weight
							let template = criteriaTemplates.length > 0 ? criteriaTemplates[0] : null
							sub.scores.forEach((score: any) => {
								let weight = 0
								if (template && template.criteria) {
									const c = template.criteria.find((c: any) => c.id === score.criteria_id)
									if (c) weight = c.weight
								}
								totalScore += (score.value * weight) / 100
							})
						}
						
						return {
							...sub,
							total_score: totalScore,
							has_been_graded: sub.scores && sub.scores.length > 0
						}
					})
					
					// Sort by submitted_at descending
					submissions.sort((a, b) => new Date(b.submitted_at || 0).getTime() - new Date(a.submitted_at || 0).getTime())
				}
			}
		} catch (error: any) {
			console.error("Failed to load results", error)
			errorMessage = error.message || "Failed to load results data."
		} finally {
			isLoading = false
		}
	})

	function getCriteriaName(criteriaId: string) {
		if (criteriaTemplates.length > 0 && criteriaTemplates[0].criteria) {
			const c = criteriaTemplates[0].criteria.find((c: any) => c.id === criteriaId)
			return c ? c.name : "Unknown Criteria"
		}
		return "Criteria"
	}
</script>

<svelte:head>
	<title>My Results - SEAL</title>
</svelte:head>

<div class="max-w-4xl mx-auto w-full p-4 md:p-8">
	<a
		href="/student"
		class="inline-flex items-center gap-2 transition-colors mb-8 font-medium {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-500 hover:text-orange-600'}"
	>
		<ArrowLeft class="w-4 h-4" />
		Back to Dashboard
	</a>

	<div class="flex items-center gap-3 mb-8">
		<div class="p-3 rounded-xl {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
			<Award class="w-6 h-6" />
		</div>
		<div>
			<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Results & Progress</h1>
			<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				View your team's submissions and grading feedback.
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
	{:else if !myTeam}
		<div class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode ? 'border-zinc-800 text-zinc-500' : 'border-gray-200 text-gray-400'}">
			<FileText class="w-12 h-12 mx-auto mb-4 opacity-50" />
			<h3 class="text-lg font-medium">No Team Found</h3>
			<p class="text-sm mt-1">You are not part of any approved team in the active season. Please create or join a team first.</p>
		</div>
	{:else if submissions.length === 0}
		<div class="text-center py-16 border-2 border-dashed rounded-2xl {theme.darkMode ? 'border-zinc-800 text-zinc-500' : 'border-gray-200 text-gray-400'}">
			<FileText class="w-12 h-12 mx-auto mb-4 opacity-50" />
			<h3 class="text-lg font-medium">No Submissions Yet</h3>
			<p class="text-sm mt-1">Your team "{myTeam.name}" has not submitted any projects.</p>
		</div>
	{:else}
		<div class="space-y-8">
			{#each submissions as sub, index}
				<div class="p-6 rounded-3xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-200 shadow-sm'}">
					<div class="flex flex-col md:flex-row justify-between items-start gap-4 mb-6 pb-6 border-b {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
						<div>
							<div class="flex items-center gap-3 mb-2">
								<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">
									{sub.title}
								</h2>
								<span class="text-xs font-semibold px-2.5 py-1 rounded-full {theme.darkMode ? 'bg-zinc-800 text-zinc-300' : 'bg-gray-100 text-gray-600'}">
									Attempt {submissions.length - index}
								</span>
							</div>
							<p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
								{sub.description}
							</p>
							{#if sub.submitted_at}
								<div class="flex items-center gap-1 mt-3 text-xs {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
									<Clock class="w-3 h-3" />
									Submitted on {new Date(sub.submitted_at).toLocaleString()}
								</div>
							{/if}
						</div>
						
						<div class="shrink-0 text-right">
							{#if sub.has_been_graded}
								<p class="text-sm font-medium mb-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Total Score</p>
								<div class="flex items-baseline gap-1 justify-end">
									<span class="text-3xl font-extrabold {sub.total_score >= 50 ? 'text-green-500' : 'text-red-500'}">
										{sub.total_score.toFixed(1)}
									</span>
									<span class="text-sm font-bold {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">/100</span>
								</div>
							{:else}
								<div class="inline-flex flex-col items-end">
									<span class="px-3 py-1.5 rounded-lg text-sm font-medium {theme.darkMode ? 'bg-amber-950/30 text-amber-400 border border-amber-900/50' : 'bg-amber-50 text-amber-600 border border-amber-200'}">
										Pending Review
									</span>
									<p class="text-xs mt-2 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Awaiting grades from judges</p>
								</div>
							{/if}
						</div>
					</div>

					{#if sub.has_been_graded && sub.scores}
						<div>
							<h3 class="text-sm font-bold uppercase tracking-wider mb-4 {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">
								Feedback & Scores
							</h3>
							<div class="space-y-3">
								{#each sub.scores as score}
									<div class="p-4 rounded-xl {theme.darkMode ? 'bg-zinc-950/50' : 'bg-gray-50'} flex flex-col md:flex-row justify-between gap-4">
										<div>
											<p class="font-semibold {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
												{getCriteriaName(score.criteria_id)}
											</p>
											{#if score.comment}
												<p class="text-sm mt-1 italic {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
													"{score.comment}"
												</p>
											{/if}
										</div>
										<div class="shrink-0">
											<span class="inline-flex items-center justify-center min-w-[3rem] px-2 py-1 rounded-lg font-bold text-sm {theme.darkMode ? 'bg-zinc-800 text-zinc-100' : 'bg-white text-gray-900 shadow-sm border'}">
												{score.value}
											</span>
										</div>
									</div>
								{/each}
							</div>
						</div>
					{/if}
				</div>
			{/each}
		</div>
	{/if}
</div>
