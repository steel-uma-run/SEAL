<script lang="ts">
	import LoginForm from "$lib/features/auth/components/LoginForm.svelte"
	import ThemeToggle from "$lib/components/ThemeToggle.svelte"
</script>

<div
	class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 sm:px-6 lg:px-8 py-12 relative transition-colors duration-300"
>
	<div class="absolute top-4 right-4 z-50">
		<ThemeToggle />
	</div>

	<div
		class="max-w-md w-full bg-white dark:bg-gray-800 p-8 rounded-2xl border border-gray-200 dark:border-gray-700 relative z-10 transition-all duration-300"
	>
		<LoginForm />
	</div>
</div>
