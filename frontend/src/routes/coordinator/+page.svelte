<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { getProfile } from "$lib/api/profile";
    import DashboardUI from "$lib/components/coordinator/DashboardUI.svelte";

    let profile: any = $state(null);
    let isLoading = $state(true);
    let errorMessage = $state("");

    async function loadData() {
        try {
            const profileRes = await getProfile();
            if (!profileRes.ok) {
                if (profileRes.status === 401 || profileRes.status === 403) {
                    goto("/auth/login");
                    return;
                }
                throw new Error("Failed to load profile");
            }
            profile = await profileRes.json();
        } catch (err: any) {
            errorMessage = err.message || "An error occurred while loading the dashboard.";
        } finally {
            isLoading = false;
        }
    }

    onMount(() => {
        loadData();
    });
</script>

<svelte:head>
    <title>Coordinator Dashboard - SEAL</title>
</svelte:head>

{#if isLoading}
    <div class="flex justify-center items-center h-[60vh]">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"></div>
    </div>
{:else if errorMessage}
    <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm m-6">
        <h3 class="text-sm font-medium text-red-800">Error loading dashboard</h3>
        <p class="text-sm text-red-700 mt-1">{errorMessage}</p>
    </div>
{:else if profile}
    <DashboardUI {profile} />
{/if}
