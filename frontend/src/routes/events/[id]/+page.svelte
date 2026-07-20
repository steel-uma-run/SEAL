<script lang="ts">
	import { page } from "$app/state"
	import { getAllTracksOfEvent, getEvent, getRounds } from "$lib/api"

	import ElevatedCard from "$lib/components/ElevatedCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import Leaderboard from "./Leaderboard.svelte"
	import { Button, Card, Chip, Icon, pathAnimatableCircle } from "m3-svelte"

	import iconArrowBack from "@ktibow/iconset-material-symbols/arrow-back"
	import iconAdd from "@ktibow/iconset-material-symbols/add"
	import iconGavel from "@ktibow/iconset-material-symbols/gavel"

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
			<Button href="/" variant="text" iconType="left">
				<Icon icon={iconArrowBack} />
				Back to events</Button
			>
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

						<hr style="color: var(--md-sys-color-outline-variant)" />

						<div class="team-size">
							<p>Team size</p>
							<p>3 - 5 members</p>
						</div>

						<div class="max-teams">
							<p>Max teams</p>
							<p>30 teams</p>
						</div>

						<Button iconType="left" disabled={!event.open_for_registration}>
							<Icon icon={iconAdd} />
							Register now
						</Button>
					</div>
				</div>
			</ElevatedCard>
		</section>

		<Leaderboard />

		<section class="tracks">
			<div class="title">
				<h2>Tracks</h2>
				<Button variant="tonal">New track</Button>
			</div>

			{#if tracks.length <= 0}
				<KaomojiError kind="neutral" text="It's empty here..." />
			{:else}
				<div class="body">
					{#each tracks as track}
						<Card variant="elevated">
							<h3>{track.name}</h3>
							<p>{track.description}</p>

							<div class="judges">
								<p><Icon icon={iconGavel} /> Judges</p>

								<div>
									{#each track.judges as judge}
										<Chip variant="general" onclick={() => {}}>{judge.name}</Chip>
									{/each}
								</div>
							</div>

							<div class="mentors">
								<p><Icon icon={iconGavel} /> Mentors</p>

								<div>
									{#each track.mentors as mentor}
										<Chip variant="general" onclick={() => {}}>{mentor.name}</Chip>
									{/each}
								</div>
							</div>
						</Card>
					{/each}
				</div>
			{/if}
		</section>

		<section class="rounds">
			<div class="title">
				<h2>Rounds</h2>
				<Button variant="tonal">New round</Button>
			</div>

			{#if tracks.length <= 0}
				<KaomojiError kind="neutral" text="Kickstart the event with a few rounds!" />
			{:else}
				<div class="body">
					{#each data.rounds as round}
						{@const now = Date.now()}
						{@const active =
							now >= new Date(round.start_time).getTime() &&
							now <= new Date(round.end_time).getTime()}

						<div
							style="display: flex; flex-direction: column; align-items: center; justify-content: center"
						>
							<svg width="2rem" viewBox="0 0 350 350" xmlns="http://www.w3.org/2000/svg">
								<path
									d={pathAnimatableCircle}
									fill={active
										? "var(--md-sys-color-primary)"
										: "var(--md-sys-color-surface-container)"}
								/>
							</svg>

							<div
								style="width: 0.2rem; margin-top: 8px; height: 100%; background-color: {active
									? 'var(--md-sys-color-primary)'
									: 'var(--md-sys-color-surface-container)'}"
							></div>
						</div>

						<div style:opacity={active ? "" : "70%"}>
							<p style="font-weight: bold; font-size: 1.1rem">{round.name}</p>
							<p>{round.description}</p>

							<p style="color: var(--md-sys-color-tertiary)">
								{new Date(round.start_time).toLocaleString(undefined, {
									month: "short",
									day: "numeric",
									hour: "numeric",
									minute: "numeric"
								})} -
								{new Date(round.end_time).toLocaleString(undefined, {
									month: "short",
									day: "numeric",
									hour: "numeric",
									minute: "numeric"
								})}
							</p>
						</div>
					{/each}
				</div>
			{/if}
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

			.team-size {
				margin-top: 3rem;
				margin-bottom: 1rem;

				:nth-child(1) {
					font-weight: bold;
				}
			}

			.max-teams {
				margin-bottom: 1rem;

				:nth-child(1) {
					font-weight: bold;
				}
			}
		}
	}

	.title {
		margin-bottom: 1rem;
		display: flex;
		gap: 1rem;
		align-items: center;
	}

	.tracks {
		.body {
			display: flex;
			flex-direction: column;
			gap: 8px;
		}
	}

	.judges {
		margin-top: 1rem;

		:nth-child(1) {
			font-weight: bold;
			color: var(--md-sys-color-on-primary-container);
			margin-bottom: 0.3rem;
		}

		div {
			display: flex;
			gap: 0.2rem;
		}
	}

	.mentors {
		margin-top: 1rem;

		:nth-child(1) {
			font-weight: bold;
			color: var(--md-sys-color-on-tertiary-container);
			margin-bottom: 0.3rem;
		}

		div {
			display: flex;
			gap: 0.2rem;
		}
	}

	.rounds {
		.body {
			display: grid;
			grid-template-columns: 1fr 8fr;
			gap: 1rem;
		}
	}
</style>
