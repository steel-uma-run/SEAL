import { API_BASE } from "."
import { getToken } from "./auth"

export async function getProfile() {
	return fetch(`${API_BASE}/profile`, {
		method: "GET",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		}
	})
}

export async function updateProfile(data: { name: string; strengths?: string; about_me?: string }) {
	return fetch(`${API_BASE}/profile`, {
		method: "PUT",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		},
		body: JSON.stringify(data)
	})
}

