import { API_BASE } from "."
import { getToken } from "./auth"

export async function getInvites() {
	return fetch(`${API_BASE}/invites`, {
		headers: {
			Authorization: `Bearer ${getToken()}`
		}
	})
}

export async function acceptInvite(inviteId: string) {
	return fetch(`${API_BASE}/invites/${inviteId}/accept`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`
		}
	})
}

export async function declineInvite(inviteId: string) {
	return fetch(`${API_BASE}/invites/${inviteId}/decline`, {
		method: "POST",
		headers: {
			Authorization: `Bearer ${getToken()}`
		}
	})
}
