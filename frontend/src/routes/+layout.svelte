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

<button onclick={() => (theme.darkMode = !theme.darkMode)}>Fuck</button>

{@render children()}
