<script lang="ts">
	import { DateFieldOutlined, Button, TimePickerDial } from "m3-svelte"

	interface Props {
		value: Date | undefined
	}

	let open = $state(false)

	let rawDate: string | undefined = $state()
	let time: Date | undefined = $state()

	const date = $derived(rawDate ? new Date(rawDate) : undefined)
	const dateTime = $derived.by(() => {
		if (time === undefined || date === undefined) return undefined

		let datetime = new Date(date)
		datetime.setHours(time.getHours() ?? 0)
		datetime.setMinutes(time.getMinutes() ?? 0)

		return datetime
	})

	let { value = $bindable(), ...rest }: Props & DateFieldOutlined = $props()

	$effect(() => {
		value = dateTime
	})
</script>

<div>
	<DateFieldOutlined bind:value={rawDate} label={"Date"} {...rest} />
	<Button variant="outlined" onclick={() => (open = true)}
		>{time
			? time!.toLocaleTimeString(undefined, { hour: "numeric", minute: "numeric" })
			: "Pick time"}</Button
	>
</div>

{#if open}
	<TimePickerDial
		setTime={(timeStr) => {
			time = new Date(`1970-01-01T${timeStr}`)
		}}
		close={() => (open = false)}
	/>
{/if}

<style lang="scss">
	div {
		display: flex;
		align-items: center;
		gap: 1rem;
	}
</style>
