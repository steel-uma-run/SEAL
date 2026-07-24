<script lang="ts">
	import { formatSeasonName } from "$lib/utils/seasons"
	import EventCard from "$lib/components/EventCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"

	let { activeSeason, events = [] } = $props<{
		activeSeason: any
		events: any[]
	}>()
</script>

<div class="active-season-container">
	{#if !activeSeason}
		<div style="text-align: center">
			<KaomojiError kind="neutral" text="We couldn't detect an active season matching today's date." />
		</div>
	{:else if events.length === 0}
		<div style="text-align: center">
			<KaomojiError kind="neutral" text="No events scheduled for {formatSeasonName(activeSeason)} yet. Check back later!" />
		</div>
	{:else}
		<div>
			<h3 class="season">Active Season: {formatSeasonName(activeSeason)}</h3>

			<div class="event-cards">
				{#each events as event}
					<EventCard {event} />
				{/each}
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	.active-season-container {
		width: 100%;
	}

	.season {
		margin-bottom: 1.5rem;
		text-transform: uppercase;
		font-weight: 800;
		font-size: 1.5rem;
	}

	.event-cards {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
		gap: 1.5rem;
		margin-bottom: 3rem;
	}
</style>
