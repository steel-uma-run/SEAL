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

	let {
		profile,
		seasonsCount = 0,
		activeParticipantsCount = 0,
		totalLecturersCount = 0
	} = $props<{
		profile: any
		seasonsCount?: number
		activeParticipantsCount?: number
		totalLecturersCount?: number
	}>()
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- 1. HEADER SECTION -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<div class="flex flex-col sm:flex-row sm:items-center gap-3">
				<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
					Coordinator Dashboard
				</h1>
				<!-- Global Season Selector Dropdown -->
				<div class="relative mt-1 sm:mt-0">
					<button
						class="flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold transition-all border border-(--md-outline-variant) bg-(--md-surface-container-high) text-(--md-on-surface) hover:bg-(--md-surface-container-highest) cursor-pointer"
					>
						<span>Season 4: Summer Hackathon 2026</span>
						<ChevronDown class="w-3.5 h-3.5 text-(--md-primary)" />
					</button>
				</div>
			</div>
			<p class="mt-2 text-sm md:text-base text-(--md-on-surface-variant)">
				Welcome back, {profile?.name || "Coordinator"}! Here's an overview of the hackathon seasons.
			</p>
		</div>

		<div class="flex items-center gap-4 mt-4 md:mt-0">
			<div class="text-right hidden sm:block">
				<p class="font-bold leading-tight text-(--md-on-surface)">
					{profile?.name || "Nguyễn Hùng Cường"}
				</p>
				<p class="text-xs font-semibold text-(--md-primary) uppercase tracking-wider">
					COORDINATOR
				</p>
			</div>
			<div
				class="w-12 h-12 rounded-full flex items-center justify-center text-(--md-on-primary-container) font-bold text-xl bg-(--md-primary-container) border border-(--md-outline-variant)"
			>
				{profile?.name?.charAt(0).toUpperCase() || "N"}
			</div>
		</div>
	</header>

	<!-- 2. METRIC CARDS (With Actionable Sub-texts) -->
	<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
		<!-- Card 1: Total Seasons -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container) flex flex-col justify-between transition-colors duration-300"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-primary-container) text-(--md-on-primary-container)"
				>
					<Calendar class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
					>
						Total Seasons
					</p>
					<h3 class="text-2xl font-bold text-(--md-on-surface)">{seasonsCount}</h3>
				</div>
			</div>
			<p class="text-xs font-medium mt-4 text-(--md-on-surface-variant)">(All active seasons)</p>
		</div>

		<!-- Card 2: Total Teams -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container) flex flex-col justify-between transition-colors duration-300"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-primary-container) text-(--md-on-primary-container)"
				>
					<Users class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
					>
						Total Teams
					</p>
					<h3 class="text-2xl font-bold text-(--md-on-surface)">32</h3>
				</div>
			</div>
			<div class="mt-4">
				<span
					class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-error-container) text-(--md-on-error-container) border border-(--md-error)"
				>
					4 teams pending approval
				</span>
			</div>
		</div>

		<!-- Card 3: Active Participants -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container) flex flex-col justify-between transition-colors duration-300"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-secondary-container) text-(--md-on-secondary-container)"
				>
					<UserCheck class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
					>
						Active Participants
					</p>
					<h3 class="text-2xl font-bold text-(--md-on-surface)">{activeParticipantsCount}</h3>
				</div>
			</div>
			<p class="text-xs font-semibold mt-4 text-(--md-primary)">+12 new registrations this week</p>
		</div>

		<!-- Card 4: Total Mentors/Judges -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container) flex flex-col justify-between transition-colors duration-300"
		>
			<div class="flex items-center gap-4">
				<div
					class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0 bg-(--md-tertiary-container) text-(--md-on-tertiary-container)"
				>
					<Shield class="w-6 h-6" />
				</div>
				<div>
					<p
						class="text-xs font-semibold uppercase tracking-wider mb-1 text-(--md-on-surface-variant)"
					>
						Total Mentors/Judges
					</p>
					<h3 class="text-2xl font-bold text-(--md-on-surface)">{totalLecturersCount}</h3>
				</div>
			</div>
			<div class="mt-4">
				<span
					class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-error-container) text-(--md-on-error-container) border border-(--md-error)"
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
			class="lg:col-span-2 p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) transition-colors duration-300"
		>
			<h2 class="text-xl font-bold mb-8 text-(--md-on-surface)">Hackathon Timeline</h2>

			<div
				class="space-y-8 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-(--md-outline-variant)"
			>
				<!-- Timeline Point 1: Done -->
				<div class="relative flex items-center gap-4">
					<div
						class="w-10 h-10 rounded-full border border-(--md-primary) bg-(--md-primary-container) text-(--md-on-primary-container) flex items-center justify-center shrink-0 z-10"
					>
						<Check class="w-5 h-5 stroke-[2.5]" />
					</div>
					<div
						class="flex items-center justify-between flex-1 bg-(--md-surface-container-low) p-3 rounded-2xl border border-(--md-outline-variant)"
					>
						<div>
							<h4 class="font-bold text-sm line-through text-(--md-on-surface-variant)">
								Team Formation Deadline
							</h4>
							<p class="text-xs mt-0.5 text-(--md-on-surface-variant) opacity-70">June 10, 2026</p>
						</div>
						<span
							class="px-2.5 py-1 rounded-lg text-xs font-bold bg-(--md-secondary-container) text-(--md-on-secondary-container) uppercase tracking-wider"
							>Done ✓</span
						>
					</div>
				</div>

				<!-- Timeline Point 2: ACTIVE -->
				<div class="relative flex items-center gap-4">
					<div
						class="w-10 h-10 rounded-full border border-(--md-primary) bg-(--md-primary-container) text-(--md-on-primary-container) flex items-center justify-center shrink-0 z-10"
					>
						<Calendar class="w-4 h-4" />
					</div>
					<div
						class="flex items-center justify-between flex-1 bg-(--md-surface-container-high) p-3.5 rounded-2xl border-2 border-(--md-primary)"
					>
						<div>
							<h4 class="font-bold text-sm text-(--md-primary) font-extrabold">
								Project Submission
							</h4>
							<p class="text-xs mt-0.5 text-(--md-on-surface-variant)">June 13, 2026 - 11:59 PM</p>
						</div>
						<span
							class="px-3 py-1 rounded-lg text-xs font-black bg-(--md-primary) text-(--md-on-primary) uppercase tracking-wider"
							>Current Phase</span
						>
					</div>
				</div>

				<!-- Timeline Point 3: Future -->
				<div class="relative flex items-center gap-4 opacity-55">
					<div
						class="w-10 h-10 rounded-full border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface-variant) flex items-center justify-center shrink-0 z-10"
					>
						<Users class="w-4 h-4" />
					</div>
					<div
						class="flex items-center justify-between flex-1 p-3 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low)"
					>
						<div>
							<h4 class="font-bold text-sm text-(--md-on-surface-variant)">Pitching & Judging</h4>
							<p class="text-xs mt-0.5 text-(--md-on-surface-variant) opacity-70">June 15, 2026</p>
						</div>
						<span class="text-xs font-semibold text-(--md-on-surface-variant)">Upcoming</span>
					</div>
				</div>
			</div>
		</div>

		<!-- Quick Actions (1 col - 2x2 Grid) -->
		<div
			class="lg:col-span-1 p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) flex flex-col justify-between transition-colors duration-300"
		>
			<div>
				<h2 class="text-xl font-bold mb-6 text-(--md-on-surface)">Quick Actions</h2>
				<p class="text-xs mb-6 text-(--md-on-surface-variant)">
					Frequently used administrative tools for this season.
				</p>
			</div>

			<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-3.5 mt-auto">
				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 cursor-pointer border-0"
				>
					<Plus class="w-4 h-4 stroke-[2.5]" />
					<span>+ New Season</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all bg-(--md-secondary-container) text-(--md-on-secondary-container) hover:opacity-90 cursor-pointer border-0"
				>
					<Megaphone class="w-4 h-4" />
					<span>Global Notice</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-semibold text-sm flex items-center justify-center gap-2.5 transition-all border border-(--md-outline) bg-transparent text-(--md-on-surface) hover:bg-(--md-surface-container-high) cursor-pointer"
				>
					<Download class="w-4 h-4" />
					<span>Export Teams</span>
				</button>

				<button
					class="w-full py-3.5 px-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2.5 transition-all bg-(--md-tertiary-container) text-(--md-on-tertiary-container) hover:opacity-90 cursor-pointer border-0"
				>
					<Zap class="w-4 h-4" />
					<span>Generate Judges</span>
				</button>
			</div>
		</div>
	</div>

	<!-- 4. ROW 3: ATTENTION REQUIRED (System Alerts) -->
	<div
		class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container) mb-8 transition-colors duration-300"
	>
		<div class="flex items-center gap-3 mb-6">
			<AlertTriangle class="w-6 h-6 text-(--md-error)" />
			<h2 class="text-xl font-bold text-(--md-on-surface)">
				Attention Required <span class="text-sm font-normal text-(--md-on-surface-variant)"
					>(System Alerts)</span
				>
			</h2>
		</div>

		<div class="space-y-3 font-sans">
			<!-- Alert 1 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface)"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-(--md-error) font-bold">⚠️</span>
					<p class="text-sm">
						Team <strong class="text-(--md-primary)">"FPT_NinhKieu"</strong> has 0 members registered
						in the system.
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border border-(--md-outline) bg-transparent hover:bg-(--md-surface-container-high) text-(--md-on-surface) cursor-pointer"
				>
					[ View Team ]
				</button>
			</div>

			<!-- Alert 2 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface)"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-(--md-error) font-bold">⚠️</span>
					<p class="text-sm">
						Project Submission deadline in <strong class="text-(--md-error)">6 hours</strong> (14 teams
						missing link).
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border border-(--md-outline) bg-transparent hover:bg-(--md-surface-container-high) text-(--md-on-surface) cursor-pointer"
				>
					[ Send Reminders ]
				</button>
			</div>

			<!-- Alert 3 -->
			<div
				class="flex flex-col sm:flex-row items-start sm:items-center justify-between p-4 rounded-2xl transition-all border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface)"
			>
				<div class="flex items-center gap-3 mb-3 sm:mb-0">
					<span class="text-(--md-error) font-bold">⚠️</span>
					<p class="text-sm">
						Judge <strong class="text-(--md-primary)">Nguyễn Văn A</strong> rejected the invitation to
						join Season 4.
					</p>
				</div>
				<button
					class="w-full sm:w-auto px-4 py-2 rounded-xl text-xs font-bold transition-all border border-(--md-outline) bg-transparent hover:bg-(--md-surface-container-high) text-(--md-on-surface) cursor-pointer"
				>
					[ Re-assign Judge ]
				</button>
			</div>
		</div>
	</div>
</div>
