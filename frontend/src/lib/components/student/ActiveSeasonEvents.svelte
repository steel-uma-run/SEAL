<script lang="ts">
	import { fade } from "svelte/transition"
	import { theme } from "$lib/theme.svelte"
	import { formatSeasonName } from "$lib/utils/seasons"
	import { formatFullDate } from "$lib/utils/formatters.js"
	import type { HackathonEvent } from "$lib/api"

	let { activeSeason, events = [] } = $props<{
		activeSeason: any
		events: any[]
	}>()

	let currentIndex = $state(0)

	function nextEvent() {
		if (events.length > 0) {
			currentIndex = (currentIndex + 1) % events.length
		}
	}

	function prevEvent() {
		if (events.length > 0) {
			currentIndex = (currentIndex - 1 + events.length) % events.length
		}
	}

	// Reset index if events list changes
	$effect(() => {
		if (currentIndex >= events.length) {
			currentIndex = 0
		}
	})
</script>

<!-- Reusable Event Card Snippet -->
{#snippet eventCard(event: any)}
	<div
		class="border border-(--md-outline-variant) rounded-2xl p-8 flex flex-col justify-between bg-(--md-surface-container) transition-colors duration-300"
	>
		<div>
			<div class="flex justify-between items-start gap-4 mb-4">
				<div>
					<h3 class="font-extrabold text-xl text-(--md-on-surface)">
						{event.name}
					</h3>
					<!-- Tracks -->
					{#if event.tracks && event.tracks.length > 0}
						<div class="flex flex-wrap gap-1.5 mt-2">
							{#each event.tracks as track}
								<span
									class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-outline-variant)"
									title={track.description}
								>
									{track.name}
								</span>
							{/each}
						</div>
					{/if}
				</div>
			</div>
			<p class="text-sm leading-relaxed mb-6 text-(--md-on-surface-variant)">
				{event.description}
			</p>
		</div>

		<div
			class="pt-5 border-t border-(--md-outline-variant) flex flex-col sm:flex-row sm:items-center justify-between gap-4"
		>
			<div class="flex items-center gap-2 text-xs text-(--md-on-surface-variant)">
				<svg
					class="w-4 h-4 shrink-0 text-(--md-primary)"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
					></path>
				</svg>
				<span class="font-medium"
					>{formatFullDate(event.start_time || event.startTime)} - {formatFullDate(
						event.end_time || event.endTime
					)}</span
				>
			</div>

			<a
				href="/events/{event.id}"
				class="px-6 py-2.5 text-center rounded-xl text-xs font-bold transition-all bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 cursor-pointer"
			>
				View Details
			</a>
		</div>
	</div>
{/snippet}

<div
	class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) mb-8 transition-colors duration-300"
>
	<div class="flex justify-between items-center mb-8">
		<div>
			<h2 class="text-xl font-bold text-(--md-on-surface)">Active Season Events</h2>
			<p class="text-sm mt-1 text-(--md-on-surface-variant)">
				{#if activeSeason}
					Events scheduled for {formatSeasonName(activeSeason)}
				{:else}
					No active season found for the current date.
				{/if}
			</p>
		</div>

		<!-- Navigation Arrows -->
		{#if events.length > 2}
			<div class="flex items-center gap-2">
				<button
					onclick={prevEvent}
					class="p-2 rounded-xl transition-all cursor-pointer border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) hover:bg-(--md-surface-container-highest)"
					aria-label="Previous event"
				>
					<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 19l-7-7 7-7"
						></path>
					</svg>
				</button>
				<button
					onclick={nextEvent}
					class="p-2 rounded-xl transition-all cursor-pointer border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) hover:bg-(--md-surface-container-highest)"
					aria-label="Next event"
				>
					<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"
						></path>
					</svg>
				</button>
			</div>
		{/if}
	</div>

	{#if !activeSeason}
		<div class="text-center py-8">
			<p class="text-lg text-(--md-on-surface-variant)">
				We couldn't detect an active season matching today's date.
			</p>
		</div>
	{:else if events.length === 0}
		<div
			class="text-center py-10 border border-dashed rounded-2xl border-(--md-outline-variant) bg-(--md-surface-container)"
		>
			<svg
				class="w-12 h-12 mx-auto mb-3 text-(--md-on-surface-variant) opacity-60"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="1.5"
					d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
				></path>
			</svg>
			<p class="text-base font-medium text-(--md-on-surface)">
				No events scheduled for {formatSeasonName(activeSeason)} yet.
			</p>
			<p class="text-xs mt-1 text-(--md-on-surface-variant)">Check back later for updates!</p>
		</div>
	{:else}
		<div class="relative overflow-hidden min-h-[220px]">
			{#key currentIndex}
				<div in:fade={{ duration: 250 }} class="grid grid-cols-1 lg:grid-cols-2 gap-6">
					{@render eventCard(events[currentIndex])}

					{#if events.length > 1}
						{@render eventCard(events[(currentIndex + 1) % events.length])}
					{/if}
				</div>
			{/key}
		</div>

		<!-- Page Dots Indicators -->
		{#if events.length > 2}
			<div class="flex justify-center items-center gap-1.5 mt-6">
				{#each events as _, i}
					<button
						onclick={() => (currentIndex = i)}
						class="h-2 rounded-full transition-all duration-300 border-0 p-0 cursor-pointer {currentIndex ===
						i
							? 'w-6 bg-(--md-primary)'
							: 'w-2 bg-(--md-outline)'}"
						aria-label="Go to event {i + 1}"
					></button>
				{/each}
			</div>
		{/if}
	{/if}
</div>
