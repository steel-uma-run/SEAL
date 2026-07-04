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
		class="border rounded-2xl p-8 flex flex-col justify-between transition-all {theme.darkMode
			? 'border-zinc-800 bg-zinc-950/60 shadow-[0_8px_30px_rgba(0,0,0,0.3)]'
			: 'border-gray-100 bg-white shadow-[0_8px_30px_rgba(0,0,0,0.02)]'}"
	>
		<div>
			<div class="flex justify-between items-start gap-4 mb-4">
				<div>
					<h3 class="font-extrabold text-xl {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
						{event.name}
					</h3>
					<!-- Tracks -->
					{#if event.tracks && event.tracks.length > 0}
						<div class="flex flex-wrap gap-1.5 mt-2">
							{#each event.tracks as track}
								<span
									class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider {theme.darkMode
										? 'bg-zinc-850 text-zinc-300 border border-zinc-700/50'
										: 'bg-zinc-100/80 text-zinc-700 border border-zinc-200/50'}"
									title={track.description}
								>
									{track.name}
								</span>
							{/each}
						</div>
					{/if}
				</div>
			</div>
			<p class="text-sm leading-relaxed mb-6 {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">
				{event.description}
			</p>
		</div>

		<div class="pt-5 border-t {theme.darkMode ? 'border-zinc-900/60' : 'border-gray-100'} flex flex-col sm:flex-row sm:items-center justify-between gap-4">
			<div class="flex items-center gap-2 text-xs {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
				<svg class="w-4 h-4 shrink-0 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
				</svg>
				<span class="font-medium">{formatFullDate(event.start_time || event.startTime)} - {formatFullDate(event.end_time || event.endTime)}</span>
			</div>

			<a
				href="/events/{event.id}"
				class="px-6 py-2.5 text-center rounded-xl text-xs font-bold transition-all border-0 shadow-sm hover:shadow cursor-pointer {theme.darkMode
					? 'bg-orange-950/40 text-orange-400 hover:bg-orange-900/40'
					: 'bg-[#ea580c] text-white hover:bg-[#d85c14]'}"
			>
				View Details
			</a>
		</div>
	</div>
{/snippet}

<div
	class="p-8 rounded-3xl border transition-all {theme.darkMode
		? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
		: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'} mb-8"
>
	<div class="flex justify-between items-center mb-8">
		<div>
			<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Active Season Events
			</h2>
			<p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
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
					class="p-2 rounded-xl transition-all cursor-pointer border {theme.darkMode
						? 'bg-zinc-950 border-zinc-800 text-zinc-355 hover:bg-zinc-800'
						: 'bg-gray-50 border-gray-100 text-gray-600 hover:bg-gray-100'}"
					aria-label="Previous event"
				>
					<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
					</svg>
				</button>
				<button
					onclick={nextEvent}
					class="p-2 rounded-xl transition-all cursor-pointer border {theme.darkMode
						? 'bg-zinc-950 border-zinc-800 text-zinc-355 hover:bg-zinc-800'
						: 'bg-gray-50 border-gray-100 text-gray-600 hover:bg-gray-100'}"
					aria-label="Next event"
				>
					<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
					</svg>
				</button>
			</div>
		{/if}
	</div>

	{#if !activeSeason}
		<div class="text-center py-8">
			<p class="text-lg {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				We couldn't detect an active season matching today's date.
			</p>
		</div>
	{:else if events.length === 0}
		<div class="text-center py-10 border border-dashed rounded-2xl {theme.darkMode ? 'border-zinc-850 bg-zinc-950/40' : 'border-gray-200 bg-gray-50/30'}">
			<svg class="w-12 h-12 mx-auto mb-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
			</svg>
			<p class="text-base font-medium {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">
				No events scheduled for {formatSeasonName(activeSeason)} yet.
			</p>
			<p class="text-xs mt-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-455'}">
				Check back later for updates!
			</p>
		</div>
	{:else}
		<div class="relative overflow-hidden min-h-[220px]">
			{#key currentIndex}
				<div
					in:fade={{ duration: 250 }}
					class="grid grid-cols-1 lg:grid-cols-2 gap-6"
				>
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
						onclick={() => currentIndex = i}
						class="h-2 rounded-full transition-all duration-300 border-0 p-0 cursor-pointer {currentIndex === i ? 'w-6 bg-[#ea580c]' : 'w-2 bg-gray-300 dark:bg-zinc-700'}"
						aria-label="Go to event {i + 1}"
					></button>
				{/each}
			</div>
		{/if}
	{/if}
</div>
