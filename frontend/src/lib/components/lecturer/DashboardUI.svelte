<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	let profile = $state({ name: "Lecturer" })

	$effect(() => {
		if (typeof window !== "undefined") {
			const data = localStorage.getItem("auth_data")
			if (data) {
				try {
					const parsed = JSON.parse(data)
					if (parsed.user && parsed.user.name) {
						profile.name = parsed.user.name
					}
				} catch (e) {}
			}
		}
	})
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<div class="mb-10">
		<h1
			class="text-4xl font-extrabold {theme.darkMode
				? 'text-zinc-100'
				: 'text-gray-900'} tracking-tight"
		>
			Lecturer Dashboard
		</h1>
		<p class="text-gray-500 dark:text-zinc-400 mt-2 text-lg">
			Welcome back, <span
				class="font-semibold {theme.darkMode ? 'text-orange-400' : 'text-orange-600'}"
				>{profile.name}</span
			>
		</p>
	</div>

	<div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-10">
		<!-- Mentoring Card -->
		<a
			href="/lecturer/mentoring"
			class="block group p-8 rounded-3xl border transition-all duration-300 relative overflow-hidden {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<div class="relative z-10">
				<div
					class="w-14 h-14 rounded-2xl border-2 border-orange-500 text-orange-500 {theme.darkMode
						? 'bg-zinc-900'
						: 'bg-white'} flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300 shadow-sm"
				>
					<svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
						></path></svg
					>
				</div>

				<h2 class="text-2xl font-bold text-orange-500 mb-3">Mentoring Teams</h2>
				<p class="text-gray-500 dark:text-zinc-400 leading-relaxed max-w-sm">
					View details of the teams you are currently mentoring. You cannot score these teams to
					prevent bias.
				</p>

				<div
					class="mt-6 flex items-center text-orange-600 dark:text-orange-400 font-semibold text-sm"
				>
					View my teams
					<svg
						class="w-4 h-4 ml-1.5 group-hover:translate-x-1.5 transition-transform"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M17 8l4 4m0 0l-4 4m4-4H3"
						></path></svg
					>
				</div>
			</div>
		</a>

		<!-- Judging Card -->
		<a
			href="/lecturer/judging"
			class="block group p-8 rounded-3xl border transition-all duration-300 relative overflow-hidden {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<div class="relative z-10">
				<div
					class="w-14 h-14 rounded-2xl border-2 border-orange-500 text-orange-500 {theme.darkMode
						? 'bg-zinc-900'
						: 'bg-white'} flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300 shadow-sm"
				>
					<svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
						></path></svg
					>
				</div>

				<h2 class="text-2xl font-bold text-orange-500 mb-3">Judging Submissions</h2>
				<p class="text-gray-500 dark:text-zinc-400 leading-relaxed max-w-sm">
					Grade assigned student projects, complete calibration processes, and provide mandatory
					feedback.
				</p>

				<div
					class="mt-6 flex items-center text-orange-600 dark:text-orange-400 font-semibold text-sm"
				>
					Start grading
					<svg
						class="w-4 h-4 ml-1.5 group-hover:translate-x-1.5 transition-transform"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M17 8l4 4m0 0l-4 4m4-4H3"
						></path></svg
					>
				</div>
			</div>
		</a>
	</div>
</div>
