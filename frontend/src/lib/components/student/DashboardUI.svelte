<script lang="ts">
	import { theme } from "$lib/theme.svelte"

	let { profile, seasons = [] } = $props<{ profile: any; seasons: any[] }>()
</script>

<!-- Student Dashboard metrics -->
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-10">
	<div
		class="rounded-2xl p-6 border transition-all {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)]'} flex items-center gap-5"
	>
		<div
			class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
				? 'bg-orange-950/40 text-orange-400'
				: 'bg-[#ffedd5] text-[#ea580c]'}"
		>
			<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
				><path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
				></path></svg
			>
		</div>
		<div>
			<p
				class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
					? 'text-zinc-500'
					: 'text-gray-400'}"
			>
				Current Season
			</p>
			<h3 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				{seasons.length > 0 ? seasons[0].name : "Spring 2026"}
			</h3>
		</div>
	</div>

	<div
		class="rounded-2xl p-6 border transition-all {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)]'} flex items-center gap-5"
	>
		<div
			class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
				? 'bg-green-950/40 text-green-400'
				: 'bg-green-50 text-green-600'}"
		>
			<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
				><path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9"
				></path></svg
			>
		</div>
		<div>
			<p
				class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
					? 'text-zinc-500'
					: 'text-gray-400'}"
			>
				Registered Tracks
			</p>
			<h3 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">2</h3>
		</div>
	</div>

	<div
		class="rounded-2xl p-6 border transition-all {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)]'} flex items-center gap-5"
	>
		<div
			class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
				? 'bg-blue-950/40 text-blue-400'
				: 'bg-blue-50 text-blue-600'}"
		>
			<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
				><path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
				></path></svg
			>
		</div>
		<div>
			<p
				class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
					? 'text-zinc-500'
					: 'text-gray-400'}"
			>
				Team Status
			</p>
			<h3 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Looking for team
			</h3>
		</div>
	</div>

	<div
		class="rounded-2xl p-6 border transition-all {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)]'} flex items-center gap-5"
	>
		<div
			class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
				? 'bg-purple-950/40 text-purple-400'
				: 'bg-purple-50 text-purple-600'}"
		>
			<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
				><path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
				></path></svg
			>
		</div>
		<div>
			<p
				class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
					? 'text-zinc-500'
					: 'text-gray-400'}"
			>
				Submission Due
			</p>
			<h3 class="text-lg font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				In 5 days
			</h3>
		</div>
	</div>
</div>

<!-- Dashboard Actions -->
<div class="pt-4 border-t-2 border-dashed {theme.darkMode ? 'border-zinc-800' : 'border-gray-200'}">
	<h2 class="text-2xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
		Dashboard Actions
	</h2>
	<div class="grid grid-cols-1 md:grid-cols-2 gap-8">
		<a
			href="/student/create-team"
			class="group block p-8 rounded-2xl shadow-sm border hover:shadow-md hover:-translate-y-1 transition-all cursor-pointer {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 hover:border-orange-900'
				: 'bg-white/80 border-gray-100 hover:border-orange-300'}"
		>
			<div class="flex items-center gap-4 mb-4">
				<div
					class="p-3 rounded-xl transition-transform group-hover:scale-110 {theme.darkMode
						? 'bg-orange-950/40 text-orange-400'
						: 'bg-orange-100 text-orange-600'}"
				>
					<svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
						></path></svg
					>
				</div>
				<h2
					class="text-xl font-bold transition-colors {theme.darkMode
						? 'text-zinc-100 group-hover:text-orange-400'
						: 'text-gray-800 group-hover:text-orange-600'}"
				>
					Create a Team
				</h2>
			</div>
			<p class="mb-6 text-sm md:text-base {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Form a new team, select a hackathon season, and invite members to collaborate on your
				innovative idea.
			</p>
			<div
				class="inline-flex items-center gap-2 font-semibold group-hover:gap-3 transition-all {theme.darkMode
					? 'text-orange-400'
					: 'text-orange-600'}"
			>
				Go to Create Team
				<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M14 5l7 7m0 0l-7 7m7-7H3"
					></path></svg
				>
			</div>
		</a>

		<a
			href="/student/submit-project"
			class="group block p-8 rounded-2xl shadow-sm border hover:shadow-md hover:-translate-y-1 transition-all cursor-pointer {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 hover:border-blue-900'
				: 'bg-white/80 border-gray-100 hover:border-blue-300'}"
		>
			<div class="flex items-center gap-4 mb-4">
				<div
					class="p-3 rounded-xl transition-transform group-hover:scale-110 {theme.darkMode
						? 'bg-blue-950/40 text-blue-400'
						: 'bg-blue-100 text-blue-600'}"
				>
					<svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
						></path></svg
					>
				</div>
				<h2
					class="text-xl font-bold transition-colors {theme.darkMode
						? 'text-zinc-100 group-hover:text-blue-400'
						: 'text-gray-800 group-hover:text-blue-600'}"
				>
					Submit Project
				</h2>
			</div>
			<p class="mb-6 text-sm md:text-base {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Ready for judging? Submit your project repository, video pitch, and presentation materials
				here.
			</p>
			<div
				class="inline-flex items-center gap-2 font-semibold group-hover:gap-3 transition-all {theme.darkMode
					? 'text-blue-400'
					: 'text-blue-600'}"
			>
				Go to Submission
				<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M14 5l7 7m0 0l-7 7m7-7H3"
					></path></svg
				>
			</div>
		</a>
	</div>
</div>
