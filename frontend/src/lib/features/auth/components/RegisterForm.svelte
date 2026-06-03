<script lang="ts">
	import { register } from "../api"
	import { goto } from "$app/navigation"
	import { toast } from "$lib/components/toast.svelte"

	let email = $state("")
	let password = $state("")
	let confirmPassword = $state("")
	let fullName = $state("")
	let studentCode = $state("")
	let studentType = $state<"FPT" | "EXTERNAL" | "">("")

	async function handleSubmit(event: Event) {
		event.preventDefault()

		if (password !== confirmPassword) {
			toast.error("Mật khẩu xác nhận không khớp!")
			return
		}

		const params = new URLSearchParams()
		params.append("email", email)
		params.append("name", fullName)
		params.append("password", password)
		params.append("external", String(studentType === "EXTERNAL"))

		try {
			await register(params)
			toast.success("Đăng ký thành công! Vui lòng đăng nhập.")
			goto("/login")
		} catch (error: any) {
			toast.error("Lỗi đăng ký: " + error.message)
		}
	}
</script>

<div class="flex flex-col items-center justify-center">
	<div class="flex flex-col items-center justify-center mb-6">
		<div class="flex flex-col items-center justify-center">
			<div class="flex flex-col items-center justify-center mb-6">
				<div class="flex items-center">
					<img
						src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
						alt="FPT Logo"
						class="h-10 w-auto"
					/>
					<span class="ml-1 text-sm font-semibold text-[#002f6c] dark:text-blue-400">Education</span
					>
				</div>
				<span
					class="text-orange-600 dark:text-orange-500 font-extrabold text-xl mt-1 tracking-wider uppercase"
					>FPT University</span
				>
			</div>
		</div>

		<h2
			class="mt-2 text-center text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight"
		>
			Tạo tài khoản
		</h2>
		<p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Tham gia cộng đồng của chúng tôi</p>
	</div>

	<form class="mt-8 space-y-5" onsubmit={handleSubmit}>
		<div class="space-y-4">
			<div>
				<label
					for="fullName"
					class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Họ và tên</label
				>
				<input
					id="fullName"
					type="text"
					required
					bind:value={fullName}
					class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
					placeholder="Nguyễn Văn A"
				/>
				<label for="reg-email" class="block text-sm font-medium text-gray-700 dark:text-white mb-1"
					>Email</label
				>
				<input
					id="reg-email"
					type="email"
					required
					bind:value={email}
					class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
					placeholder="you@example.com"
				/>
			</div>

			<div>
				<label
					for="reg-password"
					class="block text-sm font-medium text-gray-700 dark:text-white mb-1">Mật khẩu</label
				>
				<input
					id="reg-password"
					type="password"
					required
					bind:value={password}
					class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
					placeholder="••••••••"
				/>
			</div>

			<div>
				<label
					for="confirmPassword"
					class="block text-sm font-medium text-gray-700 dark:text-white mb-1"
					>Xác nhận mật khẩu</label
				>
				<input
					id="confirmPassword"
					type="password"
					required
					bind:value={confirmPassword}
					class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
					placeholder="••••••••"
				/>
			</div>

			<div class="grid grid-cols-2 gap-4">
				<div>
					<label
						for="studentCode"
						class="block text-sm font-medium text-gray-700 dark:text-white mb-1">Mã SV</label
					>
					<input
						id="studentCode"
						type="text"
						required
						bind:value={studentCode}
						placeholder="SE203..."
						class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none"
					/>
				</div>

				<div>
					<label
						for="studentType"
						class="block text-sm font-medium text-gray-700 dark:text-white mb-1">Trường học</label
					>
					<select
						id="studentType"
						required
						bind:value={studentType}
						class="block w-full px-4 py-3 bg-gray-50 dark:bg-white/5 border border-gray-200 dark:border-white/10 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all outline-none appearance-none [&>option]:bg-white dark:[&>option]:bg-gray-900"
					>
						<option value="" disabled>-- Chọn --</option>
						<option value="FPT">FPTU</option>
						<option value="EXTERNAL">External</option>
					</select>
				</div>
			</div>
		</div>

		<div class="pt-2">
			<button
				type="submit"
				class="w-full flex justify-center py-3.5 px-4 text-sm font-bold rounded-xl text-white bg-orange-600 hover:bg-orange-700 transition-colors focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 dark:focus:ring-offset-gray-900"
			>
				Đăng ký
			</button>
		</div>
	</form>
</div>

<div class="text-center mt-6">
	<p class="text-gray-500 dark:text-white text-sm">
		Đã có tài khoản?
		<a
			href="/login"
			class="font-bold text-orange-600 hover:text-orange-700 dark:text-orange-500 dark:hover:text-orange-400 transition-colors ml-1"
		>
			Đăng nhập
		</a>
	</p>
</div>
