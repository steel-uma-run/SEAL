<script lang="ts">
	import { onMount } from "svelte"
	import {
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		getInterestedParticipants,
		getAllTracksOfEvent,
		approveTeam
	} from "$lib/api"
	import {
		Users,
		UserCheck,
		Search,
		Filter,
		X,
		Mail,
		Eye,
		CheckCircle2,
		AlertTriangle,
		Plus,
		Clock
	} from "@lucide/svelte"

	// Teams State
	let teams = $state<any[]>([])
	let seasonsList = $state<any[]>([])
	let allEvents = $state<any[]>([])
	let isLoading = $state(true)

	// Filters State
	let searchQuery = $state("")
	let statusFilter = $state("ALL")
	let seasonFilter = $state("ALL")

	// Modal Control State
	let showViewModal = $state(false)
	let selectedTeam = $state<any>(null)

	// Derived current season (latest season by year)
	let currentSeason = $derived(
		seasonsList.length > 0
			? seasonsList.reduce((latest, current) => {
					return current.year > latest.year ? current : latest
				}, seasonsList[0])
			: null
	)

	// Derived running events in selected/current season
	let runningEvents = $derived.by(() => {
		let targetSeasonId = seasonFilter
		if (targetSeasonId === "ALL" && currentSeason) {
			targetSeasonId = currentSeason.id
		}

		if (!targetSeasonId) return []

		const now = new Date()
		return allEvents.filter((event) => {
			if (event.seasonId !== targetSeasonId) return false
			const start = new Date(event.startTime || event.start_time)
			const end = new Date(event.endTime || event.end_time)
			return now >= start && now <= end
		})
	})

	// Derived filtered teams list
	let filteredTeams = $derived(
		teams.filter((t) => {
			const matchesSearch =
				t.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
				t.leader.toLowerCase().includes(searchQuery.toLowerCase()) ||
				t.id.includes(searchQuery)
			const matchesStatus = statusFilter === "ALL" || t.status === statusFilter
			const matchesSeason = seasonFilter === "ALL" || t.seasonId === seasonFilter
			return matchesSearch && matchesStatus && matchesSeason
		})
	)

	// Statistics
	let totalTeams = $derived(teams.length)
	let approvedTeams = $derived(teams.filter((t) => t.status === "APPROVED").length)
	let pendingTeams = $derived(teams.filter((t) => t.status === "PENDING").length)
	let rejectedTeams = $derived(teams.filter((t) => t.status === "REJECTED").length)

	let successMessage = $state("")
	let errorMessage = $state("")

	function showNotification(msg: string, type: "success" | "error" = "success") {
		if (type === "success") {
			successMessage = msg
			setTimeout(() => (successMessage = ""), 3000)
		} else {
			errorMessage = msg
			setTimeout(() => (errorMessage = ""), 3000)
		}
	}

	function formatSemester(semester: string) {
		if (!semester) return ""
		return semester.charAt(0).toUpperCase() + semester.slice(1).toLowerCase()
	}

	async function loadRealTeams() {
		isLoading = true
		let loadedTeams: any[] = []
		let loadedSeasons: any[] = []
		let loadedEvents: any[] = []

		try {
			// 1. Fetch seasons
			const seasonsRes = await getAllSeasons({ throwOnError: false })
			if (seasonsRes.response?.ok && seasonsRes.data) {
				loadedSeasons = seasonsRes.data
			}
			seasonsList = loadedSeasons

			// 2. Fetch events per season
			for (const season of loadedSeasons) {
				const seasonId = season.id
				const seasonName = `${formatSemester(season.semester)} ${season.year}`

				let seasonEvents: any[] = []
				const eventsRes = await getEventsInSeason({ path: { seasonId }, throwOnError: false })
				if (eventsRes.response?.ok && eventsRes.data) {
					seasonEvents = [...eventsRes.data]
				}
				if (typeof window !== "undefined") {
					const localEvents = localStorage.getItem(`events_${seasonId}`)
					if (localEvents) {
						try {
							const parsed = JSON.parse(localEvents)
							if (Array.isArray(parsed)) {
								for (const e of parsed) {
									if (!seasonEvents.some((se) => se.id === e.id)) {
										seasonEvents.push(e)
									}
								}
							}
						} catch (e) {
							console.error("Error parsing local events:", e)
						}
					}
				}

				// Cache events for currently running calculation
				for (const event of seasonEvents) {
					loadedEvents.push({
						...event,
						seasonId
					})
				}

				// 3. Fetch teams, participants, tracks per event
				for (const event of seasonEvents) {
					const eventId = event.id

					let eventTeams: any[] = []
					const teamsRes = await getAllTeamsOfEvents({
						path: { seasonId, eventId },
						throwOnError: false
					})
					if (teamsRes.response?.ok && teamsRes.data) {
						eventTeams = [...teamsRes.data]
					}
					if (typeof window !== "undefined") {
						const localTeams = localStorage.getItem(`teams_${eventId}`)
						if (localTeams) {
							try {
								const parsed = JSON.parse(localTeams)
								if (Array.isArray(parsed)) {
									for (const t of parsed) {
										if (!eventTeams.some((et) => et.id === t.id)) {
											eventTeams.push(t)
										}
									}
								}
							} catch (e) {
								console.error("Error parsing local teams:", e)
							}
						}
					}

					if (eventTeams.length === 0) continue

					let participants: any[] = []
					const partsRes = await getInterestedParticipants({
						path: { seasonId, eventId },
						throwOnError: false
					})
					if (partsRes.response?.ok && partsRes.data) {
						participants = [...partsRes.data]
					}
					if (typeof window !== "undefined") {
						const localParts = localStorage.getItem(`participants_${eventId}`)
						if (localParts) {
							try {
								const parsed = JSON.parse(localParts)
								if (Array.isArray(parsed)) {
									for (const p of parsed) {
										if (!participants.some((sp) => sp.id === p.id)) {
											participants.push(p)
										}
									}
								}
							} catch (e) {
								console.error("Error parsing local participants:", e)
							}
						}
					}

					let tracks: any[] = []
					try {
						const tracksRes = await getAllTracksOfEvent({
							path: { seasonId, eventId },
							throwOnError: false
						})
						if (tracksRes.response?.ok && tracksRes.data) {
							tracks = [...tracksRes.data]
						}
					} catch (e) {
						console.error("Error loading tracks for event:", e)
					}

					for (const team of eventTeams) {
						const leaderStudent = participants.find(
							(p) => p.id === team.leader_id || p.id === team.leaderId
						)
						const trackObj = tracks.find((t) => t.id === team.track_id)

						loadedTeams.push({
							id: team.id,
							name: team.name,
							season: seasonName,
							seasonId: seasonId,
							eventId: eventId,
							event: event,
							track: trackObj ? trackObj.name : team.track || "General",
							leader: leaderStudent ? leaderStudent.email : team.leader || "unknown@fptu.edu.vn",
							leaderName: leaderStudent
								? leaderStudent.fullName || leaderStudent.name
								: team.leaderName || "Unknown Leader",
							membersCount: team.members ? team.members.length : team.membersCount || 1,
							maxMembers: 5,
							status: team.status || "PENDING"
						})
					}
				}
			}

			allEvents = loadedEvents
			teams = loadedTeams
		} catch (err) {
			console.error("Error loading team data:", err)
		} finally {
			isLoading = false
		}
	}

	async function handleApproveTeam(teamId: string, eventId?: string) {
		try {
			const res = await approveTeam({ path: { teamId }, throwOnError: false })
			if (res.response?.ok) {
				showNotification("Team approved successfully!")
				await loadRealTeams()
				return
			}
		} catch (err) {
			console.error("API Team Approval failed, falling back to local:", err)
		}

		// Fallback for mock data / local storage
		if (typeof window !== "undefined" && eventId) {
			const key = `teams_${eventId}`
			const stored = localStorage.getItem(key)
			if (stored) {
				let localTeams = JSON.parse(stored)
				localTeams = localTeams.map((t: any) =>
					t.id === teamId ? { ...t, status: "APPROVED" } : t
				)
				localStorage.setItem(key, JSON.stringify(localTeams))
			}
		}
		teams = teams.map((t) => (t.id === teamId ? { ...t, status: "APPROVED" } : t))
		showNotification("Team approved successfully!")
	}

	async function handleRejectTeam(teamId: string, eventId?: string) {
		// Since there's no reject endpoint, update local mock storage
		if (typeof window !== "undefined" && eventId) {
			const key = `teams_${eventId}`
			const stored = localStorage.getItem(key)
			if (stored) {
				let localTeams = JSON.parse(stored)
				localTeams = localTeams.map((t: any) =>
					t.id === teamId ? { ...t, status: "REJECTED" } : t
				)
				localStorage.setItem(key, JSON.stringify(localTeams))
			}
		}
		teams = teams.map((t) => (t.id === teamId ? { ...t, status: "REJECTED" } : t))
		showNotification("Team request rejected.", "error")
	}

	function openViewModal(team: any) {
		selectedTeam = team
		showViewModal = true
	}

	onMount(() => {
		loadRealTeams()
	})
</script>

<!-- ===== PAGE WRAPPER ===== -->
<div class="page-container">
	<!-- Success & Error Banners -->
	{#if successMessage}
		<div class="toast toast--success">
			<CheckCircle2 class="icon icon-lg" />
			<div class="toast__body">
				<p class="toast__title">Success</p>
				<p class="toast__message">{successMessage}</p>
			</div>
		</div>
	{/if}

	{#if errorMessage}
		<div class="toast toast--error">
			<AlertTriangle class="icon icon-lg" />
			<div class="toast__body">
				<p class="toast__title toast__title--error">Alert</p>
				<p class="toast__message">{errorMessage}</p>
			</div>
		</div>
	{/if}

	<!-- Header Section -->
	<header class="page-header">
		<div>
			<h1 class="page-title">Team Management</h1>
			<p class="page-subtitle">
				Review, approve, and track student teams participating in the hackathons.
			</p>
		</div>
	</header>

	<!-- Statistics Cards -->
	<div class="stats-grid">
		<!-- Total Teams -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--primary">
				<Users class="icon icon-xl" />
			</div>
			<div>
				<p class="stat-card__label">Total Teams</p>
				<h3 class="stat-card__value">{totalTeams}</h3>
			</div>
		</div>

		<!-- Approved -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--success">
				<UserCheck class="icon icon-xl" />
			</div>
			<div>
				<p class="stat-card__label">Approved</p>
				<h3 class="stat-card__value">{approvedTeams}</h3>
			</div>
		</div>

		<!-- Pending -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--warning">
				<Users class="icon icon-xl" />
			</div>
			<div>
				<p class="stat-card__label">Pending</p>
				<h3 class="stat-card__value">{pendingTeams}</h3>
			</div>
		</div>

		<!-- Rejected -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--danger">
				<Users class="icon icon-xl" />
			</div>
			<div>
				<p class="stat-card__label">Rejected</p>
				<h3 class="stat-card__value">{rejectedTeams}</h3>
			</div>
		</div>
	</div>

	<!-- Filters and Table Container -->
	<div class="panel">
		<!-- Filters Toolbar -->
		<div class="toolbar">
			<!-- Search bar -->
			<div class="search">
				<span class="search__icon">
					<Search class="icon icon-sm" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search by team name, leader..."
					class="search__input"
				/>
				{#if searchQuery}
					<button type="button" onclick={() => (searchQuery = "")} class="search__clear">
						Clear
					</button>
				{/if}
			</div>

			<!-- selectors -->
			<div class="filter-controls">
				<div class="filter-controls__label">
					<Filter class="icon icon-sm" />
					Filters:
				</div>

				<!-- Status Filter -->
				<select bind:value={statusFilter} class="select">
					<option value="ALL">All Statuses</option>
					<option value="APPROVED">Approved</option>
					<option value="PENDING">Pending</option>
					<option value="REJECTED">Rejected</option>
				</select>

				<!-- Season Filter -->
				<select bind:value={seasonFilter} class="select">
					<option value="ALL">All Seasons</option>
					{#each seasonsList as season}
						{@const name = `${formatSemester(season.semester)} ${season.year}`}
						<option value={season.id}>{name}</option>
					{/each}
				</select>
			</div>
		</div>

		<!-- Currently Running Events Banner -->
		{#if runningEvents.length > 0}
			<div class="running-banner">
				<div class="running-banner__main">
					<div class="running-banner__icon-wrap">
						<Clock class="icon icon-md running-banner__icon" />
					</div>
					<div>
						<p class="running-banner__title">
							Running Events in {seasonFilter === "ALL" ? "Current Season" : "Selected Season"}
						</p>
						<div class="running-banner__list">
							{#each runningEvents as event, i}
								<span>{event.name}</span>
								{#if i < runningEvents.length - 1}
									<span class="running-banner__dot">&bull;</span>
								{/if}
							{/each}
						</div>
					</div>
				</div>
			</div>
		{/if}

		<!-- Table -->
		<div class="table-wrap">
			<table class="team-table">
				<thead>
					<tr class="team-table__head-row">
						<th class="team-table__th">Team Name</th>
						<th class="team-table__th">Event</th>
						<th class="team-table__th">Leader</th>
						<th class="team-table__th">Members</th>
						<th class="team-table__th">Status</th>
						<th class="team-table__th team-table__th--center">Actions</th>
					</tr>
				</thead>
				<tbody class="team-table__body">
					{#if isLoading}
						<tr>
							<td colspan="6" class="team-table__cell team-table__cell--center">
								<div class="table-loading">
									<div class="spinner"></div>
									<span class="table-loading__text">Loading team management data...</span>
								</div>
							</td>
						</tr>
					{:else if filteredTeams.length > 0}
						{#each filteredTeams as team}
							<tr class="team-table__row">
								<td class="team-table__cell team-table__cell--bold">
									{team.name}
								</td>
								<td class="team-table__cell team-table__cell--semibold">
									{team.event ? team.event.name || team.event : team.eventName || "General Event"}
								</td>
								<td class="team-table__cell">
									<p class="team-leader-name">{team.leaderName}</p>
									<p class="team-leader-email">{team.leader}</p>
								</td>
								<td class="team-table__cell team-table__cell--semibold">
									{team.membersCount}/{team.maxMembers}
								</td>
								<td class="team-table__cell">
									{#if team.status === "APPROVED"}
										<span class="badge badge--approved">Approved</span>
									{:else if team.status === "PENDING"}
										<span class="badge badge--pending">Pending</span>
									{:else if team.status === "REJECTED"}
										<span class="badge badge--rejected">Rejected</span>
									{/if}
								</td>
								<td class="team-table__cell team-table__cell--center">
									<div class="row-actions">
										<button
											onclick={() => openViewModal(team)}
											title="View Details"
											class="icon-btn"
										>
											<Eye class="icon icon-sm" />
										</button>
										{#if team.status === "PENDING"}
											<button
												onclick={() => handleApproveTeam(team.id, team.eventId)}
												title="Approve Team"
												class="btn btn--approve"
											>
												Approve
											</button>
											<button
												onclick={() => handleRejectTeam(team.id, team.eventId)}
												title="Reject Team"
												class="btn btn--reject"
											>
												Reject
											</button>
										{/if}
									</div>
								</td>
							</tr>
						{/each}
					{:else}
						<tr>
							<td colspan="6" class="team-table__cell team-table__cell--center table-empty">
								<p class="table-empty__title">No teams found</p>
								<p class="table-empty__hint">Try adjusting your filters or search terms.</p>
							</td>
						</tr>
					{/if}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- Modal: View Details -->
{#if showViewModal && selectedTeam}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={() => (showViewModal = false)} class="modal__close">
				<X class="icon icon-lg" />
			</button>

			<h3 class="modal__title">
				<Users class="icon icon-lg modal__title-icon" />
				Team Details - {selectedTeam.name}
			</h3>

			<div class="modal__body">
				<div class="modal__section">
					<p class="modal__section-label">Event</p>
					<p class="modal__section-value">
						{selectedTeam.event
							? selectedTeam.event.name || selectedTeam.event
							: selectedTeam.eventName || "General Event"}
					</p>
				</div>

				<div class="modal__section">
					<p class="modal__section-label modal__section-label--with-icon">
						<Mail class="icon icon-xs" />
						Leader Email
					</p>
					<p class="modal__section-value modal__section-value--primary">
						{selectedTeam.leaderName} ({selectedTeam.leader})
					</p>
				</div>

				<div class="modal__grid">
					<div class="modal__section">
						<p class="modal__section-label">Members Count</p>
						<p class="modal__section-value">
							{selectedTeam.membersCount} / {selectedTeam.maxMembers}
						</p>
					</div>
					<div class="modal__section">
						<p class="modal__section-label">Status</p>
						<p class="modal__section-value">{selectedTeam.status}</p>
					</div>
				</div>
			</div>

			<button onclick={() => (showViewModal = false)} class="modal__footer-btn"> Close </button>
		</div>
	</div>
{/if}

<style>
	.icon {
		display: inline-block;
		flex: none;
	}
	.icon-xs {
		width: 0.875rem; /* w-3.5 */
		height: 0.875rem;
	}
	.icon-sm {
		width: 1rem; /* w-4 */
		height: 1rem;
	}
	.icon-md {
		width: 1.125rem; /* w-4.5 */
		height: 1.125rem;
	}
	.icon-lg {
		width: 1.25rem; /* w-5 */
		height: 1.25rem;
	}
	.icon-xl {
		width: 1.5rem; /* w-6 */
		height: 1.5rem;
	}

	.toast {
		position: fixed;
		top: 1.5rem;
		right: 1.5rem;
		z-index: 5000;
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 1rem;
		max-width: 28rem; /* max-w-md */
		border-radius: 1rem; /* rounded-2xl */
		border: 1px solid;
		box-shadow:
			0 10px 15px -3px rgb(0 0 0 / 0.1),
			0 4px 6px -4px rgb(0 0 0 / 0.1); /* shadow-lg */
		backdrop-filter: blur(12px); /* backdrop-blur-md */
		animation: fadeIn 0.2s ease-out;
	}
	.toast--success {
		background-color: rgb(16 185 129 / 0.1); /* emerald-500/10 */
		border-color: rgb(16 185 129 / 0.2);
		color: #10b981; /* emerald-500 */
	}
	.toast--error {
		background-color: rgb(244 63 94 / 0.1); /* rose-500/10 */
		border-color: rgb(244 63 94 / 0.2);
		color: #f43f5e; /* rose-500 */
	}
	.toast__body {
		display: block;
	}
	.toast__title {
		font-size: 0.875rem; /* text-sm */
		line-height: 1.25rem;
		font-weight: 700; /* font-bold */
		color: #10b981; /* emerald-500 */
	}
	.toast__title--error {
		color: #f43f5e; /* rose-500 */
	}
	.toast__message {
		margin-top: 0.125rem; /* mt-0.5 */
		font-size: 0.75rem; /* text-xs */
		line-height: 1rem;
	}

	@keyframes fadeIn {
		from {
			opacity: 0;
			transform: translateY(-4px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	/* =====================================================================
	   HEADER
	   ===================================================================== */
	.page-header {
		display: flex;
		flex-direction: column; /* flex-col */
		justify-content: space-between;
		align-items: flex-start; /* items-start */
		gap: 1rem;
		margin-bottom: 2rem; /* mb-8 */
		padding-bottom: 1.5rem; /* pb-6 */
		border-bottom: 1px solid var(--md-outline-variant);
	}
	@media (min-width: 768px) {
		.page-header {
			flex-direction: row; /* md:flex-row */
			align-items: center; /* md:items-center */
		}
	}
	.page-title {
		font-size: 1.5rem; /* text-2xl */
		line-height: 2rem;
		font-weight: 800; /* font-extrabold */
		letter-spacing: -0.025em; /* tracking-tight */
		color: var(--md-on-surface);
	}
	@media (min-width: 768px) {
		.page-title {
			font-size: 1.875rem; /* md:text-3xl */
			line-height: 2.25rem;
		}
	}
	.page-subtitle {
		margin-top: 0.25rem; /* mt-1 */
		font-size: 0.875rem; /* text-sm */
		line-height: 1.25rem;
		color: var(--md-on-surface-variant);
	}

	/* =====================================================================
	   STATISTICS GRID
	   ===================================================================== */
	.stats-grid {
		display: grid;
		grid-template-columns: 1fr; /* grid-cols-1 */
		gap: 1.5rem; /* gap-6 */
		margin-bottom: 2rem; /* mb-8 */
	}
	@media (min-width: 640px) {
		.stats-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr)); /* sm:grid-cols-2 */
		}
	}
	@media (min-width: 1024px) {
		.stats-grid {
			grid-template-columns: repeat(4, minmax(0, 1fr)); /* lg:grid-cols-4 */
		}
	}

	.stat-card {
		display: flex;
		align-items: center;
		gap: 1.25rem; /* gap-5 */
		padding: 1.5rem; /* p-6 */
		border-radius: 1.5rem; /* rounded-3xl */
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-lowest);
		box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); /* shadow-sm */
		transition: all 0.15s ease; /* transition-all */
	}
	.stat-card__icon {
		width: 3rem; /* w-12 */
		height: 3rem; /* h-12 */
		border-radius: 1rem; /* rounded-2xl */
		display: flex;
		align-items: center;
		justify-content: center;
		flex: none; /* shrink-0 */
		border: 1px solid var(--md-outline-variant);
	}
	.stat-card__icon--primary {
		background-color: var(--md-primary-container);
		color: var(--md-on-primary-container);
	}
	.stat-card__icon--success {
		background-color: rgb(16 185 129 / 0.1);
		color: #10b981;
		border-color: rgb(16 185 129 / 0.2);
	}
	.stat-card__icon--warning {
		background-color: rgb(245 158 11 / 0.1); /* amber-500/10 */
		color: #f59e0b;
		border-color: rgb(245 158 11 / 0.2);
	}
	.stat-card__icon--danger {
		background-color: rgb(244 63 94 / 0.1);
		color: #f43f5e;
		border-color: rgb(244 63 94 / 0.2);
	}
	.stat-card__label {
		font-size: 0.75rem; /* text-xs */
		line-height: 1rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em; /* tracking-wider */
		margin-bottom: 0.25rem; /* mb-1 */
		color: var(--md-on-surface-variant);
	}
	.stat-card__value {
		font-size: 1.5rem; /* text-2xl */
		line-height: 2rem;
		font-weight: 800;
		color: var(--md-on-surface);
	}

	/* =====================================================================
	   PANEL (filters + table container)
	   ===================================================================== */
	.panel {
		padding: 2rem; /* p-8 */
		border-radius: 1.5rem; /* rounded-3xl */
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-lowest);
		box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); /* shadow-sm */
		transition: all 0.15s ease;
	}

	/* =====================================================================
	   TOOLBAR (search + filters)
	   ===================================================================== */
	.toolbar {
		display: flex;
		flex-direction: column; /* flex-col */
		gap: 1rem; /* gap-4 */
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem; /* mb-6 */
	}
	@media (min-width: 768px) {
		.toolbar {
			flex-direction: row; /* md:flex-row */
		}
	}

	/* Search */
	.search {
		position: relative;
		width: 100%; /* w-full */
	}
	@media (min-width: 768px) {
		.search {
			max-width: 28rem; /* md:max-w-md */
		}
	}
	.search__icon {
		position: absolute;
		top: 0;
		bottom: 0;
		left: 0; /* inset-y-0 left-0 */
		display: flex;
		align-items: center;
		padding-left: 0.75rem; /* pl-3 */
		pointer-events: none;
		color: var(--md-on-surface-variant);
	}
	.search__input {
		width: 100%;
		padding: 0.625rem 1rem 0.625rem 2.5rem; /* py-2.5 pr-4 pl-10 */
		border-radius: 0.75rem; /* rounded-xl */
		border: 1px solid var(--md-outline);
		background-color: var(--md-surface-container-low);
		color: var(--md-on-surface);
		font-size: 0.875rem; /* text-sm */
		line-height: 1.25rem;
		outline: none;
		transition: all 0.15s ease;
	}
	.search__input::placeholder {
		/* placeholder-(--md-on-surface-variant)/60 */
		color: color-mix(in srgb, var(--md-on-surface-variant) 60%, transparent);
	}
	.search__input:focus {
		/* focus:ring-2 focus:ring-(--md-primary) */
		box-shadow: 0 0 0 2px var(--md-primary);
	}
	.search__clear {
		position: absolute;
		right: 0.75rem; /* right-3 */
		top: 50%;
		transform: translateY(-50%); /* -translate-y-1/2 */
		padding: 0.25rem; /* p-1 */
		border-radius: 0.375rem; /* rounded-md */
		border: 0;
		background: transparent;
		font-size: 0.75rem; /* text-xs */
		font-weight: 700;
		color: var(--md-on-surface-variant);
		cursor: pointer;
	}
	.search__clear:hover {
		color: var(--md-on-surface);
	}

	/* Filter controls */
	.filter-controls {
		display: flex;
		flex-wrap: wrap; /* flex-wrap */
		align-items: center;
		gap: 0.75rem; /* gap-3 */
		width: 100%; /* w-full */
	}
	@media (min-width: 768px) {
		.filter-controls {
			width: auto; /* md:w-auto */
		}
	}
	.filter-controls__label {
		display: flex;
		align-items: center;
		gap: 0.5rem; /* gap-2 */
		font-size: 0.875rem; /* text-sm */
		color: var(--md-on-surface-variant);
	}
	.select {
		border-radius: 0.75rem; /* rounded-xl */
		border: 1px solid var(--md-outline);
		padding: 0.5rem 0.75rem; /* px-3 py-2 */
		font-size: 0.875rem; /* text-sm */
		background-color: var(--md-surface-container-low);
		color: var(--md-on-surface);
		outline: none;
		transition: all 0.15s ease;
	}
	.select:focus {
		box-shadow: 0 0 0 2px var(--md-primary);
	}

	/* =====================================================================
	   RUNNING EVENTS BANNER
	   ===================================================================== */
	.running-banner {
		display: flex;
		flex-direction: column; /* flex-col */
		align-items: flex-start;
		justify-content: space-between;
		gap: 1rem; /* gap-4 */
		margin-bottom: 1.5rem; /* mb-6 */
		padding: 1rem; /* p-4 */
		border-radius: 1rem; /* rounded-2xl */
		border: 1px solid rgb(249 115 22 / 0.2); /* orange-500/20 */
		background-color: rgb(249 115 22 / 0.1); /* orange-500/10 */
		color: var(--md-on-surface);
		transition: all 0.15s ease;
	}
	@media (min-width: 640px) {
		.running-banner {
			flex-direction: row; /* sm:flex-row */
			align-items: center; /* sm:items-center */
		}
	}
	.running-banner__main {
		display: flex;
		align-items: center;
		gap: 0.75rem; /* gap-3 */
	}
	.running-banner__icon-wrap {
		width: 2rem; /* w-8 */
		height: 2rem; /* h-8 */
		border-radius: 0.5rem; /* rounded-lg */
		display: flex;
		align-items: center;
		justify-content: center;
		flex: none;
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-primary-container);
		color: var(--md-on-primary-container);
	}
	.running-banner__icon {
		color: var(--md-primary);
	}
	.running-banner__title {
		font-size: 0.75rem; /* text-xs */
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;
		color: var(--md-primary);
	}
	.running-banner__list {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		column-gap: 0.5rem; /* gap-x-2 */
		row-gap: 0.25rem; /* gap-y-1 */
		margin-top: 0.125rem; /* mt-0.5 */
		font-size: 0.875rem; /* text-sm */
		font-weight: 600; /* font-semibold */
	}
	.running-banner__dot {
		/* text-(--md-on-surface-variant)/50 */
		color: color-mix(in srgb, var(--md-on-surface-variant) 50%, transparent);
	}

	/* =====================================================================
	   TABLE
	   ===================================================================== */
	.table-wrap {
		overflow-x: auto; /* overflow-x-auto */
	}
	.team-table {
		width: 100%;
		text-align: left; /* text-left */
		border-collapse: collapse; /* border-collapse */
	}
	.team-table__head-row {
		border-bottom: 1px solid var(--md-outline-variant);
		color: var(--md-on-surface-variant);
		font-size: 0.75rem; /* text-xs */
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}
	.team-table__th {
		padding: 1rem; /* py-4 px-4 */
	}
	.team-table__th--center {
		text-align: center; /* text-center */
	}
	.team-table__body {
		font-size: 0.875rem; /* text-sm */
		color: var(--md-on-surface);
	}
	.team-table__row {
		border-bottom: 1px solid color-mix(in srgb, var(--md-outline-variant) 50%, transparent); /* /50 */
		color: var(--md-on-surface);
		transition:
			background-color 0.15s ease,
			color 0.15s ease,
			border-color 0.15s ease; /* transition-colors */
	}
	.team-table__row:hover {
		/* hover:bg-(--md-surface-container-highest)/30 */
		background-color: color-mix(in srgb, var(--md-surface-container-highest) 30%, transparent);
	}
	.team-table__cell {
		padding: 1rem; /* py-4 px-4 */
		vertical-align: top;
	}
	.team-table__cell--bold {
		font-weight: 700;
	}
	.team-table__cell--semibold {
		font-weight: 600; /* font-semibold */
		font-size: 0.75rem; /* text-xs */
		color: var(--md-on-surface);
	}
	.team-table__cell--center {
		text-align: center;
	}
	.team-leader-name {
		font-weight: 700;
		font-size: 0.75rem; /* text-xs */
	}
	.team-leader-email {
		font-size: 0.625rem; /* text-[10px] */
		line-height: 1rem;
		margin-top: 0.125rem; /* mt-0.5 */
		color: var(--md-on-surface-variant);
	}

	/* Loading state */
	.table-loading {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 0.75rem; /* gap-3 */
	}
	.table-loading__text {
		font-size: 0.875rem;
		color: var(--md-on-surface-variant);
	}
	.spinner {
		width: 2.5rem; /* h-10 w-10 */
		height: 2.5rem;
		border-radius: 9999px; /* rounded-full */
		border-top: 2px solid var(--md-primary); /* border-t-2 */
		border-bottom: 2px solid var(--md-primary); /* border-b-2 */
		border-left: 2px solid transparent;
		border-right: 2px solid transparent;
		animation: spin 0.6s linear infinite; /* animate-spin */
	}
	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	/* Empty state */
	.table-empty {
		padding: 3rem 1rem; /* py-12 */
		color: var(--md-on-surface-variant);
	}
	.table-empty__title {
		font-weight: 600;
		font-size: 1.125rem; /* text-lg */
	}
	.table-empty__hint {
		font-size: 0.875rem; /* text-sm */
	}

	/* =====================================================================
	   STATUS BADGES
	   ===================================================================== */
	.badge {
		display: inline-flex; /* inline-flex */
		align-items: center;
		gap: 0.375rem; /* gap-1.5 */
		padding: 0.25rem 0.625rem; /* px-2.5 py-1 */
		border-radius: 9999px; /* rounded-full */
		font-size: 0.75rem; /* text-xs */
		font-weight: 600; /* font-semibold */
		border: 1px solid;
	}
	.badge--approved {
		background-color: rgb(16 185 129 / 0.1);
		color: #10b981;
		border-color: rgb(16 185 129 / 0.2);
	}
	.badge--pending {
		background-color: rgb(245 158 11 / 0.1);
		/* NOTE: original had `text-amber-505` (typo) — converted to amber-500 */
		color: #f59e0b;
		border-color: rgb(245 158 11 / 0.2);
	}
	.badge--rejected {
		background-color: rgb(244 63 94 / 0.1);
		color: #f43f5e;
		border-color: rgb(244 63 94 / 0.2);
	}

	/* =====================================================================
	   ROW ACTIONS & BUTTONS
	   ===================================================================== */
	.row-actions {
		display: flex;
		justify-content: center;
		gap: 0.5rem; /* gap-2 */
	}
	.icon-btn {
		padding: 0.5rem; /* p-2 */
		border-radius: 0.5rem; /* rounded-lg */
		border: 0;
		background: transparent;
		color: var(--md-on-surface-variant);
		cursor: pointer;
		transition:
			background-color 0.15s ease,
			color 0.15s ease;
	}
	.icon-btn:hover {
		background-color: var(--md-surface-container-high);
		color: var(--md-on-surface);
	}
	.btn {
		padding: 0.375rem 0.625rem; /* px-2.5 py-1.5 */
		border-radius: 0.5rem; /* rounded-lg */
		border: 0;
		font-size: 0.75rem; /* text-xs */
		font-weight: 700; /* font-bold */
		color: #fff;
		cursor: pointer;
		transition: background-color 0.15s ease;
	}
	.btn--approve {
		background-color: #10b981; /* emerald-500 */
	}
	.btn--approve:hover {
		background-color: #059669; /* emerald-600 */
	}
	.btn--reject {
		background-color: #f43f5e; /* rose-500 */
	}
	.btn--reject:hover {
		background-color: #e11d48; /* rose-600 */
	}

	/* =====================================================================
	   MODAL
	   ===================================================================== */
	.modal-overlay {
		position: fixed;
		inset: 0; /* inset-0 */
		z-index: 2000; /* z-[2000] */
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 1rem; /* p-4 */
		background-color: rgb(0 0 0 / 0.4); /* bg-black/40 */
		backdrop-filter: blur(2px); /* backdrop-blur-[2px] */
	}
	.modal {
		position: relative;
		width: 100%;
		max-width: 32rem; /* max-w-lg */
		padding: 2rem; /* p-8 */
		border-radius: 1.5rem; /* rounded-3xl */
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-low);
		color: var(--md-on-surface);
		box-shadow: 0 25px 50px -12px rgb(0 0 0 / 0.25); /* shadow-2xl */
		transition: all 0.15s ease;
	}
	.modal__close {
		position: absolute;
		top: 1rem; /* top-4 */
		right: 1rem; /* right-4 */
		padding: 0.25rem; /* p-1 */
		border-radius: 9999px; /* rounded-full */
		border: 0;
		background: transparent;
		color: var(--md-on-surface-variant);
		cursor: pointer;
	}
	.modal__close:hover {
		background-color: var(--md-surface-container-high);
		color: var(--md-on-surface);
	}
	.modal__title {
		display: flex;
		align-items: center;
		gap: 0.5rem; /* gap-2 */
		margin-bottom: 1.5rem; /* mb-6 */
		font-size: 1.25rem; /* text-xl */
		line-height: 1.75rem;
		font-weight: 800; /* font-extrabold */
	}
	.modal__title-icon {
		color: var(--md-primary);
	}
	.modal__body {
		display: flex;
		flex-direction: column;
		gap: 1rem; /* space-y-4 */
	}
	.modal__section {
		padding: 1rem; /* p-4 */
		border-radius: 0.75rem; /* rounded-xl */
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-high);
	}
	.modal__section-label {
		display: block;
		font-size: 0.75rem; /* text-xs */
		font-weight: 600; /* font-semibold */
		text-transform: uppercase;
		letter-spacing: 0.05em;
		color: var(--md-on-surface-variant);
	}
	.modal__section-label--with-icon {
		display: flex;
		align-items: center;
		gap: 0.25rem; /* gap-1 */
	}
	.modal__section-value {
		margin-top: 0.25rem; /* mt-1 */
		font-weight: 700;
		font-size: 0.875rem; /* text-sm */
	}
	.modal__section-value--primary {
		color: var(--md-primary);
	}
	.modal__grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr)); /* grid-cols-2 */
		gap: 1rem; /* gap-4 */
	}
	.modal__footer-btn {
		margin-top: 2rem; /* mt-8 */
		width: 100%;
		padding: 0.75rem 0; /* py-3 */
		border-radius: 0.75rem; /* rounded-xl */
		border: 1px solid var(--md-outline);
		background-color: var(--md-surface-container-highest);
		color: var(--md-on-surface);
		font-weight: 600;
		cursor: pointer;
		transition: opacity 0.15s ease;
	}
	.modal__footer-btn:hover {
		opacity: 0.9;
	}
</style>
