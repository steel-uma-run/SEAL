<script lang="ts">
    import { page } from "$app/stores";
    import { goto } from "$app/navigation";

    let { children } = $props();

    function handleLogout(event: Event) {
        event.preventDefault();
        if (typeof window !== "undefined") {
            localStorage.removeItem("auth_data");
        }
        goto("/auth/login");
    }
</script>

<div class="flex h-screen bg-[#f8f9fa] overflow-hidden font-sans">
    <!-- Sidebar -->
    <aside class="w-64 bg-[#fff9f4] border-r border-[#ffe8d6] flex flex-col justify-between hidden md:flex h-full shrink-0 relative z-10 shadow-[2px_0_15px_rgba(0,0,0,0.02)]">
        <div>
            <!-- Logo -->
            <div class="p-6 pt-8 mb-4">
                <h1 class="text-3xl font-black tracking-tight text-[#f97316]">SEAL</h1>
            </div>

            <!-- Navigation Links -->
            <nav class="px-4 space-y-2">
                <a href="/dashboard" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {String($page.url.pathname) === '/dashboard' ? 'bg-[#ffedd5] text-[#ea580c] font-semibold shadow-sm' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
                    Dashboard
                </a>
                
                <a href="#" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                    Profile
                </a>

                <a href="#" class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                    Settings
                </a>
            </nav>
        </div>

        <!-- Logout -->
        <div class="p-6 border-t border-[#ffe8d6]/50">
            <a href="/auth/login" onclick={handleLogout} class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-[#ef4444] font-medium hover:bg-red-50">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
                Logout
            </a>
        </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 flex flex-col h-full overflow-hidden bg-white">
        <div class="flex-1 overflow-y-auto w-full custom-scrollbar">
            {@render children()}
        </div>
    </main>
</div>

<style>
    :global(body) {
        margin: 0;
        padding: 0;
        background-color: #f8f9fa;
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
