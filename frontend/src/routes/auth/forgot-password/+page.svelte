<script lang="ts">
	import { theme } from "$lib/theme.svelte"
	import { Mail, CheckCircle2, ArrowLeft } from "@lucide/svelte"

	let email = $state("")
	let isLoading = $state(false)
	let message = $state("")
	let isSuccess = $state(false)

	function handleResetPassword(event: Event) {
		event.preventDefault()
		isLoading = true
		message = ""

		// Simulating backend request for password reset
		setTimeout(() => {
			isLoading = false
			isSuccess = true
			message =
				"If this email is registered in our system, you will receive a reset password link shortly."
		}, 1500)
	}
</script>

<svelte:head>
	<title>Forgot Password - SEAL</title>
</svelte:head>

<main class="w-screen h-screen flex justify-center items-center bg-(--md-surface)">
	<div
		class="max-w-md w-full m-auto flex flex-col items-center rounded-2xl p-8 border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) transition-colors duration-300"
	>
		<a
			href="/auth/login"
			class="self-start flex items-center gap-2 mb-6 text-sm text-(--md-primary) hover:underline font-semibold"
		>
			<ArrowLeft size={16} />
			Back to login
		</a>

		<h1 class="text-2xl font-black mb-2">Reset Password</h1>
		<p class="mb-6 text-center text-sm text-(--md-on-surface-variant) leading-relaxed">
			Enter your registered email address below, and we will send you instructions to reset your
			password.
		</p>

		{#if isSuccess}
			<div
				class="w-full p-4 rounded-xl border border-(--md-outline-variant) bg-(--md-secondary-container) text-(--md-on-secondary-container) flex gap-3 mb-6"
			>
				<CheckCircle2 class="w-5 h-5 flex-shrink-0 mt-0.5 text-(--md-primary)" />
				<p class="text-sm font-medium leading-relaxed">{message}</p>
			</div>
		{:else}
			<form class="w-full flex flex-col gap-4" onsubmit={handleResetPassword}>
				<div class="space-y-1.5 font-sans">
					<label for="email" class="text-sm font-semibold text-(--md-on-surface-variant)">Email Address</label>
					<div class="relative w-full">
						<input
							id="email"
							class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 pl-10 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
							type="email"
							placeholder="Enter your email"
							required
							bind:value={email}
						/>
						<span class="absolute left-3 top-1/2 -translate-y-1/2 text-(--md-on-surface-variant) opacity-70">
							<Mail size={16} />
						</span>
					</div>
				</div>

				{#if message}
					<span class="text-(--md-error) text-sm font-medium">{message}</span>
				{/if}

				<button
					type="submit"
					disabled={isLoading}
					class="mt-2 w-full bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 active:scale-98 rounded-xl py-3.5 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:cursor-pointer border-0"
				>
					{isLoading ? "Sending link..." : "Send Reset Link"}
				</button>
			</form>
		{/if}
	</div>
</main>

<style>
	input::placeholder {
		color: var(--md-on-surface);
		opacity: 0.55;
	}
</style>
