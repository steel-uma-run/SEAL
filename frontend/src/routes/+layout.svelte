<script lang="ts">
    import "./layout.css"

    import { theme } from "$lib/theme.svelte"
    import { argbFromHex, Hct, hexFromArgb, SchemeNeutral } from "@poupe/material-color-utilities"
    import Header from "$lib/components/common/Header.svelte"

    let { children } = $props()

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
    <Header />

    <main class="pt-[70px] flex-1">
        {@render children()}
    </main>
</div>