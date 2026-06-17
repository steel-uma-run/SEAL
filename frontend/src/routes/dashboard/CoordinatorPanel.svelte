<script lang="ts">
    import { createSeason } from "$lib/api/seasons";
    import { theme } from "$lib/theme.svelte";
    import { Calendar, Users, UserCheck, Shield, Plus, X } from "@lucide/svelte";

    let { profile, seasons = [], refreshSeasons } = $props<{ profile: any, seasons: any[], refreshSeasons: () => void }>();

    // Season Creation Modal State
    let showSeasonModal = $state(false);
    let seasonName = $state("");
    let seasonDescription = $state("");
    let seasonStartTime = $state("");
    let seasonEndTime = $state("");
    let seasonMessage = $state("");
    let isSeasonLoading = $state(false);

    // Expert Creation Modal State
    let showExpertModal = $state(false);
    let expertName = $state("");
    let expertRole = $state("JUDGE");
    let expertSpecialization = $state("");
    let expertEmail = $state("");
    let expertMessage = $state("");
    let isExpertLoading = $state(false);

    // Toggle Modal Functions
    function toggleSeasonModal() {
        showSeasonModal = !showSeasonModal;
        seasonMessage = "";
    }

    function toggleExpertModal() {
        showExpertModal = !showExpertModal;
        expertMessage = "";
    }

    async function handleCreateSeason(e: Event) {
        e.preventDefault();

        if (!seasonStartTime || !seasonEndTime) {
            seasonMessage = "Start time and end time are required.";
            return;
        }

        const start = new Date(seasonStartTime);
        const end = new Date(seasonEndTime);

        if (start >= end) {
            seasonMessage = "Start time must be before end time.";
            return;
        }

        isSeasonLoading = true;
        seasonMessage = "";
        try {
            const res = await createSeason(
                seasonName,
                seasonDescription,
                start.toISOString(),
                end.toISOString()
            );
            if (res.ok) {
                seasonMessage = "Season created successfully!";
                seasonName = "";
                seasonDescription = "";
                seasonStartTime = "";
                seasonEndTime = "";
                setTimeout(() => {
                    showSeasonModal = false;
                    seasonMessage = "";
                }, 1500);
                refreshSeasons();
            } else {
                const errText = await res.text();
                seasonMessage = `Failed to create season: ${errText || 'Unknown error'}`;
            }
        } catch (err) {
            seasonMessage = "Error connecting to server.";
        } finally {
            isSeasonLoading = false;
        }
    }

    function handleCreateExpert(e: Event) {
        e.preventDefault();
        isExpertLoading = true;
        expertMessage = "";
        setTimeout(() => {
            expertMessage = "Expert account simulated successfully! (Backend API not implemented)";
            expertName = "";
            expertSpecialization = "";
            expertEmail = "";
            isExpertLoading = false;
            setTimeout(() => {
                showExpertModal = false;
                expertMessage = "";
            }, 2000);
        }, 1000);
    }
</script>

<!-- Metric Cards -->
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    <!-- Card 1: Total Seasons -->
    <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
            <Calendar class="w-6 h-6" />
        </div>
        <div>
            <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Seasons</p>
            <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">4</h3>
        </div>
    </div>

    <!-- Card 2: Total Teams -->
    <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
            <Users class="w-6 h-6" />
        </div>
        <div>
            <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Teams</p>
            <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">32</h3>
        </div>
    </div>

    <!-- Card 3: Active Participants -->
    <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
            <UserCheck class="w-6 h-6" />
        </div>
        <div>
            <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Active Participants</p>
            <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">156</h3>
        </div>
    </div>

    <!-- Card 4: Total Mentors/Judges -->
    <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-green-950/40 text-green-400' : 'bg-green-50 text-green-600'}">
            <Shield class="w-6 h-6" />
        </div>
        <div>
            <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Mentors/Judges</p>
            <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">28</h3>
        </div>
    </div>
</div>

<!-- Main 2-Column Dashboard Content -->
<div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
    <!-- Left Column (Active Seasons and Mentorship/Judge Panel) -->
    <div class="xl:col-span-2 space-y-8">
        <!-- Active Seasons Overview -->
        <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
            <div class="flex justify-between items-center mb-6">
                <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Active Seasons Overview</h2>
                <button onclick={toggleSeasonModal} class="flex items-center gap-2 bg-[#f26f21] hover:bg-[#d85c14] text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm transition-all cursor-pointer">
                    <Plus class="w-4 h-4" />
                    New Season
                </button>
            </div>
            
            <div class="overflow-x-auto font-sans">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-400' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                            <th class="py-3.5 px-4">Season Name</th>
                            <th class="py-3.5 px-4">Duration</th>
                            <th class="py-3.5 px-4">Teams</th>
                            <th class="py-3.5 px-4">Status</th>
                        </tr>
                    </thead>
                    <tbody class="text-sm">
                        {#if seasons.length > 0}
                            {#each seasons as season}
                                <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                                    <td class="py-4 px-4 font-bold">{season.name}</td>
                                    <td class="py-4 px-4 text-xs font-medium {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                        {season.start_time ? new Date(season.start_time).toLocaleDateString() : 'dd/mm/yyyy'} - {season.end_time ? new Date(season.end_time).toLocaleDateString() : 'dd/mm/yyyy'}
                                    </td>
                                    <td class="py-4 px-4 font-semibold">12</td>
                                    <td class="py-4 px-4">
                                        <span class="inline-flex px-2.5 py-1 rounded-full text-xs font-semibold bg-green-500/10 text-green-500 border border-green-500/20">
                                            Active
                                        </span>
                                    </td>
                                </tr>
                            {/each}
                        {:else}
                            <!-- Fallback Mock Data as shown in the image -->
                            {#each [1, 2, 3, 4] as item}
                                <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                                    <td class="py-4 px-4 font-bold">Spring 2026</td>
                                    <td class="py-4 px-4 text-xs font-medium {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">dd/mm/yyyy --:-- - --:--</td>
                                    <td class="py-4 px-4 font-semibold">{item === 1 ? '32' : item === 2 ? '11' : item === 3 ? '9' : '4'}</td>
                                    <td class="py-4 px-4">
                                        <span class="inline-flex px-2.5 py-1 rounded-full text-xs font-semibold bg-green-500/10 text-green-500 border border-green-500/20">
                                            Active
                                        </span>
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Mentorship & Judge Panel Management -->
        <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
            <div class="flex justify-between items-center mb-6">
                <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Mentorship & Judge Panel Management</h2>
                <button onclick={toggleExpertModal} class="flex items-center gap-2 bg-[#f26f21] hover:bg-[#d85c14] text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm transition-all cursor-pointer">
                    <Plus class="w-4 h-4" />
                    Add Expert
                </button>
            </div>

            <div class="overflow-x-auto font-sans">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-400' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                            <th class="py-3.5 px-4">Name</th>
                            <th class="py-3.5 px-4">Role</th>
                            <th class="py-3.5 px-4">Specialization</th>
                            <th class="py-3.5 px-4">Assigned Teams</th>
                            <th class="py-3.5 px-4">Actions</th>
                        </tr>
                    </thead>
                    <tbody class="text-sm">
                        <!-- Row 1 -->
                        <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                            <td class="py-4 px-4 font-bold">Name Name</td>
                            <td class="py-4 px-4 text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Expert</td>
                            <td class="py-4 px-4 text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Specialization</td>
                            <td class="py-4 px-4">
                                <span class="inline-flex gap-1.5">
                                    <span class="px-2 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-xs font-bold text-zinc-600 dark:text-zinc-400">1</span>
                                    <span class="px-2 py-0.5 rounded bg-zinc-200 dark:bg-zinc-700 text-xs font-bold text-zinc-800 dark:text-zinc-300">4</span>
                                </span>
                            </td>
                            <td class="py-4 px-4">
                                <div class="flex gap-2">
                                    <button class="px-3 py-1 rounded bg-orange-100 dark:bg-orange-950/40 text-orange-600 dark:text-orange-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">Edit</button>
                                    <button class="px-3 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">View Profile</button>
                                </div>
                            </td>
                        </tr>
                        <!-- Row 2 -->
                        <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                            <td class="py-4 px-4 font-bold">Name Name</td>
                            <td class="py-4 px-4 text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Coordinator</td>
                            <td class="py-4 px-4 text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Endransy</td>
                            <td class="py-4 px-4">
                                <span class="inline-flex gap-1.5">
                                    <span class="px-2 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-xs font-bold text-zinc-600 dark:text-zinc-400">2</span>
                                    <span class="px-2 py-0.5 rounded bg-zinc-200 dark:bg-zinc-700 text-xs font-bold text-zinc-800 dark:text-zinc-300">4</span>
                                </span>
                            </td>
                            <td class="py-4 px-4">
                                <div class="flex gap-2">
                                    <button class="px-3 py-1 rounded bg-orange-100 dark:bg-orange-950/40 text-orange-600 dark:text-orange-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">Edit</button>
                                    <button class="px-3 py-1 rounded bg-orange-100 dark:bg-orange-950/40 text-orange-600 dark:text-orange-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">Change Role</button>
                                    <button class="px-3 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">View Profile</button>
                                </div>
                            </td>
                        </tr>
                        <!-- Row 3 -->
                        <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                            <td class="py-4 px-4 font-bold">Name Name</td>
                            <td class="py-4 px-4 text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Expert</td>
                            <td class="py-4 px-4 text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Specialization</td>
                            <td class="py-4 px-4">
                                <span class="inline-flex gap-1.5">
                                    <span class="px-2 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-xs font-bold text-zinc-600 dark:text-zinc-400">1</span>
                                    <span class="px-2 py-0.5 rounded bg-zinc-200 dark:bg-zinc-700 text-xs font-bold text-zinc-800 dark:text-zinc-300">3</span>
                                </span>
                            </td>
                            <td class="py-4 px-4">
                                <div class="flex gap-2">
                                    <button class="px-3 py-1 rounded bg-orange-100 dark:bg-orange-950/40 text-orange-600 dark:text-orange-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">Edit</button>
                                    <button class="px-3 py-1 rounded bg-orange-100 dark:bg-orange-950/40 text-orange-600 dark:text-orange-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">Change Role</button>
                                    <button class="px-3 py-1 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 text-xs font-bold hover:brightness-90 transition-all cursor-pointer">View Profile</button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Right Column (Timeline and Past Seasons) -->
    <div class="xl:col-span-1 space-y-8">
        <!-- Hackathon Timeline -->
        <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
            <h2 class="text-xl font-bold mb-8 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Hackathon Timeline</h2>

            <div class="space-y-6 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent {theme.darkMode ? 'before:via-zinc-800' : 'before:via-gray-200'} before:to-transparent">
                <!-- Point 1 -->
                <div class="relative flex items-center gap-4">
                    <div class="w-10 h-10 rounded-full border-2 border-[#ea580c] text-[#ea580c] flex items-center justify-center shrink-0 z-10 shadow-sm {theme.darkMode ? 'bg-zinc-900' : 'bg-white'}">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Team Formation Deadline</h4>
                        <p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">June 10, 2026</p>
                    </div>
                </div>

                <!-- Point 2 -->
                <div class="relative flex items-center gap-4">
                    <div class="w-10 h-10 rounded-full border-2 border-[#ea580c] text-[#ea580c] flex items-center justify-center shrink-0 z-10 shadow-sm {theme.darkMode ? 'bg-zinc-900' : 'bg-white'}">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Project Submission</h4>
                        <p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">June 13, 2026 - 11:59 PM</p>
                    </div>
                </div>

                <!-- Point 3 -->
                <div class="relative flex items-center gap-4">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center shrink-0 z-10 {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-500' : 'bg-white border-gray-200 text-gray-400'}">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Pitching & Judging</h4>
                        <p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">June 15, 2026</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Past Seasons History -->
        <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
            <h2 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Past Seasons History</h2>
            
            <div class="overflow-x-auto font-sans">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-400' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                            <th class="py-2.5 px-2">Season Name</th>
                            <th class="py-2.5 px-2">Duration</th>
                            <th class="py-2.5 px-2">Teams</th>
                            <th class="py-2.5 px-2">Status</th>
                        </tr>
                    </thead>
                    <tbody class="text-xs">
                        {#each [
                            { name: 'Spring 2026', dur: 'June 10, 2026', teams: '36' },
                            { name: 'Spring 2026', dur: 'June 12, 2026', teams: '27' },
                            { name: 'Spring 2026', dur: 'June 11, 2026', teams: '19' },
                            { name: 'Olwe 2026', dur: 'June 11, 2026', teams: '13' },
                            { name: 'Olwe 2026', dur: 'June 15, 2026', teams: '4' }
                        ] as row}
                            <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                                <td class="py-3 px-2 font-bold">{row.name}</td>
                                <td class="py-3 px-2 text-[10px] {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">{row.dur}</td>
                                <td class="py-3 px-2 font-semibold">{row.teams}</td>
                                <td class="py-3 px-2">
                                    <span class="inline-flex px-2 py-0.5 rounded-full text-[10px] font-semibold bg-green-500/10 text-green-500 border border-green-500/20">
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

<!-- Modal Backdrops & Overlays -->
{#if showSeasonModal}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={toggleSeasonModal} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Create Season</h3>
            
            <form onsubmit={handleCreateSeason} class="flex flex-col gap-4">
                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Season Name</label>
                    <input type="text" bind:value={seasonName} required placeholder="Spring 2026" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Start Time</label>
                        <input type="datetime-local" bind:value={seasonStartTime} required class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                    </div>
                    <div class="space-y-1">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">End Time</label>
                        <input type="datetime-local" bind:value={seasonEndTime} required class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                    </div>
                </div>
                
                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Description</label>
                    <textarea bind:value={seasonDescription} rows="3" placeholder="Description..." class="w-full rounded-xl border p-3 outline-none transition-all resize-none {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"></textarea>
                </div>
                
                {#if seasonMessage}
                    <div class="p-3 rounded-lg text-sm font-medium {seasonMessage.includes('success') ? (theme.darkMode ? 'bg-green-950/20 text-green-400' : 'bg-green-50 text-green-700') : (theme.darkMode ? 'bg-red-950/20 text-red-400' : 'bg-red-50 text-red-700')}">
                        {seasonMessage}
                    </div>
                {/if}

                <button type="submit" disabled={isSeasonLoading} class="mt-4 bg-orange-500 hover:bg-orange-600 text-white rounded-xl py-3 font-semibold disabled:opacity-50 transition-all w-full shadow-sm cursor-pointer border-0">
                    {isSeasonLoading ? 'Creating...' : 'Create Season'}
                </button>
            </form>
        </div>
    </div>
{/if}

{#if showExpertModal}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={toggleExpertModal} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Add Expert Account</h3>
            
            <form onsubmit={handleCreateExpert} class="flex flex-col gap-4">
                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Full Name</label>
                    <input type="text" bind:value={expertName} required placeholder="Dr. John Doe" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-blue-500'}" />
                </div>

                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Email Address</label>
                    <input type="email" bind:value={expertEmail} required placeholder="johndoe@fpt.edu.vn" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-blue-500'}" />
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Role</label>
                        <select bind:value={expertRole} class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-blue-500'}">
                            <option value="JUDGE" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Judge</option>
                            <option value="MENTOR" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Mentor</option>
                        </select>
                    </div>
                    <div class="space-y-1">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Specialization</label>
                        <input type="text" bind:value={expertSpecialization} placeholder="Artificial Intelligence" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-blue-500'}" />
                    </div>
                </div>
                
                {#if expertMessage}
                    <div class="p-3 rounded-lg text-sm font-medium {expertMessage.includes('success') ? (theme.darkMode ? 'bg-green-950/20 text-green-400' : 'bg-green-50 text-green-700') : (theme.darkMode ? 'bg-red-950/20 text-red-400' : 'bg-red-50 text-red-700')}">
                        {expertMessage}
                    </div>
                {/if}

                <button type="submit" disabled={isExpertLoading} class="mt-4 bg-[#f26f21] hover:bg-[#d85c14] text-white rounded-xl py-3 font-semibold disabled:opacity-50 transition-all w-full shadow-sm cursor-pointer border-0">
                    {isExpertLoading ? 'Adding...' : 'Add Expert'}
                </button>
            </form>
        </div>
    </div>
{/if}
