<script lang="ts">
    import "./layout.css"

    import { theme } from "$lib/theme.svelte"
    import { argbFromHex, Hct, hexFromArgb, SchemeNeutral } from "@poupe/material-color-utilities"
    import { page } from "$app/stores"
    import { goto } from "$app/navigation"

    let { children } = $props()

    let isLoggedIn = $state(false)

    $effect(() => {
        // Track page changes to check if auth_data is present in localStorage
        const _url = $page.url
        if (typeof window !== "undefined") {
            isLoggedIn = !!localStorage.getItem("auth_data")
        }
    })

    function handleLogout() {
        if (typeof window !== "undefined") {
            localStorage.removeItem("auth_data")
            isLoggedIn = false
            goto("/")
        }
    }

    // The things one would do to reduce reliance on JavaScript
    const styleElem = $derived.by(() => {
        const scheme = new SchemeNeutral(
            Hct.fromInt(argbFromHex(theme.primaryColor)),
            theme.darkMode,
            0
        )
        const entries = new Array<string>()

        for (const dynamicColor of scheme.colors.allColors) {
            const token = dynamicColor.name.replaceAll("_", "-")
            const value = hexFromArgb(dynamicColor.getArgb(scheme))
            entries.push(`--md-${token}: ${value};`)
        }

        const str = `<style>:root {${entries.join("\n")}}</style>`
        return str
    })
</script>

<svelte:head>
    <link rel="preconnect" href="https://rsms.me/" />
    <link rel="stylesheet" href="https://rsms.me/inter/inter.css" />

    <style>
        :root {
            color: var(--md-on-surface);
        }
    </style>

    {@html styleElem}
</svelte:head>

<div class="flex flex-col min-h-screen bg-[var(--md-surface)] transition-colors duration-300 ease-in-out" data-theme={theme.darkMode ? "dark" : "light"}>
    <header class="fixed top-0 left-0 w-full z-[1000] flex justify-between items-center px-8 py-4 bg-[var(--md-surface-container)] border-b border-[var(--md-outline-variant)] transition-colors duration-300 ease-in-out">
        <div class="text-2xl font-bold text-[var(--md-primary)]">
            <a href="/" class="flex items-center gap-2.5 no-underline">
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
                class="px-4 py-2 font-semibold rounded-lg bg-[var(--md-primary)] text-[var(--md-on-primary)] transition-opacity duration-200 hover:opacity-80"
                onclick={() => (theme.darkMode = !theme.darkMode)}
            >
                {theme.darkMode ? "Light Mode" : "Dark Mode"}
            </button>
            {#if isLoggedIn}
                <a href="/dashboard" class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-white text-[#f26f21] border-2 border-[#f26f21] hover:bg-[#fff0e8]">
                    Dashboard
                </a>
                <button
                    onclick={handleLogout}
                    class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-[#ef4444] text-white border-2 border-[#ef4444] shadow-[0_2px_4px_rgba(239,68,68,0.2)] hover:bg-[#dc2626] hover:border-[#dc2626] cursor-pointer"
                >
                    Logout
                </button>
            {:else}
                <a href="/auth/register" class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-[#f26f21] text-white border-2 border-[#f26f21] shadow-[0_2px_4px_rgba(242,111,33,0.2)] hover:bg-[#d85c14] hover:border-[#d85c14]">
                    Register
                </a>
                <a href="/auth/login" class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-white text-[#f26f21] border-2 border-[#f26f21] hover:bg-[#fff0e8]">
                    Login
                </a>
            {/if}
        </div>
    </header>

    <main class="pt-[70px] flex-1">
        {@render children()}
    </main>
</div>