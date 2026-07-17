<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import Sidebar from "$lib/components/common/Sidebar.svelte"
	import { LayoutDashboard, User, Settings, Users, UploadCloud } from "@lucide/svelte"

	let { children } = $props()
	let profile = $state<any>(null)
	let isLoading = $state(true)

	const menuItems = $derived.by(() => {
		const baseItems = [
			{ href: "/student", label: "Dashboard", icon: LayoutDashboard },
			{ href: "/student/profile", label: "Profile", icon: User }
		]
		if (profile?.status !== "PENDING") {
			baseItems.push({ href: "/student/teams", label: "Teams", icon: Users })
		}
		baseItems.push({ href: "#", label: "Settings", icon: Settings })
		return baseItems
	})

	onMount(async () => {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (!profileRes?.ok || !profileData) {
				goto("/auth/login")
				return
			}
			if (profileData.role !== "STUDENT") {
				goto("/")
				return
			}
			profile = profileData
		} catch (err) {
			goto("/auth/login")
			return
		} finally {
			isLoading = false
		}
	})
</script>

{#if isLoading}
	<div class="flex justify-center items-center h-screen bg-(--md-surface)">
		<div
			class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"
		></div>
	</div>
{:else if profile?.status === "BANNED"}
	<div
		class="flex flex-col justify-center items-center h-screen bg-(--md-surface) text-(--md-on-surface) p-6 text-center"
	>
		<svg
			class="w-16 h-16 text-(--md-error) mb-4 animate-bounce"
			fill="none"
			stroke="currentColor"
			viewBox="0 0 24 24"
		>
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
			/>
		</svg>
		<h1 class="text-2xl font-black text-(--md-error)">Access Denied</h1>
		<p class="mt-2 max-w-md text-sm text-(--md-on-surface-variant)">
			This student account has been banned by the coordinator. If you believe this is a mistake,
			please contact administration.
		</p>
		<a
			href="/auth/login"
			onclick={() => localStorage.removeItem("auth_data")}
			class="mt-6 px-5 py-2.5 bg-(--md-error) text-(--md-on-error) hover:opacity-90 active:scale-98 rounded-xl text-sm font-bold border-0 transition-all decoration-none"
		>
			Return to Login
		</a>
	</div>
{:else}
	<div
		class="font-sans min-h-screen transition-colors duration-300 bg-(--md-surface) text-(--md-on-surface)"
	>
		<Sidebar {menuItems} />

		<main
			class="md:ml-64 flex-1 flex flex-col min-h-screen transition-colors duration-300 bg-(--md-surface)"
		>
			<div class="p-4 md:p-6 w-full pt-[90px]">
				{#if profile?.status === "PENDING"}
					<div
						class="mb-6 p-4 rounded-2xl border bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)/25 flex items-center gap-3"
					>
						<svg
							class="w-6 h-6 shrink-0 animate-pulse text-(--md-error)"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
							/>
						</svg>
						<div>
							<h4 class="font-bold text-sm">Account Pending Approval</h4>
							<p class="text-xs opacity-90 mt-0.5">
								Your student account is currently pending coordinator approval. You will be able to
								create or join teams once approved.
							</p>
						</div>
					</div>
				{/if}
				{@render children()}
			</div>
		</main>
	</div>
{/if}
