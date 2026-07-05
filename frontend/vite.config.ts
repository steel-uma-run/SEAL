import { defineConfig } from "vite"
import tailwindcss from "@tailwindcss/vite"
import { sveltekit } from "@sveltejs/kit/vite"
import { heyApiPlugin } from "@hey-api/vite-plugin"

export default defineConfig({
	plugins: [
		heyApiPlugin({
			config: {
				input: "../openapi.json",
				output: {
					path: "src/lib/api",
					postProcess: ["prettier"]
				},
				plugins: [
					"@hey-api/typescript",
					{
						name: "@hey-api/client-fetch",
						throwOnError: true
					},
					{
						name: "zod",
						dates: {
							offset: true
						}
					},
					{
						name: "@hey-api/sdk"
					}
				]
			}
		}),
		tailwindcss(),
		sveltekit()
	]
})