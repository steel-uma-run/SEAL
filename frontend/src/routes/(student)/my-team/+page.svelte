<script lang="ts">
	import { onMount } from "svelte"
	import { authState } from "$lib/features/auth/auth.svelte"
	import { getMyTeam, createTeam, addMemberByEmail } from "$lib/features/teams/api"
	import type { Team } from "$lib/features/teams/types"

	let team = $state<Team | null>(null)
	let isLoading = $state(true)

	let newTeamName = $state("")
	let newSeason = $state("Summer Hackathon 2026")
	let newTrack = $state("Web Dev")

	let newMemberEmail = $state("")
	let errorMsg = $state("")

	onMount(async () => {
		if (authState.user?.email) {
			team = await getMyTeam(authState.user.email)
		}
		isLoading = false
	})

	async function handleCreateTeam() {
		if (!newTeamName) return
		try {
			errorMsg = ""
			const email = authState.user?.email || ""
			team = await createTeam(email, newTeamName, newSeason, newTrack)
		} catch (err: any) {
			errorMsg = err.message
		}
	}

	async function handleAddMember() {
		if (!team || !newMemberEmail) return
		try {
			errorMsg = ""
			team = await addMemberByEmail(team.id, newMemberEmail, "Member")
			newMemberEmail = ""
		} catch (err: any) {
			errorMsg = err.message
		}
	}
</script>

<div class="max-w-4xl mx-auto space-y-8">
	<div class="flex items-center justify-between">
		<h1 class="text-3xl font-extrabold text-gray-900 dark:text-white">Đội của tôi</h1>
		<a
			href="/dashboard"
			class="px-4 py-2 text-sm font-bold border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-gray-200 transition-colors"
		>
			&larr; Về Dashboard
		</a>
	</div>

	{#if isLoading}
		<div class="p-8 text-center text-gray-500">Đang tải...</div>
	{:else if !team}
		<!-- Chưa có đội -->
		<div
			class="bg-white dark:bg-gray-800 p-8 rounded-2xl border border-gray-200 dark:border-gray-700 text-center"
		>
			<h2 class="text-2xl font-bold mb-2">Bạn chưa có đội</h2>
			<p class="text-gray-500 dark:text-white mb-6">Hãy tạo một đội mới để tham gia sự kiện</p>

			<form
				onsubmit={(e) => {
					e.preventDefault()
					handleCreateTeam()
				}}
				class="max-w-md mx-auto space-y-4 text-left"
			>
				{#if errorMsg}
					<div class="p-3 bg-red-100 text-red-700 rounded-lg text-sm font-semibold">{errorMsg}</div>
				{/if}
				<div>
					<label class="block text-sm font-bold text-gray-700 dark:text-white mb-1">Tên đội</label>
					<input
						required
						bind:value={newTeamName}
						type="text"
						class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500"
						placeholder="VD: Tech Innovators"
					/>
				</div>
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label class="block text-sm font-bold mb-1">Mùa giải (Season)</label>
						<input
							readonly
							value={newSeason}
							class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white opacity-70 cursor-not-allowed"
						/>
					</div>
					<div>
						<label class="block text-sm font-bold mb-1">Hạng mục (Track)</label>
						<select
							bind:value={newTrack}
							class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500"
						>
							<option>Web Dev</option>
							<option>AI & Data</option>
							<option>Mobile App</option>
						</select>
					</div>
				</div>
				<button
					type="submit"
					class="w-full mt-4 py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl transition-colors"
				>
					Tạo đội ngay
				</button>
			</form>
		</div>
	{:else}
		<!-- Đã có đội -->
		<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
			<!-- Cột trái: Thông tin đội & Danh sách thành viên -->
			<div class="lg:col-span-2 space-y-6">
				<!-- Thông tin đội -->
				<div
					class="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-200 dark:border-gray-700 flex flex-col md:flex-row justify-between items-start md:items-center"
				>
					<div>
						<div class="flex items-center gap-3 mb-2">
							<h2 class="text-2xl font-black text-gray-900 dark:text-white">{team.name}</h2>
							<span
								class="px-2 py-1 bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400 text-xs font-bold rounded-md border border-green-200 dark:border-green-800"
								>Hợp lệ</span
							>
						</div>
						<div
							class="text-sm text-gray-500 dark:text-gray-400 font-medium flex flex-wrap gap-x-4 gap-y-2"
						>
							<span
								>ID Đội: <span
									class="bg-gray-100 dark:bg-gray-700 px-1 py-0.5 rounded text-gray-700 dark:text-gray-300"
									>team-{team.id.substring(0, 8)}...</span
								></span
							>
							<span
								>Mùa giải: <span class="font-semibold text-gray-700 dark:text-gray-300"
									>{team.season}</span
								></span
							>
							<span
								>Track: <span class="font-semibold text-gray-700 dark:text-gray-300"
									>{team.track}</span
								></span
							>
						</div>
					</div>
					<div class="mt-4 md:mt-0">
						<a
							href="/submission"
							class="inline-block px-6 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl transition-colors shadow-sm"
						>
							Nộp Bài
						</a>
					</div>
				</div>

				<!-- Danh sách thành viên -->
				<div
					class="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden"
				>
					<div
						class="p-6 border-b border-gray-200 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-800/50"
					>
						<h3 class="text-lg font-bold text-gray-900 dark:text-white">
							Thành viên ({team.members.length}/4)
						</h3>
						<p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
							Danh sách các thành viên trong đội
						</p>
					</div>
					<div class="divide-y divide-gray-200 dark:divide-gray-700">
						{#each team.members as member}
							<div
								class="p-6 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
							>
								<div class="flex items-center space-x-4">
									<div
										class="w-12 h-12 rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 flex items-center justify-center font-bold text-lg"
									>
										{member.name.charAt(0).toUpperCase()}
									</div>
									<div>
										<div class="flex items-center gap-2">
											<h4 class="font-bold text-gray-900 dark:text-white">{member.name}</h4>
											{#if member.role === "Leader"}
												<span
													class="px-2 py-0.5 bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-500 text-xs font-bold rounded"
													>ĐỘI TRƯỞNG</span
												>
											{/if}
										</div>
										<p class="text-sm text-gray-500 dark:text-gray-400">{member.email}</p>
									</div>
								</div>
							</div>
						{/each}
					</div>
				</div>
			</div>

			<!-- Cột phải: Mời thành viên & Mentor Info -->
			<div class="lg:col-span-1 space-y-6">
				<!-- Add Member -->
				<div
					class="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-200 dark:border-gray-700"
				>
					<h3 class="text-lg font-bold mb-4 text-gray-900 dark:text-white">Mời thành viên</h3>
					{#if errorMsg}
						<div class="p-3 mb-4 bg-red-100 text-red-700 rounded-lg text-sm font-semibold">
							{errorMsg}
						</div>
					{/if}
					<form
						onsubmit={(e) => {
							e.preventDefault()
							handleAddMember()
						}}
						class="space-y-4"
					>
						<div>
							<input
								required
								bind:value={newMemberEmail}
								type="email"
								class="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500 transition-shadow text-sm"
								placeholder="Nhập email sinh viên..."
							/>
						</div>
						<button
							type="submit"
							class="w-full py-3 bg-purple-600 hover:bg-purple-700 text-white font-bold rounded-xl transition-colors text-sm"
						>
							Thêm vào đội
						</button>
					</form>
				</div>

				<!-- Mentor Info -->
				<div
					class="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-200 dark:border-gray-700"
				>
					<h3 class="text-lg font-bold mb-2 text-gray-900 dark:text-white">Mentor hướng dẫn</h3>
					<p class="text-sm text-gray-500 dark:text-gray-400">
						Mentor hướng dẫn sẽ được <span class="font-semibold text-gray-900 dark:text-white"
							>Ban tổ chức phân công</span
						> trong thời gian tới.
					</p>
				</div>
			</div>
		</div>
	{/if}
</div>
