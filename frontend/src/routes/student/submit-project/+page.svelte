<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { theme } from "$lib/theme.svelte"
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

	let leaderTeams = $state<any[]>([])
	let selectedLeaderTeamIndex = $state(0)

	let currentLeaderTeam = $derived(leaderTeams[selectedLeaderTeamIndex] || null)
	let myEventId = $derived(currentLeaderTeam?.eventId || null)
	let mySeasonId = $derived(currentLeaderTeam?.seasonId || null)
	let myTeamId = $derived(currentLeaderTeam?.teamId || null)
	let myTeamLeaderId = $derived(currentLeaderTeam?.teamLeaderId || null)
	let profileId = $state<string | null>(null)
	let submissionsHistory = $state<any[]>([])

	$effect(() => {
		if (myTeamId) {
			fetchSubmissions(myTeamId)
		}
	})

	async function fetchSubmissions(teamId: string) {
		const { data: subData } = await getAllSubmissions({
			path: { teamId: teamId },
			throwOnError: false
		})
		if (subData) {
			submissionsHistory = subData
		} else {
			submissionsHistory = []
		}
	}

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

			let foundTeams: any[] = []

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
								if (me) {
									const { data: eventTeams } = await getAllTeamsOfEvents({
										path: { eventId: event.id } as any,
										throwOnError: false
									})
									if (eventTeams) {
										const team = eventTeams.find(
											(t: any) =>
												(me.team_ids && me.team_ids.includes(t.id)) ||
												t.leader_id === me.id ||
												t.leaderId === me.id
										)
										if (team) {
											foundTeams.push({
												eventId: event.id,
												eventName: event.name,
												seasonId: season.id,
												teamId: team.id,
												teamName: team.name,
												teamLeaderId: team.leader_id || team.leaderId
											})
										}
									}
								}
							}
						}
					}
				}
			}

			leaderTeams = foundTeams
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
				path: { eventId: myEventId },
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

<div class="submit-page {theme.darkMode ? 'dark' : ''}">
	<a href="/student/results" class="submit-page__back-link">
		<svg class="submit-page__back-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			/>
		</svg>
		Back to Results
	</a>

	<div class="submit-page__card">
		<div class="submit-page__header">
			<div class="submit-page__header-icon-wrap">
				<svg class="submit-page__header-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
					/>
				</svg>
			</div>
			<div>
				<h1 class="submit-page__title">Submit Project</h1>
				<p class="submit-page__subtitle">Submit your team's final work for evaluation.</p>
			</div>
		</div>

		{#if myTeamLeaderId !== profileId}
			<div class="submit-page__not-leader">
				<svg class="submit-page__lock-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
					/>
				</svg>
				<h2 class="submit-page__not-leader-title">Only the Team Leader can submit</h2>
				<p class="submit-page__not-leader-text">
					You are a member of this team. Please ask your team leader to submit the project.
				</p>
			</div>
		{:else}
			<form onsubmit={handleSubmit} class="submit-page__form">
				{#if leaderTeams.length > 1}
					<div class="submit-page__field">
						<label class="submit-page__label">Select Event / Team</label>
						<select
							bind:value={selectedLeaderTeamIndex}
							class="submit-page__input"
						>
							{#each leaderTeams as t, idx}
								<option value={idx}>
									{t.eventName}: {t.teamName}
								</option>
							{/each}
						</select>
					</div>
				{/if}

				<!-- Project Title -->
				<div class="submit-page__field">
					<label class="submit-page__label">Project Title (Required)</label>
					<input
						type="text"
						bind:value={title}
						required
						placeholder="Enter your project title"
						class="submit-page__input"
					/>
				</div>

				<!-- Project Description -->
				<div class="submit-page__field">
					<label class="submit-page__label">Project Description (Required)</label>
					<textarea
						bind:value={description}
						required
						rows="4"
						placeholder="Describe your project"
						class="submit-page__textarea"
					/>
				</div>

				<!-- Git Link -->
				<div class="submit-page__field">
					<label class="submit-page__label">Git Link (Required)</label>
					<div class="submit-page__input-wrap">
						<div class="submit-page__input-icon">
							<svg class="submit-page__input-svg" fill="currentColor" viewBox="0 0 24 24">
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
							class="submit-page__input submit-page__input--with-icon"
						/>
					</div>
				</div>

				<!-- YouTube Link -->
				<div class="submit-page__field">
					<label class="submit-page__label">YouTube Link (Optional)</label>
					<div class="submit-page__input-wrap">
						<div class="submit-page__input-icon">
							<svg class="submit-page__input-svg" fill="currentColor" viewBox="0 0 24 24">
								<path
									d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
								/>
							</svg>
						</div>
						<input
							type="url"
							bind:value={youtubeLink}
							placeholder="https://youtube.com/..."
							class="submit-page__input submit-page__input--with-icon"
						/>
					</div>
				</div>

				<!-- Presentation Slide Link -->
				<div class="submit-page__field">
					<label class="submit-page__label">Presentation Slide Link</label>
					<div class="submit-page__input-wrap">
						<div class="submit-page__input-icon">
							<svg
								class="submit-page__input-svg"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
								/>
							</svg>
						</div>
						<input
							type="url"
							bind:value={slideLink}
							placeholder="Google Slides or PPT link"
							class="submit-page__input submit-page__input--with-icon"
						/>
					</div>
				</div>

				<!-- Submit message -->
				{#if submitMessage}
					<div class="submit-page__message">
						<svg
							class="submit-page__message-icon"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
							/>
						</svg>
						{submitMessage}
					</div>
				{/if}

				<!-- Submit button -->
				<button type="submit" disabled={isLoading} class="submit-page__submit-btn">
					{#if isLoading}
						<svg class="submit-page__spinner" fill="none" viewBox="0 0 24 24">
							<circle
								class="submit-page__spinner-circle"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							/>
							<path
								class="submit-page__spinner-path"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							/>
						</svg>
					{/if}
					{isLoading ? "Submitting..." : "Submit Project"}
				</button>
			</form>
		{/if}
	</div>

	<!-- Submission History -->
	{#if submissionsHistory.length > 0}
		<div class="submit-page__history">
			<h2 class="submit-page__history-title">Submission History</h2>
			<div class="submit-page__history-list">
				{#each submissionsHistory as sub, index}
					<div class="submit-page__history-item">
						<div class="submit-page__history-item-header">
							<h3 class="submit-page__history-item-title">
								{sub.title}
								<span class="submit-page__attempt-badge">
									Attempt {submissionsHistory.length - index}
								</span>
							</h3>
						</div>
						<p class="submit-page__history-desc">{sub.description}</p>
						<div class="submit-page__links">
							<a
								href={sub.github_link}
								target="_blank"
								rel="noopener noreferrer"
								class="submit-page__link submit-page__link--github"
							>
								<svg class="submit-page__link-icon" fill="currentColor" viewBox="0 0 24 24">
									<path
										fill-rule="evenodd"
										d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
										clip-rule="evenodd"
									/>
								</svg>
								GitHub Repository
							</a>
							{#if sub.youtube_link}
								<a
									href={sub.youtube_link}
									target="_blank"
									rel="noopener noreferrer"
									class="submit-page__link submit-page__link--youtube"
								>
									<svg class="submit-page__link-icon" fill="currentColor" viewBox="0 0 24 24">
										<path
											d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
										/>
									</svg>
									YouTube Demo
								</a>
							{/if}
							{#if sub.slide_link}
								<a
									href={sub.slide_link}
									target="_blank"
									rel="noopener noreferrer"
									class="submit-page__link submit-page__link--slide"
								>
									<svg
										class="submit-page__link-icon"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
										/>
									</svg>
									Presentation Slide
								</a>
							{/if}
						</div>

						<!-- Score Section -->
						<div class="submit-page__scores-section">
							<h4 class="submit-page__scores-title">Scores & Feedback</h4>
							{#if sub.scores && sub.scores.length > 0}
								<div class="submit-page__scores-list">
									{#each sub.scores as score}
										<div class="submit-page__score-row">
											<span class="submit-page__score-label">
												{score.criteria_name || "Criteria"}
											</span>
											<span class="submit-page__score-value">
												{score.value}/10
											</span>
										</div>
									{/each}
									<div class="submit-page__score-total">
										<span class="submit-page__score-total-label">Total Average</span>
										<span class="submit-page__score-total-value">
											{(
												sub.scores.reduce((acc, curr) => acc + curr.value, 0) / sub.scores.length
											).toFixed(1)}/10
										</span>
									</div>
								</div>
							{:else}
								<div class="submit-page__no-grade">
									<svg
										class="submit-page__no-grade-icon"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
										/>
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

<style lang="scss">
	// ============================================================
	// Submit Project Page — SCSS
	// ============================================================
	// Dark mode: add `.dark` to a common ancestor (e.g. <body> or
	// the component root). Toggle it from Svelte via
	//   <svelte:body class:dark={theme.darkMode} />
	// or any equivalent mechanism that sets `theme.darkMode`.
	// ============================================================

	// ── Colour tokens ──────────────────────────────────────────────
	$white: #ffffff;
	$gray-50: #f9fafb;
	$gray-100: #f3f4f6;
	$gray-200: #e5e7eb;
	$gray-400: #9ca3af;
	$gray-500: #6b7280;
	$gray-600: #4b5563;
	$gray-700: #374151;
	$gray-800: #1f2937;
	$gray-900: #111827;

	$zinc-100: #f4f4f5;
	$zinc-200: #e4e4e7;
	$zinc-300: #d4d4d8;
	$zinc-400: #a1a1aa;
	$zinc-500: #71717a;
	$zinc-600: #52525b;
	$zinc-700: #3f3f46;
	$zinc-800: #27272a;
	$zinc-900: #18181b;
	$zinc-950: #09090b;

	$blue-50: #eff6ff;
	$blue-100: #dbeafe;
	$blue-300: #93c5fd;
	$blue-400: #60a5fa;
	$blue-500: #3b82f6;
	$blue-600: #2563eb;
	$blue-700: #1d4ed8;
	$blue-900: #1e3a8a;

	$red-500: #ef4444;
	$orange-500: #f97316;

	// ── Misc tokens ────────────────────────────────────────────────
	$radius-xl: 0.75rem; // 12px
	$radius-3xl: 1.5rem; // 24px
	$radius-full: 9999px;

	$transition-colors: color, background-color, border-color, text-decoration-color, fill, stroke;
	$transition-duration: 150ms;
	$transition-ease: cubic-bezier(0.4, 0, 0.2, 1);

	$shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
	$shadow-md:
		0 4px 6px -1px rgb(0 0 0 / 0.1),
		0 2px 4px -2px rgb(0 0 0 / 0.1);

	// ── Spinner keyframes ──────────────────────────────────────────
	@keyframes submit-spin {
		to {
			transform: rotate(360deg);
		}
	}

	// ============================================================
	// Block: .submit-page
	// ============================================================
	.submit-page {
		max-width: 42rem; // max-w-2xl
		width: 100%;
		margin-inline: auto;

		// ── Back link ──────────────────────────────────────────────
		&__back-link {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem;
			margin-bottom: 2rem;
			font-weight: 500;
			color: $gray-500;
			text-decoration: none;
			transition: $transition-colors $transition-duration $transition-ease;

			&:hover {
				color: $blue-600;
			}

			.dark & {
				color: $zinc-400;

				&:hover {
					color: $blue-400;
				}
			}
		}

		&__back-icon {
			width: 1.25rem;
			height: 1.25rem;
		}

		// ── Main card ──────────────────────────────────────────────
		&__card {
			padding: 2rem;
			border-radius: $radius-3xl;
			border: 1px solid $gray-100;
			background: $white;
			box-shadow: 0 4px 20px rgb(0 0 0 / 0.03);
			transition: all $transition-duration $transition-ease;

			@media (min-width: 768px) {
				padding: 2.5rem;
			}

			.dark & {
				background: $zinc-900;
				border-color: $zinc-800;
				box-shadow: 0 4px 30px rgb(0 0 0 / 0.2);
			}
		}

		// ── Header ─────────────────────────────────────────────────
		&__header {
			display: flex;
			align-items: center;
			gap: 1rem;
			margin-bottom: 2rem;
			padding-bottom: 1.5rem;
			border-bottom: 1px solid $gray-100;

			.dark & {
				border-bottom-color: $zinc-800;
			}
		}

		&__header-icon-wrap {
			padding: 0.75rem;
			border-radius: $radius-xl;
			background: $blue-100;
			color: $blue-600;

			.dark & {
				background: rgb(30 58 138 / 0.4); // blue-950/40
				color: $blue-400;
			}
		}

		&__header-icon {
			width: 2rem;
			height: 2rem;
		}

		&__title {
			font-size: 1.5rem;
			line-height: 2rem;
			font-weight: 700;
			color: $gray-800;

			.dark & {
				color: $zinc-100;
			}
		}

		&__subtitle {
			font-size: 0.875rem;
			line-height: 1.25rem;
			margin-top: 0.25rem;
			color: $gray-500;

			.dark & {
				color: $zinc-400;
			}
		}

		// ── Not-leader message ─────────────────────────────────────
		&__not-leader {
			margin-top: 2rem;
			padding: 2rem;
			border-radius: $radius-3xl;
			background: var(--md-surface-container);
			border: 1px solid var(--md-outline-variant);
			text-align: center;
		}

		&__lock-icon {
			width: 4rem;
			height: 4rem;
			margin-inline: auto;
			margin-bottom: 1rem;
			color: var(--md-on-surface-variant);
			opacity: 0.5;
		}

		&__not-leader-title {
			font-size: 1.25rem;
			line-height: 1.75rem;
			font-weight: 700;
			margin-bottom: 0.5rem;
			color: $gray-800;

			.dark & {
				color: $zinc-100;
			}
		}

		&__not-leader-text {
			color: $gray-500;

			.dark & {
				color: $zinc-400;
			}
		}

		// ── Form ───────────────────────────────────────────────────
		&__form {
			display: flex;
			flex-direction: column;
			gap: 1.5rem;
		}

		&__field {
			display: flex;
			flex-direction: column;
			gap: 0.5rem;
		}

		&__label {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 600;
			color: $gray-700;

			.dark & {
				color: $zinc-300;
			}
		}

		// Shared styles for <input> and <textarea>
		&__input,
		&__textarea {
			border-radius: $radius-xl;
			padding: 0.875rem;
			outline: none;
			border: 1px solid $gray-200;
			background: $gray-50;
			color: $gray-900;
			transition: all $transition-duration $transition-ease;

			&::placeholder {
				color: $zinc-600;
			}

			&:focus {
				box-shadow: 0 0 0 2px $blue-500;
				border-color: transparent;
			}

			.dark & {
				background: $zinc-950;
				border-color: $zinc-800;
				color: $zinc-100;

				&::placeholder {
					color: $zinc-600;
				}
			}
		}

		&__textarea {
			resize: none;
		}

		// Modifier: input with left icon
		&__input--with-icon {
			padding-left: 3rem; // pl-12
		}

		// Icon wrapper inside input
		&__input-wrap {
			position: relative;
		}

		&__input-icon {
			position: absolute;
			inset: 0 auto 0 0;
			padding-left: 1rem;
			display: flex;
			align-items: center;
			pointer-events: none;
		}

		&__input-svg {
			width: 1.25rem;
			height: 1.25rem;
			color: $gray-400;

			.dark & {
				color: $zinc-600;
			}
		}

		// ── Info message (submitMessage) ───────────────────────────
		&__message {
			display: flex;
			align-items: flex-start;
			gap: 0.75rem;
			padding: 1rem;
			border-radius: $radius-xl;
			border: 1px solid $blue-100;
			background: $blue-50;
			color: $blue-700;
			font-size: 0.875rem;
			font-weight: 500;
			transition: all $transition-duration $transition-ease;

			.dark & {
				background: rgb(23 37 84 / 0.2); // blue-950/20
				color: $blue-400;
				border-color: rgb(30 58 138 / 0.5); // blue-900/50
			}
		}

		&__message-icon {
			width: 1.25rem;
			height: 1.25rem;
			flex-shrink: 0;
			margin-top: 0.125rem;
		}

		// ── Submit button ──────────────────────────────────────────
		&__submit-btn {
			display: flex;
			justify-content: center;
			align-items: center;
			gap: 0.75rem;
			width: 100%;
			margin-top: 1rem;
			padding: 1rem 0;
			border: none;
			border-radius: $radius-xl;
			background: $blue-600;
			color: $white;
			font-size: 1.125rem;
			line-height: 1.75rem;
			font-weight: 700;
			box-shadow: $shadow-sm;
			cursor: pointer;
			transition: all $transition-duration $transition-ease;

			&:hover {
				background: $blue-700;
				box-shadow: $shadow-md;
			}

			&:disabled {
				opacity: 0.7;
			}
		}

		// Spinner inside button
		&__spinner {
			width: 1.25rem;
			height: 1.25rem;
			color: $white;
			animation: submit-spin 1s linear infinite;
		}

		&__spinner-circle {
			opacity: 0.25;
		}

		&__spinner-path {
			opacity: 0.75;
		}

		// ── Submission history section ─────────────────────────────
		&__history {
			margin-top: 2rem;
			padding: 2rem;
			border-radius: $radius-3xl;
			border: 1px solid $gray-100;
			background: $white;
			transition: all $transition-duration $transition-ease;

			@media (min-width: 768px) {
				padding: 2.5rem;
			}

			.dark & {
				background: $zinc-900;
				border-color: $zinc-800;
			}
		}

		&__history-title {
			font-size: 1.25rem;
			line-height: 1.75rem;
			font-weight: 700;
			margin-bottom: 1rem;
			color: $gray-800;

			.dark & {
				color: $zinc-100;
			}
		}

		&__history-list {
			display: flex;
			flex-direction: column;
			gap: 1rem;
		}

		// Individual history card
		&__history-item {
			padding: 1rem;
			border-radius: $radius-xl;
			border: 1px solid $gray-200;
			background: $gray-50;

			.dark & {
				border-color: $zinc-700;
				background: $zinc-800;
			}
		}

		&__history-item-header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			margin-bottom: 0.5rem;
		}

		&__history-item-title {
			font-weight: 700;
			color: $gray-800;

			.dark & {
				color: $zinc-200;
			}
		}

		&__attempt-badge {
			margin-left: 0.5rem;
			display: inline-block;
			font-size: 0.75rem;
			line-height: 1rem;
			font-weight: 500;
			padding: 0.125rem 0.5rem;
			border-radius: $radius-full;
			background: $blue-100;
			color: $blue-700;

			.dark & {
				background: rgb(30 58 138 / 0.5); // blue-900/50
				color: $blue-300;
			}
		}

		&__history-desc {
			font-size: 0.875rem;
			line-height: 1.25rem;
			margin-bottom: 0.75rem;
			color: $gray-600;

			.dark & {
				color: $zinc-400;
			}
		}

		// Links row
		&__links {
			display: flex;
			flex-direction: column;
			gap: 0.5rem;
			font-size: 0.875rem;
		}

		&__link {
			display: inline-flex;
			align-items: center;
			gap: 0.25rem;
			text-decoration: none;

			&:hover {
				text-decoration: underline;
			}

			&--github {
				color: $blue-500;
			}

			&--youtube {
				color: $red-500;
			}

			&--slide {
				color: $orange-500;
			}
		}

		&__link-icon {
			width: 1rem;
			height: 1rem;
		}

		// ── Scores & Feedback ──────────────────────────────────────
		&__scores-section {
			margin-top: 1rem;
			padding-top: 1rem;
			border-top: 1px solid $gray-200;

			.dark & {
				border-top-color: $zinc-700;
			}
		}

		&__scores-title {
			font-size: 0.875rem;
			line-height: 1.25rem;
			font-weight: 600;
			margin-bottom: 0.5rem;
			color: $gray-700;

			.dark & {
				color: $zinc-300;
			}
		}

		&__scores-list {
			display: flex;
			flex-direction: column;
			gap: 0.5rem;
		}

		&__score-row {
			display: flex;
			justify-content: space-between;
			align-items: center;
			font-size: 0.875rem;
		}

		&__score-label {
			color: $gray-600;

			.dark & {
				color: $zinc-400;
			}
		}

		&__score-value {
			font-weight: 500;
			color: $gray-900;

			.dark & {
				color: $zinc-200;
			}
		}

		&__score-total {
			display: flex;
			justify-content: space-between;
			align-items: center;
			font-weight: 700;
			margin-top: 0.5rem;
			padding-top: 0.5rem;
			border-top: 1px solid $gray-200;

			.dark & {
				border-top-color: $zinc-700;
			}
		}

		&__score-total-label {
			color: $gray-700;

			.dark & {
				color: $zinc-300;
			}
		}

		&__score-total-value {
			color: $blue-500;
		}

		// "Not graded yet" placeholder
		&__no-grade {
			display: flex;
			align-items: center;
			gap: 0.5rem;
			font-size: 0.875rem;
			font-style: italic;
			color: $gray-500;

			.dark & {
				color: $zinc-500;
			}
		}

		&__no-grade-icon {
			width: 1rem;
			height: 1rem;
		}
	}
</style>
