<script lang="ts">
	import { createSeason } from "$lib/api/seasons"
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
			const res = await createSeason(
				seasonSemester,
				seasonYear
			)
			if (res.ok) {
				seasonMessage = "Season created successfully!"
				seasonSemester = ""
				seasonYear = new Date().getFullYear()
				setTimeout(() => {
					showSeasonModal = false
					seasonMessage = ""
				}, 1500)
				refreshSeasons()
			} else {
				const errText = await res.text()
				seasonMessage = `Failed to create season: ${errText || "Unknown error"}`
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
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 {theme.darkMode
			? 'border-zinc-800'
			: 'border-gray-100'}"
	>
		<div>
			<h1
				class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode
					? 'text-zinc-100'
					: 'text-gray-800'}"
			>
				Season Management
			</h1>
			<p class="mt-1 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Create and monitor hackathon seasons for FPT SEAL.
			</p>
		</div>
		<div class="flex items-center gap-3 mt-4 md:mt-0">
			<button
				onclick={toggleSeasonModal}
				class="flex items-center gap-2 bg-[#f26f21] hover:bg-[#d85c14] text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all cursor-pointer border-0"
			>
				<Plus class="w-4 h-4" />
				New Season
			</button>
		</div>
	</header>

	<div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
		<!-- Left Column: Active Seasons Overview -->
		<div
			class="xl:col-span-2 p-8 rounded-3xl border transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<h2 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Active Seasons Overview
			</h2>

			<div class="overflow-x-auto font-sans">
				<table class="w-full text-left border-collapse">
					<thead>
						<tr
							class="border-b {theme.darkMode
								? 'border-zinc-800 text-zinc-400'
								: 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider"
						>
							<th class="py-3.5 px-4">Season Name</th>
							<th class="py-3.5 px-4">Time</th>
							<th class="py-3.5 px-4">Status</th>
						</tr>
					</thead>
					<tbody class="text-sm">
						{#if seasons.length > 0}
							{#each seasons as season}
								<tr
									onclick={() => goto(`/coordinator/seasons/${season.id}`)}
									class="border-b transition-colors cursor-pointer {theme.darkMode
										? 'border-zinc-800/50 hover:bg-zinc-800/30 text-zinc-100'
										: 'border-gray-50 hover:bg-gray-100/50 text-gray-700'}"
								>
									<td class="py-4 px-4 font-bold text-orange-500 hover:text-orange-600 transition-colors">
										{formatSemester(season.semester)} {season.year}
									</td>
									<td
										class="py-4 px-4 text-xs font-medium {theme.darkMode
											? 'text-zinc-400'
											: 'text-gray-500'}"
									>
										{season.year}
									</td>
									<td class="py-4 px-4">
										<span
											class="inline-flex px-2.5 py-1 rounded-full text-xs font-semibold {season.status === 'FINALIZED'
												? 'bg-blue-500/10 text-blue-500 border border-blue-500/20'
												: 'bg-green-500/10 text-green-500 border border-green-500/20'}"
										>
											{season.status || 'DRAFT'}
										</span>
									</td>
								</tr>
							{/each}
						{:else}
							<tr>
								<td colspan="4" class="py-8 px-4 text-center {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
									No active seasons found. Click "New Season" to create one.
								</td>
							</tr>
						{/if}
					</tbody>
				</table>
			</div>
		</div>

		<!-- Right Column: Past Seasons History -->
		<div
			class="xl:col-span-1 p-8 rounded-3xl border transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<h2 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Past Seasons History
			</h2>

			<div class="overflow-x-auto font-sans">
				<table class="w-full text-left border-collapse">
					<thead>
						<tr
							class="border-b {theme.darkMode
								? 'border-zinc-800 text-zinc-400'
								: 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider"
						>
							<th class="py-2.5 px-2">Season Name</th>
							<th class="py-2.5 px-2">Time</th>
							<th class="py-2.5 px-2">Status</th>
						</tr>
					</thead>
					<tbody class="text-xs">
						{#each [{ name: "Spring 2026", dur: "June 10, 2026", teams: "36" }, { name: "Spring 2026", dur: "June 12, 2026", teams: "27" }, { name: "Spring 2026", dur: "June 11, 2026", teams: "19" }, { name: "Olwe 2026", dur: "June 11, 2026", teams: "13" }, { name: "Olwe 2026", dur: "June 15, 2026", teams: "4" }] as row}
							<tr
								class="border-b transition-colors {theme.darkMode
									? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100'
									: 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}"
							>
								<td class="py-3 px-2 font-bold">{row.name}</td>
								<td
									class="py-3 px-2 text-[10px] {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}"
									>{row.dur}</td
								>
								<td class="py-3 px-2">
									<span
										class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-semibold bg-green-500/10 text-green-500 border border-green-500/20"
									>
										Completed
									</span>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>

<!-- Create Season Modal -->
{#if showSeasonModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
	>
		<div
			class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 text-zinc-100'
				: 'bg-white border-gray-100 text-gray-800'}"
		>
			<button
				onclick={toggleSeasonModal}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Calendar class="w-5 h-5 text-orange-500" />
				Create Season
			</h3>

			<form onsubmit={handleCreateSeason} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Semester *</label
					>
					<select
						bind:value={seasonSemester}
						required
						class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
							? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500'
							: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
					>
						<option value="" disabled selected>Select Semester</option>
						<option value="SPRING">SPRING</option>
						<option value="SUMMER">SUMMER</option>
						<option value="FALL">FALL</option>
					</select>
				</div>

				<div class="space-y-1">
					<label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}"
						>Year *</label
					>
					<input
						type="number"
						bind:value={seasonYear}
						required
						min="1990"
						max="9999"
						placeholder="2026"
						class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode
							? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500'
							: 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
					/>
				</div>

				{#if seasonMessage}
					<div
						class="p-3 rounded-lg text-sm font-medium {seasonMessage.includes('success')
							? theme.darkMode
								? 'bg-green-950/20 text-green-400'
								: 'bg-green-50 text-green-700'
							: theme.darkMode
								? 'bg-red-950/20 text-red-400'
								: 'bg-red-50 text-red-700'}"
					>
						{seasonMessage}
					</div>
				{/if}

				<div class="flex gap-3 mt-4">
					<button
						type="button"
						onclick={toggleSeasonModal}
						class="w-1/2 rounded-xl border py-3 font-semibold transition-all cursor-pointer {theme.darkMode
							? 'border-zinc-800 bg-transparent text-zinc-300 hover:bg-zinc-800/20'
							: 'border-gray-250 bg-transparent text-gray-700 hover:bg-gray-50'}"
					>
						Cancel
					</button>
					<button
						type="submit"
						disabled={isSeasonLoading}
						class="w-1/2 bg-orange-500 hover:bg-orange-600 text-white rounded-xl py-3 font-semibold disabled:opacity-50 transition-all shadow-sm cursor-pointer border-0"
					>
						{isSeasonLoading ? "Creating..." : "Create"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
