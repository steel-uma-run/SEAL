<script lang="ts">
    import { theme } from "$lib/theme.svelte";

    let teams = [
        { id: 1, name: "Team Beta", project: "Blockchain Ticketing System", status: "Mentoring", members: 5 },
        { id: 2, name: "Team Epsilon", project: "Smart Home IoT Platform", status: "Needs Review", members: 4 },
        { id: 3, name: "Team Zeta", project: "Healthcare Analytics Dashboard", status: "Completed", members: 6 }
    ];
</script>

<div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
    <div class="flex items-center gap-3 mb-6">
        <div class="p-2 bg-orange-100 rounded-lg text-orange-650 shrink-0">
            <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
        </div>
        <div>
            <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">My Mentoring Teams</h2>
            <p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mt-0.5">You can only view submissions and provide feedback to these teams.</p>
        </div>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {#each teams as team}
            <div class="group p-5 flex flex-col justify-between border rounded-xl transition-all duration-300 {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:bg-zinc-900/30' : 'border-orange-100 bg-white hover:bg-orange-50/50 hover:border-orange-200'}">
                <div>
                    <div class="flex items-start justify-between mb-3">
                        <h3 class="font-bold text-lg leading-tight {theme.darkMode ? 'text-orange-400' : 'text-orange-900'}">{team.name}</h3>
                        <span class="px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider whitespace-nowrap {team.status === 'Completed' ? (theme.darkMode ? 'bg-green-900/40 text-green-400' : 'bg-green-100 text-green-700') : team.status === 'Needs Review' ? (theme.darkMode ? 'bg-amber-900/40 text-amber-400' : 'bg-amber-100 text-amber-700') : (theme.darkMode ? 'bg-orange-900/40 text-orange-400' : 'bg-orange-100 text-orange-700')}">
                            {team.status}
                        </span>
                    </div>
                    <p class="text-sm font-medium mb-1 {theme.darkMode ? 'text-zinc-300' : 'text-orange-800/80'}">{team.project}</p>
                    <p class="text-xs mb-4 {theme.darkMode ? 'text-zinc-500' : 'text-orange-600/60'}">{team.members} members</p>
                </div>
                
                <div>
                    <div class="pt-4 border-t flex flex-wrap gap-2 {theme.darkMode ? 'border-zinc-800/80' : 'border-orange-50'}">
                        <button class="flex-1 py-1.5 px-3 text-xs font-semibold rounded-lg bg-orange-600 text-white hover:bg-orange-700 transition-colors shadow-sm cursor-pointer text-center">View</button>
                        <button class="flex-1 py-1.5 px-3 text-xs font-semibold rounded-lg transition-colors cursor-pointer text-center {theme.darkMode ? 'bg-zinc-800 text-zinc-300 hover:bg-zinc-700' : 'bg-orange-50 text-orange-700 hover:bg-orange-100'}">Feedback</button>
                    </div>
                    <p class="text-[10px] mt-3 text-center {theme.darkMode ? 'text-red-400/80' : 'text-red-500/80'}">* Cannot view detailed scores from judges.</p>
                </div>
            </div>
        {/each}
    </div>
</div>
