<script lang="ts">
	import { themeState } from "../stores/theme.svelte"
	import type { ThemeType } from "../stores/theme.svelte"

	let currentTheme = $derived(themeState.value)

	function toggleTheme() {
		const sequence: Record<ThemeType, ThemeType> = {
			light: "dark",
			dark: "system",
			system: "light"
		}
		themeState.setTheme(sequence[currentTheme])
	}
</script>

<button
	onclick={toggleTheme}
	class="flex items-center justify-center p-2 rounded-full transition-all duration-300 bg-gray-200/50 hover:bg-gray-300/80 dark:bg-white/5 dark:hover:bg-white/20 text-gray-600 dark:text-white hover:text-gray-900 dark:hover:text-white focus:outline-none backdrop-blur-md shadow-sm border border-transparent dark:border-white/5"
	aria-label="Toggle Dark Mode"
>
	{#if currentTheme === "light"}
		<svg
			class="w-5 h-5 transition-transform duration-300 hover:rotate-12"
			fill="none"
			stroke="currentColor"
			viewBox="0 0 24 24"
		>
			<!-- Moon Icon -->
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
			/>
		</svg>
	{:else if currentTheme === "dark"}
		<svg
			class="w-5 h-5 transition-transform duration-300 hover:rotate-45"
			fill="none"
			stroke="currentColor"
			viewBox="0 0 24 24"
		>
			<!-- Sun Icon -->
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
			/>
		</svg>
	{:else}
		<svg
			class="w-5 h-5 transition-transform duration-300"
			fill="none"
			stroke="currentColor"
			viewBox="0 0 24 24"
		>
			<!-- System Icon -->
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
			/>
		</svg>
	{/if}
</button>
