<script lang="ts">
  import { authState } from '$lib/features/auth/auth.svelte';
</script>

<div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 sm:p-8">
  <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
    Xin chào, {authState.user?.fullName || 'Sinh viên'}!
  </h1>
  <p class="text-gray-500 dark:text-gray-400 mb-8">
    Chào mừng bạn đến với SEAL Hackathon.
  </p>

  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
    <!-- Card 1: My Team -->
    <div class="border border-gray-100 dark:border-gray-700 rounded-lg p-6 hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-full flex items-center justify-center mb-4">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
      </div>
      <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">Đội thi của bạn</h3>
      <p class="text-gray-600 dark:text-gray-300 text-sm mb-4">Bạn chưa tham gia đội thi nào. Hãy tạo hoặc xin gia nhập đội.</p>
      <a href="/my-team" class="text-purple-600 dark:text-purple-400 font-medium text-sm hover:underline">Đi tới Đội của tôi &rarr;</a>
    </div>

    <!-- Card 2: Submission -->
    <div class="border border-gray-100 dark:border-gray-700 rounded-lg p-6 hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full flex items-center justify-center mb-4">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
      </div>
      <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">Nộp bài dự thi</h3>
      <p class="text-gray-600 dark:text-gray-300 text-sm mb-4">Theo dõi và nộp sản phẩm đúng hạn.</p>
      <a href="/submission" class="text-blue-600 dark:text-blue-400 font-medium text-sm hover:underline">Đi tới Nộp bài &rarr;</a>
    </div>
  </div>
</div>
