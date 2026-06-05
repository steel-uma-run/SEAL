<script lang="ts">
	import "./layout.css"

	import { primaryColor } from "$lib/theme.svelte"
	import { argbFromHex, Hct, hexFromArgb, SchemeVibrant } from "@poupe/material-color-utilities"

	let { children } = $props()

	// The things one would do to reduce reliance on JavaScript
	const styleElem = $derived(() => {
		const scheme = new SchemeVibrant(Hct.fromInt(argbFromHex(primaryColor)), false, 0.3)
		const entries = new Array<string>()

		for (const dynamicColor of scheme.colors.allColors) {
			const token = dynamicColor.name.replaceAll("_", "-")
			const value = hexFromArgb(dynamicColor.getArgb(scheme))
			entries.push(`--md-sys-color-${token}: ${value};`)
		}

		const str = `<style>:root {${entries.join("\n")}}`
		return str
	})
</script>

{@render children()}

<svelte:head>
	<link rel="preconnect" href="https://rsms.me/" />
	<link rel="stylesheet" href="https://rsms.me/inter/inter.css" />

	{@html styleElem()}
</svelte:head>
