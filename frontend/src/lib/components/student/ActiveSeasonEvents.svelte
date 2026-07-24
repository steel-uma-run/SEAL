<script lang="ts">
	import { formatSeasonName } from "$lib/utils/seasons"
	import EventCard from "$lib/components/EventCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"

	let { activeSeason, events = [] } = $props<{
		activeSeason: any
		events: any[]
	}>()
</script>

<!-- Reusable Event Card Snippet -->
{#snippet eventCard(event: any)}
	<div class="active-events-card">
		<div>
			<div class="active-events-card__header">
				<div>
					<h3 class="active-events-card__title">
						{event.name}
					</h3>
					<!-- Tracks -->
					{#if event.tracks && event.tracks.length > 0}
						<div class="active-events-card__tracks">
							{#each event.tracks as track}
								<span class="active-events-card__track-badge" title={track.description}>
									{track.name}
								</span>
							{/each}
						</div>
					{/if}
				</div>
			</div>
			<p class="active-events-card__description">
				{event.description}
			</p>
		</div>

		<div class="active-events-card__footer">
			<div class="active-events-card__date">
				<svg class="active-events-card__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
					></path>
				</svg>
				<span
					>{formatFullDate(event.start_time || event.startTime)} - {formatFullDate(
						event.end_time || event.endTime
					)}</span
				>
			</div>

			<a href="/events/{event.id}" class="active-events-card__link"> View Details </a>
		</div>
	</div>
{/snippet}

<div class="active-events-panel">
	<div class="active-events-panel__header">
		<div>
			<h2 class="active-events-panel__title">Active Season Events</h2>
			<p class="active-events-panel__subtitle">
				{#if activeSeason}
					Events scheduled for {formatSeasonName(activeSeason)}
				{:else}
					No active season found for the current date.
				{/if}
			</p>
		</div>

		<!-- Navigation Arrows -->
		{#if events.length > 3}
			<div class="active-events-panel__nav">
				<button
					onclick={prevEvent}
					class="active-events-panel__nav-btn"
					aria-label="Previous event"
				>
					<svg
						class="active-events__nav-icon"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 19l-7-7 7-7"
						></path>
					</svg>
				</button>
				<button onclick={nextEvent} class="active-events-panel__nav-btn" aria-label="Next event">
					<svg
						class="active-events__nav-icon"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"
						></path>
					</svg>
				</button>
			</div>
		{/if}
	</div>

	{#if !activeSeason}
		<div class="active-events-panel__empty-season">
			<p>We couldn't detect an active season matching today's date.</p>
		</div>
	{:else if events.length === 0}
		<div class="active-events-panel__empty">
			<svg class="active-events__empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="1.5"
					d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
				></path>
			</svg>
			<p class="active-events-panel__empty-title">
				No events scheduled for {formatSeasonName(activeSeason)} yet.
			</p>
			<p class="active-events-panel__empty-subtitle">Check back later for updates!</p>
		</div>
	{:else}
		<div class="relative-container">
			{#key currentIndex}
				<div in:fade={{ duration: 250 }} class="active-events-grid">
					{@render eventCard(events[currentIndex])}

					{#if events.length > 1}
						{@render eventCard(events[(currentIndex + 1) % events.length])}
					{/if}

					{#if events.length > 2}
						{@render eventCard(events[(currentIndex + 2) % events.length])}
					{/if}
				</div>
			{/key}
		</div>

		<!-- Page Dots Indicators -->
		{#if events.length > 3}
			<div class="active-events-panel__dots">
				{#each events as _, i}
					<button
						onclick={() => (currentIndex = i)}
						class="active-events-panel__dot {currentIndex === i
							? 'active-events-panel__dot--active'
							: 'active-events-panel__dot--inactive'}"
						aria-label="Go to event {i + 1}"
					></button>
				{/each}
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	$bp-sm: 640px;
	$bp-lg: 1024px;

	.active-events-panel {
		padding: 2rem;
		border-radius: 1.5rem;
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-low);
		margin-bottom: 2rem;
		transition:
			background-color 0.3s,
			color 0.3s;

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 2rem;
		}

		&__title {
			font-size: 1.25rem;
			font-weight: 700;
			color: var(--md-on-surface);
			margin: 0;
		}

		&__subtitle {
			font-size: 0.875rem;
			margin-top: 0.25rem;
			color: var(--md-on-surface-variant);
			margin-bottom: 0;
		}

		&__nav {
			display: flex;
			align-items: center;
			gap: 0.5rem;
		}

		&__nav-btn {
			padding: 0.5rem;
			border-radius: 0.75rem;
			border: 1px solid var(--md-outline-variant);
			background-color: var(--md-surface-container-high);
			color: var(--md-on-surface);
			cursor: pointer;
			transition: all 0.2s;
			display: flex;
			align-items: center;
			justify-content: center;

			&:hover {
				background-color: var(--md-surface-container-highest);
			}
		}

		&__empty-season {
			text-align: center;
			padding: 2rem 0;
			color: var(--md-on-surface-variant);
			font-size: 1.125rem;
		}

		&__empty {
			text-align: center;
			padding: 2.5rem 1rem;
			border: 1px dashed var(--md-outline-variant);
			border-radius: 1rem;
			background-color: var(--md-surface-container);
		}

		&__empty-title {
			font-size: 1rem;
			font-weight: 500;
			color: var(--md-on-surface);
			margin: 0;
		}

		&__empty-subtitle {
			font-size: 0.75rem;
			margin-top: 0.25rem;
			color: var(--md-on-surface-variant);
			margin-bottom: 0;
		}

		&__dots {
			display: flex;
			justify-content: center;
			align-items: center;
			gap: 0.375rem;
			margin-top: 1.5rem;
		}

		&__dot {
			height: 0.5rem;
			border-radius: 9999px;
			border: 0;
			padding: 0;
			cursor: pointer;
			transition: all 0.3s;

			&--active {
				width: 1.5rem;
				background-color: var(--md-primary);
			}

			&--inactive {
				width: 0.5rem;
				background-color: var(--md-outline);
			}
		}
	}

	.active-events__nav-icon {
		width: 1.25rem;
		height: 1.25rem;
	}

	.active-events__empty-icon {
		width: 3rem;
		height: 3rem;
		margin: 0 auto 0.75rem;
		color: var(--md-on-surface-variant);
		opacity: 0.6;
	}

	.active-events-card {
		border: 1px solid var(--md-outline-variant);
		border-radius: 1rem;
		padding: 2rem;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		background-color: var(--md-surface-container);
		transition: background-color 0.3s;

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: flex-start;
			gap: 1rem;
			margin-bottom: 1rem;
		}

		&__title {
			font-weight: 800;
			font-size: 1.25rem;
			color: var(--md-on-surface);
			margin: 0;
		}

		&__description {
			font-size: 0.875rem;
			line-height: 1.625;
			margin-bottom: 1.5rem;
			color: var(--md-on-surface-variant);
		}

		&__footer {
			padding-top: 1.25rem;
			border-top: 1px solid var(--md-outline-variant);
			display: flex;
			flex-direction: column;
			gap: 1rem;

			@media (min-width: $bp-sm) {
				flex-direction: row;
				align-items: center;
				justify-content: space-between;
			}
		}

		&__date {
			display: flex;
			align-items: center;
			gap: 0.5rem;
			font-size: 0.75rem;
			color: var(--md-on-surface-variant);
		}

		&__icon {
			width: 1rem;
			height: 1rem;
			flex-shrink: 0;
			color: var(--md-primary);
		}

		&__link {
			padding: 0.625rem 1.5rem;
			text-align: center;
			border-radius: 0.75rem;
			font-size: 0.75rem;
			font-weight: 700;
			text-decoration: none;
			background-color: var(--md-primary);
			color: var(--md-on-primary);
			cursor: pointer;
			transition: opacity 0.2s;

			&:hover {
				opacity: 0.9;
			}
		}

		&__tracks {
			display: flex;
			flex-wrap: wrap;
			gap: 0.375rem;
			margin-top: 0.5rem;
		}

		&__track-badge {
			display: inline-flex;
			align-items: center;
			padding: 0.125rem 0.625rem;
			border-radius: 9999px;
			font-size: 0.625rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			background-color: var(--md-secondary-container);
			color: var(--md-on-secondary-container);
			border: 1px solid var(--md-outline-variant);
		}
	}

	.active-events-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem;

		@media (min-width: $bp-sm) {
			grid-template-columns: repeat(2, 1fr);
		}

		@media (min-width: $bp-lg) {
			grid-template-columns: repeat(3, 1fr);
		}
	}

	.relative-container {
		position: relative;
		overflow: hidden;
		min-h: 220px;
	}
</style>
