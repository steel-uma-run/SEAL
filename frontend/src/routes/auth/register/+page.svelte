<script lang="ts">
	import { register } from "$lib/api"
	import { Eye, EyeOff } from "@lucide/svelte"
	import { goto } from "$app/navigation"

	let email = $state("")
	let name = $state("")
	let password = $state("")
	let confirmPassword = $state("")
	let isFptuStudent = $state(false)
	let studentId = $state("")
	let schoolName = $state("")

	let showPassword = $state(false)
	let showConfirmPassword = $state(false)
	let isLoading = $state(false)
	let errorMessage = $state("")
	function togglePassword() {
		showPassword = !showPassword
	}

	function toggleConfirmPassword() {
		showConfirmPassword = !showConfirmPassword
	}
	async function handleSubmit(event: Event) {
		event.preventDefault()
		errorMessage = ""

		if (password !== confirmPassword) {
			errorMessage = "Passwords do not match"
			return
		}
		let isExternal = !isFptuStudent
		isLoading = true
		try {
			const { response, error } = await register({
				body: {
					email: email.trim(),
					password,
					name,
					student_id: studentId.trim(),
					is_external: isExternal,
					school_name: isExternal ? schoolName.trim() : undefined
				},
				throwOnError: false
			})
			if (response?.ok) {
				goto("/auth/login")
			} else {
				const errBody = error as any
				errorMessage = errBody?.detail || errBody?.title || response?.statusText || "Registration failed!"
			}
		} catch (error) {
			console.error("Registration failed", error)
			errorMessage = "An error occurred. Please try again later."
		} finally {
			isLoading = false
		}
	}
</script>

<main class="min-w-screen min-h-screen justify-center content-center bg-(--md-surface)">
	<div class="max-w-md w-full m-auto flex flex-col items-center rounded-md p-4">
		<h1 class="text-xl font-bold mb-6">Create an account</h1>

		<form class="w-full flex flex-col gap-2" onsubmit={handleSubmit}>
			<label for="name" class="mb-1 mt-2 text-sm">Name</label>
			<input
				id="name"
				class="w-full rounded-md"
				type="text"
				placeholder="Enter your name"
				required
				bind:value={name}
			/>
			<label for="email" class="mb-1 mt-2 text-sm">Email</label>
			<input
				id="email"
				class="w-full rounded-md"
				type="email"
				placeholder="Enter email"
				required
				bind:value={email}
			/>
			<label for="password" class="mb-1 mt-2 text-sm">Password</label>
			<div class="relative w-full">
				<input
					id="password"
					placeholder="Enter your password"
					class="w-full rounded-md pr-10"
					type={showPassword ? "text" : "password"}
					required
					bind:value={password}
				/>
				<button
					type="button"
					onclick={togglePassword}
					class="absolute right-3 top-1/2 -translate-y-1/2 text-(--md-surface-variant) hover:text-(--md-on-surface) hover:cursor-pointer"
					aria-label={showPassword ? "Hide password" : "Show password"}
				>
					{#if showPassword}
						<EyeOff size={20} />
					{:else}
						<Eye size={20} />
					{/if}
				</button>
			</div>
			<label for="confirmPassword" class="mb-1 mt-2 text-sm">Confirm password</label>
			<div class="relative w-full">
				<input
					id="confirmPassword"
					placeholder="Confirm your password"
					class="w-full rounded-md pr-10"
					type={showConfirmPassword ? "text" : "password"}
					required
					bind:value={confirmPassword}
				/>
				<button
					type="button"
					onclick={toggleConfirmPassword}
					class="absolute right-3 top-1/2 -translate-y-1/2 text-(--md-surface-variant) hover:text-(--md-on-surface) hover:cursor-pointer"
					aria-label={showConfirmPassword ? "Hide password" : "Show password"}
				>
					{#if showConfirmPassword}
						<EyeOff size={20} />
					{:else}
						<Eye size={20} />
					{/if}
				</button>
			</div>
			<div class="mb-4 mt-2 flex items-center gap-3">
				<div class="flex-1">
					<label for="studentId" class="mb-1 block text-sm">Student ID</label>
					<input
						type="text"
						id="studentId"
						class="w-full rounded-md border border-gray-400 px-3 py-2"
						placeholder="Enter Student ID"
						required
						bind:value={studentId}
					/>
				</div>
				<label
					class="flex cursor-pointer items-center gap-2 mt-6 text-sm text-gray-800 whitespace-nowrap"
				>
					<input
						type="checkbox"
						class="h-4 w-4 rounded border-gray-400"
						bind:checked={isFptuStudent}
					/>
					I am a student of FPTU
				</label>
			</div>
			{#if !isFptuStudent}
				<div class="mb-4">
					<label for="schoolName" class="mb-1 block text-sm">School Name</label>
					<input
						type="text"
						id="schoolName"
						class="w-full rounded-md border border-gray-400 px-3 py-2"
						placeholder="Enter your school name"
						required
						bind:value={schoolName}
					/>
				</div>
			{/if}
			{#if errorMessage}
				<span class="text-red-500 text-sm">{errorMessage}</span>
			{/if}
			<button
				type="submit"
				disabled={isLoading}
				class="mt-2 rounded-md p-2 font-semibold transition-all bg-orange-500 text-white hover:cursor-pointer hover:brightness-75 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				{isLoading ? "Registering..." : "Register"}
			</button>
		</form>
		<span class="mt-4">
			Already have an account?
			<a href="/auth/login" class="text-blue-500 underline">Login now</a>
		</span>
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
