<script lang="ts">
	import { authState } from "$lib/features/auth/auth.svelte"

	// Lấy thông tin user hiện tại từ state
	let user = $derived({
		name: authState.user?.full_name || "Người dùng",
		studentId: authState.user?.student_code || "Chưa có"
	})

	// Mock dữ liệu Seasons hiện có
	const seasons = [
		{
			id: 1,
			name: "Summer Hackathon 2026",
			status: "Đang diễn ra",
			statusColor:
				"bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400 border-green-200 dark:border-green-800"
		},
		{
			id: 2,
			name: "Winter CodeFest 2026",
			status: "Sắp tới",
			statusColor:
				"bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 border-blue-200 dark:border-blue-800"
		}
	]
</script>

<div class="space-y-8">
	<!-- Thẻ Welcome -->
	<div
		class="bg-white dark:bg-gray-800 p-8 rounded-2xl border border-gray-200 dark:border-gray-700"
	>
		<h1 class="text-3xl font-extrabold text-gray-900 dark:text-white">
			Chào mừng sinh viên <span class="text-orange-700 dark:text-orange-400">{user.name}</span>
		</h1>
		<p class="text-lg text-gray-700 dark:text-white mt-3 font-medium">
			Mã số sinh viên: <span
				class="font-bold text-black dark:text-white bg-gray-100 dark:bg-gray-700 px-2 py-1 border border-gray-300 dark:border-gray-600 rounded-lg"
				>{user.studentId}</span
			>
		</p>
	</div>

	<!-- Danh sách Mùa giải (Seasons) -->
	<div>
		<h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4 uppercase tracking-wider">
			Các sự kiện (Seasons) hiện có
		</h2>
		<div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
			{#each seasons as season}
				<div
					class="p-6 bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 hover:border-orange-500 dark:hover:border-orange-400 transition-colors"
				>
					<div class="flex justify-between items-start mb-4">
						<h3 class="text-xl font-bold text-gray-900 dark:text-white">{season.name}</h3>
						<span class="px-3 py-1 text-xs font-bold border {season.statusColor} rounded-md">
							{season.status}
						</span>
					</div>
					<a
						href="/my-team"
						class="inline-block mt-2 px-6 py-2 border-2 border-orange-600 dark:border-orange-500 text-orange-700 dark:text-orange-400 font-bold rounded-xl hover:bg-orange-600 hover:text-white dark:hover:bg-orange-500 dark:hover:text-white transition-colors"
					>
						Đi tới Đội của tôi
					</a>
				</div>
			{/each}
		</div>
	</div>
</div>
