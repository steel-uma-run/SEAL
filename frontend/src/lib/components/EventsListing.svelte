<script lang="ts">
	import { getAllSeasons, getEventsInSeason } from "$lib/api/sdk.gen"
	import { getCurrentSeasonInfo, formatSeasonName } from "$lib/utils/seasons"

	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import EventCard from "$lib/components/EventCard.svelte"
	import { LoadingIndicator, Tabs } from "m3-svelte"

	interface Props {
		timeframe?: "current" | "past" | "upcoming"
	}

	let { timeframe = $bindable("current") }: Props = $props()

	let selectedSeasonId = $state("ALL")

	$effect(() => {
		timeframe
		selectedSeasonId = "ALL"
	})

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

	const pastSeasonsList = $derived.by(async () => {
		const seasons = await allSeasons
		const now = Date.now()
		return seasons.filter((seasonData) => {
			return seasonData.events.some((eventData) => new Date(eventData.end_time).getTime() < now)
		})
	})

	const timeframeSeasons = $derived.by(async () => {
		const currentSelectedSeasonId = selectedSeasonId
		const now = Date.now()

		switch (timeframe) {
			case "current": {
				const currentSeason = getCurrentSeasonInfo()
				return (await allSeasons).filter((seasonData) => {
					return (
						seasonData.season.semester === currentSeason.semester &&
						seasonData.season.year === currentSeason.year
					)
				})
			}

			case "past": {
				const seasons = (await allSeasons)
					.map((seasonData) => {
						const filteredEvents = seasonData.events.filter((eventData) => {
							return new Date(eventData.end_time).getTime() < now
						})
						return {
							...seasonData,
							events: filteredEvents
						}
					})
					.filter((seasonData) => seasonData.events.length > 0)

				if (currentSelectedSeasonId !== "ALL") {
					return seasons.filter((s) => s.season.id === currentSelectedSeasonId)
				}
				return seasons
			}

			case "upcoming": {
				return (await allSeasons)
					.map((seasonData) => {
						const filteredEvents = seasonData.events.filter((eventData) => {
							return new Date(eventData.start_time).getTime() > now
						})
						return {
							...seasonData,
							events: filteredEvents
						}
					})
					.filter((seasonData) => seasonData.events.length > 0)
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

{#if timeframe === "past"}
	{#await pastSeasonsList}
		<!-- Loading seasons... -->
	{:then seasons}
		{#if seasons.length > 0}
			<div class="filter-container">
				<label for="season-select" class="filter-label">Filter by Season:</label>
				<select id="season-select" bind:value={selectedSeasonId} class="season-select-dropdown">
					<option value="ALL">All Seasons</option>
					{#each seasons as seasonData}
						<option value={seasonData.season.id}>
							{formatSeasonName(seasonData.season)}
						</option>
					{/each}
				</select>
			</div>
		{/if}
	{/await}
{/if}

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
		margin-bottom: 1.5rem;
		font-size: 1.5rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.5px;
	}

	.event-cards {
		display: grid;
		gap: 24px;
		grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
		align-items: stretch;
	}

	hr {
		margin: 2.5rem 0;
		border: none;
		border-top: 1px solid var(--md-sys-color-outline-variant);
	}

	.filter-container {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin: 1.5rem 0;
		padding: 0.75rem 1rem;
		background-color: var(--md-sys-color-surface-container-low);
		border-radius: 12px;
		border: 1px solid var(--md-sys-color-outline-variant);
		max-width: fit-content;
	}

	.filter-label {
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--md-sys-color-on-surface-variant);
	}

	.season-select-dropdown {
		padding: 0.5rem 2rem 0.5rem 1rem;
		font-size: 0.875rem;
		font-weight: 500;
		font-family: inherit;
		color: var(--md-sys-color-on-surface);
		background-color: var(--md-sys-color-surface-container);
		border: 1px solid var(--md-sys-color-outline-variant);
		border-radius: 8px;
		cursor: pointer;
		outline: none;
		transition: all 0.2s ease;
		appearance: none;
		background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
		background-repeat: no-repeat;
		background-position: right 0.75rem center;
		background-size: 1rem;

		&:hover {
			background-color: var(--md-sys-color-surface-container-high);
			border-color: var(--md-sys-color-primary);
		}

		&:focus {
			border-color: var(--md-sys-color-primary);
		}
	}
</style>
