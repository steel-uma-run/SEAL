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
			if (name.length <= 0) {
				errors.eventName = "Event name cannot be empty"
			}

			const seasonResp = await createSeason({
				body: {
					year: parseInt(year),
					semester: semester!
				}
			})

			if (!seasonResp.response.ok) {
				throw (seasonResp.data as unknown as CreateSeasonError).detail
			}

			const eventResp = await createEvent({
				body: {
					season_id: seasonResp.data.id,
					name: name,
					description: description,
					start_time: registrationStartTime.toISOString(),
					end_time: registrationEndTime.toISOString(),
					price: prize
				}
			})

			if (!eventResp.response.ok) {
				throw (eventResp.data as unknown as CreateEventError).detail
			}

			goto(`/events/${eventResp.data.id}`)
		} catch (err) {
			errors.generic = err as string
		}
	}
</script>

<div class="container">
	<h1>Create event</h1>

	<form onsubmit={handleSubmit} class="body">
		<div class="section">
			<div>
				<p>Event name</p>
				<TextFieldOutlined required minlength={1} label="" bind:value={name} />

				{#if errors.eventName}
					<span class="error">{errors.eventName}</span>
				{/if}
			</div>

			<div>
				<p>Prize pool</p>
				<TextFieldOutlined required minlength={1} label="" bind:value={prize} />
			</div>

			<div>
				<p>Semester</p>
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

			<div>
				<p>Year</p>
				<TextFieldOutlined required type="number" label="" min="2000" bind:value={year} />
			</div>
		</div>

		<div class="section">
			<div>
				<p>Registration start</p>
				<div class="input">
					<DateTimePicker bind:value={registrationStartTime} />
				</div>

				{#if errors.startTime}
					<span class="error">{errors.startTime}</span>
				{/if}
			</div>

			<div>
				<p>Registration end</p>
				<div class="input">
					<DateTimePicker bind:value={registrationEndTime} />
				</div>

				{#if errors.endTime}
					<span class="error">{errors.endTime}</span>
				{/if}
			</div>
		</div>

		<TextFieldOutlinedMultiline
			required
			minlength={1}
			label="Description"
			bind:value={description}
		/>

		<p style="text-align: end; margin-top: 1rem">
			You will be able to create tracks and rounds afterwards
		</p>

		<div class="create">
			<Button><Icon icon={iconCheck} /> Create</Button>
		</div>
	</form>

	{#if errors.generic}
		<p class="error">{errors.generic}</p>
	{/if}
</div>

<style lang="scss">
	.container {
		margin: auto;
		width: 100%;
	}

	h1 {
		margin-bottom: 2rem;
	}

	.body {
		display: flex;
		flex-direction: column;
		gap: 1rem;

		.section {
			display: flex;
			gap: 1rem;
		}
	}

	.input {
		display: flex;
		gap: 1rem;
	}

	.create {
		margin-top: 1rem;
		display: flex;
		justify-content: end;
	}

	.error {
		color: var(--md-sys-color-error);
	}
</style>
