<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    import { 
        Plus, X, Shield, Edit2, RefreshCw, Eye, 
        User, Mail, BookOpen, Search,
        Users, Gavel, Lightbulb, AlertCircle // <-- Đã import thêm 4 icon cho thẻ Statistic
    } from "@lucide/svelte";

    // 1. MOCK DATA
    let expertsList = $state([
        { id: 1, name: "Nguyễn Văn A", role: "Judge", specialization: "Artificial Intelligence", email: "nva@fpt.edu.vn", assignedTeams: [1, 4] },
        { id: 2, name: "Trần Thị B", role: "Mentor", specialization: "System Design", email: "ttb@fpt.edu.vn", assignedTeams: [2, 4] },
        { id: 3, name: "Lê Văn C", role: "Mentor", specialization: "Cybersecurity", email: "lvc@fpt.edu.vn", assignedTeams: [1, 3] },
        { id: 4, name: "David Miller", role: "Judge", specialization: "Blockchain & Web3", email: "david@fpt.edu.vn", assignedTeams: [] }, // Sửa người này thành mảng rỗng để test unassigned
    ]);

    // 2. STATE & DERIVED CHO STATISTIC CARDS (Tự động đếm)
    let totalExperts = $derived(expertsList.length);
    let totalJudges = $derived(expertsList.filter(e => e.role === 'Judge').length);
    let totalMentors = $derived(expertsList.filter(e => e.role === 'Mentor').length);
    let unassignedExperts = $derived(expertsList.filter(e => e.assignedTeams.length === 0).length);

    // 3. STATE & DERIVED CHO SEARCH
    let searchQuery = $state("");

    let filteredExperts = $derived(
        expertsList.filter(exp => {
            const q = searchQuery.trim().toLowerCase();
            if (!q) return true;
            return (
                exp.name.toLowerCase().includes(q) ||
                exp.specialization.toLowerCase().includes(q) ||
                exp.role.toLowerCase().includes(q) ||
                exp.email.toLowerCase().includes(q)
            );
        })
    );

    // 4. STATE CHO MODAL FORM
    let showExpertModal = $state(false);
    let isExpertLoading = $state(false);
    let expertMessage = $state("");
    
    let editingId = $state<number | null>(null);
    let formName = $state("");
    let formRole = $state("JUDGE");
    let formSpecialization = $state("");
    let formEmail = $state("");

    function openExpertForm(expert: any = null) {
        expertMessage = "";
        if (expert) {
            editingId = expert.id;
            formName = expert.name;
            formRole = expert.role.toUpperCase();
            formSpecialization = expert.specialization;
            formEmail = expert.email || "";
        } else {
            editingId = null;
            formName = "";
            formRole = "JUDGE";
            formSpecialization = "";
            formEmail = "";
        }
        showExpertModal = true;
    }

    function closeExpertForm() {
        showExpertModal = false;
    }

    function handleSaveExpert(e: Event) {
        e.preventDefault();
        isExpertLoading = true;
        expertMessage = "";

        setTimeout(() => {
            const roleLabel = formRole === 'JUDGE' ? 'Judge' : 'Mentor';
            
            if (editingId) {
                expertsList = expertsList.map(exp => 
                    exp.id === editingId 
                        ? { ...exp, name: formName, role: roleLabel, specialization: formSpecialization, email: formEmail }
                        : exp
                );
                expertMessage = "Expert details updated successfully!";
            } else {
                const newExpert = {
                    id: Date.now(),
                    name: formName,
                    role: roleLabel,
                    specialization: formSpecialization || 'General',
                    email: formEmail,
                    assignedTeams: []
                };
                expertsList = [newExpert, ...expertsList]; 
                expertMessage = "New expert account created!";
            }

            isExpertLoading = false;
            setTimeout(() => { closeExpertForm(); }, 1200);
        }, 600);
    }

    // 5. LOGIC QUICK CHANGE ROLE
    function toggleRole(id: number) {
        expertsList = expertsList.map(exp => {
            if (exp.id === id) {
                if (exp.role === 'Judge') return { ...exp, role: 'Mentor' };
                if (exp.role === 'Mentor') return { ...exp, role: 'Judge' };
            }
            return exp;
        });
    }

    // 6. LOGIC VIEW PROFILE
    let viewingExpert = $state<any>(null);

    function openProfile(expert: any) { viewingExpert = expert; }
    function closeProfile() { viewingExpert = null; }
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full font-sans">
    <header class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
        <div>
            <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
                Mentorship & Judge Panel Management
            </h1>
            <p class="mt-1 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                Manage, edit, and assign experts (Mentors & Judges) for hackathon tracks.
            </p>
        </div>
        <div class="flex items-center gap-3 mt-4 md:mt-0">
            <button onclick={() => openExpertForm(null)} class="flex items-center gap-2 bg-[#ea580c] hover:bg-[#c2410c] text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all cursor-pointer border-0">
                <Plus class="w-4 h-4" />
                Add Expert
            </button>
        </div>
    </header>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-orange-50 text-orange-600'}">
                <Users class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-bold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Experts</p>
                <h3 class="text-2xl font-black {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{totalExperts}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
                <Gavel class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-bold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Judges</p>
                <h3 class="text-2xl font-black {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{totalJudges}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-green-950/40 text-green-400' : 'bg-green-50 text-green-600'}">
                <Lightbulb class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-bold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Mentors</p>
                <h3 class="text-2xl font-black {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{totalMentors}</h3>
            </div>
        </div>

        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 transition-colors {unassignedExperts > 0 ? (theme.darkMode ? 'bg-red-950/40 text-red-400' : 'bg-red-50 text-red-600') : (theme.darkMode ? 'bg-zinc-800 text-zinc-500' : 'bg-gray-100 text-gray-400')}">
                <AlertCircle class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-bold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Unassigned</p>
                <h3 class="text-2xl font-black transition-colors {unassignedExperts > 0 ? (theme.darkMode ? 'text-red-400' : 'text-red-600') : (theme.darkMode ? 'text-zinc-100' : 'text-gray-800')}">
                    {unassignedExperts}
                </h3>
            </div>
        </div>

    </div>

    <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
        
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6 pb-6 border-b {theme.darkMode ? 'border-zinc-800/80' : 'border-gray-100'}">
            
            <div class="relative w-full sm:w-80">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
                    <Search class="w-4 h-4" />
                </span>
                <input 
                    type="text" 
                    bind:value={searchQuery}
                    placeholder="Search by ID, name, or email..." 
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl border outline-none transition-all text-sm {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
                />
                {#if searchQuery}
                    <button type="button" onclick={() => searchQuery = ""} class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-md text-xs font-bold text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 cursor-pointer border-0 bg-transparent">
                        Clear
                    </button>
                {/if}
            </div>

            <div class="text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                Showing <strong class="text-[#ea580c] font-black">{filteredExperts.length}</strong> of {expertsList.length} experts
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[800px]">
                <thead>
                    <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-500' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                        <th class="py-3.5 px-4">Name</th>
                        <th class="py-3.5 px-4">Role</th>
                        <th class="py-3.5 px-4">Specialization</th>
                        <th class="py-3.5 px-4">Assigned Teams</th>
                        <th class="py-3.5 px-4">Actions</th>
                    </tr>
                </thead>
                <tbody class="text-sm">
                    
                    {#if filteredExperts.length === 0}
                        <tr>
                            <td colspan="5" class="py-16 text-center">
                                <div class="max-w-xs mx-auto space-y-2">
                                    <p class="text-3xl">🔍</p>
                                    <p class="font-bold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">No experts found</p>
                                    <p class="text-xs {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
                                        We couldn't find anyone matching <strong class="text-[#ea580c]">"{searchQuery}"</strong>.
                                    </p>
                                </div>
                            </td>
                        </tr>
                    {/if}

                    {#each filteredExperts as expert (expert.id)}
                        <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/30 text-zinc-200' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                            <td class="py-4 px-4 font-bold">{expert.name}</td>
                            
                            <td class="py-4 px-4 text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                {expert.role}
                            </td>
                            
                            <td class="py-4 px-4 text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                {expert.specialization}
                            </td>
                            
                            <td class="py-4 px-4">
                                {#if expert.assignedTeams.length > 0}
                                    <span class="inline-flex gap-1.5 flex-wrap">
                                        {#each expert.assignedTeams as team}
                                            <span class="px-2 py-0.5 rounded text-xs font-bold shadow-sm border {theme.darkMode ? 'bg-zinc-800 border-zinc-700 text-zinc-300' : 'bg-white border-gray-200 text-gray-600'}">
                                                {team}
                                            </span>
                                        {/each}
                                    </span>
                                {:else}
                                    <span class="text-xs italic opacity-50">None</span>
                                {/if}
                            </td>
                            
                            <td class="py-4 px-4">
                                <div class="flex gap-2">
                                    <button onclick={() => openExpertForm(expert)} class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border {theme.darkMode ? 'bg-orange-950/30 border-orange-900/50 text-orange-400 hover:bg-orange-900/50' : 'bg-orange-50 border-orange-100 text-orange-600 hover:bg-orange-100'}">
                                        <Edit2 class="w-3.5 h-3.5" /> Edit
                                    </button>
                                    
                                    <button onclick={() => toggleRole(expert.id)} class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border {theme.darkMode ? 'bg-purple-950/30 border-purple-900/50 text-purple-400 hover:bg-purple-900/50' : 'bg-purple-50 border-purple-100 text-purple-600 hover:bg-purple-100'}">
                                        <RefreshCw class="w-3.5 h-3.5" /> Change Role
                                    </button>
                                    
                                    <button onclick={() => openProfile(expert)} class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border {theme.darkMode ? 'bg-zinc-800/50 border-zinc-700 text-zinc-300 hover:bg-zinc-700' : 'bg-gray-100 border-gray-200 text-gray-600 hover:bg-gray-200'}">
                                        <Eye class="w-3.5 h-3.5" /> View Profile
                                    </button>
                                </div>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </div>
</div>

{#if showExpertModal}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 font-sans">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button type="button" onclick={closeExpertForm} class="absolute top-4 right-4 p-1.5 rounded-lg hover:bg-zinc-500/10 transition-colors text-zinc-400 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 flex items-center gap-2">
                <Shield class="w-5 h-5 text-[#ea580c]" />
                {editingId ? 'Edit Expert Details' : 'Add New Expert'}
            </h3>
            <form onsubmit={handleSaveExpert} class="flex flex-col gap-4">
                <div class="space-y-1.5">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Full Name *</label>
                    <input type="text" bind:value={formName} required placeholder="e.g. Dr. John Doe" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:border-[#ea580c]' : 'border-gray-200 bg-gray-50 focus:border-[#ea580c] focus:bg-white'}" />
                </div>
                <div class="space-y-1.5">
                    <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Email Address *</label>
                    <input type="email" bind:value={formEmail} required placeholder="email@fpt.edu.vn" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:border-[#ea580c]' : 'border-gray-200 bg-gray-50 focus:border-[#ea580c] focus:bg-white'}" />
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1.5">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">System Role</label>
                        <select bind:value={formRole} class="w-full rounded-xl border p-3 outline-none transition-all cursor-pointer {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100' : 'border-gray-200 bg-gray-50'}">
                            <option value="JUDGE">Judge</option>
                            <option value="MENTOR">Mentor</option>
                        </select>
                    </div>
                    <div class="space-y-1.5">
                        <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Specialization</label>
                        <input type="text" bind:value={formSpecialization} placeholder="e.g. Cybersecurity" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:border-[#ea580c]' : 'border-gray-200 bg-gray-50 focus:border-[#ea580c]'}" />
                    </div>
                </div>
                {#if expertMessage}
                    <div class="p-3 mt-2 rounded-xl text-sm font-bold border {expertMessage.includes('success') || expertMessage.includes('created') ? 'bg-green-500/20 border-green-500/50 text-green-400' : 'bg-red-500/20 border-red-500/50 text-red-400'}">
                        {expertMessage}
                    </div>
                {/if}
                <div class="flex gap-3 mt-4 pt-2 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
                    <button type="button" onclick={closeExpertForm} class="w-1/2 rounded-xl border py-3 font-bold transition-all cursor-pointer {theme.darkMode ? 'border-zinc-700 text-zinc-300' : 'border-gray-300 text-gray-700'}">Cancel</button>
                    <button type="submit" disabled={isExpertLoading} class="w-1/2 bg-[#ea580c] hover:bg-[#c2410c] text-white rounded-xl py-3 font-bold disabled:opacity-50 transition-all border-0 cursor-pointer">{isExpertLoading ? 'Saving...' : 'Save'}</button>
                </div>
            </form>
        </div>
    </div>
{/if}

{#if viewingExpert}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 font-sans">
        <div class="w-full max-w-sm rounded-3xl border relative transition-all shadow-2xl overflow-hidden {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-100'}">
            <div class="h-24 {theme.darkMode ? 'bg-zinc-800' : 'bg-orange-50'} relative">
                <button type="button" onclick={closeProfile} class="absolute top-4 right-4 p-1.5 rounded-full bg-black/20 hover:bg-black/40 text-white cursor-pointer border-0 z-10"><X class="w-4 h-4" /></button>
            </div>
            <div class="px-6 relative flex justify-center -mt-12 mb-4">
                <div class="w-24 h-24 rounded-full border-4 flex items-center justify-center text-3xl font-black shadow-md {theme.darkMode ? 'bg-zinc-800 border-zinc-900 text-zinc-300' : 'bg-white border-white text-gray-700'}">{viewingExpert.name.charAt(0).toUpperCase()}</div>
            </div>
            <div class="px-6 pb-8 text-center">
                <h3 class="text-xl font-extrabold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'}">{viewingExpert.name}</h3>
                <span class="inline-block mt-1.5 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider {viewingExpert.role === 'Judge' ? 'bg-blue-500/20 text-blue-400' : 'bg-green-500/20 text-green-400'}">{viewingExpert.role}</span>
                <div class="mt-6 space-y-3 text-left">
                    <div class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800 text-zinc-200' : 'bg-gray-50 border-gray-100 text-gray-700'}">
                        <Mail class="w-4 h-4 text-zinc-500" />
                        <div><p class="text-[10px] font-bold uppercase text-zinc-500">Email</p><p class="text-sm font-semibold">{viewingExpert.email}</p></div>
                    </div>
                    <div class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800 text-zinc-200' : 'bg-gray-50 border-gray-100 text-gray-700'}">
                        <BookOpen class="w-4 h-4 text-zinc-500" />
                        <div><p class="text-[10px] font-bold uppercase text-zinc-500">Specialization</p><p class="text-sm font-semibold">{viewingExpert.specialization}</p></div>
                    </div>
                    <div class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode ? 'bg-zinc-950/50 border-zinc-800 text-zinc-200' : 'bg-gray-50 border-gray-100 text-gray-700'}">
                        <User class="w-4 h-4 text-zinc-500" />
                        <div><p class="text-[10px] font-bold uppercase text-zinc-500">Teams</p><p class="text-sm font-semibold">{viewingExpert.assignedTeams.length > 0 ? `Team ${viewingExpert.assignedTeams.join(', Team ')}` : 'None'}</p></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
{/if}