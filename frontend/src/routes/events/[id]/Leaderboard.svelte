<script>
	import { Card, Icon, LinearProgress } from "m3-svelte"

	import iconGroups from "@ktibow/iconset-material-symbols/groups"

	let shown = $state(false)
</script>

<Card onclick={() => (shown = !shown)} variant="elevated">
	<div class="card {shown ? 'shown' : ''}">
		<div class="header">
			<div class="rank">
				<p>#1</p>
			</div>

			<div>
				<p>Team name</p>
				<p class="track">on Track 1</p>
			</div>
		</div>

		<div class="content">
			<div class="prog">
				<p>Round 2/3</p>
				<div>
					<LinearProgress title="Team's submission progress" percent={(100 / 3) * 2} />
				</div>
			</div>

			<div class="members">
				<p>
					<Icon icon={iconGroups} /> Members
				</p>
			</div>
		</div>
	</div>
</Card>

<style lang="scss">
	.card {
		display: grid;
		grid-template-rows: min-content 0fr;
		transition: grid-template-rows 200ms;

		&.shown {
			grid-template-rows: min-content 1fr;
		}
	}

	.rank {
		background-color: var(--md-sys-color-primary);
		color: var(--md-sys-color-on-primary);
		border-radius: 100%;
		aspect-ratio: 1;
		text-align: center;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.header {
		display: flex;
		gap: 1rem;
	}

	.content {
		overflow: hidden;
		margin-top: 1rem;
	}

	.track {
		opacity: 70%;
	}

	.prog {
		display: flex;
		justify-content: center;
		align-items: center;
		margin-top: 1rem;
		gap: 1rem;

		p {
			white-space: nowrap;
		}

		div {
			width: 100%;
		}
	}

	.members {
		margin-top: 1rem;
	}
</style>
