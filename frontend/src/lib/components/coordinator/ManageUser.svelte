<script lang="ts">
	import { onMount } from "svelte"
	import { getAllAccounts, approveAccount } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import {
		Users,
		GraduationCap,
		Shield,
		BookOpen,
		Search,
		Filter,
		Plus,
		Edit3,
		Eye,
		Trash2,
		X,
		Mail,
		User,
		CheckCircle2,
		AlertTriangle
	} from "@lucide/svelte"

	// Live Users Data
	let users = $state<any[]>([])
	let isPageLoading = $state(true)

	async function loadUsers() {
		try {
			const { data, response } = await getAllAccounts({ throwOnError: false })
			if (response?.ok && data) {
				users = data.map((u: any) => ({
					id: u.id,
					name: u.name,
					email: u.email,
					role: u.role,
					status: u.status || "ACTIVE"
				}))
			}
		} catch (err) {
			console.error("Failed to load unapproved students", err)
		} finally {
			isPageLoading = false
		}
	}

	onMount(() => {
		loadUsers()
	})

	async function handleApproveStudent(user: any) {
		try {
			const { response } = await approveAccount({
				path: { accountId: user.id },
				throwOnError: false
			})
			if (response?.ok) {
				showNotification(`Account for ${user.name} approved successfully!`)
				await loadUsers()
			} else {
				showNotification(`Failed to approve account: ${response?.statusText || "Error"}`, "error")
			}
		} catch (err) {
			showNotification("Error connecting to the server.", "error")
		}
	}

	// Search & Filter State
	let searchQuery = $state("")
	let roleFilter = $state("ALL")
	let statusFilter = $state("ALL")

	// Modal Control State
	let showAddModal = $state(false)
	let showEditModal = $state(false)
	let showViewModal = $state(false)
	let showDeleteModal = $state(false)

	// Selected User State
	let selectedUser = $state<any>(null)

	// Form inputs state
	let formName = $state("")
	let formEmail = $state("")
	let formRole = $state("STUDENT")
	let formStatus = $state("ACTIVE")
	let formStudentId = $state("")

	// Message notifications
	let successMessage = $state("")
	let errorMessage = $state("")

	// Derived filtered users list
	let filteredUsers = $derived(
		users.filter((u) => {
			const matchesSearch =
				u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
				u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
				u.id.includes(searchQuery)
			const matchesRole = roleFilter === "ALL" || u.role === roleFilter
			const matchesStatus = statusFilter === "ALL" || u.status === statusFilter
			return matchesSearch && matchesRole && matchesStatus
		})
	)

	let currentPage = $state(1)
	const itemsPerPage = 8

	$effect(() => {
		// Reset page to 1 whenever filters change
		searchQuery
		roleFilter
		statusFilter
		currentPage = 1
	})

	let totalPages = $derived(Math.max(1, Math.ceil(filteredUsers.length / itemsPerPage)))

	let paginatedUsers = $derived(
		filteredUsers.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
	)

	// Derived Statistics
	let totalUsersCount = $derived(users.length)
	let studentsCount = $derived(users.filter((u) => u.role === "STUDENT").length)
	let lecturersCount = $derived(users.filter((u) => u.role === "LECTURER").length)
	let coordinatorsCount = $derived(users.filter((u) => u.role === "COORDINATOR").length)

	// Helper functions
	function showNotification(msg: string, type: "success" | "error" = "success") {
		if (type === "success") {
			successMessage = msg
			setTimeout(() => (successMessage = ""), 3000)
		} else {
			errorMessage = msg
			setTimeout(() => (errorMessage = ""), 3000)
		}
	}

	function openAddModal() {
		formName = ""
		formEmail = ""
		formRole = "STUDENT"
		formStatus = "ACTIVE"
		formStudentId = ""
		showAddModal = true
	}

	function handleAddUser(e: Event) {
		e.preventDefault()

		if (!formName.trim() || !formEmail.trim()) {
			showNotification("Please fill in all required fields.", "error")
			return
		}

		if (users.some((u) => u.email.toLowerCase() === formEmail.toLowerCase())) {
			showNotification("Email already registered.", "error")
			return
		}

		const newId = String(100000 + users.length + 1)
		const newUser = {
			id: newId,
			name: formName,
			email: formEmail,
			role: formRole,
			status: formRole === "STUDENT" ? formStatus : "ACTIVE"
		}

		users = [...users, newUser]
		showAddModal = false
		showNotification("User created successfully!")
	}

	function openEditModal(user: any) {
		selectedUser = user
		formName = user.name
		formEmail = user.email
		formRole = user.role
		formStatus = user.status
		showEditModal = true
	}

	function handleEditUser(e: Event) {
		e.preventDefault()

		if (!formName.trim()) {
			showNotification("Name cannot be empty.", "error")
			return
		}

		users = users.map((u) => {
			if (u.id === selectedUser.id) {
				return {
					...u,
					name: formName,
					role: formRole,
					status: formRole === "STUDENT" ? formStatus : "ACTIVE"
				}
			}
			return u
		})

		showEditModal = false
		showNotification("User updated successfully!")
	}

	function openViewModal(user: any) {
		selectedUser = user
		showViewModal = true
	}

	function openDeleteModal(user: any) {
		selectedUser = user
		showDeleteModal = true
	}

	function handleDeleteUser() {
		users = users.filter((u) => u.id !== selectedUser.id)
		showDeleteModal = false
		showNotification("User deleted successfully!")
	}
</script>

<div class="page-wrapper">
	<!-- Success & Error Banners -->
	{#if successMessage}
		<div class="banner banner--success">
			<CheckCircle2 class="banner__icon banner__icon--success" />
			<div class="banner__content">
				<p class="banner__title banner__title--success">Success</p>
				<p class="banner__message banner__message--success">{successMessage}</p>
			</div>
		</div>
	{/if}

	{#if errorMessage}
		<div class="banner banner--error">
			<AlertTriangle class="banner__icon banner__icon--error" />
			<div class="banner__content">
				<p class="banner__title banner__title--error">Error</p>
				<p class="banner__message banner__message--error">{errorMessage}</p>
			</div>
		</div>
	{/if}

	<!-- Header Section -->
	<header class="page-header">
		<div class="page-header__text">
			<h1 class="page-header__title">User Management</h1>
			<p class="page-header__subtitle">
				Manage, edit, and audit system users for the SEAL event platform.
			</p>
		</div>
		<div class="page-header__actions">
			<button onclick={openAddModal} class="btn btn--primary btn--with-icon">
				<Plus class="btn__icon" />
				Add User
			</button>
		</div>
	</header>

	<!-- Statistics Cards -->
	<div class="stats-grid">
		<!-- Card 1: Total Users -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--primary">
				<Users class="stat-card__icon-svg" />
			</div>
			<div class="stat-card__body">
				<p class="stat-card__label">Total Users</p>
				<h3 class="stat-card__value">{totalUsersCount}</h3>
			</div>
		</div>

		<!-- Card 2: Students -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--secondary">
				<GraduationCap class="stat-card__icon-svg" />
			</div>
			<div class="stat-card__body">
				<p class="stat-card__label">Students</p>
				<h3 class="stat-card__value">{studentsCount}</h3>
			</div>
		</div>

		<!-- Card 3: Lecturers -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--tertiary">
				<BookOpen class="stat-card__icon-svg" />
			</div>
			<div class="stat-card__body">
				<p class="stat-card__label">Lecturers</p>
				<h3 class="stat-card__value">{lecturersCount}</h3>
			</div>
		</div>

		<!-- Card 4: Coordinators -->
		<div class="stat-card">
			<div class="stat-card__icon stat-card__icon--neutral">
				<Shield class="stat-card__icon-svg" />
			</div>
			<div class="stat-card__body">
				<p class="stat-card__label">Coordinators</p>
				<h3 class="stat-card__value">{coordinatorsCount}</h3>
			</div>
		</div>
	</div>

	<!-- Filters and Table Container -->
	<div class="content-panel">
		<!-- Filters Toolbar -->
		<div class="toolbar">
			<!-- Search bar -->
			<div class="search-wrapper">
				<span class="search-wrapper__icon-wrap">
					<Search class="search-wrapper__icon" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search by ID, name, or email..."
					class="search-input"
				/>
				{#if searchQuery}
					<button type="button" onclick={() => (searchQuery = "")} class="search-clear-btn">
						Clear
					</button>
				{/if}
			</div>

			<!-- Role & Status selectors -->
			<div class="filters-group">
				<div class="filters-label">
					<Filter class="filters-label__icon" />
					Filters:
				</div>
				<select bind:value={roleFilter} class="filter-select">
					<option value="ALL">All Roles</option>
					<option value="STUDENT">Student</option>
					<option value="LECTURER">Lecturer</option>
					<option value="COORDINATOR">Coordinator</option>
				</select>

				<select bind:value={statusFilter} class="filter-select">
					<option value="ALL">All Statuses</option>
					<option value="ACTIVE">Active</option>
					<option value="INACTIVE">Inactive</option>
					<option value="PENDING">Pending</option>
					<option value="BANNED">Banned</option>
				</select>
			</div>
		</div>

		<!-- User Table -->
		<div class="table-wrapper">
			<table class="user-table">
				<thead class="user-table__head">
					<tr class="user-table__head-row">
						<th class="user-table__th">User ID</th>
						<th class="user-table__th">Full Name</th>
						<th class="user-table__th">Email</th>
						<th class="user-table__th">System Role</th>
						<th class="user-table__th">Status</th>
						<th class="user-table__th user-table__th--center">Actions</th>
					</tr>
				</thead>
				<tbody class="user-table__body">
					{#if paginatedUsers.length > 0}
						{#each paginatedUsers as user}
							<tr class="user-table__row">
								<td class="user-table__cell user-table__cell--id">#{user.id}</td>
								<td class="user-table__cell user-table__cell--name">{user.name}</td>
								<td class="user-table__cell user-table__cell--email">{user.email}</td>
								<td class="user-table__cell">
									{#if user.role === "COORDINATOR"}
										<span class="badge badge--coordinator">Coordinator</span>
									{:else if user.role === "LECTURER"}
										<span class="badge badge--lecturer">Lecturer</span>
									{:else}
										<span class="badge badge--student">Student</span>
									{/if}
								</td>
								<td class="user-table__cell">
									{#if user.status === "ACTIVE"}
										<span class="status-badge status-badge--active">
											<span class="status-badge__dot"></span>
											Active
										</span>
									{:else if user.status === "INACTIVE"}
										<span class="status-badge status-badge--inactive">
											<span class="status-badge__dot"></span>
											Inactive
										</span>
									{:else if user.status === "PENDING"}
										<span class="status-badge status-badge--pending">
											<span class="status-badge__dot status-badge__dot--pulse"></span>
											Pending
										</span>
									{:else if user.status === "BANNED"}
										<span class="status-badge status-badge--banned">
											<span class="status-badge__dot"></span>
											Banned
										</span>
									{/if}
								</td>
								<td class="user-table__cell user-table__cell--center">
									<div class="action-buttons">
										{#if user.status === "PENDING"}
											<button
												onclick={() => handleApproveStudent(user)}
												title="Approve Account"
												class="action-btn action-btn--approve"
											>
												<CheckCircle2 class="action-btn__icon action-btn__icon--approve" />
											</button>
										{/if}
										<button
											onclick={() => openEditModal(user)}
											title="Edit User"
											class="action-btn action-btn--edit"
										>
											<Edit3 class="action-btn__icon action-btn__icon--edit" />
										</button>
										<button
											onclick={() => openViewModal(user)}
											title="View Details"
											class="action-btn action-btn--view"
										>
											<Eye class="action-btn__icon action-btn__icon--view" />
										</button>
										<button
											onclick={() => openDeleteModal(user)}
											title="Delete User"
											class="action-btn action-btn--delete"
										>
											<Trash2 class="action-btn__icon action-btn__icon--delete" />
										</button>
									</div>
								</td>
							</tr>
						{/each}
					{:else}
						<tr class="user-table__row user-table__row--empty">
							<td colspan="6" class="empty-state">
								<div class="empty-state__content">
									<Search class="empty-state__icon" />
									<p class="empty-state__title">No users found</p>
									<p class="empty-state__desc">Try adjusting your filters or search terms.</p>
								</div>
							</td>
						</tr>
					{/if}
				</tbody>
			</table>
		</div>

		<!-- Pagination Controls -->
		{#if totalPages > 1}
			<div class="pagination">
				<span class="pagination__info">
					Showing {Math.min(filteredUsers.length, (currentPage - 1) * itemsPerPage + 1)}-{Math.min(
						filteredUsers.length,
						currentPage * itemsPerPage
					)} of {filteredUsers.length} users
				</span>
				<div class="pagination__controls">
					<button
						onclick={() => (currentPage = Math.max(1, currentPage - 1))}
						disabled={currentPage === 1}
						class="pagination__btn"
					>
						<svg class="pagination__btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M15 19l-7-7 7-7"
							/>
						</svg>
					</button>
					<span class="pagination__page-info"> Page {currentPage} of {totalPages} </span>
					<button
						onclick={() => (currentPage = Math.min(totalPages, currentPage + 1))}
						disabled={currentPage === totalPages}
						class="pagination__btn"
					>
						<svg class="pagination__btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M9 5l7 7-7 7"
							/>
						</svg>
					</button>
				</div>
			</div>
		{/if}
	</div>
</div>

<!-- ======================================= MODALS ======================================= -->
{#if showAddModal}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={() => (showAddModal = false)} class="modal__close">
				<X class="modal__close-icon" />
			</button>
			<h3 class="modal__title">
				<User class="modal__title-icon modal__title-icon--primary" />
				Add User Account
			</h3>
			<form onsubmit={handleAddUser} class="form">
				<div class="form-group">
					<label for="add-full-name" class="form-label">Full Name *</label>
					<input
						id="add-full-name"
						type="text"
						bind:value={formName}
						required
						placeholder="Dr. Nguyen Van A"
						class="form-input"
					/>
				</div>
				<div class="form-group">
					<label for="add-email" class="form-label">Email Address *</label>
					<input
						id="add-email"
						type="email"
						bind:value={formEmail}
						required
						placeholder="anv@fpt.edu.vn"
						class="form-input"
					/>
				</div>
				<div class="form-row">
					<div class="form-group">
						<label for="add-system-role" class="form-label">System Role</label>
						<select
							id="add-system-role"
							bind:value={formRole}
							class="form-input form-input--select"
						>
							<option value="STUDENT">Student</option>
							<option value="LECTURER">Lecturer</option>
							<option value="COORDINATOR">Coordinator</option>
						</select>
					</div>
					{#if formRole === "STUDENT"}
						<div class="form-group">
							<label for="add-student-id" class="form-label">Student ID</label>
							<input
								id="add-student-id"
								type="text"
								bind:value={formStudentId}
								placeholder="SE160000"
								class="form-input"
							/>
						</div>
					{/if}
				</div>
				{#if formRole === "STUDENT"}
					<div class="form-group">
						<label for="add-status" class="form-label">Status</label>
						<select id="add-status" bind:value={formStatus} class="form-input form-input--select">
							<option value="ACTIVE">Active</option>
							<option value="INACTIVE">Inactive</option>
							<option value="PENDING">Pending</option>
							<option value="BANNED">Banned</option>
						</select>
					</div>
				{/if}
				<div class="form-actions">
					<button
						type="button"
						onclick={() => (showAddModal = false)}
						class="btn btn--outline btn--half">Cancel</button
					>
					<button type="submit" class="btn btn--primary btn--half">Create User</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if showEditModal && selectedUser}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={() => (showEditModal = false)} class="modal__close">
				<X class="modal__close-icon" />
			</button>
			<h3 class="modal__title">
				<Edit3 class="modal__title-icon modal__title-icon--primary" />
				Edit User Account
			</h3>
			<form onsubmit={handleEditUser} class="form">
				<div class="form-group">
					<label for="edit-user-id" class="form-label">User ID</label>
					<input
						id="edit-user-id"
						type="text"
						value="#{selectedUser.id}"
						disabled
						class="form-input form-input--disabled"
					/>
				</div>
				<div class="form-group">
					<label for="edit-full-name" class="form-label">Full Name</label>
					<input
						id="edit-full-name"
						type="text"
						bind:value={formName}
						required
						class="form-input"
					/>
				</div>
				<div class="form-group">
					<label for="edit-email" class="form-label">Email (Cannot be modified)</label>
					<input
						id="edit-email"
						type="email"
						value={formEmail}
						disabled
						class="form-input form-input--disabled"
					/>
				</div>
				<div class="form-group">
					<label for="edit-system-role" class="form-label">System Role</label>
					<select id="edit-system-role" bind:value={formRole} class="form-input form-input--select">
						<option value="STUDENT">Student</option>
						<option value="LECTURER">Lecturer</option>
						<option value="COORDINATOR">Coordinator</option>
					</select>
				</div>
				{#if formRole === "STUDENT"}
					<div class="form-group">
						<label for="edit-status" class="form-label">Student Status</label>
						<select id="edit-status" bind:value={formStatus} class="form-input form-input--select">
							<option value="ACTIVE">Active</option>
							<option value="INACTIVE">Inactive</option>
							<option value="PENDING">Pending</option>
							<option value="BANNED">Banned</option>
						</select>
					</div>
				{/if}
				<div class="form-actions">
					<button
						type="button"
						onclick={() => (showEditModal = false)}
						class="btn btn--outline btn--half">Cancel</button
					>
					<button type="submit" class="btn btn--primary btn--half">Save Changes</button>
				</div>
			</form>
		</div>
	</div>
{/if}

{#if showViewModal && selectedUser}
	<div class="modal-overlay">
		<div class="modal">
			<button onclick={() => (showViewModal = false)} class="modal__close">
				<X class="modal__close-icon" />
			</button>
			<h3 class="modal__title">
				<Eye class="modal__title-icon modal__title-icon--secondary" />
				User Details Profile
			</h3>
			<div class="profile-details">
				<div class="profile-details__header">
					<div class="profile-details__avatar">
						{selectedUser.name.charAt(0).toUpperCase()}
					</div>
					<div class="profile-details__meta">
						<h4 class="profile-details__name">{selectedUser.name}</h4>
						<p class="profile-details__uuid">UUID: {selectedUser.id}</p>
					</div>
				</div>
				<div class="profile-details__grid">
					<div class="profile-details__card">
						<p class="profile-details__card-label">System Role</p>
						<p class="profile-details__card-value">{selectedUser.role}</p>
					</div>
					<div class="profile-details__card">
						<p class="profile-details__card-label">Status</p>
						<p class="profile-details__card-value">{selectedUser.status}</p>
					</div>
				</div>
				<div class="profile-details__card">
					<p class="profile-details__card-label profile-details__card-label--with-icon">
						<Mail class="profile-details__card-icon" />
						Email Address
					</p>
					<p class="profile-details__card-value profile-details__card-value--primary">
						{selectedUser.email}
					</p>
				</div>
			</div>
			<button
				onclick={() => (showViewModal = false)}
				class="btn btn--outline btn--full btn--view-close">Close Profile</button
			>
		</div>
	</div>
{/if}

{#if showDeleteModal && selectedUser}
	<div class="modal-overlay">
		<div class="modal modal--sm">
			<button onclick={() => (showDeleteModal = false)} class="modal__close">
				<X class="modal__close-icon" />
			</button>
			<h3 class="modal__title modal__title--danger">
				<Trash2 class="modal__title-icon modal__title-icon--danger" />
				Delete User Account?
			</h3>
			<p class="modal__description">
				Are you sure you want to permanently delete the user <strong class="modal__highlight"
					>{selectedUser.name}</strong
				>
				({selectedUser.email})? This action is irreversible and will remove all their records from
				the platform.
			</p>
			<div class="form-actions">
				<button onclick={() => (showDeleteModal = false)} class="btn btn--outline btn--half"
					>Cancel</button
				>
				<button onclick={handleDeleteUser} class="btn btn--danger btn--half">Delete User</button>
			</div>
		</div>
	</div>
{/if}

<style lang="scss">
	// ============================================================================
	// User Management - SCSS Conversion from TailwindCSS
	// ============================================================================
	// Breakpoints
	$bp-sm: 640px;
	$bp-md: 768px;
	$bp-lg: 1024px;

	// Mixins
	@mixin focus-ring {
		&:focus {
			outline: none;
			border-color: transparent;
			box-shadow: 0 0 0 2px var(--md-primary);
		}
	}

	@mixin transition {
		transition: all 0.2s ease;
	}

	// ----------------------------------------------------------------------------
	// Page Wrapper
	// p-6 md:p-10 max-w-[1600px] mx-auto w-full
	// ----------------------------------------------------------------------------
	.page-wrapper {
		max-width: 1600px;
	}

	// ----------------------------------------------------------------------------
	// Banners - Success & Error
	// fixed top-6 right-6 z-[5000] flex items-center gap-3 p-4 rounded-xl max-w-md
	// ----------------------------------------------------------------------------
	.banner {
		position: fixed;
		top: 1.5rem;
		right: 1.5rem;
		z-index: 5000;
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 1rem;
		border-radius: 0.75rem;
		max-width: 28rem;
		border-left-width: 4px;
		border-left-style: solid;

		// Modifiers
		&--success {
			background-color: rgba(16, 185, 129, 0.1); // bg-emerald-500/10
			border-left-color: #10b981; // border-emerald-500
		}

		&--error {
			background-color: rgba(244, 63, 94, 0.1); // bg-rose-500/10
			border-left-color: #f43f5e; // border-rose-500
		}

		&__icon {
			width: 1.25rem;
			height: 1.25rem;
			flex-shrink: 0;

			&--success {
				color: #10b981;
			}

			&--error {
				color: #f43f5e;
			}
		}

		&__title {
			font-size: 0.875rem; // text-sm
			font-weight: 600; // font-semibold

			&--success {
				color: #065f46; // text-emerald-800
				:global(.dark) & {
					color: #6ee7b7; // dark:text-emerald-300
				}
			}

			&--error {
				color: #9f1239; // text-rose-800
				:global(.dark) & {
					color: #fda4af; // dark:text-rose-300
				}
			}
		}

		&__message {
			font-size: 0.75rem; // text-xs
			margin-top: 0.125rem; // mt-0.5

			&--success {
				color: #047857; // text-emerald-700
				:global(.dark) & {
					color: #34d399; // dark:text-emerald-400
				}
			}

			&--error {
				color: #be123c; // text-rose-700
				:global(.dark) & {
					color: #fb7185; // dark:text-rose-400
				}
			}
		}
	}

	// Dark mode fallback via prefers-color-scheme
	@media (prefers-color-scheme: dark) {
		.banner__title--success {
			color: #6ee7b7;
		}
		.banner__title--error {
			color: #fda4af;
		}
		.banner__message--success {
			color: #34d399;
		}
		.banner__message--error {
			color: #fb7185;
		}
	}

	// ----------------------------------------------------------------------------
	// Page Header
	// flex flex-col md:flex-row justify-between items-start md:items-center
	// mb-8 pb-6 border-b border-(--md-outline-variant)
	// ----------------------------------------------------------------------------
	.page-header {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem; // mb-8
		padding-bottom: 1.5rem; // pb-6
		border-bottom: 1px solid var(--md-outline-variant);

		@media (min-width: $bp-md) {
			flex-direction: row;
			align-items: center;
		}

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 800; // font-extrabold
			letter-spacing: -0.025em; // tracking-tight
			color: var(--md-on-surface);

			@media (min-width: $bp-md) {
				font-size: 1.875rem; // md:text-3xl
			}
		}

		&__subtitle {
			margin-top: 0.25rem; // mt-1
			font-size: 0.875rem; // text-sm
			color: var(--md-on-surface-variant);
		}

		&__actions {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			margin-top: 1rem; // mt-4

			@media (min-width: $bp-md) {
				margin-top: 0;
			}
		}
	}

	// ----------------------------------------------------------------------------
	// Shared Button Styles
	// flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold
	// transition-all cursor-pointer border-0
	// ----------------------------------------------------------------------------
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		padding: 0.625rem 1.25rem; // py-2.5 px-5
		border-radius: 0.75rem; // rounded-xl
		font-size: 0.875rem;
		font-weight: 700; // font-bold
		cursor: pointer;
		border: none;
		transition: all 0.2s ease;
		text-decoration: none;
		line-height: 1;

		&:active {
			transform: scale(0.98); // active:scale-98
		}

		&__icon {
			width: 1rem;
			height: 1rem;
		}

		&--with-icon {
			gap: 0.5rem;
		}

		&--primary {
			background-color: var(--md-primary);
			color: var(--md-on-primary);

			&:hover {
				opacity: 0.9;
			}
		}

		&--outline {
			border: 1px solid var(--md-outline);
			background: transparent;
			color: var(--md-on-surface);
			padding-top: 0.75rem; // py-3 for modal buttons
			padding-bottom: 0.75rem;
			font-weight: 600;

			&:hover {
				background-color: var(--md-surface-container-low);
			}
		}

		&--danger {
			background-color: #e11d48; // bg-rose-600
			color: #fff;
			padding-top: 0.75rem;
			padding-bottom: 0.75rem;
			font-weight: 600;

			&:hover {
				background-color: #be123c; // hover:bg-rose-700
			}
		}

		&--half {
			width: 50%;
		}

		&--full {
		}

		&--view-close {
			margin-top: 2rem;
		}
	}

	// ----------------------------------------------------------------------------
	// Stats Grid
	// grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8
	// ----------------------------------------------------------------------------
	.stats-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem;
		margin-bottom: 2rem;

		@media (min-width: $bp-sm) {
			grid-template-columns: repeat(2, 1fr);
		}

		@media (min-width: $bp-lg) {
			grid-template-columns: repeat(4, 1fr);
		}
	}

	.stat-card {
		padding: 1.5rem; // p-6
		border-radius: 1rem; // rounded-2xl
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-lowest);
		display: flex;
		align-items: center;
		gap: 1.25rem; // gap-5
		color: var(--md-on-surface);
		@include transition;

		&__icon {
			width: 3rem; // w-12
			height: 3rem; // h-12
			border-radius: 0.75rem; // rounded-xl
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;

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

			&--neutral {
				background-color: var(--md-surface-container-high);
				color: var(--md-on-surface);
			}
		}

		&__icon-svg {
			width: 1.5rem;
			height: 1.5rem;
		}

		&__label {
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em; // tracking-wider
			margin-bottom: 0.25rem;
			color: var(--md-on-surface-variant);
		}

		&__value {
			font-size: 1.5rem; // text-2xl
			font-weight: 700; // font-bold
			line-height: 1;
		}
	}

	// ----------------------------------------------------------------------------
	// Content Panel (Filters + Table)
	// p-8 rounded-3xl border bg-(--md-surface-container-lowest)
	// ----------------------------------------------------------------------------
	.content-panel {
		padding: 2rem; // p-8
		border-radius: 1.5rem; // rounded-3xl
		border: 1px solid var(--md-outline-variant);
		background-color: var(--md-surface-container-lowest);
		color: var(--md-on-surface);
	}

	// ----------------------------------------------------------------------------
	// Toolbar
	// flex flex-col md:flex-row gap-4 justify-between items-center mb-6
	// ----------------------------------------------------------------------------
	.toolbar {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;

		@media (min-width: $bp-md) {
			flex-direction: row;
		}
	}

	// ----------------------------------------------------------------------------
	// Search
	// relative w-full md:max-w-md
	// ----------------------------------------------------------------------------
	.search-wrapper {
		position: relative;
		width: 100%;

		@media (min-width: $bp-md) {
			max-width: 28rem; // md:max-w-md
		}

		&__icon-wrap {
			position: absolute;
			top: 0;
			bottom: 0;
			left: 0;
			padding-left: 0.75rem;
			display: flex;
			align-items: center;
			pointer-events: none;
			color: var(--md-on-surface-variant);
		}

		&__icon {
			width: 1rem;
			height: 1rem;
		}
	}

	.search-input {
		width: 100%;
		padding: 0.625rem 1rem 0.625rem 2.5rem; // pl-10 pr-4 py-2.5
		border-radius: 0.75rem;
		border: 1px solid var(--md-outline);
		outline: none;
		transition: all 0.2s ease;
		font-size: 0.875rem;
		background-color: var(--md-surface-container);
		color: var(--md-on-surface);

		@include focus-ring;
	}

	.search-clear-btn {
		position: absolute;
		right: 0.75rem;
		top: 50%;
		transform: translateY(-50%);
		padding: 0.25rem;
		border-radius: 0.375rem;
		font-size: 0.75rem;
		font-weight: 700;
		color: var(--md-on-surface-variant);
		cursor: pointer;
		border: none;
		background: transparent;
		@include transition;

		&:hover {
			color: var(--md-on-surface);
		}
	}

	// ----------------------------------------------------------------------------
	// Filters Group
	// flex flex-wrap items-center gap-3 w-full md:w-auto
	// ----------------------------------------------------------------------------
	.filters-group {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 0.75rem;
		width: 100%;

		@media (min-width: $bp-md) {
			width: auto;
		}
	}

	.filters-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-size: 0.875rem;
		color: var(--md-on-surface-variant);

		&__icon {
			width: 1rem;
			height: 1rem;
		}
	}

	.filter-select {
		border-radius: 0.75rem;
		border: 1px solid var(--md-outline);
		padding: 0.5rem 0.75rem; // px-3 py-2
		font-size: 0.875rem;
		outline: none;
		transition: all 0.2s ease;
		background-color: var(--md-surface-container);
		color: var(--md-on-surface);
		cursor: pointer;

		@include focus-ring;
	}

	// ----------------------------------------------------------------------------
	// Table
	// overflow-x-auto, w-full text-left border-collapse
	// ----------------------------------------------------------------------------
	.table-wrapper {
		overflow-x: auto;
	}

	.user-table {
		width: 100%;
		text-align: left;
		border-collapse: collapse;

		&__head-row {
			border-bottom: 1px solid var(--md-outline-variant);
			color: var(--md-on-surface-variant);
			font-size: 0.75rem;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.05em;
		}

		&__th {
			padding: 1rem;
			white-space: nowrap;

			&--center {
				text-align: center;
			}
		}

		&__body {
			font-size: 0.875rem;
		}

		&__row {
			border-bottom: 1px solid rgba(var(--md-outline-variant), 0.5);
			color: var(--md-on-surface);
			@include transition;

			&:hover {
				background-color: var(--md-surface-container-high);
			}

			// Handle opacity border: fallback
			border-bottom-color: color-mix(in srgb, var(--md-outline-variant) 50%, transparent);

			&--empty {
				&:hover {
					background: transparent;
				}
			}
		}

		&__cell {
			padding: 1rem;

			&--id {
				font-family: monospace;
				font-size: 0.75rem;
				font-weight: 600;
				color: var(--md-on-surface-variant);
			}

			&--name {
				font-weight: 700;
			}

			&--email {
				font-size: 0.875rem;
				color: var(--md-on-surface-variant);
			}

			&--center {
				text-align: center;
			}
		}
	}

	// Badges - Roles
	// inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium
	.badge {
		display: inline-flex;
		align-items: center;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		font-size: 0.75rem;
		font-weight: 500;
		border-width: 1px;
		border-style: solid;

		&--coordinator {
			background-color: var(--md-primary-container);
			color: var(--md-on-primary-container);
			border-color: color-mix(in srgb, var(--md-primary) 10%, transparent);
		}

		&--lecturer {
			background-color: var(--md-secondary-container);
			color: var(--md-on-secondary-container);
			border-color: color-mix(in srgb, var(--md-secondary) 10%, transparent);
		}

		&--student {
			background-color: var(--md-tertiary-container);
			color: var(--md-on-tertiary-container);
			border-color: color-mix(in srgb, var(--md-tertiary) 10%, transparent);
		}
	}

	// Status Badges
	// inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold
	.status-badge {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.25rem 0.625rem;
		border-radius: 9999px;
		font-size: 0.75rem;
		font-weight: 600;
		border-width: 1px;
		border-style: solid;

		&__dot {
			width: 0.375rem;
			height: 0.375rem;
			border-radius: 9999px;

			&--pulse {
				animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
			}
		}

		&--active {
			background-color: rgba(16, 185, 129, 0.1);
			color: #10b981;
			border-color: rgba(16, 185, 129, 0.2);

			.status-badge__dot {
				background-color: #10b981;
			}
		}

		&--inactive {
			background-color: rgba(107, 114, 128, 0.1);
			color: #9ca3af; // text-gray-400
			border-color: rgba(107, 114, 128, 0.2);

			.status-badge__dot {
				background-color: #9ca3af;
			}
		}

		&--pending {
			background-color: rgba(245, 158, 11, 0.1);
			color: #f59e0b;
			border-color: rgba(245, 158, 11, 0.2);

			.status-badge__dot {
				background-color: #f59e0b;
			}
		}

		&--banned {
			background-color: rgba(244, 63, 94, 0.1);
			color: #f43f5e;
			border-color: rgba(244, 63, 94, 0.2);

			.status-badge__dot {
				background-color: #f43f5e;
			}
		}
	}

	@keyframes pulse {
		0%,
		100% {
			opacity: 1;
		}
		50% {
			opacity: 0.5;
		}
	}

	// Action Buttons
	// p-2 rounded-lg hover:bg-... text-zinc-400 transition-colors
	.action-buttons {
		display: flex;
		justify-content: center;
		gap: 0.5rem;
	}

	.action-btn {
		padding: 0.5rem;
		border-radius: 0.5rem;
		color: #a1a1aa; // text-zinc-400
		background: transparent;
		border: none;
		cursor: pointer;
		transition: all 0.2s ease;
		display: inline-flex;
		align-items: center;
		justify-content: center;

		&__icon {
			width: 1rem;
			height: 1rem;

			&--approve {
				width: 1.125rem;
				height: 1.125rem;
				color: #10b981;
			}

			&--edit {
				width: 1.125rem;
				height: 1.125rem;
				color: var(--md-primary);
			}

			&--view {
				color: #3b82f6; // text-blue-500
			}

			&--delete {
				color: #f43f5e; // text-rose-500
			}
		}

		&--approve:hover {
			background-color: rgba(16, 185, 129, 0.1);
			color: #10b981;
		}

		&--edit {
			color: var(--md-on-surface-variant);
			&:hover {
				background-color: var(--md-primary-container);
				color: var(--md-on-primary-container);
			}
		}

		&--view {
			color: var(--md-on-surface-variant);
			&:hover {
				background-color: var(--md-secondary-container);
				color: var(--md-on-secondary-container);
			}
		}

		&--delete:hover {
			background-color: rgba(244, 63, 94, 0.1);
			color: #f43f5e;
		}
	}

	// Empty State
	// py-12 text-center
	.empty-state {
		padding: 3rem 0;
		text-align: center;
		color: var(--md-on-surface-variant);

		&__content {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 0.5rem;
		}

		&__icon {
			width: 2rem;
			height: 2rem;
			opacity: 0.4;
		}

		&__title {
			font-weight: 600;
			font-size: 1.125rem;
			margin-top: 0.5rem;
			color: var(--md-on-surface);
		}

		&__desc {
			font-size: 0.875rem;
		}
	}

	// ----------------------------------------------------------------------------
	// Pagination
	// flex flex-col sm:flex-row justify-between items-center gap-4 mt-6 pt-4
	// border-t border-(--md-outline-variant)
	// ----------------------------------------------------------------------------
	.pagination {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		align-items: center;
		gap: 1rem;
		margin-top: 1.5rem;
		padding-top: 1rem;
		border-top: 1px solid var(--md-outline-variant);

		@media (min-width: $bp-sm) {
			flex-direction: row;
		}

		&__info {
			font-size: 0.75rem;
			color: var(--md-on-surface-variant);
		}

		&__controls {
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}

		&__btn {
			padding: 0.5rem;
			border-radius: 0.5rem;
			border: 1px solid var(--md-outline);
			color: var(--md-on-surface);
			background: transparent;
			cursor: pointer;
			transition: all 0.2s ease;
			display: flex;
			align-items: center;
			justify-content: center;

			&:hover:not(:disabled) {
				background-color: var(--md-surface-container-high);
			}

			&:disabled {
				opacity: 0.4;
				cursor: not-allowed;
			}
		}

		&__btn-icon {
			width: 1rem;
			height: 1rem;
		}

		&__page-info {
			font-size: 0.75rem;
			font-weight: 700;
			color: var(--md-on-surface);
		}
	}

	// ----------------------------------------------------------------------------
	// Modal Overlay
	// fixed inset-0 z-[2000] flex items-center justify-center bg-black/45
	// backdrop-blur-xs p-4
	// ----------------------------------------------------------------------------
	.modal-overlay {
		position: fixed;
		inset: 0;
		z-index: 2000;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: rgba(0, 0, 0, 0.45);
		backdrop-filter: blur(4px);
		padding: 1rem;
	}

	.modal {
		width: 100%;
		max-width: 32rem; // max-w-lg
		border-radius: 1rem; // rounded-2xl
		border: 1px solid var(--md-outline-variant);
		padding: 2rem;
		position: relative;
		background-color: var(--md-surface-container-high);
		color: var(--md-on-surface);
		@include transition;

		&--sm {
			max-width: 28rem; // max-w-md for delete modal
		}

		&__close {
			position: absolute;
			top: 1rem;
			right: 1rem;
			padding: 0.25rem;
			border-radius: 0.5rem;
			color: var(--md-on-surface-variant);
			background: transparent;
			border: none;
			cursor: pointer;
			@include transition;

			&:hover {
				background-color: var(--md-surface-container-low);
			}
		}

		&__close-icon {
			width: 1.25rem;
			height: 1.25rem;
		}

		&__title {
			font-size: 1.25rem; // text-xl
			font-weight: 700;
			margin-bottom: 1.5rem;
			display: flex;
			align-items: center;
			gap: 0.5rem;

			&--danger {
				color: #f43f5e; // text-rose-500
			}
		}

		&__title-icon {
			width: 1.25rem;
			height: 1.25rem;

			&--primary {
				color: var(--md-primary);
			}

			&--secondary {
				color: var(--md-secondary);
			}

			&--danger {
				color: #f43f5e;
			}
		}

		&__description {
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
			line-height: 1.5;
		}

		&__highlight {
			color: var(--md-primary);
		}
	}

	// ----------------------------------------------------------------------------
	// Forms
	// flex flex-col gap-4, space-y-1, text-sm font-semibold
	// ----------------------------------------------------------------------------
	.form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.25rem; // space-y-1
	}

	.form-label {
		font-size: 0.875rem;
		font-weight: 600;
		color: var(--md-on-surface-variant);
	}

	.form-input {
		border-radius: 0.75rem;
		border: 1px solid var(--md-outline);
		padding: 0.75rem; // p-3
		outline: none;
		transition: all 0.2s ease;
		background-color: var(--md-surface-container);
		color: var(--md-on-surface);
		font-size: 0.875rem;

		@include focus-ring;

		&--select {
			cursor: pointer;
		}

		&--disabled {
			background-color: var(--md-surface-container-low);
			color: var(--md-on-surface-variant);
			cursor: not-allowed;
			border-color: var(--md-outline-variant);
		}
	}

	.form-row {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1rem;

		@media (min-width: $bp-sm) {
			grid-template-columns: repeat(2, 1fr);
		}
	}

	.form-actions {
		display: flex;
		gap: 0.75rem;
		margin-top: 1.5rem; // mt-6, mt-8 variations
	}

	// ----------------------------------------------------------------------------
	// Profile Details (View Modal)
	// ----------------------------------------------------------------------------
	.profile-details {
		display: flex;
		flex-direction: column;
		gap: 1rem;

		&__header {
			display: flex;
			align-items: center;
			gap: 1rem;
			padding: 1rem;
			border-radius: 0.75rem;
			background-color: var(--md-surface-container-low);
			border: 1px solid var(--md-outline-variant);
		}

		&__avatar {
			width: 3.5rem;
			height: 3.5rem;
			border-radius: 9999px;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 1.5rem;
			font-weight: 700;
			background-color: var(--md-primary-container);
			color: var(--md-on-primary-container);
			flex-shrink: 0;
		}

		&__name {
			font-weight: 800;
			font-size: 1.125rem;
		}

		&__uuid {
			font-size: 0.75rem;
			font-family: monospace;
			color: var(--md-on-surface-variant);
		}

		&__grid {
			display: grid;
			grid-template-columns: repeat(2, 1fr);
			gap: 1rem;
		}

		&__card {
			padding: 1rem;
			border-radius: 0.75rem;
			border: 1px solid var(--md-outline-variant);
			background-color: var(--md-surface-container-low);
		}

		&__card-label {
			font-size: 0.75rem;
			color: var(--md-on-surface-variant);
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;

			&--with-icon {
				display: flex;
				align-items: center;
				gap: 0.25rem;
			}
		}

		&__card-icon {
			width: 0.875rem;
			height: 0.875rem;
		}

		&__card-value {
			font-weight: 700;
			font-size: 0.875rem;
			margin-top: 0.25rem;

			&--primary {
				color: var(--md-primary);
			}
		}
	}
</style>
