<script lang="ts">
  import LoginForm from '$lib/features/auth/components/LoginForm.svelte';
  import RegisterForm from '$lib/features/auth/components/RegisterForm.svelte';
  import ThemeToggle from '$lib/components/ThemeToggle.svelte';

  let isLogin: boolean = $state(true);

  function toggleMode() {
    isLogin = !isLogin;
  }
</script>

<div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-[#0a0a0a] px-4 sm:px-6 lg:px-8 py-12 relative overflow-hidden transition-colors duration-300">
  <!-- Theme Toggle in top right corner -->
  <div class="absolute top-4 right-4 z-50">
    <ThemeToggle />
  </div>
  <!-- Neon Orbs Background -->
  <div class="absolute top-1/4 left-1/4 w-72 h-72 bg-purple-600 rounded-full mix-blend-multiply filter blur-[100px] opacity-30 animate-pulse"></div>
  <div class="absolute bottom-1/4 right-1/4 w-72 h-72 bg-blue-600 rounded-full mix-blend-multiply filter blur-[100px] opacity-30 animate-pulse" style="animation-delay: 2s;"></div>

  <div class="max-w-md w-full bg-white/5 backdrop-blur-xl p-8 rounded-2xl shadow-[0_0_40px_rgba(139,92,246,0.15)] border border-white/10 relative z-10 transition-all duration-500">
    
    {#if isLogin}
      <LoginForm onToggleMode={toggleMode} />
    {:else}
      <RegisterForm onToggleMode={toggleMode} />
    {/if}

  </div>
</div>
