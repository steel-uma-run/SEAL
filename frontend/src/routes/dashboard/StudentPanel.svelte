<script lang="ts">

    let { profile, seasons } = $props<{ profile: any, seasons: any[] }>();

</script>

<div class="grid grid-cols-1 md:grid-cols-2 gap-8">
    <!-- Create Team Section -->
    <a 
        href="/student/create-team" 
        class="group block bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-orange-300 hover:-translate-y-1 transition-all cursor-pointer"
    >
        <div class="flex items-center gap-4 mb-4">
            <div class="p-3 bg-orange-100 rounded-xl text-orange-600 group-hover:scale-110 transition-transform">
                <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            </div>
            <h2 class="text-xl font-bold text-gray-800 group-hover:text-orange-600 transition-colors">Create a Team</h2>
        </div>
        
        <p class="text-gray-500 mb-6 text-sm md:text-base">
            Form a new team, select a hackathon season, and invite members to collaborate on your innovative idea.
        </p>
        
        <div class="inline-flex items-center gap-2 text-orange-600 font-semibold group-hover:gap-3 transition-all">
            Go to Create Team
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
        </div>
    </a>

    <!-- Submit Section -->
    <a 
        href="/student/submit-project" 
        class="group block bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-300 hover:-translate-y-1 transition-all cursor-pointer"
    >
        <div class="flex items-center gap-4 mb-4">
            <div class="p-3 bg-blue-100 rounded-xl text-blue-600 group-hover:scale-110 transition-transform">
                <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
            </div>
            <h2 class="text-xl font-bold text-gray-800 group-hover:text-blue-600 transition-colors">Submit Project</h2>
        </div>
        
        <p class="text-gray-500 mb-6 text-sm md:text-base">
            Ready for judging? Submit your project repository, video pitch, and presentation materials here.
        </p>
        
        <div class="inline-flex items-center gap-2 text-blue-600 font-semibold group-hover:gap-3 transition-all">
            Go to Submission
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
        </div>
    </a>
</div>
