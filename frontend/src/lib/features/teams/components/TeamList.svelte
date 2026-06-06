<script lang="ts">
	import type { Team, TeamMember } from "../types"

	let { team, currentUserId, onRemoveMember, onTransferLeader } = $props<{
		team: Team
		currentUserId: string
		onRemoveMember: (memberId: string) => void
		onTransferLeader: (memberId: string) => void
	}>()

	let isLeader = $derived(team.leaderId === currentUserId)
</script>

<div
	class="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden"
>
	<div
		class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-800/50 flex justify-between items-center"
	>
		<div>
			<h3 class="text-lg font-bold text-gray-900 dark:text-white">
				Thành viên ({team.memberIds.length}/4)
			</h3>
			<p class="text-sm text-gray-500 dark:text-gray-400">Danh sách các thành viên trong đội</p>
		</div>
	</div>

	<ul class="divide-y divide-gray-100 dark:divide-gray-700">
		{#if team.members}
			{#each team.members as member}
				<li class="p-6 flex items-center justify-between">
					<div class="flex items-center space-x-4">
						<!-- Avatar -->
						<div
							class="h-10 w-10 rounded-full bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 flex items-center justify-center font-bold text-sm"
						>
							{member.fullName.charAt(0)}
						</div>

						<!-- Info -->
						<div>
							<div class="flex items-center space-x-2">
								<p class="text-sm font-semibold text-gray-900 dark:text-white">{member.fullName}</p>
								{#if member.id === team.leaderId}
									<span
										class="px-2 py-0.5 rounded text-[10px] font-bold bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-500 uppercase tracking-wider"
									>
										Đội trưởng
									</span>
								{/if}
							</div>
							<div
								class="flex items-center text-xs text-gray-500 dark:text-gray-400 mt-0.5 space-x-3"
							>
								<span>{member.email}</span>
								<span>•</span>
								<span>{member.studentCode} ({member.studentType})</span>
							</div>
						</div>
					</div>

					<!-- Actions (Only for Leader, and not for themselves) -->
					{#if isLeader && member.id !== currentUserId}
						<div class="flex items-center space-x-2">
							<button
								onclick={() => onTransferLeader(member.id)}
								class="px-3 py-1.5 text-xs font-medium text-blue-600 hover:bg-blue-50 dark:text-blue-400 dark:hover:bg-blue-900/20 rounded-md transition-colors"
								title="Chuyển quyền Đội trưởng"
							>
								Nhường quyền
							</button>
							<button
								onclick={() => onRemoveMember(member.id)}
								class="px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20 rounded-md transition-colors"
								title="Xóa khỏi đội"
							>
								Kích
							</button>
						</div>
					{/if}
				</li>
			{/each}
		{/if}
	</ul>
</div>
