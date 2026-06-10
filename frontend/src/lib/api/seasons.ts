import { API_BASE } from "."
import { getToken } from "./auth"

export async function getSeasons() {
	return fetch(`${API_BASE}/seasons`, {
		method: "GET"
	})
}

export async function createSeason(
	name: string,
	description: string,
	startTime: string,
	endTime: string
) {
	return fetch(`${API_BASE}/seasons`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			name,
			description,
			start_time: startTime,
			end_time: endTime
		})
	})
}
