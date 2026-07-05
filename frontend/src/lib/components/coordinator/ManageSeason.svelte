<script lang="ts">
	import { createSeason } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { Calendar, Plus, X } from "@lucide/svelte"
	import { goto } from "$app/navigation"

	let { seasons = [], refreshSeasons } = $props<{ seasons: any[]; refreshSeasons: () => void }>()

	// Season Creation Modal State
	let showSeasonModal = $state(false)
	let seasonSemester = $state("")
	let seasonYear = $state(new Date().getFullYear())
	let seasonMessage = $state("")
	let isSeasonLoading = $state(false)

	function toggleSeasonModal() {
		showSeasonModal = !showSeasonModal
		seasonMessage = ""
	}

	function getEventCount(seasonId: string): number {
		if (typeof window !== "undefined") {
			const stored = localStorage.getItem(`events_${seasonId}`)
			if (stored) {
				try {
					const parsed = JSON.parse(stored)
					if (Array.isArray(parsed)) {
						return parsed.length
					}
				} catch (e) {
					console.error("Error parsing events from localStorage for season " + seasonId, e)
				}
			}
		}
		return 0
	}

	function formatSemester(semester: string) {
		if (!semester) return ""
		return semester.charAt(0).toUpperCase() + semester.slice(1).toLowerCase()
	}

	async function handleCreateSeason(e: Event) {
		e.preventDefault()

		if (!seasonSemester) {
			seasonMessage = "Semester is required."
			return
		}
		if (!seasonYear || seasonYear < 1990 || seasonYear > 9999) {
			seasonMessage = "Please enter a valid year."
			return
		}

		isSeasonLoading = true
		seasonMessage = ""
		try {
			const { response } = await createSeason({
				body: {
					semester: seasonSemester as "SPRING" | "SUMMER" | "FALL",
					year: seasonYear
				},
				throwOnError: false
			})
			if (response?.ok) {
				seasonMessage = "Season created successfully!"
				seasonSemester = ""
				seasonYear = new Date().getFullYear()
				setTimeout(() => {
					showSeasonModal = false
					seasonMessage = ""
				}, 1500)
				refreshSeasons()
			} else {
				seasonMessage = `Failed to create season: ${response?.statusText || "Unknown error"}`
			}
		} catch (err) {
			seasonMessage = "Error connecting to server."
		} finally {
			isSeasonLoading = false
		}
	}
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Season Management
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Create and monitor hackathon seasons for FPT SEAL.
			</p>
		</div>
		<div class="flex items-center gap-3 mt-4 md:mt-0">
			<button
				onclick={toggleSeasonModal}
				class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-5 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer border-0"
			>
				<Plus class="w-4 h-4" />
				New Season
			</button>
		</div>
	</header>

	<div class="space-y-8">
		<!-- Active Seasons Overview -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) transition-colors duration-300"
		>
			<h2 class="text-xl font-bold mb-6 text-(--md-on-surface)">Active Seasons Overview</h2>

			<div class="overflow-x-auto font-sans">
				<table class="w-full text-left border-collapse">
					<thead>
						<tr
							class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
						>
							<th class="py-3.5 px-4">Season Name</th>
							<th class="py-3.5 px-4">Time</th>
							<th class="py-3.5 px-4">Events</th>
						</tr>
					</thead>
					<tbody class="text-sm">
						{#if seasons.length > 0}
							{#each seasons as season}
								<tr
									onclick={() => goto(`/coordinator/seasons/${season.id}`)}
									class="border-b transition-colors cursor-pointer border-(--md-outline-variant) hover:bg-zinc-800/5 hover:dark:bg-zinc-100/5 text-(--md-on-surface)"
								>
									<td
										class="py-4 px-4 font-bold text-(--md-primary) hover:opacity-85 transition-colors"
									>
										{formatSemester(season.semester)}
										{season.year}
									</td>
									<td class="py-4 px-4 text-xs font-medium text-(--md-on-surface-variant)">
										{season.year}
									</td>
									<td class="py-4 px-4 text-xs font-bold text-(--md-on-surface)">
										{getEventCount(season.id)}
									</td>
								</tr>
							{/each}
						{:else}
							<tr>
								<td colspan="4" class="py-8 px-4 text-center text-(--md-on-surface-variant)">
									No active seasons found. Click "New Season" to create one.
								</td>
							</tr>
						{/if}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>

<!-- Create Season Modal -->
{#if showSeasonModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4"
	>
		<div
			class="w-full max-w-lg rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) p-8 relative transition-colors duration-300"
		>
			<button
				onclick={toggleSeasonModal}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-highest) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Calendar class="w-5 h-5 text-(--md-primary)" />
				Create Season
			</h3>

			<form onsubmit={handleCreateSeason} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label class="text-sm font-semibold text-(--md-on-surface-variant)">Semester *</label>
					<select
						bind:value={seasonSemester}
						required
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary) cursor-pointer"
					>
						<option value="" disabled selected>Select Semester</option>
						<option value="SPRING">SPRING</option>
						<option value="SUMMER">SUMMER</option>
						<option value="FALL">FALL</option>
					</select>
				</div>

				<div class="space-y-1">
					<label class="text-sm font-semibold text-(--md-on-surface-variant)">Year *</label>
					<input
						type="number"
						bind:value={seasonYear}
						required
						min="1990"
						max="9999"
						placeholder="2026"
						class="w-full rounded-xl border border-(--md-outline) p-3 outline-none transition-all bg-(--md-surface-container-highest) text-(--md-on-surface) focus:border-(--md-primary) focus:ring-1 focus:ring-(--md-primary)"
					/>
				</div>

				{#if seasonMessage}
					<div
						class="p-3 rounded-lg text-sm font-medium border {seasonMessage.includes('success')
							? 'bg-(--md-secondary-container) text-(--md-on-secondary-container) border-(--md-outline-variant)'
							: 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)'}"
					>
						{seasonMessage}
					</div>
				{/if}

				<div class="flex gap-3 mt-4">
					<button
						type="button"
						onclick={toggleSeasonModal}
						class="w-1/2 rounded-xl border border-(--md-outline) py-3 font-semibold transition-all cursor-pointer bg-transparent text-(--md-on-surface-variant) hover:bg-(--md-surface-container-highest)"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isSeasonLoading}
						class="w-1/2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) rounded-xl py-3 font-semibold disabled:opacity-50 transition-all cursor-pointer border-0"
					>
						{isSeasonLoading ? "Creating..." : "Create"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
