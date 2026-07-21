<script lang="ts">
	import { onMount } from "svelte"
	import {
		getAllAccounts,
		getAllSeasons,
		getEventsInSeason,
		getAllTeamsOfEvents,
		createLecturer
	} from "$lib/api"
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

	async function handleSaveExpert(e: Event) {
		e.preventDefault()
		isExpertLoading = true
		expertMessage = ""

		try {
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
				isExpertLoading = false
				setTimeout(() => {
					closeExpertForm()
				}, 1200)
			} else {
				const { data, response } = await createLecturer({
					body: {
						email: formEmail,
						name: formName
					}
				})

				if (response?.ok && data) {
					expertMessage = "New lecturer account created in database!"
					await loadLecturersData()
					isExpertLoading = false
					setTimeout(() => {
						closeExpertForm()
					}, 1200)
				} else {
					let detail = ""
					if (response && response.status === 409) {
						detail = "This email is already registered."
					} else {
						detail = "Failed to create lecturer account."
					}
					expertMessage = "Error: " + detail
					isExpertLoading = false
				}
			}
		} catch (err: any) {
			console.error("Error saving lecturer:", err)
			let detail = ""
			if (err.body) {
				detail = err.body.detail || err.body.title || JSON.stringify(err.body)
			} else {
				detail = err.message || "Failed to save."
			}
			expertMessage = "Error: " + detail
			isExpertLoading = false
		}
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

<div class="lecturers-page">
	<header class="page-header">
		<div>
			<h1 class="page-title">Lecturer Management</h1>
			<p class="page-subtitle">Manage, edit, and assign lecturers for hackathon tracks.</p>
		</div>
		<div class="header-actions">
			<button type="button" onclick={() => openExpertForm(null)} class="btn-add">
				<Plus class="btn-add-icon" />
				Add Lecturer
			</button>
		</div>
	</header>

	<div class="panel">
		<div class="toolbar">
			<div class="search-field">
				<span class="search-icon">
					<Search class="search-icon-svg" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search by ID, name, or email..."
					class="search-input"
				/>
				{#if searchQuery}
					<button type="button" onclick={() => (searchQuery = "")} class="search-clear">
						Clear
					</button>
				{/if}
			</div>
			<div class="result-count">
				Showing <strong class="count-highlight">{filteredExperts.length}</strong> of
				{expertsList.length} lecturers
			</div>
		</div>

		<div class="table-scroll">
			<table class="data-table">
				<thead>
					<tr class="table-head-row">
						<th class="table-head-cell">Name</th>
						<th class="table-head-cell">Role</th>
						<th class="table-head-cell">Assigned Teams</th>
						<th class="table-head-cell">Actions</th>
					</tr>
				</thead>
				<tbody class="table-body">
					{#if isLoading}
						<tr>
							<td colspan="4" class="state-cell">
								<div class="state-loading">
									<div class="spinner spinner--primary"></div>
									<span class="state-text">Loading lecturer data...</span>
								</div>
							</td>
						</tr>
					{:else}
						{#if filteredExperts.length === 0}
							<tr>
								<td colspan="4" class="state-cell">
									<div class="empty-state">
										<p class="empty-emoji">🔍</p>
										<p class="empty-title">No lecturers found</p>
										<p class="empty-desc">
											We couldn't find anyone matching
											<strong class="empty-highlight">"{searchQuery}"</strong>.
										</p>
									</div>
								</td>
							</tr>
						{/if}
					{/if}
					{#each filteredExperts as expert (expert.id)}
						<tr class="data-row">
							<td class="cell-name">{expert.name}</td>
							<td class="cell-role">{expert.role}</td>
							<td class="cell-teams">
								{#if expert.assignedTeamsCount > 0}
									<span class="team-badge">
										{expert.assignedTeamsCount}
										{expert.assignedTeamsCount === 1 ? "team" : "teams"}
									</span>
								{:else}
									<span class="team-none">None</span>
								{/if}
							</td>
							<td class="cell-actions">
								<div class="action-group">
									<button type="button" onclick={() => openExpertForm(expert)} class="btn-action">
										<Edit2 class="action-icon" /> Edit
									</button>
									<button type="button" onclick={() => openProfile(expert)} class="btn-action">
										<Eye class="action-icon" /> View Profile
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
	<div class="modal-overlay">
		<div class="modal-card">
			<button type="button" onclick={closeExpertForm} class="modal-close">
				<X class="modal-close-icon" />
			</button>
			<h3 class="modal-title">
				<Shield class="modal-title-icon" />
				{editingId ? "Edit Lecturer Details" : "Add New Lecturer"}
			</h3>
			<form onsubmit={handleSaveExpert} class="modal-form">
				<div class="form-field">
					<label class="form-label">Full Name *</label>
					<input
						type="text"
						bind:value={formName}
						required
						placeholder="e.g. Dr. John Doe"
						class="form-input"
					/>
				</div>
				<div class="form-field">
					<label class="form-label">Email Address *</label>
					<input
						type="email"
						bind:value={formEmail}
						required
						placeholder="email@fpt.edu.vn"
						class="form-input"
					/>
				</div>
				{#if expertMessage}
					<div
						class="form-message"
						class:form-message--success={expertMessage.includes("success") ||
							expertMessage.includes("created")}
						class:form-message--error={!(
							expertMessage.includes("success") || expertMessage.includes("created")
						)}
					>
						{expertMessage}
					</div>
				{/if}
				<div class="modal-footer">
					<button type="button" onclick={closeExpertForm} class="btn-cancel"> Cancel </button>
					<button type="submit" disabled={isExpertLoading} class="btn-save">
						{isExpertLoading ? "Saving..." : "Save"}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if viewingExpert}
	<div class="modal-overlay">
		<div class="profile-card">
			<div class="profile-banner">
				<button type="button" onclick={closeProfile} class="profile-close">
					<X class="profile-close-icon" />
				</button>
			</div>
			<div class="profile-avatar-wrap">
				<div class="profile-avatar">
					{viewingExpert.name.charAt(0).toUpperCase()}
				</div>
			</div>
			<div class="profile-content">
				<h3 class="profile-name">{viewingExpert.name}</h3>
				<span
					class="profile-role"
					class:profile-role--judge={viewingExpert.role.includes("Judge")}
					class:profile-role--standard={!viewingExpert.role.includes("Judge")}
				>
					{viewingExpert.role}
				</span>
				<div class="profile-info">
					<div class="info-row">
						<Mail class="info-icon" />
						<div>
							<p class="info-label">Email</p>
							<p class="info-value">{viewingExpert.email}</p>
						</div>
					</div>
					<div class="info-row">
						<User class="info-icon" />
						<div>
							<p class="info-label">Teams</p>
							<p class="info-value">
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

<style lang="scss">
	// ---------- Tokens ----------
	$sans:
		ui-sans-serif,
		system-ui,
		-apple-system,
		"Segoe UI",
		Roboto,
		Helvetica,
		Arial,
		sans-serif;

	// Fixed Tailwind palette referenced directly
	$emerald-500: #10b981;
	$blue-500: #3b82f6;
	$rose-500: #f43f5e;

	// ---------- Page ----------
	.lecturers-page {
		max-width: 1600px; // max-w-[1600px]
		margin-inline: auto; // mx-auto
		width: 100%; // w-full
		font-family: $sans; // font-sans
		color: var(--md-on-surface); // text-(--md-on-surface)

		// ---- Header ----
		.page-header {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			align-items: flex-start;
			margin-bottom: 2rem; // mb-8
			padding-bottom: 1.5rem; // pb-6
			border-bottom: 1px solid var(--md-outline-variant);

			@media (min-width: 768px) {
				flex-direction: row; // md:flex-row
				align-items: center; // md:items-center
			}
		}

		.page-title {
			font-size: 1.5rem; // text-2xl
			font-weight: 800; // font-extrabold
			letter-spacing: -0.025em; // tracking-tight
			color: var(--md-on-surface);

			@media (min-width: 768px) {
				font-size: 1.875rem; // md:text-3xl
			}
		}

		.page-subtitle {
			margin-top: 0.25rem; // mt-1
			font-size: 0.875rem; // text-sm
			color: var(--md-on-surface-variant);
		}

		.header-actions {
			display: flex;
			align-items: center;
			gap: 0.75rem; // gap-3
			margin-top: 1rem; // mt-4

			@media (min-width: 768px) {
				margin-top: 0; // md:mt-0
			}
		}

		.btn-add {
			display: inline-flex;
			align-items: center;
			gap: 0.5rem; // gap-2
			padding: 0.625rem 1.25rem; // py-2.5 px-5
			border: none;
			border-radius: 0.75rem; // rounded-xl
			background: var(--md-primary);
			color: var(--md-on-primary);
			font-size: 0.875rem; // text-sm
			font-weight: 700; // font-bold
			cursor: pointer;
			box-shadow: none; // shadow-none
			transition: all 0.15s ease;

			&:hover {
				opacity: 0.9;
			} // hover:opacity-90
		}

		.btn-add-icon {
			width: 1rem; // w-4
			height: 1rem; // h-4
		}

		// ---- Panel ----
		.panel {
			padding: 2rem; // p-8
			border: 1px solid var(--md-outline-variant);
			border-radius: 1.5rem; // rounded-3xl
			background: var(--md-surface-container-lowest);
			box-shadow: none;
			transition: all 0.15s ease;
		}

		// ---- Toolbar ----
		.toolbar {
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			align-items: center;
			gap: 1rem; // gap-4
			margin-bottom: 1.5rem; // mb-6
			padding-bottom: 1.5rem; // pb-6
			border-bottom: 1px solid var(--md-outline-variant);

			@media (min-width: 640px) {
				flex-direction: row; // sm:flex-row
			}
		}

		.search-field {
			position: relative;
			width: 100%;

			@media (min-width: 640px) {
				width: 20rem; // sm:w-80
			}
		}

		.search-icon {
			position: absolute;
			inset-block: 0; // inset-y-0
			left: 0;
			display: flex;
			align-items: center;
			padding-left: 0.75rem; // pl-3
			pointer-events: none;
			color: var(--md-on-surface-variant);
		}

		.search-icon-svg {
			width: 1rem; // w-4
			height: 1rem; // h-4
		}

		.search-input {
			width: 100%;
			padding: 0.625rem 1rem 0.625rem 2.5rem; // py-2.5 pr-4 pl-10
			border: 1px solid var(--md-outline);
			border-radius: 0.75rem; // rounded-xl
			background: var(--md-surface-container-low);
			color: var(--md-on-surface);
			font-size: 0.875rem; // text-sm
			outline: none;
			box-shadow: none;
			transition: all 0.15s ease;

			&::placeholder {
				// placeholder-(--md-on-surface-variant)/60
				color: color-mix(in srgb, var(--md-on-surface-variant) 60%, transparent);
			}

			&:focus {
				box-shadow: 0 0 0 2px var(--md-primary); // focus:ring-2 focus:ring-
			}
		}

		.search-clear {
			position: absolute;
			right: 0.75rem; // right-3
			top: 50%;
			transform: translateY(-50%); // top-1/2 -translate-y-1/2
			padding: 0.25rem; // p-1
			border: none;
			border-radius: 0.375rem; // rounded-md
			background: transparent;
			font-size: 0.75rem; // text-xs
			font-weight: 700; // font-bold
			color: var(--md-on-surface-variant);
			cursor: pointer;

			&:hover {
				color: var(--md-on-surface);
			}
		}

		.result-count {
			font-size: 0.75rem; // text-xs
			font-weight: 600; // font-semibold
			color: var(--md-on-surface-variant);
		}

		.count-highlight {
			color: var(--md-primary);
			font-weight: 900; // font-black
		}

		// ---- Table ----
		.table-scroll {
			overflow-x: auto;
		}

		.data-table {
			width: 100%; // w-full
			min-width: 800px; // min-w-[800px]
			text-align: left;
			border-collapse: collapse;
		}

		.table-head-row {
			border-bottom: 1px solid var(--md-outline-variant);
			color: var(--md-on-surface-variant);
			font-size: 0.75rem; // text-xs
			font-weight: 700; // font-bold
			text-transform: uppercase;
			letter-spacing: 0.05em; // tracking-wider
		}

		.table-head-cell {
			padding: 0.875rem 1rem; // py-3.5 px-4
		}

		.table-body {
			font-size: 0.875rem;
		} // text-sm

		.state-cell {
			padding-block: 4rem; // py-16
			text-align: center;
		}

		.state-loading {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 0.75rem; // gap-3
		}

		.spinner {
			border-radius: 9999px;
			border: 2px solid transparent;
			animation: spin 1s linear infinite;

			&--primary {
				width: 2.5rem; // h-10
				height: 2.5rem; // w-10
				border-top-color: var(--md-primary);
				border-bottom-color: var(--md-primary);
			}
		}

		@keyframes spin {
			to {
				transform: rotate(360deg);
			}
		}

		.state-text {
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
		}

		.empty-state {
			display: flex;
			flex-direction: column;
			max-width: 20rem; // max-w-xs
			margin-inline: auto; // mx-auto
			gap: 0.5rem; // space-y-2
		}

		.empty-emoji {
			font-size: 1.875rem;
		} // text-3xl
		.empty-title {
			font-weight: 700;
			color: var(--md-on-surface);
		}
		.empty-desc {
			font-size: 0.75rem;
			color: var(--md-on-surface-variant);
		}
		.empty-highlight {
			color: var(--md-primary);
		}

		.data-row {
			// border-(--md-outline-variant)/50
			border-bottom: 1px solid color-mix(in srgb, var(--md-outline-variant) 50%, transparent);
			color: var(--md-on-surface);
			transition: background-color 0.15s ease; // transition-colors

			&:hover {
				// hover:bg-(--md-surface-container-highest)/30
				background: color-mix(in srgb, var(--md-surface-container-highest) 30%, transparent);
			}
		}

		.cell-name {
			padding: 1rem; // py-4 px-4
			font-weight: 700; // font-bold
		}
		.cell-role {
			padding: 1rem;
			font-size: 0.75rem; // text-xs
			font-weight: 600; // font-semibold
			color: var(--md-on-surface-variant);
		}
		.cell-teams {
			padding: 1rem;
			font-weight: 600;
			font-size: 0.75rem;
		}

		.team-badge {
			display: inline-block;
			padding: 0.25rem 0.625rem; // py-1 px-2.5
			border-radius: 9999px; // rounded-full
			font-size: 0.75rem;
			font-weight: 600;
			background: rgba($emerald-500, 0.1); // bg-emerald-500/10
			color: $emerald-500; // text-emerald-500
			border: 1px solid rgba($emerald-500, 0.2); // border-emerald-500/20
			box-shadow: none;
		}

		.team-none {
			font-size: 0.75rem;
			font-style: italic;
			opacity: 0.5; // opacity-50
		}

		.cell-actions {
			padding: 1rem;
		}

		.action-group {
			display: flex;
			gap: 0.5rem; // gap-2
		}

		.btn-action {
			display: inline-flex;
			align-items: center;
			gap: 0.375rem; // gap-1.5
			padding: 0.375rem 0.75rem; // py-1.5 px-3
			border: 1px solid var(--md-outline-variant);
			border-radius: 0.5rem; // rounded-lg
			background: var(--md-surface-container-low);
			color: var(--md-on-surface);
			font-size: 0.75rem;
			font-weight: 700;
			cursor: pointer;
			box-shadow: none;
			transition: all 0.15s ease;

			&:hover {
				background: var(--md-surface-container-high);
			}
		}

		.action-icon {
			width: 0.875rem; // w-3.5
			height: 0.875rem; // h-3.5
		}
	}

	// ---------- Modals (siblings of .lecturers-page) ----------
	.modal-overlay {
		position: fixed;
		inset: 0; // inset-0
		z-index: 2000; // z-[2000]
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 1rem; // p-4
		font-family: $sans;
		color: var(--md-on-surface);
		background: rgba(0, 0, 0, 0.4); // bg-black/40
		backdrop-filter: blur(2px); // backdrop-blur-[2px]
	}

	.modal-card {
		width: 100%;
		max-width: 32rem; // max-w-lg
		padding: 2rem; // p-8
		position: relative;
		border: 1px solid var(--md-outline-variant);
		border-radius: 1.5rem; // rounded-3xl
		background: var(--md-surface-container-low);
		box-shadow: none;
		transition: all 0.15s ease;
	}

	.modal-close {
		position: absolute;
		top: 1rem; // top-4
		right: 1rem; // right-4
		padding: 0.375rem; // p-1.5
		border: none;
		border-radius: 0.5rem; // rounded-lg
		background: transparent;
		color: var(--md-on-surface-variant);
		cursor: pointer;
		transition: color 0.15s ease; // transition-colors

		&:hover {
			background: var(--md-surface-container-high);
		}
	}

	.modal-close-icon {
		width: 1.25rem; // w-5
		height: 1.25rem; // h-5
	}

	.modal-title {
		display: flex;
		align-items: center;
		gap: 0.5rem; // gap-2
		margin-bottom: 1.5rem; // mb-6
		font-size: 1.25rem; // text-xl
		font-weight: 700; // font-bold
	}

	.modal-title-icon {
		width: 1.25rem;
		height: 1.25rem;
		color: var(--md-primary);
	}

	.modal-form {
		display: flex;
		flex-direction: column;
		gap: 1rem; // gap-4
	}

	.form-field {
		display: flex;
		flex-direction: column;
		gap: 0.375rem; // space-y-1.5
	}

	.form-label {
		font-size: 0.875rem; // text-sm
		font-weight: 600; // font-semibold
		color: var(--md-on-surface-variant);
	}

	.form-input {
		padding: 0.75rem; // p-3
		border: 1px solid var(--md-outline);
		border-radius: 0.75rem; // rounded-xl
		background: var(--md-surface-container-low);
		color: var(--md-on-surface);
		outline: none;
		transition: all 0.15s ease;

		&:focus {
			border-color: var(--md-primary);
		} // focus:border-
	}

	.form-message {
		padding: 0.75rem; // p-3
		margin-top: 0.5rem; // mt-2
		border: 1px solid;
		border-radius: 0.75rem; // rounded-xl
		font-size: 0.875rem;
		font-weight: 700; // font-bold

		&--success {
			background: rgba($emerald-500, 0.1);
			border-color: rgba($emerald-500, 0.2);
			color: $emerald-500;
		}
		&--error {
			background: rgba($rose-500, 0.1);
			border-color: rgba($rose-500, 0.2);
			color: $rose-500;
		}
	}

	.modal-footer {
		display: flex;
		gap: 0.75rem; // gap-3
		margin-top: 1rem; // mt-4
		padding-top: 0.5rem; // pt-2
		border-top: 1px solid var(--md-outline-variant);
	}

	.btn-cancel {
		width: 50%; // w-1/2
		padding-block: 0.75rem; // py-3
		border: 1px solid var(--md-outline);
		border-radius: 0.75rem; // rounded-xl
		background: var(--md-surface-container-highest);
		color: var(--md-on-surface);
		font-weight: 700;
		cursor: pointer;
		box-shadow: none;
		transition: all 0.15s ease;

		&:hover {
			opacity: 0.9;
		} // hover:opacity-90
	}

	.btn-save {
		width: 50%; // w-1/2
		padding-block: 0.75rem;
		border: none;
		border-radius: 0.75rem;
		background: var(--md-primary);
		color: var(--md-on-primary);
		font-weight: 700;
		cursor: pointer;
		box-shadow: none;
		transition: all 0.15s ease;

		&:hover {
			opacity: 0.95;
		} // hover:opacity-95
		&:disabled {
			opacity: 0.5; // disabled:opacity-50
			cursor: not-allowed;
		}
	}

	// ---------- Profile modal ----------
	.profile-card {
		width: 100%;
		max-width: 24rem; // max-w-sm
		position: relative;
		border: 1px solid var(--md-outline-variant);
		border-radius: 1.5rem; // rounded-3xl
		background: var(--md-surface-container-low);
		box-shadow: none;
		transition: all 0.15s ease;
		overflow: hidden; // overflow-hidden
	}

	.profile-banner {
		height: 6rem; // h-24
		position: relative;
		background: var(--md-primary-container);
	}

	.profile-close {
		position: absolute;
		top: 1rem; // top-4
		right: 1rem; // right-4
		padding: 0.375rem; // p-1.5
		border: none;
		border-radius: 9999px; // rounded-full
		background: rgba(0, 0, 0, 0.2); // bg-black/20
		color: #ffffff;
		cursor: pointer;
		z-index: 10; // z-10

		&:hover {
			background: rgba(0, 0, 0, 0.4);
		} // hover:bg-black/40
	}

	.profile-close-icon {
		width: 1rem; // w-4
		height: 1rem; // h-4
	}

	.profile-avatar-wrap {
		position: relative;
		display: flex;
		justify-content: center;
		padding-inline: 1.5rem; // px-6
		margin-top: -3rem; // -mt-12
		margin-bottom: 1rem; // mb-4
	}

	.profile-avatar {
		width: 6rem; // w-24
		height: 6rem; // h-24
		border-radius: 9999px; // rounded-full
		border: 4px solid var(--md-surface-container-low);
		background: var(--md-surface-container-high);
		color: var(--md-on-surface);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 1.875rem; // text-3xl
		font-weight: 900; // font-black
		box-shadow: none;
	}

	.profile-content {
		padding: 0 1.5rem 2rem; // px-6 pb-8
		text-align: center;
	}

	.profile-name {
		font-size: 1.25rem; // text-xl
		font-weight: 800; // font-extrabold
		color: var(--md-on-surface);
	}

	.profile-role {
		display: inline-block;
		margin-top: 0.375rem; // mt-1.5
		padding: 0.25rem 0.75rem; // py-1 px-3
		border: 1px solid;
		border-radius: 9999px; // rounded-full
		font-size: 0.75rem;
		font-weight: 700; // font-bold
		text-transform: uppercase;
		letter-spacing: 0.05em; // tracking-wider

		&--judge {
			background: rgba($blue-500, 0.1);
			border-color: rgba($blue-500, 0.2);
			color: $blue-500;
		}
		&--standard {
			background: rgba($emerald-500, 0.1);
			border-color: rgba($emerald-500, 0.2);
			color: $emerald-500;
		}
	}

	.profile-info {
		margin-top: 1.5rem; // mt-6
		display: flex;
		flex-direction: column;
		gap: 0.75rem; // space-y-3
		text-align: left;
	}

	.info-row {
		display: flex;
		align-items: center;
		gap: 0.75rem; // gap-3
		padding: 0.75rem; // p-3
		border: 1px solid var(--md-outline-variant);
		border-radius: 0.75rem; // rounded-xl
		background: var(--md-surface-container-high);
		color: var(--md-on-surface);
	}

	.info-icon {
		width: 1rem; // w-4
		height: 1rem; // h-4
		color: var(--md-on-surface-variant);
	}

	.info-label {
		font-size: 10px; // text-[10px]
		font-weight: 700; // font-bold
		text-transform: uppercase;
		color: var(--md-on-surface-variant);
	}

	.info-value {
		font-size: 0.875rem; // text-sm
		font-weight: 600; // font-semibold
	}
</style>
