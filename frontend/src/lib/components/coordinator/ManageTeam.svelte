<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    import { 
        Users, 
        UserCheck, 
        Search, 
        Filter, 
        X, 
        Mail, 
        Eye, 
        CheckCircle2, 
        AlertTriangle, 
        Plus
    } from "@lucide/svelte";

    // Mock Teams Data
    let teams = $state([
        { id: "T-001", name: "Team Alpha", season: "AI for Good 2026", track: "AI & Machine Learning", leader: "john.doe@fptu.edu.vn", leaderName: "John Doe", membersCount: 4, maxMembers: 5, status: "APPROVED" },
        { id: "T-002", name: "Cyber Knights", season: "AI for Good 2026", track: "Cybersecurity", leader: "alice.smith@fptu.edu.vn", leaderName: "Alice Smith", membersCount: 5, maxMembers: 5, status: "APPROVED" },
        { id: "T-003", name: "Eco Warriors", season: "Green Tech Revolution", track: "Green Technology", leader: "bob.johnson@fptu.edu.vn", leaderName: "Bob Johnson", membersCount: 3, maxMembers: 5, status: "PENDING" },
        { id: "T-004", name: "FinTech Disruptors", season: "FinTech Disruptors", track: "Blockchain & DeFi", leader: "jane.davis@fptu.edu.vn", leaderName: "Jane Davis", membersCount: 2, maxMembers: 5, status: "PENDING" },
        { id: "T-005", name: "Health Tech", season: "AI for Good 2026", track: "Healthcare AI", leader: "david.wilson@fptu.edu.vn", leaderName: "David Wilson", membersCount: 4, maxMembers: 5, status: "REJECTED" }
    ]);

    // Filters State
    let searchQuery = $state("");
    let statusFilter = $state("ALL");
    let seasonFilter = $state("ALL");

    // Modal Control State
    let showViewModal = $state(false);
    let selectedTeam = $state<any>(null);

    // Derived filtered teams list
    let filteredTeams = $derived(
        teams.filter(t => {
            const matchesSearch = t.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                  t.leader.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                  t.id.includes(searchQuery);
            const matchesStatus = statusFilter === "ALL" || t.status === statusFilter;
            const matchesSeason = seasonFilter === "ALL" || t.season.includes(seasonFilter);
            return matchesSearch && matchesStatus && matchesSeason;
        })
    );

    // Statistics
    let totalTeams = $derived(teams.length);
    let approvedTeams = $derived(teams.filter(t => t.status === "APPROVED").length);
    let pendingTeams = $derived(teams.filter(t => t.status === "PENDING").length);
    let rejectedTeams = $derived(teams.filter(t => t.status === "REJECTED").length);

    let successMessage = $state("");
    let errorMessage = $state("");

    function showNotification(msg: string, type: "success" | "error" = "success") {
        if (type === "success") {
            successMessage = msg;
            setTimeout(() => successMessage = "", 3000);
        } else {
            errorMessage = msg;
            setTimeout(() => errorMessage = "", 3000);
        }
    }

    function approveTeam(teamId: string) {
        teams = teams.map(t => t.id === teamId ? { ...t, status: "APPROVED" } : t);
        showNotification("Team approved successfully!");
    }

    function rejectTeam(teamId: string) {
        teams = teams.map(t => t.id === teamId ? { ...t, status: "REJECTED" } : t);
        showNotification("Team request rejected.", "error");
    }

    function openViewModal(team: any) {
        selectedTeam = team;
        showViewModal = true;
    }
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
    <!-- Success & Error Banners -->
    {#if successMessage}
        <div class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-emerald-50 border-l-4 border-emerald-500 p-4 rounded shadow-lg animate-fade-in max-w-md">
            <CheckCircle2 class="w-5 h-5 text-emerald-500" />
            <div>
                <p class="text-sm font-semibold text-emerald-800">Success</p>
                <p class="text-xs text-emerald-700 mt-0.5">{successMessage}</p>
            </div>
        </div>
    {/if}

    {#if errorMessage}
        <div class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-rose-50 border-l-4 border-rose-500 p-4 rounded shadow-lg animate-fade-in max-w-md">
            <AlertTriangle class="w-5 h-5 text-rose-500" />
            <div>
                <p class="text-sm font-semibold text-rose-800">Alert</p>
                <p class="text-xs text-rose-700 mt-0.5">{errorMessage}</p>
            </div>
        </div>
    {/if}

    <!-- Header Section -->
    <header class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-6 border-b {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
        <div>
            <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
                Team Management
            </h1>
            <p class="mt-1 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                Review, approve, and track student teams participating in the hackathons.
            </p>
        </div>
    </header>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-[#fff2e8] text-[#ea580c]'}">
                <Users class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Teams</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{totalTeams}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-green-950/40 text-green-400' : 'bg-green-50 text-green-600'}">
                <UserCheck class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Approved</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{approvedTeams}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
                <Users class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Pending</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{pendingTeams}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-red-950/40 text-red-400' : 'bg-red-50 text-red-650'}">
                <Users class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Rejected</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{rejectedTeams}</h3>
            </div>
        </div>
    </div>

    <!-- Filters and Table Container -->
    <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
        
        <!-- Filters Toolbar -->
        <div class="flex flex-col md:flex-row gap-4 justify-between items-center mb-6">
            <!-- Search bar -->
            <div class="relative w-full md:max-w-md">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
                    <Search class="w-4 h-4" />
                </span>
                <input 
                    type="text" 
                    bind:value={searchQuery}
                    placeholder="Search by ID, team name, leader..." 
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl border outline-none transition-all text-sm {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
                />
                {#if searchQuery}
                    <button type="button" onclick={() => searchQuery = ""} class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-md text-xs font-bold text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 cursor-pointer border-0 bg-transparent">
                        Clear
                    </button>
                {/if}
            </div>

            <!-- selectors -->
            <div class="flex flex-wrap items-center gap-3 w-full md:w-auto">
                <div class="flex items-center gap-2 text-sm text-gray-500">
                    <Filter class="w-4 h-4" />
                    Filters:
                </div>
                <!-- Status Filter -->
                <select bind:value={statusFilter} class="rounded-xl border px-3 py-2 text-sm outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                    <option value="ALL">All Statuses</option>
                    <option value="APPROVED">Approved</option>
                    <option value="PENDING">Pending</option>
                    <option value="REJECTED">Rejected</option>
                </select>

                <!-- Season Filter -->
                <select bind:value={seasonFilter} class="rounded-xl border px-3 py-2 text-sm outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                    <option value="ALL">All Seasons</option>
                    <option value="Good 2026">AI for Good 2026</option>
                    <option value="Green Tech">Green Tech</option>
                    <option value="FinTech">FinTech</option>
                </select>
            </div>
        </div>

        <!-- Table -->
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-400' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                        <th class="py-4 px-4">Team ID</th>
                        <th class="py-4 px-4">Team Name</th>
                        <th class="py-4 px-4">Season & Track</th>
                        <th class="py-4 px-4">Leader</th>
                        <th class="py-4 px-4">Members</th>
                        <th class="py-4 px-4">Status</th>
                        <th class="py-4 px-4 text-center">Actions</th>
                    </tr>
                </thead>
                <tbody class="text-sm">
                    {#if filteredTeams.length > 0}
                        {#each filteredTeams as team}
                            <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                                <td class="py-4 px-4 font-mono text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                    #{team.id}
                                </td>
                                <td class="py-4 px-4 font-bold">
                                    {team.name}
                                </td>
                                <td class="py-4 px-4">
                                    <p class="font-semibold text-xs {theme.darkMode ? 'text-zinc-300' : 'text-gray-800'}">{team.season}</p>
                                    <p class="text-[10px] mt-0.5 {theme.darkMode ? 'text-zinc-500' : 'text-gray-405'}">{team.track}</p>
                                </td>
                                <td class="py-4 px-4">
                                    <p class="font-bold text-xs">{team.leaderName}</p>
                                    <p class="text-[10px] mt-0.5 {theme.darkMode ? 'text-zinc-400' : 'text-gray-550'}">{team.leader}</p>
                                </td>
                                <td class="py-4 px-4 font-semibold text-xs">
                                    {team.membersCount}/{team.maxMembers}
                                </td>
                                <td class="py-4 px-4">
                                    {#if team.status === "APPROVED"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
                                            Approved
                                        </span>
                                    {:else if team.status === "PENDING"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-505 border border-amber-500/20">
                                            Pending
                                        </span>
                                    {:else if team.status === "REJECTED"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-500 border border-rose-500/20">
                                            Rejected
                                        </span>
                                    {/if}
                                </td>
                                <td class="py-4 px-4 text-center">
                                    <div class="flex justify-center gap-2">
                                        <button onclick={() => openViewModal(team)} title="View Details" class="p-2 rounded-lg hover:bg-blue-100 hover:text-blue-600 dark:hover:bg-blue-950/40 dark:hover:text-blue-400 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent">
                                            <Eye class="w-4 h-4" />
                                        </button>
                                        {#if team.status === "PENDING"}
                                            <button onclick={() => approveTeam(team.id)} title="Approve Team" class="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-emerald-500 hover:bg-emerald-600 text-white cursor-pointer border-0 transition-colors">
                                                Approve
                                            </button>
                                            <button onclick={() => rejectTeam(team.id)} title="Reject Team" class="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-rose-500 hover:bg-rose-600 text-white cursor-pointer border-0 transition-colors">
                                                Reject
                                            </button>
                                        {/if}
                                    </div>
                                </td>
                            </tr>
                        {/each}
                    {:else}
                        <tr>
                            <td colspan="7" class="py-12 text-center text-gray-500">
                                <p class="font-semibold text-lg">No teams found</p>
                                <p class="text-sm">Try adjusting your filters or search terms.</p>
                            </td>
                        </tr>
                    {/if}
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal: View Details -->
{#if showViewModal && selectedTeam}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={() => showViewModal = false} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 flex items-center gap-2">
                <Users class="w-5 h-5 text-orange-500" />
                Team Details - {selectedTeam.name}
            </h3>
            
            <div class="space-y-4">
                <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950 border-zinc-800' : 'bg-gray-50 border-gray-100'}">
                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Season & Track</p>
                    <p class="font-bold text-sm mt-1">{selectedTeam.season}</p>
                    <p class="text-xs text-gray-500 mt-0.5">Track: {selectedTeam.track}</p>
                </div>

                <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950 border-zinc-800' : 'bg-gray-50 border-gray-100'}">
                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1">
                        <Mail class="w-3.5 h-3.5" />
                        Leader Email
                    </p>
                    <p class="font-bold text-sm mt-1 text-[#ea580c]">{selectedTeam.leaderName} ({selectedTeam.leader})</p>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950 border-zinc-800' : 'bg-gray-50 border-gray-100'}">
                        <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Members Count</p>
                        <p class="font-bold text-sm mt-1">{selectedTeam.membersCount} / {selectedTeam.maxMembers}</p>
                    </div>
                    <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-950 border-zinc-800' : 'bg-gray-50 border-gray-100'}">
                        <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Status</p>
                        <p class="font-bold text-sm mt-1">{selectedTeam.status}</p>
                    </div>
                </div>
            </div>

            <button onclick={() => showViewModal = false} class="mt-8 bg-zinc-500 hover:bg-zinc-650 text-white rounded-xl py-3 font-semibold transition-all w-full cursor-pointer border-0">
                Close
            </button>
        </div>
    </div>
{/if}

<style>
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in {
        animation: fadeIn 0.3s ease-out forwards;
    }
</style>
