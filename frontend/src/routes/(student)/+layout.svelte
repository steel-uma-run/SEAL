<script lang="ts">
  import { page } from '$app/stores';
  import { authState } from '$lib/features/auth/auth.svelte';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  // Chuyển hướng nếu chưa đăng nhập
  onMount(() => {
    if (!authState.isAuthenticated) {
      goto('/');
    }
  });

  // State cho Kebab Menu
  let isMenuOpen = $state(false);

  function toggleMenu() {
    isMenuOpen = !isMenuOpen;
  }

  function handleLogout() {
    authState.logout();
    goto('/');
  }

  let { children } = $props();

  const navLinks = [
    { name: 'Dashboard', href: '/dashboard' },
    { name: 'Đội của tôi', href: '/my-team' },
    { name: 'Nộp bài', href: '/submission' }
  ];
</script>

<!-- Bố cục trang đơn giản -->
<div class="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100 flex flex-col font-sans">
  <!-- Thanh Dashboard (Navbar) -->
  <header class="bg-white dark:bg-gray-800 shadow-sm sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
      
      <!-- Logo và Danh sách tính năng (Desktop) -->
      <div class="flex items-center gap-8">
        <div class="font-bold text-xl text-purple-600 dark:text-purple-400">SEAL</div>
        
        <!-- Thanh tính năng -->
        <nav class="hidden md:flex space-x-4">
          {#each navLinks as link}
            <a 
              href={link.href} 
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors
              {$page.url.pathname === link.href 
                ? 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300' 
                : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-white'}"
            >
              {link.name}
            </a>
          {/each}
        </nav>
      </div>

      <!-- Kebab Menu (Nút 3 chấm) -->
      <div class="relative">
        <button 
          onclick={toggleMenu}
          class="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors focus:outline-none"
          aria-label="Menu"
        >
          <!-- Kebab Icon -->
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="5" r="1"></circle>
            <circle cx="12" cy="12" r="1"></circle>
            <circle cx="12" cy="19" r="1"></circle>
          </svg>
        </button>

        {#if isMenuOpen}
          <!-- Backdrop đóng menu -->
          <div 
            class="fixed inset-0 z-40" 
            onclick={() => isMenuOpen = false}
            onkeydown={(e) => e.key === 'Escape' && (isMenuOpen = false)}
            role="button"
            tabindex="0"
          ></div>
          
          <!-- Menu thả xuống -->
          <div class="absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 z-50">
            <!-- Hiển thị lại các tính năng trong Menu (Dùng cho Mobile hoặc tiện ích) -->
            <div class="border-b border-gray-100 dark:border-gray-700 mb-1 pb-1">
              {#each navLinks as link}
                <a 
                  href={link.href} 
                  onclick={() => isMenuOpen = false}
                  class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-700"
                >
                  {link.name}
                </a>
              {/each}
            </div>

            <!-- Tính năng Logout (Đăng xuất) ở dưới -->
            <button 
              onclick={handleLogout}
              class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20"
            >
              Đăng xuất
            </button>
          </div>
        {/if}
      </div>
    </div>
  </header>

  <!-- Phần hiển thị các trang con (ví dụ: Dashboard, My Team...) -->
  <main class="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
    {@render children()}
  </main>
</div>
