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

		// Explicit overrides to achieve pure "Orange/White" and "Orange/Black" themes
		// We explicitly override primary colors to bypass Material Design's desaturation (muddy colors)
		entries.push(`--md-primary: #F26F21;`)
		entries.push(`--md-on-primary: #ffffff;`)

		if (theme.darkMode) {
			// Black Backgrounds
			entries.push(`--md-background: #000000;`)
			entries.push(`--md-surface: #000000;`)
			entries.push(`--md-surface-container-lowest: #000000;`)
			entries.push(`--md-surface-container-low: #0a0a0a;`)
			entries.push(`--md-surface-container: #141414;`)
			entries.push(`--md-surface-container-high: #1f1f1f;`)
			entries.push(`--md-surface-container-highest: #292929;`)
			// Vibrant Dark Orange Accents
			entries.push(`--md-primary-container: #472008;`)
			entries.push(`--md-on-primary-container: #ffcbb0;`)
			entries.push(`--md-secondary-container: #3d2314;`)
			entries.push(`--md-on-secondary-container: #ffcdb3;`)
		} else {
			// White Backgrounds
			entries.push(`--md-background: #ffffff;`)
			entries.push(`--md-surface: #ffffff;`)
			entries.push(`--md-surface-container-lowest: #ffffff;`)
			entries.push(`--md-surface-container-low: #f7f7f7;`)
			entries.push(`--md-surface-container: #eeeeee;`)
			entries.push(`--md-surface-container-high: #e6e6e6;`)
			entries.push(`--md-surface-container-highest: #dddddd;`)
			// Vibrant Light Orange Accents
			entries.push(`--md-primary-container: #ffede3;`)
			entries.push(`--md-on-primary-container: #c44e00;`)
			entries.push(`--md-secondary-container: #fff4ed;`)
			entries.push(`--md-on-secondary-container: #d65900;`)
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
