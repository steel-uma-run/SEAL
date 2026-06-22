<script lang="ts">
    import { theme } from "$lib/theme.svelte";

    // Global Mock Data
    let calibrationProgress = $state(80); // Must be 100% to unlock main phase
    let standardThreshold = 60; 
    let currentPhase = $state<'calibration' | 'main'>('calibration');
    let filterStatus = $state<'all' | 'pending' | 'graded'>('all');

    // Mock Submissions Data for Calibration Phase
    let calibSubmissions = $state([
        { id: 101, teamName: "Sample Project A", project: "E-learning mock", score: "", feedback: "", status: "pending", errorMsg: "", submitMessage: "" },
    ]);

    // Mock Submissions Data for Main Phase
    let mainSubmissions = $state([
        { id: 1, teamName: "Team Alpha", project: "AI powered study planner", score: "", feedback: "", status: "pending", errorMsg: "", submitMessage: "" },
        { id: 2, teamName: "Team Delta", project: "Blockchain Voting System", score: 55, feedback: "", status: "pending", errorMsg: "", submitMessage: "" },
        { id: 3, teamName: "Team Gamma", project: "E-Commerce for Local Farmers", score: 85, feedback: "Solid architecture and clear presentation.", status: "graded", errorMsg: "", submitMessage: "Scores submitted successfully! (Blind grading applied)" }
    ]);

    // Derived states based on current phase
    let activeSubmissions = $derived(currentPhase === 'calibration' ? calibSubmissions : mainSubmissions);

    let filteredSubmissions = $derived(
        activeSubmissions.filter(sub => {
            if (filterStatus === 'all') return true;
            return sub.status === filterStatus;
        })
    );

    let gradedCount = $derived(activeSubmissions.filter(s => s.status === 'graded').length);
    let totalCount = $derived(activeSubmissions.length);
    let gradingProgress = $derived(Math.round((gradedCount / totalCount) * 100) || 0);

    function handleScore(e: Event, id: number) {
        e.preventDefault();
        const subList = currentPhase === 'calibration' ? calibSubmissions : mainSubmissions;
        const subIndex = subList.findIndex(s => s.id === id);
        if (subIndex === -1) return;
        const sub = subList[subIndex];
        
        sub.errorMsg = "";
        sub.submitMessage = "";

        if (typeof sub.score === 'number' && sub.score < standardThreshold && sub.feedback.trim() === "") {
            sub.errorMsg = `Score is below standard threshold (${standardThreshold}). A mandatory documented reason (feedback) is required.`;
            return;
        }

        sub.status = "graded";
        sub.submitMessage = "Scores submitted successfully! (Blind grading applied)";

        // Simulate Calibration Progress increase
        if (currentPhase === 'calibration' && sub.status === 'graded') {
            calibrationProgress = 100;
        }
    }

    function handleEdit(id: number) {
        const subList = currentPhase === 'calibration' ? calibSubmissions : mainSubmissions;
        const subIndex = subList.findIndex(s => s.id === id);
        if (subIndex === -1) return;
        const sub = subList[subIndex];
        sub.status = "pending";
        sub.submitMessage = "";
    }

    function completeCalibration() {
        calibrationProgress = 100;
        currentPhase = 'main';
    }
</script>

<div class="p-8 rounded-3xl border transition-all flex flex-col {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
    <div class="flex items-center gap-3 mb-6">
        <div class="p-2 bg-orange-100 dark:bg-orange-900/30 rounded-lg text-orange-600 shrink-0">
            <svg class="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <!-- Document outline -->
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <path d="M14 2v6h6" />
                
                <!-- Checkmarks and lines -->
                <path d="M7.5 12.5l1.5 1.5 2.5-2.5" />
                <path d="M12.5 12.5h3.5" />
                
                <path d="M7.5 17.5l1.5 1.5 2.5-2.5" />
                <path d="M12.5 17.5h3.5" />
                
                <!-- A+ Badge -->
                <circle cx="16.5" cy="8.5" r="4.5" fill="currentColor" stroke="none" />
                <text x="16.5" y="10" font-family="sans-serif" font-size="4.5" font-weight="900" text-anchor="middle" fill="#fff" stroke="none">A+</text>
            </svg>
        </div>
        <div class="flex-1 flex justify-between items-center">
            <div>
                <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Judging Phase</h2>
                <p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mt-0.5">Score and evaluate submissions.</p>
            </div>
            <div class="text-right hidden sm:block">
                <span class="text-sm font-bold {theme.darkMode ? 'text-orange-400' : 'text-orange-600'}">{gradedCount}/{totalCount} Graded</span>
                <p class="text-[10px] uppercase tracking-wider font-semibold {theme.darkMode ? 'text-zinc-500' : 'text-gray-500'}">{gradingProgress}% Completed</p>
            </div>
        </div>
    </div>

    <!-- Phase Selectors (Clickable Stages) -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        <!-- Calibration Phase Button -->
        <button onclick={() => currentPhase = 'calibration'} class="text-left p-4 rounded-xl border transition-all cursor-pointer relative overflow-hidden {currentPhase === 'calibration' ? (theme.darkMode ? 'border-orange-500 bg-orange-900/20 ring-1 ring-orange-500' : 'border-orange-500 bg-orange-50 ring-1 ring-orange-500') : (theme.darkMode ? 'border-zinc-800 bg-zinc-950/50 hover:border-zinc-700' : 'border-gray-200 bg-gray-50 hover:border-gray-300')}">
            <div class="flex justify-between items-start mb-2">
                <h3 class="font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Calibration Phase</h3>
                <span class="text-xs font-bold {calibrationProgress < 100 ? 'text-amber-500' : 'text-green-500'}">{calibrationProgress}%</span>
            </div>
            <p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mb-3">Grade sample projects to align standards.</p>
            <div class="w-full bg-gray-200 rounded-full h-1.5 dark:bg-zinc-800">
                <div class="{calibrationProgress < 100 ? 'bg-amber-500' : 'bg-green-500'} h-1.5 rounded-full transition-all" style="width: {calibrationProgress}%"></div>
            </div>
        </button>

        <!-- Main Phase Button -->
        <button onclick={() => currentPhase = 'main'} class="text-left p-4 rounded-xl border transition-all cursor-pointer relative {currentPhase === 'main' ? (theme.darkMode ? 'border-orange-500 bg-orange-900/20 ring-1 ring-orange-500' : 'border-orange-500 bg-orange-50 ring-1 ring-orange-500') : (theme.darkMode ? 'border-zinc-800 bg-zinc-950/50 hover:border-zinc-700' : 'border-gray-200 bg-gray-50 hover:border-gray-300')}">
            <div class="flex justify-between items-start mb-2">
                <h3 class="font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Main Phase</h3>
                {#if calibrationProgress < 100}
                    <svg class="w-4 h-4 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                {/if}
            </div>
            <p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mb-3">Grade actual student submissions.</p>
            {#if calibrationProgress >= 100}
                <div class="inline-flex items-center gap-1.5 px-2 py-1 rounded-md text-[10px] font-semibold {theme.darkMode ? 'bg-red-900/30 text-red-400' : 'bg-red-100 text-red-700'}">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    47h 59m remaining
                </div>
            {:else}
                <p class="text-[10px] font-semibold text-amber-500">Locked until Calibration is 100%</p>
            {/if}
        </button>
    </div>

    <!-- Toolbar: Title & Toggle Filter -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 pb-4 border-b {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
        <h3 class="font-bold text-lg {theme.darkMode ? 'text-orange-400' : 'text-orange-700'}">
            {currentPhase === 'calibration' ? 'Sample Projects' : 'Student Submissions'}
        </h3>
        
        <!-- Toggle Filter Dropdown -->
        <div class="flex items-center gap-2">
            <svg class="w-4 h-4 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"></path></svg>
            <select bind:value={filterStatus} class="text-sm font-semibold border rounded-lg pl-3 pr-8 py-2 cursor-pointer outline-none transition-all {theme.darkMode ? 'bg-zinc-950 border-zinc-700 text-zinc-200 focus:border-orange-500' : 'bg-white border-gray-200 text-gray-700 focus:border-orange-500'}">
                <option value="all">All ({totalCount})</option>
                <option value="pending">Pending ({totalCount - gradedCount})</option>
                <option value="graded">Graded ({gradedCount})</option>
            </select>
        </div>
    </div>

    <!-- Submission List Area -->
    <div class="space-y-6 relative overflow-hidden min-h-[200px]">
        {#if currentPhase === 'main' && calibrationProgress < 100}
            <div class="absolute inset-0 z-10 flex flex-col items-center justify-center p-4 text-center rounded-xl backdrop-blur-md {theme.darkMode ? 'bg-zinc-950/70' : 'bg-white/70'}">
                <div class="p-4 bg-orange-100 dark:bg-orange-900/30 rounded-full mb-3">
                    <svg class="w-8 h-8 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                </div>
                <p class="font-bold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">Main Phase is Locked</p>
                <p class="text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mt-1 max-w-sm">You must complete 100% of calibration grading before you can grade actual student submissions.</p>
                <button onclick={completeCalibration} class="mt-4 px-4 py-2 bg-orange-500 text-white rounded-lg text-sm font-semibold shadow hover:bg-orange-600 transition-colors cursor-pointer">Simulate Complete Calibration</button>
            </div>
        {/if}

        {#if filteredSubmissions.length === 0}
            <div class="flex flex-col items-center justify-center py-10 text-center">
                <div class="w-16 h-16 rounded-full flex items-center justify-center mb-3 {theme.darkMode ? 'bg-zinc-900' : 'bg-gray-50'}">
                    <svg class="w-8 h-8 {theme.darkMode ? 'text-zinc-600' : 'text-gray-400'}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                </div>
                <h3 class="text-sm font-bold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">All caught up!</h3>
                <p class="text-xs {theme.darkMode ? 'text-zinc-500' : 'text-gray-500'} mt-1">No {filterStatus} submissions found.</p>
            </div>
        {/if}

        {#each filteredSubmissions as sub (sub.id)}
            <div class="p-5 border rounded-xl transition-all {sub.status === 'graded' ? (theme.darkMode ? 'border-green-900/50 bg-green-950/10' : 'border-green-200 bg-green-50/50') : (theme.darkMode ? 'border-orange-900/30 bg-orange-950/10' : 'border-orange-100 bg-orange-50/50')}">
                <div class="flex justify-between items-start mb-4">
                    <div>
                        <div class="flex items-center gap-3">
                            <h3 class="font-bold text-lg {theme.darkMode ? 'text-orange-400' : 'text-orange-900'}">{sub.teamName}</h3>
                            {#if sub.status === 'graded'}
                                <span class="px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider {theme.darkMode ? 'bg-green-900/40 text-green-400' : 'bg-green-100 text-green-700'}">Graded</span>
                            {:else}
                                <span class="px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider {theme.darkMode ? 'bg-amber-900/40 text-amber-400' : 'bg-amber-100 text-amber-700'}">Pending</span>
                            {/if}
                        </div>
                        <p class="text-sm {theme.darkMode ? 'text-orange-300' : 'text-orange-700'} mt-1">Project: {sub.project}</p>
                    </div>
                    {#if sub.status === 'graded'}
                        <button type="button" onclick={() => handleEdit(sub.id)} class="flex items-center gap-1.5 px-3 py-1.5 border rounded-lg text-xs font-semibold shadow-sm hover:shadow transition-all cursor-pointer {theme.darkMode ? 'bg-zinc-800 border-zinc-700 text-zinc-300 hover:text-white' : 'bg-white border-gray-200 text-gray-600 hover:text-gray-900'}">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                            Edit Score
                        </button>
                    {/if}
                </div>

                <form onsubmit={(e) => handleScore(e, sub.id)} class="flex flex-col gap-4">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div class="space-y-1">
                            <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Score (0-100)</label>
                            <input type="number" bind:value={sub.score} min="0" max="100" required placeholder="85" disabled={sub.status === 'graded'} class="w-full rounded-xl border p-3 text-lg font-medium transition-all outline-none disabled:opacity-60 {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                        </div>
                        
                        <div class="space-y-1">
                            <label class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Feedback / Reason</label>
                            <textarea bind:value={sub.feedback} rows="2" placeholder="If score < {standardThreshold}, reason is required..." disabled={sub.status === 'graded'} class="w-full rounded-xl border p-3 transition-all outline-none resize-none disabled:opacity-60 {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"></textarea>
                        </div>
                    </div>
                    
                    {#if sub.errorMsg}
                        <div class="p-3 rounded-lg text-sm font-medium border flex items-start gap-2 {theme.darkMode ? 'bg-red-950/20 text-red-400 border-red-900/50' : 'bg-red-50 text-red-700 border-red-200'}">
                            <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            {sub.errorMsg}
                        </div>
                    {/if}

                    {#if sub.submitMessage}
                        <div class="p-3 rounded-lg text-sm font-medium border flex items-start gap-2 {theme.darkMode ? 'bg-green-950/20 text-green-400 border-green-900/50' : 'bg-green-50 text-green-700 border border-green-100'}">
                            <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                            {sub.submitMessage}
                        </div>
                    {/if}

                    {#if sub.status === 'pending'}
                        <button type="submit" class="mt-2 w-full md:w-auto self-end px-6 bg-orange-600 text-white rounded-xl py-2.5 font-semibold shadow-sm hover:bg-orange-700 hover:shadow transition-all border-0 cursor-pointer">
                            Submit Evaluation
                        </button>
                    {/if}
                </form>
            </div>
        {/each}
    </div>
</div>
