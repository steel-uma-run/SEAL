<script lang="ts">
	import { Button, Icon } from "m3-svelte"
	import EventsListing from "$lib/components/EventsListing.svelte"

	import iconArrowForward from "@ktibow/iconset-material-symbols/arrow-forward"
</script>

<div class="container">
	<section class="hero">
		<div>
			<h1>
				<span class="seal">SEAL</span> HACKATHON
			</h1>
			<p>
				The official event management and student support platform for FPT University. Register now
				to form your team, submit your project, and experience the platform!
			</p>

			<Button iconType="left" href="/auth/login"><Icon icon={iconArrowForward} /> Join now</Button>
		</div>
	</section>

	<section class="events">
		<EventsListing />
	</section>
</div>

<style lang="scss">
	.container {
		padding: 0.5rem;
	}

	.seal {
		color: var(--md-sys-color-primary);
	}

	.hero {
		display: flex;
		align-items: center;
		text-align: center;
		min-height: 100vh;

		div {
			margin: auto;
			max-width: 768px;

			> h1 {
				font-size: 2.5rem;
				font-weight: bolder;
				margin-bottom: 1rem;
			}

			> p {
				line-height: 1.5rem;
				margin-bottom: 1rem;
			}
		}
	}

	.events {
		margin: auto;
		max-width: 768px;
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}
</style>
