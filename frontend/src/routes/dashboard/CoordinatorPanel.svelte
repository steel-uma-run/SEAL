<script lang="ts">
    import { createSeason } from "$lib/api/seasons";
    import { createTeam } from "$lib/api/teams";
    import { theme } from "$lib/theme.svelte";

    let { profile, seasons, refreshSeasons } = $props<{ profile: any, seasons: any[], refreshSeasons: () => void }>();

    // Season Creation
    let seasonName = $state("");
    let seasonDescription = $state("");
    let seasonStartTime = $state("");
    let seasonEndTime = $state("");
    let seasonMessage = $state("");
    let isSeasonLoading = $state(false);

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

    // Team Creation
    let teamName = $state("");
    let teamDescription = $state("");
    let selectedSeasonId = $state("");
    let teamMessage = $state("");
    let isTeamLoading = $state(false);

    async function handleCreateTeam(e: Event) {
        e.preventDefault();
        isTeamLoading = true;
        teamMessage = "";
        try {
            const res = await createTeam(teamName, teamDescription, selectedSeasonId, profile.id);
            if (res.ok) {
                teamMessage = "Team created successfully!";
                teamName = "";
                teamDescription = "";
                selectedSeasonId = "";
            } else {
                teamMessage = "Failed to create team.";
            }
        } catch (err) {
            teamMessage = "Error connecting to server.";
        } finally {
            isTeamLoading = false;
        }
    }

    // Dummy forms for Missing APIs
    let dummyMessage = $state("");
    function handleDummy(e: Event) {
        e.preventDefault();
        dummyMessage = "Action simulated! (API not implemented in backend)";
    }
</script>

<div class="flex flex-col gap-8">
    <!-- Functional Coordinator Tools -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Create Season -->
        <div class="p-8 rounded-2xl shadow-sm border transition-shadow relative overflow-hidden {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white/80 border-gray-100 hover:shadow-md'}">
            <div class="absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b from-orange-400 to-orange-600"></div>
            <div class="flex items-center gap-3 mb-6">
                <div class="p-2 rounded-lg {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                </div>
                <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Create Season</h2>
            </div>
            
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
                    <textarea bind:value={seasonDescription} rows="2" placeholder="Description..." class="w-full rounded-xl border p-3 outline-none transition-all resize-none {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"></textarea>
                </div>
                
                {#if seasonMessage}
                    <div class="p-3 rounded-lg text-sm font-medium {seasonMessage.includes('success') ? (theme.darkMode ? 'bg-green-950/20 text-green-400' : 'bg-green-50 text-green-700') : (theme.darkMode ? 'bg-red-950/20 text-red-400' : 'bg-red-50 text-red-700')}">
                        {seasonMessage}
                    </div>
                {/if}

                <button type="submit" disabled={isSeasonLoading} class="mt-2 bg-orange-500 text-white rounded-xl py-3 font-semibold hover:bg-orange-600 disabled:opacity-50 transition-all w-full shadow-sm">
                    {isSeasonLoading ? 'Creating...' : 'Create Season'}
                </button>
            </form>
        </div>

        <!-- Create Team -->
        <div class="p-8 rounded-2xl shadow-sm border transition-shadow relative overflow-hidden {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white/80 border-gray-100 hover:shadow-md'}">
            <div class="absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b from-orange-400 to-orange-600"></div>
            <div class="flex items-center gap-3 mb-6">
                <div class="p-2 rounded-lg {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                </div>
                <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Create Team</h2>
            </div>
            <form onsubmit={handleCreateTeam} class="flex flex-col gap-4">
                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Season</label>
                    <select bind:value={selectedSeasonId} required class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                        <option value="" disabled class="{theme.darkMode ? 'bg-zinc-950' : ''}">Select a season</option>
                        {#each seasons as season}
                            <option value={season.id} class="{theme.darkMode ? 'bg-zinc-950' : ''}">{season.name}</option>
                        {/each}
                    </select>
                </div>

                <div class="space-y-1">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Team Name</label>
                    <input type="text" bind:value={teamName} required placeholder="Team Name" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                </div>
                
                {#if teamMessage}
                    <div class="p-3 rounded-lg text-sm font-medium {teamMessage.includes('success') ? (theme.darkMode ? 'bg-green-950/20 text-green-400' : 'bg-green-50 text-green-700') : (theme.darkMode ? 'bg-red-950/20 text-red-400' : 'bg-red-50 text-red-700')}">
                        {teamMessage}
                    </div>
                {/if}

                <button type="submit" disabled={isTeamLoading} class="mt-2 bg-orange-500 text-white rounded-xl py-3 font-semibold hover:bg-orange-600 disabled:opacity-50 transition-all w-full shadow-sm">
                    {isTeamLoading ? 'Creating...' : 'Create Team'}
                </button>
            </form>
        </div>
    </div>

    <!-- UI Mockups for Missing Backend Features -->
    <div class="p-8 rounded-2xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white/80 border-gray-100'}">
        <div class="flex items-center gap-3 mb-6">
            <div class="p-2 rounded-lg {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-100 text-blue-600'}">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
            </div>
            <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Advanced Management <span class="text-sm font-normal text-blue-600 bg-blue-50 px-2 py-1 rounded ml-2 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : ''}">UI Demo</span></h2>
        </div>
        
        {#if dummyMessage}
            <div class="mb-8 p-4 rounded-xl border font-medium flex items-center gap-3 {theme.darkMode ? 'bg-blue-950/20 text-blue-400 border-blue-900/50' : 'bg-blue-50 text-blue-700 border border-blue-100'}">
                <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                {dummyMessage}
            </div>
        {/if}

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <!-- Assign Members -->
            <div class="p-6 border rounded-xl transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:bg-zinc-900' : 'border-gray-100 bg-gray-50 hover:bg-white hover:shadow-md'}">
                <h3 class="font-bold mb-4 flex items-center gap-2 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}"><svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>Assign Members</h3>
                <form onsubmit={handleDummy} class="flex flex-col gap-3">
                    <input type="text" placeholder="Team ID/Name" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <input type="email" placeholder="Student Email" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <button class="py-2.5 rounded-lg text-sm font-semibold transition-colors {theme.darkMode ? 'bg-orange-600 text-white hover:bg-orange-500' : 'bg-gray-800 text-white hover:bg-gray-700 mt-2'}">Assign Member</button>
                </form>
            </div>

            <!-- Assign Judges/Mentors -->
            <div class="p-6 border rounded-xl transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:bg-zinc-900' : 'border-gray-100 bg-gray-50 hover:bg-white hover:shadow-md'}">
                <h3 class="font-bold mb-4 flex items-center gap-2 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}"><svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>Assign Judge/Mentor</h3>
                <form onsubmit={handleDummy} class="flex flex-col gap-3">
                    <input type="text" placeholder="Team ID/Name" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <select class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}" required>
                        <option value="" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Select Role</option>
                        <option value="judge" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Judge</option>
                        <option value="mentor" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Mentor</option>
                    </select>
                    <input type="email" placeholder="Staff Email" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <button class="py-2.5 rounded-lg text-sm font-semibold transition-colors {theme.darkMode ? 'bg-orange-600 text-white hover:bg-orange-500' : 'bg-gray-800 text-white hover:bg-gray-700 mt-2'}">Assign</button>
                </form>
            </div>

            <!-- Create Judge -->
            <div class="p-6 border rounded-xl transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:bg-zinc-900' : 'border-gray-100 bg-gray-50 hover:bg-white hover:shadow-md'}">
                <h3 class="font-bold mb-4 flex items-center gap-2 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}"><svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>Create Judge Account</h3>
                <form onsubmit={handleDummy} class="flex flex-col gap-3">
                    <input type="email" placeholder="Judge Email" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <input type="text" placeholder="Judge Name" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <button class="py-2.5 rounded-lg text-sm font-semibold transition-colors {theme.darkMode ? 'bg-orange-600 text-white hover:bg-orange-500' : 'bg-gray-800 text-white hover:bg-gray-700 mt-2'}">Create Account</button>
                </form>
            </div>

            <!-- Ban Member -->
            <div class="p-6 border rounded-xl transition-all {theme.darkMode ? 'border-red-950 bg-red-950/20 hover:bg-red-900/30' : 'border-red-100 bg-red-50/50 hover:bg-white hover:shadow-md'}">
                <h3 class="font-bold mb-4 flex items-center gap-2 {theme.darkMode ? 'text-red-400' : 'text-red-700'}"><svg class="w-5 h-5 {theme.darkMode ? 'text-red-400' : 'text-red-500'}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>Ban Member</h3>
                <form onsubmit={handleDummy} class="flex flex-col gap-3">
                    <input type="email" placeholder="User Email" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-red-900 bg-zinc-900 text-zinc-100 placeholder-red-900/50 focus:ring-2 focus:ring-red-500' : 'border-red-200 bg-white focus:ring-2 focus:ring-red-500'}"/>
                    <input type="text" placeholder="Reason" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-red-900 bg-zinc-900 text-zinc-100 placeholder-red-900/50 focus:ring-2 focus:ring-red-500' : 'border-red-200 bg-white focus:ring-2 focus:ring-red-500'}"/>
                    <button class="bg-red-600 text-white py-2.5 rounded-lg text-sm font-semibold hover:bg-red-700 mt-2 transition-colors">Ban User</button>
                </form>
            </div>

            <!-- Assign Team (Track) -->
            <div class="p-6 border rounded-xl transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:bg-zinc-900' : 'border-gray-100 bg-gray-50 hover:bg-white hover:shadow-md'}">
                <h3 class="font-bold mb-4 flex items-center gap-2 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}"><svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>Assign Track</h3>
                <form onsubmit={handleDummy} class="flex flex-col gap-3">
                    <input type="text" placeholder="Team ID/Name" required class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}"/>
                    <select class="border p-2.5 rounded-lg text-sm outline-none transition-colors {theme.darkMode ? 'border-zinc-800 bg-zinc-900 text-zinc-100 focus:ring-2 focus:ring-blue-500' : 'border-gray-200 bg-white focus:ring-2 focus:ring-blue-500'}" required>
                        <option value="" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Select Track</option>
                        <option value="web" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Web Development</option>
                        <option value="mobile" class="{theme.darkMode ? 'bg-zinc-900' : ''}">Mobile App</option>
                        <option value="ai" class="{theme.darkMode ? 'bg-zinc-900' : ''}">AI / ML</option>
                    </select>
                    <button class="py-2.5 rounded-lg text-sm font-semibold transition-colors {theme.darkMode ? 'bg-orange-600 text-white hover:bg-orange-500' : 'bg-gray-800 text-white hover:bg-gray-700 mt-2'}">Assign Track</button>
                </form>
            </div>
        </div>
    </div>
</div>
