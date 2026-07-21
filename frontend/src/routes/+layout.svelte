<script lang="ts">
	import "./layout.css"

	import { argbFromHex, Hct, hexFromArgb, SchemeRainbow } from "@poupe/material-color-utilities"
	import { theme } from "$lib/theme.svelte"
	import { client } from "$lib/api/client.gen"

	import Header from "$lib/components/common/Header.svelte"

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
			entries.push(`--md-sys-color-${token}: ${value};`)
			entries.push(`--m3c-${token}: ${value};`)
		}

		const str = `<style>:root {${entries.join("\n")}}</style>`
		return str
	})

	client.interceptors.request.use((request) => {
		if (typeof window !== "undefined") {
			const url = new URL(request.url)
			if (url.pathname.endsWith("/auth/login") || url.pathname.endsWith("/auth/register")) {
				return request
			}

			const data = localStorage.getItem("auth_data")
			if (data) {
				let token = ""
				try {
					const parsed = JSON.parse(data)
					token = parsed.token || parsed
				} catch {
					token = data
				}
				if (token && token !== "undefined" && token !== "null") {
					request.headers.set("Authorization", `Bearer ${token}`)
				}
			}
		}
		return request
	})

	client.setConfig({ baseUrl: "http://localhost:8080" })
</script>

<svelte:head>
	<link rel="preconnect" href="https://rsms.me/" />
	<link rel="stylesheet" href="https://rsms.me/inter/inter.css" />

	{@html styleElem}
</svelte:head>

<Header />

<main>
	{@render children()}
</main>

<style lang="scss">
	main {
		background-color: var(--md-sys-color-background);
	}
</style>
