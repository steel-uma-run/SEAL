<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Users, FileText, CheckCircle, Clock } from "@lucide/svelte"
	import {
		getSelfProfile,
		getAllSeasons,
		getEventsInSeason,
		getAllTracksOfEvent,
		getAllTeamsOfEvents,
		getAllSubmissions
	} from "$lib/api"
	import { getCurrentSeasonInfo, formatSeasonName } from "$lib/utils/seasons"
	import ActiveSeasonEvents from "$lib/components/student/ActiveSeasonEvents.svelte"

	let profile: any = $state(null)
	let stats = $state({
		mentoredTeamsCount: 0,
		submissionsToGradeCount: 0
	})
	let isLoading = $state(true)
	let activeSeason: any = $state(null)
	let activeSeasonEvents: any[] = $state([])
	let allAssignedTracks: any[] = $state([])
	let activeTab: "past" | "ongoing" | "upcoming" = $state("ongoing")
	let roleFilter: "all" | "mentor" | "judge" = $state("all")

	function getSeasonCategory(season: any, currentInfo: any) {
		const s1 =
			season.year * 3 + (season.semester === "SPRING" ? 0 : season.semester === "SUMMER" ? 1 : 2)
		const s2 =
			currentInfo.year * 3 +
			(currentInfo.semester === "SPRING" ? 0 : currentInfo.semester === "SUMMER" ? 1 : 2)
		if (s1 < s2) return "past"
		if (s1 === s2) return "ongoing"
		return "upcoming"
	}

	let filteredTracks = $derived(
		allAssignedTracks.filter(
			(t) =>
				t.category === activeTab &&
				(roleFilter === "all" ||
					(roleFilter === "mentor" && t.role.includes("Mentor")) ||
					(roleFilter === "judge" && t.role.includes("Judge")))
		)
	)

	onMount(async () => {
		try {
			const { data: userProfile } = await getSelfProfile({ throwOnError: true })
			profile = userProfile

			const currentInfo = getCurrentSeasonInfo()
			const { data: seasons } = await getAllSeasons({ throwOnError: true })
			activeSeason = seasons?.find(
				(s) => s.semester === currentInfo.semester && s.year === currentInfo.year
			)

			if (!seasons || seasons.length === 0) return

			let mentoredTeams = 0
			let submissionsToGrade = 0
			let tracksData: any[] = []

			// Fetch events for all seasons
			for (const season of seasons) {
				const category = getSeasonCategory(season, currentInfo)

				const { data: events } = await getEventsInSeason({
					path: { seasonId: season.id },
					throwOnError: false
				})

				if (category === "ongoing" && events) {
					activeSeasonEvents = events
				}

				if (!events) continue

				for (const event of events) {
					const { data: tracks } = await getAllTracksOfEvent({
						path: { eventId: event.id },
						throwOnError: false
					})

					if (!tracks) continue

					// Fetch teams for the event once
					let eventTeams: any[] = []
					const { data: teams } = await getAllTeamsOfEvents({
						path: { eventId: event.id } as any,
						throwOnError: false
					})
					if (teams) eventTeams = teams

					for (const track of tracks) {
						let isMentor = track.mentors?.find((v: any) => v.id == profile.id)
						let isJudge = track.judges?.find((v: any) => v.id == profile.id)

						if (isMentor || isJudge) {
							let trackTeams = eventTeams.filter(
								(t: any) => t.track_id === track.id || t.trackId === track.id
							)

							if (isMentor) {
								mentoredTeams += trackTeams.length
							}

							if (isJudge) {
								for (const team of trackTeams) {
									const { data: subs } = await getAllSubmissions({
										path: { teamId: team.id },
										throwOnError: false
									})
									if (subs) submissionsToGrade += subs.length
								}
							}

							tracksData.push({
								...track,
								eventName: event.name,
								category: category,
								role: isMentor && isJudge ? "Mentor & Judge" : isMentor ? "Mentor" : "Judge",
								teams: trackTeams
							})
						}
					}
				}
			}

			allAssignedTracks = tracksData
			stats = {
				mentoredTeamsCount: mentoredTeams,
				submissionsToGradeCount: submissionsToGrade
			}
		} catch (error) {
			console.error("Failed to load dashboard stats", error)
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>Lecturer Dashboard - SEAL</title>
</svelte:head>
<div class="dashboard-page">
	{#if isLoading}
		<div class="loading-state">
			<div class="spinner"></div>
		</div>
	{:else if profile}
		<header class="top-header">
			<div>
				<h1 class="page-title">
					Welcome back, {profile?.fullName || profile?.name || "Lecturer"}!
				</h1>
				<p class="page-subtitle">
					Here is an overview of your responsibilities for the active season.
				</p>
			</div>

			<div class="profile-summary">
				<div class="profile-text">
					<p class="profile-name">
						{profile?.fullName || profile?.name || "Lecturer"}
					</p>
					<p class="profile-role">
						{profile?.role || "LECTURER"}
					</p>
				</div>
				<div class="avatar">
					{(profile?.fullName || profile?.name || "L").charAt(0).toUpperCase()}
				</div>
			</div>
		</header>

		<div class="overview-grid">
			<div class="overview-card">
				<div class="overview-icon orange">
					<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
						></path>
					</svg>
				</div>
				<div>
					<p class="card-label">Current Season</p>
					<h3 class="card-value">
						{activeSeason ? formatSeasonName(activeSeason) : "---"}
					</h3>
				</div>
			</div>

			<div class="overview-card">
				<div class="overview-icon orange">
					<Users class="icon" />
				</div>
				<div>
					<p class="card-label">Mentored Teams</p>
					<h3 class="card-value">{stats.mentoredTeamsCount}</h3>
				</div>
			</div>

			<div class="overview-card">
				<div class="overview-icon orange">
					<FileText class="icon" />
				</div>
				<div>
					<p class="card-label">Submissions to Grade</p>
					<h3 class="card-value">{stats.submissionsToGradeCount}</h3>
				</div>
			</div>
		</div>

		<section class="assigned-tracks">
			<div class="section-header">
				<h2 class="section-title">My Assigned Tracks</h2>
				<p class="section-subtitle">Tracks you are assigned to mentor or judge.</p>

				<div class="filters-row">
					<div class="tabs-container">
						<button
							class="tab-btn {activeTab === 'past' ? 'active' : ''}"
							onclick={() => (activeTab = "past")}
						>
							Past
						</button>
						<button
							class="tab-btn {activeTab === 'ongoing' ? 'active' : ''}"
							onclick={() => (activeTab = "ongoing")}
						>
							Ongoing
						</button>
						<button
							class="tab-btn {activeTab === 'upcoming' ? 'active' : ''}"
							onclick={() => (activeTab = "upcoming")}
						>
							Upcoming
						</button>
					</div>

					<div class="role-filter">
						<button
							class="role-btn {roleFilter === 'all' ? 'active' : ''}"
							onclick={() => (roleFilter = "all")}>All Roles</button
						>
						<button
							class="role-btn {roleFilter === 'mentor' ? 'active' : ''}"
							onclick={() => (roleFilter = "mentor")}>Mentor</button
						>
						<button
							class="role-btn {roleFilter === 'judge' ? 'active' : ''}"
							onclick={() => (roleFilter = "judge")}>Judge</button
						>
					</div>
				</div>
			</div>

			{#if filteredTracks.length === 0}
				<div class="empty-state">
					<FileText class="empty-icon" />
					<p class="empty-title">You haven't been assigned to any {activeTab} tracks.</p>
				</div>
			{:else}
				<div class="tracks-grid">
					{#each filteredTracks as track}
						<div class="track-card">
							<div class="track-top">
								<div class="track-heading-row">
									<h3 class="track-name">{track.name}</h3>
									<span
										class="track-role-badge {track.role === 'Mentor & Judge'
											? 'badge-both'
											: track.role === 'Mentor'
												? 'badge-mentor'
												: 'badge-judge'}"
									>
										{track.role}
									</span>
								</div>
								<p class="track-event">
									Event: <span>{track.eventName}</span>
								</p>
							</div>

							<div class="track-teams">
								<h4 class="teams-label">Assigned Teams</h4>
								{#if track.teams && track.teams.length > 0}
									<div class="teams-list">
										{#each track.teams as team}
											<span class="team-chip">
												{team.name}
											</span>
										{/each}
									</div>
								{:else}
									<p class="no-teams">No teams assigned yet.</p>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</section>

		<ActiveSeasonEvents {activeSeason} events={activeSeasonEvents} />
	{/if}
</div>

<style lang="scss">
	.dashboard-page {
		max-width: 1600px;
	}

	@media (min-width: 768px) {
		.dashboard-page {
			padding: 2.5rem;
		}
	}

	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 60vh;
	}

	.spinner {
		width: 3rem;
		height: 3rem;
		border-radius: 9999px;
		border: 2px solid transparent;
		border-top-color: var(--md-primary);
		border-bottom-color: var(--md-primary);
		animation: spin 1s linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.top-header {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--md-outline-variant);
	}

	@media (min-width: 768px) {
		.top-header {
			flex-direction: row;
			align-items: center;
		}
	}

	.page-title {
		font-size: 1.5rem;
		line-height: 2rem;
		font-weight: 800;
		letter-spacing: -0.025em;
		color: var(--md-on-surface);
	}

	@media (min-width: 768px) {
		.page-title {
			font-size: 1.875rem;
			line-height: 2.25rem;
		}
	}

	.page-subtitle {
		margin-top: 0.25rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);
	}

	@media (min-width: 768px) {
		.page-subtitle {
			font-size: 1rem;
			line-height: 1.5rem;
		}
	}

	.profile-summary {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 1rem;
	}

	@media (min-width: 768px) {
		.profile-summary {
			margin-top: 0;
		}
	}

	.profile-text {
		display: none;
		text-align: right;
	}

	@media (min-width: 640px) {
		.profile-text {
			display: block;
		}
	}

	.profile-name {
		font-weight: 700;
		line-height: 1.25;
		color: var(--md-on-surface);
	}

	.profile-role {
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		color: var(--md-primary);
	}

	.avatar {
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

	.overview-grid,
	.tracks-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1rem;
	}

	@media (min-width: 640px) {
		.overview-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}

	@media (min-width: 1024px) {
		.overview-grid {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}

		.tracks-grid {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}
	}

	@media (min-width: 768px) {
		.overview-grid,
		.tracks-grid {
			gap: 1.5rem;
		}
	}

	.overview-card,
	.track-card,
	.empty-state,
	.assigned-tracks {
		border: 1px solid var(--md-outline-variant);
	}

	.overview-card {
		border-color: color-mix(in srgb, var(--md-outline-variant) 50%, transparent);
		border-radius: 1rem;
		padding: 1.5rem;
		background: var(--md-surface-container-lowest);
		display: flex;
		align-items: center;
		gap: 1.25rem;
		transition: all 0.3s ease;
	}

	.overview-card:hover {
		background: var(--md-surface-container-low);
	}

	.overview-icon {
		width: 3rem;
		height: 3rem;
		border-radius: 0.75rem;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.overview-icon.orange {
		background: rgb(249 115 22 / 0.1);
		color: rgb(249 115 22);
	}

	.icon {
		width: 1.5rem;
		height: 1.5rem;
	}

	.card-label {
		margin-bottom: 0.25rem;
		font-size: 0.75rem;
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		color: var(--md-on-surface-variant);
	}

	.card-value {
		font-size: 1.25rem;
		line-height: 1.75rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.assigned-tracks {
		padding: 2rem;
		border-radius: 1.5rem;
		background: var(--md-surface-container-low);
		margin-bottom: 2rem;
		transition: background-color 0.3s ease;
	}

	.section-header {
		margin-bottom: 1.5rem;
	}

	.section-title {
		font-size: 1.25rem;
		line-height: 1.75rem;
		font-weight: 700;
		color: var(--md-on-surface);
	}

	.section-subtitle {
		margin-top: 0.25rem;
		font-size: 0.875rem;
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);
	}

	.empty-state {
		text-align: center;
		padding: 2.5rem 1rem;
		border-style: dashed;
		border-radius: 1rem;
		background: var(--md-surface-container);
	}

	.empty-icon {
		width: 3rem;
		height: 3rem;
		margin: 0 auto 0.75rem;
		color: var(--md-on-surface-variant);
		opacity: 0.6;
	}

	.empty-title {
		font-size: 1rem;
		line-height: 1.5rem;
		font-weight: 500;
		color: var(--md-on-surface);
	}

	.track-card {
		border-radius: 1rem;
		padding: 1.5rem;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		background: var(--md-surface-container);
		box-shadow: 0 1px 2px rgb(0 0 0 / 0.05);
		height: 100%;
		transition: all 0.3s ease;
	}

	.track-card:hover {
		background: var(--md-surface-container-high);
	}

	.track-top {
		margin-bottom: 1rem;
	}

	.track-heading-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 0.5rem;
		gap: 0.75rem;
	}

	.track-name {
		font-size: 1.125rem;
		line-height: 1.75rem;
		font-weight: 800;
		color: var(--md-on-surface);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.track-role-badge {
		display: inline-flex;
		padding: 0.125rem 0.625rem;
		border-radius: 9999px;
		font-size: 10px;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.1em;
		flex-shrink: 0;
	}

	.badge-both {
		background: rgb(99 102 241 / 0.1);
		color: rgb(99 102 241);
		border: 1px solid rgb(99 102 241 / 0.2);
	}

	.badge-mentor {
		background: rgb(139 92 246 / 0.1);
		color: rgb(139 92 246);
		border: 1px solid rgb(139 92 246 / 0.2);
	}

	.badge-judge {
		background: rgb(14 165 233 / 0.1);
		color: rgb(14 165 233);
		border: 1px solid rgb(14 165 233 / 0.2);
	}

	.track-event {
		font-size: 0.75rem;
		line-height: 1rem;
		color: var(--md-on-surface-variant);
		margin-bottom: 1rem;
	}

	.track-event span {
		font-weight: 700;
		color: var(--md-primary);
	}

	.track-teams {
		flex-grow: 1;
		padding-top: 1rem;
		border-top: 1px solid var(--md-outline-variant);
	}

	.teams-label {
		margin-bottom: 0.5rem;
		font-size: 0.75rem;
		line-height: 1rem;
		text-transform: uppercase;
		font-weight: 700;
		letter-spacing: 0.1em;
		color: var(--md-on-surface-variant);
	}

	.teams-list {
		display: flex;
		flex-wrap: wrap;
		gap: 0.25rem;
	}

	.team-chip {
		display: inline-flex;
		align-items: center;
		padding: 0.125rem 0.5rem;
		border-radius: 0.25rem;
		font-size: 0.75rem;
		line-height: 1rem;
		background: rgb(16 185 129 / 0.1);
		color: rgb(16 185 129);
		font-weight: 500;
	}

	.no-teams {
		font-size: 0.75rem;
		line-height: 1rem;
		font-style: italic;
		color: var(--md-on-surface-variant);
		opacity: 0.75;
	}

	.filters-row {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		margin-top: 1rem;
		border-bottom: 1px solid var(--md-outline-variant);
		padding-bottom: 1rem;
	}

	@media (min-width: 768px) {
		.filters-row {
			flex-direction: row;
			justify-content: space-between;
			align-items: center;
		}
	}

	.tabs-container {
		display: flex;
		gap: 0.5rem;
	}

	.tab-btn {
		padding: 0.5rem 1rem;
		border-radius: 9999px;
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--md-on-surface-variant);
		background: transparent;
		border: 1px solid transparent;
		cursor: pointer;
		transition: all 0.2s ease;
	}

	.tab-btn:hover {
		background: var(--md-surface-container-high);
	}

	.tab-btn.active {
		background: var(--md-primary);
		color: var(--md-on-primary);
	}

	.role-filter {
		display: flex;
		background: var(--md-surface-container-high);
		border-radius: 0.5rem;
		padding: 0.25rem;
	}

	.role-btn {
		padding: 0.375rem 0.75rem;
		font-size: 0.75rem;
		font-weight: 600;
		border: none;
		background: transparent;
		color: var(--md-on-surface-variant);
		border-radius: 0.375rem;
		cursor: pointer;
		transition: all 0.2s ease;
	}

	.role-btn:hover {
		color: var(--md-on-surface);
	}

	.role-btn.active {
		background: var(--md-surface);
		color: var(--md-primary);
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
	}
</style>
