import { defineConfig } from "vite"
import tailwindcss from "@tailwindcss/vite"
import { sveltekit } from "@sveltejs/kit/vite"
import { heyApiPlugin } from "@hey-api/vite-plugin"
import { createClient } from "@hey-api/openapi-ts"

const heyApiConfig = {
	input: "../openapi.json",
	output: {
		path: "src/lib/api",
		clean: false, // Prevents ENOTEMPTY/EPERM errors on Windows due to file locks
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

export default defineConfig(async () => {
	// Pre-generate API client at startup to prevent SvelteKit compilation / 500 Internal Error
	// when src/lib/api has been deleted or is missing before build/dev runs.
	await createClient(heyApiConfig)

	return {
		plugins: [
			heyApiPlugin({
				config: heyApiConfig
			}),
			tailwindcss(),
			sveltekit()
		]
	}
})
