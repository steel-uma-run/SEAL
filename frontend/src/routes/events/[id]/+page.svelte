<script lang="ts">
	import { page } from "$app/state"
	import { getAllTracksOfEvent, getEvent, getRounds, markInterested } from "$lib/api"
	import { auth } from "$lib/auth.svelte"

	import ElevatedCard from "$lib/components/ElevatedCard.svelte"
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import Leaderboard from "./Leaderboard.svelte"
	import { Button, Card, Chip, Icon, pathAnimatableCircle } from "m3-svelte"

	import iconArrowBack from "@ktibow/iconset-material-symbols/arrow-back"
	import iconAdd from "@ktibow/iconset-material-symbols/add"
	import iconGavel from "@ktibow/iconset-material-symbols/gavel"

	const id = page.params.id
	function getRoundStartEnd(round: any) {
		const startIso = round.activeTime || round.active_time || round.startTime || round.start_time
		if (!startIso) return { start: null, end: null }
		const start = new Date(startIso)
		if (isNaN(start.getTime())) return { start: null, end: null }

		const submissionEndMs = start.getTime() + (round.activeDuration || round.active_duration || 0)
		const gradingStartIso = round.gradingStartTime || round.grading_start_time
		const gradingStartMs = gradingStartIso ? new Date(gradingStartIso).getTime() : submissionEndMs
		const endMs = gradingStartMs + (round.gradingDuration || round.grading_duration || 0)
		const end = new Date(endMs > start.getTime() ? endMs : submissionEndMs)

		return { start, end }
	}

	const data = $derived.by(async () => {
		const event = await getEvent({ path: { eventId: id! } })
		const tracks = await getAllTracksOfEvent({ path: { eventId: id! } })
		const rounds = await getRounds({ path: { eventId: id! } })

		const regStartIso = event.data?.registration_start_time || event.data?.start_time
		const startTime = regStartIso ? new Date(regStartIso) : undefined
		const regDuration = event.data?.registration_duration || 0
		const endTime = event.data?.end_time
			? new Date(event.data.end_time)
			: startTime
				? new Date(startTime.getTime() + regDuration)
				: undefined

		const now = Date.now()
		const openForRegistration =
			startTime && endTime ? now >= startTime.getTime() && now <= endTime.getTime() : false

		return {
			event: event.data,
			tracks: tracks.data,
			rounds: rounds.data,

			openForRegistration,
			startTime,
			endTime
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
									await markInterested({ path: { eventId: event.id } })
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

		<Leaderboard eventId={id} event={data.event} tracks={data.tracks} rounds={data.rounds} />

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
						{@const roundTimes = getRoundStartEnd(round)}
						{@const active =
							roundTimes.start && roundTimes.end
								? now >= roundTimes.start.getTime() && now <= roundTimes.end.getTime()
								: false}

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

							{#if roundTimes.start && roundTimes.end}
								<p style="color: var(--md-sys-color-tertiary)">
									{roundTimes.start.toLocaleString(undefined, {
										month: "short",
										day: "numeric",
										hour: "numeric",
										minute: "numeric"
									})} -
									{roundTimes.end.toLocaleString(undefined, {
										month: "short",
										day: "numeric",
										hour: "numeric",
										minute: "numeric"
									})}
								</p>
							{:else}
								<p style="color: var(--md-sys-color-tertiary)">Schedule TBD</p>
							{/if}
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
</style>
