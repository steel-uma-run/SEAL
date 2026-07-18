import { vitePreprocess } from "@sveltejs/vite-plugin-svelte"
import adapter from "@sveltejs/adapter-static"

const config = {
	compilerOptions: {
		runes: ({ filename }) => (filename.split(/[/\\]/).includes("node_modules") ? undefined : true),
		experimental: { async: true }
	},
	preprocess: [
		vitePreprocess({
			style: true,
			script: true
		})
	],
	kit: {
		adapter: adapter(),
		prerender: {
			handleUnseenRoutes: "warn"
		}
	}
}

export default config
