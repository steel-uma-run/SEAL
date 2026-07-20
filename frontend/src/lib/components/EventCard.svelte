<script lang="ts">
	import type { HackathonEvent } from "$lib/api"

	import ElevatedCard from "./ElevatedCard.svelte"

	import "@material/web/button/filled-tonal-button"

	interface Props {
		event: HackathonEvent
	}

	const { event }: Props = $props()
</script>

<ElevatedCard>
	<div class="content">
		<div>
			<img
				src="https://images.unsplash.com/photo-1631350397792-8e0c2de5b637?q=80&w=870&auto=format&fit=crop"
				alt="Shut up"
			/>
		</div>

		<div class="body">
			<p class="name">{event.name}</p>
			<p class="desc">{event.description}</p>

			<md-filled-tonal-button href="/events/{event.id}">
				<span>Details</span>
				<div slot="icon" style="display: contents">
					<span class="material-symbols-rounded">arrow_forward</span>
				</div>
			</md-filled-tonal-button>
		</div>
	</div>
</ElevatedCard>

<style lang="scss">
	* {
		margin: 0;
	}

	.content {
		display: flex;
		flex-direction: column;
	}

	.body {
		padding: 16px;
		flex: 1 1 auto;
	}

	.name {
		font-weight: bold;
	}

	.desc {
		opacity: 70%;
		font-size: 0.9rem;
		margin-bottom: 1rem;
	}

	img {
		width: 100%;
		height: auto;
	}
</style>
