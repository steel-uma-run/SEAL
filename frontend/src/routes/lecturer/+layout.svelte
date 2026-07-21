<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile } from "$lib/api"
	import Sidebar from "$lib/components/common/Sidebar.svelte"

	let { children } = $props()
	let isLoading = $state(false)

	// onMount(async () => {
	// 	try {
	// 		const { data: profile, response: profileRes } = await getSelfProfile({ throwOnError: false })
	// 		if (!profileRes?.ok || !profile) {
	// 			goto("/auth/login")
	// 			return
	// 		}
	// 		if (profile.role !== "LECTURER") {
	// 			goto("/")
	// 			return
	// 		}
	// 	} catch (err) {
	// 		goto("/auth/login")
	// 		return
	// 	} finally {
	// 		isLoading = false
	// 	}
	// })
</script>

{#if isLoading}
	<p>Loading...</p>
{:else}
	<div class="main">
		<aside>
			<Sidebar role="LECTURER" />
		</aside>

		<main>
			{@render children()}
		</main>
	</div>
{/if}

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
