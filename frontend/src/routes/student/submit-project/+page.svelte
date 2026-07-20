<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { goto } from "$app/navigation"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getInterestedParticipants,
		submitWork,
		getAllSubmissions,
		getAllTeamsOfEvents
	} from "$lib/api"

	let title = $state("")
	let description = $state("")
	let submitLink = $state("")
	let youtubeLink = $state("")
	let slideLink = $state("")

	let submitMessage = $state("")
	let isLoading = $state(false)
	let isPageLoading = $state(true)

	let myEventId = $state<string | null>(null)
	let mySeasonId = $state<string | null>(null)
	let myTeamId = $state<string | null>(null)
	let myTeamLeaderId = $state<string | null>(null)
	let profileId = $state<string | null>(null)
	let submissionsHistory = $state<any[]>([])

	onMount(async () => {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (!profileRes?.ok || !profileData) {
				goto("/auth/login")
				return
			}
			profileId = profileData.id

			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			if (seasons) {
				for (const season of seasons) {
					const { data: events } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (events) {
						for (const event of events) {
							const { data: participants } = await getInterestedParticipants({
								path: { eventId: event.id },
								throwOnError: false
							})
							if (participants) {
								const me = participants.find((p: any) => p.email === profileData.email)
								if (me && me.team_ids && me.team_ids.length > 0) {
									const { data: eventTeams } = await getAllTeamsOfEvents({
										path: { eventId: event.id } as any,
										throwOnError: false
									})
									if (eventTeams) {
										const team = eventTeams.find((t: any) => me.team_ids.includes(t.id))
										if (team) {
											myEventId = event.id
											mySeasonId = season.id
											myTeamId = team.id
											myTeamLeaderId = team.leader_id
											break
										}
									}
								}
							}
						}
					}
					if (myEventId) break
				}
			}

			// Fetch submissions history
			if (myTeamId) {
				const { data: subData } = await getAllSubmissions({
					path: { teamId: myTeamId },
					throwOnError: false
				})
				if (subData) {
					submissionsHistory = subData
				}
			}
		} catch (e) {
			console.error("Error loading team info", e)
		} finally {
			isPageLoading = false
		}
	})

	async function handleSubmit(e: Event) {
		e.preventDefault()

		// BR-42, BR-43: GitHub link validation
		if (!submitLink.trim().startsWith("https://github.com/")) {
			submitMessage = "Error: Git Link must be a valid https://github.com/ repository URL."
			return
		}

		if (!myEventId || !mySeasonId) {
			submitMessage = "Error: You are not part of any team or event."
			return
		}

		isLoading = true
		submitMessage = ""

		try {
			const { response, error } = await submitWork({
				path: { seasonId: mySeasonId, eventId: myEventId },
				body: {
					title: title,
					description: description,
					github_link: submitLink,
					youtube_link: youtubeLink,
					slide_link: slideLink
				},
				throwOnError: false
			})

			if (response?.ok) {
				submitMessage = "Submission successful!"
				// Update history array without reloading
				submissionsHistory = [
					{
						title,
						description,
						github_link: submitLink,
						youtube_link: youtubeLink,
						slide_link: slideLink
					},
					...submissionsHistory
				]
				// Clear form
				title = ""
				description = ""
				submitLink = ""
				youtubeLink = ""
				slideLink = ""
			} else {
				const errBody = error as any
				submitMessage = `Error: ${errBody?.detail || errBody?.title || response?.statusText || "Failed to submit"}`
			}
		} catch (error) {
			submitMessage = "Error connecting to server."
		} finally {
			isLoading = false
		}
	}
</script>

<svelte:head>
	<title>Submit Project - SEAL</title>
</svelte:head>

<div class="max-w-2xl mx-auto w-full p-6 md:p-10">
	<a
		href="/student"
		class="inline-flex items-center gap-2 transition-colors mb-8 font-medium
        {theme.darkMode
			? 'text-zinc-400 hover:text-blue-400'
			: 'text-gray-500 hover:text-blue-600'}"
	>
		<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path>
		</svg>
		Back to Dashboard
	</a>

	<div
		class="p-8 md:p-10 rounded-3xl transition-all border
        {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
	>
		<div
			class="flex items-center gap-4 mb-8 border-b pb-6 {theme.darkMode
				? 'border-zinc-800'
				: 'border-gray-100'}"
		>
			<div
				class="p-3 rounded-xl {theme.darkMode
					? 'bg-blue-950/40 text-blue-400'
					: 'bg-blue-100 text-blue-600'}"
			>
				<svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
					></path>
				</svg>
			</div>
			<div>
				<h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					Submit Project
				</h1>
				<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
					Submit your team's final work for evaluation.
				</p>
			</div>
		</div>

		{#if myTeamLeaderId !== profileId}
			<div
				class="mt-8 p-8 rounded-3xl bg-(--md-surface-container) border border-(--md-outline-variant) text-center"
			>
				<svg
					class="w-16 h-16 mx-auto text-(--md-on-surface-variant) mb-4 opacity-50"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
					></path></svg
				>
				<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'} mb-2">
					Only the Team Leader can submit
				</h2>
				<p class={theme.darkMode ? "text-zinc-400" : "text-gray-500"}>
					You are a member of this team. Please ask your team leader to submit the project.
				</p>
			</div>
		{:else}
			<form onsubmit={handleSubmit} class="flex flex-col gap-6">
				<div class="space-y-2">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Project Title (Required)</label
					>
					<input
						type="text"
						bind:value={title}
						required
						placeholder="Enter your project title"
						class="w-full rounded-xl p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none border
					{theme.darkMode
							? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600'
							: 'bg-gray-50 border-gray-200 text-gray-900'}"
					/>
				</div>

				<div class="space-y-2">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Project Description (Required)</label
					>
					<textarea
						bind:value={description}
						required
						rows="4"
						placeholder="Describe your project"
						class="w-full rounded-xl p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none resize-none border
					{theme.darkMode
							? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600'
							: 'bg-gray-50 border-gray-200 text-gray-900'}"
					></textarea>
				</div>

				<div class="space-y-2">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Git Link (Required)</label
					>
					<div class="relative">
						<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
							<svg
								class="h-5 w-5 {theme.darkMode ? 'text-zinc-600' : 'text-gray-400'}"
								fill="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									fill-rule="evenodd"
									d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
									clip-rule="evenodd"
								/>
							</svg>
						</div>
						<input
							type="url"
							bind:value={submitLink}
							required
							placeholder="https://github.com/..."
							class="pl-12 w-full rounded-xl p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none border
                        {theme.darkMode
								? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600'
								: 'bg-gray-50 border-gray-200 text-gray-900'}"
						/>
					</div>
				</div>

				<div class="space-y-2">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>YouTube Link (Optional)</label
					>
					<div class="relative">
						<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
							<svg
								class="h-5 w-5 {theme.darkMode ? 'text-zinc-600' : 'text-gray-400'}"
								fill="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
								/>
							</svg>
						</div>
						<input
							type="url"
							bind:value={youtubeLink}
							placeholder="https://youtube.com/..."
							class="pl-12 w-full rounded-xl p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none border
                        {theme.darkMode
								? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600'
								: 'bg-gray-50 border-gray-200 text-gray-900'}"
						/>
					</div>
				</div>

				<div class="space-y-2">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Presentation Slide Link</label
					>
					<div class="relative">
						<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
							<svg
								class="h-5 w-5 {theme.darkMode ? 'text-zinc-600' : 'text-gray-400'}"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
								></path>
							</svg>
						</div>
						<input
							type="url"
							bind:value={slideLink}
							placeholder="Google Slides or PPT link"
							class="pl-12 w-full rounded-xl p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none border
                        {theme.darkMode
								? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600'
								: 'bg-gray-50 border-gray-200 text-gray-900'}"
						/>
					</div>
				</div>

				{#if submitMessage}
					<div
						class="p-4 rounded-xl text-sm font-medium border flex items-start gap-3 transition-all
                    {theme.darkMode
							? 'bg-blue-950/20 text-blue-400 border-blue-900/50'
							: 'bg-blue-50 text-blue-700 border border-blue-100'}"
					>
						<svg
							class="w-5 h-5 flex-shrink-0 mt-0.5"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
							></path>
						</svg>
						{submitMessage}
					</div>
				{/if}

				<button
					type="submit"
					disabled={isLoading}
					class="mt-4 w-full bg-blue-600 text-white rounded-xl py-4 font-bold text-lg shadow-sm hover:bg-blue-700 hover:shadow-md disabled:opacity-70 transition-all flex justify-center items-center gap-3 cursor-pointer"
				>
					{#if isLoading}
						<svg class="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
							<circle
								class="opacity-25"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							></circle>
							<path
								class="opacity-75"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							></path>
						</svg>
					{/if}
					{isLoading ? "Submitting..." : "Submit Project"}
				</button>
			</form>
		{/if}
	</div>

	{#if submissionsHistory.length > 0}
		<div
			class="mt-8 p-8 md:p-10 rounded-3xl transition-all border {theme.darkMode
				? 'bg-zinc-900 border-zinc-800'
				: 'bg-white border-gray-100'}"
		>
			<h2 class="text-xl font-bold mb-4 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Submission History
			</h2>
			<div class="space-y-4">
				{#each submissionsHistory as sub, index}
					<div
						class="p-4 rounded-xl border {theme.darkMode
							? 'border-zinc-700 bg-zinc-800'
							: 'border-gray-200 bg-gray-50'}"
					>
						<div class="flex justify-between items-start mb-2">
							<h3 class="font-bold {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
								{sub.title}
								<span
									class="ml-2 text-xs font-medium px-2 py-0.5 rounded-full {theme.darkMode
										? 'bg-blue-900/50 text-blue-300'
										: 'bg-blue-100 text-blue-700'}"
								>
									Attempt {submissionsHistory.length - index}
								</span>
							</h3>
						</div>
						<p class="text-sm mb-3 {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
							{sub.description}
						</p>
						<div class="flex flex-col gap-2 text-sm">
							<a
								href={sub.github_link}
								target="_blank"
								rel="noopener noreferrer"
								class="text-blue-500 hover:underline flex items-center gap-1"
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

						<!-- Score Section Placeholder -->
						<div
							class="mt-4 pt-4 border-t {theme.darkMode ? 'border-zinc-700' : 'border-gray-200'}"
						>
							<h4
								class="text-sm font-semibold mb-2 {theme.darkMode
									? 'text-zinc-300'
									: 'text-gray-700'}"
							>
								Scores & Feedback
							</h4>
							{#if sub.scores && sub.scores.length > 0}
								<!-- Ready for backend score integration -->
								<div class="space-y-2">
									{#each sub.scores as score}
										<div class="flex justify-between items-center text-sm">
											<span class={theme.darkMode ? "text-zinc-400" : "text-gray-600"}
												>{score.criteria_name || "Criteria"}</span
											>
											<span class="font-medium {theme.darkMode ? 'text-zinc-200' : 'text-gray-900'}"
												>{score.value}/100</span
											>
										</div>
									{/each}
									<div
										class="mt-2 pt-2 border-t {theme.darkMode
											? 'border-zinc-700'
											: 'border-gray-200'} flex justify-between items-center font-bold"
									>
										<span class={theme.darkMode ? "text-zinc-300" : "text-gray-700"}
											>Total Average</span
										>
										<span class="text-blue-500">
											{(
												sub.scores.reduce((acc, curr) => acc + curr.value, 0) / sub.scores.length
											).toFixed(1)}/100
										</span>
									</div>
								</div>
							{:else}
								<div
									class="flex items-center gap-2 text-sm italic {theme.darkMode
										? 'text-zinc-500'
										: 'text-gray-500'}"
								>
									<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
										></path>
									</svg>
									Not Graded Yet / Chưa có điểm
								</div>
							{/if}
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/if}
</div>
