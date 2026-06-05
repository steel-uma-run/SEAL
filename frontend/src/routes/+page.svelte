<script lang="ts">
	import ThemeToggle from "$lib/components/ThemeToggle.svelte"
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300">
	<header
		class="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 sticky top-0 z-40"
	>
		<div class="max-w-[85%] mx-auto px-4 sm:px-6 lg:px-8 h-24 flex items-center justify-between">
			<div class="flex items-center">
				<a href="/" class="flex flex-col items-center hover:opacity-80 transition-opacity">
					<div class="flex items-center">
						<img
							src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg"
							alt="FPT Logo"
							class="h-8 w-auto"
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

			<div class="flex items-center space-x-6">
				<ThemeToggle />
				<a
					href="/login"
					class="text-base font-bold text-gray-700 dark:text-white hover:text-orange-600 dark:hover:text-orange-400 transition-colors"
				>
					Đăng nhập
				</a>
				<a
					href="/register"
					class="text-base font-bold px-6 py-2 rounded-xl bg-orange-600 hover:bg-orange-700 text-white transition-all duration-300 hover:scale-105 hover:shadow-lg"
				>
					Đăng ký
				</a>
			</div>
		</div>
	</header>

	<main class="relative w-full min-h-[85vh] flex flex-col justify-center items-center text-center overflow-hidden">
        
        <div class="absolute inset-0 z-0 pointer-events-none">
            <img 
                src="src\image\abstract-futuristic-background-with-3d-design.jpg" 
                alt="Background" 
                class="w-full h-full object-cover " 
            />
            <div class="absolute inset-0 bg-gradient-to-b from-white/30 via-white/80 to-gray-50 dark:from-gray-900/50 dark:via-gray-900/90 dark:to-gray-900"></div>
        </div>
        <div class="relative z-10 max-w-3xl space-y-8 px-4 sm:px-6 lg:px-8 py-20">
            <div class="inline-flex items-center px-4 py-1 rounded-full bg-orange-100 dark:bg-orange-900/60 text-orange-600 dark:text-orange-400 text-sm font-bold border border-orange-200 dark:border-orange-800 shadow-sm">
                Khởi nguồn sáng tạo, kiến tạo tương lai
            </div>

            <h1 class="text-5xl sm:text-6xl font-black tracking-tight text-gray-900 dark:text-white uppercase drop-shadow-md">
                <span class="text-orange-600">SEAL</span> HACKATHON
            </h1>

            <p class="text-lg sm:text-xl text-gray-700 dark:text-gray-200 leading-relaxed max-w-2xl mx-auto font-medium drop-shadow-sm">
                Hệ thống quản lý sự kiện và hỗ trợ sinh viên trường Đại học FPT. Đăng ký ngay để thành lập
                đội thi, nộp sản phẩm và trải nghiệm nền tảng!
            </p>

            <div class="flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-4 pt-4">
                <a href="/register" class="w-full sm:w-auto px-10 py-4 text-lg font-bold rounded-xl text-white bg-orange-600 hover:bg-orange-700 transition-all duration-300 hover:scale-105 hover:shadow-xl">
                    Tham gia ngay
                </a>

                <a href="/login" class="w-full sm:w-auto px-10 py-4 text-lg font-bold rounded-xl text-orange-600 dark:text-orange-400 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm border-2 border-orange-600 dark:border-orange-400 hover:bg-orange-50 dark:hover:bg-gray-700 transition-all duration-300 hover:scale-105 hover:shadow-lg">
                    Dành cho sinh viên đã có đội
                </a>
            </div>
        </div>
    </main>

	<section class="w-full max-w-[1400px] mx-auto px-4 sm:px-6 lg:px-8 py-20">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center">
        
        <div class="space-y-6">
            <h2 class="text-4xl md:text-5xl font-extrabold text-gray-900 dark:text-white tracking-tight leading-tight">
                Kiến tạo tương lai <br />
                <span class="text-orange-600">Dẫn đầu công nghệ</span>
            </h2>

            <div class="w-24 h-1 bg-orange-600 rounded-full"></div>

            <p class="text-lg text-gray-600 dark:text-gray-300 leading-relaxed text-justify">
                SEAL Hackathon không chỉ là một cuộc thi lập trình, mà là bệ phóng để các bạn sinh viên FPT University biến những ý tưởng điên rồ nhất thành sản phẩm thực tế. Xuyên suốt hành trình, bạn sẽ được dẫn dắt bởi đội ngũ mentor dày dặn kinh nghiệm, trải nghiệm áp lực thời gian thực, và học cách làm việc nhóm hiệu quả để giải quyết các bài toán hóc búa từ doanh nghiệp.
            </p>
            <p class="text-lg text-gray-600 dark:text-gray-300 leading-relaxed text-justify">
                Bước ra khỏi cuộc thi, mỗi thí sinh không chỉ mang theo một "chiến tích" ấn tượng trong CV, mà còn tích lũy được tư duy giải quyết vấn đề sắc bén, sẵn sàng chinh phục thế giới công nghệ không ngừng đổi mới.
            </p>

            <div class="pt-4">
                <a
                    href="/register"
                    class="inline-flex items-center px-8 py-3.5 text-base font-bold rounded-xl text-white bg-orange-600 hover:bg-orange-700 transition-all duration-300 hover:scale-105 hover:shadow-lg w-fit"
                >
                    Tìm hiểu ngay
                    <svg class="ml-2 w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                    </svg>
                </a>
            </div>
        </div>

        <div class="relative w-full mt-8 lg:mt-0">
            <div class="absolute -right-4 -bottom-4 w-full h-full bg-orange-500 rounded-2xl transform translate-x-3 translate-y-3 opacity-30 dark:opacity-20 hidden md:block z-0"></div>
            
            <div class="absolute -left-4 -top-4 w-full h-full border-2 border-gray-200 dark:border-gray-700 rounded-2xl transform -translate-x-3 -translate-y-3 hidden md:block z-0"></div>

            <div class="relative z-10 rounded-2xl overflow-hidden shadow-2xl aspect-video group bg-gray-100 dark:bg-gray-800 border border-gray-100 dark:border-gray-700">
                <video 
            src="src\image\FPT HACKATHON 2025 - AFTERMOVIE - YouTube.mp4" 
            class="w-full h-full object-cover "
            loop
            muted
            playsinline
            controls
        		></video>
            </div>
        </div>

    </div>
</section>

	<section class="max-w-[85%] mx-auto px-4 sm:px-6 lg:px-8 py-16">
    <div class="flex items-center justify-center mb-12 overflow-hidden">
        <div class="hidden sm:block flex-1 h-4 bg-[repeating-linear-gradient(-45deg,transparent,transparent_4px,currentColor_4px,currentColor_8px)] text-gray-300 dark:text-gray-700 max-w-[150px]"></div>
        <h2 class="text-3xl sm:text-4xl font-extrabold text-gray-800 dark:text-white mx-6 uppercase tracking-wider text-center">
            Trải nghiệm <span class="text-orange-600">khác biệt</span>
        </h2>
        <div class="hidden sm:block flex-1 h-4 bg-[repeating-linear-gradient(45deg,transparent,transparent_4px,currentColor_4px,currentColor_8px)] text-gray-300 dark:text-gray-700 max-w-[150px]"></div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-12 gap-4 sm:gap-6">
        
        <div class="relative group overflow-hidden rounded-2xl md:col-span-6 h-64 md:h-80 shadow-md">
            <img src="src\image\anh-ki-niem.webp" alt="Tự tin vươn mình" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-xl md:text-2xl font-bold border-l-4 border-orange-500 pl-3 leading-tight">Tự tin vươn mình</h3>
            </div>
        </div>

        <div class="relative group overflow-hidden rounded-2xl md:col-span-3 h-64 md:h-80 shadow-md">
            <img src="src\image\cac-team.jpg" alt="Biến ý tưởng thành hiện thực" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-lg font-bold border-l-4 border-orange-500 pl-3 leading-tight">Biến ý tưởng<br/> thành hiện thực</h3>
            </div>
        </div>

        <div class="relative group overflow-hidden rounded-2xl md:col-span-3 h-64 md:h-80 shadow-md">
            <img src="src\image\tcbc-chung-ket-fpt-edu-hackathon-2024anh-1.webp" alt="Học tập tại doanh nghiệp" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-lg font-bold border-l-4 border-orange-500 pl-3 leading-tight">Làm quen dự án<br/>thực tế</h3>
            </div>
        </div>

        <div class="relative group overflow-hidden rounded-2xl md:col-span-3 h-64 md:h-80 shadow-md">
            <img src="src\image\mentor.jpg" alt="Dàn mentor cực chất" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-lg font-bold border-l-4 border-orange-500 pl-3 leading-tight">Đội ngũ mentor<br/>cực chất</h3>
            </div>
        </div>

        <div class="relative group overflow-hidden rounded-2xl md:col-span-3 h-64 md:h-80 shadow-md">
            <img src="src\image\team1.webp" alt="Kết nối đồng đội" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-lg font-bold border-l-4 border-orange-500 pl-3 leading-tight">Kết nối<br/>Đồng đội</h3>
            </div>
        </div>

        <div class="relative group overflow-hidden rounded-2xl md:col-span-6 h-64 md:h-80 shadow-md">
            <img src="src\image\anh-ki-niem2.jpg" alt="Giải thưởng" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            <div class="absolute bottom-6 left-6">
                <h3 class="text-white text-xl md:text-2xl font-bold border-l-4 border-orange-500 pl-3 leading-tight">Giải thưởng hấp dẫn</h3>
            </div>
        </div>

    </div>
</section>
</div>
