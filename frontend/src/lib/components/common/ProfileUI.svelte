<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { User, Mail, Award, BookOpen, Edit2, Check, X } from "@lucide/svelte"
	import { updateProfile } from "$lib/api/profile"

	let { profile = $bindable() } = $props<{ profile: any }>()

	let isEditing = $state(false)
	let isSaving = $state(false)
	let editName = $state("")
	let editStrengths = $state("")
	let editAboutMe = $state("")
	let saveError = $state("")

	// Sync local storage modifications to profile
	$effect(() => {
		if (profile && profile.id) {
			const cached = localStorage.getItem(`profile_mock_${profile.id}`)
			if (cached) {
				try {
					const parsed = JSON.parse(cached)
					if (
						profile.name !== parsed.name ||
						profile.strengths !== parsed.strengths ||
						profile.about_me !== parsed.about_me
					) {
						profile = {
							...profile,
							name: parsed.name ?? profile.name,
							strengths: parsed.strengths ?? profile.strengths,
							about_me: parsed.about_me ?? profile.about_me
						}
					}
				} catch (e) {
					console.error("Error parsing cached profile", e)
				}
			}
		}
	})

	function startEditing() {
		editName = profile.name || ""
		editStrengths = profile.strengths || ""
		editAboutMe = profile.about_me || ""
		saveError = ""
		isEditing = true
	}

	function cancelEditing() {
		isEditing = false
		saveError = ""
	}

	async function saveProfile() {
		if (!editName.trim()) {
			saveError = "Name cannot be empty"
			return
		}
		isSaving = true
		saveError = ""

		const localData = {
			...profile,
			name: editName,
			strengths: editStrengths,
			about_me: editAboutMe
		}

		try {
			const res = await updateProfile({
				name: editName,
				strengths: editStrengths,
				about_me: editAboutMe
			})
			if (res.ok) {
				const updatedProfile = await res.json()
				localStorage.setItem(`profile_mock_${profile.id}`, JSON.stringify(localData))
				profile = {
					...updatedProfile,
					strengths: editStrengths,
					about_me: editAboutMe
				}
				isEditing = false
			} else {
				console.warn("Backend update failed, saving locally.")
				localStorage.setItem(`profile_mock_${profile.id}`, JSON.stringify(localData))
				profile = localData
				isEditing = false
			}
		} catch (error) {
			console.warn("Backend update error, saving locally:", error)
			localStorage.setItem(`profile_mock_${profile.id}`, JSON.stringify(localData))
			profile = localData
			isEditing = false
		} finally {
			isSaving = false
		}
	}
</script>

<div class="max-w-4xl mx-auto w-full p-4">
	{#if profile.role === "STUDENT"}
		<a
			href="/student"
			class="inline-flex items-center gap-2 transition-colors mb-6 font-medium
			{theme.darkMode
				? 'text-zinc-400 hover:text-orange-400'
				: 'text-gray-500 hover:text-orange-600'}"
		>
			<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M10 19l-7-7m0 0l7-7m-7 7h18"
				></path>
			</svg>
			Back to Dashboard
		</a>
	{/if}

	<div
		class="w-full rounded-2xl border p-8 relative transition-all shadow-xl {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 text-zinc-100'
			: 'bg-white border-gray-100 text-gray-800'}"
	>
		<div class="flex justify-between items-center mb-6">
			<h3 class="text-xl font-bold flex items-center gap-2">
				<User class="w-5 h-5 text-[#ea580c]" />
				My Profile
			</h3>
			
			{#if profile.role === "STUDENT" || profile.role === "MENTOR" || profile.role === "LECTURER" || profile.role === "JUDGE"}
				<div class="flex gap-2">
					{#if isEditing}
						<button
							onclick={saveProfile}
							disabled={isSaving}
							class="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-lg bg-orange-600 text-white hover:bg-orange-500 disabled:opacity-50 transition-all border-0 cursor-pointer shadow-md"
						>
							<Check class="w-3.5 h-3.5" />
							{isSaving ? "Saving..." : "Save"}
						</button>
						<button
							onclick={cancelEditing}
							disabled={isSaving}
							class="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-lg transition-all border cursor-pointer shadow-sm
							{theme.darkMode
								? 'bg-zinc-800 border-zinc-700 text-zinc-300 hover:bg-zinc-700'
								: 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}"
						>
							<X class="w-3.5 h-3.5" />
							Cancel
						</button>
					{:else}
						<button
							onclick={startEditing}
							class="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-lg transition-all border cursor-pointer shadow-md
							{theme.darkMode
								? 'bg-zinc-800 border-zinc-700 text-zinc-200 hover:bg-zinc-700 hover:text-orange-400'
								: 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50 hover:text-orange-600'}"
						>
							<Edit2 class="w-3.5 h-3.5" />
							Edit Profile
						</button>
					{/if}
				</div>
			{/if}
		</div>

		{#if saveError}
			<div class="mb-4 bg-red-500/10 border border-red-500/20 text-red-500 text-xs p-3 rounded-lg flex items-center gap-2">
				<span class="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse"></span>
				{saveError}
			</div>
		{/if}

		<div class="space-y-4">
			<div
				class="flex items-center gap-4 p-4 rounded-xl {theme.darkMode
					? 'bg-zinc-950'
					: 'bg-gray-50'}"
			>
				<div
					class="w-16 h-16 rounded-full flex items-center justify-center text-3xl font-bold text-[#ea580c] shrink-0 {theme.darkMode
						? 'bg-orange-950/40'
						: 'bg-orange-100'}"
				>
					{editName ? editName.charAt(0).toUpperCase() : (profile.name ? profile.name.charAt(0).toUpperCase() : "U")}
				</div>
				<div class="flex-1 min-w-0">
					{#if isEditing}
						<label class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Full Name</label>
						<input
							type="text"
							bind:value={editName}
							class="w-full px-3 py-2 text-sm rounded-lg border focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-all
							{theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100' : 'bg-white border-gray-200 text-gray-800'}"
							placeholder="Enter your name"
						/>
					{:else}
						<h4 class="font-extrabold text-xl">{profile.name || "Unknown User"}</h4>
						<p class="text-sm font-mono mt-1 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
							UUID: {profile.id}
						</p>
					{/if}
				</div>
			</div>

			<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">System Role</p>
					<p class="font-bold text-sm mt-1 text-blue-500">{profile.role}</p>
				</div>
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Status</p>
					<p class="font-bold text-sm mt-1 text-green-500">ACTIVE</p>
				</div>
			</div>

			<div
				class="p-4 rounded-xl border {theme.darkMode
					? 'bg-zinc-900 border-zinc-800'
					: 'bg-white border-gray-100'}"
			>
				<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1">
					<Mail class="w-3.5 h-3.5" />
					Email Address
				</p>
				<p class="font-bold text-sm mt-1 text-[#ea580c]">{profile.email}</p>
			</div>

			<!-- Strengths Section -->
			{#if profile.role === "STUDENT" || profile.role === "MENTOR" || profile.role === "LECTURER" || profile.role === "JUDGE"}
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1 mb-2">
						<Award class="w-3.5 h-3.5 text-orange-500" />
						Strengths / Specializations
					</p>
					
					{#if isEditing}
						<input
							type="text"
							bind:value={editStrengths}
							class="w-full px-3 py-2 text-sm rounded-lg border focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-all
							{theme.darkMode ? 'bg-zinc-950 border-zinc-800 text-zinc-100' : 'bg-white border-gray-200 text-gray-800'}"
							placeholder="e.g. frontend, backend, UI/UX, analyst, DevOps"
						/>
					{:else}
						{#if profile.strengths}
							<div class="flex flex-wrap gap-2 mt-1">
								{#each profile.strengths.split(',').map((s: string) => s.trim()).filter(Boolean) as strength}
									<span class="px-2.5 py-1 text-xs font-semibold rounded-full bg-orange-500/10 text-orange-500 border border-orange-500/20">
										{strength}
									</span>
								{/each}
							</div>
						{:else}
							<p class="text-sm mt-1 italic {theme.darkMode ? 'text-zinc-550' : 'text-gray-400'}">
								No strengths or specializations added yet.
							</p>
						{/if}
					{/if}
				</div>

				<!-- About Me Section -->
				<div
					class="p-4 rounded-xl border {theme.darkMode
						? 'bg-zinc-900 border-zinc-800'
						: 'bg-white border-gray-100'}"
				>
					<p class="text-xs text-gray-400 font-semibold uppercase tracking-wider flex items-center gap-1 mb-2">
						<BookOpen class="w-3.5 h-3.5 text-orange-500" />
						About Me
					</p>
					
					{#if isEditing}
						<textarea
							bind:value={editAboutMe}
							rows="4"
							class="w-full px-3 py-2 text-sm rounded-lg border focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-all resize-y
							{theme.darkMode ? 'bg-zinc-950 border-zinc-800 text-zinc-100' : 'bg-white border-gray-200 text-gray-800'}"
							placeholder="Write a short bio about yourself..."
						></textarea>
					{:else}
						{#if profile.about_me}
							<p class="text-sm mt-1 whitespace-pre-wrap leading-relaxed {theme.darkMode ? 'text-zinc-300' : 'text-gray-650'}">
								{profile.about_me}
							</p>
						{:else}
							<p class="text-sm mt-1 italic {theme.darkMode ? 'text-zinc-550' : 'text-gray-400'}">
								Write something about yourself to complete your profile!
							</p>
						{/if}
					{/if}
				</div>
			{/if}
			
			<!-- ROLE BASED LOGIC UI HERE -->
			{#if profile.role === "STUDENT"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Student Information</h4>
					<div class="p-6 rounded-xl border border-dashed {theme.darkMode ? 'bg-zinc-950/50 border-zinc-700' : 'bg-gray-50 border-gray-300'}">
						<p class="text-center text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
							No specific student data available yet. Please join a team or register for an event.
						</p>
					</div>
				</div>
			{:else if profile.role === "COORDINATOR"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Coordinator Tools</h4>
					<div class="grid grid-cols-2 gap-4">
						<button class="p-3 text-sm font-semibold rounded-lg bg-orange-500 text-white hover:bg-orange-600 transition-colors cursor-pointer border-0">
							Manage Users
						</button>
						<button class="p-3 text-sm font-semibold rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition-colors cursor-pointer border-0">
							System Settings
						</button>
					</div>
				</div>
			{:else if profile.role === "JUDGE" || profile.role === "MENTOR" || profile.role === "LECTURER"}
				<div class="mt-6 border-t {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'} pt-6">
					<h4 class="font-bold text-lg mb-4">Expert Information</h4>
					<div class="p-6 rounded-xl border border-dashed {theme.darkMode ? 'bg-zinc-950/50 border-zinc-700' : 'bg-gray-50 border-gray-300'}">
						<p class="text-center text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
							You are currently assigned as an expert. Awaiting track assignments.
						</p>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
