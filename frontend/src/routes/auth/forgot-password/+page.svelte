<script lang="ts">
    import { theme } from "$lib/theme.svelte"
    import { Mail, CheckCircle2, ArrowLeft } from "@lucide/svelte"

    let email = $state("")
    let isLoading = $state(false)
    let message = $state("")
    let isSuccess = $state(false)

    function handleResetPassword(event: Event) {
        event.preventDefault()
        isLoading = true
        message = ""
        
        // Simulating backend request for password reset
        setTimeout(() => {
            isLoading = false
            isSuccess = true
            message = "If this email is registered in our system, you will receive a reset password link shortly."
        }, 1500)
    }
</script>

<svelte:head>
    <title>Forgot Password - SEAL</title>
</svelte:head>

<main class="w-screen h-screen flex justify-center items-center bg-(--md-surface)">
    <div class="max-w-md w-full m-auto flex flex-col items-center rounded-xl p-8 border {theme.darkMode ? 'bg-zinc-900 border-zinc-800 text-zinc-100 shadow-[0_4px_30px_rgba(0,0,0,0.2)]' : 'bg-white border-gray-100 text-gray-800 shadow-[0_4px_20px_rgba(0,0,0,0.03)]'}">
        
        <a href="/auth/login" class="self-start flex items-center gap-2 mb-6 text-sm text-[#f54901] hover:underline font-semibold">
            <ArrowLeft size={16} />
            Back to login
        </a>

        <h1 class="text-2xl font-black mb-2">Reset Password</h1>
        <p class="mb-6 text-center text-sm text-(--md-surface-variant) leading-relaxed">
            Enter your registered email address below, and we will send you instructions to reset your password.
        </p>

        {#if isSuccess}
            <div class="w-full p-4 rounded-xl border flex gap-3 mb-6 bg-emerald-50 border-emerald-100 text-emerald-800 dark:bg-emerald-950/20 dark:border-emerald-900/50 dark:text-emerald-400">
                <CheckCircle2 class="w-5 h-5 flex-shrink-0 mt-0.5" />
                <p class="text-sm font-medium leading-relaxed">{message}</p>
            </div>
        {:else}
            <form class="w-full flex flex-col gap-4" onsubmit={handleResetPassword}>
                <div class="space-y-1">
                    <label for="email" class="text-sm font-semibold">Email Address</label>
                    <div class="relative w-full">
                        <input
                            id="email"
                            class="w-full rounded-xl border p-3 pl-10 focus:ring-2 focus:ring-orange-500 transition-all outline-none {theme.darkMode ? 'border-zinc-800 bg-zinc-950 text-zinc-100 placeholder-zinc-650' : 'border-gray-200 bg-gray-50'}"
                            type="email"
                            placeholder="Enter your email"
                            required
                            bind:value={email}
                        />
                        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400">
                            <Mail size={16} />
                        </span>
                    </div>
                </div>

                {#if message}
                    <span class="text-red-500 text-sm font-medium">{message}</span>
                {/if}

                <button
                    type="submit"
                    disabled={isLoading}
                    class="mt-2 w-full bg-[#f97316] hover:bg-[#ea580c] text-white rounded-xl py-3.5 font-bold shadow-sm transition-all disabled:opacity-75 disabled:cursor-not-allowed hover:cursor-pointer border-0"
                >
                    {isLoading ? "Sending link..." : "Send Reset Link"}
                </button>
            </form>
        {/if}
    </div>
</main>

<style>
    input {
        border-color: var(--md-outline);
    }
</style>
