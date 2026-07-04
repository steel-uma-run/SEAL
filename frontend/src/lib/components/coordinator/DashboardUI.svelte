<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import {
		Calendar,
		Users,
		UserCheck,
		Shield,
		ChevronDown,
		Check,
		AlertTriangle,
		Plus,
		Megaphone,
		Download,
		Zap
	} from "@lucide/svelte"

	let { profile, seasonsCount = 0 } = $props<{ profile: any; seasonsCount?: number }>()
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- 1. HEADER SECTION -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 {theme.darkMode
			? 'border-zinc-800'
			: 'border-gray-100'}"
	>
		<div>
			<div class="flex flex-col sm:flex-row sm:items-center gap-3">
				<h1
					class="text-2xl md:text-3xl font-extrabold tracking-tight {theme.darkMode
						? 'text-zinc-100'
						: 'text-gray-800'}"
				>
					Coordinator Dashboard
				</h1>
				<!-- Global Season Selector Dropdown -->
				<div class="relative mt-1 sm:mt-0">
					<button
						class="flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold transition-all border shadow-sm {theme.darkMode
							? 'bg-zinc-800/80 border-zinc-700 text-zinc-200 hover:bg-zinc-700'
							: 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'}"
					>
						<span>Season 4: Summer Hackathon 2026</span>
						<ChevronDown class="w-3.5 h-3.5 text-[#ea580c]" />
					</button>
				</div>
			</div>
			<p class="mt-2 text-sm md:text-base {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
				Welcome back, {profile?.name || "Coordinator"}! Here's an overview of the hackathon seasons.
			</p>
		</div>

		<div class="flex items-center gap-4 mt-4 md:mt-0">
			<div class="text-right hidden sm:block">
				<p class="font-bold leading-tight {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					{profile?.name || "Nguyễn Hùng Cường"}
				</p>
				<p class="text-xs font-semibold text-[#ea580c] uppercase tracking-wider">COORDINATOR</p>
			</div>
			<div
				class="w-12 h-12 rounded-full flex items-center justify-center text-[#ea580c] font-bold text-xl shadow-sm {theme.darkMode
					? 'bg-orange-950/40 border border-orange-900/50'
					: 'bg-[#ffedd5] border border-[#fed7aa]'}"
			>
				{profile?.name?.charAt(0).toUpperCase() || "N"}
			</div>
		</div>
	</header>

	<!-- 2. METRIC CARDS (With Actionable Sub-texts) -->
	<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
		<!-- Card 1: Total Seasons -->
		<div
			class="p-6 rounded-2xl border flex flex-col justify-between transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
						? 'bg-orange-950/40 text-orange-400'
						: 'bg-orange-100 text-orange-600'}"
				>
					<Calendar class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
							? 'text-zinc-500'
							: 'text-gray-400'}"
					>
						Total Seasons
					</p>
					<h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">{seasonsCount}</h3>
				</div>
			</div>
			<p class="text-xs font-medium mt-4 {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
				(All active seasons)
			</p>
		</div>

		<!-- Card 2: Total Teams -->
		<div
			class="p-6 rounded-2xl border flex flex-col justify-between transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
						? 'bg-orange-950/40 text-orange-400'
						: 'bg-orange-100 text-orange-600'}"
				>
					<Users class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
							? 'text-zinc-500'
							: 'text-gray-400'}"
					>
						Total Teams
					</p>
					<h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
						32
					</h3>
				</div>
			</div>
			<div class="mt-4">
				<span
					class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-orange-100 text-orange-700 {theme.darkMode
						? 'bg-orange-950/60 text-orange-400 border border-orange-900/50'
						: ''}"
				>
					4 teams pending approval
				</span>
			</div>
		</div>

		<!-- Card 3: Active Participants -->
		<div
			class="p-6 rounded-2xl border flex flex-col justify-between transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
						? 'bg-blue-950/40 text-blue-400'
						: 'bg-blue-50 text-blue-600'}"
				>
					<UserCheck class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
							? 'text-zinc-500'
							: 'text-gray-400'}"
					>
						Active Participants
					</p>
					<h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
						156
					</h3>
				</div>
			</div>
			<p class="text-xs font-semibold mt-4 {theme.darkMode ? 'text-blue-400' : 'text-blue-600'}">
				+12 new registrations this week
			</p>
		</div>

		<!-- Card 4: Total Mentors/Judges -->
		<div
			class="p-6 rounded-2xl border flex flex-col justify-between transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.02)]'}"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 {theme.darkMode
						? 'bg-green-950/40 text-green-400'
						: 'bg-green-50 text-green-600'}"
				>
					<Shield class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 {theme.darkMode
							? 'text-zinc-500'
							: 'text-gray-400'}"
					>
						Total Mentors/Judges
					</p>
					<h3 class="text-2xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
						28
					</h3>
				</div>
			</div>
			<div class="mt-4">
				<span
					class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-red-100 text-red-700 {theme.darkMode
						? 'bg-red-950/60 text-red-400 border border-red-900/50'
						: ''}"
				>
					2 judges unassigned to rooms
				</span>
			</div>
		</div>
	</div>

	<!-- 3. ROW 2: TIMELINE (2/3) & QUICK ACTIONS (1/3) -->
	<div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
		<!-- Hackathon Timeline (2 cols) -->
		<div
			class="lg:col-span-2 p-8 rounded-3xl border transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<h2 class="text-xl font-bold mb-8 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Hackathon Timeline
			</h2>

			<div
				class="space-y-8 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent {theme.darkMode
					? 'before:via-zinc-800'
					: 'before:via-gray-200'} before:to-transparent"
			>
				<!-- Timeline Point 1: Done -->
				<div class="relative flex items-center gap-4">
					<div
						class="w-10 h-10 rounded-full border-2 border-green-500 bg-green-50 text-green-600 flex items-center justify-center shrink-0 z-10 shadow-sm {theme.darkMode
							? 'bg-green-950/50 border-green-600 text-green-400'
							: ''}"
					>
						<Check class="w-5 h-5 stroke-[2.5]" />
					</div>
					<div
						class="flex items-center justify-between flex-1 bg-gray-50/50 p-3 rounded-2xl border {theme.darkMode
							? 'bg-zinc-800/40 border-zinc-800'
							: 'border-gray-100'}"
					>
						<div>
							<h4
								class="font-bold text-sm line-through {theme.darkMode
									? 'text-zinc-400'
									: 'text-gray-500'}"
							>
								Team Formation Deadline
							</h4>
							<p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
								June 10, 2026
							</p>
						</div>
						<span
							class="px-2.5 py-1 rounded-lg text-xs font-bold bg-green-100 text-green-700 uppercase tracking-wider {theme.darkMode
								? 'bg-green-950 text-green-400'
								: ''}">Done ✓</span
						>
					</div>
				</div>

				<!-- Timeline Point 2: ACTIVE (With Glowing ring effect) -->
				<div class="relative flex items-center gap-4">
					<div
						class="w-10 h-10 rounded-full border-2 border-[#ea580c] bg-orange-50 text-[#ea580c] ring-4 ring-orange-500/20 flex items-center justify-center shrink-0 z-10 shadow-md {theme.darkMode
							? 'bg-orange-950 border-orange-500 text-orange-400 ring-orange-500/30'
							: ''}"
					>
						<Calendar class="w-4 h-4" />
					</div>
					<div
						class="flex items-center justify-between flex-1 bg-orange-50/40 p-3.5 rounded-2xl border-2 border-orange-200/80 shadow-sm {theme.darkMode
							? 'bg-orange-950/20 border-orange-900/60'
							: ''}"
					>
						<div>
							<h4
								class="font-bold text-sm text-[#ea580c] {theme.darkMode
									? 'text-orange-400 font-extrabold'
									: ''}"
							>
								Project Submission
							</h4>
							<p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-400' : 'text-gray-600'}">
								June 13, 2026 - 11:59 PM
							</p>
						</div>
						<span
							class="px-3 py-1 rounded-lg text-xs font-black bg-[#ea580c] text-white uppercase tracking-wider shadow-sm animate-pulse"
							>Current Phase</span
						>
					</div>
				</div>

				<!-- Timeline Point 3: Future -->
				<div class="relative flex items-center gap-4 opacity-50">
					<div
						class="w-10 h-10 rounded-full border-2 flex items-center justify-center shrink-0 z-10 {theme.darkMode
							? 'bg-zinc-900 border-zinc-700 text-zinc-600'
							: 'bg-white border-gray-300 text-gray-300'}"
					>
						<Users class="w-4 h-4" />
					</div>
					<div
						class="flex items-center justify-between flex-1 p-3 rounded-2xl border {theme.darkMode
							? 'border-zinc-800/80'
							: 'border-gray-100'}"
					>
						<div>
							<h4 class="font-bold text-sm {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
								Pitching & Judging
							</h4>
							<p class="text-xs mt-0.5 {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}">
								June 15, 2026
							</p>
						</div>
						<span class="text-xs font-semibold {theme.darkMode ? 'text-zinc-600' : 'text-gray-400'}"
							>Upcoming</span
						>
					</div>
				</div>
			</div>
		</div>

		<!-- Quick Actions (1 col - 2x2 Grid) -->
		<div
			class="lg:col-span-1 p-8 rounded-3xl border flex flex-col justify-between transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<div>
				<h2 class="text-xl font-bold mb-6 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					Quick Actions
				</h2>
				<p class="text-xs mb-6 {theme.darkMode ? 'text-zinc-400' : 'text-gray-500'}">
					Frequently used administrative tools for this season.
				</p>
			</div>

			<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-3.5 mt-auto">
				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all shadow-sm bg-[#ea580c] hover:bg-[#d04e0a] text-white"
				>
					<Plus class="w-4 h-4 stroke-[2.5]" />
					<span>+ New Season</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all shadow-sm bg-blue-600 hover:bg-blue-700 text-white"
				>
					<Megaphone class="w-4 h-4" />
					<span>Global Notice</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-semibold text-sm flex items-center justify-center gap-2.5 transition-all border {theme.darkMode
						? 'bg-zinc-800/60 border-zinc-700 text-zinc-200 hover:bg-zinc-800'
						: 'bg-gray-50 border-gray-200 text-gray-700 hover:bg-gray-100'}"
				>
					<Download class="w-4 h-4" />
					<span>Export Teams</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all shadow-sm bg-purple-600 hover:bg-purple-700 text-white"
				>
					<Zap class="w-4 h-4" />
					<span>Generate Judges</span>
				</button>
			</div>
		</div>
	</div>

	<!-- 4. ROW 3: ATTENTION REQUIRED (System Alerts) -->
	<div
		class="p-8 rounded-3xl border mb-8 transition-all {theme.darkMode
			? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
			: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
	>
		<div class="flex items-center gap-3 mb-6">
			<AlertTriangle class="w-6 h-6 text-amber-500 animate-bounce" />
			<h2 class="text-xl font-bold {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Attention Required <span
					class="text-sm font-normal {theme.darkMode ? 'text-zinc-500' : 'text-gray-400'}"
					>(System Alerts)</span
				>
			</h2>
		</div>

		<div class="space-y-3">
			<!-- Alert 1 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border {theme.darkMode
					? 'bg-amber-950/10 border-amber-900/30 text-zinc-200'
					: 'bg-amber-50/50 border-amber-100 text-gray-700'}"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-amber-500 font-bold">⚠️</span>
					<p class="text-sm">
						Team <strong class={theme.darkMode ? "text-amber-400" : "text-amber-800"}
							>"FPT_NinhKieu"</strong
						> has 0 members registered in the system.
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border shadow-sm {theme.darkMode
						? 'bg-zinc-800 border-zinc-700 hover:bg-zinc-700 text-zinc-100'
						: 'bg-white border-gray-200 hover:bg-gray-50 text-gray-800'}"
				>
					[ View Team ]
				</button>
			</div>

			<!-- Alert 2 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border {theme.darkMode
					? 'bg-amber-950/10 border-amber-900/30 text-zinc-200'
					: 'bg-amber-50/50 border-amber-100 text-gray-700'}"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-amber-500 font-bold">⚠️</span>
					<p class="text-sm">
						Project Submission deadline in <strong>6 hours</strong> (14 teams missing link).
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border shadow-sm {theme.darkMode
						? 'bg-zinc-800 border-zinc-700 hover:bg-zinc-700 text-zinc-100'
						: 'bg-white border-gray-200 hover:bg-gray-50 text-gray-800'}"
				>
					[ Send Reminders ]
				</button>
			</div>

			<!-- Alert 3 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border {theme.darkMode
					? 'bg-amber-950/10 border-amber-900/30 text-zinc-200'
					: 'bg-amber-50/50 border-amber-100 text-gray-700'}"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-amber-500 font-bold">⚠️</span>
					<p class="text-sm">
						Judge <strong>Nguyễn Văn A</strong> rejected the invitation to join Season 4.
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border shadow-sm {theme.darkMode
						? 'bg-zinc-800 border-zinc-700 hover:bg-zinc-700 text-zinc-100'
						: 'bg-white border-gray-200 hover:bg-gray-50 text-gray-800'}"
				>
					[ Re-assign Judge ]
				</button>
			</div>
		</div>
	</div>

	<!-- 5. ROW 4: BOTTOM TRACKERS -->
	<div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
		<!-- Live Tracker -->
		<div
			class="p-8 rounded-3xl border transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<div class="flex items-center justify-between mb-4">
				<h3 class="font-bold text-lg {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
					Live Submission Tracker
				</h3>
				<span class="text-sm font-extrabold text-[#ea580c]">18/32 teams submitted</span>
			</div>

			<div
				class="w-full h-3.5 bg-gray-100 rounded-full overflow-hidden p-0.5 border {theme.darkMode
					? 'bg-zinc-800 border-zinc-700'
					: 'border-gray-200'} mb-3"
			>
				<div
					class="bg-gradient-to-r from-orange-500 to-amber-500 h-full rounded-full transition-all duration-500"
					style="width: 56%;"
				></div>
			</div>

			<div
				class="flex items-center justify-between text-xs {theme.darkMode
					? 'text-zinc-400'
					: 'text-gray-500'} font-medium"
			>
				<span>14 teams missing slide deck</span>
				<span
					class="flex items-center gap-1 font-bold {theme.darkMode
						? 'text-amber-400'
						: 'text-amber-700'}"
				>
					<span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-ping"></span>
					4 hours remaining
				</span>
			</div>
		</div>

		<!-- Upcoming Assignments -->
		<div
			class="p-8 rounded-3xl border transition-all {theme.darkMode
				? 'bg-zinc-900 border-zinc-800 shadow-[0_4px_30px_rgba(0,0,0,0.2)]'
				: 'bg-white border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}"
		>
			<h3 class="font-bold text-lg mb-4 {theme.darkMode ? 'text-zinc-100' : 'text-gray-800'}">
				Upcoming Mentor Assignments
			</h3>

			<div class="space-y-2.5 text-sm {theme.darkMode ? 'text-zinc-300' : 'text-gray-600'}">
				<div
					class="flex items-center justify-between p-3 rounded-xl bg-gray-50/80 {theme.darkMode
						? 'bg-zinc-800/50'
						: ''}"
				>
					<span>Pending Room 104 Assignment</span>
					<strong class={theme.darkMode ? "text-orange-400" : "text-orange-600"}>Team Alpha</strong>
				</div>
				<div
					class="flex items-center justify-between p-3 rounded-xl bg-gray-50/80 {theme.darkMode
						? 'bg-zinc-800/50'
						: ''}"
				>
					<span>Pending Room 105 Assignment</span>
					<strong class={theme.darkMode ? "text-blue-400" : "text-blue-600"}>Team Cyber_Bot</strong>
				</div>
			</div>
		</div>
	</div>
</div>
