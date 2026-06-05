<script>
	import { Check } from "@lucide/svelte"
	import { Checkbox } from "bits-ui"
</script>

<main class="min-w-screen min-h-screen justify-center content-center bg-(--md-surface)">
	<div class="max-w-md w-full m-auto flex flex-col items-center rounded-md p-4">
		<h1 class="text-xl font-bold mb-6">Create an account</h1>

		<form class="w-full flex flex-col gap-6">
			<label>
				<p>Name</p>
				<input class="w-full rounded-md" type="text" placeholder="Enter your name" required />
			</label>

			<label>
				<p>Email</p>
				<input class="w-full rounded-md" type="email" placeholder="Enter email" required />
			</label>

			<!-- TODO: reveal password -->
			<div class="flex gap-4">
				<label>
					<p>Password</p>
					<input class="w-full rounded-md" type="password" required />
				</label>

				<label>
					<p>Confirm password</p>
					<input class="w-full rounded-md" type="password" required />
				</label>
			</div>

			<label>
				<p>Student ID</p>
				<input class="w-full rounded-md" type="text" required />
			</label>

			<label class="flex gap-2">
				<Checkbox.Root
					checked={true}
					class="rounded-md size-6 inline-flex justify-center transition-all data-[state=checked]:bg-(--md-surface-container) border border-(--md-outline-variant) active:scale-95 hover:cursor-pointer"
				>
					{#snippet children({ checked })}
						{#if checked}
							<Check class="text-(--md-secondary)" />
						{/if}
					{/snippet}
				</Checkbox.Root>
				<p>I am a student of FPTU</p>
			</label>

			<input
				value="Register"
				type="submit"
				class="rounded-md p-2 font-semibold transition-all bg-(--md-primary-container) text-(--md-on-primary-container) hover:cursor-pointer hover:brightness-75"
			/>
		</form>

		<span class="mt-4"
			>Already have an account? <a href="/login" class="text-blue-500 underline">Login now</a></span
		>
	</div>
</main>

<style>
	input {
		border-color: var(--md-outline);
	}

	input[type="email"],
	input[type="text"],
	input[type="password"] {
		background-color: var(--md-surface-bright);
		color: var(--md-on-surface);
	}

	input::placeholder {
		color: var(--md-on-surface);
		opacity: 0.5;
	}
</style>
