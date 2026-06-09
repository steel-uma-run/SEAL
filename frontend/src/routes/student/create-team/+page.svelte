<script lang="ts">
    import { createTeam } from "$lib/api/teams";
    import { theme } from "$lib/theme.svelte";

    let { data } = $props<{ data: { profile: any, seasons: any[] } }>();
    let profile = data?.profile || { id: "dummy" }; 
    let seasons = data?.seasons || [];

    let teamName = $state("");
    let teamDescription = $state("");
    let selectedSeasonId = $state("");
    let isLoading = $state(false);
    let message = $state("");
    let isError = $state(false);

    async function handleCreateTeam(e: Event) {
        e.preventDefault();
        isLoading = true;
        message = "";
        isError = false;

        try {
            const res = await createTeam(teamName, teamDescription, selectedSeasonId, profile.id);
            if (res.ok) {
                message = "Team created successfully!";
                teamName = "";
                teamDescription = "";
                selectedSeasonId = "";
            } else {
                isError = true;
                message = "Failed to create team. Please try again.";
            }
        } catch (err) {
            isError = true;
            message = "Error connecting to the server.";
        } finally {
            isLoading = false;
        }
    }
</script>

<svelte:head>
    <title>Create Team - SEAL</title>
</svelte:head>

<div class="max-w-2xl mx-auto w-full p-6 md:p-10">
    <a href="/dashboard" class="inline-flex items-center gap-2 transition-colors mb-8 font-medium
        {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-500 hover:text-orange-600'}">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        Back to Dashboard
    </a>

    <div class="p-8 md:p-10 rounded-3xl transition-all border
        {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
        
        <div class="flex items-center gap-4 mb-8 border-b pb-6 {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
            <div class="p-3 rounded-xl {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100 text-orange-600'}">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            </div>
            <div>
                <h1 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Create a Team</h1>
                <p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Form your squad for the upcoming hackathon.</p>
            </div>
        </div>
        
        <form onsubmit={handleCreateTeam} class="flex flex-col gap-6">
            <!-- Field 1: Season -->
            <div class="space-y-2">
                <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Season</label>
                <select bind:value={selectedSeasonId} required class="w-full rounded-xl p-3.5 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none border
                    {theme.darkMode ? 'bg-zinc-950 border-zinc-800 text-zinc-100' : 'bg-gray-50 border-gray-200 text-gray-900'}">
                    <option value="" disabled class="{theme.darkMode ? 'bg-zinc-950' : ''}">Select a season</option>
                    {#each seasons as season}
                        <option value={season.id} class="{theme.darkMode ? 'bg-zinc-950' : ''}">{season.name}</option>
                    {/each}
                </select>
            </div>
            <!-- Field 2: Team Name -->
            <div class="space-y-2">
                <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Team Name</label>
                <input type="text" bind:value={teamName} required placeholder="Enter team name" class="w-full rounded-xl p-3.5 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none border
                    {theme.darkMode ? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600' : 'bg-gray-50 border-gray-200 text-gray-900'}" />
            </div>
            <!-- Field 3: Description -->
            <div class="space-y-2">
                <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Description</label>
                <textarea bind:value={teamDescription} rows="4" placeholder="Describe your team's project idea" class="w-full rounded-xl p-3.5 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none resize-none border
                    {theme.darkMode ? 'bg-zinc-950 border-zinc-800 text-zinc-100 placeholder-zinc-600' : 'bg-gray-50 border-gray-200 text-gray-900'}"></textarea>
            </div>
            <!-- Message -->
            {#if message}
                <div class="p-4 rounded-xl text-sm font-medium border flex items-center gap-3
                    {isError 
                        ? (theme.darkMode ? 'bg-red-950/20 text-red-400 border-red-900/50' : 'bg-red-50 text-red-600 border border-red-100') 
                        : (theme.darkMode ? 'bg-green-950/20 text-green-400 border-green-900/50' : 'bg-green-50 text-green-600 border border-green-100')}"
                >
                    {#if !isError}
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                    {/if}
                    {message}
                </div>
            {/if}
            <!-- Submit button -->
            <button type="submit" disabled={isLoading} class="mt-4 w-full bg-[#ea580c] text-white rounded-xl py-4 font-bold text-lg shadow-sm hover:bg-[#c2410c] hover:shadow-md disabled:opacity-70 transition-all flex justify-center items-center gap-3">
                {#if isLoading}
                    <svg class="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                {/if}
                {isLoading ? 'Creating Team...' : 'Create Team'}
            </button>
        </form>
    </div>
</div>