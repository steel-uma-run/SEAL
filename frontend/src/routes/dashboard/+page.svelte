<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { getProfile } from "$lib/api/profile";
    import { getSeasons } from "$lib/api/seasons";
    
    // Components
    import SeasonsList from "./SeasonsList.svelte";
    import StudentPanel from "./StudentPanel.svelte";
    import JudgePanel from "./JudgePanel.svelte";
    import MentorPanel from "./MentorPanel.svelte";
    import CoordinatorPanel from "./CoordinatorPanel.svelte";

    let profile: any = $state(null);
    let seasons: any[] = $state([]);
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

            await fetchSeasons();
        } catch (err: any) {
            errorMessage = err.message || "An error occurred while loading the dashboard.";
        } finally {
            isLoading = false;
        }
    }

    async function fetchSeasons() {
        const seasonsRes = await getSeasons();
        if (seasonsRes.ok) {
            seasons = await seasonsRes.json();
        }
    }

    onMount(() => {
        loadData();
    });
</script>

<svelte:head>
    <title>Dashboard - SEAL</title>
</svelte:head>

<div class="p-6 md:p-10 max-w-[1600px] mx-auto w-full">
    {#if isLoading}
        <div class="flex justify-center items-center h-[60vh]">
            <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-orange-500"></div>
        </div>
    {:else if errorMessage}
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm mb-6">
            <h3 class="text-sm font-medium text-red-800">Error loading dashboard</h3>
            <p class="text-sm text-red-700 mt-1">{errorMessage}</p>
        </div>
    {:else if profile}
        <!-- Top Header -->
        <header class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b border-gray-100 pb-6">
            <div>
                <h1 class="text-2xl md:text-3xl font-extrabold text-gray-800 tracking-tight">Welcome back, {profile.name}!</h1>
                <p class="text-gray-500 mt-1 text-sm md:text-base">Here is what's happening today.</p>
            </div>
            
            <div class="flex items-center gap-4 mt-4 md:mt-0">
                <div class="text-right hidden sm:block">
                    <p class="font-bold text-gray-800 leading-tight">{profile.name}</p>
                    <p class="text-xs font-semibold text-[#ea580c] uppercase tracking-wider">{profile.role}</p>
                </div>
                <div class="w-12 h-12 rounded-full bg-[#ffedd5] border border-[#fed7aa] flex items-center justify-center text-[#ea580c] font-bold text-xl shadow-sm">
                    {profile.name.charAt(0).toUpperCase()}
                </div>
            </div>
        </header>

        <!-- Stats Row -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-10">
            <!-- Stat 1 -->
            <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
                <div class="w-12 h-12 rounded-xl bg-[#ffedd5] text-[#ea580c] flex items-center justify-center shrink-0">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
                </div>
                <div>
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Current Season</p>
                    <h3 class="text-xl font-bold text-gray-800">{seasons.length > 0 ? seasons[0].name : 'Spring 2026'}</h3>
                </div>
            </div>

            <!-- Stat 2 -->
            <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
                <div class="w-12 h-12 rounded-xl bg-green-50 text-green-600 flex items-center justify-center shrink-0">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9"></path></svg>
                </div>
                <div>
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Registered Tracks</p>
                    <h3 class="text-xl font-bold text-gray-800">2</h3>
                </div>
            </div>

            <!-- Stat 3 -->
            <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
                <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                </div>
                <div>
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Team Status</p>
                    <h3 class="text-lg font-bold text-gray-800">Looking for team</h3>
                </div>
            </div>

            <!-- Stat 4 -->
            <div class="bg-white rounded-2xl p-6 border border-gray-100 shadow-[0_2px_10px_rgba(0,0,0,0.02)] flex items-center gap-5">
                <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                </div>
                <div>
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-1">Submission Due</p>
                    <h3 class="text-lg font-bold text-gray-800">In 5 days</h3>
                </div>
            </div>
        </div>

        <!-- Main Content Split -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
            <!-- Left Column: Tracks & Functional Panels -->
            <div class="xl:col-span-2 space-y-8">
                
                <!-- My Registered Tracks (UI Mockup matching image) -->
                <div class="bg-white p-8 rounded-3xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] border border-gray-100">
                    <div class="flex justify-between items-center mb-8">
                        <h2 class="text-xl font-bold text-gray-800">My Registered Tracks</h2>
                        <button class="bg-[#fff2e8] text-[#ea580c] px-4 py-2 rounded-lg text-sm font-semibold hover:bg-[#ffedd5] transition-colors">Join More</button>
                    </div>

                    <div class="space-y-4">
                        <!-- Track 1 -->
                        <div class="border border-gray-100 rounded-2xl p-6 flex gap-5 hover:border-[#fed7aa] hover:shadow-sm transition-all bg-white">
                            <div class="w-10 h-10 rounded-lg bg-[#fff2e8] text-[#ea580c] flex items-center justify-center shrink-0 mt-1">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
                            </div>
                            <div>
                                <h3 class="font-bold text-gray-800 text-lg">AI & Machine Learning Track</h3>
                                <p class="text-sm text-gray-500 mt-1">Build innovative AI solutions. Sponsored by Google.</p>
                                <div class="mt-3 inline-block bg-[#ecfdf5] text-[#059669] px-3 py-1 rounded-full text-xs font-semibold">Approved</div>
                            </div>
                        </div>

                        <!-- Track 2 -->
                        <div class="border border-gray-100 rounded-2xl p-6 flex gap-5 hover:border-blue-200 hover:shadow-sm transition-all bg-white">
                            <div class="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 mt-1">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
                            </div>
                            <div>
                                <h3 class="font-bold text-gray-800 text-lg">FinTech Innovation Track</h3>
                                <p class="text-sm text-gray-500 mt-1">Next generation financial applications. Sponsored by Stripe.</p>
                                <div class="mt-3 inline-block bg-[#fefce8] text-[#ca8a04] px-3 py-1 rounded-full text-xs font-semibold">Pending Approval</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Functional Tools (The previously built components) -->
                <div class="pt-4 border-t-2 border-dashed border-gray-200">
                    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard Actions</h2>
                    {#if profile.role === 'STUDENT'}
                        <StudentPanel {profile} {seasons} />
                    {:else if profile.role === 'JUDGE'}
                        <JudgePanel />
                    {:else if profile.role === 'MENTOR'}
                        <MentorPanel />
                    {:else if profile.role === 'COORDINATOR'}
                        <CoordinatorPanel {profile} {seasons} refreshSeasons={fetchSeasons} />
                    {:else}
                        <div class="bg-white/80 backdrop-blur-md p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center">
                            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                                <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            </div>
                            <h3 class="text-lg font-semibold text-gray-900">No specific tools available</h3>
                            <p class="text-gray-500 mt-2">Your role doesn't have any specific dashboard tools configured yet.</p>
                        </div>
                    {/if}
                </div>
            </div>

            <!-- Right Column: Timeline & Seasons -->
            <div class="xl:col-span-1 space-y-8">
                <!-- Timeline (UI Mockup matching image) -->
                <div class="bg-white p-8 rounded-3xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] border border-gray-100">
                    <h2 class="text-xl font-bold text-gray-800 mb-8">Hackathon Timeline</h2>
                    
                    <div class="space-y-6 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-gray-200 before:to-transparent">
                        
                        <div class="relative flex items-center gap-4">
                            <div class="w-10 h-10 rounded-full bg-white border-2 border-[#ea580c] text-[#ea580c] flex items-center justify-center shrink-0 z-10 shadow-sm">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            </div>
                            <div>
                                <h4 class="font-bold text-gray-800 text-sm">Team Formation Deadline</h4>
                                <p class="text-xs text-gray-500 mt-0.5">June 10, 2026</p>
                            </div>
                        </div>

                        <div class="relative flex items-center gap-4">
                            <div class="w-10 h-10 rounded-full bg-white border-2 border-[#ea580c] text-[#ea580c] flex items-center justify-center shrink-0 z-10 shadow-sm">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            </div>
                            <div>
                                <h4 class="font-bold text-gray-800 text-sm">Project Submission</h4>
                                <p class="text-xs text-gray-500 mt-0.5">June 13, 2026 - 11:59 PM</p>
                            </div>
                        </div>

                        <div class="relative flex items-center gap-4">
                            <div class="w-10 h-10 rounded-full bg-white border-2 border-gray-200 text-gray-400 flex items-center justify-center shrink-0 z-10">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            </div>
                            <div>
                                <h4 class="font-bold text-gray-800 text-sm">Pitching & Judging</h4>
                                <p class="text-xs text-gray-500 mt-0.5">June 15, 2026</p>
                            </div>
                        </div>

                    </div>
                </div>

                <!-- Available Seasons -->
                <SeasonsList />
            </div>
        </div>
    {/if}
</div>
