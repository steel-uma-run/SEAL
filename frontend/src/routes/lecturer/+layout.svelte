<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { auth } from "$lib/auth.svelte"
	import Sidebar from "$lib/components/common/Sidebar.svelte"

	let { children } = $props()

	onMount(async () => {
		if (auth.value === undefined) {
			goto("/auth/login")
			return
		}
		if (auth.value.role !== "LECTURER") {
			goto("/")
			return
		}
	})
</script>

<div class="main">
	<aside>
		<Sidebar role="LECTURER" />
	</aside>

	<main>
		{@render children()}
	</main>
</div>

<style lang="scss">
	.main {
		min-height: 100vh;

		main {
			display: flex;
			flex-direction: column;
			padding: 1rem;
			margin-left: 96px;
		}
	}

	aside {
		position: fixed;
		height: 100vh;
		z-index: 999;
	}
</style>
