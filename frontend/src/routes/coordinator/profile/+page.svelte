<script lang="ts">
	import { onMount } from "svelte"
	import { getSelfProfile } from "$lib/api"
	import { goto } from "$app/navigation"
	import ProfileUI from "$lib/components/common/ProfileUI.svelte"

	let profile: any = $state(null)
	let isLoading = $state(true)
	let errorMessage = $state("")

	onMount(async () => {
		try {
			const { data, response: res } = await getSelfProfile({ throwOnError: false })
			if (res?.ok && data) {
				profile = data
			} else {
				errorMessage = "Failed to load profile. Please log in again."
				localStorage.removeItem("auth_data")
				goto("/auth/login")
			}
		} catch (error) {
			console.error("Error fetching profile:", error)
			errorMessage = "An error occurred while loading your profile."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>My Profile - Coordinator</title>
</svelte:head>

{#if isLoading}
	<div class="flex justify-center items-center h-[60vh]">
		<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"></div>
	</div>
{:else if errorMessage}
	<div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm m-6">
		<h3 class="text-sm font-medium text-red-800">Error loading profile</h3>
		<p class="text-sm text-red-700 mt-1">{errorMessage}</p>
	</div>
{:else if profile}
	<ProfileUI {profile} />
{/if}
