<script lang="ts">
    import { page } from "$app/stores";
    import { goto } from "$app/navigation";
    import { theme } from "$lib/theme.svelte";
    import { LogOut } from "@lucide/svelte";

    interface MenuItem {
        href?: string;
        label: string;
        icon: any; // Lucide icon component
        children?: { href: string; label: string }[];
    }

    let { menuItems = [] } = $props<{ menuItems: MenuItem[] }>();
    
    // Manage expansion states for collapsible menus
    let expandedMenus = $state<Record<string, boolean>>({
        "Management": true
    });

    function toggleMenu(label: string) {
        expandedMenus[label] = !expandedMenus[label];
    }

    function handleLogout(event: Event) {
        event.preventDefault();
        if (typeof window !== "undefined") {
            localStorage.removeItem("auth_data");
        }
        goto("/auth/login");
    }
</script>

<aside class="fixed top-0 left-0 z-40 w-64 flex flex-col hidden md:flex h-screen shrink-0 shadow-[2px_0_15px_rgba(0,0,0,0.02)] transition-colors duration-300 {theme.darkMode ? 'bg-zinc-900 border-r border-zinc-800' : 'bg-[#fff9f4] border-r border-[#ffe8d6]'}">
    
    <div class="p-6 pt-8 mb-2 shrink-0">
        <a href={$page.url.pathname.startsWith('/coordinator') ? '/coordinator' : '/lecturer'} class="flex items-center gap-2.5 no-underline hover:opacity-80 transition-opacity">
            <img
                src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
                alt="Logo"
                class="h-8 w-auto object-contain block"
            />
            <h1 class="text-3xl font-black tracking-tight text-[#f97316] m-0">SEAL</h1>
        </a>
    </div>

    <nav class="px-4 space-y-2 flex-1 overflow-y-auto custom-scrollbar pb-4">
        {#each menuItems as item}
            {#if item.children}
                <div>
                    <button onclick={() => toggleMenu(item.label)} class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-all text-left cursor-pointer {theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]'}">
                        <span class="flex items-center gap-3">
                            <svelte:component this={item.icon} class="w-5 h-5" />
                            {item.label}
                        </span>
                        <svg class="w-4 h-4 transition-transform duration-200 {expandedMenus[item.label] ? 'rotate-180' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
                    </button>
                    
                    {#if expandedMenus[item.label]}
                        <div class="pl-8 mt-1 space-y-1">
                            {#each item.children as child}
                                {@const isChildActive = String($page.url.pathname) === child.href}
                                <a href={child.href} class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {isChildActive ? (theme.darkMode ? 'text-orange-400 font-semibold bg-orange-950/20' : 'text-[#ea580c] font-semibold bg-[#ffedd5]/50') : (theme.darkMode ? 'text-zinc-400 hover:text-orange-400' : 'text-gray-650 hover:bg-[#fff2e8] hover:text-[#ea580c]')}">
                                    {child.label}
                                </a>
                            {/each}
                        </div>
                    {/if}
                </div>
            {:else}
                {@const isActive = item.href ? String($page.url.pathname) === item.href : false}
                <a href={item.href || '#'} class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {isActive ? (theme.darkMode ? 'bg-orange-950/40 text-orange-400 font-semibold' : 'bg-[#ffedd5] text-[#ea580c] font-semibold shadow-sm') : (theme.darkMode ? 'text-zinc-400 hover:bg-zinc-800 hover:text-orange-400' : 'text-gray-600 hover:bg-[#fff2e8] hover:text-[#ea580c]')}">
                    <svelte:component this={item.icon} class="w-5 h-5" />
                    {item.label}
                </a>
            {/if}
        {/each}
    </nav>

    <div class="p-4 border-t mt-auto shrink-0 {theme.darkMode ? 'border-zinc-800' : 'border-[#ffe8d6]/50'}">
        <a href="/auth/login" onclick={handleLogout} class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm transition-all text-[#ef4444] font-medium {theme.darkMode ? 'hover:bg-red-950/20' : 'hover:bg-red-50'}">
            <LogOut class="w-4 h-4 shrink-0" />
            <span class="truncate">Logout</span>
        </a>
    </div>
</aside>

<style>
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
