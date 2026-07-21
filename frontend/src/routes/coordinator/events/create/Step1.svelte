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

	let name = $state("")
	let season = $state("SUMMER")
	let year = $state(new Date(Date.now()).getFullYear().toString())
	let registrationStartTime: Date | undefined = $state(undefined)
	let registrationEndTime: Date | undefined = $state(undefined)
</script>

<div class="container">
	<h1>Create event</h1>

	<div class="body">
		<div class="section">
			<div style="width: 100%">
				<p>Event name</p>
				<TextFieldOutlined required label="" bind:value={name} />
			</div>

			<div>
				<p>Season</p>
				<SelectOutlined
					label={""}
					options={[
						{ text: "Spring", value: "SPRING" },
						{ text: "Summer", value: "SUMMER" },
						{ text: "Fall", value: "FALL" }
					]}
					bind:value={season}
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
			</div>

			<div>
				<p>Registration end</p>
				<div class="input">
					<DateTimePicker bind:value={registrationEndTime} />
				</div>
			</div>
		</div>

		<TextFieldOutlinedMultiline required label="Description" />
	</div>

	<p style="text-align: end; margin-top: 1rem">
		You will be able to create tracks and rounds afterwards
	</p>
	<div class="create">
		<Button><Icon icon={iconCheck} /> Create</Button>
	</div>
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
</style>
