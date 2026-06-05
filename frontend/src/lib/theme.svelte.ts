class ThemeStore {
	primaryColor = $state("#ff00ff")
	darkMode = $state(false)
}

export const theme = new ThemeStore()
