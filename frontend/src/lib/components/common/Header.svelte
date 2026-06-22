<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    import { page } from "$app/stores";
    import { goto } from "$app/navigation";

    let isLoggedIn = $state(false);
    let profileRole = $state("");

    $effect(() => {
        // Track page changes to check if auth_data is present in localStorage
        const _url = $page.url;
        if (typeof window !== "undefined") {
            const data = localStorage.getItem("auth_data");
            isLoggedIn = !!data;
            if (data) {
                try {
                    const parsed = JSON.parse(data);
                    if (parsed.user && parsed.user.role) {
                        profileRole = parsed.user.role;
                    }
                } catch (e) {}
            }
        }
    });
</script>

<header class="fixed top-0 left-0 w-full z-[1000] flex justify-between items-center px-8 py-4 bg-[var(--md-surface-container)] border-b border-[var(--md-outline-variant)] transition-colors duration-300 ease-in-out">
    <div class="text-2xl font-bold text-[var(--md-primary)]">
        <a href={isLoggedIn ? (profileRole === "COORDINATOR" ? "/coordinator" : (profileRole === "LECTURER" || profileRole === "JUDGE" ? "/lecturer" : (profileRole === "STUDENT" ? "/student" : "/"))) : "/"} class="flex items-center gap-2.5 no-underline hover:opacity-80 transition-opacity">
            <img
                src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
                alt="Logo"
                class="h-9 w-auto object-contain block"
            />
            <p class="text-[#f54901] text-[2rem] font-bold m-0">SEAL</p>
        </a>
    </div>

    <div class="flex items-center gap-5">
        <button
            class="p-2 rounded-full transition-all duration-300 hover:scale-110 hover:shadow-md hover:cursor-pointer border {theme.darkMode ? 'bg-zinc-800 text-orange-400 border-zinc-700' : 'bg-white text-orange-500 border-orange-200 hover:border-orange-500'}"
            onclick={() => (theme.darkMode = !theme.darkMode)}
            aria-label="Toggle theme"
        >
            {#if theme.darkMode}
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
            {:else}
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path></svg>
            {/if}
        </button>
        {#if !isLoggedIn}
            <a href="/auth/register" class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-[#f26f21] text-white border-2 border-[#f26f21] shadow-[0_2px_4px_rgba(242,111,33,0.2)] hover:bg-[#d85c14] hover:border-[#d85c14]">
                Register
            </a>
            <a href="/auth/login" class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-white text-[#f26f21] border-2 border-[#f26f21] hover:bg-[#fff0e8]">
                Login
            </a>
        {/if}
    </div>
</header>
