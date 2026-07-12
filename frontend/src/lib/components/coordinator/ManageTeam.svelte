<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
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

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Success & Error Banners -->
	{#if successMessage}
		<div
			class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 p-4 rounded-2xl shadow-lg animate-fade-in max-w-md backdrop-blur-md"
		>
			<CheckCircle2 class="w-5 h-5" />
			<div>
				<p class="text-sm font-bold text-emerald-500">Success</p>
				<p class="text-xs mt-0.5">{successMessage}</p>
			</div>
		</div>
	{/if}

	{#if errorMessage}
		<div
			class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-rose-500/10 border border-rose-500/20 text-rose-500 p-4 rounded-2xl shadow-lg animate-fade-in max-w-md backdrop-blur-md"
		>
			<AlertTriangle class="w-5 h-5" />
			<div>
				<p class="text-sm font-bold text-rose-500">Alert</p>
				<p class="text-xs mt-0.5">{errorMessage}</p>
			</div>
		</div>
	{/if}

	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-6 border-b border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Team Management
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Review, approve, and track student teams participating in the hackathons.
			</p>
		</div>
	</header>

	<!-- Statistics Cards -->
	<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
		<div
			class="p-6 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all shadow-sm"
		>
			<div
				class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
			>
				<Users class="w-6 h-6" />
			</div>
			<div>
				<p class="text-xs font-bold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
					Total Teams
				</p>
				<h3 class="text-2xl font-extrabold text-(--md-on-surface)">
					{totalTeams}
				</h3>
			</div>
		</div>

		<div
			class="p-6 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all shadow-sm"
		>
			<div
				class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
			>
				<UserCheck class="w-6 h-6" />
			</div>
			<div>
				<p class="text-xs font-bold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
					Approved
				</p>
				<h3 class="text-2xl font-extrabold text-(--md-on-surface)">
					{approvedTeams}
				</h3>
			</div>
		</div>

		<div
			class="p-6 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all shadow-sm"
		>
			<div
				class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 bg-amber-500/10 text-amber-500 border border-amber-500/20"
			>
				<Users class="w-6 h-6" />
			</div>
			<div>
				<p class="text-xs font-bold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
					Pending
				</p>
				<h3 class="text-2xl font-extrabold text-(--md-on-surface)">
					{pendingTeams}
				</h3>
			</div>
		</div>

		<div
			class="p-6 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex items-center gap-5 transition-all shadow-sm"
		>
			<div
				class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 bg-rose-500/10 text-rose-500 border border-rose-500/20"
			>
				<Users class="w-6 h-6" />
			</div>
			<div>
				<p class="text-xs font-bold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)">
					Rejected
				</p>
				<h3 class="text-2xl font-extrabold text-(--md-on-surface)">
					{rejectedTeams}
				</h3>
			</div>
		</div>
	</div>

	<!-- Filters and Table Container -->
	<div
		class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-all shadow-sm"
	>
		<!-- Filters Toolbar -->
		<div class="flex flex-col md:flex-row gap-4 justify-between items-center mb-6">
			<!-- Search bar -->
			<div class="relative w-full md:max-w-md">
				<span
					class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-(--md-on-surface-variant)"
				>
					<Search class="w-4 h-4" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search by team name, leader..."
					class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-(--md-outline) bg-(--md-surface-container-low) text-(--md-on-surface) placeholder-(--md-on-surface-variant)/60 focus:ring-2 focus:ring-(--md-primary) outline-none transition-all text-sm"
				/>
				{#if searchQuery}
					<button
						type="button"
						onclick={() => (searchQuery = "")}
						class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-md text-xs font-bold text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer border-0 bg-transparent"
					>
						Clear
					</button>
				{/if}
			</div>

			<!-- selectors -->
			<div class="flex flex-wrap items-center gap-3 w-full md:w-auto">
				<div class="flex items-center gap-2 text-sm text-(--md-on-surface-variant)">
					<Filter class="w-4 h-4" />
					Filters:
				</div>
				<!-- Status Filter -->
				<select
					bind:value={statusFilter}
					class="rounded-xl border border-(--md-outline) px-3 py-2 text-sm bg-(--md-surface-container-low) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
				>
					<option value="ALL">All Statuses</option>
					<option value="APPROVED">Approved</option>
					<option value="PENDING">Pending</option>
					<option value="REJECTED">Rejected</option>
				</select>

				<!-- Season Filter -->
				<select
					bind:value={seasonFilter}
					class="rounded-xl border border-(--md-outline) px-3 py-2 text-sm bg-(--md-surface-container-low) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) outline-none transition-all"
				>
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
			<div
				class="mb-6 p-4 rounded-2xl border flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all bg-orange-500/10 border-orange-500/20 text-(--md-on-surface)"
			>
				<div class="flex items-center gap-3">
					<div
						class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0 bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
					>
						<Clock class="w-4.5 h-4.5 text-(--md-primary)" />
					</div>
					<div>
						<p class="text-xs font-bold uppercase tracking-wider text-(--md-primary)">
							Running Events in {seasonFilter === "ALL" ? "Current Season" : "Selected Season"}
						</p>
						<div class="flex flex-wrap items-center gap-x-2 gap-y-1 mt-0.5 text-sm font-semibold">
							{#each runningEvents as event, i}
								<span>
									{event.name}
								</span>
								{#if i < runningEvents.length - 1}
									<span class="text-(--md-on-surface-variant)/50">&bull;</span>
								{/if}
							{/each}
						</div>
					</div>
				</div>
			</div>
		{/if}

		<!-- Table -->
		<div class="overflow-x-auto">
			<table class="w-full text-left border-collapse">
				<thead>
					<tr
						class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
					>
						<th class="py-4 px-4">Team Name</th>
						<th class="py-4 px-4">Event</th>
						<th class="py-4 px-4">Leader</th>
						<th class="py-4 px-4">Members</th>
						<th class="py-4 px-4">Status</th>
						<th class="py-4 px-4 text-center">Actions</th>
					</tr>
				</thead>
				<tbody class="text-sm">
					{#if isLoading}
						<tr>
							<td colspan="6" class="py-16 text-center">
								<div class="flex flex-col items-center justify-center gap-3">
									<div
										class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-(--md-primary)"
									></div>
									<span class="text-sm text-(--md-on-surface-variant)"
										>Loading team management data...</span
									>
								</div>
							</td>
						</tr>
					{:else if filteredTeams.length > 0}
						{#each filteredTeams as team}
							<tr
								class="border-b border-(--md-outline-variant)/50 transition-colors hover:bg-(--md-surface-container-highest)/30 text-(--md-on-surface)"
							>
								<td class="py-4 px-4 font-bold">
									{team.name}
								</td>
								<td class="py-4 px-4 font-semibold text-xs text-(--md-on-surface)">
									{team.event ? team.event.name || team.event : team.eventName || "General Event"}
								</td>
								<td class="py-4 px-4">
									<p class="font-bold text-xs">{team.leaderName}</p>
									<p class="text-[10px] mt-0.5 text-(--md-on-surface-variant)">
										{team.leader}
									</p>
								</td>
								<td class="py-4 px-4 font-semibold text-xs text-(--md-on-surface)">
									{team.membersCount}/{team.maxMembers}
								</td>
								<td class="py-4 px-4">
									{#if team.status === "APPROVED"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
										>
											Approved
										</span>
									{:else if team.status === "PENDING"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-505 border border-amber-500/20"
										>
											Pending
										</span>
									{:else if team.status === "REJECTED"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-500 border border-rose-500/20"
										>
											Rejected
										</span>
									{/if}
								</td>
								<td class="py-4 px-4 text-center">
									<div class="flex justify-center gap-2">
										<button
											onclick={() => openViewModal(team)}
											title="View Details"
											class="p-2 rounded-lg hover:bg-(--md-surface-container-high) text-(--md-on-surface-variant) hover:text-(--md-on-surface) transition-colors cursor-pointer border-0 bg-transparent"
										>
											<Eye class="w-4 h-4" />
										</button>
										{#if team.status === "PENDING"}
											<button
												onclick={() => handleApproveTeam(team.id, team.eventId)}
												title="Approve Team"
												class="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-emerald-500 hover:bg-emerald-600 text-white cursor-pointer border-0 transition-colors"
											>
												Approve
											</button>
											<button
												onclick={() => handleRejectTeam(team.id, team.eventId)}
												title="Reject Team"
												class="px-2.5 py-1.5 rounded-lg text-xs font-bold bg-rose-500 hover:bg-rose-600 text-white cursor-pointer border-0 transition-colors"
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
							<td colspan="6" class="py-12 text-center text-(--md-on-surface-variant)">
								<p class="font-semibold text-lg">No teams found</p>
								<p class="text-sm">Try adjusting your filters or search terms.</p>
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
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4"
	>
		<div
			class="w-full max-w-lg rounded-3xl border border-(--md-outline-variant) p-8 relative transition-all shadow-2xl bg-(--md-surface-container-low) text-(--md-on-surface)"
		>
			<button
				onclick={() => (showViewModal = false)}
				class="absolute top-4 right-4 p-1 rounded-full hover:bg-(--md-surface-container-high) border-0 bg-transparent text-(--md-on-surface-variant) hover:text-(--md-on-surface) cursor-pointer"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-extrabold mb-6 flex items-center gap-2">
				<Users class="w-5 h-5 text-(--md-primary)" />
				Team Details - {selectedTeam.name}
			</h3>

			<div class="space-y-4">
				<div
					class="p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high)"
				>
					<p class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider">
						Event
					</p>
					<p class="font-bold text-sm mt-1">
						{selectedTeam.event
							? selectedTeam.event.name || selectedTeam.event
							: selectedTeam.eventName || "General Event"}
					</p>
				</div>

				<div
					class="p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high)"
				>
					<p
						class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider flex items-center gap-1"
					>
						<Mail class="w-3.5 h-3.5" />
						Leader Email
					</p>
					<p class="font-bold text-sm mt-1 text-(--md-primary)">
						{selectedTeam.leaderName} ({selectedTeam.leader})
					</p>
				</div>

				<div class="grid grid-cols-2 gap-4">
					<div
						class="p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high)"
					>
						<p
							class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider"
						>
							Members Count
						</p>
						<p class="font-bold text-sm mt-1">
							{selectedTeam.membersCount} / {selectedTeam.maxMembers}
						</p>
					</div>
					<div
						class="p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high)"
					>
						<p
							class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider"
						>
							Status
						</p>
						<p class="font-bold text-sm mt-1">{selectedTeam.status}</p>
					</div>
				</div>
			</div>

			<button
				onclick={() => (showViewModal = false)}
				class="mt-8 bg-(--md-surface-container-highest) hover:opacity-90 transition-all border border-(--md-outline) text-(--md-on-surface) rounded-xl py-3 font-semibold w-full cursor-pointer"
			>
				Close
			</button>
		</div>
	</div>
{/if}

<style>
	@keyframes fadeIn {
		from {
			opacity: 0;
			transform: translateY(-10px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
	.animate-fade-in {
		animation: fadeIn 0.3s ease-out forwards;
	}
</style>
