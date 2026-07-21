<script lang="ts">
	import { getAllSeasons, getEventsInSeason } from "$lib/api/sdk.gen"

	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import EventCard from "$lib/components/EventCard.svelte"
	import { LoadingIndicator, Tabs } from "m3-svelte"

	interface Props {
		timeframe?: "current" | "past" | "upcoming"
	}

	let { timeframe = $bindable("current") }: Props = $props()

	const allSeasons = $derived.by(async () => {
		const seasons = await getAllSeasons()
		const events = await Promise.all(
			seasons.data.map(async (season) => {
				return {
					season: season,
					events: (await getEventsInSeason({ path: { seasonId: season.id } })).data
				}
			})
		)

		return events
	})

	const timeframeSeasons = $derived.by(async () => {
		const now = Date.now()

		switch (timeframe) {
			case "current": {
				return (await allSeasons).filter((seasonData) => {
					const filteredEvents = seasonData.events.filter((eventData) => {
						return (
							new Date(eventData.start_time).getMilliseconds() <= now &&
							new Date(eventData.end_time).getMilliseconds() >= now
						)
					})

					return filteredEvents.length > 0
				})
			}

			case "past": {
				return (await allSeasons).filter((seasonData) => {
					const filteredEvents = seasonData.events.filter((eventData) => {
						const now = Date.now()
						return (
							new Date(eventData.start_time).getMilliseconds() <= now &&
							new Date(eventData.end_time).getMilliseconds() <= now
						)
					})

					return filteredEvents.length > 0
				})
			}

			case "upcoming": {
				return (await allSeasons).filter((seasonData) => {
					const filteredEvents = seasonData.events.filter((eventData) => {
						const now = Date.now()
						return (
							new Date(eventData.start_time).getMilliseconds() >= now &&
							new Date(eventData.end_time).getMilliseconds() >= now
						)
					})

					return filteredEvents.length > 0
				})
			}
		}
	})
</script>

<Tabs
	bind:tab={timeframe}
	items={[
		{ name: "Current events", value: "current" },
		{ name: "Upcoming events", value: "upcoming" },
		{ name: "Past events", value: "past" }
	]}
/>

{#await timeframeSeasons}
	<LoadingIndicator aria-label="spinner" />
{:then data}
	{#if data.length <= 0}
		<div style="text-align: center">
			<KaomojiError kind="neutral" text="Seems like there are no events. Check back later!" />
		</div>
	{:else}
		{#each data as season}
			<div>
				<h3 class="season">{season.season.semester} {season.season.year}</h3>

				<div class="event-cards">
					{#each season.events as event}
						<EventCard {event} />
					{/each}
				</div>

				<hr />
			</div>
		{/each}
	{/if}
{/await}

<style lang="scss">
	.season {
		margin-bottom: 1rem;
	}

	.event-cards {
		display: grid;
		gap: 8px;
		grid-template-columns: repeat(3, 1fr);
	}

	hr {
		margin-top: 1rem;
		color: var(--md-sys-color-outline-variant);
	}
</style>
