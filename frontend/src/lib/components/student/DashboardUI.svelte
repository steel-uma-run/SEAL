<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { formatSeasonName } from "$lib/utils/seasons"
	import { formatFullDate } from "$lib/utils/formatters"

	let {
		profile,
		seasons = [],
		activeSeason = null,
		joinedEvents = [],
		activeRounds = []
	} = $props<{
		profile: any
		seasons: any[]
		activeSeason?: any
		joinedEvents?: any[]
		activeRounds?: any[]
	}>()

	let nearestEvent = $derived(
		joinedEvents.length > 0
			? joinedEvents
					.slice()
					.sort(
						(a, b) =>
							new Date(a.endTime || a.end_time).getTime() -
							new Date(b.endTime || b.end_time).getTime()
					)[0]
			: null
	)
</script>

<div class="dashboard-stats">
	<!-- Current Season (Always visible) -->
	<div class="dashboard-stats__card">
		<div class="dashboard-stats__icon dashboard-stats__icon--primary">
			<svg class="dashboard-stats__icon-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
				></path>
			</svg>
		</div>
		<div class="dashboard-stats__content">
			<p class="dashboard-stats__label">Current Season</p>
			<h3 class="dashboard-stats__value">
				{activeSeason
					? formatSeasonName(activeSeason)
					: seasons.length > 0
						? formatSeasonName(seasons[0])
						: "---"}
			</h3>
		</div>
	</div>

	{#if joinedEvents.length > 0}
		<!-- Joined Events Count -->
		<div class="dashboard-stats__card">
			<div class="dashboard-stats__icon dashboard-stats__icon--secondary">
				<svg
					class="dashboard-stats__icon-svg"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9"
					></path>
				</svg>
			</div>
			<div class="dashboard-stats__content">
				<p class="dashboard-stats__label">Joined Events</p>
				<h3 class="dashboard-stats__value">{joinedEvents.length}</h3>
			</div>
		</div>

		<!-- Progress Status -->
		<div class="dashboard-stats__card">
			<div class="dashboard-stats__icon dashboard-stats__icon--tertiary">
				<svg
					class="dashboard-stats__icon-svg"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
					></path>
				</svg>
			</div>
			<div class="dashboard-stats__content">
				<p class="dashboard-stats__label">Progress Status</p>
				<h3 class="dashboard-stats__value dashboard-stats__value--sm">
					{#if activeRounds && activeRounds.length > 0}
						<span class="dashboard-stats__clamp"
							>Round: {activeRounds.map((r) => r.name).join(", ")}</span
						>
					{:else}
						Round: asd
					{/if}
				</h3>
			</div>
		</div>

		<!-- Submission Due -->
		<div class="dashboard-stats__card">
			<div class="dashboard-stats__icon dashboard-stats__icon--error">
				<svg
					class="dashboard-stats__icon-svg"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
					></path>
				</svg>
			</div>
			<div class="dashboard-stats__content">
				<p class="dashboard-stats__label">Submission Due</p>
				<h3 class="dashboard-stats__value dashboard-stats__value--sm">
					{nearestEvent ? formatFullDate(nearestEvent.endTime || nearestEvent.end_time) : "TBA"}
				</h3>
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	// ============================================================================
	// Dashboard Stats Grid - SCSS Conversion
	// grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-8
	// ============================================================================
	$bp-sm: 640px;
	$bp-md: 768px;
	$bp-lg: 1024px;

	.dashboard-stats {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1rem; // gap-4
		margin-bottom: 2rem; // mb-8

		@media (min-width: $bp-sm) {
			grid-template-columns: repeat(2, 1fr);
		}

		@media (min-width: $bp-md) {
			gap: 1.5rem; // md:gap-6
		}

		@media (min-width: $bp-lg) {
			grid-template-columns: repeat(4, 1fr);
		}

		// --------------------------------------------------------------------------
		// Card
		// rounded-2xl p-6 border border-(--md-outline-variant)/50
		// bg-(--md-surface-container-lowest) flex items-center gap-5
		// transition-all duration-300 hover:bg-(--md-surface-container-low)
		// --------------------------------------------------------------------------
		&__card {
			border-radius: 1rem; // rounded-2xl
			padding: 1.5rem; // p-6
			border: 1px solid color-mix(in srgb, var(--md-outline-variant) 50%, transparent);
			background-color: var(--md-surface-container-lowest);
			display: flex;
			align-items: center;
			gap: 1.25rem; // gap-5
			transition: all 0.3s ease;

			&:hover {
				background-color: var(--md-surface-container-low);
			}
		}

		&__icon {
			width: 3rem; // w-12
			height: 3rem; // h-12
			border-radius: 0.75rem; // rounded-xl
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0; // shrink-0

			&--primary {
				background-color: var(--md-primary-container);
				color: var(--md-on-primary-container);
			}

			&--secondary {
				background-color: var(--md-secondary-container);
				color: var(--md-on-secondary-container);
			}

			&--tertiary {
				background-color: var(--md-tertiary-container);
				color: var(--md-on-tertiary-container);
			}

			&--error {
				background-color: var(--md-error-container);
				color: var(--md-on-error-container);
			}
		}

		&__icon-svg {
			width: 1.5rem; // w-6
			height: 1.5rem; // h-6
		}

		&__content {
			min-width: 0; // allow truncation inside flex
			flex: 1;
		}

		&__label {
			font-size: 0.75rem; // text-xs
			font-weight: 600; // font-semibold
			text-transform: uppercase;
			letter-spacing: 0.05em; // tracking-wider
			margin-bottom: 0.25rem; // mb-1
			color: var(--md-on-surface-variant);
		}

		&__value {
			font-size: 1.25rem; // text-xl
			font-weight: 700; // font-bold
			color: var(--md-on-surface);
			line-height: 1.2;

			&--sm {
				font-size: 1.125rem; // text-lg for longer values
			}
		}

		&__clamp {
			// line-clamp-1
			display: -webkit-box;
			-webkit-line-clamp: 1;
			-webkit-box-orient: vertical;
			overflow: hidden;
			// standard property for future
			line-clamp: 1;
		}
	}
</style>
