import { browser } from "$app/environment"

export type ThemeType = "light" | "dark" | "system"

class ThemeState {
	// Mặc định là system
	value = $state<ThemeType>("system")

	// Trạng thái thực tế đang được áp dụng (light/dark)
	active = $state<"light" | "dark">("light")

	constructor() {
		if (browser) {
			this.init()
		}
	}

	private init() {
		// Lấy theme đã lưu
		const saved = localStorage.getItem("theme") as ThemeType | null
		if (saved) {
			this.value = saved
		}

		// Áp dụng theme
		this.applyTheme(this.value)

		// Lắng nghe thay đổi từ system
		window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", (e) => {
			if (this.value === "system") {
				this.applyTheme("system")
			}
		})
	}

	private applyTheme(theme: ThemeType) {
		let resolvedTheme: "light" | "dark" = "light"

		if (theme === "system") {
			resolvedTheme = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"
		} else {
			resolvedTheme = theme
		}

		this.active = resolvedTheme

		// Cập nhật DOM
		if (resolvedTheme === "dark") {
			document.documentElement.classList.add("dark")
		} else {
			document.documentElement.classList.remove("dark")
		}
	}

	setTheme(theme: ThemeType) {
		this.value = theme
		if (browser) {
			localStorage.setItem("theme", theme)
			this.applyTheme(theme)
		}
	}
}

export const themeState = new ThemeState()
