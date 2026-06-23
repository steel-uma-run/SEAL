<script lang="ts">
	import "./layout.css"

	import { argbFromHex, Hct, hexFromArgb, SchemeRainbow } from "@poupe/material-color-utilities"

	import { theme } from "$lib/theme.svelte"
	import Header from "$lib/components/common/Header.svelte"
	import { client } from "$lib/api/client.gen"
	import { dev } from "$app/environment"

	let { children } = $props()

	// The things one would do to reduce reliance on JavaScript
	const styleElem = $derived.by(() => {
		const scheme = new SchemeRainbow(
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

	if (dev) {
		client.setConfig({
			baseUrl: "http://localhost:8080"
		})
	}
</script>

<svelte:head>
	<link rel="preconnect" href="https://rsms.me/" />
	<link rel="stylesheet" href="https://rsms.me/inter/inter.css" />

	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Icons" />

	<style>
		:root {
			color: var(--md-on-surface);
		}
	</style>

	{@html styleElem}
</svelte:head>

<Header />

<main class="bg-(--md-surface)">
	{@render children()}
</main>
