<script lang="ts">
	import type { Snippet } from "svelte"

	interface Props {
		children: Snippet
	}

	const { children }: Props = $props()
</script>

<div>
	{@render children()}
</div>

<style lang="scss">
	div {
		border-radius: 12px;
		background-color: var(--md-sys-color-surface-container-low);
		box-shadow: 0 2px 5px -2px #000000aa;
		overflow: hidden;
	}
</style>
