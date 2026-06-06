import tailwindcss from "@tailwindcss/vite"
import { sveltekit } from "@sveltejs/kit/vite"
import { defineConfig } from "vite"

export default defineConfig({
	plugins: [tailwindcss(), sveltekit()],
	server: {
		proxy: {
			"/api": {
				target: "http://localhost:8080",
				changeOrigin: true,
				secure: false,
				configure: (proxy, _options) => {
					proxy.on("proxyReq", (proxyReq, req, res) => {
						// Xóa Origin header để Backend tưởng đây là request nội bộ (giống Postman)
						proxyReq.removeHeader("Origin")
						proxyReq.removeHeader("Referer")
					})
				}
			}
		}
	}
})
