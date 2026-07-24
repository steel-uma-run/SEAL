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
	let registrationStartTime: Date | undefined = $state(undefined)
	let registrationEndTime: Date | undefined = $state(undefined)

	let errors: Errors = $state({})

	async function handleSubmit() {
		errors = {}

		try {
			if (registrationStartTime === undefined) {
				errors.startTime = "Select a time"
				return
			}
			if (registrationEndTime === undefined) {
				errors.endTime = "Select a time"
				return
			}
			if (registrationStartTime >= registrationEndTime) {
				errors.startTime = "Start time cannot be after end time"
				errors.endTime = errors.startTime
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

			const eventResp = await createEvent({
				body: {
					season_id: seasonId!,
					name: name,
					description: description,
					start_time: registrationStartTime.toISOString(),
					end_time: registrationEndTime.toISOString(),
					price: prize
				}
			})

			goto(`/events/${eventResp.data.id}`)
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
				<p class="label">Registration start</p>
				<div class="input-wrapper">
					<DateTimePicker bind:value={registrationStartTime} />
				</div>
				{#if errors.startTime}
					<span class="error">{errors.startTime}</span>
				{/if}
			</div>

			<div class="field">
				<p class="label">Registration end</p>
				<div class="input-wrapper">
					<DateTimePicker bind:value={registrationEndTime} />
				</div>
				{#if errors.endTime}
					<span class="error">{errors.endTime}</span>
				{/if}
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
		background-color: #f8fafc;
		min-height: calc(100vh - 64px);
		display: flex;
		justify-content: center;
	}

	/* Hiệu ứng thẻ (Card) với đổ bóng mượt và bo góc */
	.form-card {
		background: #ffffff;
		border-radius: 16px;
		box-shadow:
			0 10px 30px -5px rgba(0, 0, 0, 0.08),
			0 4px 6px -2px rgba(0, 0, 0, 0.04);
		padding: 2.5rem 3rem;
		border: 1px solid #f1f5f9;
		margin: 0;
		width: 100%;
		max-width: 900px;
	}

	/* Tiêu đề có điểm nhấn màu sắc */
	.page-title {
		color: var(--md-sys-color-primary, #8a2be2);
		font-size: 1.8rem;
		font-weight: 700;
		margin-top: 0;
		margin-bottom: 2.5rem;
		padding-bottom: 1rem;
		border-bottom: 2px solid #e2e8f0;
		position: relative;
	}

	.page-title::after {
		content: "";
		position: absolute;
		bottom: -2px;
		left: 0;
		width: 80px;
		height: 2px;
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

	/* Label màu dịu hơn, đổi màu khi hover vào field */
	.label {
		margin: 0;
		font-weight: 600;
		font-size: 0.9rem;
		color: #64748b;
		transition: color 0.2s ease;
	}

	.field:hover .label {
		color: var(--md-sys-color-primary, #8a2be2);
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

	/* Tút lại phần Footer của form */
	.form-footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 1.5rem;
		padding-top: 1.5rem;
		border-top: 1px dashed #cbd5e1;
	}

	.footer-info {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		background: #f1f5f9;
		padding: 0.75rem 1.25rem;
		border-radius: 8px;
	}

	.hint-text {
		margin: 0;
		color: #475569;
		font-size: 0.9rem;
		font-weight: 500;
	}

	.create-btn {
		/* Phóng to nút một chút để tăng tính tương tác */
		transform: scale(1.05);
	}

	.error {
		color: var(--md-sys-color-error, #ef4444);
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
		background-color: #fef2f2;
		border: 1px solid #fca5a5;
		padding: 1rem;
		border-radius: 8px;
		text-align: center;
	}

	.global-error-box .error {
		font-size: 1rem;
		margin: 0;
		justify-content: center;
	}

	.global-error-box .error::before {
		display: none;
	}
</style>
