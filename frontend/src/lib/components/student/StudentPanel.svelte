<script lang="ts">
    import { theme } from "$lib/theme.svelte";
</script>

<div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'} mb-8">
    <div class="flex justify-between items-center mb-8">
        <h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">My Registered Tracks</h2>
        <button class="px-4 py-2 rounded-lg text-sm font-semibold transition-colors {theme.darkMode ? 'bg-orange-950/40 text-orange-400 hover:bg-orange-900/40' : 'bg-[#fff2e8] text-[#ea580c] hover:bg-[#ffedd5]'} hover:cursor-pointer border-0">
            Join More
        </button>
    </div>

    <div class="space-y-4">
        <!-- Track 1 -->
        <div class="border rounded-2xl p-6 flex gap-5 transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:border-orange-900/50' : 'border-gray-100 bg-white hover:border-[#fed7aa] hover:shadow-sm'}">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 mt-1 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-[#fff2e8] text-[#ea580c]'}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
            </div>
            <div>
                <h3 class="font-bold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">AI & Machine Learning Track</h3>
                <p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Build innovative AI solutions. Sponsored by Google.</p>
                <div class="mt-3 inline-block px-3 py-1 rounded-full text-xs font-semibold {theme.darkMode ? 'bg-emerald-950/40 text-emerald-400' : 'bg-[#ecfdf5] text-[#059669]'}">Approved</div>
            </div>
        </div>

        <!-- Track 2 -->
        <div class="border rounded-2xl p-6 flex gap-5 transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 hover:border-blue-900/50' : 'border-gray-100 bg-white hover:border-blue-200 hover:shadow-sm'}">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 mt-1 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
            </div>
            <div>
                <h3 class="font-bold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">FinTech Innovation Track</h3>
                <p class="text-sm mt-1 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">Next generation financial applications. Sponsored by Stripe.</p>
                <div class="mt-3 inline-block px-3 py-1 rounded-full text-xs font-semibold {theme.darkMode ? 'bg-yellow-950/40 text-yellow-400' : 'bg-[#fefce8] text-[#ca8a04]'}">Pending Approval</div>
            </div>
        </div>
    </div>
</div>
