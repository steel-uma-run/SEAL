<script lang="ts">
	import type { HackathonEvent } from "$lib/api"
	import { Button, Icon } from "m3-svelte"
	import iconArrowForward from "@ktibow/iconset-material-symbols/arrow-forward"

	import ElevatedCard from "./ElevatedCard.svelte"

	interface Props {
		event: HackathonEvent
		href?: string
	}

	const { event, href }: Props = $props()
</script>

<ElevatedCard>
	<div class="content">
		<div class="image-container">
			<img
				src="https://images.unsplash.com/photo-1631350397792-8e0c2de5b637?q=80&w=870&auto=format&fit=crop"
				alt="Event thumbnail"
			/>
		</div>

		<div class="body">
			<p class="name">{event.name}</p>
			<p class="desc">{event.description}</p>

			<div class="action-container">
				<Button variant="tonal" iconType="left" href={href || `/events/${event.id}`}>
					<Icon icon={iconArrowForward} /> Details
				</Button>
			</div>
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
		flex-grow: 1;
		height: 100%;
	}

	.image-container {
		width: 100%;
		height: 180px;
		max-height: 200px;
		overflow: hidden;
	}

	img {
		width: 100%;
		height: 100%;
		object-fit: cover;
		transition: transform 0.3s ease;
	}

	.content:hover img {
		transform: scale(1.04);
	}

	.body {
		padding: 20px 16px;
		display: flex;
		flex-direction: column;
		flex: 1 1 auto;
		height: 100%;
	}

	.name {
		font-weight: 600;
		font-size: 1.1rem;
		margin-bottom: 8px;
		line-height: 1.4;
		height: 2.8em;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
		color: var(--md-sys-color-on-surface);
	}

	.desc {
		opacity: 70%;
		font-size: 0.9rem;
		margin-bottom: 1.5rem;
		line-height: 1.5;
		height: 4.5em;
		display: -webkit-box;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.action-container {
		margin-top: auto;
		padding-top: 1rem;
		display: flex;
		justify-content: flex-end;
	}
</style>
