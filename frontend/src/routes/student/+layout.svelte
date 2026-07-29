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
