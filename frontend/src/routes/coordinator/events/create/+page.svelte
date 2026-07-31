<script lang="ts">
	import DateTimePicker from "$lib/components/DateTimePicker.svelte"
	import {
		Button,
		Icon,
		SelectOutlined,
		TextFieldOutlined,
		TextFieldOutlinedMultiline
	} from "m3-svelte"

	import iconCheck from "@ktibow/iconset-material-symbols/check"
	import {
		createEvent,
		createSeason,
		getAllSeasons,
		type CreateEventError,
		type CreateSeasonError,
		type Season
	} from "$lib/api"
	import { goto } from "$app/navigation"

	interface Errors {
		eventName?: string
		startTime?: string
		endTime?: string
		generic?: string
	}

	let name = $state("")
	let prize = $state("")
	let semester: Season["semester"] = $state("SPRING")
	let year = $state(new Date(Date.now()).getFullYear().toString())
	let description = $state("")
	let registrationDurationDays = $state(7)

	let errors: Errors = $state({})

	async function handleSubmit(e?: SubmitEvent) {
		if (e) e.preventDefault()
		errors = {}

		try {
			if (registrationDurationDays <= 0) {
				errors.eventName = "Duration must be greater than 0"
				return
			}
			if (name.trim().length <= 0) {
				errors.eventName = "Event name cannot be empty"
				return
			}

			let seasonId: string | undefined

			// Try to find if season already exists
			const seasonsResp = await getAllSeasons({ throwOnError: false })
			if (seasonsResp.response?.ok && seasonsResp.data) {
				const existingSeason = seasonsResp.data.find(
					(s) => s.semester === semester && s.year === parseInt(year)
				)
				if (existingSeason) {
					seasonId = existingSeason.id
				}
			}

			// If not found, create new season
			if (!seasonId) {
				const seasonResp = await createSeason({
					body: {
						year: parseInt(year),
						semester: semester!
					}
				})
				seasonId = seasonResp.data.id
			}

			const durationMs = registrationDurationDays * 24 * 60 * 60 * 1000

			const eventResp = await createEvent({
				body: {
					season_id: seasonId!,
					name: name,
					description: description,
					registration_duration: durationMs,
					prize: prize
				}
			})

			goto(`/coordinator/events/${eventResp.data.id}`)
		} catch (err: any) {
			errors.generic =
				err?.detail || err?.message || JSON.stringify(err) || "An unknown error occurred"
		}
	}
</script>

<div class="page-wrapper">
	<div class="container form-card">
		<h1 class="page-title">Create Event</h1>

		<form onsubmit={handleSubmit} class="body">
			<div class="field">
				<p class="label">Event name</p>
				<TextFieldOutlined required minlength={1} label="" bind:value={name} />
				{#if errors.eventName}
					<span class="error">{errors.eventName}</span>
				{/if}
			</div>

			<div class="field">
				<p class="label">Prize pool</p>
				<TextFieldOutlined required minlength={1} label="" bind:value={prize} />
			</div>

			<div class="field">
				<p class="label">Semester</p>
				<SelectOutlined
					label={""}
					options={[
						{ text: "Spring", value: "SPRING" },
						{ text: "Summer", value: "SUMMER" },
						{ text: "Fall", value: "FALL" }
					]}
					bind:value={semester!}
				/>
			</div>

			<div class="field">
				<p class="label">Year</p>
				<TextFieldOutlined required type="number" label="" min="2000" bind:value={year} />
			</div>

			<div class="field">
				<p class="label">Registration Duration (Days)</p>
				<TextFieldOutlined required type="number" label="" min="1" bind:value={registrationDurationDays} />
			</div>

			<div class="field full-width description-box">
				<p class="label">Description</p>
				<TextFieldOutlinedMultiline required minlength={1} label="" bind:value={description} />
			</div>

			<div class="full-width form-footer">
				<div class="footer-info">
					<Icon icon={iconCheck} style="color: var(--md-sys-color-primary, #6750a4);" />
					<p class="hint-text">You will be able to create tracks and rounds afterwards.</p>
				</div>

				<div class="create-btn">
					<Button><Icon icon={iconCheck} /> Create Event</Button>
				</div>
			</div>
		</form>

		{#if errors.generic}
			<div class="global-error-box">
				<p class="error">{errors.generic}</p>
			</div>
		{/if}
	</div>
</div>

<style lang="scss">
	/* Wrapper tạo màu nền xám nhạt cho toàn trang để nổi bật form */
	.page-wrapper {
		padding: 2rem;
		background-color: var(--md-sys-color-background);
		color: var(--md-sys-color-on-surface);
		min-height: calc(100vh - 64px);
		display: flex;
		justify-content: center;
	}

	.form-card {
		background: var(--md-sys-color-surface-container-low);
		color: var(--md-sys-color-on-surface);
		border-radius: 16px;
		box-shadow:
			0 10px 30px -5px rgba(0, 0, 0, 0.2),
			0 4px 6px -2px rgba(0, 0, 0, 0.1);
		padding: 2.5rem 3rem;
		border: 1px solid var(--md-sys-color-outline-variant);
		margin: 0;
		width: 100%;
		max-width: 900px;
	}

	.page-title {
		color: var(--md-sys-color-primary);
		font-size: 1.8rem;
		font-weight: 700;
		margin-top: 0;
		margin-bottom: 2.5rem;
		padding-bottom: 1rem;
		border-bottom: 2px solid var(--md-sys-color-outline-variant);
		position: relative;
	}

	.page-title::after {
		content: "";
		position: absolute;
		bottom: -2px;
		left: 0;
		width: 80px;
		height: 2px;
		background-color: var(--md-sys-color-primary);
	}

	.body {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 2rem 2.5rem;
	}

	.field {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		transition: transform 0.2s ease;
	}

	.label {
		margin: 0;
		font-weight: 600;
		font-size: 0.9rem;
		color: var(--md-sys-color-on-surface-variant);
		transition: color 0.2s ease;
	}

	.field:hover .label {
		color: var(--md-sys-color-primary);
	}

	.full-width {
		grid-column: 1 / -1;
	}

	.description-box {
		margin-top: 0.5rem;
	}

	.input-wrapper {
		display: flex;
		gap: 1rem;
	}

	.form-footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 1.5rem;
		padding-top: 1.5rem;
		border-top: 1px dashed var(--md-sys-color-outline-variant);
	}

	.footer-info {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		background: var(--md-sys-color-surface-container-high);
		padding: 0.75rem 1.25rem;
		border-radius: 8px;
	}

	.hint-text {
		margin: 0;
		color: var(--md-sys-color-on-surface-variant);
		font-size: 0.9rem;
		font-weight: 500;
	}

	.create-btn {
		transform: scale(1.05);
	}

	.error {
		color: var(--md-sys-color-error);
		font-size: 0.85rem;
		font-weight: 500;
		margin-top: 0.25rem;
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}

	.error::before {
		content: "•";
	}

	.global-error-box {
		margin-top: 2rem;
		background-color: var(--md-sys-color-error-container);
		border: 1px solid var(--md-sys-color-error);
		padding: 1rem;
		border-radius: 8px;
		text-align: center;
	}

	.global-error-box .error {
		color: var(--md-sys-color-on-error-container);
		font-size: 1rem;
		margin: 0;
		justify-content: center;
	}

	.global-error-box .error::before {
		display: none;
	}
</style>
