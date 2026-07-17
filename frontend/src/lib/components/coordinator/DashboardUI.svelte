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
		Zap,
		ArrowRight
	} from "@lucide/svelte"

	let {
		profile,
		seasonsCount = 0,
		activeParticipantsCount = 0,
		totalLecturersCount = 0,
		totalTeamsCount = 0,
		pendingTeamsCount = 0,
		pendingStudentsCount = 0,
		unassignedJudgesCount = 0,
		activeEvents = [],
		currentSeason = null
	} = $props<{
		profile: any
		seasonsCount?: number
		activeParticipantsCount?: number
		totalLecturersCount?: number
		totalTeamsCount?: number
		pendingTeamsCount?: number
		pendingStudentsCount?: number
		unassignedJudgesCount?: number
		activeEvents?: any[]
		currentSeason?: any
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
						<span>
							{#if currentSeason}
								Season: {currentSeason.semester.charAt(0) +
									currentSeason.semester.slice(1).toLowerCase()}
								{currentSeason.year}
							{:else}
								No Active Season
							{/if}
						</span>
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
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex flex-col justify-between transition-colors duration-300 shadow-sm"
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
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex flex-col justify-between transition-colors duration-300 shadow-sm"
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
					<h3 class="text-2xl font-bold text-(--md-on-surface)">{totalTeamsCount}</h3>
				</div>
			</div>
			<div class="mt-4">
				{#if pendingTeamsCount > 0}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-error-container) text-(--md-on-error-container) border border-(--md-error)"
					>
						{pendingTeamsCount}
						{pendingTeamsCount === 1 ? "team" : "teams"} pending approval
					</span>
				{:else}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-outline-variant)"
					>
						All teams approved
					</span>
				{/if}
			</div>
		</div>

		<!-- Card 3: Active Participants -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex flex-col justify-between transition-colors duration-300 shadow-sm"
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
			<div class="mt-4">
				{#if pendingStudentsCount > 0}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-error-container) text-(--md-on-error-container) border border-(--md-error)"
					>
						{pendingStudentsCount} pending student approvals
					</span>
				{:else}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-outline-variant)"
					>
						No pending accounts
					</span>
				{/if}
			</div>
		</div>

		<!-- Card 4: Total Lecturers/Judges -->
		<div
			class="p-6 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) flex flex-col justify-between transition-colors duration-300 shadow-sm"
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
						Total Lecturers/Judges
					</p>
					<h3 class="text-2xl font-bold text-(--md-on-surface)">{totalLecturersCount}</h3>
				</div>
			</div>
			<div class="mt-4">
				{#if unassignedJudgesCount > 0}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-error-container) text-(--md-on-error-container) border border-(--md-error)"
					>
						{unassignedJudgesCount}
						{unassignedJudgesCount === 1 ? "judge" : "judges"} unassigned to tracks
					</span>
				{:else}
					<span
						class="inline-block px-2.5 py-1 rounded-md text-xs font-semibold bg-(--md-secondary-container) text-(--md-on-secondary-container) border border-(--md-outline-variant)"
					>
						All judges assigned
					</span>
				{/if}
			</div>
		</div>
	</div>

	<!-- 3. ROW 2: ACTIVE EVENTS (Full width) -->
	<div class="mb-8">
		<!-- Active Events -->
		<div
			class="p-8 rounded-3xl border border-(--md-outline-variant) bg-(--md-surface-container-lowest) transition-colors duration-300 shadow-sm"
		>
			<div class="flex justify-between items-center mb-6">
				<h2 class="text-xl font-bold text-(--md-on-surface)">Active Events</h2>
			</div>

			{#if activeEvents.length === 0}
				<div
					class="flex flex-col items-center justify-center py-12 px-4 text-center border border-dashed border-(--md-outline-variant) rounded-3xl"
				>
					<Calendar class="w-12 h-12 text-(--md-on-surface-variant) opacity-50 mb-3" />
					<h3 class="font-bold text-sm text-(--md-on-surface)">No active events found</h3>
					<p class="text-xs text-(--md-on-surface-variant) mt-1 max-w-sm">
						Go to Season & Event management to finalize and activate events.
					</p>
				</div>
			{:else}
				<div class="space-y-4 max-h-[500px] overflow-y-auto pr-2">
					{#each activeEvents as event}
						<div
							class="p-5 rounded-2xl border border-(--md-outline-variant) bg-(--md-surface-container-low) hover:bg-(--md-surface-container-high) transition-all flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
						>
							<div class="flex-1">
								<div class="flex items-center gap-2 mb-1.5 flex-wrap">
									<span
										class="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-(--md-primary-container) text-(--md-on-primary-container) border border-(--md-outline-variant)"
									>
										{event.seasonSemester}
										{event.seasonYear}
									</span>
									<span
										class="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-(--md-tertiary-container) text-(--md-on-tertiary-container)"
									>
										{event.teamsCount}
										{event.teamsCount === 1 ? "Team" : "Teams"}
									</span>
								</div>
								<h3 class="text-base font-bold text-(--md-on-surface) mb-1">
									{event.name}
								</h3>
								<p class="text-xs text-(--md-on-surface-variant) line-clamp-2 mb-3">
									{event.description}
								</p>
								<div class="flex items-center gap-4 text-xs text-(--md-on-surface-variant)">
									<div class="flex items-center gap-1.5">
										<Calendar class="w-3.5 h-3.5 text-(--md-primary)" />
										<span>
											{new Date(event.startTime || event.start_time).toLocaleDateString()} - {new Date(
												event.endTime || event.end_time
											).toLocaleDateString()}
										</span>
									</div>
								</div>
							</div>
							<a
								href="/coordinator/seasons/{event.seasonId}/events/{event.id}"
								class="shrink-0 flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs transition-all bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 cursor-pointer no-underline border-0"
							>
								<span>View Event Details</span>
								<ArrowRight class="w-4 h-4" />
							</a>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</div>
