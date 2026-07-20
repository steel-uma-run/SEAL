import { defineConfig } from "vite"
import { sveltekit } from "@sveltejs/kit/vite"
import { heyApiPlugin } from "@hey-api/vite-plugin"
import { functionsMixins } from "vite-plugin-functions-mixins"

export default defineConfig({
	plugins: [
		heyApiPlugin({
			config: {
				input: "../openapi.json",
				output: {
					path: "src/lib/api"
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
		functionsMixins({ deps: ["m3-svelte"] }),
		sveltekit()
	]
})
