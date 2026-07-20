<script lang="ts">
	import { superForm } from "sveltekit-superforms"

	import Step1 from "./Step1.svelte"

	let step = $state(0)

	let { data } = $props()

	const { form } = superForm(data.form)
</script>

{#if step == 0}
	<Step1 {form} />
{/if}

<style lang="scss">
	* {
		margin: 0;
	}
</style>
