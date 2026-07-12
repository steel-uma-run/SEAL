<script lang="ts">
	import { onMount } from "svelte"
	import { getAllAccounts, getAllSeasons, getEventsInSeason, getAllTeamsOfEvents } from "$lib/api"
	import { Plus, X, Shield, Edit2, Eye, User, Mail, Search } from "@lucide/svelte"

	// 1. DATA LOADER & STATES
	let expertsList = $state<any[]>([])
	let isLoading = $state(true)
	let searchQuery = $state("")

	async function loadLecturersData() {
		isLoading = true
		try {
			// Fetch all accounts
			const accountsRes = await getAllAccounts({ throwOnError: false })
			let lecturers: any[] = []
			if (accountsRes.response?.ok && accountsRes.data) {
				lecturers = accountsRes.data.filter(
					(u: any) => u.role === "LECTURER" || u.role === "Lecturer"
				)
			}

			// Fetch all teams across all seasons & events
			let allTeams: any[] = []
			const seasonsRes = await getAllSeasons({ throwOnError: false })
			if (seasonsRes.response?.ok && seasonsRes.data) {
				for (const season of seasonsRes.data) {
					const seasonId = season.id
					let seasonEvents: any[] = []
					const eventsRes = await getEventsInSeason({ path: { seasonId }, throwOnError: false })
					if (eventsRes.response?.ok && eventsRes.data) {
						seasonEvents = eventsRes.data
					} else if (typeof window !== "undefined") {
						const localEvents = localStorage.getItem(`events_${seasonId}`)
						if (localEvents) {
							seasonEvents = JSON.parse(localEvents)
						}
					}

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
											} else {
												eventTeams = eventTeams.map((et) => {
													if (et.id === t.id) {
														return { ...et, ...t }
													}
													return et
												})
											}
										}
									}
								} catch (e) {
									console.error("Error parsing local teams:", e)
								}
							}
						}
						allTeams.push(...eventTeams)
					}
				}
			}

			// Map lecturers with their assigned teams count
			expertsList = lecturers.map((lecturer: any) => {
				const mentoredTeams = allTeams.filter(
					(t) => t.mentor_id === lecturer.id || t.mentorId === lecturer.id
				)
				const judgedTeams = allTeams.filter(
					(t) =>
						(t.judge_ids && t.judge_ids.includes(lecturer.id)) ||
						(t.judgeIds && t.judgeIds.includes(lecturer.id))
				)

				const mentoredCount = mentoredTeams.length
				const judgedCount = judgedTeams.length
				const totalAssigned = mentoredCount + judgedCount

				let displayRole = "Lecturer"
				if (mentoredCount > 0 && judgedCount > 0) {
					displayRole = "Mentor & Judge"
				} else if (mentoredCount > 0) {
					displayRole = "Mentor"
				} else if (judgedCount > 0) {
					displayRole = "Judge"
				}

				return {
					id: lecturer.id,
					name: lecturer.name || lecturer.fullName || "Unnamed Lecturer",
					email: lecturer.email,
					role: displayRole,
					assignedTeamsCount: totalAssigned,
					assignedTeams: [
						...mentoredTeams.map((t) => `${t.name} (Mentor)`),
						...judgedTeams.map((t) => `${t.name} (Judge)`)
					]
				}
			})
		} catch (err) {
			console.error("Error loading lecturers:", err)
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadLecturersData()
	})

	// 2. SEARCH FILTER
	let filteredExperts = $derived(
		expertsList.filter((exp) => {
			const q = searchQuery.trim().toLowerCase()
			if (!q) return true
			return (
				exp.name.toLowerCase().includes(q) ||
				exp.role.toLowerCase().includes(q) ||
				exp.email.toLowerCase().includes(q)
			)
		})
	)

	// 3. STATE CHO MODAL FORM
	let showExpertModal = $state(false)
	let isExpertLoading = $state(false)
	let expertMessage = $state("")

	let editingId = $state<string | null>(null)
	let formName = $state("")
	let formEmail = $state("")

	function openExpertForm(expert: any = null) {
		expertMessage = ""
		if (expert) {
			editingId = expert.id
			formName = expert.name
			formEmail = expert.email || ""
		} else {
			editingId = null
			formName = ""
			formEmail = ""
		}
		showExpertModal = true
	}

	function closeExpertForm() {
		showExpertModal = false
	}

	function handleSaveExpert(e: Event) {
		e.preventDefault()
		isExpertLoading = true
		expertMessage = ""

		setTimeout(() => {
			if (editingId) {
				expertsList = expertsList.map((exp) =>
					exp.id === editingId
						? {
								...exp,
								name: formName,
								email: formEmail
							}
						: exp
				)
				expertMessage = "Lecturer details updated successfully!"
			} else {
				const newExpert = {
					id: Date.now().toString(),
					name: formName,
					role: "Lecturer",
					email: formEmail,
					assignedTeamsCount: 0,
					assignedTeams: []
				}
				expertsList = [newExpert, ...expertsList]
				expertMessage = "New lecturer account created!"
			}

			isExpertLoading = false
			setTimeout(() => {
				closeExpertForm()
			}, 1200)
		}, 600)
	}

	// 4. LOGIC VIEW PROFILE
	let viewingExpert = $state<any>(null)

	function openProfile(expert: any) {
		viewingExpert = expert
	}
	function closeProfile() {
		viewingExpert = null
	}
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full font-sans text-(--md-on-surface)">
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Lecturer Management
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Manage, edit, and assign lecturers for hackathon tracks.
			</p>
		</div>
		<div class="flex items-center gap-3 mt-4 md:mt-0">
			<button
				onclick={() => openExpertForm(null)}
				class="flex items-center gap-2 bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-5 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer border-0 shadow-none"
			>
				<Plus class="w-4 h-4" />
				Add Lecturer
			</button>
		</div>
	</header>

	<div
		class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-all shadow-none"
	>
		<div
			class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6 pb-6 border-b border-(--md-outline-variant)"
		>
			<div class="relative w-full sm:w-80">
				<span
					class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-(--md-on-surface-variant)"
				>
					<Search class="w-4 h-4" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search by ID, name, or email..."
					class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-(--md-outline) bg-(--md-surface-container-low) text-(--md-on-surface) placeholder-(--md-on-surface-variant)/60 focus:ring-2 focus:ring-(--md-primary) outline-none transition-all text-sm shadow-none"
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

			<div class="text-xs font-semibold text-(--md-on-surface-variant)">
				Showing <strong class="text-(--md-primary) font-black">{filteredExperts.length}</strong> of {expertsList.length}
				lecturers
			</div>
		</div>

		<div class="overflow-x-auto">
			<table class="w-full text-left border-collapse min-w-[800px]">
				<thead>
					<tr
						class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
					>
						<th class="py-3.5 px-4">Name</th>
						<th class="py-3.5 px-4">Role</th>
						<th class="py-3.5 px-4">Assigned Teams</th>
						<th class="py-3.5 px-4">Actions</th>
					</tr>
				</thead>
				<tbody class="text-sm">
					{#if isLoading}
						<tr>
							<td colspan="4" class="py-16 text-center">
								<div class="flex flex-col items-center justify-center gap-3">
									<div
										class="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-(--md-primary)"
									></div>
									<span class="text-sm text-(--md-on-surface-variant)"
										>Loading lecturer data...</span
									>
								</div>
							</td>
						</tr>
					{:else}
						{#if filteredExperts.length === 0}
							<tr>
								<td colspan="4" class="py-16 text-center">
									<div class="max-w-xs mx-auto space-y-2">
										<p class="text-3xl">🔍</p>
										<p class="font-bold text-(--md-on-surface)">No lecturers found</p>
										<p class="text-xs text-(--md-on-surface-variant)">
											We couldn't find anyone matching <strong class="text-(--md-primary)"
												>"{searchQuery}"</strong
											>.
										</p>
									</div>
								</td>
							</tr>
						{/if}
					{/if}

					{#each filteredExperts as expert (expert.id)}
						<tr
							class="border-b border-(--md-outline-variant)/50 transition-colors hover:bg-(--md-surface-container-highest)/30 text-(--md-on-surface)"
						>
							<td class="py-4 px-4 font-bold">{expert.name}</td>

							<td class="py-4 px-4 text-xs font-semibold text-(--md-on-surface-variant)">
								{expert.role}
							</td>

							<td class="py-4 px-4 font-semibold text-xs">
								{#if expert.assignedTeamsCount > 0}
									<span
										class="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 shadow-none"
									>
										{expert.assignedTeamsCount}
										{expert.assignedTeamsCount === 1 ? "team" : "teams"}
									</span>
								{:else}
									<span class="text-xs italic opacity-50">None</span>
								{/if}
							</td>

							<td class="py-4 px-4">
								<div class="flex gap-2">
									<button
										onclick={() => openExpertForm(expert)}
										class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) hover:bg-(--md-surface-container-high) shadow-none"
									>
										<Edit2 class="w-3.5 h-3.5" /> Edit
									</button>

									<button
										onclick={() => openProfile(expert)}
										class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) hover:bg-(--md-surface-container-high) shadow-none"
									>
										<Eye class="w-3.5 h-3.5" /> View Profile
									</button>
								</div>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

{#if showExpertModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4 font-sans text-(--md-on-surface)"
	>
		<div
			class="w-full max-w-lg rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) p-8 relative transition-all shadow-none"
		>
			<button
				type="button"
				onclick={closeExpertForm}
				class="absolute top-4 right-4 p-1.5 rounded-lg hover:bg-(--md-surface-container-high) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Shield class="w-5 h-5 text-(--md-primary)" />
				{editingId ? "Edit Lecturer Details" : "Add New Lecturer"}
			</h3>
			<form onsubmit={handleSaveExpert} class="flex flex-col gap-4">
				<div class="space-y-1.5">
					<label class="text-sm font-semibold text-(--md-on-surface-variant)">Full Name *</label>
					<input
						type="text"
						bind:value={formName}
						required
						placeholder="e.g. Dr. John Doe"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-container-low) text-(--md-on-surface) p-3 outline-none transition-all focus:border-(--md-primary)"
					/>
				</div>
				<div class="space-y-1.5">
					<label class="text-sm font-semibold text-(--md-on-surface-variant)">Email Address *</label
					>
					<input
						type="email"
						bind:value={formEmail}
						required
						placeholder="email@fpt.edu.vn"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-container-low) text-(--md-on-surface) p-3 outline-none transition-all focus:border-(--md-primary)"
					/>
				</div>

				{#if expertMessage}
					<div
						class="p-3 mt-2 rounded-xl text-sm font-bold border {expertMessage.includes(
							'success'
						) || expertMessage.includes('created')
							? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-500'
							: 'bg-rose-500/10 border-rose-500/20 text-rose-500'}"
					>
						{expertMessage}
					</div>
				{/if}
				<div class="flex gap-3 mt-4 pt-2 border-t border-(--md-outline-variant)">
					<button
						type="button"
						onclick={closeExpertForm}
						class="w-1/2 rounded-xl border border-(--md-outline) bg-(--md-surface-container-highest) text-(--md-on-surface) py-3 font-bold transition-all cursor-pointer hover:opacity-90 shadow-none"
						>Cancel</button
					>
					<button
						type="submit"
						disabled={isExpertLoading}
						class="w-1/2 bg-(--md-primary) text-(--md-on-primary) rounded-xl py-3 font-bold disabled:opacity-50 transition-all border-0 cursor-pointer hover:opacity-95 shadow-none"
						>{isExpertLoading ? "Saving..." : "Save"}</button
					>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if viewingExpert}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-[2px] p-4 font-sans text-(--md-on-surface)"
	>
		<div
			class="w-full max-w-sm rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) relative transition-all shadow-none overflow-hidden"
		>
			<div class="h-24 bg-(--md-primary-container) relative">
				<button
					type="button"
					onclick={closeProfile}
					class="absolute top-4 right-4 p-1.5 rounded-full bg-black/20 hover:bg-black/40 text-white cursor-pointer border-0 z-10"
					><X class="w-4 h-4" /></button
				>
			</div>
			<div class="px-6 relative flex justify-center -mt-12 mb-4">
				<div
					class="w-24 h-24 rounded-full border-4 border-(--md-surface-container-low) bg-(--md-surface-container-high) text-(--md-on-surface) flex items-center justify-center text-3xl font-black shadow-none"
				>
					{viewingExpert.name.charAt(0).toUpperCase()}
				</div>
			</div>
			<div class="px-6 pb-8 text-center">
				<h3 class="text-xl font-extrabold text-(--md-on-surface)">
					{viewingExpert.name}
				</h3>
				<span
					class="inline-block mt-1.5 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider {viewingExpert.role.includes(
						'Judge'
					)
						? 'bg-blue-500/10 text-blue-500 border border-blue-500/20'
						: 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'}"
					>{viewingExpert.role}</span
				>

				<div class="mt-6 space-y-3 text-left">
					<div
						class="flex items-center gap-3 p-3 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface)"
					>
						<Mail class="w-4 h-4 text-(--md-on-surface-variant)" />
						<div>
							<p class="text-[10px] font-bold uppercase text-(--md-on-surface-variant)">Email</p>
							<p class="text-sm font-semibold">{viewingExpert.email}</p>
						</div>
					</div>

					<div
						class="flex items-center gap-3 p-3 rounded-xl border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface)"
					>
						<User class="w-4 h-4 text-(--md-on-surface-variant)" />
						<div>
							<p class="text-[10px] font-bold uppercase text-(--md-on-surface-variant)">Teams</p>
							<p class="text-sm font-semibold">
								{viewingExpert.assignedTeams.length > 0
									? viewingExpert.assignedTeams.join(", ")
									: "None"}
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
{/if}
