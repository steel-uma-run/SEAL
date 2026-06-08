<script lang="ts">
	import "./layout.css"

	import { theme } from "$lib/theme.svelte"
	import { argbFromHex, Hct, hexFromArgb, SchemeNeutral } from "@poupe/material-color-utilities"

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

<div class="app-container">
    <header class="app-header">
        <div class="logo">My Startpage</div>
        
        <button class="theme-btn" onclick={() => (theme.darkMode = !theme.darkMode)}>
            {theme.darkMode ? 'Light Mode' : 'Dark Mode'}
        </button>
    </header>

    <main class="app-main">
        {@render children()}
    </main>
</div>

<style>
	.app-container{
		display: flex;
		flex-direction: column;
		min-height: 100vh;
	}

	.app-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 2rem;
        background-color: var(--md-surface-container);
        border-bottom: 1px solid var(--md-outline-variant);
        transition: background-color 0.3s ease;
    }

	.logo{
		font-size: 1.5rem;
        font-weight: 700;
        color: var(--md-primary);
	}

	.theme-btn {
        padding: 0.5rem 1rem;
        background-color: var(--md-primary);
        color: var(--md-on-primary);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        font-weight: 600;
        transition: opacity 0.2s ease;
    }

    .theme-btn:hover {
        opacity: 0.8;
    }

</style>
