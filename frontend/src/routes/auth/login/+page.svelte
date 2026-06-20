<script lang="ts">
    import { login } from "$lib/api/auth"
    import { getProfile } from "$lib/api/profile"
    import { goto } from "$app/navigation"
    import { Eye, EyeOff } from "@lucide/svelte"

    let email = $state("")
    let password = $state("")
    let showPassword = $state(false)
    let isLoading = $state(false)
    let errorMessage = $state("")

    function togglePassword() {
        showPassword = !showPassword
    }

    async function handleLogin(event: Event) {
        event.preventDefault()
        isLoading = true
        errorMessage = ""
        try {
            const response = await login(email, password)
            if (response.ok) {
                const data = await response.json()
                localStorage.setItem("auth_data", JSON.stringify(data))
                
                // Fetch profile to determine role and redirect
                const profileRes = await getProfile()
                if (profileRes.ok) {
                    const profile = await profileRes.json()
                    if (profile.role === "COORDINATOR") {
                        goto("/coordinator")
                    } else if (profile.role === "STUDENT") {
                        goto("/student")
                    } else if (profile.role === "MENTOR" || profile.role === "JUDGE") {
                        goto("/mentor")
                    } else {
                        goto("/")
                    }
                } else {
                    goto("/")
                }
            } else {
                errorMessage = "Invalid email or password"
            }
        } catch (error) {
            console.error("Login failed:", error)
            errorMessage = "An error occurred. Please try again later."
        } finally {
            isLoading = false;
        }
    }
</script>

<main class="w-screen h-screen justify-center content-center bg-(--md-surface)">
    <div class="max-w-md w-full m-auto flex flex-col items-center rounded-md p-4">
        <h1 class="text-xl font-bold">Welcome back</h1>
        <p class="mb-6 text-(--md-surface-variant)">Login with your account</p>

        <form class="w-full flex flex-col gap-2" onsubmit={handleLogin}>
            
            <label for="email" class="mb-1 text-sm">Email</label>
            <input
                id="email"
                class="w-full rounded-md"
                type="email"
                placeholder="Enter email"
                required
                bind:value={email}
            />

            <label for="password" class="mb-1 text-sm">Password</label>
            <div class="relative w-full"> <input
                    id="password"
                    class="w-full rounded-md pr-10" type={showPassword ? "text" : "password"} 
                    required
                    placeholder="Enter your password"
                    bind:value={password}
                />
                <button
                    type="button" onclick={togglePassword}
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-(--md-surface-variant) hover:text-(--md-on-surface) hover:cursor-pointer"
                    aria-label={showPassword ? "Hide password" : "Show password"}
                >
                    {#if showPassword}
                        <EyeOff size={20} />
                    {:else}
                        <Eye size={20} />
                    {/if}
                </button>
            </div>
            <a href="/auth/forgot-password" class="mt-2 self-end text-sm text-blue-600 hover:underline">
                Forgot password?
            </a>

            {#if errorMessage}
                <span class="text-red-500 text-sm">{errorMessage}</span>
            {/if}

            <button
                type="submit"
                disabled={isLoading}
                class="mt-2 rounded-md p-2 font-semibold transition-all bg-orange-500 text-white hover:cursor-pointer hover:brightness-75 disabled:opacity-50 disabled:cursor-not-allowed"
>
                {isLoading ? "Logging in..." : "Login"}
            </button>
        </form>

        <span class="mt-4">
            Don't have an account yet? 
            <a href="/auth/register" class="text-blue-600 hover:underline">Register now</a>
        </span>
    </div>
</main>

<style>
    input {
        border-color: var(--md-outline);
    }
    input[type="email"],
    input[type="password"],
    input[type="text"] {
        background-color: var(--md-surface-bright);
        color: var(--md-on-surface);
    }

    input::placeholder {
        color: var(--md-on-surface);
        opacity: 0.5;
    }
</style>