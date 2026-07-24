<script lang="ts">
	import type { Snippet } from "svelte"

	interface Props {
		children: Snippet
	}

	const { children }: Props = $props()
</script>

<div class="elevated-card">
	{@render children()}
</div>

<style lang="scss">
	.elevated-card {
		border-radius: 12px;
		background-color: var(--md-sys-color-surface-container-low);
		/* Làm bóng đổ mềm mại và chuyên nghiệp hơn */
		box-shadow: 0 2px 8px -2px rgba(0, 0, 0, 0.1);
		overflow: hidden;
		display: flex;
		flex-direction: column;
		transition:
			transform 0.2s ease,
			box-shadow 0.2s ease;

		&:hover {
			transform: translateY(-4px);
			box-shadow: 0 6px 16px -4px rgba(0, 0, 0, 0.15);
		}
	}
</style>
