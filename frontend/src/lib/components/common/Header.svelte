<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { page } from "$app/stores"
	import { goto } from "$app/navigation"
	import { getProfile } from "$lib/api/profile"

	let isLoggedIn = $state(false)
	let logoHref = $state("/")

	$effect(() => {
		// Track page changes to check if auth_data is present in localStorage
		const _url = $page.url
		if (typeof window !== "undefined") {
			const authData = localStorage.getItem("auth_data")
			isLoggedIn = !!authData
			
			if (isLoggedIn && logoHref === "/") {
				getProfile().then(res => {
					if (res.ok) {
						res.json().then(profile => {
							if (profile.role === "COORDINATOR") logoHref = "/coordinator"
							else if (profile.role === "STUDENT") logoHref = "/student"
							else if (profile.role === "MENTOR" || profile.role === "JUDGE") logoHref = "/mentor"
						})
					}
				}).catch(() => {})
			} else if (!isLoggedIn) {
				logoHref = "/"
			}
		}
	})
</script>

<header
	class="fixed top-0 left-0 w-full z-[1000] flex justify-between items-center px-8 py-4 bg-[var(--md-surface-container)] border-b border-[var(--md-outline-variant)] transition-colors duration-300 ease-in-out"
>
	<div class="text-2xl font-bold text-[var(--md-primary)]">
		<a href={logoHref} class="flex items-center gap-2.5 no-underline">
			<img
				src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
				alt="Logo"
				class="h-9 w-auto object-contain block"
			/>
			<p class="text-[#f54901] text-[2rem] font-bold m-0">SEAL</p>
		</a>
	</div>

	<div class="flex items-center gap-5">
		<button
			class="px-4 py-2 font-semibold rounded-lg bg-[var(--md-primary)] text-[var(--md-on-primary)] transition-opacity duration-200 hover:opacity-80 hover:cursor-pointer"
			onclick={() => (theme.darkMode = !theme.darkMode)}
		>
			{theme.darkMode ? "Light Mode" : "Dark Mode"}
		</button>
		{#if !isLoggedIn}
			<a
				href="/auth/register"
				class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-[#f26f21] text-white border-2 border-[#f26f21] shadow-[0_2px_4px_rgba(242,111,33,0.2)] hover:bg-[#d85c14] hover:border-[#d85c14]"
			>
				Register
			</a>
			<a
				href="/auth/login"
				class="px-5 py-2 rounded-lg font-semibold text-[0.95rem] transition-all duration-200 ease-in-out bg-white text-[#f26f21] border-2 border-[#f26f21] hover:bg-[#fff0e8]"
			>
				Login
			</a>
		{/if}
	</div>
</header>
