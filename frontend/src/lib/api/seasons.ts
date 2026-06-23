import { API_BASE } from "."
import { getToken } from "./auth"

export async function getSeasons() {
	return fetch(`${API_BASE}/seasons`, {
		method: "GET"
	})
}

export async function getSeason(seasonId: string) {
	return fetch(`${API_BASE}/seasons/${seasonId}`, {
		method: "GET"
	})
}

export async function createSeason(
	semester: string,
	year: number
) {
	return fetch(`${API_BASE}/seasons`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			semester,
			year
		})
	})
}
