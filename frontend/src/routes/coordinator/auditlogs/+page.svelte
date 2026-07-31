<script lang="ts">
	import { onMount } from "svelte"
	import { getAllAuditLogs } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { Activity, Search, Filter, ShieldCheck, Ban, UploadCloud, CheckSquare } from "@lucide/svelte"

	let logs = $state<any[]>([])
	let isLoading = $state(true)
	let errorMessage = $state("")
	let searchQuery = $state("")

	async function loadLogs() {
		try {
			const { data, error } = await getAllAuditLogs({ throwOnError: false })
			if (data) {
				// Sort logs by action_time descending (newest first)
				logs = data.sort((a, b) => new Date(b.action_time).getTime() - new Date(a.action_time).getTime())
			} else if (error) {
				console.error("Audit log load error:", error)
				errorMessage = `Failed to load audit logs. Error: ${JSON.stringify(error)}`
			}
		} catch (err: any) {
			errorMessage = err.message || "An error occurred."
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadLogs()
	})

	function getLogType(log: any) {
		if ('approved_student_email' in log) return "ACCOUNT_APPROVED"
		if ('banned_student_email' in log) return "ACCOUNT_BANNED"
		if ('submission_id' in log && !('criteria_id' in log) && !('comment' in log)) return "SUBMISSION_CREATED" // Just guessing structure
		if ('comment' in log) return "GRADING" // GradingLogDto
		return "SYSTEM_EVENT"
	}

	function getLogDetails(log: any) {
		if ('approved_student_email' in log) return `Approved account for ${log.approved_student_email}`
		if ('banned_student_email' in log) return `Banned account for ${log.banned_student_email}`
		if ('submission_id' in log) return `Action on submission ${log.submission_id}`
		return "System event recorded."
	}

	let filteredLogs = $derived(
		logs.filter((log) => {
			if (!searchQuery) return true;
			const query = searchQuery.toLowerCase();
			return (
				log.actor_email?.toLowerCase().includes(query) ||
				getLogDetails(log).toLowerCase().includes(query) ||
				log.id?.toLowerCase().includes(query)
			)
		})
	)

	function formatDate(dateString: string) {
		return new Date(dateString).toLocaleString()
	}
</script>

<svelte:head>
	<title>Audit Logs - SEAL</title>
</svelte:head>

<div class="page-wrapper" class:dark={theme.darkMode}>
	<header class="page-header">
		<div class="page-header__text">
			<h1 class="page-header__title">System Audit Logs</h1>
			<p class="page-header__subtitle">
				View a trail of critical actions performed on the platform.
			</p>
		</div>
	</header>

	<div class="content-panel">
		<!-- Toolbar -->
		<div class="toolbar">
			<div class="search-wrapper">
				<span class="search-wrapper__icon-wrap">
					<Search class="search-wrapper__icon" />
				</span>
				<input
					type="text"
					bind:value={searchQuery}
					placeholder="Search logs by actor, detail or ID..."
					class="search-input"
				/>
				{#if searchQuery}
					<button type="button" onclick={() => (searchQuery = "")} class="search-clear-btn">
						Clear
					</button>
				{/if}
			</div>
		</div>

		<!-- Table -->
		<div class="table-wrapper">
			<table class="user-table">
				<thead class="user-table__head">
					<tr class="user-table__head-row">
						<th class="user-table__th">Timestamp</th>
						<th class="user-table__th">Actor (Email)</th>
						<th class="user-table__th">Event Type</th>
						<th class="user-table__th">Details</th>
					</tr>
				</thead>
				<tbody class="user-table__body">
					{#if isLoading}
						<tr>
							<td colspan="4" class="text-center" style="padding: 2rem;">Loading logs...</td>
						</tr>
					{:else if errorMessage}
						<tr>
							<td colspan="4" class="text-center" style="padding: 2rem; color: #ef4444;">{errorMessage}</td>
						</tr>
					{:else if filteredLogs.length > 0}
						{#each filteredLogs as log}
							<tr class="user-table__row">
								<td class="user-table__cell user-table__cell--date">
									<div class="time-stamp">{formatDate(log.action_time)}</div>
								</td>
								<td class="user-table__cell">{log.actor_email}</td>
								<td class="user-table__cell">
									{#if getLogType(log) === 'ACCOUNT_APPROVED'}
										<span class="badge badge--success"><ShieldCheck class="icon-sm" /> Approved</span>
									{:else if getLogType(log) === 'ACCOUNT_BANNED'}
										<span class="badge badge--danger"><Ban class="icon-sm" /> Banned</span>
									{:else if getLogType(log) === 'GRADING'}
										<span class="badge badge--info"><CheckSquare class="icon-sm" /> Graded</span>
									{:else if getLogType(log) === 'SUBMISSION_CREATED'}
										<span class="badge badge--primary"><UploadCloud class="icon-sm" /> Submission</span>
									{:else}
										<span class="badge badge--default"><Activity class="icon-sm" /> Event</span>
									{/if}
								</td>
								<td class="user-table__cell">
									<span class="log-details">{getLogDetails(log)}</span>
									<span class="log-id">ID: {log.id}</span>
								</td>
							</tr>
						{/each}
					{:else}
						<tr class="user-table__row user-table__row--empty">
							<td colspan="4" class="empty-state text-center" style="padding: 2rem;">
								No audit logs found.
							</td>
						</tr>
					{/if}
				</tbody>
			</table>
		</div>
	</div>
</div>

<style lang="scss">
	/* Base styles from ManageUser for consistency */
	.page-wrapper {
		max-width: 1600px;
		width: 100%;
		margin: 0 auto;
		padding: 2rem;
	}

	.page-header {
		margin-bottom: 2rem;
		&__title {
			font-size: 1.875rem;
			font-weight: 700;
			color: var(--md-sys-color-on-background, #1f2937);
			margin: 0;
		}
		&__subtitle {
			color: var(--md-sys-color-on-surface-variant, #6b7280);
			margin-top: 0.5rem;
		}
	}

	.content-panel {
		background: var(--md-sys-color-surface, #ffffff);
		border-radius: 1rem;
		box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
		border: 1px solid var(--md-sys-color-outline-variant, #e5e7eb);
		overflow: hidden;
	}

	.toolbar {
		padding: 1.5rem;
		border-bottom: 1px solid var(--md-sys-color-outline-variant, #e5e7eb);
		background: var(--md-sys-color-surface-container-lowest, #f9fafb);
	}

	.search-wrapper {
		position: relative;
		max-width: 400px;
		display: flex;
		align-items: center;

		&__icon-wrap {
			position: absolute;
			left: 1rem;
			color: #9ca3af;
			display: flex;
		}

		.search-input {
			width: 100%;
			padding: 0.75rem 1rem 0.75rem 3rem;
			border-radius: 0.5rem;
			border: 1px solid #d1d5db;
			outline: none;
			transition: all 0.2s;

			&:focus {
				border-color: #6366f1;
				box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
			}
		}

		.search-clear-btn {
			position: absolute;
			right: 1rem;
			background: none;
			border: none;
			color: #6b7280;
			cursor: pointer;
			font-size: 0.875rem;
			&:hover { color: #1f2937; }
		}
	}

	.table-wrapper {
		width: 100%;
		overflow-x: auto;
	}

	.user-table {
		width: 100%;
		border-collapse: collapse;
		text-align: left;

		&__head {
			background: var(--md-sys-color-surface-container-low, #f9fafb);
			border-bottom: 2px solid var(--md-sys-color-outline-variant, #e5e7eb);
		}

		&__th {
			padding: 1rem 1.5rem;
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			color: var(--md-sys-color-on-surface-variant, #6b7280);
		}

		&__row {
			border-bottom: 1px solid var(--md-sys-color-outline-variant, #e5e7eb);
			transition: background-color 0.15s;
			&:hover {
				background-color: var(--md-sys-color-surface-container-lowest, #f9fafb);
			}
		}

		&__cell {
			padding: 1rem 1.5rem;
			vertical-align: middle;
			font-size: 0.875rem;
			color: var(--md-sys-color-on-surface, #1f2937);
		}
	}

	.time-stamp {
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
		color: #4b5563;
		font-weight: 500;
	}

	.badge {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.25rem 0.75rem;
		border-radius: 9999px;
		font-size: 0.75rem;
		font-weight: 600;

		.icon-sm { width: 0.875rem; height: 0.875rem; }

		&--success { background: #dcfce7; color: #166534; }
		&--danger { background: #fee2e2; color: #991b1b; }
		&--info { background: #dbeafe; color: #1e40af; }
		&--primary { background: #f3e8ff; color: #6b21a8; }
		&--default { background: #f3f4f6; color: #374151; }
	}

	.log-details {
		display: block;
		font-weight: 500;
		color: #1f2937;
	}

	.log-id {
		display: block;
		font-size: 0.75rem;
		color: #9ca3af;
		font-family: monospace;
		margin-top: 0.25rem;
	}

	.dark {
		.content-panel {
			background: #18181b;
			border-color: #27272a;
		}
		.toolbar {
			background: #09090b;
			border-color: #27272a;
		}
		.search-input {
			background: #27272a;
			border-color: #3f3f46;
			color: #f4f4f5;
		}
		.user-table__head {
			background: #09090b;
			border-color: #27272a;
		}
		.user-table__row {
			border-color: #27272a;
			&:hover { background-color: #27272a; }
		}
		.user-table__cell, .time-stamp, .log-details {
			color: #e4e4e7;
		}
		.badge {
			&--success { background: rgba(22, 163, 74, 0.2); color: #4ade80; }
			&--danger { background: rgba(220, 38, 38, 0.2); color: #f87171; }
			&--info { background: rgba(37, 99, 235, 0.2); color: #60a5fa; }
			&--primary { background: rgba(147, 51, 234, 0.2); color: #c084fc; }
			&--default { background: rgba(82, 82, 91, 0.2); color: #a1a1aa; }
		}
	}
</style>
