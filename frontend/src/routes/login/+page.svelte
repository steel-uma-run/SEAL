<main class="w-screen h-screen justify-center content-center">
	<div class="max-w-md w-full m-auto flex flex-col items-center rounded-md p-4">
		<h1 class="text-xl font-bold">Welcome back</h1>
		<p class="mb-6" style="color: var(--md-on-surface-variant)">Login with your account</p>

		<form class="w-full flex flex-col gap-6">
			<label>
				<p>Email</p>
				<input class="w-full rounded-md" type="email" placeholder="Enter email" required />
			</label>

			<label>
				<span class="flex justify-between">
					<p>Password</p>
					<a href="/forgot-password" class="text-sm text-blue-500 underline">Forgot password?</a>
				</span>
				<input class="w-full rounded-md" type="password" required />
			</label>

			<input
				value="Login"
				type="submit"
				style="background-color: var(--md-primary-container); color: var(--md-on-primary-container)"
				class="rounded-md p-2 font-semibold hover:cursor-pointer hover:brightness-75 transition-all"
			/>
		</form>

		<span class="mt-4"
			>Don't have an account yet? <a href="/register" class="text-blue-500 underline"
				>Register now.</a
			></span
		>
	</div>
</main>

<style>
	main {
		background-color: var(--md-surface);
	}

	input {
		border-color: var(--md-outline);
	}
</style>
