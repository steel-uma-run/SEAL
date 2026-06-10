import { API_BASE } from "."
import { getToken } from "./auth"

export async function createTeam(
	teamName: string,
	description: string,
	seasonId: string,
	leaderId: string
) {
	return fetch(`${API_BASE}/teams`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			name: teamName,
			description: description,
			season_id: seasonId,
			leader_id: leaderId
		})
	})
}
