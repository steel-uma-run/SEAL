<script lang="ts">
    // Missing backend API for score/evaluate
    let score = $state("");
    let feedback = $state("");
    let submitMessage = $state("");

    function handleScore(e: Event) {
        e.preventDefault();
        submitMessage = "Scores submitted! (API not implemented in backend)";
    }
</script>

<div class="bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
    <div class="flex items-center gap-3 mb-6">
        <div class="p-2 bg-purple-100 rounded-lg text-purple-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"></path></svg>
        </div>
        <h2 class="text-xl font-bold text-gray-800">Score & Evaluate</h2>
    </div>
    
    <div class="mb-6 p-5 border border-purple-100 rounded-xl bg-purple-50/50">
        <div class="flex justify-between items-start">
            <div>
                <h3 class="font-bold text-lg text-purple-900">Team Alpha <span class="ml-2 text-xs font-medium px-2 py-1 bg-purple-100 text-purple-700 rounded-full">DEMO</span></h3>
                <p class="text-sm text-purple-700 mt-1">Project: AI powered study planner.</p>
            </div>
            <a href="#" class="inline-flex items-center gap-1 text-purple-600 hover:text-purple-800 bg-white px-3 py-1.5 rounded-lg text-sm font-medium shadow-sm transition-colors border border-purple-100 hover:border-purple-200">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path></svg>
                View Submission
            </a>
        </div>
    </div>

    <form onsubmit={handleScore} class="flex flex-col gap-5">
        <div class="space-y-1">
            <label class="text-sm font-semibold text-gray-700">Score (0-100)</label>
            <input type="number" bind:value={score} min="0" max="100" required placeholder="85" class="w-full rounded-xl border border-gray-200 bg-gray-50 p-3 text-lg font-medium focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none" />
        </div>
        
        <div class="space-y-1">
            <label class="text-sm font-semibold text-gray-700">Feedback</label>
            <textarea bind:value={feedback} rows="4" placeholder="Provide constructive feedback..." class="w-full rounded-xl border border-gray-200 bg-gray-50 p-3 focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all outline-none resize-none"></textarea>
        </div>
        
        {#if submitMessage}
            <div class="p-3 rounded-lg text-sm font-medium bg-purple-50 text-purple-700 border border-purple-100 flex items-start gap-2">
                <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                {submitMessage}
            </div>
        {/if}

        <button type="submit" class="mt-2 w-full bg-purple-600 text-white rounded-xl py-3 font-semibold shadow-sm hover:bg-purple-700 hover:shadow transition-all">
            Submit Evaluation
        </button>
    </form>
</div>
