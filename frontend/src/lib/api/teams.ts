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

export async function getMyTeam() {
	return fetch(`${API_BASE}/teams/my-team`, {
		headers: {
			Authorization: `Bearer ${getToken()}`
		}
	})
}

export async function inviteMember(teamId: string, studentId: string) {
	return fetch(`${API_BASE}/teams/${teamId}/invite/${studentId}`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`
		}
	})
}
