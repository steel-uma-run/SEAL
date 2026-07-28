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
		min-height: calc(100vh - 3rem);
		display: flex;

		main {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding: 2rem 3.5rem;
			margin-left: 96px;
			width: calc(100% - 96px);
			box-sizing: border-box;

			@media (max-width: 768px) {
				padding: 1.25rem 1.5rem;
				margin-left: 80px;
				width: calc(100% - 80px);
			}
		}
	}

	aside {
		position: fixed;
		top: 3rem;
		left: 0;
		width: 96px;
		height: calc(100vh - 3rem);
		z-index: 999;
	}
</style>
