<script lang="ts">
	import type { Snippet } from "svelte"

	type Direction = "left-right" | "top-bottom"

	interface Props {
		children: Snippet
		direction?: Direction
		header?: Snippet
	}

	const { header, direction = "top-bottom", children }: Props = $props()
</script>

<div
	class="flex {direction == 'top-bottom'
		? 'flex-col'
		: 'flex-row'} shadow-sm/50 rounded-xl bg-(--md-surface-container-low)"
>
	{#if header}
		{@render header()}
	{/if}

	<div class="px-4 py-3">
		{@render children()}
	</div>
</div>
