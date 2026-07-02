<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { page } from "$app/state"
	import { getSelfProfile } from "$lib/api/sdk.gen"
	import { Sun, Moon } from "@lucide/svelte"

</script>

<header
	class="sticky top-0 z-999 w-full h-14 flex justify-between items-center px-8 bg-(--md-surface-container) border-b border-(--md-outline-variant)"
>
	<div class="text-2xl font-bold text-(--md-primary)">
		<a href={"/coordinator"} class="flex items-center gap-2.5 no-underline">
			<img
				src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
				alt="Logo"
				class="h-9 w-auto object-contain block"
			/>
			<p class="text-[2rem] font-bold m-0">SEAL</p>
		</a>
	</div>

	<div class="flex items-center gap-5">
		<button
			class="p-2.5 rounded-full transition-all duration-200 hover:cursor-pointer flex items-center justify-center border border-(--md-outline-variant) shadow-sm"
			onclick={() => (theme.darkMode = !theme.darkMode)}
			title={theme.darkMode ? "Switch to Light Mode" : "Switch to Dark Mode"}
		>
			{#if theme.darkMode}
				<Moon class="w-5 h-5" />
			{:else}
				<Sun class="w-5 h-5" />
			{/if}
		</button>
		<!--
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
		-->
	</div>
</header>
