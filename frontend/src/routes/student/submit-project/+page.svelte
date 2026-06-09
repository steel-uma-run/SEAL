<script lang="ts">
	let submitLink = $state("")
	let youtubeLink = $state("")
	let slideLink = $state("")
	let submitMessage = $state("")
	let isLoading = $state(false)

	function handleSubmit(e: Event) {
		e.preventDefault()
		isLoading = true

		// Giả lập API call
		setTimeout(() => {
			submitMessage = "Submission successful!"
			isLoading = false
		}, 1000)
	}
</script>

<svelte:head>
	<title>Submit Project - SEAL</title>
</svelte:head>

<div class="max-w-2xl mx-auto w-full p-6 md:p-10">
	<a
		href="/dashboard"
		class="inline-flex items-center gap-2 text-gray-500 hover:text-blue-600 transition-colors mb-8 font-medium"
	>
		<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
			><path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path></svg
		>
		Back to Dashboard
	</a>

	<div
		class="bg-white p-8 md:p-10 rounded-3xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] border border-gray-100"
	>
		<div class="flex items-center gap-4 mb-8 border-b border-gray-100 pb-6">
			<div class="p-3 bg-blue-100 rounded-xl text-blue-600">
				<svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"
					><path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
					></path></svg
				>
			</div>
			<div>
				<h1 class="text-2xl font-bold text-gray-800">Submit Project</h1>
				<p class="text-sm text-gray-500 mt-1">Submit your team's final work for evaluation.</p>
			</div>
		</div>

		<form onsubmit={handleSubmit} class="flex flex-col gap-6">
			<div class="space-y-2">
				<label class="text-sm font-semibold text-gray-700">Git Link (Required)</label>
				<div class="relative">
					<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
						<svg class="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 24 24"
							><path
								fill-rule="evenodd"
								d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
								clip-rule="evenodd"
							/></svg
						>
					</div>
					<input
						type="url"
						bind:value={submitLink}
						required
						placeholder="https://github.com/..."
						class="pl-12 w-full rounded-xl border border-gray-200 bg-gray-50 p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none"
					/>
				</div>
			</div>

			<div class="space-y-2">
				<label class="text-sm font-semibold text-gray-700">YouTube Link (Optional)</label>
				<div class="relative">
					<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
						<svg class="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 24 24"
							><path
								d="M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z"
							/></svg
						>
					</div>
					<input
						type="url"
						bind:value={youtubeLink}
						placeholder="https://youtube.com/..."
						class="pl-12 w-full rounded-xl border border-gray-200 bg-gray-50 p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none"
					/>
				</div>
			</div>

			<div class="space-y-2">
				<label class="text-sm font-semibold text-gray-700">Presentation Slide Link</label>
				<div class="relative">
					<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
						<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"
							><path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"
							></path></svg
						>
					</div>
					<input
						type="url"
						bind:value={slideLink}
						placeholder="Google Slides or PPT link"
						class="pl-12 w-full rounded-xl border border-gray-200 bg-gray-50 p-3.5 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none"
					/>
				</div>
			</div>

			{#if submitMessage}
				<div
					class="p-4 rounded-xl text-sm font-medium bg-blue-50 text-blue-700 border border-blue-100 flex items-start gap-3"
				>
					<svg
						class="w-5 h-5 flex-shrink-0 mt-0.5"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
						><path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
						></path></svg
					>
					{submitMessage}
				</div>
			{/if}

			<button
				type="submit"
				disabled={isLoading}
				class="mt-4 w-full bg-blue-600 text-white rounded-xl py-4 font-bold text-lg shadow-sm hover:bg-blue-700 hover:shadow-md disabled:opacity-70 transition-all flex justify-center items-center gap-3"
			>
				{#if isLoading}
					<svg class="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
				{/if}
				{isLoading ? "Submitting..." : "Submit Project"}
			</button>
		</form>
	</div>
</div>
