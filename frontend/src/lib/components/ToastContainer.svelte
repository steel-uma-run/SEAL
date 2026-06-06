<script lang="ts">
	import { toast } from "./toast.svelte"
	import { fade, slide } from "svelte/transition"
</script>

<div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
	{#each toast.toasts as t (t.id)}
		<div
			in:slide={{ duration: 200 }}
			out:fade={{ duration: 200 }}
			class="pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-sm border w-max min-w-[250px] max-w-sm"
			class:bg-green-500:20={t.type === "success"}
			class:border-green-500:30={t.type === "success"}
			class:text-green-900={t.type === "success"}
			class:dark:text-green-100={t.type === "success"}
			class:bg-red-500:20={t.type === "error"}
			class:border-red-500:30={t.type === "error"}
			class:text-red-900={t.type === "error"}
			class:dark:text-red-100={t.type === "error"}
			class:bg-blue-500:20={t.type === "info"}
			class:border-blue-500:30={t.type === "info"}
			class:text-blue-900={t.type === "info"}
			class:dark:text-blue-100={t.type === "info"}
			style="background: {t.type === 'success'
				? 'rgba(34, 197, 94, 0.1)'
				: t.type === 'error'
					? 'rgba(239, 68, 68, 0.1)'
					: 'rgba(59, 130, 246, 0.1)'}; border-color: {t.type === 'success'
				? 'rgba(34, 197, 94, 0.2)'
				: t.type === 'error'
					? 'rgba(239, 68, 68, 0.2)'
					: 'rgba(59, 130, 246, 0.2)'}"
		>
			{#if t.type === "success"}
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="w-5 h-5 text-green-500"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
						clip-rule="evenodd"
					/>
				</svg>
			{:else if t.type === "error"}
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="w-5 h-5 text-red-500"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
						clip-rule="evenodd"
					/>
				</svg>
			{:else}
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="w-5 h-5 text-blue-500"
					viewBox="0 0 20 20"
					fill="currentColor"
				>
					<path
						fill-rule="evenodd"
						d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
						clip-rule="evenodd"
					/>
				</svg>
			{/if}
			<span class="text-sm font-medium">{t.message}</span>
			<button class="ml-auto opacity-70 hover:opacity-100" onclick={() => toast.remove(t.id)}>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="h-4 w-4"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M6 18L18 6M6 6l12 12"
					/>
				</svg>
			</button>
		</div>
	{/each}
</div>
