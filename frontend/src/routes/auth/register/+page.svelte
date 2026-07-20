<script lang="ts">
	import { register } from "$lib/api"
	import { goto } from "$app/navigation"
	import { Button, Checkbox, TextField } from "m3-svelte"

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

<main class="register-page">
	<div class="register-card">
		<h1>Create an account</h1>

		<form onsubmit={handleSubmit}>
			<TextField label="Name" required bind:value={name} />
			<TextField label="Email" required type="email" bind:value={email} />

			<TextField label="Password" required type="password" bind:value={password} />
			<TextField label="Confirm password" required type="password" bind:value={confirmPassword} />

			<div class="inline-row">
				<TextField label="Student ID" required bind:value={studentId} />

				<label>
					<Checkbox>
						<input type="checkbox" bind:checked={isFptuStudent} />
					</Checkbox>
					I am a student of FPTU
				</label>
			</div>

			{#if !isFptuStudent}
				<TextField label="School name" required bind:value={schoolName} />
			{/if}

			{#if errorMessage}
				<span class="error-message">{errorMessage}</span>
			{/if}

			<Button type="submit" disabled={isLoading}>{isLoading ? "Registering..." : "Register"}</Button
			>
		</form>

		<span class="bottom-text">
			Already have an account?
			<a href="/auth/login" class="auth-link">Login now</a>
		</span>
	</div>
</main>

<style>
	main.register-page {
		min-width: 100vw;
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--md-surface);

		.register-card {
			width: 100%;
			max-width: 28rem;
			margin: auto;
			display: flex;
			flex-direction: column;
			align-items: center;
		}

		h1 {
			margin-bottom: 1.5rem;
			font-size: 1.5rem;
			font-weight: 900;
			color: var(--md-on-surface);
		}

		form {
			width: 100%;
			display: flex;
			flex-direction: column;
			gap: 1rem;
			font-family: inherit;
		}

		.field {
			display: flex;
			flex-direction: column;
		}

		label {
			margin-bottom: 0.375rem;
			font-size: 0.875rem;
			font-weight: 600;
			color: var(--md-on-surface-variant);
		}

		input[type="text"],
		input[type="email"],
		input[type="password"] {
			width: 100%;
			padding: 0.75rem;
			border: 1px solid var(--md-outline);
			border-radius: 0.75rem;
			background: var(--md-surface-bright);
			color: var(--md-on-surface);
			outline: none;
			transition: all 150ms;

			&:focus {
				box-shadow: 0 0 0 2px var(--md-primary);
			}
		}

		.password-wrap {
			position: relative;
			width: 100%;
		}

		.password-input {
			padding-right: 2.5rem;
		}

		.toggle-password {
			position: absolute;
			right: 0.75rem;
			top: 50%;
			transform: translateY(-50%);
			color: var(--md-on-surface-variant);
			opacity: 0.7;
			transition: all 150ms;
			cursor: pointer;

			&:hover {
				color: var(--md-on-surface);
				opacity: 1;
			}
		}

		.inline-row {
			margin-top: 0.5rem;
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}

		.student-field {
			flex: 1;
			display: flex;
			flex-direction: column;
		}

		.checkbox-label {
			display: flex;
			align-items: center;
			gap: 0.5rem;
			margin-top: 1.5rem;
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
			white-space: nowrap;
			cursor: pointer;
		}

		.checkbox-label input[type="checkbox"] {
			width: 1.25rem;
			height: 1.25rem;
			border-radius: 0.25rem;
			border: 1px solid var(--md-outline);
			background: var(--md-surface-bright);
			accent-color: var(--md-primary);
			flex-shrink: 0;
		}

		.error-message {
			color: var(--md-error);
			font-size: 0.875rem;
			font-weight: 500;
		}

		.submit-button {
			width: 100%;
			margin-top: 0.5rem;
			padding: 0.875rem 1rem;
			border: 0;
			border-radius: 0.75rem;
			background: var(--md-primary);
			color: var(--md-on-primary);
			font-weight: 700;
			transition: all 150ms;
			cursor: pointer;

			&:hover {
				opacity: 0.9;
			}

			&:active {
				transform: scale(0.98);
			}

			&:disabled {
				opacity: 0.5;
				cursor: not-allowed;
				transform: none;
			}
		}

		.bottom-text {
			margin-top: 1.5rem;
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
		}

		.auth-link {
			color: var(--md-primary);
			font-weight: 600;

			&:hover {
				text-decoration: underline;
			}
		}
	}
</style>
