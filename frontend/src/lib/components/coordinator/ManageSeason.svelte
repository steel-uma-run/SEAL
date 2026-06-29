<script lang="ts">
    import { createSeason } from "$lib/api/seasons"
    import { theme } from "$lib/theme.svelte"
    import { Calendar, Plus, X } from "@lucide/svelte"
    import { goto } from "$app/navigation"

    let { seasons = [], refreshSeasons } = $props<{ seasons: any[]; refreshSeasons: () => void }>()

    const sortedSeasons = $derived(
        [...seasons].sort((a, b) => {
            if (b.year !== a.year) {
                return b.year - a.year
            }
            const semesterOrder: Record<string, number> = { FALL: 3, SUMMER: 2, SPRING: 1 }
            const aSem = semesterOrder[a.semester.toUpperCase()] || 0
            const bSem = semesterOrder[b.semester.toUpperCase()] || 0
            return bSem - aSem
        })
    )

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

	<!-- Active season -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <div
            class="xl:col-span-2 p-8 rounded-3xl border transition-all {theme.darkMode
                ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
                : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
        >
            <h2 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
                Active Seasons Overview
            </h2>

            <div class="overflow-x-auto font-sans rounded-2xl border {theme.darkMode ? 'border-zinc-800/60' : 'border-gray-100'}">
                <table class="w-full text-left border-collapse whitespace-nowrap">
                    <thead class="{theme.darkMode ? 'bg-zinc-800/40 text-zinc-400' : 'bg-gray-50/80 text-gray-400'} text-[11px] uppercase tracking-wider font-bold">
                        <tr>
                            <th class="py-4 px-6 rounded-tl-2xl">Season Name</th>
                            <th class="py-4 px-6">Year</th>
                            <th class="py-4 px-6 text-right rounded-tr-2xl">Action</th>
                        </tr>
                    </thead>
                    <tbody class="text-sm divide-y {theme.darkMode ? 'divide-zinc-800/60' : 'divide-gray-100'}">
                        {#if sortedSeasons.length > 0}
                            {#each sortedSeasons as season}
                                <tr
                                    onclick={() => goto(`/coordinator/seasons/${season.id}`)}
                                    class="group transition-colors cursor-pointer {theme.darkMode
                                        ? 'hover:bg-orange-500/10'
                                        : 'hover:bg-orange-50/60'}"
                                >
                                    <td class="py-4 px-6">
                                        <span class="font-bold text-orange-500 group-hover:text-orange-600 transition-colors">
                                            {formatSemester(season.semester)} {season.year}
                                        </span>
                                    </td>
                                    <td
                                        class="py-4 px-6 font-medium {theme.darkMode
                                            ? 'text-zinc-400'
                                            : 'text-gray-600'}"
                                    >
                                        {season.year}
                                    </td>
                                    <td class="py-4 px-6 text-right">
                                        <span class="inline-flex items-center justify-center p-2 rounded-lg transition-all {theme.darkMode ? 'text-zinc-500 group-hover:text-orange-400 group-hover:bg-orange-500/20' : 'text-gray-400 group-hover:text-orange-500 group-hover:bg-orange-100'}">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                                            </svg>
                                        </span>
                                    </td>
                                </tr>
                            {/each}
                        {:else}
                            <tr>
                                <td colspan="4" class="py-12 px-6 text-center">
                                    <div class="flex flex-col items-center justify-center">
                                        <p class="text-sm font-medium mb-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">No active seasons found</p>
                                        <p class="text-xs {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Click "New Season" to create your first one.</p>
                                    </div>
                                </td>
                            </tr>
                        {/if}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
	
	
</div>

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