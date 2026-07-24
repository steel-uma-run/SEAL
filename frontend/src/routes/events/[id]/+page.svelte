<script lang="ts">
	import { page } from "$app/state"
	import { getAllTracksOfEvent, getEvent, getRounds, markInterested } from "$lib/api"
	import { auth } from "$lib/auth.svelte"
	import { CheckCircle2, AlertTriangle } from "@lucide/svelte"

	import ElevatedCard from "$lib/components/ElevatedCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import Leaderboard from "./Leaderboard.svelte"
	import { Button, Card, Chip, Icon, pathAnimatableCircle } from "m3-svelte"

	import iconArrowBack from "@ktibow/iconset-material-symbols/arrow-back"
	import iconAdd from "@ktibow/iconset-material-symbols/add"
	import iconGavel from "@ktibow/iconset-material-symbols/gavel"

	let successMessage = $state("")
	let errorMessage = $state("")

	const id = page.params.id
	const data = $derived.by(async () => {
		const event = await getEvent({ path: { eventId: id! } })
		const tracks = await getAllTracksOfEvent({ path: { eventId: id! } })
		const rounds = await getRounds({ path: { eventId: id! } })

		// the earliest time possible will be the start time (registration open time)
		const startTime = new Date(event.data.start_time)

		// the latest time possible will be the last round's ending time
		const endTime = rounds.data.at(-1) ? new Date(rounds.data.at(-1)!.end_time) : undefined

		return {
			event: event.data,
			tracks: tracks.data,
			rounds: rounds.data,

			openForRegistration:
				Date.now() >= Date.parse(event.data.start_time) &&
				Date.now() <= Date.parse(event.data.end_time),
			startTime: startTime,
			endTime: endTime
		}
	})
</script>

{#await data}
	<p>Loading...</p>
{:then data}
	{@const event = data.event}
	{@const tracks = data.tracks}

	<div class="container">
		{#if successMessage}
			<div class="toast toast--success">
				<CheckCircle2 class="icon icon-lg" />
				<div class="toast__body">
					<p class="toast__title">Success</p>
					<p class="toast__message">{successMessage}</p>
				</div>
			</div>
		{/if}

		{#if errorMessage}
			<div class="toast toast--error">
				<AlertTriangle class="icon icon-lg" />
				<div class="toast__body">
					<p class="toast__title toast__title--error">Alert</p>
					<p class="toast__message">{errorMessage}</p>
				</div>
			</div>
		{/if}

		<section class="back">
			<Button
				href={auth.value?.role === "COORDINATOR"
					? "/coordinator"
					: auth.value?.role === "LECTURER"
						? "/lecturer"
						: auth.value?.role === "STUDENT"
							? "/student"
							: "/"}
				variant="text"
				iconType="left"
			>
				<Icon icon={iconArrowBack} />
				Back to events</Button
			>
		</section>

		{#if data.openForRegistration}
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
								{data.startTime.toLocaleString(undefined, {
									month: "short",
									day: "numeric",
									hour: "numeric",
									minute: "numeric"
								})} -
								{data.endTime
									? data.endTime.toLocaleString(undefined, {
											month: "short",
											day: "numeric",
											hour: "numeric",
											minute: "numeric"
										})
									: " - "}
							</span>
						</div>

						<h1 class="name">{event.name}</h1>
						<p class="desc">{event.description}</p>

						<hr style="color: var(--md-sys-color-outline-variant)" />

						<div class="prize">
							{#each (event.price || "").split("\n") as line}
								<p>{line}</p>
							{/each}
						</div>

						<div class="team-size">
							<p>Team size</p>
							<p>3 - 5 members</p>
						</div>

						<div class="max-teams">
							<p>Max teams</p>
							<p>30 teams</p>
						</div>

						{#if auth.value === undefined || auth.value?.role === "STUDENT"}
							<Button
								iconType="left"
								disabled={!data.openForRegistration}
								onclick={async () => {
									try {
										const { response, error } = await markInterested({
											path: { eventId: event.id },
											throwOnError: false
										})
										if (response?.ok) {
											successMessage = "Successfully registered for the event!"
											errorMessage = ""
											setTimeout(() => {
												successMessage = ""
											}, 4000)
										} else {
											const errBody = error as any
											errorMessage = errBody?.detail || "Failed to register for the event."
											successMessage = ""
											setTimeout(() => {
												errorMessage = ""
											}, 4000)
										}
									} catch (err: any) {
										errorMessage = err.message || "An error occurred."
										successMessage = ""
										setTimeout(() => {
											errorMessage = ""
										}, 4000)
									}
								}}
							>
								<Icon icon={iconAdd} />
								Register now
							</Button>
						{/if}

						{#if data.event.status === "DRAFT" && auth.value?.role === "COORDINATOR"}
							<Button iconType="left">
								<Icon icon={iconAdd} />
								Open to the public
							</Button>
						{/if}
					</div>
				</div>
			</ElevatedCard>
		</section>

		<Leaderboard />

		<section class="tracks">
			<div class="title">
				<h2>Tracks</h2>

				{#if auth.value?.role == "COORDINATOR"}
					<Button variant="tonal">New track</Button>
				{/if}
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

				{#if auth.value?.role == "COORDINATOR"}
					<Button variant="tonal">New round</Button>
				{/if}
			</div>

			{#if tracks.length <= 0}
				<KaomojiError kind="neutral" text="It's empty here..." />
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

	.prize {
		margin-top: 1rem;
	}

	.toast {
		position: fixed;
		top: 1.5rem;
		right: 1.5rem;
		z-index: 5000;
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 1rem;
		max-width: 28rem;
		border-radius: 1rem;
		border: 1px solid;
		box-shadow:
			0 10px 15px -3px rgb(0 0 0 / 0.1),
			0 4px 6px -4px rgb(0 0 0 / 0.1);
		backdrop-filter: blur(12px);
		animation: fadeIn 0.2s ease-out;
		color: var(--md-sys-color-on-surface);
	}
	.toast--success {
		background-color: rgb(16 185 129 / 0.1);
		border-color: rgb(16 185 129 / 0.2);
		color: #10b981;
	}
	.toast--error {
		background-color: rgb(244 63 94 / 0.1);
		border-color: rgb(244 63 94 / 0.2);
		color: #f43f5e;
	}
	.toast__body {
		display: block;
		text-align: left;
	}
	.toast__title {
		font-size: 0.875rem;
		line-height: 1.25rem;
		font-weight: 700;
		color: #10b981;
		margin: 0;
	}
	.toast__title--error {
		color: #f43f5e;
	}
	.toast__message {
		margin-top: 0.125rem;
		font-size: 0.75rem;
		line-height: 1rem;
		margin-bottom: 0;
	}
	:global(.toast .icon-lg) {
		width: 1.5rem;
		height: 1.5rem;
		flex-shrink: 0;
	}

	@keyframes fadeIn {
		from {
			opacity: 0;
			transform: translateY(-4px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
</style>
