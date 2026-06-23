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
