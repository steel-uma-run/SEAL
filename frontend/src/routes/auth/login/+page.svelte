<script lang="ts">
	import { login, getSelfProfile } from "$lib/api"
	import { goto } from "$app/navigation"
	import { Eye, EyeOff } from "@lucide/svelte"

	let email = $state("")
	let password = $state("")
	let showPassword = $state(false)
	let isLoading = $state(false)
	let errorMessage = $state("")

	function togglePassword() {
		showPassword = !showPassword
	}

	async function handleLogin(event: Event) {
		event.preventDefault()
		isLoading = true
		errorMessage = ""

		try {
			// Truyền tham số dưới dạng object
			const { data } = await login({
				body: {
					email: email.trim(),
					password: password
				}
			})

			// Nếu thành công, data đã được parse sẵn
			if (data) {
				localStorage.setItem("auth_data", JSON.stringify(data))

				// Gọi hàm getSelfProfile
				const { data: profile } = await getSelfProfile()

				if (profile) {
					if (profile.role === "COORDINATOR") {
						goto("/coordinator")
					} else if (profile.role === "STUDENT") {
						goto("/student")
					} else if (profile.role === "LECTURER") {
						goto("/lecturer")
					} else {
						// Handle other roles
					}
				}
			} // Đã bổ sung dấu đóng ngoặc cho if (data)
		} catch (err: any) {
			console.error("Lỗi đăng nhập", err)
			let detail = ""
			if (err.body) {
				detail = err.body.detail || err.body.title || JSON.stringify(err.body)
			} else if (err.message) {
				detail = err.message
			} else {
				detail = JSON.stringify(err)
			}
			errorMessage = `Lỗi: ${detail}`
		} finally {
			isLoading = false
		}
	}
</script>

<main class="w-screen h-screen flex justify-center items-center bg-(--md-surface)">
	<div
		class="max-w-md w-full m-auto flex flex-col items-center rounded-2xl p-8 border border-(--md-outline-variant) bg-(--md-surface-container-low) text-(--md-on-surface) transition-colors duration-300"
	>
		<h1 class="text-2xl font-black mb-2 text-(--md-on-surface)">Welcome back</h1>
		<p class="mb-6 text-sm text-(--md-on-surface-variant) text-center">Login with your account</p>

		<form class="w-full flex flex-col gap-4 font-sans" onsubmit={handleLogin}>
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
						class="w-full rounded-xl border border-(--md-outline) bg-(--md-surface-bright) text-(--md-on-surface) p-3 pr-10 focus:ring-2 focus:ring-(--md-primary) transition-all outline-none"
						type={showPassword ? "text" : "password"}
						required
						placeholder="Enter your password"
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
			<a
				href="/auth/forgot-password"
				class="mt-1 self-end text-sm text-(--md-primary) hover:underline font-semibold"
			>
				Forgot password?
			</a>

			{#if errorMessage}
				<span class="text-(--md-error) text-sm font-medium">{errorMessage}</span>
			{/if}

			<button
				type="submit"
				disabled={isLoading}
				class="mt-2 w-full bg-(--md-primary) text-(--md-on-primary) hover:opacity-90 active:scale-98 rounded-xl py-3.5 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:cursor-pointer border-0"
			>
				{isLoading ? "Logging in..." : "Login"}
			</button>
		</form>

		<span class="mt-6 text-sm text-(--md-on-surface-variant)">
			Don't have an account yet?
			<a href="/auth/register" class="text-(--md-primary) hover:underline font-semibold"
				>Register now</a
			>
		</span>
	</div>
</main>

<style>
	input::placeholder {
		color: var(--md-on-surface);
		opacity: 0.55;
	}
</style>
