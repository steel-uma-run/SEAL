<script lang="ts">
	import { Button, TextField } from "m3-svelte"
	import { superForm } from "sveltekit-superforms"
	import { login } from "$lib/api/sdk.gen"
	import { goto } from "$app/navigation"
	import { zod4Client } from "sveltekit-superforms/adapters"
	import { zLoginRequestPayload } from "$lib/api/zod.gen"
	import { auth, token } from "$lib/auth.svelte"
	import type { LoginError } from "$lib/api/types.gen"

	let errorMessage: string | undefined = $state(undefined)
	let debounce = $state(false)

	let { data } = $props()
	const { form, enhance, constraints, errors } = superForm(data.form, {
		SPA: true,
		validationMethod: "oninput",
		validators: zod4Client(zLoginRequestPayload),

		async onSubmit(input) {
			debounce = true

			try {
				const resp = await login({
					body: {
						email: $form.email,
						password: $form.password
					}
				})

				if (!resp.response.ok) {
					throw (resp.data as unknown as LoginError).detail
				}

				token.value = resp.data.token
				auth.value = resp.data.user

				switch (resp.data.user.role) {
					case "STUDENT": {
						goto("/student")
						break
					}

					case "COORDINATOR": {
						goto("/coordinator")
						break
					}

					case "LECTURER": {
						goto("/lecturer")
						break
					}
				}
			} catch (err) {
				errorMessage = err as string
			} finally {
				debounce = false
			}
		}
	})
</script>

<main class="login-page">
	<div class="login-card">
		<h1>Welcome back</h1>
		<p class="subtitle">Login with your account</p>

		<form method="POST" use:enhance>
			<TextField
				label="Email"
				name="email"
				type="email"
				bind:value={$form.email}
				error={$errors.email !== undefined}
				{...$constraints.email}
			/>
			{#if $errors.email}
				<span style="color: var(--md-sys-color-error)">{$errors.email}</span>
			{/if}

			<TextField
				label="Password"
				name="password"
				type="password"
				bind:value={$form.password}
				error={$errors.password !== undefined}
				{...$constraints.password}
			/>
			{#if $errors.password}
				<span style="color: var(--md-sys-color-error)">{$errors.password}</span>
			{/if}

			<a href="/auth/forgot-password" class="forgot-link">Forgot password?</a>

			{#if errorMessage}
				<span class="error-message">{errorMessage}</span>
			{/if}

			<Button type="submit" style="width: 100%" disabled={debounce}>Login</Button>
		</form>

		<span class="bottom-text">
			Don't have an account yet?
			<a href="/auth/register" class="register-link">Register now</a>
		</span>
	</div>
</main>

<style>
	main.login-page {
		width: 100vw;
		height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--md-surface);

		.login-card {
			width: 100%;
			max-width: 28rem;
			margin: auto;
			display: flex;
			flex-direction: column;
			align-items: center;
			border-radius: 1rem;
		}

		h1 {
			margin-bottom: 0.5rem;
			font-size: 1.5rem;
			font-weight: 900;
			color: var(--md-on-surface);
		}

		.subtitle {
			margin-bottom: 1.5rem;
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
			text-align: center;
		}

		form {
			width: 100%;
			display: flex;
			flex-direction: column;
			gap: 1rem;
			font-family: inherit;
		}

		.forgot-link,
		.register-link {
			color: var(--md-sys-color-tertiary);
			font-weight: 600;

			&:hover {
				text-decoration: underline;
			}
		}

		.forgot-link {
			margin-top: 0.25rem;
			align-self: flex-end;
			font-size: 0.875rem;
		}

		.error-message {
			color: var(--md-error);
			font-size: 0.875rem;
			font-weight: 500;
		}

		.bottom-text {
			margin-top: 1.5rem;
			font-size: 0.875rem;
			color: var(--md-on-surface-variant);
		}
	}
</style>
