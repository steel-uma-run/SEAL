<script lang="ts">
	import { page } from "$app/stores"
	import { onMount } from "svelte"
	import { goto } from "$app/navigation"
	import { getSelfProfile, getAllSeasons, getEventsInSeason, getInterestedParticipants, markInterested } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { formatFullDate } from "$lib/utils/formatters.js"
	
	let eventId = $page.params.id
	let eventDetail: any = $state(null)
	let seasonId = $state("")
	let profile: any = $state(null)
	let isRegistered = $state(false)
	let isLoading = $state(true)
	let errorMessage = $state("")

	let isJoining = $state(false)
	let actionMessage = $state("")
	let actionError = $state(false)
	let studentIdResolved = $state("")

	async function resolveStudentId(email: string) {
		if (!email) return "MOCK-STUDENT"

		// 1. Try cached local storage
		const cachedKey = `student_id_${email}`
		if (typeof window !== "undefined") {
			const cached = localStorage.getItem(cachedKey)
			if (cached) {
				studentIdResolved = cached
				return cached
			}
		}

		// 2. Try extracting from FPT email pattern (e.g., cuongnhhe170560@fpt.edu.vn -> HE170560)
		const match = email.match(/[a-zA-Z]{2}\d{6}/)
		if (match) {
			const id = match[0].toUpperCase()
			if (typeof window !== "undefined") {
				localStorage.setItem(cachedKey, id)
			}
			studentIdResolved = id
			return id
		}

		// 3. Scan all seasons and events to locate the student's registration
		try {
			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			if (seasons) {
				for (const season of seasons) {
					const { data: events } = await getEventsInSeason({ path: { seasonId: season.id }, throwOnError: false })
					if (events) {
						for (const event of events) {
							const { data: participants } = await getInterestedParticipants({
								path: { seasonId: season.id, eventId: event.id },
								throwOnError: false
							})
							if (participants) {
								const self = participants.find((p: any) => p.email === email)
								if (self && (self.student_id || self.studentId)) {
									const id = self.student_id || self.studentId
									if (typeof window !== "undefined") {
										localStorage.setItem(cachedKey, id)
									}
									studentIdResolved = id
									return id
								}
							}
						}
					}
				}
			}
		} catch (e) {
			console.error("Error scanning student ID:", e)
		}

		// 4. Default fallback: use email prefix
		const prefix = email.split("@")[0].toUpperCase()
		studentIdResolved = prefix
		return prefix
	}

	async function handleJoinEvent() {
		isJoining = true
		actionMessage = ""
		actionError = false

		try {
			const { response } = await markInterested({
				path: { seasonId: seasonId, eventId: eventId },
				throwOnError: false
			})

			if (response?.ok) {
				isRegistered = true
				actionMessage = "Successfully joined the event! You can now create a team or join an existing team."
				actionError = false
			} else if (response?.status === 403) {
				actionMessage = "Only approved (active) students are allowed to join events. Please contact the coordinator."
				actionError = true
			} else {
				// Fallback to LocalStorage join for mock events (404/not found on backend)
				if (typeof window !== "undefined") {
					const key = `participants_${eventId}`
					const stored = localStorage.getItem(key)
					let list = stored ? JSON.parse(stored) : []
					if (!list.some((p: any) => p.email === profile.email)) {
						list.push({
							id: profile.id,
							email: profile.email,
							fullName: profile.fullName || profile.name,
							status: "ACTIVE",
							studentId: studentIdResolved || "MOCK-STUDENT",
							is_external: false
						})
						localStorage.setItem(key, JSON.stringify(list))
					}
					isRegistered = true
					actionMessage = "Successfully joined the mock event locally!"
					actionError = false
				} else {
					actionMessage = "Failed to join the event. Please try again."
					actionError = true
				}
			}
		} catch (error) {
			actionMessage = "An error occurred. Please try again later."
			actionError = true
		} finally {
			isJoining = false
		}
	}

	onMount(async () => {
		try {
			const { data: profileData, response: profileRes } = await getSelfProfile({ throwOnError: false })
			if (!profileRes?.ok || !profileData) {
				goto("/auth/login")
				return
			}
			profile = profileData
			await resolveStudentId(profile.email)
			
			const { data: seasons } = await getAllSeasons({ throwOnError: false })
			if (seasons) {
				for (const season of seasons) {
					// 1. Check LocalStorage first (for mock events created locally by coordinator)
					if (typeof window !== "undefined") {
						const key = `events_${season.id}`
						const stored = localStorage.getItem(key)
						if (stored) {
							const allLocalEvents = JSON.parse(stored)
							const found = allLocalEvents.find((e: any) => e.id === eventId)
							if (found) {
								eventDetail = found
								seasonId = season.id
								break
							}
						}
					}

					// 2. Fallback to API if not in LocalStorage
					const { data: events } = await getEventsInSeason({ path: { seasonId: season.id }, throwOnError: false })
					if (events) {
						const found = events.find((e: any) => e.id === eventId)
						if (found) {
							eventDetail = found
							seasonId = season.id
							break
						}
					}
				}
			}
			
			if (!eventDetail) {
				errorMessage = "Event not found."
			} else {
				// Check registration status from the API.
				const { data: participants } = await getInterestedParticipants({
					path: { seasonId: seasonId, eventId: eventId },
					throwOnError: false
				})
				if (participants) {
					isRegistered = participants.some((p: any) => p.email === profile.email)
				}
				
				// Also check LocalStorage participants if not registered on DB
				if (!isRegistered && typeof window !== "undefined") {
					const localParts = localStorage.getItem(`participants_${eventId}`)
					if (localParts) {
						const parsed = JSON.parse(localParts)
						if (parsed.some((p: any) => p.email === profile.email)) {
							isRegistered = true
						}
					}
				}
			}
		} catch (err) {
			errorMessage = "Error loading event details."
		} finally {
			isLoading = false
		}
	})
</script>

<svelte:head>
	<title>{eventDetail ? eventDetail.name : "Event Detail"} - SEAL</title>
</svelte:head>

<div class="max-w-4xl mx-auto w-full p-6 md:p-10">
	<button
		onclick={() => history.back()}
		class="inline-flex items-center gap-2 transition-colors mb-8 font-medium bg-transparent border-0 cursor-pointer text-(--md-on-surface-variant) hover:text-(--md-primary)"
	>
		<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
		</svg>
		Back
	</button>

	{#if isLoading}
		<div class="flex justify-center py-20">
			<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-(--md-primary)"></div>
		</div>
	{:else if errorMessage}
		<div class="bg-(--md-error-container) text-(--md-on-error-container) p-6 rounded-2xl border border-(--md-error)">
			<h2 class="text-lg font-bold">Error</h2>
			<p>{errorMessage}</p>
		</div>
	{:else if eventDetail}
		<div class="p-8 md:p-10 rounded-3xl transition-all border bg-(--md-surface-container-low) border-(--md-outline-variant) shadow-sm">
			{#if actionMessage}
				<div class="mb-6 p-4 rounded-xl border text-sm font-medium {actionError ? 'bg-(--md-error-container) text-(--md-on-error-container) border-(--md-error)' : 'bg-(--md-secondary-container) text-(--md-on-secondary-container) border-(--md-outline-variant)'}">
					{actionMessage}
				</div>
			{/if}

			<div class="mb-8 border-b pb-8 border-(--md-outline-variant) flex flex-col md:flex-row md:items-center justify-between gap-6">
				<div class="flex-1">
					<h1 class="text-3xl font-extrabold text-(--md-on-surface)">
						{eventDetail.name}
					</h1>
					<p class="mt-4 text-lg leading-relaxed text-(--md-on-surface-variant)">
						{eventDetail.description}
					</p>
				</div>
				<div class="shrink-0">
					{#if profile && profile.role === "STUDENT"}
						{#if profile.status !== "ACTIVE"}
							<div class="flex flex-col items-end gap-1.5">
								<button
									disabled
									class="px-8 py-3 rounded-xl text-sm font-bold bg-(--md-surface-container-highest) text-(--md-on-surface-variant) border border-(--md-outline-variant) cursor-not-allowed flex items-center gap-2"
								>
									Awaiting Approval
								</button>
								<span class="text-[10px] text-(--md-error) font-semibold max-w-[200px] text-right">
									Requires active student status
								</span>
							</div>
						{:else if isRegistered}
							<div class="inline-flex items-center gap-2 px-6 py-3 rounded-xl text-sm font-bold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
								<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
								</svg>
								Joined Event
							</div>
						{:else}
							<button
								onclick={handleJoinEvent}
								disabled={isJoining}
								class="px-8 py-3 rounded-xl text-sm font-bold bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 disabled:opacity-50 transition-all cursor-pointer border-0 flex items-center gap-2"
							>
								{#if isJoining}
									<div class="animate-spin rounded-full h-4 w-4 border-2 border-current border-t-transparent"></div>
									Joining...
								{:else}
									Join Event
								{/if}
							</button>
						{/if}
					{/if}
				</div>
			</div>

			<div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
				<div class="p-5 rounded-2xl border bg-(--md-surface-container) border-(--md-outline-variant)">
					<p class="text-sm font-semibold uppercase tracking-wider mb-2 text-(--md-on-surface-variant)">
						Event Start
					</p>
					<p class="font-bold text-lg text-(--md-on-surface)">
						{formatFullDate(eventDetail.start_time || eventDetail.startTime)}
					</p>
				</div>
				<div class="p-5 rounded-2xl border bg-(--md-secondary-container) border-(--md-outline-variant)">
					<p class="text-sm font-semibold uppercase tracking-wider mb-2 text-(--md-primary)">
						Submission Deadline
					</p>
					<p class="font-bold text-lg text-(--md-on-surface) flex items-center gap-2">
						<svg class="w-5 h-5 text-(--md-primary)" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
						{formatFullDate(eventDetail.end_time || eventDetail.endTime)}
					</p>
					<p class="text-xs text-(--md-on-surface-variant) mt-1">
						All work must be submitted by this accurate deadline pulled from the event configuration.
					</p>
				</div>
			</div>

			{#if eventDetail.tracks && eventDetail.tracks.length > 0}
				<div>
					<h3 class="text-xl font-bold mb-4 text-(--md-on-surface)">Available Tracks</h3>
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each eventDetail.tracks as track}
							<div class="p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-surface)">
								<h4 class="font-bold text-md text-(--md-on-surface)">{track.name}</h4>
								<p class="text-sm mt-1 text-(--md-on-surface-variant)">{track.description}</p>
							</div>
						{/each}
					</div>
				</div>
			{/if}
		</div>
	{/if}
</div>
