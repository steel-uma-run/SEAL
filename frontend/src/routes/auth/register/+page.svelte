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

		if (isFptuStudent && !email.trim().endsWith("@fpt.edu.vn")) {
			errorMessage = "FPTU students must use a valid @fpt.edu.vn email address"
			return
		}

		const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
		if (!passwordRegex.test(password)) {
			errorMessage =
				"Password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character."
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
				errorMessage =
					errBody?.detail || errBody?.title || response?.statusText || "Registration failed!"
			}
		} catch (error) {
			console.error("Registration failed", error)
			errorMessage = "An error occurred. Please try again later."
		} finally {
			isLoading = false
		}
	}
</script>

<main class="min-w-screen min-h-screen flex justify-center items-center bg-(--md-surface) py-8">
	<div
		class="max-w-md w-full m-auto flex flex-col items-center rounded-2xl p-8 border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) transition-colors duration-300"
	>
		<h1 class="text-2xl font-black mb-6 text-(--md-on-surface)">Create an account</h1>

		<form class="w-full flex flex-col gap-4 font-sans" onsubmit={handleSubmit}>
			<div class="flex flex-col">
				<label for="name" class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5"
					>Name</label
				>
				<input
					id="name"
					class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
					type="text"
					placeholder="Enter your name"
					required
					bind:value={name}
				/>
			</div>

			<div class="flex flex-col">
				<label for="email" class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5"
					>Email</label
				>
				<input
					id="email"
					class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
					type="email"
					placeholder="Enter email"
					required
					bind:value={email}
				/>
			</div>

			<div class="flex flex-col">
				<label for="password" class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5"
					>Password</label
				>
				<div class="relative w-full">
					<input
						id="password"
						placeholder="Enter your password"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 pr-10 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
						type={showPassword ? "text" : "password"}
						required
						bind:value={password}
					/>
					<button
						type="button"
						onclick={togglePassword}
						class="absolute right-3 top-1/2 -translate-y-1/2 text-(--md-on-surface-variant) hover:text-(--md-on-surface) hover:cursor-pointer opacity-70 hover:opacity-100 transition-all"
						aria-label={showPassword ? "Hide password" : "Show password"}
					>
						{#if showPassword}
							<EyeOff size={20} />
						{:else}
							<Eye size={20} />
						{/if}
					</button>
				</div>
			</div>

			<div class="flex flex-col">
				<label
					for="confirmPassword"
					class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5"
					>Confirm password</label
				>
				<div class="relative w-full">
					<input
						id="confirmPassword"
						placeholder="Confirm your password"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 pr-10 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
						type={showConfirmPassword ? "text" : "password"}
						required
						bind:value={confirmPassword}
					/>
					<button
						type="button"
						onclick={toggleConfirmPassword}
						class="absolute right-3 top-1/2 -translate-y-1/2 text-(--md-on-surface-variant) hover:text-(--md-on-surface) hover:cursor-pointer opacity-70 hover:opacity-100 transition-all"
						aria-label={showConfirmPassword ? "Hide password" : "Show password"}
					>
						{#if showConfirmPassword}
							<EyeOff size={20} />
						{:else}
							<Eye size={20} />
						{/if}
					</button>
				</div>
			</div>

			<div class="mt-2 flex items-center gap-3">
				<div class="flex-1 flex flex-col">
					<label for="studentId" class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5"
						>Student ID</label
					>
					<input
						type="text"
						id="studentId"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
						placeholder="Enter Student ID"
						required
						bind:value={studentId}
					/>
				</div>
				<label
					class="flex cursor-pointer items-center gap-2 mt-6 text-sm text-(--md-on-surface-variant) whitespace-nowrap"
				>
					<input
						type="checkbox"
						class="h-5 w-5 rounded border-(--md-outline) bg-(--md-surface-bright) checked:bg-(--md-primary) checked:border-(--md-primary) accent-(--md-primary) focus:ring-0"
						bind:checked={isFptuStudent}
					/>
					I am a student of FPTU
				</label>
			</div>

			{#if !isFptuStudent}
				<div class="flex flex-col">
					<label
						for="schoolName"
						class="text-sm font-semibold text-(--md-on-surface-variant) mb-1.5">School Name</label
					>
					<input
						type="text"
						id="schoolName"
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
						placeholder="Enter your school name"
						required
						bind:value={schoolName}
					/>
				</div>
			{/if}

			{#if errorMessage}
				<span class="text-(--md-error) text-sm font-medium">{errorMessage}</span>
			{/if}

			<button
				type="submit"
				disabled={isLoading}
				class="mt-2 w-full bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 active:scale-98 rounded-xl py-3.5 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:cursor-pointer border-0"
			>
				{isLoading ? "Registering..." : "Register"}
			</button>
		</form>

		<span class="mt-6 text-sm text-(--md-on-surface-variant)">
			Already have an account?
			<a href="/auth/login" class="text-(--md-primary) hover:underline font-semibold">Login now</a>
		</span>
	</div>
</main>

<style>
	input::placeholder {
		color: var(--md-on-surface);
		opacity: 0.55;
	}
</style>
