<script lang="ts">
	import { getAllSeasons, getEventsInSeason } from "$lib/api/sdk.gen"

	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import EventCard from "$lib/components/EventCard.svelte"

	import "@material/web/button/filled-button"
	import "@material/web/button/outlined-button"
	import "@material/web/tabs/tabs"
	import "@material/web/tabs/primary-tab"

	const timeframe: "current" | "past" = $state("current")
	const data = $derived.by(async () => {
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
</script>

<div class="container">
	<section class="hero">
		<div>
			<h1>
				<span class="seal">SEAL</span> HACKATHON
			</h1>
			<p>
				The official event management and student support platform for FPT University. Register now
				to form your team, submit your project, and experience the platform!
			</p>

			<md-filled-button href="/auth/login">Join now</md-filled-button>
		</div>
	</section>

	<section class="events">
		<md-tabs>
			<md-primary-tab>Current events</md-primary-tab>
			<md-primary-tab>Past events</md-primary-tab>
		</md-tabs>

		{#await data}
			<p style="text-align: center">Loading...</p>
		{:then data}
			{#if data.length <= 0}
				<div style="text-align: center">
					<KaomojiError
						kind="neutral"
						text="Seems like no events are ongoing right now. Check back later!"
					/>
				</div>
			{:else}
				{#each data as season}
					<div class="season">
						<h3>{season.season.semester} {season.season.year}</h3>

						<div class="event-cards">
							{#each season.events as event}
								<EventCard {event} />
							{/each}
						</div>

						<hr />
					</div>
				{/each}
			{/if}
		{:catch}
			<div style="text-align: center">
				<KaomojiError kind="bad" text="An error occurred!" />
			</div>
		{/await}
	</section>
</div>

<style lang="scss">
	hr {
		color: var(--md-sys-color-outline-variant);
	}

	.container {
		padding: 0.5rem;
	}

	.seal {
		color: var(--md-sys-color-primary);
	}

	.hero {
		display: flex;
		align-items: center;
		text-align: center;
		min-height: 100vh;

		& div {
			margin: auto;
			max-width: 768px;

			& > h1 {
				font-size: 2.5rem;
				font-weight: bolder;
				margin-bottom: 1rem;
			}

			& > p {
				line-height: 1.5rem;
			}
		}
	}

	.events {
		margin: auto;
		max-width: 768px;
	}

	.event-cards {
		display: grid;
		gap: 8px;
		grid-template-columns: repeat(3, 1fr);
	}
</style>
