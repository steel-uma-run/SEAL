<script lang="ts">
	import { formatSeasonName } from "$lib/utils/seasons"
	import EventCard from "$lib/components/EventCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"

	let {
		activeSeason,
		events = [],
		basePath = "/events"
	} = $props<{
		activeSeason: any
		events: any[]
		basePath?: string
	}>()
</script>

<div class="active-season-container">
	{#if !activeSeason}
		<div style="text-align: center">
			<KaomojiError
				kind="neutral"
				text="We couldn't detect an active season matching today's date."
			/>
		</div>
	{:else if events.length === 0}
		<div style="text-align: center">
			<KaomojiError
				kind="neutral"
				text="No events scheduled for {formatSeasonName(activeSeason)} yet. Check back later!"
			/>
		</div>
	{:else}
		<div class="season-section">
			<div class="season-header">
				<div class="season-title-wrapper">
					<span class="season-badge">Active Season</span>
					<h2 class="season-title">{formatSeasonName(activeSeason)}</h2>
				</div>
				<span class="event-count-chip"
					>{events.length} {events.length === 1 ? "event" : "events"}</span
				>
			</div>

			<div class="event-cards">
				{#each events as event}
					<EventCard {event} href="{basePath}/{event.id}" />
				{/each}
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	.active-season-container {
		width: 100%;
	}

	.season-section {
		margin-top: 0.5rem;
	}

	.season-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 1.5rem;
		padding-bottom: 0.75rem;
		border-bottom: 1px solid var(--md-outline-variant, #e0e0e0);
	}

	.season-title-wrapper {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		flex-wrap: wrap;
	}

	.season-badge {
		background: var(--md-primary-container, #e8def8);
		color: var(--md-on-primary-container, #1d192b);
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
	}

	.season-title {
		margin: 0;
		font-weight: 800;
		font-size: 1.35rem;
		color: var(--md-on-surface, #1c1b1f);
		letter-spacing: -0.01em;
	}

	.event-count-chip {
		font-size: 0.8rem;
		font-weight: 600;
		color: var(--md-on-surface-variant, #49454f);
		background: var(--md-surface-container-high, #f3edf7);
		padding: 0.25rem 0.75rem;
		border-radius: 8px;
	}

	.event-cards {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 280px), 340px));
		justify-content: start;
		gap: 1.5rem;
		margin-bottom: 3rem;
	}
</style>

