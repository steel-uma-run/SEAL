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

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Success & Error Banners -->
	{#if successMessage}
		<div
			class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-emerald-500/10 border-l-4 border-emerald-500 p-4 rounded-xl max-w-md"
		>
			<CheckCircle2 class="w-5 h-5 text-emerald-500" />
			<div>
				<p class="text-sm font-semibold text-emerald-800 dark:text-emerald-300">Success</p>
				<p class="text-xs text-emerald-700 dark:text-emerald-400 mt-0.5">{successMessage}</p>
			</div>
		</div>
	{/if}

	{#if errorMessage}
		<div
			class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-rose-500/10 border-l-4 border-rose-500 p-4 rounded-xl max-w-md"
		>
			<AlertTriangle class="w-5 h-5 text-rose-500" />
			<div>
				<p class="text-sm font-semibold text-rose-800 dark:text-rose-300">Error</p>
				<p class="text-xs text-rose-700 dark:text-rose-400 mt-0.5">{errorMessage}</p>
			</div>
		</div>
	{/if}

	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-6 border-b border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				User Management
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Manage, edit, and audit system users for the SEAL event platform.
			</p>
		</div>

		<div class="flex items-center gap-3 mt-4 md:mt-0">
			<button
				onclick={openAddModal}
				class="flex items-center gap-2 bg-(--md-primary) text-(--md-on-primary) px-5 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer border-0 hover:opacity-90 active:scale-98"
			>
				<Plus class="w-4 h-4" />
				Add User
			</button>
		</div>
	</header>

	<!-- Statistics Cards -->
	<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
		<!-- Card 1: Total Users -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) flex items-center gap-5 transition-all text-(--md-on-surface)"
		>
			<div
				class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-primary-container) text-(--md-on-primary-container)"
			>
				<Users class="w-6 h-6" />
			</div>
			<div>
				<p
					class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
				>
					Total Users
				</p>
				<h3 class="text-2xl font-bold">
					{totalUsersCount}
				</h3>
			</div>
		</div>

		<!-- Card 2: Students -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) flex items-center gap-5 transition-all text-(--md-on-surface)"
		>
			<div
				class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-secondary-container) text-(--md-on-secondary-container)"
			>
				<GraduationCap class="w-6 h-6" />
			</div>
			<div>
				<p
					class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
				>
					Students
				</p>
				<h3 class="text-2xl font-bold">
					{studentsCount}
				</h3>
			</div>
		</div>

		<!-- Card 3: Lecturers -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) flex items-center gap-5 transition-all text-(--md-on-surface)"
		>
			<div
				class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-tertiary-container) text-(--md-on-tertiary-container)"
			>
				<BookOpen class="w-6 h-6" />
			</div>
			<div>
				<p
					class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
				>
					Lecturers
				</p>
				<h3 class="text-2xl font-bold">
					{lecturersCount}
				</h3>
			</div>
		</div>

		<!-- Card 4: Coordinators -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) flex items-center gap-5 transition-all text-(--md-on-surface)"
		>
			<div
				class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-surface-container-high) text-(--md-on-surface)"
			>
				<Shield class="w-6 h-6" />
			</div>
			<div>
				<p
					class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
				>
					Coordinators
				</p>
				<h3 class="text-2xl font-bold">
					{coordinatorsCount}
				</h3>
			</div>
		</div>
	</div>

	<!-- Filters and Table Container -->
	<div
		class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface)"
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
					placeholder="Search by ID, name, or email..."
					class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-(--md-outline) outline-none transition-all text-sm bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) focus:border-transparent"
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

			<!-- Role & Status selectors -->
			<div class="flex flex-wrap items-center gap-3 w-full md:w-auto">
				<div class="flex items-center gap-2 text-sm text-(--md-on-surface-variant)">
					<Filter class="w-4 h-4" />
					Filters:
				</div>
				<!-- Role Filter -->
				<select
					bind:value={roleFilter}
					class="rounded-xl border border-(--md-outline) px-3 py-2 text-sm outline-none transition-all bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
				>
					<option value="ALL">All Roles</option>
					<option value="STUDENT">Student</option>
					<option value="LECTURER">Lecturer</option>
					<option value="COORDINATOR">Coordinator</option>
				</select>

				<!-- Status Filter -->
				<select
					bind:value={statusFilter}
					class="rounded-xl border border-(--md-outline) px-3 py-2 text-sm outline-none transition-all bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
				>
					<option value="ALL">All Statuses</option>
					<option value="ACTIVE">Active</option>
					<option value="INACTIVE">Inactive</option>
					<option value="PENDING">Pending</option>
					<option value="BANNED">Banned</option>
				</select>
			</div>
		</div>

		<!-- User Table -->
		<div class="overflow-x-auto">
			<table class="w-full text-left border-collapse">
				<thead>
					<tr
						class="border-b border-(--md-outline-variant) text-(--md-on-surface-variant) text-xs font-bold uppercase tracking-wider"
					>
						<th class="py-4 px-4">User ID</th>
						<th class="py-4 px-4">Full Name</th>
						<th class="py-4 px-4">Email</th>
						<th class="py-4 px-4">System Role</th>
						<th class="py-4 px-4">Status</th>
						<th class="py-4 px-4 text-center">Actions</th>
					</tr>
				</thead>
				<tbody class="text-sm">
					{#if paginatedUsers.length > 0}
						{#each paginatedUsers as user}
							<tr
								class="border-b border-(--md-outline-variant)/50 hover:bg-(--md-surface-container-high) text-(--md-on-surface)"
							>
								<td
									class="py-4 px-4 font-mono text-xs font-semibold text-(--md-on-surface-variant)"
								>
									#{user.id}
								</td>
								<td class="py-4 px-4 font-bold">
									{user.name}
								</td>
								<td class="py-4 px-4 text-sm text-(--md-on-surface-variant)">
									{user.email}
								</td>
								<td class="py-4 px-4">
									{#if user.role === "COORDINATOR"}
										<span
											class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-primary)/10"
										>
											Coordinator
										</span>
									{:else if user.role === "LECTURER"}
										<span
											class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-secondary)/10"
										>
											Lecturer
										</span>
									{:else}
										<span
											class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-(--md-tertiary-container) text-(--md-on-tertiary-container) border border-(--md-tertiary)/10"
										>
											Student
										</span>
									{/if}
								</td>
								<td class="py-4 px-4">
									{#if user.status === "ACTIVE"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
										>
											<span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
											Active
										</span>
									{:else if user.status === "INACTIVE"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-500/10 text-gray-400 border border-gray-500/20"
										>
											<span class="w-1.5 h-1.5 rounded-full bg-gray-400"></span>
											Inactive
										</span>
									{:else if user.status === "PENDING"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-500 border border-amber-500/20"
										>
											<span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
											Pending
										</span>
									{:else if user.status === "BANNED"}
										<span
											class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-500 border border-rose-500/20"
										>
											<span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
											Banned
										</span>
									{/if}
								</td>
								<td class="py-4 px-4 text-center">
									<div class="flex justify-center gap-2">
										{#if user.status === "PENDING"}
											<button
												onclick={() => handleApproveStudent(user)}
												title="Approve Account"
												class="p-2 rounded-lg hover:bg-emerald-500/10 hover:text-emerald-500 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent"
											>
												<CheckCircle2 class="w-4.5 h-4.5 text-emerald-500" />
											</button>
										{/if}
										<button
											onclick={() => openEditModal(user)}
											title="Edit User"
											class="p-2 rounded-lg hover:bg-(--md-primary-container) hover:text-(--md-on-primary-container) text-(--md-on-surface-variant) transition-colors cursor-pointer border-0 bg-transparent"
										>
											<Edit3 class="w-4.5 h-4.5 text-(--md-primary)" />
										</button>
										<button
											onclick={() => openViewModal(user)}
											title="View Details"
											class="p-2 rounded-lg hover:bg-(--md-secondary-container) hover:text-(--md-on-secondary-container) text-(--md-on-surface-variant) transition-colors cursor-pointer border-0 bg-transparent"
										>
											<Eye class="w-4 h-4 text-blue-500" />
										</button>
										<button
											onclick={() => openDeleteModal(user)}
											title="Delete User"
											class="p-2 rounded-lg hover:bg-rose-500/10 hover:text-rose-500 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent"
										>
											<Trash2 class="w-4 h-4 text-rose-500" />
										</button>
									</div>
								</td>
							</tr>
						{/each}
					{:else}
						<tr>
							<td colspan="6" class="py-12 text-center text-(--md-on-surface-variant)">
								<div class="flex flex-col items-center gap-2">
									<Search class="w-8 h-8 opacity-40" />
									<p class="font-semibold text-lg mt-2">No users found</p>
									<p class="text-sm">Try adjusting your filters or search terms.</p>
								</div>
							</td>
						</tr>
					{/if}
				</tbody>
			</table>
		</div>

		<!-- Pagination Controls -->
		{#if totalPages > 1}
			<div
				class="flex flex-col sm:flex-row justify-between items-center gap-4 mt-6 pt-4 border-t border-(--md-outline-variant)"
			>
				<span class="text-xs text-(--md-on-surface-variant)">
					Showing {Math.min(filteredUsers.length, (currentPage - 1) * itemsPerPage + 1)}-{Math.min(
						filteredUsers.length,
						currentPage * itemsPerPage
					)} of {filteredUsers.length} users
				</span>
				<div class="flex items-center gap-3">
					<button
						onclick={() => (currentPage = Math.max(1, currentPage - 1))}
						disabled={currentPage === 1}
						class="p-2 rounded-lg border border-(--md-outline) text-(--md-on-surface) hover:bg-(--md-surface-container-high) disabled:opacity-40 disabled:cursor-not-allowed transition-all cursor-pointer bg-transparent"
					>
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M15 19l-7-7 7-7"
							/>
						</svg>
					</button>
					<span class="text-xs font-bold text-(--md-on-surface)">
						Page {currentPage} of {totalPages}
					</span>
					<button
						onclick={() => (currentPage = Math.min(totalPages, currentPage + 1))}
						disabled={currentPage === totalPages}
						class="p-2 rounded-lg border border-(--md-outline) text-(--md-on-surface) hover:bg-(--md-surface-container-high) disabled:opacity-40 disabled:cursor-not-allowed transition-all cursor-pointer bg-transparent"
					>
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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

<!-- Modal: Add User -->
{#if showAddModal}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/45 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-lg rounded-2xl border p-8 relative transition-all bg-(--md-surface-container-high) border-(--md-outline-variant) text-(--md-on-surface)"
		>
			<button
				onclick={() => (showAddModal = false)}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-low) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<User class="w-5 h-5 text-(--md-primary)" />
				Add User Account
			</h3>

			<form onsubmit={handleAddUser} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label for="add-full-name" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>Full Name *</label
					>
					<input
						id="add-full-name"
						type="text"
						bind:value={formName}
						required
						placeholder="Dr. Nguyen Van A"
						class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) focus:border-transparent"
					/>
				</div>

				<div class="space-y-1">
					<label for="add-email" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>Email Address *</label
					>
					<input
						id="add-email"
						type="email"
						bind:value={formEmail}
						required
						placeholder="anv@fpt.edu.vn"
						class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) focus:border-transparent"
					/>
				</div>

				<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
					<div class="space-y-1">
						<label
							for="add-system-role"
							class="text-sm font-semibold text-(--md-on-surface-variant)">System Role</label
						>
						<select
							id="add-system-role"
							bind:value={formRole}
							class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
						>
							<option value="STUDENT">Student</option>
							<option value="LECTURER">Lecturer</option>
							<option value="COORDINATOR">Coordinator</option>
						</select>
					</div>

					{#if formRole === "STUDENT"}
						<div class="space-y-1">
							<label
								for="add-student-id"
								class="text-sm font-semibold text-(--md-on-surface-variant)">Student ID</label
							>
							<input
								id="add-student-id"
								type="text"
								bind:value={formStudentId}
								placeholder="SE160000"
								class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) focus:border-transparent"
							/>
						</div>
					{/if}
				</div>

				{#if formRole === "STUDENT"}
					<div class="space-y-1">
						<label for="add-status" class="text-sm font-semibold text-(--md-on-surface-variant)"
							>Status</label
						>
						<select
							id="add-status"
							bind:value={formStatus}
							class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
						>
							<option value="ACTIVE">Active</option>
							<option value="INACTIVE">Inactive</option>
							<option value="PENDING">Pending</option>
							<option value="BANNED">Banned</option>
						</select>
					</div>
				{/if}

				<div class="flex gap-3 mt-6">
					<button
						type="button"
						onclick={() => (showAddModal = false)}
						class="w-1/2 rounded-xl border border-(--md-outline) bg-transparent text-(--md-on-surface) py-3 font-semibold transition-all cursor-pointer hover:bg-(--md-surface-container-low)"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="w-1/2 bg-(--md-primary) text-(--md-on-primary) rounded-xl py-3 font-semibold transition-all cursor-pointer border-0 hover:opacity-90 active:scale-98"
					>
						Create User
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- Modal: Edit User -->
{#if showEditModal && selectedUser}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/45 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-lg rounded-2xl border p-8 relative transition-all bg-(--md-surface-container-high) border-(--md-outline-variant) text-(--md-on-surface)"
		>
			<button
				onclick={() => (showEditModal = false)}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-low) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Edit3 class="w-5 h-5 text-(--md-primary)" />
				Edit User Account
			</h3>

			<form onsubmit={handleEditUser} class="flex flex-col gap-4">
				<div class="space-y-1">
					<label for="edit-user-id" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>User ID</label
					>
					<input
						id="edit-user-id"
						type="text"
						value="#{selectedUser.id}"
						disabled
						class="w-full rounded-xl border p-3 bg-(--md-surface-container-low) text-(--md-on-surface-variant) cursor-not-allowed outline-none border-(--md-outline-variant)"
					/>
				</div>

				<div class="space-y-1">
					<label for="edit-full-name" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>Full Name</label
					>
					<input
						id="edit-full-name"
						type="text"
						bind:value={formName}
						required
						class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary) focus:border-transparent"
					/>
				</div>

				<div class="space-y-1">
					<label for="edit-email" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>Email (Cannot be modified)</label
					>
					<input
						id="edit-email"
						type="email"
						value={formEmail}
						disabled
						class="w-full rounded-xl border p-3 bg-(--md-surface-container-low) text-(--md-on-surface-variant) cursor-not-allowed outline-none border-(--md-outline-variant)"
					/>
				</div>

				<div class="space-y-1">
					<label for="edit-system-role" class="text-sm font-semibold text-(--md-on-surface-variant)"
						>System Role</label
					>
					<select
						id="edit-system-role"
						bind:value={formRole}
						class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
					>
						<option value="STUDENT">Student</option>
						<option value="LECTURER">Lecturer</option>
						<option value="COORDINATOR">Coordinator</option>
					</select>
				</div>

				{#if formRole === "STUDENT"}
					<div class="space-y-1">
						<label for="edit-status" class="text-sm font-semibold text-(--md-on-surface-variant)"
							>Student Status</label
						>
						<select
							id="edit-status"
							bind:value={formStatus}
							class="w-full rounded-xl border p-3 outline-none transition-all border-(--md-outline) bg-(--md-surface-container) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
						>
							<option value="ACTIVE">Active</option>
							<option value="INACTIVE">Inactive</option>
							<option value="PENDING">Pending</option>
							<option value="BANNED">Banned</option>
						</select>
					</div>
				{/if}

				<div class="flex gap-3 mt-6">
					<button
						type="button"
						onclick={() => (showEditModal = false)}
						class="w-1/2 rounded-xl border border-(--md-outline) bg-transparent text-(--md-on-surface) py-3 font-semibold transition-all cursor-pointer hover:bg-(--md-surface-container-low)"
					>
						Cancel
					</button>
					<button
						type="submit"
						class="w-1/2 bg-(--md-primary) text-(--md-on-primary) rounded-xl py-3 font-semibold transition-all cursor-pointer border-0 hover:opacity-90 active:scale-98"
					>
						Save Changes
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}

<!-- Modal: View User Details -->
{#if showViewModal && selectedUser}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/45 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-lg rounded-2xl border p-8 relative transition-all bg-(--md-surface-container-high) border-(--md-outline-variant) text-(--md-on-surface)"
		>
			<button
				onclick={() => (showViewModal = false)}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-low) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-6 flex items-center gap-2">
				<Eye class="w-5 h-5 text-(--md-secondary)" />
				User Details Profile
			</h3>

			<div class="space-y-4">
				<div
					class="flex items-center gap-4 p-4 rounded-xl bg-(--md-surface-container-low) border border-(--md-outline-variant)"
				>
					<div
						class="w-14 h-14 rounded-full flex items-center justify-center text-2xl font-bold bg-(--md-primary-container) text-(--md-on-primary-container)"
					>
						{selectedUser.name.charAt(0).toUpperCase()}
					</div>
					<div>
						<h4 class="font-extrabold text-lg">{selectedUser.name}</h4>
						<p class="text-xs font-mono text-(--md-on-surface-variant)">
							UUID: {selectedUser.id}
						</p>
					</div>
				</div>

				<div class="grid grid-cols-2 gap-4">
					<div
						class="p-4 rounded-xl border bg-(--md-surface-container-low) border-(--md-outline-variant)"
					>
						<p
							class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider"
						>
							System Role
						</p>
						<p class="font-bold text-sm mt-1">{selectedUser.role}</p>
					</div>
					<div
						class="p-4 rounded-xl border bg-(--md-surface-container-low) border-(--md-outline-variant)"
					>
						<p
							class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider"
						>
							Status
						</p>
						<p class="font-bold text-sm mt-1">{selectedUser.status}</p>
					</div>
				</div>

				<div
					class="p-4 rounded-xl border bg-(--md-surface-container-low) border-(--md-outline-variant)"
				>
					<p
						class="text-xs text-(--md-on-surface-variant) font-semibold uppercase tracking-wider flex items-center gap-1"
					>
						<Mail class="w-3.5 h-3.5" />
						Email Address
					</p>
					<p class="font-bold text-sm mt-1 text-(--md-primary)">{selectedUser.email}</p>
				</div>
			</div>

			<button
				onclick={() => (showViewModal = false)}
				class="mt-8 border border-(--md-outline) bg-transparent text-(--md-on-surface) hover:bg-(--md-surface-container-low) rounded-xl py-3 font-semibold transition-all w-full cursor-pointer"
			>
				Close Profile
			</button>
		</div>
	</div>
{/if}

<!-- Modal: Delete User Confirmation -->
{#if showDeleteModal && selectedUser}
	<div
		class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/45 backdrop-blur-xs p-4"
	>
		<div
			class="w-full max-w-md rounded-2xl border p-8 relative transition-all bg-(--md-surface-container-high) border-(--md-outline-variant) text-(--md-on-surface)"
		>
			<button
				onclick={() => (showDeleteModal = false)}
				class="absolute top-4 right-4 p-1 rounded-lg hover:bg-(--md-surface-container-low) transition-colors text-(--md-on-surface-variant) cursor-pointer border-0 bg-transparent"
			>
				<X class="w-5 h-5" />
			</button>
			<h3 class="text-xl font-bold mb-4 flex items-center gap-2 text-rose-500">
				<Trash2 class="w-5 h-5" />
				Delete User Account?
			</h3>

			<p class="text-sm text-(--md-on-surface-variant)">
				Are you sure you want to permanently delete the user <strong class="text-(--md-primary)"
					>{selectedUser.name}</strong
				>
				({selectedUser.email})? This action is irreversible and will remove all their records from
				the platform.
			</p>

			<div class="flex gap-3 mt-8">
				<button
					onclick={() => (showDeleteModal = false)}
					class="w-1/2 rounded-xl border border-(--md-outline) bg-transparent text-(--md-on-surface) py-3 font-semibold transition-all cursor-pointer hover:bg-(--md-surface-container-low)"
				>
					Cancel
				</button>
				<button
					onclick={handleDeleteUser}
					class="w-1/2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl py-3 font-semibold transition-all cursor-pointer border-0"
				>
					Delete User
				</button>
			</div>
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
