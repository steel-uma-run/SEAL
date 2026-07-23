<script lang="ts">
	import { Icon } from "m3-svelte"
	import EventsListing from "$lib/components/EventsListing.svelte"

	import iconCalendar from "@ktibow/iconset-material-symbols/calendar-today"
	import iconPerson from "@ktibow/iconset-material-symbols/person"
	import iconShield from "@ktibow/iconset-material-symbols/shield"

	let {
		profile,
		seasonsCount = 0,
		activeParticipantsCount = 0,
		totalLecturersCount = 0,
		totalTeamsCount = 0,
		pendingTeamsCount = 0,
		pendingStudentsCount = 0,
		unassignedJudgesCount = 0,
		activeEvents = [],
		currentSeason = null
	} = $props<{
		profile: any
		seasonsCount?: number
		activeParticipantsCount?: number
		totalLecturersCount?: number
		totalTeamsCount?: number
		pendingTeamsCount?: number
		pendingStudentsCount?: number
		unassignedJudgesCount?: number
		activeEvents?: any[]
		currentSeason?: any
	}>()
</script>

<div class="dashboard">
	<header class="dashboard__header">
		<div>
			<div class="dashboard__title-row">
				<h1 class="dashboard__title">Dashboard</h1>
			</div>

			<p class="dashboard__subtitle">
				Welcome back, {profile?.name || "Coordinator"}! Here's an overview of the system.
			</p>
		</div>

		<div class="dashboard__profile">
			<div class="dashboard__profile-text">
				<p class="dashboard__profile-name">{profile?.name || "Nguyễn Hùng Cường"}</p>
				<p class="dashboard__profile-role">COORDINATOR</p>
			</div>

			<div class="dashboard__avatar">
				{profile?.name?.charAt(0).toUpperCase() || "N"}
			</div>
		</div>
	</header>

	<div class="metrics-grid">
		<div class="metric-card">
			<div class="metric-card__top">
				<div class="metric-card__icon metric-card__icon--primary">
					<Icon icon={iconCalendar} />
				</div>
				<div>
					<p class="metric-card__label">Total Seasons</p>
					<h3 class="metric-card__value">{seasonsCount}</h3>
				</div>
			</div>
			<p class="metric-card__footer">(All active seasons)</p>
		</div>

		<div class="metric-card">
			<div class="metric-card__top">
				<div class="metric-card__icon metric-card__icon--primary">
					<Icon icon={iconPerson} />
				</div>
				<div>
					<p class="metric-card__label">Total Teams</p>
					<h3 class="metric-card__value">{totalTeamsCount}</h3>
				</div>
			</div>

			<div class="metric-card__status">
				{#if pendingTeamsCount > 0}
					<span class="badge badge--error">
						{pendingTeamsCount}
						{pendingTeamsCount === 1 ? "team" : "teams"} pending approval
					</span>
				{:else}
					<span class="badge badge--success">All teams approved</span>
				{/if}
			</div>
		</div>

		<div class="metric-card">
			<div class="metric-card__top">
				<div class="metric-card__icon metric-card__icon--secondary">
					<Icon icon={iconPerson} />
				</div>
				<div>
					<p class="metric-card__label">Active Participants</p>
					<h3 class="metric-card__value">{activeParticipantsCount}</h3>
				</div>
			</div>

			<div class="metric-card__status">
				{#if pendingStudentsCount > 0}
					<span class="badge badge--error">
						{pendingStudentsCount} pending student approvals
					</span>
				{:else}
					<span class="badge badge--success">No pending accounts</span>
				{/if}
			</div>
		</div>

		<div class="metric-card">
			<div class="metric-card__top">
				<div class="metric-card__icon metric-card__icon--tertiary">
					<Icon icon={iconShield} />
				</div>
				<div>
					<p class="metric-card__label">Total Lecturers/Judges</p>
					<h3 class="metric-card__value">{totalLecturersCount}</h3>
				</div>
			</div>

			<div class="metric-card__status">
				{#if unassignedJudgesCount > 0}
					<span class="badge badge--error">
						{unassignedJudgesCount}
						{unassignedJudgesCount === 1 ? "judge" : "judges"} unassigned to tracks
					</span>
				{:else}
					<span class="badge badge--success">All judges assigned</span>
				{/if}
			</div>
		</div>
	</div>

	<div class="events-list">
		<EventsListing />
	</div>
</div>

<style lang="scss">
	.dashboard {
		width: 100%;
	}

	.dashboard__header {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--md-outline-variant);

		@media (min-width: 768px) {
			flex-direction: row;
			align-items: center;
		}
	}

	.dashboard__title-row {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;

		@media (min-width: 640px) {
			flex-direction: row;
			align-items: center;
		}
	}

	.dashboard__title {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 800;
		letter-spacing: -0.025em;
		color: var(--md-on-surface);

		@media (min-width: 768px) {
			font-size: 1.875rem;
			line-height: 2.25rem;
		}
	}

	.season-selector {
		position: relative;
		margin-top: 0.25rem;

		@media (min-width: 640px) {
			margin-top: 0;
		}
	}

	.season-selector__button,
	.event-card__button {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		border: 1px solid var(--md-outline-variant);
		transition: all 0.2s ease;
		cursor: pointer;
		text-decoration: none;
	}

	.season-selector__button {
		padding: 0.375rem 0.75rem;
		border-radius: 0.75rem;
		font-size: 0.75rem;
		font-weight: 700;
		background: var(--md-surface-container-high);
		color: var(--md-on-surface);

		&:hover {
			background: var(--md-surface-container-highest);
		}
	}

	.dashboard__subtitle {
		margin-top: 0.5rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);

		@media (min-width: 768px) {
			font-size: 1rem;
			line-height: 1.5rem;
		}
	}

	.dashboard__profile {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 1rem;

		@media (min-width: 768px) {
			margin-top: 0;
		}
	}

	.dashboard__profile-text {
		display: none;
		text-align: right;

		@media (min-width: 640px) {
			display: block;
		}
	}

	.dashboard__profile-name {
		font-weight: 700;
		line-height: 1.25;
		color: var(--md-on-surface);
	}

	.dashboard__profile-role {
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;
		color: var(--md-primary);
	}

	.dashboard__avatar {
		width: 3rem;
		height: 3rem;
		border-radius: 9999px;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 1.25rem;
		font-weight: 700;
		color: var(--md-on-primary-container);
		background: var(--md-primary-container);
		border: 1px solid var(--md-outline-variant);
	}

	.metrics-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem;
		margin-bottom: 2rem;

		@media (min-width: 640px) {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		@media (min-width: 1024px) {
			grid-template-columns: repeat(4, minmax(0, 1fr));
		}
	}

	.metric-card {
		padding: 1.5rem;
		border-radius: 1.5rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container-lowest);
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
		transition:
			color 0.3s ease,
			background-color 0.3s ease;
	}

	.metric-card__top {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.metric-card__icon {
		width: 3rem;
		height: 3rem;
		border-radius: 0.75rem;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;

		&--primary {
			background: var(--md-primary-container);
			color: var(--md-on-primary-container);
		}

		&--secondary {
			background: var(--md-secondary-container);
			color: var(--md-on-secondary-container);
		}

		&--tertiary {
			background: var(--md-tertiary-container);
			color: var(--md-on-tertiary-container);
		}
	}

	.metric-card__label {
		margin-bottom: 0.25rem;
		font-size: 0.75rem;
		font-weight: 600;
		text-transform: uppercase;
		letter-spacing: 0.05em;
		color: var(--md-on-surface-variant);
	}

	.metric-card__value {
		font-size: 1.5rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.metric-card__footer {
		margin-top: 1rem;
		font-size: 0.75rem;
		font-weight: 500;
		color: var(--md-on-surface-variant);
	}

	.metric-card__status {
		margin-top: 1rem;
	}

	.badge,
	.chip {
		display: inline-block;
		font-size: 0.75rem;
		font-weight: 600;
		border-radius: 0.375rem;
	}

	.badge {
		padding: 0.25rem 0.625rem;
		border: 1px solid transparent;

		&--error {
			background: var(--md-error-container);
			color: var(--md-on-error-container);
			border-color: var(--md-error);
		}

		&--success {
			background: var(--md-secondary-container);
			color: var(--md-on-secondary-container);
			border-color: var(--md-outline-variant);
		}
	}

	.events-panel {
		margin-bottom: 2rem;
		padding: 2rem;
		border-radius: 1.5rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container-lowest);
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
	}

	.events-panel__header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;
	}

	.events-panel__title {
		font-size: 1.25rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 3rem 1rem;
		text-align: center;
		border: 1px dashed var(--md-outline-variant);
		border-radius: 1.5rem;
	}

	.empty-state__title {
		font-size: 0.875rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.empty-state__text {
		margin-top: 0.25rem;
		max-width: 24rem;
		font-size: 0.75rem;
		color: var(--md-on-surface-variant);
	}

	.events-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding-right: 0.5rem;
		margin-top: 1.5rem;
	}

	.event-card {
		padding: 1.25rem;
		border-radius: 1rem;
		border: 1px solid var(--md-outline-variant);
		background: var(--md-surface-container-low);
		transition: all 0.2s ease;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		gap: 1rem;

		&:hover {
			background: var(--md-surface-container-high);
		}

		@media (min-width: 768px) {
			flex-direction: row;
			align-items: center;
		}
	}

	.event-card__content {
		flex: 1;
	}

	.event-card__chips {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-bottom: 0.375rem;
		flex-wrap: wrap;
	}

	.chip {
		padding: 0.125rem 0.625rem;
		border-radius: 9999px;
		font-size: 0.625rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;

		&--primary {
			background: var(--md-primary-container);
			color: var(--md-on-primary-container);
			border: 1px solid var(--md-outline-variant);
		}

		&--tertiary {
			background: var(--md-tertiary-container);
			color: var(--md-on-tertiary-container);
		}
	}

	.event-card__title {
		margin-bottom: 0.25rem;
		font-size: 1rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.event-card__description,
	.event-card__meta {
		font-size: 0.75rem;
		color: var(--md-on-surface-variant);
	}

	.event-card__description {
		margin-bottom: 0.75rem;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.event-card__meta-row {
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	.event-card__button {
		padding: 0.625rem 1rem;
		border-radius: 0.75rem;
		font-size: 0.75rem;
		font-weight: 700;
		background: var(--md-primary);
		color: var(--md-on-primary);
		border: 0;
		flex-shrink: 0;

		&:hover {
			opacity: 0.9;
		}
	}
</style>
