<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import {
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getAllSubmissions
	} from "$lib/api"
	import { getCurrentSeasonInfo } from "$lib/utils/seasons"

	let activeSeason: any = $state(null)
	let events: any[] = $state([])
	let selectedEventId: string = $state("")

	let teams: any[] = $state([])
	let isLoadingTeams = $state(false)

	let selectedTeam: any = $state(null)
	let teamSubmissions: any[] = $state([])
	let isLoadingSubmissions = $state(false)

	onMount(async () => {
		try {
			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			if (seasons) {
				const currentInfo = getCurrentSeasonInfo()
				activeSeason = seasons.find(
					(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
				)
				if (activeSeason) {
					const { data: eventData } = await getEventsInSeason({
						path: { seasonId: activeSeason.id },
						throwOnError: false
					})
					if (eventData && eventData.length > 0) {
						events = eventData
						selectedEventId = events[0].id
						await loadTeams(selectedEventId)
					}
				}
			}
		} catch (error) {
			console.error("Failed to load initial data", error)
		}
	})

	async function onEventChange(e: Event) {
		const target = e.target as HTMLSelectElement
		selectedEventId = target.value
		selectedTeam = null
		teamSubmissions = []
		await loadTeams(selectedEventId)
	}

	async function loadTeams(eventId: string) {
		if (!eventId) return
		isLoadingTeams = true
		try {
			const { data: teamData } = await getAllTeamsOfEvents({
				path: { eventId },
				throwOnError: false
			})
			teams = teamData || []
		} catch (e) {
			console.error(e)
		} finally {
			isLoadingTeams = false
		}
	}

	async function selectTeamForReview(team: any) {
		selectedTeam = team
		isLoadingSubmissions = true
		teamSubmissions = []
		try {
			const { data: subData } = await getAllSubmissions({
				path: { teamId: team.id },
				throwOnError: false
			})
			if (subData) {
				teamSubmissions = subData
			}
		} catch (e) {
			console.error(e)
		} finally {
			isLoadingSubmissions = false
		}
	}
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Expert Dashboard
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				View project submissions from teams.
			</p>
		</div>
		{#if events.length > 0}
			<div class="mt-4 md:mt-0 flex items-center gap-3">
				<label class="text-sm font-semibold text-(--md-on-surface)">Select Event:</label>
				<select
					value={selectedEventId}
					onchange={onEventChange}
					class="rounded-xl border p-2 text-sm outline-none bg-(--md-surface) border-(--md-outline) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
				>
					{#each events as event}
						<option value={event.id}>{event.name}</option>
					{/each}
				</select>
			</div>
		{/if}
	</header>

	<div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
		<!-- Column 1: Teams List -->
		<div
			class="p-8 rounded-3xl border transition-all bg-(--md-surface-container-low) border-(--md-outline-variant) shadow-sm"
		>
			<div class="flex items-center gap-3 mb-6">
				<div
					class="p-2 bg-(--md-primary-container) rounded-lg text-(--md-on-primary-container) shrink-0"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
						></path></svg
					>
				</div>
				<div>
					<h2 class="text-xl font-bold text-(--md-on-surface)">Teams</h2>
					<p class="text-xs text-(--md-on-surface-variant) mt-0.5">
						All teams participating in the selected event.
					</p>
				</div>
			</div>

			<div class="space-y-4 max-h-[600px] overflow-y-auto pr-2">
				{#if isLoadingTeams}
					<p class="text-sm text-(--md-on-surface-variant)">Loading teams...</p>
				{:else if teams.length === 0}
					<p class="text-sm text-(--md-on-surface-variant)">No teams found for this event.</p>
				{:else}
					{#each teams as team}
						<div
							class="group p-5 border rounded-xl transition-all duration-300 cursor-pointer bg-(--md-surface-container) border-(--md-outline-variant) hover:bg-(--md-surface-container-high) {selectedTeam?.id ===
							team.id
								? 'ring-2 ring-(--md-primary)'
								: ''}"
							onclick={() => selectTeamForReview(team)}
						>
							<div class="flex items-center justify-between">
								<h3 class="font-bold text-lg text-(--md-on-surface) transition-colors">
									{team.name}
								</h3>
							</div>
							<p class="text-sm text-(--md-on-surface-variant) mt-2 whitespace-pre-wrap">
								{team.description}
							</p>
						</div>
					{/each}
				{/if}
			</div>
		</div>

		<!-- Column 2: Submissions Review -->
		<div
			class="p-8 rounded-3xl border transition-all bg-(--md-surface-container-low) border-(--md-outline-variant) shadow-sm"
		>
			<div class="flex items-center gap-3 mb-6">
				<div
					class="p-2 bg-(--md-secondary-container) rounded-lg text-(--md-on-secondary-container) shrink-0"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M10 21h7a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v11m0 5l4.879-4.879m0 0a3 3 0 104.243-4.242 3 3 0 00-4.243 4.242z"
						></path></svg
					>
				</div>
				<div>
					<h2 class="text-xl font-bold text-(--md-on-surface)">Submissions Review</h2>
					<p class="text-xs text-(--md-on-surface-variant) mt-0.5">
						View project links and files submitted by the team.
					</p>
				</div>
			</div>

			{#if !selectedTeam}
				<div
					class="flex flex-col items-center justify-center h-48 text-center border-2 border-dashed rounded-xl border-(--md-outline)"
				>
					<p class="text-(--md-on-surface-variant) font-medium">
						Select a team to view their submissions.
					</p>
				</div>
			{:else}
				<div
					class="mb-6 p-5 border rounded-xl bg-(--md-surface-container-high) border-(--md-outline-variant)"
				>
					<h3 class="font-bold text-lg text-(--md-on-surface)">
						{selectedTeam.name}
					</h3>
					<p class="text-sm text-(--md-on-surface-variant) mt-1 whitespace-pre-wrap">
						{selectedTeam.description}
					</p>
				</div>

				{#if isLoadingSubmissions}
					<p class="text-sm text-(--md-on-surface-variant)">Loading submissions...</p>
				{:else if teamSubmissions.length === 0}
					<p class="text-sm text-(--md-on-surface-variant)">
						This team has not submitted any projects yet.
					</p>
				{:else}
					<div class="space-y-4">
						{#each teamSubmissions as sub, index}
							<div class="p-4 rounded-xl border bg-(--md-surface) border-(--md-outline-variant)">
								<div class="flex justify-between items-start mb-2">
									<h3 class="font-bold text-(--md-on-surface)">
										{sub.title}
										<span
											class="ml-2 text-xs font-medium px-2 py-0.5 rounded-full bg-(--md-primary-container) text-(--md-on-primary-container)"
										>
											Attempt {teamSubmissions.length - index}
										</span>
									</h3>
								</div>
								<p class="text-sm mb-4 text-(--md-on-surface-variant) whitespace-pre-wrap">
									{sub.description}
								</p>
								<div class="flex flex-col gap-3 text-sm">
									{#if sub.github_link}
										<a
											href={sub.github_link}
											target="_blank"
											rel="noopener noreferrer"
											class="text-(--md-primary) hover:underline flex items-center gap-1"
										>
											<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"
												><path
													fill-rule="evenodd"
													d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
													clip-rule="evenodd"
												/></svg
											>
											GitHub Repository
										</a>
									{/if}
									{#if sub.youtube_link}
										<a
											href={sub.youtube_link}
											target="_blank"
											rel="noopener noreferrer"
											class="text-red-500 hover:underline flex items-center gap-1"
										>
											<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"
												><path
													d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
												/></svg
											>
											YouTube Demo
										</a>
									{/if}
									{#if sub.slide_link}
										<a
											href={sub.slide_link}
											target="_blank"
											rel="noopener noreferrer"
											class="text-orange-500 hover:underline flex items-center gap-1"
										>
											<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
												><path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
												></path></svg
											>
											Presentation Slide
										</a>
									{/if}
								</div>
							</div>
						{/each}
					</div>
				{/if}
			{/if}
		</div>
	</div>
</div>
