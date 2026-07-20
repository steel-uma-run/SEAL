<script lang="ts">
	import KaomojiError from "$lib/components/KaomojiError.svelte"
	import { Button, Tabs } from "m3-svelte"
</script>

<svelte:head>
	<title>Season Management - SEAL</title>
</svelte:head>

<div class="header">
	<div>
		<h1>Events management</h1>
		<p>Manage all hackathon events, timelines, tracks, and configurations.</p>
	</div>

	<div>
		<Button href="/coordinator/events/create">Create event</Button>
	</div>
</div>

<Tabs
	tab="all"
	items={[
		{ value: "all", name: "All" },
		{ value: "ongoing", name: "Ongoing" },
		{ value: "draft", name: "Draft" },
		{ value: "completed", name: "Completed" }
	]}
/>

<div class="kao">
	<KaomojiError kind="good" text="No events found. Start creating one now!" />
</div>

<style lang="scss">
	* {
		margin: 0;
	}

	.header {
		display: flex;
		justify-content: space-between;
		margin-bottom: 2rem;
	}

	.kao {
		margin-top: 2rem;
	}
</style>
