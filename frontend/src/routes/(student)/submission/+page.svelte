<script lang="ts">
	import { onMount } from "svelte"
	import { authState } from "$lib/features/auth/auth.svelte"
	import { getMyTeam } from "$lib/features/teams/api"
	import type { Team } from "$lib/features/teams/types"

	let team = $state<Team | null>(null)
	let isLoading = $state(true)

	// Form states
	let githubUrl = $state("")
	let pdfUrl = $state("")
	let videoUrl = $state("")
	let isSubmitting = $state(false)
	let successMsg = $state("")
	let errorMsg = $state("")

	onMount(async () => {
		if (authState.user?.email) {
			try {
				team = await getMyTeam(authState.user.email)
			} catch (err) {
				// Not in a team
				team = null
			}
		}
		isLoading = false
	})

	async function handleSubmit(event: Event) {
		event.preventDefault()
		isSubmitting = true
		errorMsg = ""
		successMsg = ""

		// Simulate API call for submission
		try {
			await new Promise((resolve) => setTimeout(resolve, 1000))
			successMsg = "Bài dự thi đã được cập nhật thành công!"
		} catch (err: any) {
			errorMsg = err.message || "Có lỗi xảy ra khi nộp bài."
		} finally {
			isSubmitting = false
		}
	}
</script>

<div class="max-w-4xl mx-auto space-y-8">
	<div class="flex items-center justify-between">
		<h1 class="text-3xl font-extrabold text-gray-900 dark:text-white">Nộp bài dự thi</h1>
		<a
			href="/dashboard"
			class="px-4 py-2 text-sm font-bold border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
		>
			&larr; Về Dashboard
		</a>
	</div>

	{#if isLoading}
		<div class="p-8 text-center text-gray-500">Đang tải thông tin đội...</div>
	{:else if !team}
		<div
			class="bg-white dark:bg-gray-800 p-8 rounded-2xl border border-gray-200 dark:border-gray-700 text-center"
		>
			<h2 class="text-2xl font-bold mb-2">Bạn chưa có đội</h2>
			<p class="text-gray-500 dark:text-white mb-6">
				Chỉ những sinh viên đã có đội thi mới có thể nộp bài.
			</p>
			<a
				href="/my-team"
				class="inline-block px-6 py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl transition-colors"
			>
				Đi tới trang Đội của tôi
			</a>
		</div>
	{:else}
		<div
			class="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden"
		>
			<!-- Team Info Header -->
			<div
				class="p-6 md:p-8 border-b border-gray-200 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-800/50 flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
			>
				<div>
					<h2 class="text-2xl font-black text-gray-900 dark:text-white">
						Đội thi: <span class="text-orange-600 dark:text-orange-500">{team.name}</span>
					</h2>
					<p class="text-gray-600 dark:text-white font-medium mt-1">Chủ đề: {team.track}</p>
				</div>
				<div
					class="px-4 py-2 bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg text-sm font-bold text-gray-700 dark:text-white"
				>
					Mã đội: {team.id.substring(0, 8)}...
				</div>
			</div>

			<!-- Submission Form -->
			<div class="p-6 md:p-8">
				{#if successMsg}
					<div
						class="mb-6 p-4 bg-green-100 border border-green-200 text-green-800 dark:bg-green-900/30 dark:border-green-800 dark:text-green-400 rounded-xl font-bold"
					>
						{successMsg}
					</div>
				{/if}
				{#if errorMsg}
					<div
						class="mb-6 p-4 bg-red-100 border border-red-200 text-red-800 dark:bg-red-900/30 dark:border-red-800 dark:text-red-400 rounded-xl font-bold"
					>
						{errorMsg}
					</div>
				{/if}

				<form onsubmit={handleSubmit} class="space-y-8">
					<!-- GitHub URL -->
					<div class="space-y-2">
						<label class="block text-base font-bold text-gray-900 dark:text-white"
							>1. GitHub Repository URL <span class="text-red-500">*</span></label
						>
						<div class="relative">
							<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
								<svg
									class="h-5 w-5 text-gray-400"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
									/>
								</svg>
							</div>
							<input
								type="url"
								required
								bind:value={githubUrl}
								class="block w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500 transition-shadow"
								placeholder="https://github.com/organization/repository"
							/>
						</div>
						<p class="text-sm text-gray-500 dark:text-white font-medium">
							Liên kết bắt buộc phải bắt đầu bằng https://github.com/
						</p>
					</div>

					<!-- PDF URL -->
					<div class="space-y-2">
						<label class="block text-base font-bold text-gray-900 dark:text-white"
							>2. PDF Description File URL <span class="text-red-500">*</span></label
						>
						<div class="relative">
							<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
								<svg
									class="h-5 w-5 text-gray-400"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
									/>
								</svg>
							</div>
							<input
								type="url"
								required
								bind:value={pdfUrl}
								class="block w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500 transition-shadow"
								placeholder="https://example.com/project-report.pdf"
							/>
						</div>
						<p class="text-sm text-gray-500 dark:text-white font-medium">
							Tải báo cáo lên Google Drive/Dropbox và dán liên kết chia sẻ công khai vào đây.
						</p>
					</div>

					<!-- Video URL -->
					<div class="space-y-2">
						<label class="block text-base font-bold text-gray-900 dark:text-white"
							>3. Demo Video URL <span class="text-red-500">*</span></label
						>
						<div class="relative">
							<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
								<svg
									class="h-5 w-5 text-gray-400"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
									/>
								</svg>
							</div>
							<input
								type="url"
								required
								bind:value={videoUrl}
								class="block w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-orange-500 transition-shadow"
								placeholder="https://www.youtube.com/watch?v=..."
							/>
						</div>
						<p class="text-sm text-gray-500 dark:text-white font-medium">
							Liên kết Video demo dự án (Youtube, Vimeo, Drive...).
						</p>
					</div>

					<div class="pt-6">
						<button
							type="submit"
							disabled={isSubmitting}
							class="w-full py-4 bg-orange-600 hover:bg-orange-700 disabled:opacity-70 disabled:cursor-not-allowed text-white text-lg font-black uppercase tracking-wider rounded-xl transition-colors"
						>
							{isSubmitting ? "Đang cập nhật..." : "Nộp / Cập nhật bài dự thi"}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}
</div>
