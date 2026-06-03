<script lang="ts">
	import ThemeToggle from "$lib/components/ThemeToggle.svelte"
</script>

<div class="min-h-screen bg-gray-50 dark:bg-[#0a0a0a] transition-colors duration-300">
	<header
		class="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 sticky top-0 z-40"
	>
		<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
			<div class="flex items-center">
				<a href="/" class="flex flex-col items-center hover:opacity-80 transition-opacity">
					<div class="flex items-center">
						<img
							src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
							alt="FPT Logo"
							class="h-6 w-auto"
						/>
						<span class="ml-1 text-xs font-semibold text-[#002f6c] dark:text-blue-400"
							>Education</span
						>
					</div>
					<span
						class="text-orange-600 dark:text-orange-500 font-extrabold text-sm tracking-wider uppercase"
						>FPT University</span
					>
				</a>
			</div>

			<div class="flex items-center space-x-4">
				<ThemeToggle />
				<a
					href="/login"
					class="text-sm font-bold text-gray-700 dark:text-white hover:text-orange-600 dark:hover:text-orange-400 transition-colors"
				>
					Đăng nhập
				</a>
				<a
					href="/register"
					class="text-sm font-bold px-4 py-2 rounded-xl bg-orange-600 hover:bg-orange-700 text-white transition-colors"
				>
					Đăng ký
				</a>
			</div>
		</div>
	</header>

	<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 flex flex-col items-center text-center">
		<div class="max-w-3xl space-y-8">
			<div
				class="inline-flex items-center px-4 py-1 rounded-full bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 text-sm font-bold border border-orange-200 dark:border-orange-800"
			>
				Khởi nguồn sáng tạo, kiến tạo tương lai
			</div>

			<h1
				class="text-5xl sm:text-6xl font-black tracking-tight text-gray-900 dark:text-white uppercase"
			>
				<span class="text-orange-600">SEAL</span> HACKATHON
			</h1>

			<p
				class="text-lg sm:text-xl text-gray-600 dark:text-white leading-relaxed max-w-2xl mx-auto font-medium"
			>
				Hệ thống quản lý sự kiện và hỗ trợ sinh viên trường Đại học FPT. Đăng ký ngay để thành lập
				đội thi, nộp sản phẩm và trải nghiệm nền tảng!
			</p>

			<div
				class="flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-4 pt-4"
			>
				<a
					href="/register"
					class="w-full sm:w-auto px-8 py-4 text-base font-bold rounded-xl text-white bg-orange-600 hover:bg-orange-700 transition-colors"
				>
					Tham gia ngay
				</a>

				<a
					href="/login"
					class="w-full sm:w-auto px-8 py-4 text-base font-bold rounded-xl text-orange-600 dark:text-orange-400 bg-white dark:bg-gray-800 border-2 border-orange-600 dark:border-orange-400 hover:bg-orange-50 dark:hover:bg-gray-700 transition-colors"
				>
					Dành cho sinh viên đã có đội
				</a>
			</div>
		</div>
	</main>
</div>
