<script lang="ts">
    import { theme } from "$lib/theme.svelte";
    import { 
        Users, 
        GraduationCap, 
        Shield, 
        BookOpen, 
        Search, 
        Filter, 
        Plus, 
        Upload, 
        Edit3, 
        Eye, 
        Trash2, 
        X, 
        Mail, 
        User, 
        CheckCircle2, 
        AlertTriangle 
    } from "@lucide/svelte";

    // Mock Users Data
    let users = $state([
        { id: "100001", name: "Ngay Name", email: "haachkly@gmail.com", role: "COORDINATOR", status: "ACTIVE" },
        { id: "100002", name: "Slimath Name", email: "shutiah@gmail.com", role: "STUDENT", status: "INACTIVE" },
        { id: "100003", name: "Briah Name", email: "briah@gmail.com", role: "LECTURER", status: "ACTIVE" },
        { id: "100004", name: "Jarm Aldnen", email: "jarm@gmail.com", role: "LECTURER", status: "INACTIVE" },
        { id: "100005", name: "Nguyen Van A", email: "nva@fpt.edu.vn", role: "STUDENT", status: "PENDING" },
        { id: "100006", name: "Tran Thi B", email: "ttb@fpt.edu.vn", role: "STUDENT", status: "BANNED" }
    ]);

    // Search & Filter State
    let searchQuery = $state("");
    let roleFilter = $state("ALL");
    let statusFilter = $state("ALL");

    // Modal Control State
    let showAddModal = $state(false);
    let showImportModal = $state(false);
    let showEditModal = $state(false);
    let showViewModal = $state(false);
    let showDeleteModal = $state(false);

    // Selected User State
    let selectedUser = $state<any>(null);

    // Form inputs state
    let formName = $state("");
    let formEmail = $state("");
    let formRole = $state("STUDENT");
    let formStatus = $state("ACTIVE");
    let formStudentId = $state("");

    // Message notifications
    let successMessage = $state("");
    let errorMessage = $state("");

    // CSV input for import simulation
    let csvData = $state("");

    // Derived filtered users list
    let filteredUsers = $derived(
        users.filter(u => {
            const matchesSearch = u.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                  u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                  u.id.includes(searchQuery);
            const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
            const matchesStatus = statusFilter === "ALL" || u.status === statusFilter;
            return matchesSearch && matchesRole && matchesStatus;
        })
    );

    // Derived Statistics
    let totalUsersCount = $derived(users.length);
    let studentsCount = $derived(users.filter(u => u.role === "STUDENT").length);
    let lecturersCount = $derived(users.filter(u => u.role === "LECTURER").length);
    let coordinatorsCount = $derived(users.filter(u => u.role === "COORDINATOR").length);

    // Helper functions
    function showNotification(msg: string, type: "success" | "error" = "success") {
        if (type === "success") {
            successMessage = msg;
            setTimeout(() => successMessage = "", 3000);
        } else {
            errorMessage = msg;
            setTimeout(() => errorMessage = "", 3000);
        }
    }

    function openAddModal() {
        formName = "";
        formEmail = "";
        formRole = "STUDENT";
        formStatus = "ACTIVE";
        formStudentId = "";
        showAddModal = true;
    }

    function handleAddUser(e: Event) {
        e.preventDefault();
        
        if (!formName.trim() || !formEmail.trim()) {
            showNotification("Please fill in all required fields.", "error");
            return;
        }

        // Check if email already exists
        if (users.some(u => u.email.toLowerCase() === formEmail.toLowerCase())) {
            showNotification("Email already registered.", "error");
            return;
        }

        const newId = String(100000 + users.length + 1);
        const newUser = {
            id: newId,
            name: formName,
            email: formEmail,
            role: formRole,
            status: formRole === "STUDENT" ? formStatus : "ACTIVE"
        };

        users = [...users, newUser];
        showAddModal = false;
        showNotification("User created successfully!");
    }

    function openEditModal(user: any) {
        selectedUser = user;
        formName = user.name;
        formEmail = user.email;
        formRole = user.role;
        formStatus = user.status;
        showEditModal = true;
    }

    function handleEditUser(e: Event) {
        e.preventDefault();
        
        if (!formName.trim()) {
            showNotification("Name cannot be empty.", "error");
            return;
        }

        users = users.map(u => {
            if (u.id === selectedUser.id) {
                return {
                    ...u,
                    name: formName,
                    role: formRole,
                    status: formRole === "STUDENT" ? formStatus : "ACTIVE"
                };
            }
            return u;
        });

        showEditModal = false;
        showNotification("User updated successfully!");
    }

    function openViewModal(user: any) {
        selectedUser = user;
        showViewModal = true;
    }

    function openDeleteModal(user: any) {
        selectedUser = user;
        showDeleteModal = true;
    }

    function handleDeleteUser() {
        users = users.filter(u => u.id !== selectedUser.id);
        showDeleteModal = false;
        showNotification("User deleted successfully!");
    }

    function handleImportCSV(e: Event) {
        e.preventDefault();
        if (!csvData.trim()) {
            showNotification("Please paste some CSV data first.", "error");
            return;
        }

        // Simple CSV parsing simulation: Full Name, Email, Role
        const lines = csvData.trim().split("\n");
        let addedCount = 0;
        let errors = 0;

        lines.forEach((line, index) => {
            // Skip header if detected
            if (index === 0 && line.toLowerCase().includes("email")) return;

            const parts = line.split(",");
            if (parts.length >= 2) {
                const name = parts[0].trim();
                const email = parts[1].trim();
                let role = parts[2]?.trim().toUpperCase() || "STUDENT";
                
                if (role !== "STUDENT" && role !== "LECTURER" && role !== "COORDINATOR") {
                    role = "STUDENT";
                }

                if (name && email && !users.some(u => u.email.toLowerCase() === email.toLowerCase())) {
                    const newId = String(100000 + users.length + 1);
                    users.push({
                        id: newId,
                        name,
                        email,
                        role,
                        status: "ACTIVE"
                    });
                    addedCount++;
                } else {
                    errors++;
                }
            }
        });

        // Trigger reactivity for users array
        users = [...users];
        showImportModal = false;
        csvData = "";
        
        if (addedCount > 0) {
            showNotification(`Imported ${addedCount} users successfully! ${errors > 0 ? `${errors} skipped.` : ""}`);
        } else {
            showNotification("No new users imported. Check formatting or duplicates.", "error");
        }
    }
</script>

<svelte:head>
    <title>User Management - SEAL</title>
</svelte:head>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
    <!-- Success & Error Banners -->
    {#if successMessage}
        <div class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-emerald-50 border-l-4 border-emerald-500 p-4 rounded shadow-lg animate-fade-in max-w-md">
            <CheckCircle2 class="w-5 h-5 text-emerald-500" />
            <div>
                <p class="text-sm font-semibold text-emerald-800">Success</p>
                <p class="text-xs text-emerald-700 mt-0.5">{successMessage}</p>
            </div>
        </div>
    {/if}

    {#if errorMessage}
        <div class="fixed top-6 right-6 z-[5000] flex items-center gap-3 bg-rose-50 border-l-4 border-rose-500 p-4 rounded shadow-lg animate-fade-in max-w-md">
            <AlertTriangle class="w-5 h-5 text-rose-500" />
            <div>
                <p class="text-sm font-semibold text-rose-800">Error</p>
                <p class="text-xs text-rose-700 mt-0.5">{errorMessage}</p>
            </div>
        </div>
    {/if}

    <!-- Header Section -->
    <header class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-6 border-b {theme.darkMode ? 'border-zinc-800' : 'border-gray-100'}">
        <div>
            <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
                User Management
            </h1>
            <p class="mt-1 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                Manage, edit, and audit system users for the SEAL event platform.
            </p>
        </div>
        
        <div class="flex items-center gap-3 mt-4 md:mt-0">
            <button onclick={openAddModal} class="flex items-center gap-2 bg-[#f97316] hover:bg-[#ea580c] text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition-all cursor-pointer border-0">
                <Plus class="w-4 h-4" />
                Add User
            </button>
        </div>
    </header>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Card 1: Total Users -->
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-orange-950/40 text-orange-400' : 'bg-[#fff2e8] text-[#ea580c]'}">
                <Users class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Total Users</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{totalUsersCount}</h3>
            </div>
        </div>

        <!-- Card 2: Students -->
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-blue-950/40 text-blue-400' : 'bg-blue-50 text-blue-600'}">
                <GraduationCap class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Students</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{studentsCount}</h3>
            </div>
        </div>

        <!-- Card 3: Lecturers -->
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-green-950/40 text-green-400' : 'bg-green-50 text-green-600'}">
                <BookOpen class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Lecturers</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{lecturersCount}</h3>
            </div>
        </div>

        <!-- Card 4: Coordinators -->
        <div class="p-6 rounded-2xl border flex items-center gap-5 transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode ? 'bg-purple-950/40 text-purple-400' : 'bg-purple-50 text-purple-600'}">
                <Shield class="w-6 h-6" />
            </div>
            <div>
                <p class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">Coordinators</p>
                <h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{coordinatorsCount}</h3>
            </div>
        </div>
    </div>

    <!-- Filters and Table Container -->
    <div class="p-8 rounded-3xl border transition-all {theme.darkMode ? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
        
        <!-- Filters Toolbar -->
        <div class="flex flex-col md:flex-row gap-4 justify-between items-center mb-6">
            <!-- Search bar -->
            <div class="relative w-full md:max-w-md">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
                    <Search class="w-4 h-4" />
                </span>
                <input 
                    type="text" 
                    bind:value={searchQuery}
                    placeholder="Search by ID, name, or email..." 
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl border outline-none transition-all text-sm {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-650 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}"
                />
            </div>

            <!-- Role & Status selectors -->
            <div class="flex flex-wrap items-center gap-3 w-full md:w-auto">
                <div class="flex items-center gap-2 text-sm text-gray-500">
                    <Filter class="w-4 h-4" />
                    Filters:
                </div>
                <!-- Role Filter -->
                <select bind:value={roleFilter} class="rounded-xl border px-3 py-2 text-sm outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                    <option value="ALL">All Roles</option>
                    <option value="STUDENT">Student</option>
                    <option value="LECTURER">Lecturer</option>
                    <option value="COORDINATOR">Coordinator</option>
                </select>

                <!-- Status Filter -->
                <select bind:value={statusFilter} class="rounded-xl border px-3 py-2 text-sm outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
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
                    <tr class="border-b {theme.darkMode ? 'border-zinc-800 text-zinc-400' : 'border-gray-100 text-gray-400'} text-xs font-bold uppercase tracking-wider">
                        <th class="py-4 px-4">User ID</th>
                        <th class="py-4 px-4">Full Name</th>
                        <th class="py-4 px-4">Email</th>
                        <th class="py-4 px-4">System Role</th>
                        <th class="py-4 px-4">Status</th>
                        <th class="py-4 px-4 text-center">Actions</th>
                    </tr>
                </thead>
                <tbody class="text-sm">
                    {#if filteredUsers.length > 0}
                        {#each filteredUsers as user}
                            <tr class="border-b transition-colors {theme.darkMode ? 'border-zinc-800/50 hover:bg-zinc-800/20 text-zinc-100' : 'border-gray-50 hover:bg-gray-50/50 text-gray-700'}">
                                <td class="py-4 px-4 font-mono text-xs font-semibold {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                    #{user.id}
                                </td>
                                <td class="py-4 px-4 font-bold">
                                    {user.name}
                                </td>
                                <td class="py-4 px-4 text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
                                    {user.email}
                                </td>                 
                                <td class="py-4 px-4">
                                    {#if user.role === "COORDINATOR"}
                                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-orange-50 text-orange-700 border border-orange-200 dark:bg-orange-500/10 dark:text-orange-400 dark:border-orange-500/20">
                                            Coordinator
                                        </span>
                                    {:else if user.role === "LECTURER"}
                                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/20">
                                            Lecturer
                                        </span>
                                    {:else}
                                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200 dark:bg-blue-500/10 dark:text-blue-400 dark:border-blue-500/20">
                                            Student
                                        </span>
                                    {/if}
                                </td>
                                <td class="py-4 px-4">
                                    {#if user.status === "ACTIVE"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
                                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                                            Active
                                        </span>
                                    {:else if user.status === "INACTIVE"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-500/10 text-gray-400 border border-gray-500/20">
                                            <span class="w-1.5 h-1.5 rounded-full bg-gray-400"></span>
                                            Inactive
                                        </span>
                                    {:else if user.status === "PENDING"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-500 border border-amber-500/20">
                                            <span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                                            Pending
                                        </span>
                                    {:else if user.status === "BANNED"}
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-500 border border-rose-500/20">
                                            <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                                            Banned
                                        </span>
                                    {/if}
                                </td>
                                <td class="py-4 px-4 text-center">
                                    <div class="flex justify-center gap-2">
                                        <button 
                                            onclick={() => openEditModal(user)} 
                                            title="Edit User"
                                            class="p-2 rounded-lg hover:bg-orange-100 hover:text-orange-600 dark:hover:bg-orange-950/40 dark:hover:text-orange-400 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent"
                                        >
                                            <Edit3 class="w-4 h-4" />
                                        </button>
                                        <button 
                                            onclick={() => openViewModal(user)} 
                                            title="View Details"
                                            class="p-2 rounded-lg hover:bg-blue-100 hover:text-blue-600 dark:hover:bg-blue-950/40 dark:hover:text-blue-400 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent"
                                        >
                                            <Eye class="w-4 h-4" />
                                        </button>
                                        <button 
                                            onclick={() => openDeleteModal(user)} 
                                            title="Delete User"
                                            class="p-2 rounded-lg hover:bg-rose-100 hover:text-rose-600 dark:hover:bg-rose-950/40 dark:hover:text-rose-400 text-zinc-400 transition-colors cursor-pointer border-0 bg-transparent"
                                        >
                                            <Trash2 class="w-4 h-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        {/each}
                    {:else}
                        <tr>
                            <td colspan="6" class="py-12 text-center text-gray-500 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
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
    </div>
</div>

<!-- ======================================= MODALS ======================================= -->

<!-- Modal: Add User -->
{#if showAddModal}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={() => showAddModal = false} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 flex items-center gap-2">
                <User class="w-5 h-5 text-orange-500" />
                Add User Account
            </h3>
            
            <form onsubmit={handleAddUser} class="flex flex-col gap-4">
                <div class="space-y-1">
                    <label for="add-full-name" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Full Name *</label>
                    <input id="add-full-name" type="text" bind:value={formName} required placeholder="Dr. Nguyen Van A" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                </div>

                <div class="space-y-1">
                    <label for="add-email" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Email Address *</label>
                    <input id="add-email" type="email" bind:value={formEmail} required placeholder="anv@fpt.edu.vn" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-600 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="space-y-1">
                        <label for="add-system-role" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">System Role</label>
                        <select id="add-system-role" bind:value={formRole} class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                            <option value="STUDENT">Student</option>
                            <option value="LECTURER">Lecturer</option>
                            <option value="COORDINATOR">Coordinator</option>
                        </select>
                    </div>

                    {#if formRole === "STUDENT"}
                        <div class="space-y-1">
                            <label for="add-student-id" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Student ID</label>
                            <input id="add-student-id" type="text" bind:value={formStudentId} placeholder="SE160000" class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-650 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                        </div>
                    {/if}
                </div>

                {#if formRole === "STUDENT"}
                    <div class="space-y-1">
                        <label for="add-status" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Status</label>
                        <select id="add-status" bind:value={formStatus} class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                            <option value="ACTIVE">Active</option>
                            <option value="INACTIVE">Inactive</option>
                            <option value="PENDING">Pending</option>
                            <option value="BANNED">Banned</option>
                        </select>
                    </div>
                {/if}

                <div class="flex gap-3 mt-6">
                    <button type="button" onclick={() => showAddModal = false} class="w-1/2 rounded-xl border py-3 font-semibold transition-all cursor-pointer {theme.darkMode ? 'border-zinc-800 bg-transparent text-zinc-300 hover:bg-zinc-800/20' : 'border-gray-250 bg-transparent text-gray-700 hover:bg-gray-50'}">
                        Cancel
                    </button>
                    <button type="submit" class="w-1/2 bg-[#f97316] hover:bg-[#ea580c] text-white rounded-xl py-3 font-semibold transition-all cursor-pointer border-0">
                        Create User
                    </button>
                </div>
            </form>
        </div>
    </div>
{/if}

<!-- Modal: Edit User -->
{#if showEditModal && selectedUser}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={() => showEditModal = false} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 flex items-center gap-2">
                <Edit3 class="w-5 h-5 text-orange-500" />
                Edit User Account
            </h3>
            
            <form onsubmit={handleEditUser} class="flex flex-col gap-4">
                <div class="space-y-1">
                    <label for="edit-user-id" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">User ID</label>
                    <input id="edit-user-id" type="text" value="#{selectedUser.id}" disabled class="w-full rounded-xl border p-3 bg-gray-100 dark:bg-zinc-950/40 text-gray-500 cursor-not-allowed outline-none border-gray-200 dark:border-zinc-800" />
                </div>

                <div class="space-y-1">
                    <label for="edit-full-name" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Full Name</label>
                    <input id="edit-full-name" type="text" bind:value={formName} required class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}" />
                </div>

                <div class="space-y-1">
                    <label for="edit-email" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Email (Cannot be modified)</label>
                    <input id="edit-email" type="email" value={formEmail} disabled class="w-full rounded-xl border p-3 bg-gray-100 dark:bg-zinc-950/40 text-gray-500 cursor-not-allowed outline-none border-gray-200 dark:border-zinc-800" />
                </div>

                <div class="space-y-1">
                    <label for="edit-system-role" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">System Role</label>
                    <select id="edit-system-role" bind:value={formRole} class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                        <option value="STUDENT">Student</option>
                        <option value="LECTURER">Lecturer</option>
                        <option value="COORDINATOR">Coordinator</option>
                    </select>
                </div>

                {#if formRole === "STUDENT"}
                    <div class="space-y-1">
                        <label for="edit-status" class="text-sm font-semibold {theme.darkMode ? 'text-zinc-300' : 'text-gray-700'}">Student Status</label>
                        <select id="edit-status" bind:value={formStatus} class="w-full rounded-xl border p-3 outline-none transition-all {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 focus:ring-2 focus:ring-orange-500' : 'border-gray-200 bg-gray-50 focus:ring-2 focus:ring-orange-500'}">
                            <option value="ACTIVE">Active</option>
                            <option value="INACTIVE">Inactive</option>
                            <option value="PENDING">Pending</option>
                            <option value="BANNED">Banned</option>
                        </select>
                    </div>
                {/if}

                <div class="flex gap-3 mt-6">
                    <button type="button" onclick={() => showEditModal = false} class="w-1/2 rounded-xl border py-3 font-semibold transition-all cursor-pointer {theme.darkMode ? 'border-zinc-800 bg-transparent text-zinc-300 hover:bg-zinc-800/20' : 'border-gray-250 bg-transparent text-gray-700 hover:bg-gray-50'}">
                        Cancel
                    </button>
                    <button type="submit" class="w-1/2 bg-[#f97316] hover:bg-[#ea580c] text-white rounded-xl py-3 font-semibold transition-all cursor-pointer border-0">
                        Save Changes
                    </button>
                </div>
            </form>
        </div>
    </div>
{/if}

<!-- Modal: View User Details -->
{#if showViewModal && selectedUser}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-lg rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={() => showViewModal = false} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-6 flex items-center gap-2">
                <Eye class="w-5 h-5 text-blue-500" />
                User Details Profile
            </h3>
            
            <div class="space-y-4">
                <div class="flex items-center gap-4 p-4 rounded-xl {theme.darkMode ? 'bg-zinc-950' : 'bg-gray-50'}">
                    <div class="w-14 h-14 rounded-full flex items-center justify-center text-2xl font-bold text-[#ea580c] {theme.darkMode ? 'bg-orange-950/40' : 'bg-orange-100'}">
                        {selectedUser.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <h4 class="font-extrabold text-lg">{selectedUser.name}</h4>
                        <p class="text-xs font-mono {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">UUID: {selectedUser.id}</p>
                    </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-100'}">
                        <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">System Role</p>
                        <p class="font-bold text-sm mt-1">{selectedUser.role}</p>
                    </div>
                    <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-100'}">
                        <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Status</p>
                        <p class="font-bold text-sm mt-1">{selectedUser.status}</p>
                    </div>
                </div>

                <div class="p-4 rounded-xl border {theme.darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-100'}">
                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1">
                        <Mail class="w-3.5 h-3.5" />
                        Email Address
                    </p>
                    <p class="font-bold text-sm mt-1 text-[#ea580c]">{selectedUser.email}</p>
                </div>
            </div>

            <button onclick={() => showViewModal = false} class="mt-8 bg-zinc-500 hover:bg-zinc-650 text-white rounded-xl py-3 font-semibold transition-all w-full cursor-pointer border-0">
                Close Profile
            </button>
        </div>
    </div>
{/if}

<!-- Modal: Delete User Confirmation -->
{#if showDeleteModal && selectedUser}
    <div class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
        <div class="w-full max-w-md rounded-2xl border p-8 relative transition-all shadow-2xl {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-100 text-gray-800'}">
            <button onclick={() => showDeleteModal = false} class="absolute top-4 right-4 p-1 rounded-lg hover:bg-zinc-800/10 dark:hover:bg-zinc-800 transition-colors text-zinc-500 cursor-pointer border-0 bg-transparent">
                <X class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold mb-4 flex items-center gap-2 text-rose-500">
                <Trash2 class="w-5 h-5" />
                Delete User Account?
            </h3>
            
            <p class="text-sm {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">
                Are you sure you want to permanently delete the user <strong class="text-orange-500">{selectedUser.name}</strong> ({selectedUser.email})? This action is irreversible and will remove all their records from the platform.
            </p>

            <div class="flex gap-3 mt-8">
                <button onclick={() => showDeleteModal = false} class="w-1/2 rounded-xl border py-3 font-semibold transition-all cursor-pointer {theme.darkMode ? 'border-zinc-800 bg-transparent text-zinc-300 hover:bg-zinc-800/20' : 'border-gray-250 bg-transparent text-gray-700 hover:bg-gray-50'}">
                    Cancel
                </button>
                <button onclick={handleDeleteUser} class="w-1/2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl py-3 font-semibold transition-all cursor-pointer border-0">
                    Delete User
                </button>
            </div>
        </div>
    </div>
{/if}

<style>
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in {
        animation: fadeIn 0.3s ease-out forwards;
    }
</style>
