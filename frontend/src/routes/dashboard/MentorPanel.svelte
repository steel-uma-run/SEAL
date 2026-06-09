<script lang="ts">
    // Missing backend API for fetching mentored teams
</script>

<div class="bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
    <div class="flex items-center gap-3 mb-6">
        <div class="p-2 bg-indigo-100 rounded-lg text-indigo-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
        </div>
        <div>
            <h2 class="text-xl font-bold text-gray-800">My Mentoring Teams</h2>
            <p class="text-sm text-gray-500 mt-1">You are currently mentoring the following teams.</p>
        </div>
    </div>
    
    <div class="space-y-4">
        <!-- Demo UI -->
        <div class="group p-5 border border-indigo-100 rounded-xl bg-white hover:bg-indigo-50/50 hover:border-indigo-200 transition-all duration-300">
            <div class="flex items-center justify-between">
                <h3 class="font-bold text-lg text-indigo-900 group-hover:text-indigo-700 transition-colors">Team Beta <span class="ml-2 text-xs font-medium px-2 py-1 bg-indigo-100 text-indigo-700 rounded-full">DEMO</span></h3>
                <span class="bg-white border border-indigo-100 px-3 py-1 rounded-full text-xs font-semibold text-indigo-600 shadow-sm">Active</span>
            </div>
            <p class="text-sm text-indigo-700/80 mt-2">Project: Blockchain Ticketing System</p>
            <div class="flex items-center gap-2 mt-4 pt-4 border-t border-indigo-50">
                <div class="w-6 h-6 rounded-full bg-indigo-200 flex items-center justify-center text-xs font-bold text-indigo-700">JD</div>
                <p class="text-xs font-medium text-indigo-600">Leader: john.doe@fptu.edu.vn</p>
            </div>
        </div>

        <div class="group p-5 border border-indigo-100 rounded-xl bg-white hover:bg-indigo-50/50 hover:border-indigo-200 transition-all duration-300">
            <div class="flex items-center justify-between">
                <h3 class="font-bold text-lg text-indigo-900 group-hover:text-indigo-700 transition-colors">Team Gamma <span class="ml-2 text-xs font-medium px-2 py-1 bg-indigo-100 text-indigo-700 rounded-full">DEMO</span></h3>
                <span class="bg-white border border-indigo-100 px-3 py-1 rounded-full text-xs font-semibold text-indigo-600 shadow-sm">Active</span>
            </div>
            <p class="text-sm text-indigo-700/80 mt-2">Project: Smart Home Hub</p>
            <div class="flex items-center gap-2 mt-4 pt-4 border-t border-indigo-50">
                <div class="w-6 h-6 rounded-full bg-indigo-200 flex items-center justify-center text-xs font-bold text-indigo-700">JS</div>
                <p class="text-xs font-medium text-indigo-600">Leader: jane.smith@fptu.edu.vn</p>
            </div>
        </div>
    </div>
</div>
