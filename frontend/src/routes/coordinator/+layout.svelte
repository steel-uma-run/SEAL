<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import Sidebar from "$lib/components/common/Sidebar.svelte"

	let { children } = $props()
	let isLoading = $state(true)

	const menuItems = [
		{ href: "/coordinator", label: "Dashboard" },
		{
			label: "Management",
			children: [
				{ href: "/coordinator/seasons", label: "Seasons & Events" },
				{ href: "/coordinator/teams", label: "Teams" },
				{ href: "/coordinator/experts", label: "Lecturers" },
				{ href: "/coordinator/users", label: "Users" },
				{ href: "/coordinator/templates", label: "Template" }
			]
		},
		{ href: "/coordinator/profile", label: "Profile" },
		{ href: "/coordinator/settings", label: "Settings" }
	]

	onMount(async () => {
		try {
			const { data: profile, response: profileRes } = await getSelfProfile({ throwOnError: false })
			if (!profileRes?.ok || !profile) {
				return
			}
			if (profile.role !== "COORDINATOR") {
				// If logged in but not coordinator, send to home
				return
			}
		} catch (err) {
			return
		} finally {
			isLoading = false
		}
	})
</script>

{#if isLoading}
	<p>Loading...</p>
{:else}
	<div class="main">
		<aside>
			<Sidebar role="COORDINATOR" />
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
