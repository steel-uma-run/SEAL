<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import {
		createTeam,
		getAllSeasons,
		getSelfProfile,
		getEventsInSeason,
		getInterestedParticipants
	} from "$lib/api"
	import { theme } from "$lib/theme.svelte"

	let profile = $state<any>(null)
	let events = $state<any[]>([])
	let eventsError = $state("")
	let isPageLoading = $state(true)
	let hasNoEvents = $state(false)

	let teamName = $state("")
	let teamSize = $state(4)
	let teamDescription = $state("")
	let selectedEventId = $state("")
	let isLoading = $state(false)
	let message = $state("")
	let isError = $state(false)

	onMount(async () => {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (profileRes?.ok && profileData) {
				profile = profileData
			} else {
				goto("/auth/login")
				return
			}
		} catch (err) {
			goto("/auth/login")
			return
		}

		// Load joined events across all active seasons
		try {
			const { data: seasonsData, response: seasonsRes } = await getAllSeasons({
				throwOnError: false
			})
			if (seasonsRes?.ok && seasonsData) {
				let joinedList: any[] = []
				for (const season of seasonsData) {
					let allSeasonEvents: any[] = []

					const { data: eventsData, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes?.ok && eventsData) {
						allSeasonEvents = eventsData
					}

					// 3. Check participants for each event
					for (const event of allSeasonEvents) {
						let hasJoined = false

						const { data: participants } = await getInterestedParticipants({
							path: { eventId: event.id },
							throwOnError: false
						})
						if (participants && participants.some((p: any) => p.email === profile.email)) {
							hasJoined = true
						}

						if (hasJoined && event.open_for_registration) {
							joinedList.push(event)
						}
					}
				}
				events = joinedList
				if (events.length === 0) {
					hasNoEvents = true
				}
			} else {
				eventsError = "Could not load data."
			}
		} catch (err) {
			eventsError = "Could not connect to the server."
		} finally {
			isPageLoading = false
		}
	})

	async function handleCreateTeam(e: Event) {
		e.preventDefault()
		if (!selectedEventId) {
			isError = true
			message = "Please select an event."
			return
		}

		if (teamSize < 3 || teamSize > 5) {
			isError = true
			message = "Team size must be between 3 and 5 members."
			return
		}
		isLoading = true
		message = ""
		isError = false

		try {
			const finalDescription = `[Expected Size: ${teamSize}] ${teamDescription}`

			const {
				data: teamData,
				error: apiErr,
				response: res
			} = await createTeam({
				body: {
					name: teamName,
					description: finalDescription,
					event_id: selectedEventId
				},
				throwOnError: false
			})
			if (res?.ok) {
				message = "Team created successfully!"
				teamName = ""
				teamDescription = ""
				selectedEventId = ""
				setTimeout(() => goto("/student/teams"), 1500)
			} else {
				isError = true
				const errBody = apiErr as any
				message =
					errBody?.detail ||
					errBody?.title ||
					res?.statusText ||
					"Failed to create team. Please try again."
			}
		} catch (err) {
			isError = true
			message = "Error connecting to the server."
		} finally {
			isLoading = false
		}
	}
</script>

<svelte:head>
	<title>Create Team - SEAL</title>
</svelte:head>
<div class="create-team-page {theme.darkMode ? 'create-team-page--dark' : ''}">
	<a href="/student" class="back-link">
		<svg class="back-link__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
			><path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path></svg
		>
		Back to Dashboard
	</a>

	<div class="form-card">
		<div class="form-card__header">
			<div class="form-card__icon-wrap">
				<svg class="form-card__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
					></path></svg
				>
			</div>
			<div class="form-card__text">
				<h1 class="form-card__title">Create a Team</h1>
				<p class="form-card__subtitle">Form your squad for the upcoming hackathon.</p>
			</div>
		</div>

		{#if hasNoEvents && !isPageLoading}
			<div class="alert-box alert-box--required">
				<div class="alert-box__content">
					<h3 class="alert-box__title">Action Required: Join an Event</h3>
					<p class="alert-box__desc">
						You must be a registered participant of at least one active event before you can create
						a team. Return to the dashboard to find and join an event.
					</p>
				</div>
				<a href="/student" class="btn btn--danger">
					<svg class="btn__icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M10 19l-7-7m0 0l7-7m-7 7h18"
						></path></svg
					>
					Go to Dashboard
				</a>
			</div>
		{/if}

		<form onsubmit={handleCreateTeam} class="team-form" class:team-form--disabled={hasNoEvents}>
			<!-- Field 1: Event -->
			<div class="form-group">
				<label class="form-label">Event</label>
				<select
					bind:value={selectedEventId}
					required
					disabled={hasNoEvents}
					class="form-input form-input--select"
				>
					<option value="" disabled
						>{isPageLoading ? "Loading events..." : "Select an event"}</option
					>
					{#each events as event}
						<option value={event.id}>{event.name}</option>
					{/each}
				</select>
				{#if eventsError}
					<p class="field-error">
						<svg class="field-error__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
							></path></svg
						>
						{eventsError}
					</p>
				{/if}
			</div>

			<!-- Field 2: Team Name -->
			<div class="form-group">
				<label class="form-label">Team Name</label>
				<input
					type="text"
					bind:value={teamName}
					required
					placeholder="Enter team name"
					class="form-input"
				/>
			</div>

			<!-- Field 3: Expected Team Size -->
			<div class="form-group">
				<label class="form-label">Expected Team Size (3-5 members)</label>
				<input type="number" bind:value={teamSize} min="3" max="5" required class="form-input" />
			</div>

			<!-- Field 4: Description -->
			<div class="form-group">
				<label class="form-label">Description</label>
				<textarea
					bind:value={teamDescription}
					rows="4"
					placeholder="Describe your team's project idea"
					class="form-input form-input--textarea"
				></textarea>
			</div>

			<!-- Message -->
			{#if message}
				<div
					class="form-message"
					class:form-message--error={isError}
					class:form-message--success={!isError}
				>
					{#if !isError}
						<svg class="form-message__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M5 13l4 4L19 7"
							></path></svg
						>
					{/if}
					{message}
				</div>
			{/if}

			<!-- Submit button -->
			<button type="submit" disabled={isLoading} class="btn btn--submit">
				{#if isLoading}
					<svg class="btn__spinner" fill="none" viewBox="0 0 24 24">
						<circle
							class="btn__spinner-bg"
							cx="12"
							cy="12"
							r="10"
							stroke="currentColor"
							stroke-width="4"
						></circle>
						<path
							class="btn__spinner-fg"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
				{/if}
				{isLoading ? "Creating Team..." : "Create Team"}
			</button>
		</form>
	</div>
</div>

<style lang="scss">
	// ============================================================================
	// Create Team Page - SCSS Conversion
	// ============================================================================
	$bp-md: 768px;

	.create-team-page {
		max-width: 42rem; // max-w-2xl
		margin-left: auto;
		margin-right: auto;
		width: 100%;
		padding: 1.5rem; // p-6
		@media (min-width: $bp-md) {
			padding: 2.5rem;
		} // md:p-10

		&--dark {
		}
	}

	// Back link - inline-flex items-center gap-2 transition-colors mb-8 font-medium text-zinc-400 hover:text-orange-400 / gray-500 hover:orange-600
	.back-link {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		transition: color 0.2s ease;
		margin-bottom: 2rem;
		font-weight: 500;
		text-decoration: none;
		color: #6b7280;
		&:hover {
			color: #ea580c;
		}

		.create-team-page--dark & {
			color: #a1a1aa;
			&:hover {
				color: #fb923c;
			}
		}

		&__icon {
			width: 1.25rem;
			height: 1.25rem;
		}
	}

	// Form card - p-8 md:p-10 rounded-3xl border bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)] / bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]
	.form-card {
		padding: 2rem; // p-8
		border-radius: 1.5rem; // rounded-3xl
		border: 1px solid #f3f4f6; // border-gray-100
		background: #fff;
		box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
		transition: all 0.3s ease;

		@media (min-width: $bp-md) {
			padding: 2.5rem;
		} // md:p-10

		.create-team-page--dark & {
			background: #18181b; // bg-zinc-900
			border-color: #27272a; // border-zinc-800
			box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
		}

		&__header {
			display: flex;
			align-items: center;
			gap: 1rem;
			margin-bottom: 2rem;
			border-bottom: 1px solid #f3f4f6; // border-gray-100
			padding-bottom: 1.5rem; // pb-6

			.create-team-page--dark & {
				border-color: #27272a; // border-zinc-800
			}
		}

		&__icon-wrap {
			padding: 0.75rem; // p-3
			border-radius: 0.75rem; // rounded-xl
			background: #ffedd5; // bg-orange-100
			color: #ea580c; // text-orange-600

			.create-team-page--dark & {
				background: rgba(67, 20, 7, 0.4); // bg-orange-950/40
				color: #fb923c;
			}
		}

		&__icon {
			width: 2rem;
			height: 2rem;
		} // w-8 h-8

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			color: #1f2937; // text-gray-800
			.create-team-page--dark & {
				color: #f4f4f5;
			}
		}

		&__subtitle {
			font-size: 0.875rem; // text-sm
			margin-top: 0.25rem; // mt-1
			color: #6b7280; // text-gray-500
			.create-team-page--dark & {
				color: #a1a1aa;
			}
		}
	}

	// Alert box - p-6 rounded-xl border border-red-500/20 bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 font-medium flex flex-col items-start gap-4 shadow-sm
	.alert-box {
		padding: 1.5rem; // p-6
		border-radius: 0.75rem; // rounded-xl
		border: 1px solid rgba(239, 68, 68, 0.2); // border-red-500/20
		background: #fef2f2; // bg-red-50
		color: #dc2626; // text-red-600
		font-weight: 500;
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 1rem;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

		.create-team-page--dark & {
			background: rgba(69, 10, 10, 0.3); // bg-red-950/30
			color: #f87171; // text-red-400
		}

		&__title {
			font-size: 1.125rem;
			font-weight: 700;
		} // text-lg font-bold
		&__desc {
			margin-top: 0.25rem;
			font-size: 0.875rem;
			opacity: 0.9;
			line-height: 1.625;
		} // text-sm opacity-90 leading-relaxed
	}

	// Buttons
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		border-radius: 0.75rem; // rounded-xl
		font-weight: 700;
		cursor: pointer;
		border: none;
		text-decoration: none;
		transition: all 0.2s ease;

		&:disabled {
			opacity: 0.7;
			cursor: not-allowed;
		}

		&__icon-sm {
			width: 1rem;
			height: 1rem;
		} // w-4 h-4
		&__spinner {
			width: 1.25rem;
			height: 1.25rem; // h-5 w-5
			color: #fff;
			animation: spin 1s linear infinite;
		}
		&__spinner-bg {
			opacity: 0.25;
		}
		&__spinner-fg {
			opacity: 0.75;
		}

		// px-5 py-2.5 bg-red-600 hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600 text-white rounded-xl text-sm font-bold
		&--danger {
			padding: 0.625rem 1.25rem;
			background: #dc2626; // bg-red-600
			color: #fff;
			font-size: 0.875rem;
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
			&:hover {
				background: #b91c1c;
			}

			.create-team-page--dark & {
				background: #ef4444; // bg-red-500
				&:hover {
					background: #dc2626;
				}
			}
		}

		// mt-4 w-full bg-[#ea580c] text-white rounded-xl py-4 font-bold text-lg shadow-sm hover:bg-[#c2410c] hover:shadow-md disabled:opacity-70
		&--submit {
			margin-top: 1rem;
			width: 100%;
			background: #ea580c;
			color: #fff;
			border-radius: 0.75rem;
			padding: 1rem 0; // py-4
			font-weight: 700;
			font-size: 1.125rem; // text-lg
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
			&:hover {
				background: #c2410c;
				box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
			}
			&:disabled {
				opacity: 0.7;
			}
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	// Team form - flex flex-col gap-6 opacity-50 pointer-events-none when disabled
	.team-form {
		display: flex;
		flex-direction: column;
		gap: 1.5rem; // gap-6

		&--disabled {
			opacity: 0.5;
			pointer-events: none;
		}
	}

	// Form group - space-y-2
	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem; // space-y-2
	}

	.form-label {
		font-size: 0.875rem; // text-sm
		font-weight: 600; // font-semibold
		color: #374151; // text-gray-700
		.create-team-page--dark & {
			color: #d4d4d8;
		} // text-zinc-300
	}

	// Form input - w-full rounded-xl p-3.5 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none border bg-gray-50 border-gray-200 text-gray-900 / bg-zinc-950 border-zinc-800 text-zinc-100
	.form-input {
		width: 100%;
		border-radius: 0.75rem; // rounded-xl
		padding: 0.875rem; // p-3.5
		border: 1px solid #e5e7eb; // border-gray-200
		background: #f9fafb; // bg-gray-50
		color: #111827; // text-gray-900
		outline: none;
		transition: all 0.2s ease;
		font-size: 0.9rem;

		&::placeholder {
			color: #9ca3af;
		}

		&:focus {
			box-shadow: 0 0 0 2px #f97316; // focus:ring-2 focus:ring-orange-500
			border-color: transparent; // focus:border-transparent
		}

		.create-team-page--dark & {
			background: #09090b; // bg-zinc-950
			border-color: #27272a; // border-zinc-800
			color: #f4f4f5; // text-zinc-100
			&::placeholder {
				color: #52525b;
			} // placeholder-zinc-600
		}

		&--select {
			cursor: pointer;
			// option dark bg
			option {
				.create-team-page--dark & {
					background: #09090b;
				}
			}
		}

		&--textarea {
			resize: none; // resize-none
		}
	}

	// Field error - text-sm text-red-500 flex items-center gap-1.5 mt-1
	.field-error {
		font-size: 0.875rem;
		color: #ef4444;
		display: flex;
		align-items: center;
		gap: 0.375rem;
		margin-top: 0.25rem;

		&__icon {
			width: 1rem;
			height: 1rem;
			flex-shrink: 0;
		}
	}

	// Form message - p-4 rounded-xl text-sm font-medium border flex items-center gap-3
	// variants: bg-red-950/20 text-red-400 border-red-900/50 / bg-red-50 text-red-600 border-red-100 / bg-green-950/20 etc / bg-green-50
	.form-message {
		padding: 1rem; // p-4
		border-radius: 0.75rem;
		font-size: 0.875rem;
		font-weight: 500;
		border: 1px solid;
		display: flex;
		align-items: center;
		gap: 0.75rem;

		&--error {
			background: #fef2f2; // bg-red-50
			color: #dc2626; // text-red-600
			border-color: #fee2e2; // border-red-100
			.create-team-page--dark & {
				background: rgba(69, 10, 10, 0.2); // bg-red-950/20
				color: #f87171; // text-red-400
				border-color: rgba(127, 29, 29, 0.5); // border-red-900/50
			}
		}

		&--success {
			background: #f0fdf4; // bg-green-50
			color: #16a34a; // text-green-600
			border-color: #dcfce7; // border-green-100
			.create-team-page--dark & {
				background: rgba(20, 83, 45, 0.2); // bg-green-950/20
				color: #4ade80; // text-green-400
				border-color: rgba(20, 83, 45, 0.5); // border-green-900/50
			}
		}

		&__icon {
			width: 1.25rem;
			height: 1.25rem;
		}
	}
</style>
