<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { Button, Icon } from "m3-svelte"

	import iconDarkMode from "@ktibow/iconset-material-symbols/dark-mode"
	import iconLightMode from "@ktibow/iconset-material-symbols/light-mode"
</script>

<header>
	<div>
		<a href={"/"} class="logo">
			<img src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg" alt="Logo" />
			<p>SEAL</p>
		</a>
	</div>

	<div>
		<Button variant="tonal" onclick={() => (theme.darkMode = !theme.darkMode)}>
			<Icon icon={theme.darkMode ? iconDarkMode : iconLightMode} />
		</Button>
	</div>
</header>

<style lang="scss">
	* {
		margin: 0;
	}

	header {
		position: sticky;
		top: 0;
		z-index: 999;
		height: 3rem;
		padding: 0.2rem 0.5rem;
		display: flex;
		justify-content: space-between;
		align-items: center;

		background-color: var(--md-sys-color-surface-container);
		border-bottom: 1px solid var(--md-sys-color-outline-variant);
	}

	.logo {
		display: flex;
		align-items: center;
		text-decoration: none;
		gap: 0.5rem;
		font-size: 2rem;
		font-weight: bold;
		color: var(--md-sys-color-primary);

		> img {
			height: 2rem;
		}
	}
</style>
