<script lang="ts">
    import { onMount } from "svelte";
    import { getSeasons } from "$lib/api/seasons";

    let seasons: any[] = $state([]);
    let isLoading = $state(true);
    let errorMessage = $state("");

    onMount(async () => {
        try {
            const res = await getSeasons();
            if (res.ok) {
                seasons = await res.json();
            } else {
                errorMessage = "Failed to load seasons.";
            }
        } catch (err) {
            errorMessage = "Error connecting to server.";
        } finally {
            isLoading = false;
        }
    });
</script>

<div class="bg-white/90 backdrop-blur-md p-6 rounded-2xl shadow-sm border border-gray-100 sticky top-24">
    <div class="flex items-center gap-2 mb-6">
        <div class="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center">
            <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
        </div>
        <h2 class="text-xl font-bold text-gray-800">Available Seasons</h2>
    </div>

    {#if isLoading}
        <div class="space-y-4 animate-pulse">
            <div class="h-24 bg-gray-100 rounded-xl"></div>
            <div class="h-24 bg-gray-100 rounded-xl"></div>
        </div>
    {:else if errorMessage}
        <div class="p-4 bg-red-50 text-red-600 rounded-xl text-sm border border-red-100">{errorMessage}</div>
    {:else if seasons.length === 0}
        <div class="text-center py-8">
            <svg class="w-12 h-12 text-gray-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path></svg>
            <p class="text-gray-500 text-sm">No seasons available yet.</p>
        </div>
    {:else}
        <ul class="space-y-4 max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
            {#each seasons as season}
                <li class="group p-5 border border-gray-100 rounded-xl hover:border-orange-200 hover:shadow-md transition-all duration-300 bg-white relative overflow-hidden">
                    <div class="absolute top-0 left-0 w-1 h-full bg-orange-500 scale-y-0 group-hover:scale-y-100 transition-transform origin-top"></div>
                    <h3 class="font-semibold text-lg text-gray-800 group-hover:text-orange-600 transition-colors">{season.name}</h3>
                    <p class="text-gray-500 text-sm mt-2 leading-relaxed">{season.description || 'No description available.'}</p>
                </li>
            {/each}
        </ul>
    {/if}
</div>

<style>
    .custom-scrollbar::-webkit-scrollbar {
        width: 4px;
    }
    .custom-scrollbar::-webkit-scrollbar-track {
        background: #f1f1f1; 
        border-radius: 4px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb {
        background: #d1d5db; 
        border-radius: 4px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background: #f97316; 
    }
</style>
