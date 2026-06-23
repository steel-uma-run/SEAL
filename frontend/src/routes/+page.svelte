<script lang="ts">
	import { getAllSeasons, getEventsInSeason } from "$lib/api/sdk.gen"
	import { formatDate, formatFullDate } from "$lib/utils/formatters.js"

	import type { HackathonEvent } from "$lib/api"
	import * as kaomojis from "$lib/kaomojis"
	import FilledButton from "$lib/components/FilledButton.svelte"
	import ElevatedCard from "$lib/components/ElevatedCard.svelte"
	import TonalButton from "$lib/components/TonalButton.svelte"

	async function getAllEvents() {
		const seasons = await getAllSeasons()
		let toReturn = new Array<HackathonEvent>()

		for (const season of seasons.data) {
			const events = await getEventsInSeason({
				path: { seasonId: season.id }
			})

			toReturn = toReturn.concat(events.data)
		}

		return toReturn
	}
</script>

<div class="mx-2 py-2">
	<section class="flex items-center justify-center text-center min-h-screen">
		<div class="z-2 max-w-xl">
			<h1 class="text-6xl font-black mb-4 text-(--md-on-surface)">
				<span class="text-(--md-primary)">SEAL</span> HACKATHON
			</h1>
			<p class="text-xl text-(--md-on-surface) opacity-80 mb-8 leading-relaxed">
				The official event management and student support platform for FPT University. Register now
				to form your team, submit your project, and experience the platform!
			</p>

			<FilledButton size="md" text={"Join now"} href="/auth/login" iconName="arrow_forward" />
		</div>
	</section>

	<section class="m-auto max-w-3xl mb-10">
		<h1 class="text-3xl font-bold mb-10">Ongoing events</h1>

		<svelte:boundary>
			{@const events = await getAllEvents()}

			{#if events.length <= 0}
				<div class="text-center flex flex-col gap-4 text-(--md-on-surface-variant)">
					<p class="text-3xl">{kaomojis.random(kaomojis.neutral)}</p>
					<p class="text-xl">No events are ongoing right now, check back later!</p>
				</div>
			{:else}
				{@const event = events[0]}
				<ElevatedCard direction="left-right">
					{#snippet header()}
						<div class="min-h-full max-w-[40%]">
							<img
								class="object-cover w-full h-full rounded-tl-xl rounded-bl-xl"
								alt="Some idk"
								src="https://images.unsplash.com/photo-1631350397792-8e0c2de5b637?q=80&w=870&auto=format&fit=crop"
								height="870"
								width="580"
							/>
						</div>
					{/snippet}

					<div class="flex flex-col justify-between h-full">
						<div>
							<h2 class="text-2xl font-bold">{event.name}</h2>
							<p class="mb-8">{event.description}</p>
						</div>

						<div>
							<TonalButton
								href="/events/{event.id}"
								text="View details"
								size="sm"
								iconName="arrow_forward"
							/>
						</div>
					</div>
				</ElevatedCard>
			{/if}

			{#snippet pending()}
				<p>Loading...</p>
			{/snippet}

			{#snippet failed()}
				<div class="text-center flex flex-col gap-4 text-(--md-on-surface-variant)">
					<p class="text-3xl">{kaomojis.random(kaomojis.bad)}</p>
					<p class="text-xl">An error occured!</p>
				</div>
			{/snippet}
		</svelte:boundary>
	</section>
</div>
