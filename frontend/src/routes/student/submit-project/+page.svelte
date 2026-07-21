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

<div class="page-container">
	<!-- Back Link -->
	<a href="/student" class="back-link">
		<svg class="icon-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path>
		</svg>
		Back to Dashboard
	</a>

	<!-- Main Card -->
	<div class="card">
		<div class="card-header">
			<div class="card-header__icon">
				<svg class="icon-32" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
					></path>
				</svg>
			</div>
			<div>
				<h1 class="card-header__title">Submit Project</h1>
				<p class="card-header__subtitle">Submit your team's final work for evaluation.</p>
			</div>
		</div>

		<!-- Not Team Leader Message -->
		<div class="locked-notice" id="lockedNotice">
			<svg class="icon-64" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
				></path>
			</svg>
			<h2 class="locked-notice__title">Only the Team Leader can submit</h2>
			<p class="locked-notice__text">
				You are a member of this team. Please ask your team leader to submit the project.
			</p>
		</div>

		<!-- Submit Form -->
		<form id="submitForm" class="submit-form">
			<!-- Project Title -->
			<div class="form-group">
				<label class="form-label">Project Title (Required)</label>
				<input
					type="text"
					required
					placeholder="Enter your project title"
					class="form-input"
					id="title"
				/>
			</div>

			<!-- Project Description -->
			<div class="form-group">
				<label class="form-label">Project Description (Required)</label>
				<textarea
					required
					rows="4"
					placeholder="Describe your project"
					class="form-input form-textarea"
					id="description"
				></textarea>
			</div>

			<!-- Git Link -->
			<div class="form-group">
				<label class="form-label">Git Link (Required)</label>
				<div class="input-with-icon">
					<div class="input-icon">
						<svg class="icon-20" fill="currentColor" viewBox="0 0 24 24">
							<path
								fill-rule="evenodd"
								d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
								clip-rule="evenodd"
							></path>
						</svg>
					</div>
					<input
						type="url"
						required
						placeholder="https://github.com/..."
						class="form-input form-input--padded"
						id="submitLink"
					/>
				</div>
			</div>

			<!-- YouTube Link -->
			<div class="form-group">
				<label class="form-label">YouTube Link (Optional)</label>
				<div class="input-with-icon">
					<div class="input-icon">
						<svg class="icon-20" fill="currentColor" viewBox="0 0 24 24">
							<path
								d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
							></path>
						</svg>
					</div>
					<input
						type="url"
						placeholder="https://youtube.com/..."
						class="form-input form-input--padded"
						id="youtubeLink"
					/>
				</div>
			</div>

			<!-- Presentation Slide Link -->
			<div class="form-group">
				<label class="form-label">Presentation Slide Link</label>
				<div class="input-with-icon">
					<div class="input-icon">
						<svg class="icon-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
						placeholder="Google Slides or PPT link"
						class="form-input form-input--padded"
						id="slideLink"
					/>
				</div>
			</div>

			<!-- Submit Message -->
			<div class="submit-message" id="submitMessage" style="display: none;">
				<svg
					class="icon-20 flex-shrink-0 mt-05"
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
				<span id="submitMessageText"></span>
			</div>

			<!-- Submit Button -->
			<button type="submit" class="submit-btn" id="submitBtn">
				<svg class="spinner" id="spinner" style="display: none;" fill="none" viewBox="0 0 24 24">
					<circle
						class="spinner__track"
						cx="12"
						cy="12"
						r="10"
						stroke="currentColor"
						stroke-width="4"
					></circle>
					<path
						class="spinner__head"
						fill="currentColor"
						d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
					></path>
				</svg>
				<span id="submitBtnText">Submit Project</span>
			</button>
		</form>
	</div>

	<!-- Submission History -->
	<div class="card history-card" id="historySection">
		<h2 class="history__title">Submission History</h2>
		<div class="history__list">
			<!-- Example history item — repeat this block per submission -->
			<div class="history-item">
				<div class="history-item__header">
					<h3 class="history-item__name">
						Project Title Here
						<span class="history-item__badge">Attempt 2</span>
					</h3>
				</div>
				<p class="history-item__desc">Description of the submission goes here.</p>

				<div class="history-item__links">
					<a href="#" target="_blank" rel="noopener noreferrer" class="link link--blue">
						<svg class="icon-16" fill="currentColor" viewBox="0 0 24 24">
							<path
								fill-rule="evenodd"
								d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
								clip-rule="evenodd"
							></path>
						</svg>
						GitHub Repository
					</a>

					<a href="#" target="_blank" rel="noopener noreferrer" class="link link--red">
						<svg class="icon-16" fill="currentColor" viewBox="0 0 24 24">
							<path
								d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
							></path>
						</svg>
						YouTube Demo
					</a>

					<a href="#" target="_blank" rel="noopener noreferrer" class="link link--orange">
						<svg class="icon-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
							></path>
						</svg>
						Presentation Slide
					</a>
				</div>

				<!-- Scores Section -->
				<div class="scores">
					<h4 class="scores__heading">Scores &amp; Feedback</h4>

					<!-- If scores exist -->
					<div class="scores__list">
						<div class="scores__row">
							<span class="scores__criteria">Criteria Name</span>
							<span class="scores__value">85/100</span>
						</div>
						<div class="scores__row">
							<span class="scores__criteria">Another Criteria</span>
							<span class="scores__value">92/100</span>
						</div>
						<div class="scores__total">
							<span>Total Average</span>
							<span class="scores__avg">88.5/100</span>
						</div>
					</div>

					<!-- If no scores yet -->
					<div class="scores__empty">
						<svg class="icon-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
							></path>
						</svg>
						Not Graded Yet / Chưa có điểm
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<style lang="scss">
	// ============================================
	// Variables & Design Tokens
	// ============================================

	// Breakpoints
	$md: 768px;

	// Colors — Light
	$gray-50: #f9fafb;
	$gray-100: #f3f4f6;
	$gray-200: #e5e7eb;
	$gray-400: #9ca3af;
	$gray-500: #6b7280;
	$gray-600: #4b5563;
	$gray-700: #374151;
	$gray-800: #1f2937;
	$gray-900: #111827;

	// Colors — Dark
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

	// Colors — Accents
	$blue-100: #dbeafe;
	$blue-300: #93c5fd;
	$blue-400: #60a5fa;
	$blue-500: #3b82f6;
	$blue-600: #2563eb;
	$blue-700: #1d4ed8;
	$blue-900: #1e3a5f;
	$blue-950: #172554;
	$red-500: #ef4444;
	$orange-500: #f97316;

	// Spacing scale (Tailwind 4px base)
	$sp-1: 0.25rem; // 4px
	$sp-1-5: 0.375rem; // 6px
	$sp-2: 0.5rem; // 8px
	$sp-3: 0.75rem; // 12px
	$sp-3-5: 0.875rem; // 14px
	$sp-4: 1rem; // 16px
	$sp-5: 1.25rem; // 20px
	$sp-6: 1.5rem; // 24px
	$sp-8: 2rem; // 32px
	$sp-10: 2.5rem; // 40px
	$sp-12: 3rem; // 48px

	// Border radius
	$rounded-xl: 0.75rem;
	$rounded-3xl: 1.5rem;
	$rounded-full: 9999px;

	// ============================================
	// Mixins
	// ============================================

	@mixin md {
		@media (min-width: $md) {
			@content;
		}
	}

	// ============================================
	// Reset & Base
	// ============================================

	*,
	*::before,
	*::after {
		box-sizing: border-box;
		margin: 0;
		padding: 0;
	}

	body {
		font-family:
			-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
		line-height: 1.5;
		color: $gray-800;
		background-color: #fff;
		transition:
			color 0.2s,
			background-color 0.2s;
	}

	// ============================================
	// Dark Mode (body.dark-mode)
	// ============================================

	body.dark-mode {
		color: $zinc-100;
		background-color: $zinc-950;
	}

	// ============================================
	// Icon Helpers
	// ============================================

	.icon-16 {
		width: 1rem;
		height: 1rem;
		flex-shrink: 0;
	}
	.icon-20 {
		width: 1.25rem;
		height: 1.25rem;
		flex-shrink: 0;
	}
	.icon-32 {
		width: 2rem;
		height: 2rem;
		flex-shrink: 0;
	}
	.icon-64 {
		width: 4rem;
		height: 4rem;
		flex-shrink: 0;
	}

	// ============================================
	// Page Container
	// ============================================

	.page-container {
		max-width: 42rem; // max-w-2xl
		margin-left: auto;
		margin-right: auto;
		width: 100%;
		padding: $sp-6;

		@include md {
			padding: $sp-10;
		}
	}

	// ============================================
	// Back Link
	// ============================================

	.back-link {
		display: inline-flex;
		align-items: center;
		gap: $sp-2;
		margin-bottom: $sp-8;
		font-weight: 500;
		text-decoration: none;
		transition: color 0.15s ease;

		color: $gray-500;
		&:hover {
			color: $blue-600;
		}

		body.dark-mode & {
			color: $zinc-400;
			&:hover {
				color: $blue-400;
			}
		}
	}

	// ============================================
	// Card
	// ============================================

	.card {
		padding: $sp-8;
		border-radius: $rounded-3xl;
		transition: all 0.2s ease;
		border: 1px solid;
		margin-bottom: 0;

		background-color: #fff;
		border-color: $gray-100;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

		@include md {
			padding: $sp-10;
		}

		body.dark-mode & {
			background-color: $zinc-900;
			border-color: $zinc-800;
			box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
		}
	}

	.history-card {
		margin-top: $sp-8;
	}

	// ============================================
	// Card Header
	// ============================================

	.card-header {
		display: flex;
		align-items: center;
		gap: $sp-4;
		margin-bottom: $sp-8;
		padding-bottom: $sp-6;
		border-bottom: 1px solid;

		border-color: $gray-100;

		body.dark-mode & {
			border-color: $zinc-800;
		}

		&__icon {
			padding: $sp-3;
			border-radius: $rounded-xl;

			background-color: $blue-100;
			color: $blue-600;

			body.dark-mode & {
				background-color: rgba($blue-950, 0.4);
				color: $blue-400;
			}
		}

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			line-height: 1.2;

			color: $gray-800;

			body.dark-mode & {
				color: $zinc-100;
			}
		}

		&__subtitle {
			font-size: 0.875rem; // text-sm
			margin-top: $sp-1;

			color: $gray-500;

			body.dark-mode & {
				color: $zinc-400;
			}
		}
	}

	// ============================================
	// Locked Notice (not team leader)
	// ============================================

	.locked-notice {
		margin-top: $sp-8;
		padding: $sp-8;
		border-radius: $rounded-3xl;
		text-align: center;

		// Uses CSS custom properties for Material-style tokens
		background-color: var(--md-surface-container, #f5f5f5);
		border: 1px solid var(--md-outline-variant, #e0e0e0);
		color: var(--md-on-surface-variant, #666);

		svg {
			margin-left: auto;
			margin-right: auto;
			margin-bottom: $sp-4;
			opacity: 0.5;
			color: inherit;
		}

		&__title {
			font-size: 1.25rem; // text-xl
			font-weight: 700;
			margin-bottom: $sp-2;

			color: $gray-800;

			body.dark-mode & {
				color: $zinc-100;
			}
		}

		&__text {
			color: $gray-500;

			body.dark-mode & {
				color: $zinc-400;
			}
		}
	}

	// ============================================
	// Submit Form
	// ============================================

	.submit-form {
		display: flex;
		flex-direction: column;
		gap: $sp-6;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: $sp-2;
	}

	.form-label {
		font-size: 0.875rem;
		font-weight: 600;

		color: $gray-700;

		body.dark-mode & {
			color: $zinc-300;
		}
	}

	.form-input {
		width: 100%;
		border-radius: $rounded-xl;
		padding: $sp-3-5;
		border: 1px solid;
		outline: none;
		font-size: 1rem;
		transition: all 0.15s ease;
		font-family: inherit;

		background-color: $gray-50;
		border-color: $gray-200;
		color: $gray-900;

		&::placeholder {
			color: $gray-400;

			body.dark-mode & {
				color: $zinc-600;
			}
		}

		&:focus {
			box-shadow: 0 0 0 2px $blue-500;
			border-color: transparent;
		}

		body.dark-mode & {
			background-color: $zinc-950;
			border-color: $zinc-800;
			color: $zinc-100;
		}
	}

	.form-textarea {
		resize: none;
	}

	.form-input--padded {
		padding-left: $sp-12;
	}

	// ============================================
	// Input with Icon
	// ============================================

	.input-with-icon {
		position: relative;
	}

	.input-icon {
		position: absolute;
		top: 0;
		bottom: 0;
		left: 0;
		padding-left: $sp-4;
		display: flex;
		align-items: center;
		pointer-events: none;

		color: $gray-400;

		body.dark-mode & {
			color: $zinc-600;
		}
	}

	// ============================================
	// Submit Message
	// ============================================

	.submit-message {
		padding: $sp-4;
		border-radius: $rounded-xl;
		font-size: 0.875rem;
		font-weight: 500;
		border: 1px solid;
		display: flex;
		align-items: flex-start;
		gap: $sp-3;
		transition: all 0.2s ease;

		background-color: $blue-100;
		color: $blue-700;
		border-color: $blue-100;

		body.dark-mode & {
			background-color: rgba($blue-950, 0.2);
			color: $blue-400;
			border-color: rgba($blue-900, 0.5);
		}
	}

	.flex-shrink-0 {
		flex-shrink: 0;
	}
	.mt-05 {
		margin-top: 0.125rem;
	}

	// ============================================
	// Submit Button
	// ============================================

	.submit-btn {
		margin-top: $sp-4;
		width: 100%;
		border: none;
		background-color: $blue-600;
		color: #fff;
		border-radius: $rounded-xl;
		padding: $sp-4 $sp-4;
		font-weight: 700;
		font-size: 1.125rem; // text-lg
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		cursor: pointer;
		display: flex;
		justify-content: center;
		align-items: center;
		gap: $sp-3;
		transition: all 0.15s ease;
		font-family: inherit;

		&:hover {
			background-color: $blue-700;
			box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
		}

		&:disabled {
			opacity: 0.7;
			cursor: not-allowed;
		}
	}

	// Spinner
	.spinner {
		width: 1.25rem;
		height: 1.25rem;
		animation: spin 0.75s linear infinite;

		&__track {
			opacity: 0.25;
		}

		&__head {
			opacity: 0.75;
		}
	}

	@keyframes spin {
		from {
			transform: rotate(0deg);
		}
		to {
			transform: rotate(360deg);
		}
	}

	// ============================================
	// Submission History
	// ============================================

	.history {
		&__title {
			font-size: 1.25rem;
			font-weight: 700;
			margin-bottom: $sp-4;

			color: $gray-800;

			body.dark-mode & {
				color: $zinc-100;
			}
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: $sp-4;
		}
	}

	// ============================================
	// History Item
	// ============================================

	.history-item {
		padding: $sp-4;
		border-radius: $rounded-xl;
		border: 1px solid;

		border-color: $gray-200;
		background-color: $gray-50;

		body.dark-mode & {
			border-color: $zinc-700;
			background-color: $zinc-800;
		}

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			margin-bottom: $sp-2;
		}

		&__name {
			font-weight: 700;

			color: $gray-800;

			body.dark-mode & {
				color: $zinc-200;
			}
		}

		&__badge {
			display: inline-block;
			margin-left: $sp-2;
			font-size: 0.75rem;
			font-weight: 500;
			padding: $sp-1-5 $sp-2;
			border-radius: $rounded-full;
			vertical-align: middle;

			background-color: $blue-100;
			color: $blue-700;

			body.dark-mode & {
				background-color: rgba($blue-900, 0.5);
				color: $blue-300;
			}
		}

		&__desc {
			font-size: 0.875rem;
			margin-bottom: $sp-3;

			color: $gray-600;

			body.dark-mode & {
				color: $zinc-400;
			}
		}

		&__links {
			display: flex;
			flex-direction: column;
			gap: $sp-2;
			font-size: 0.875rem;
		}
	}

	// ============================================
	// Links (in history items)
	// ============================================

	.link {
		display: flex;
		align-items: center;
		gap: $sp-1;
		text-decoration: none;
		transition: text-decoration-color 0.15s ease;

		&:hover {
			text-decoration: underline;
		}

		&--blue {
			color: $blue-500;
		}
		&--red {
			color: $red-500;
		}
		&--orange {
			color: $orange-500;
		}
	}

	// ============================================
	// Scores Section
	// ============================================

	.scores {
		margin-top: $sp-4;
		padding-top: $sp-4;
		border-top: 1px solid;

		border-color: $gray-200;

		body.dark-mode & {
			border-color: $zinc-700;
		}

		&__heading {
			font-size: 0.875rem;
			font-weight: 600;
			margin-bottom: $sp-2;

			color: $gray-700;

			body.dark-mode & {
				color: $zinc-300;
			}
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: $sp-2;
		}

		&__row {
			display: flex;
			justify-content: space-between;
			align-items: center;
			font-size: 0.875rem;
		}

		&__criteria {
			color: $gray-600;

			body.dark-mode & {
				color: $zinc-400;
			}
		}

		&__value {
			font-weight: 500;

			color: $gray-900;

			body.dark-mode & {
				color: $zinc-200;
			}
		}

		&__total {
			display: flex;
			justify-content: space-between;
			align-items: center;
			font-weight: 700;
			margin-top: $sp-2;
			padding-top: $sp-2;
			border-top: 1px solid;

			border-color: $gray-200;

			body.dark-mode & {
				border-color: $zinc-700;
			}

			span:first-child {
				color: $gray-700;

				body.dark-mode & {
					color: $zinc-300;
				}
			}
		}

		&__avg {
			color: $blue-500;
		}

		&__empty {
			display: flex;
			align-items: center;
			gap: $sp-2;
			font-size: 0.875rem;
			font-style: italic;

			color: $gray-500;

			body.dark-mode & {
				color: $zinc-500;
			}
		}
	}
</style>
