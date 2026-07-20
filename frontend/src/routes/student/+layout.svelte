<script lang="ts">
	import Sidebar from "$lib/components/common/Sidebar.svelte"

	let { children } = $props()
	let profile = $state<any>(null)
	let isLoading = $state(false)

	// onMount(async () => {
	// 	try {
	// 		const { data: profileData, response: profileRes } = await getSelfProfile({
	// 			throwOnError: false
	// 		})
	// 		if (!profileRes?.ok || !profileData) {
	// 			goto("/auth/login")
	// 			return
	// 		}
	// 		if (profileData.role !== "STUDENT") {
	// 			goto("/")
	// 			return
	// 		}
	// 		profile = profileData
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
			<Sidebar role="STUDENT" />
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
