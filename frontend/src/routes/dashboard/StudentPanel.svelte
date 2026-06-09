<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    let { profile, seasons } = $props<{ profile: any, seasons: any[] }>();

</script>

<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-10">
    <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
        <div class="w-12 h-12 rounded-xl bg-[#ffedd5] text-[#ea580c] flex items-center justify-center shrink-0">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
        </div>
        <div>
            <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Current Season</p>
            <h3 class="text-xl font-bold text-gray-800">{seasons.length > 0 ? seasons[0].name : 'Spring 2026'}</h3>
        </div>
    </div>

    <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
        <div class="w-12 h-12 rounded-xl bg-green-50 text-green-600 flex items-center justify-center shrink-0">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9"></path></svg>
        </div>
        <div>
            <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Registered Tracks</p>
            <h3 class="text-xl font-bold text-gray-800">2</h3>
        </div>
    </div>

    <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
        <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
        </div>
        <div>
            <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Team Status</p>
            <h3 class="text-lg font-bold text-gray-800">Looking for team</h3>
        </div>
    </div>

    <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
        <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        </div>
        <div>
            <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Submission Due</p>
            <h3 class="text-lg font-bold text-gray-800">In 5 days</h3>
        </div>
    </div>
</div>

<div class="bg-white p-8 rounded-3xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] border border-gray-100 mb-8">
    <div class="flex justify-between items-center mb-8">
        <h2 class="text-xl font-bold text-gray-800">My Registered Tracks</h2>
        <button class="bg-[#fff2e8] text-[#ea580c] px-4 py-2 rounded-lg text-sm font-semibold hover:bg-[#ffedd5] transition-colors">Join More</button>
    </div>

    <div class="space-y-4">
        <div class="border border-gray-100 rounded-2xl p-6 flex gap-5 hover:border-[#fed7aa] hover:shadow-sm transition-all bg-white">
            <div class="w-10 h-10 rounded-lg bg-[#fff2e8] text-[#ea580c] flex items-center justify-center shrink-0 mt-1">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
            </div>
            <div>
                <h3 class="font-bold text-gray-800 text-lg">AI & Machine Learning Track</h3>
                <p class="text-sm text-gray-500 mt-1">Build innovative AI solutions. Sponsored by Google.</p>
                <div class="mt-3 inline-block bg-[#ecfdf5] text-[#059669] px-3 py-1 rounded-full text-xs font-semibold">Approved</div>
            </div>
        </div>

        <div class="border border-gray-100 rounded-2xl p-6 flex gap-5 hover:border-blue-200 hover:shadow-sm transition-all bg-white">
            <div class="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 mt-1">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path>
            </div>
            <div>
                <h3 class="font-bold text-gray-800 text-lg">FinTech Innovation Track</h3>
                <p class="text-sm text-gray-500 mt-1">Next generation financial applications. Sponsored by Stripe.</p>
                <div class="mt-3 inline-block bg-[#fefce8] text-[#ca8a04] px-3 py-1 rounded-full text-xs font-semibold">Pending Approval</div>
            </div>
        </div>
    </div>
</div>

<div class="pt-4 border-t-2 border-dashed border-gray-200">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard Actions</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <a href="/student/create-team" class="group block bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-orange-300 hover:-translate-y-1 transition-all cursor-pointer">
            <div class="flex items-center gap-4 mb-4">
                <div class="p-3 bg-orange-100 rounded-xl text-orange-600 group-hover:scale-110 transition-transform">
                    <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                </div>
                <h2 class="text-xl font-bold text-gray-800 group-hover:text-orange-600 transition-colors">Create a Team</h2>
            </div>
            <p class="text-gray-500 mb-6 text-sm md:text-base">Form a new team, select a hackathon season, and invite members to collaborate on your innovative idea.</p>
            <div class="inline-flex items-center gap-2 text-orange-600 font-semibold group-hover:gap-3 transition-all">
                Go to Create Team
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
            </div>
        </a>

        <a href="/student/submit-project" class="group block bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-300 hover:-translate-y-1 transition-all cursor-pointer">
            <div class="flex items-center gap-4 mb-4">
                <div class="p-3 bg-blue-100 rounded-xl text-blue-600 group-hover:scale-110 transition-transform">
                    <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
                </div>
                <h2 class="text-xl font-bold text-gray-800 group-hover:text-blue-600 transition-colors">Submit Project</h2>
            </div>
            <p class="text-gray-500 mb-6 text-sm md:text-base">Ready for judging? Submit your project repository, video pitch, and presentation materials here.</p>
            <div class="inline-flex items-center gap-2 text-blue-600 font-semibold group-hover:gap-3 transition-all">
                Go to Submission
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
            </div>
        </a>
    </div>
</div>