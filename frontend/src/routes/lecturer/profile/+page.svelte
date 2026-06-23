<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { Mail, Briefcase, Key, Shield, User as UserIcon } from "@lucide/svelte"

	let profile = $state({
		name: "Loading...",
		email: "loading...",
		role: "Unknown",
		id: "0"
	})

	$effect(() => {
		if (typeof window !== "undefined") {
			const data = localStorage.getItem("auth_data")
			if (data) {
				try {
					const parsed = JSON.parse(data)
					if (parsed.user) {
						profile.name = parsed.user.name || "Lecturer"
						profile.email = parsed.user.email || "lecturer@fpt.edu.vn"
						profile.role = parsed.user.role || "LECTURER"
						profile.id = parsed.user.id?.toString() || "12345"
					}
				} catch (e) {}
			}
		}
	})
</script>

<svelte:head>
	<title>Profile | SEAL</title>
</svelte:head>

<div class="p-4 md:p-8 max-w-[1000px] mx-auto w-full">
	<div class="mb-10">
		<h1
			class="text-3xl md:text-4xl font-extrabold {theme.darkMode
				? 'text-zinc-100'
				: 'text-gray-900'} tracking-tight"
		>
			My Profile
		</h1>
		<p class="text-gray-500 dark:text-zinc-400 mt-2 text-lg">
			Manage your account settings and personal information.
		</p>
	</div>

	<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
		<!-- Left Column: Avatar & Basic Info -->
		<div class="col-span-1">
			<div
				class="p-8 rounded-3xl border text-center transition-all {theme.darkMode
					? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
					: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
			>
				<div class="relative mx-auto mb-6 flex-shrink-0" style="width: 128px; height: 128px;">
					<div
						class="w-full h-full rounded-full bg-orange-500 flex items-center justify-center text-white shadow-xl shadow-orange-500/30 text-5xl font-bold"
						style="aspect-ratio: 1/1;"
					>
						{profile.name.charAt(0).toUpperCase()}
					</div>
					<button
						class="absolute bottom-0 right-0 p-2.5 rounded-full border-2 shadow-md hover:scale-110 transition-transform {theme.darkMode
							? 'bg-zinc-800 border-zinc-700 text-orange-400'
							: 'bg-white border-orange-100 text-orange-600'}"
					>
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
							></path></svg
						>
					</button>
				</div>

				<h2 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-900'} mb-1">
					{profile.name}
				</h2>
				<p class="text-orange-500 font-medium text-sm mb-4 uppercase tracking-wider">
					{profile.role}
				</p>

				<div class="flex justify-center gap-3">
					<span
						class="px-3 py-1 text-xs font-semibold rounded-full flex items-center gap-1.5 {theme.darkMode
							? 'bg-green-900/30 text-green-400'
							: 'bg-green-100 text-green-700'}"
					>
						<span class="w-2 h-2 rounded-full bg-green-500"></span> Active
					</span>
				</div>
			</div>
		</div>

		<!-- Right Column: Details & Settings -->
		<div class="col-span-1 lg:col-span-2 space-y-8">
			<!-- Personal Information -->
			<div
				class="p-8 rounded-3xl border transition-all {theme.darkMode
					? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
					: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
			>
				<div class="flex items-center justify-between mb-6">
					<h3
						class="text-xl font-bold {theme.darkMode
							? 'text-zinc-100'
							: 'text-gray-900'} flex items-center gap-2"
					>
						<UserIcon class="w-5 h-5 text-orange-500" /> Personal Information
					</h3>
					<button
						class="text-sm font-semibold hover:underline {theme.darkMode
							? 'text-orange-400'
							: 'text-orange-600'}">Edit</button
					>
				</div>

				<div class="space-y-6">
					<div>
						<label
							class="block text-xs font-semibold uppercase tracking-wider {theme.darkMode
								? 'text-zinc-500'
								: 'text-gray-400'} mb-2">Full Name</label
						>
						<div
							class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode
								? 'bg-zinc-950/50 border-zinc-800 text-zinc-300'
								: 'bg-gray-50 border-gray-200 text-gray-700'}"
						>
							<UserIcon class="w-5 h-5 opacity-50" />
							<span class="font-medium">{profile.name}</span>
						</div>
					</div>

					<div>
						<label
							class="block text-xs font-semibold uppercase tracking-wider {theme.darkMode
								? 'text-zinc-500'
								: 'text-gray-400'} mb-2">Email Address</label
						>
						<div
							class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode
								? 'bg-zinc-950/50 border-zinc-800 text-zinc-300'
								: 'bg-gray-50 border-gray-200 text-gray-700'}"
						>
							<Mail class="w-5 h-5 opacity-50" />
							<span class="font-medium">{profile.email}</span>
						</div>
					</div>

					<div>
						<label
							class="block text-xs font-semibold uppercase tracking-wider {theme.darkMode
								? 'text-zinc-500'
								: 'text-gray-400'} mb-2">Role & Department</label
						>
						<div
							class="flex items-center gap-3 p-3 rounded-xl border {theme.darkMode
								? 'bg-zinc-950/50 border-zinc-800 text-zinc-300'
								: 'bg-gray-50 border-gray-200 text-gray-700'}"
						>
							<Briefcase class="w-5 h-5 opacity-50" />
							<span class="font-medium">{profile.role} (Software Engineering)</span>
						</div>
					</div>
				</div>
			</div>

			<!-- Security & Preferences -->
			<div
				class="p-8 rounded-3xl border transition-all {theme.darkMode
					? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
					: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
			>
				<h3
					class="text-xl font-bold {theme.darkMode
						? 'text-zinc-100'
						: 'text-gray-900'} flex items-center gap-2 mb-6"
				>
					<Shield class="w-5 h-5 text-orange-500" /> Security
				</h3>

				<div
					class="flex items-center justify-between p-4 rounded-xl border {theme.darkMode
						? 'border-zinc-800 bg-zinc-950/30'
						: 'border-gray-100 bg-gray-50'}"
				>
					<div class="flex items-center gap-4">
						<div
							class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 {theme.darkMode
								? 'bg-zinc-800 text-orange-400'
								: 'bg-orange-100 text-orange-600'}"
						>
							<Key class="w-5 h-5" />
						</div>
						<div>
							<p class="font-bold {theme.darkMode ? 'text-zinc-200' : 'text-gray-800'}">
								Change Password
							</p>
							<p class="text-xs {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'} mt-0.5">
								Ensure your account uses a strong, unique password.
							</p>
						</div>
					</div>
					<button
						class="px-4 py-2 text-sm font-semibold rounded-lg bg-orange-600 text-white hover:bg-orange-700 transition-colors shrink-0"
						>Update</button
					>
				</div>
			</div>
		</div>
	</div>
</div>
