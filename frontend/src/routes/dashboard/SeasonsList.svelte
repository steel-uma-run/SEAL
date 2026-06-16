<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    let { seasons = [], errorMessage = "" } = $props<{ seasons?: any[], errorMessage?: string }>();
</script>

<div class="p-6 rounded-2xl shadow-sm border sticky top-24 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white/90 border-gray-100'}">
    <div class="flex items-center gap-2 mb-6">
        <div class="w-8 h-8 rounded-full flex items-center justify-center {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-100'}">
            <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
        </div>
        <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Available Seasons</h2>
    </div>

    {#if errorMessage}
        <div class="p-4 rounded-xl text-sm flex items-start gap-2 {theme.darkMode ? 'bg-red-950/20 text-red-400 border border-red-900/50' : 'bg-red-50 text-red-600 border border-red-100'}">
            <svg class="w-4 h-4 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span>{errorMessage}</span>
        </div>
    {:else if seasons.length === 0}
        <div class="text-center py-8">
            <svg class="w-12 h-12 text-gray-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path></svg>
            <p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">No seasons available yet.</p>
        </div>
    {:else}
        <ul class="space-y-4 max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
            {#each seasons as season}
                <li class="group p-5 border rounded-xl hover:border-orange-200 hover:shadow-md transition-all duration-300 relative overflow-hidden {theme.darkMode ? 'border-zinc-800 bg-zinc-950' : 'border-gray-100 bg-white'}">
                    <div class="absolute top-0 left-0 w-1 h-full bg-orange-500 scale-y-0 group-hover:scale-y-100 transition-transform origin-top"></div>
                    <h3 class="font-semibold text-lg transition-colors {theme.darkMode ? 'text-zinc-100 group-hover:text-orange-400' : 'text-gray-800 group-hover:text-orange-600'}">{season.name}</h3>
                    <p class="text-sm mt-2 leading-relaxed {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">{season.description || 'No description available.'}</p>
                    <div class="flex flex-col gap-1.5 mt-4 pt-3 text-xs font-medium border-t {theme.darkMode ? 'border-zinc-900 text-zinc-500' : 'border-gray-50 text-gray-400'}">
                        <div class="flex items-center gap-2">
                            <svg class="w-3.5 h-3.5 text-gray-400 group-hover:text-orange-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            <span>Start: <span class="font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">{season.start_time ? new Date(season.start_time).toLocaleString() : 'N/A'}</span></span>
                        </div>
                        <div class="flex items-center gap-2">
                            <svg class="w-3.5 h-3.5 text-gray-400 group-hover:text-orange-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            <span>End: <span class="font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">{season.end_time ? new Date(season.end_time).toLocaleString() : 'N/A'}</span></span>
                        </div>
                    </div>
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
