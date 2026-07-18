<script lang="ts">
	import { page } from "$app/state"
	import { getAllTracksOfEvent, getEvent, getRounds } from "$lib/api"

	import ElevatedCard from "$lib/components/ElevatedCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"

	import "@material/web/button/filled-button"
	import "@material/web/button/text-button"

	const id = page.params.id
	const data = $derived.by(async () => {
		const event = await getEvent({ path: { eventId: id! } })
		const tracks = await getAllTracksOfEvent({ path: { eventId: id! } })
		const rounds = await getRounds({ path: { eventId: id! } })

		return {
			event: event.data,
			tracks: tracks.data,
			rounds: rounds.data
		}
	})
</script>

{#await data}
	<p>Loading...</p>
{:then data}
	{@const event = data.event}
	{@const tracks = data.tracks}

	<div class="container">
		<section class="back">
			<md-text-button href="/">
				<span>Back to events</span>
				<div slot="icon" style="display:contents">
					<span class="material-symbols-outlined">arrow_back</span>
				</div>
			</md-text-button>
		</section>

		{#if event.open_for_registration}
			<section class="registration-notice">
				<h2>Open for registration!</h2>
			</section>
		{/if}

		<section>
			<ElevatedCard>
				<div class="summary-card">
					<div class="header">
						<img
							src="https://images.unsplash.com/photo-1631350397792-8e0c2de5b637?q=80&w=870&auto=format&fit=crop"
							alt="Shut up"
						/>
					</div>

					<div class="body">
						<div class="date-time">
							<span class="material-symbols-rounded">event_upcoming</span>
							<span>
								{new Date(event.start_time).toLocaleString(undefined, {
									month: "short",
									day: "numeric",
									hour: "numeric",
									minute: "numeric"
								})} -
								{new Date(event.end_time).toLocaleString(undefined, {
									month: "short",
									day: "numeric",
									hour: "numeric",
									minute: "numeric"
								})}
							</span>
						</div>

						<h1 class="name">{event.name}</h1>
						<p class="desc">{event.description}</p>

						<md-filled-button disabled={!event.open_for_registration}>
							<span>Register now</span>
							<div slot="icon" style="display: contents">
								<span class="material-symbols-rounded">add</span>
							</div>
						</md-filled-button>
					</div>
				</div>
			</ElevatedCard>
		</section>

		<section class="tracks">
			<h2>Tracks</h2>

			<div class="body">
				{#each tracks as track}
					<ElevatedCard>
						<div class="track">
							<h3>{track.name}</h3>
							<p>{track.description}</p>
						</div>

						<hr />

						<div class="track">
							<md-text-button>More details</md-text-button>
						</div>
					</ElevatedCard>
				{/each}
			</div>
		</section>

		<section class="rounds">
			<h2>Rounds</h2>

			<div class="body">
				{#each data.rounds as round}
					{@const now = Date.now()}
					{@const active =
						now >= new Date(round.start_time).getTime() &&
						now <= new Date(round.end_time).getTime()}

					<ElevatedCard>
						<div class="round-card {active ? '' : 'disabled'}">
							<div class="round">
								<h3>{round.name}</h3>
								<p>{round.description}</p>
							</div>

							<hr />

							<div class="round">
								<md-text-button disabled={!active}>More details</md-text-button>
							</div>
						</div>
					</ElevatedCard>
				{/each}
			</div>
		</section>
	</div>
{:catch}
	<KaomojiError kind="bad" text="An error occurred!" />
{/await}

<style lang="scss">
	* {
		margin: 0;
	}

	.container {
		max-width: 768px;
		min-height: 100dvh;
		margin: auto;
		padding: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 2rem;
	}

	.registration-notice {
		text-align: center;
		padding: 0.5rem;
		border-radius: 12px;

		background-color: var(--md-sys-color-primary-container);

		* {
			color: var(--md-sys-color-on-primary-container);
		}
	}

	.summary-card {
		.header {
			img {
				max-height: 200px;
				width: 100%;
				object-fit: cover;
			}
		}

		.body {
			padding: 16px;

			.date-time {
				display: flex;
				align-items: center;
				gap: 0.5rem;
				font-size: 0.8rem;
				opacity: 90%;
				margin-bottom: 0.5rem;
			}

			.name {
				font-weight: bold;
			}

			.desc {
				opacity: 70%;
				font-size: 0.9rem;
				margin-bottom: 1rem;
			}
		}
	}

	.tracks {
		> h2 {
			margin-bottom: 1rem;
		}

		hr {
			color: var(--md-sys-color-outline-variant);
			opacity: 50%;
		}

		.body {
			display: flex;
			flex-direction: column;
			gap: 8px;

			.track {
				padding: 16px;
			}
		}
	}

	.rounds {
		> h2 {
			margin-bottom: 1rem;
		}

		hr {
			color: var(--md-sys-color-outline-variant);
			opacity: 50%;
		}

		.body {
			display: flex;
			flex-direction: column;
			gap: 8px;

			.round-card {
				&.disabled {
					opacity: 50%;
				}

				.round {
					padding: 16px;
				}
			}
		}
	}
</style>
