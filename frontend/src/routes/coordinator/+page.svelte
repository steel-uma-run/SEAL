<script lang="ts">
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile, getAllSeasons, getAllAccounts } from "$lib/api"
	import DashboardUI from "$lib/components/coordinator/DashboardUI.svelte"

	let profile: any = $state(null)
	let seasonsCount = $state(0)
	let activeParticipantsCount = $state(0)
	let totalLecturersCount = $state(0)
	let isLoading = $state(true)
	let errorMessage = $state("")

	async function loadData() {
		try {
			const { data, response: profileRes } = await getSelfProfile({ throwOnError: false })
			if (!profileRes?.ok || !data) {
				if (profileRes?.status === 401 || profileRes?.status === 403) {
					goto("/auth/login")
					return
				}
				throw new Error("Failed to load profile")
			}
			profile = data

			const { data: seasonsData, response: seasonsRes } = await getAllSeasons({
				throwOnError: false
			})
			if (seasonsRes?.ok && seasonsData) {
				seasonsCount = seasonsData.length
			}

			const { data: accountsData, response: accountsRes } = await getAllAccounts({
				throwOnError: false
			})
			if (accountsRes?.ok && accountsData) {
				activeParticipantsCount = accountsData.filter(
					(u: any) => u.role === "STUDENT" && u.status === "ACTIVE"
				).length
				totalLecturersCount = accountsData.filter((u: any) => u.role === "LECTURER").length
			}
		} catch (err: any) {
			errorMessage = err.message || "An error occurred while loading the dashboard."
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadData()
	})
</script>

<svelte:head>
	<title>Coordinator Dashboard - SEAL</title>
</svelte:head>

{#if isLoading}
	<div class="flex justify-center items-center h-[60vh]">
		<div
			class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"
		></div>
	</div>
{:else}
	{#if errorMessage}
		<div
			class="bg-(--md-error-container) border-l-4 border-(--md-error) p-4 rounded-r text-(--md-on-error-container) m-6"
		>
			<h3 class="text-sm font-bold">Error loading dashboard</h3>
			<p class="text-sm mt-1">{errorMessage}</p>
		</div>
	{:else if profile}
		<DashboardUI {profile} {seasonsCount} {activeParticipantsCount} {totalLecturersCount} />
	{/if}
{/if}
