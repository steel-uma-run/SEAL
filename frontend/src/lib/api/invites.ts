import { API_BASE } from "."
import { getToken } from "./auth"

export async function getInvites() {
	return fetch(`${API_BASE}/invites`, {
		method: "GET",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		}
	})
}

export async function acceptInvite(inviteId: string) {
	return fetch(`${API_BASE}/invites/${inviteId}/accept`, {
		method: "PUT",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		}
	})
}

export async function declineInvite(inviteId: string) {
	return fetch(`${API_BASE}/invites/${inviteId}/decline`, {
		method: "PUT",
		headers: {
			Authorization: `Bearer ${getToken()}`,
			"Content-Type": "application/json"
		}
	})
}
