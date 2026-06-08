<script lang="ts">
	import "./layout.css"

	import { theme } from "$lib/theme.svelte"
	import { argbFromHex, Hct, hexFromArgb, SchemeNeutral } from "@poupe/material-color-utilities"

	let { children } = $props()

	// The things one would do to reduce reliance on JavaScript
	const styleElem = $derived.by(() => {
		const scheme = new SchemeNeutral(
			Hct.fromInt(argbFromHex(theme.primaryColor)),
			theme.darkMode,
			0
		)
		const entries = new Array<string>()

		for (const dynamicColor of scheme.colors.allColors) {
			const token = dynamicColor.name.replaceAll("_", "-")
			const value = hexFromArgb(dynamicColor.getArgb(scheme))
			entries.push(`--md-${token}: ${value};`)
		}

		const str = `<style>:root {${entries.join("\n")}}</style>`
		return str
	})
</script>

<svelte:head>
	<link rel="preconnect" href="https://rsms.me/" />
	<link rel="stylesheet" href="https://rsms.me/inter/inter.css" />

	<style>
		:root {
			color: var(--md-on-surface);
		}

		
	</style>

	{@html styleElem}
</svelte:head>

<div class="app-container" data-theme={theme.darkMode ? 'dark' : 'light'}>
    <header class="app-header">
        <div class="logo">
				<a href="/home">
					<img src="https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg" alt="Logo" class="logo-img" />
					<p>SEAL</p>
				</a>
			</div>

        <div class="regis-log-btn">
        	<button class="theme-btn" onclick={() => (theme.darkMode = !theme.darkMode)}>
            	{theme.darkMode ? 'Light Mode' : 'Dark Mode'}
    		</button>
			<a href="/register" class="btn btn-login">Đăng kí</a>
			<a href="/login" class="btn btn-register">Đăng nhập</a>
		</div>
    </header>

    <main class="app-main">
        {@render children()}
    </main>
</div>

<style>
	.app-container{
		display: flex;
		flex-direction: column;
		min-height: 100vh;
		background-color: var(--md-surface); 
        transition: background-color 0.3s ease;
	}

	.logo{
		font-size: 1.5rem;
        font-weight: 700;
        color: var(--md-primary);
	}

	.logo > a {
		display: flex;
		align-items: center;
		gap: 10px;
	}

	.logo > a > p {
		color: #f54901;
		font-size: 2rem;
		font-weight: 700;
		margin: 0;
	}

	.logo-img {
        height: 36px;
        width: auto;
        object-fit: contain;
        display: block; 
    }

	.regis-log-btn {
		display: flex;
		align-items: center;
		gap: 20px;

	}

	.btn {
        padding: 0.5rem 1.25rem;
        border-radius: 8px;
        text-decoration: none;
        font-weight: 600;
        font-size: 0.95rem;
        transition: all 0.2s ease;
    }

	.btn-login {
        background-color: #ffffff;
        color: #F26F21;
        border: 2px solid #F26F21;
    }

	.btn-login:hover {
        background-color: #fff0e8;
    }

	.btn-register {
        background-color: #F26F21;
        color: #ffffff;
        border: 2px solid #F26F21;
        box-shadow: 0 2px 4px rgba(242, 111, 33, 0.2);
    }

    .btn-register:hover {
        background-color: #d85c14;
        border-color: #d85c14;
    }

	.app-header {
		position: fixed; 
        top: 0; 
        left: 0;
        width: 100%;
        box-sizing: border-box;
        z-index: 1000;

        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 2rem;
        background-color: var(--md-surface-container);
        border-bottom: 1px solid var(--md-outline-variant);
        transition: background-color 0.3s ease;
    }

	.theme-btn {
        padding: 0.5rem 1rem;
        background-color: var(--md-primary);
        color: var(--md-on-primary);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        font-weight: 600;
        transition: opacity 0.2s ease;
    }

	.app-main {
        padding-top: 70px;
        flex: 1;
    }
	
    .theme-btn:hover {
        opacity: 0.8;
    }

</style>
