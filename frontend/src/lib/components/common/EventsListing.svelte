<script lang="ts">
	import { onMount } from "svelte"
	import { getAllSeasons, getEventsInSeason } from "$lib/api"
	import { theme } from "$lib/theme.svelte"
	import { Calendar, ArrowRight, Tag, Clock } from "@lucide/svelte"

	let seasons = $state<any[]>([])
	let seasonEventsMap = $state<Record<string, { season: any; events: any[] }>>({})
	let isLoading = $state(true)
	let activeTab = $state<"current" | "upcoming" | "past">("current")

	const defaultBanners = [
		"https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=800&q=80",
		"https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80",
		"https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=800&q=80",
		"https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=800&q=80"
	]

	async function loadEventsData() {
		try {
			const seasonsRes = await getAllSeasons({ throwOnError: false })
			if (seasonsRes.response?.ok && seasonsRes.data) {
				seasons = seasonsRes.data

				let map: Record<string, { season: any; events: any[] }> = {}
				for (const season of seasons) {
					const eventsRes = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes.response?.ok && eventsRes.data) {
						const formattedEvents = eventsRes.data.map((evt, idx) => ({
							...evt,
							seasonSemester: season.semester,
							seasonYear: season.year,
							imageUrl: evt.imageUrl || defaultBanners[idx % defaultBanners.length]
						}))
						map[season.id] = {
							season,
							events: formattedEvents
						}
					}
				}
				seasonEventsMap = map
			}
		} catch (err) {
			console.error("Error loading events data:", err)
		} finally {
			isLoading = false
		}
	}

	function isEventInTab(event: any, tab: "current" | "upcoming" | "past"): boolean {
		const now = Date.now()
		const start = new Date(event.start_time).getTime()
		const end = new Date(event.end_time).getTime()

		if (tab === "current") {
			return (
				(now >= start && now <= end) || event.status === "ACTIVE" || event.status === "FINALIZED"
			)
		} else if (tab === "upcoming") {
			return start > now || event.status === "DRAFT" || event.status === "PENDING"
		} else if (tab === "past") {
			return end < now || event.status === "COMPLETED" || event.status === "ARCHIVED"
		}
		return true
	}

	const seasonsWithFilteredEvents = $derived.by(() => {
		const result: { season: any; events: any[] }[] = []
		for (const key in seasonEventsMap) {
			const group = seasonEventsMap[key]
			const matchingEvents = group.events.filter((evt) => isEventInTab(evt, activeTab))
			if (matchingEvents.length > 0) {
				result.push({
					season: group.season,
					events: matchingEvents
				})
			}
		}
		return result
	})

	onMount(() => {
		loadEventsData()
	})
</script>

<div class="events-listing-container {theme.darkMode ? 'events-listing--dark' : ''}">
	<div class="header">
		<div>
			<h1 class="header__title">Events</h1>
			<p class="header__subtitle">Explore hackathon events, themes, tracks, and details</p>
		</div>
	</div>

	<div class="tabs-nav">
		<button
			class="tab-btn {activeTab === 'current' ? 'tab-btn--active' : ''}"
			onclick={() => (activeTab = "current")}
		>
			Current events
		</button>
		<button
			class="tab-btn {activeTab === 'upcoming' ? 'tab-btn--active' : ''}"
			onclick={() => (activeTab = "upcoming")}
		>
			Upcoming events
		</button>
		<button
			class="tab-btn {activeTab === 'past' ? 'tab-btn--active' : ''}"
			onclick={() => (activeTab = "past")}
		>
			Past events
		</button>
	</div>

	{#if isLoading}
		<div class="loading-state">
			<div class="loading-spinner"></div>
		</div>
	{:else if seasonsWithFilteredEvents.length === 0}
		<div class="empty-state">
			<Calendar class="empty-state__icon" />
			<h3 class="empty-state__title">No Events Available</h3>
			<p class="empty-state__desc">There are no {activeTab} events to display at the moment.</p>
		</div>
	{:else}
		{#each seasonsWithFilteredEvents as group}
			<div class="season-section">
				<h2 class="season-header">{group.season.semester.toUpperCase()} {group.season.year}</h2>

				<div class="events-grid">
					{#each group.events as event}
						<div class="event-card">
							<div class="event-card__banner">
								<img src={event.imageUrl} alt={event.name} />
							</div>

							<div class="event-card__content">
								<h3 class="event-card__title">{event.name}</h3>
								<p class="event-card__desc">{event.description || "No description available."}</p>

								<div class="event-card__footer">
									<a href="/events/{event.id}" class="btn btn--details">
										Details <ArrowRight class="btn__icon" />
									</a>
								</div>
							</div>
						</div>
					{/each}
				</div>
			</div>
		{/each}
	{/if}
</div>

<style lang="scss">
	$bp-md: 768px;

	.events-listing-container {
		width: 100%;
	}

	.header {
		margin-bottom: 1.5rem;

		&__title {
			font-size: 2rem;
			font-weight: 800;
			color: #111827;

			.events-listing--dark & {
				color: #f4f4f5;
			}
		}

		&__subtitle {
			font-size: 0.875rem;
			color: #6b7280;

			.events-listing--dark & {
				color: #a1a1aa;
			}
		}
	}

	.tabs-nav {
		display: flex;
		gap: 1.5rem;
		border-bottom: 1px solid #e5e7eb;
		margin-bottom: 2rem;

		.events-listing--dark & {
			border-color: #27272a;
		}
	}

	.tab-btn {
		background: none;
		border: none;
		padding: 0.75rem 0.25rem;
		font-size: 0.95rem;
		font-weight: 600;
		color: #6b7280;
		cursor: pointer;
		position: relative;
		transition: all 0.2s ease;

		&:hover {
			color: #111827;
		}

		&--active {
			color: #ec4899; // accent color like screenshot 2
			font-weight: 700;

			&::after {
				content: "";
				position: absolute;
				bottom: -1px;
				left: 0;
				right: 0;
				height: 2px;
				background-color: #ec4899;
				border-radius: 999px;
			}
		}

		.events-listing--dark & {
			color: #a1a1aa;

			&:hover {
				color: #f4f4f5;
			}

			&--active {
				color: #f472b6;

				&::after {
					background-color: #f472b6;
				}
			}
		}
	}

	.season-section {
		margin-bottom: 2.5rem;
	}

	.season-header {
		font-size: 1.5rem;
		font-weight: 800;
		letter-spacing: 0.05em;
		color: #111827;
		margin-bottom: 1.25rem;

		.events-listing--dark & {
			color: #f4f4f5;
		}
	}

	.events-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 1.5rem;

		@media (min-width: 640px) {
			grid-template-columns: repeat(2, 1fr);
		}

		@media (min-width: 1024px) {
			grid-template-columns: repeat(3, 1fr);
		}
	}

	.event-card {
		background: #ffffff;
		border: 1px solid #e5e7eb;
		border-radius: 1.25rem;
		overflow: hidden;
		display: flex;
		flex-direction: column;
		box-shadow: 0 4px 15px rgba(0, 0, 0, 0.04);
		transition: all 0.2s ease;

		&:hover {
			transform: translateY(-4px);
			box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
		}

		.events-listing--dark & {
			background: #18181b;
			border-color: #27272a;
		}

		&__banner {
			width: 100%;
			height: 180px;
			overflow: hidden;

			img {
				width: 100%;
				height: 100%;
				object-fit: cover;
				transition: transform 0.3s ease;
			}

			&:hover img {
				transform: scale(1.05);
			}
		}

		&__content {
			padding: 1.25rem;
			display: flex;
			flex-direction: column;
			flex-grow: 1;
		}

		&__title {
			font-size: 1.125rem;
			font-weight: 700;
			color: #111827;
			margin-bottom: 0.5rem;

			.events-listing--dark & {
				color: #f4f4f5;
			}
		}

		&__desc {
			font-size: 0.875rem;
			color: #6b7280;
			line-height: 1.4;
			margin-bottom: 1.25rem;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
			flex-grow: 1;

			.events-listing--dark & {
				color: #a1a1aa;
			}
		}

		&__footer {
			margin-top: auto;
		}
	}

	.btn--details {
		display: inline-flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.5rem 1rem;
		background: #f3f4f6;
		color: #374151;
		font-size: 0.875rem;
		font-weight: 600;
		border-radius: 0.625rem;
		text-decoration: none;
		transition: all 0.2s ease;

		&:hover {
			background: #e5e7eb;
			color: #111827;
		}

		.events-listing--dark & {
			background: #27272a;
			color: #d4d4d8;

			&:hover {
				background: #3f3f46;
				color: #f4f4f5;
			}
		}

		.btn__icon {
			width: 1rem;
			height: 1rem;
		}
	}

	.empty-state {
		text-align: center;
		padding: 4rem 1rem;
		background: #ffffff;
		border: 1px dashed #e5e7eb;
		border-radius: 1.5rem;

		.events-listing--dark & {
			background: #18181b;
			border-color: #27272a;
		}

		&__icon {
			width: 3rem;
			height: 3rem;
			color: #9ca3af;
			margin-bottom: 1rem;
		}

		&__title {
			font-size: 1.25rem;
			font-weight: 700;
			color: #111827;
			margin-bottom: 0.25rem;

			.events-listing--dark & {
				color: #f4f4f5;
			}
		}

		&__desc {
			font-size: 0.875rem;
			color: #6b7280;

			.events-listing--dark & {
				color: #a1a1aa;
			}
		}
	}

	.loading-state {
		display: flex;
		justify-content: center;
		align-items: center;
		padding: 4rem 0;
	}

	.loading-spinner {
		width: 2.5rem;
		height: 2.5rem;
		border: 3px solid #e5e7eb;
		border-top-color: #ec4899;
		border-radius: 50%;
		animation: spin 0.8s linear infinite;

		.events-listing--dark & {
			border-color: #27272a;
			border-top-color: #f472b6;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
