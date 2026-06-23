<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getProfile } from "$lib/api/profile"
	import { theme } from "$lib/theme.svelte"
	import Sidebar from "$lib/components/common/Sidebar.svelte"
	import { LayoutDashboard, FolderKanban, User } from "@lucide/svelte"

	let { children } = $props()
	let isLoading = $state(true)

	const menuItems = [
		{ href: "/coordinator", label: "Dashboard", icon: LayoutDashboard },
		{
			label: "Management",
			icon: FolderKanban,
			children: [
				{ href: "/coordinator/seasons", label: "Seasons & Events" },
				{ href: "/coordinator/teams", label: "Teams" },
				{ href: "/coordinator/experts", label: "Judges & Mentors" },
				{ href: "/coordinator/users", label: "Users" }
			]
		},
		{ href: "#", label: "Profile", icon: User }
	]

	onMount(async () => {
		try {
			const profileRes = await getProfile()
			if (!profileRes.ok) {
				goto("/auth/login")
				return
			}
			const profile = await profileRes.json()
			if (profile.role !== "COORDINATOR") {
				// If logged in but not coordinator, send to home
				goto("/")
				return
			}
		} catch (err) {
			goto("/auth/login")
			return
		} finally {
			isLoading = false
		}
	})
</script>

{#if isLoading}
	<div class="flex justify-center items-center h-screen bg-zinc-950">
		<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"></div>
	</div>
{:else}
	<div
		class="font-sans min-h-screen transition-colors duration-300 {theme.darkMode
			? 'bg-zinc-950 text-zinc-100'
			: 'bg-[#f8f9fa] text-gray-800'}"
	>
		<Sidebar {menuItems} />

		<main
			class="md:ml-64 flex-1 flex flex-col min-h-screen transition-colors duration-300 {theme.darkMode
				? 'bg-zinc-950'
				: 'bg-white'}"
		>
			<div class="p-4 md:p-6 w-full pt-[90px]">
				{@render children()}
			</div>
		</main>
	</div>
{/if}
