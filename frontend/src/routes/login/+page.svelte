<main class="w-screen h-screen justify-center content-center bg-(--md-surface)">
	<div class="max-w-md w-full m-auto flex flex-col items-center rounded-md p-4">
		<h1 class="text-xl font-bold">Welcome back</h1>
		<p class="mb-6 text-(--md-surface-variant)">Login with your account</p>

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
				class="rounded-md p-2 font-semibold transition-all bg-(--md-primary-container) text-(--md-on-primary-container) hover:cursor-pointer hover:brightness-75"
			/>
		</form>

		<span class="mt-4"
			>Don't have an account yet? <a href="/register" class="text-blue-500 underline"
				>Register now</a
			></span
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
