import type { UserProfile } from "./types"

import { browser } from "$app/environment"

class AuthState {
	user = $state<UserProfile | null>(null)
	token = $state<string | null>(null)
	isAuthenticated = $derived(!!this.user && !!this.token)

	constructor() {
		if (browser) {
			const storedUser = localStorage.getItem("auth_user")
			const storedToken = localStorage.getItem("auth_token")
			if (storedUser && storedToken) {
				try {
					this.user = JSON.parse(storedUser)
					this.token = storedToken
				} catch (e) {
					// ignore parsing error
				}
			}
		}
	}

	login(userData: UserProfile, authToken: string) {
		this.user = userData
		this.token = authToken
		if (browser) {
			localStorage.setItem("auth_user", JSON.stringify(userData))
			localStorage.setItem("auth_token", authToken)
		}
	}

	logout() {
		this.user = null
		this.token = null
		if (browser) {
			localStorage.removeItem("auth_user")
			localStorage.removeItem("auth_token")
		}
	}
}

export const authState = new AuthState()
