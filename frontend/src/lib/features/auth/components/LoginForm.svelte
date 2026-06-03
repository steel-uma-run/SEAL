<script lang="ts">
	import { login } from "../api"
	import { authState } from "../auth.svelte"
	import { goto } from "$app/navigation"
	import { toast } from "$lib/components/toast.svelte"

	let email = $state("")
	let password = $state("")

	async function handleSubmit(event: Event) {
		event.preventDefault()

		const params = new URLSearchParams()
		params.append("email", email)
		params.append("password", password)

		try {
			const data = await login(params)
			authState.login(data.user, data.token)
			toast.success("Đăng nhập thành công! Xin chào " + data.user.full_name)
			goto("/dashboard")
		} catch (error: any) {
			toast.error("Lỗi: " + error.message)
		}
	}
</script>

<div class="flex flex-col items-center justify-center">
	<div class="flex flex-col items-center justify-center mb-6">
		<div class="flex items-center">
			<img
				src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
				alt="FPT Logo"
				class="h-10 w-auto"
			/>
			<span class="ml-1 text-sm font-semibold text-[#002f6c] dark:text-blue-400">Education</span>
		</div>
		<span
			class="text-orange-600 dark:text-orange-500 font-extrabold text-xl mt-1 tracking-wider uppercase"
			>FPT University</span
		>
	</div>
	<h2 class="mt-2 text-center text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight">
		Đăng nhập
	</h2>
	<p class="mt-2 text-sm text-gray-500 dark:text-white">Chào mừng bạn trở lại</p>
</div>

<form class="mt-8 space-y-5" onsubmit={handleSubmit}>
	<div class="space-y-4">
		<div>
			<label for="email" class="block text-sm font-medium text-gray-700 dark:text-white mb-1"
				>Email</label
			>
			<input
				id="email"
				type="email"
				required
				bind:value={email}
				class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
				placeholder="you@example.com"
			/>
		</div>

		<div>
			<label for="password" class="block text-sm font-medium text-gray-700 dark:text-white mb-1"
				>Mật khẩu</label
			>
			<input
				id="password"
				type="password"
				required
				bind:value={password}
				class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
				placeholder="••••••••"
			/>
		</div>
	</div>

	<div class="pt-2">
		<button
			type="submit"
			class="w-full flex justify-center py-3.5 px-4 text-sm font-bold rounded-xl text-white bg-orange-600 hover:bg-orange-700 transition-colors focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900"
		>
			Đăng nhập
		</button>
	</div>
</form>

<div class="text-center mt-6">
	<p class="text-gray-500 dark:text-white text-sm">
		Chưa có tài khoản?
		<a
			href="/register"
			class="font-bold text-orange-600 hover:text-orange-700 dark:text-orange-500 dark:hover:text-orange-400 transition-colors ml-1"
		>
			Đăng ký ngay
		</a>
	</p>
</div>
