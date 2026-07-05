<script lang="ts">
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import { theme } from "$lib/theme.svelte"
	import { LogOut } from "@lucide/svelte"

	interface MenuItem {
		href?: string
		label: string
		icon: any // Lucide icon component
		children?: { href: string; label: string }[]
	}

	let { menuItems = [] } = $props<{ menuItems: MenuItem[] }>()

	// Manage expansion states for collapsible menus
	let expandedMenus = $state<Record<string, boolean>>({
		Management: true
	})

	function toggleMenu(label: string) {
		expandedMenus[label] = !expandedMenus[label]
	}

	function handleLogout(event: Event) {
		event.preventDefault()
		if (typeof window !== "undefined") {
			localStorage.removeItem("auth_data")
		}
		goto("/auth/login")
	}
</script>

<aside
	class="fixed top-14 left-0 z-40 w-64 flex flex-col hidden md:flex h-[calc(100vh-3.5rem)] shrink-0 border-r border-(--md-outline-variant) bg-(--md-surface-container-low) transition-colors duration-300"
>
	<nav class="px-4 space-y-2 flex-1 overflow-y-auto custom-scrollbar pb-4 mt-4">
		{#each menuItems as item}
			{#if item.children}
				<div>
					<button
						onclick={() => toggleMenu(item.label)}
						class="w-full flex items-center justify-between px-4 py-3 rounded-xl transition-all text-left cursor-pointer text-(--md-on-surface-variant) hover:bg-(--md-surface-container-high) hover:text-(--md-on-surface)"
					>
						<span class="flex items-center gap-3">
							<svelte:component this={item.icon} class="w-5 h-5" />
							{item.label}
						</span>
						<svg
							class="w-4 h-4 transition-transform duration-200 {expandedMenus[item.label]
								? 'rotate-180'
								: ''}"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M19 9l-7 7-7-7"
							></path></svg
						>
					</button>

					{#if expandedMenus[item.label]}
						<div class="pl-8 mt-1 space-y-1">
							{#each item.children as child}
								{@const isChildActive = String($page.url.pathname) === child.href}
								<a
									href={child.href}
									class="flex items-center gap-3 px-4 py-2 rounded-lg text-sm transition-all {isChildActive
										? 'text-(--md-primary) font-semibold bg-(--md-primary-container)'
										: 'text-(--md-on-surface-variant) hover:bg-(--md-surface-container-high) hover:text-(--md-on-surface)'}"
								>
									{child.label}
								</a>
							{/each}
						</div>
					{/if}
				</div>
			{:else}
				{@const isActive = item.href ? String($page.url.pathname) === item.href : false}
				<a
					href={item.href || "#"}
					class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all {isActive
						? 'bg-(--md-secondary-container) text-(--md-on-secondary-container) font-semibold'
						: 'text-(--md-on-surface-variant) hover:bg-(--md-surface-container-high) hover:text-(--md-on-surface)'}"
				>
					<svelte:component this={item.icon} class="w-5 h-5" />
					{item.label}
				</a>
			{/if}
		{/each}
	</nav>

	<div class="p-4 border-t mt-auto shrink-0 border-(--md-outline-variant)">
		<a
			href="/auth/login"
			onclick={handleLogout}
			class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm transition-all text-(--md-error) font-medium hover:bg-(--md-error-container)/20"
		>
			<LogOut class="w-4 h-4 shrink-0" />
			<span class="truncate">Logout</span>
		</a>
	</div>
</aside>

<style>
	.custom-scrollbar::-webkit-scrollbar {
		width: 6px;
	}
	.custom-scrollbar::-webkit-scrollbar-track {
		background: transparent;
	}
	.custom-scrollbar::-webkit-scrollbar-thumb {
		background: #e5e7eb;
		border-radius: 10px;
	}
	.custom-scrollbar::-webkit-scrollbar-thumb:hover {
		background: #d1d5db;
	}
</style>
