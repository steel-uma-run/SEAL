<script lang="ts">
    import { page } from "$app/stores";
    import { goto } from "$app/navigation";
    import { theme } from "$lib/theme.svelte";

    let { children } = $props();
    let isManagementExpanded = $state(true);

    function handleLogout(event: Event) {
        event.preventDefault();
        if (typeof window !== "undefined") {
            localStorage.removeItem("auth_data");
        }
        goto("/auth/login");
    }
</script>

<div class="font-sans transition-colors duration-300 {theme.darkMode ? 'bg-zinc-950 text-zinc-100' : 'bg-[#f8f9fa] text-gray-800'}">
    <!-- Side bar -->
    <aside class="fixed top-0 left-0 z-40 w-64 flex flex-col hidden md:flex h-screen shrink-0 shadow-[2px_0_15px_rgba(0,0,0,0.02)] transition-colors duration-300 {theme.darkMode ? 'bg-zinc-900 border-r border-zinc-800' : 'bg-[#fff9f4] border-r border-[#ffe8d6]'}">
        
        <div class="p-6 pt-8 mb-2 shrink-0">
            <h1 class="text-3xl font-black tracking-tight text-[#f97316]">SEAL</h1>
        </div>

        <nav class="px-4 space-y-2 flex-1 overflow-y-auto custom-scrollbar pb-4">
            <a href="/dashboard" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {String($page.url.pathname) === '/dashboard' ? (theme.darkMode ? 'bg-orange-950/40 text-orange-400 font-semibold' : 'bg-[#ffedd5] text-[#ea580c] font-semibold shadow-sm') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]')}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
                Dashboard
            </a>

            <div>
                <button onclick={() => isManagementExpanded = !isManagementExpanded} class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-all text-left cursor-pointer {theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">
                    <span class="flex items-center gap-3">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
                        Management
                    </span>
                    <svg class="w-4 h-4 transition-transform duration-200 {isManagementExpanded ? 'rotate-180' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
                </button>
                
                {#if isManagementExpanded}
                    <div class="pl-8 mt-1 space-y-1">
                        <a href="/dashboard/seasons" class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">Seasons</a>
                        <a href="/dashboard/teams" class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">Teams</a>
                        <a href="/dashboard/experts" class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">Judges & Mentors</a>
                        <a href="/dashboard/manageUser" class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">Users</a>
                    </div>
                {/if}
            </div>
            
            <a href="#" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                Profile
            </a>

            <a href="#" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                Settings
            </a>
        </nav>

        <div class="p-6 border-t mt-auto shrink-0 {theme.darkMode ? 'border-zinc-800' : 'border-[#ffe8d6]/50'}">
            <a href="/auth/login" onclick={handleLogout} class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-[#ef4444] font-medium {theme.darkMode ? 'hover:bg-red-950/20' : 'hover:bg-red-50'}">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
                Logout
            </a>
        </div>
    </aside>

    <!-- Main -->
    <main class="md:ml-64 flex-1 flex flex-col min-h-screen transition-colors duration-300 {theme.darkMode ? 'bg-zinc-950' : 'bg-white'}">
        <div class="p-4 md:p-6 w-full">
            {@render children()}
        </div>
    </main>
</div>

<style>
    :global(body) {
        margin: 0;
        padding: 0;
    }
    .custom-scrollbar::-webkit-scrollbar {
        width: 6px;
    }
    .custom-scrollbar::-webkit-scrollbar-track {
        background: transparent;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb {
        background: #e5e7eb;
        border-radius: 10px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background: #d1d5db;
    }
</style>
