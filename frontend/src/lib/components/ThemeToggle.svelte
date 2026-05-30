<script lang="ts">
  import { themeState } from '../stores/theme.svelte';
  
  let currentTheme = $derived(themeState.value);

  function toggleTheme() {
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    themeState.setTheme(newTheme);
  }
</script>

<button 
  onclick={toggleTheme}
  class="flex items-center justify-center p-2 rounded-full transition-all duration-300 bg-gray-200/50 hover:bg-gray-300/80 dark:bg-white/5 dark:hover:bg-white/20 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white focus:outline-none backdrop-blur-md shadow-sm border border-transparent dark:border-white/5"
  aria-label="Toggle Dark Mode"
>
  {#if currentTheme === 'light'}
    <svg class="w-5 h-5 transition-transform duration-300 hover:rotate-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <!-- Moon Icon -->
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
    </svg>
  {:else}
    <svg class="w-5 h-5 transition-transform duration-300 hover:rotate-45" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <!-- Sun Icon -->
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
    </svg>
  {/if}
</button>
