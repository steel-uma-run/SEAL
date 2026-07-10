<script lang="ts">
	import { theme } from "$lib/theme.svelte"

	let score = $state("")
	let feedback = $state("")
	let selectedTeamToEvaluate = $state<any>(null)
	let tryEvaluateOtherTeamId = $state("")

	function handleScore(e: Event) {
		e.preventDefault()
		submitMessage = "Scores submitted! (API not implemented in backend)"
	}

	function selectTeamForEvaluation(team: any) {
		selectedTeamToEvaluate = team
		score = ""
		feedback = ""
		submitMessage = ""
	}

	function attemptEvaluateOtherTeam() {
		if (tryEvaluateOtherTeamId) {
			alert(
				"Error: You cannot view detailed scores or evaluate teams you are not lecturing/advising."
			)
			tryEvaluateOtherTeamId = ""
		}
	}

	let submitMessage = $state("")
</script>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
	<!-- Header Section -->
	<header
		class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b pb-6 border-(--md-outline-variant)"
	>
		<div>
			<h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-(--md-on-surface)">
				Expert Dashboard
			</h1>
			<p class="mt-1 text-sm text-(--md-on-surface-variant)">
				Manage your Lecturering teams and evaluate project submissions.
			</p>
		</div>
	</header>

	<div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
		<!-- Column 1: Lecturering Teams -->
		<div
			class="p-8 rounded-3xl border transition-all bg-(--md-surface-container-low) border-(--md-outline-variant) shadow-sm"
		>
			<div class="flex items-center gap-3 mb-6">
				<div
					class="p-2 bg-(--md-primary-container) rounded-lg text-(--md-on-primary-container) shrink-0"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
						></path></svg
					>
				</div>
				<div>
					<h2 class="text-xl font-bold text-(--md-on-surface)">My Lecturering Teams</h2>
					<p class="text-xs text-(--md-on-surface-variant) mt-0.5">
						You are currently Lecturering the following teams.
					</p>
				</div>
			</div>

			<div class="space-y-4">
				<!-- Team 1 -->
				<div
					class="group p-5 border rounded-xl transition-all duration-300 cursor-pointer bg-(--md-surface-container) border-(--md-outline-variant) hover:bg-(--md-surface-container-high) {selectedTeamToEvaluate?.name ===
					'Team Beta'
						? 'ring-2 ring-(--md-primary)'
						: ''}"
					onclick={() =>
						selectTeamForEvaluation({
							name: "Team Beta",
							project: "Blockchain Ticketing System",
							id: "T-001"
						})}
				>
					<div class="flex items-center justify-between">
						<h3 class="font-bold text-lg text-(--md-on-surface) transition-colors">
							Team Beta <span
								class="ml-2 text-xs font-medium px-2 py-1 bg-(--md-secondary-container) text-(--md-on-secondary-container) rounded-full"
								>DEMO</span
							>
						</h3>
						<span
							class="bg-(--md-surface) border border-(--md-outline-variant) px-3 py-1 rounded-full text-xs font-semibold text-(--md-primary) shadow-sm"
							>Active</span
						>
					</div>
					<p class="text-sm text-(--md-on-surface-variant) mt-2">
						Project: Blockchain Ticketing System
					</p>
					<div class="flex items-center gap-2 mt-4 pt-4 border-t border-(--md-outline-variant)">
						<div
							class="w-6 h-6 rounded-full bg-(--md-primary-container) flex items-center justify-center text-xs font-bold text-(--md-on-primary-container)"
						>
							JD
						</div>
						<p class="text-xs font-medium text-(--md-on-surface-variant)">
							Leader: john.doe@fptu.edu.vn
						</p>
					</div>
				</div>

				<!-- Team 2 -->
				<div
					class="group p-5 border rounded-xl transition-all duration-300 cursor-pointer bg-(--md-surface-container) border-(--md-outline-variant) hover:bg-(--md-surface-container-high) {selectedTeamToEvaluate?.name ===
					'Team Gamma'
						? 'ring-2 ring-(--md-primary)'
						: ''}"
					onclick={() =>
						selectTeamForEvaluation({ name: "Team Gamma", project: "Smart Home Hub", id: "T-002" })}
				>
					<div class="flex items-center justify-between">
						<h3 class="font-bold text-lg text-(--md-on-surface) transition-colors">
							Team Gamma <span
								class="ml-2 text-xs font-medium px-2 py-1 bg-(--md-secondary-container) text-(--md-on-secondary-container) rounded-full"
								>DEMO</span
							>
						</h3>
						<span
							class="bg-(--md-surface) border border-(--md-outline-variant) px-3 py-1 rounded-full text-xs font-semibold text-(--md-primary) shadow-sm"
							>Active</span
						>
					</div>
					<p class="text-sm text-(--md-on-surface-variant) mt-2">Project: Smart Home Hub</p>
					<div class="flex items-center gap-2 mt-4 pt-4 border-t border-(--md-outline-variant)">
						<div
							class="w-6 h-6 rounded-full bg-(--md-primary-container) flex items-center justify-center text-xs font-bold text-(--md-on-primary-container)"
						>
							JS
						</div>
						<p class="text-xs font-medium text-(--md-on-surface-variant)">
							Leader: jane.smith@fptu.edu.vn
						</p>
					</div>
				</div>
			</div>

			<div class="mt-8 pt-6 border-t border-(--md-outline-variant)">
				<p class="text-sm font-semibold mb-2 text-(--md-on-surface-variant)">
					Search other teams by ID
				</p>
				<div class="flex gap-2">
					<input
						type="text"
						bind:value={tryEvaluateOtherTeamId}
						placeholder="Enter Team ID..."
						class="flex-1 rounded-xl border p-2 text-sm outline-none bg-(--md-surface) border-(--md-outline) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
					/>
					<button
						onclick={attemptEvaluateOtherTeam}
						class="bg-(--md-primary) hover:opacity-90 text-(--md-on-primary) px-4 rounded-xl text-sm font-bold border-0 cursor-pointer transition-opacity"
					>
						View Score
					</button>
				</div>
				<p class="text-xs mt-2 text-(--md-error) italic">
					Note: Lecturers cannot view detailed scores of teams they are not lecturing.
				</p>
			</div>
		</div>

		<!-- Column 2: Score & Evaluate -->
		<div
			class="p-8 rounded-3xl border transition-all bg-(--md-surface-container-low) border-(--md-outline-variant) shadow-sm"
		>
			<div class="flex items-center gap-3 mb-6">
				<div
					class="p-2 bg-(--md-secondary-container) rounded-lg text-(--md-on-secondary-container) shrink-0"
				>
					<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"
						></path></svg
					>
				</div>
				<h2 class="text-xl font-bold text-(--md-on-surface)">Score & Evaluate</h2>
			</div>

			{#if !selectedTeamToEvaluate}
				<div
					class="flex flex-col items-center justify-center h-48 text-center border-2 border-dashed rounded-xl border-(--md-outline)"
				>
					<p class="text-(--md-on-surface-variant) font-medium">
						Select a team from your list to evaluate.
					</p>
				</div>
			{:else}
				<div
					class="mb-6 p-5 border rounded-xl bg-(--md-surface-container-high) border-(--md-outline-variant)"
				>
					<div class="flex justify-between items-start">
						<div>
							<h3 class="font-bold text-lg text-(--md-on-surface)">
								{selectedTeamToEvaluate.name}
								<span
									class="ml-2 text-xs font-medium px-2 py-1 bg-(--md-primary-container) text-(--md-on-primary-container) rounded-full"
									>DEMO</span
								>
							</h3>
							<p class="text-sm text-(--md-on-surface-variant) mt-1">
								Project: {selectedTeamToEvaluate.project}
							</p>
						</div>
						<a
							href="#"
							class="inline-flex items-center gap-1 text-(--md-primary) hover:opacity-80 bg-(--md-surface) px-3 py-1.5 rounded-lg text-sm font-medium shadow-sm transition-opacity border border-(--md-outline-variant)"
						>
							<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
								></path></svg
							>
							View
						</a>
					</div>
				</div>

				<form onsubmit={handleScore} class="flex flex-col gap-5">
					<div class="space-y-1">
						<label class="text-sm font-semibold text-(--md-on-surface-variant)">Score (0-100)</label
						>
						<input
							type="number"
							bind:value={score}
							min="0"
							max="100"
							required
							placeholder="85"
							class="w-full rounded-xl border p-3 text-lg font-medium transition-all outline-none bg-(--md-surface) border-(--md-outline) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
						/>
					</div>

					<div class="space-y-1">
						<label class="text-sm font-semibold text-(--md-on-surface-variant)">Feedback</label>
						<textarea
							bind:value={feedback}
							rows="4"
							required
							placeholder="Provide constructive feedback..."
							class="w-full rounded-xl border p-3 transition-all outline-none resize-none bg-(--md-surface) border-(--md-outline) text-(--md-on-surface) focus:ring-2 focus:ring-(--md-primary)"
						></textarea>
					</div>

					{#if submitMessage}
						<div
							class="p-3 rounded-lg text-sm font-medium border flex items-start gap-2 bg-(--md-secondary-container) text-(--md-on-secondary-container) border-(--md-outline-variant)"
						>
							<svg
								class="w-5 h-5 flex-shrink-0 mt-0.5"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
								><path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
								></path></svg
							>
							{submitMessage}
						</div>
					{/if}

					<button
						type="submit"
						class="mt-2 w-full bg-(--md-primary) text-(--md-on-primary) rounded-xl py-3 font-semibold shadow-sm hover:opacity-90 transition-opacity border-0 cursor-pointer"
					>
						Submit Evaluation
					</button>
				</form>
			{/if}
		</div>
	</div>
</div>
