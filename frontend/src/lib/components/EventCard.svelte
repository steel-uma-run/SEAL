<script lang="ts">
	import type { HackathonEvent } from "$lib/api"
	import { ArrowRight } from "@lucide/svelte"

	interface Props {
		event: HackathonEvent
		isJoined?: boolean
	}

	const { event, isJoined = false }: Props = $props()

	const defaultBanner =
		"https://images.unsplash.com/photo-1631350397792-8e0c2de5b637?q=80&w=870&auto=format&fit=crop"
</script>

<div class="event-card">
	<div class="event-card__banner">
		<img src={event.imageUrl || defaultBanner} alt={event.name} />
		{#if isJoined}
			<span class="event-card__badge-joined">Joined</span>
		{/if}
	</div>

	<div class="event-card__content">
		<h3 class="event-card__title">{event.name}</h3>
		<p class="event-card__desc">{event.description || "No description available."}</p>

		<div class="event-card__footer">
			<a href="/events/{event.id}" class="btn btn--details">
				Details <ArrowRight class="btn__icon" />
			</a>
		</div>
	</div>
</div>

<style lang="scss">
	.event-card {
		background: var(--md-sys-color-surface-container-low, #1e1e24);
		border: 1px solid var(--md-sys-color-outline-variant, rgba(255, 255, 255, 0.1));
		border-radius: 1.25rem;
		overflow: hidden;
		display: flex;
		flex-direction: column;
		height: 100%;
		box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
		transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

		&:hover {
			transform: translateY(-4px);
			box-shadow: 0 10px 25px rgba(0, 0, 0, 0.18);
			border-color: var(--md-sys-color-primary, #ff6b00);
		}

		&__banner {
			width: 100%;
			height: 160px;
			overflow: hidden;
			position: relative;
			background: var(--md-sys-color-surface-container-high, #2a2a32);

			img {
				width: 100%;
				height: 100%;
				object-fit: cover;
				transition: transform 0.3s ease;
			}

			&:hover img {
				transform: scale(1.05);
			}
		}

		&__badge-joined {
			position: absolute;
			top: 0.75rem;
			right: 0.75rem;
			padding: 0.25rem 0.75rem;
			border-radius: 9999px;
			font-size: 0.7rem;
			font-weight: 800;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			background: #10b981;
			color: #ffffff;
			box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
			z-index: 2;
		}

		&__content {
			padding: 1.25rem;
			display: flex;
			flex-direction: column;
			flex-grow: 1;
		}

		&__title {
			font-size: 1.125rem;
			font-weight: 700;
			color: var(--md-sys-color-on-surface, #ffffff);
			margin: 0 0 0.5rem 0;
			line-height: 1.35;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		&__desc {
			font-size: 0.875rem;
			color: var(--md-sys-color-on-surface-variant, #a0a0a0);
			line-height: 1.45;
			margin: 0 0 1.25rem 0;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
			flex-grow: 1;
		}

		&__footer {
			margin-top: auto;
		}
	}

	.btn--details {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.5rem 1rem;
		background: var(--md-sys-color-surface-container-high, rgba(255, 255, 255, 0.1));
		color: var(--md-sys-color-on-surface, #ffffff);
		font-size: 0.875rem;
		font-weight: 600;
		border-radius: 0.625rem;
		text-decoration: none;
		transition: all 0.2s ease;
		border: 1px solid transparent;

		&:hover {
			background: var(--md-sys-color-primary, #ff6b00);
			color: var(--md-sys-color-on-primary, #ffffff);
		}

		.btn__icon {
			width: 1rem;
			height: 1rem;
		}
	}
</style>
