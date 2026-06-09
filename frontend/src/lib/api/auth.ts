import { API_BASE } from "."

export async function login(email: string, password: string) {
	return fetch(`${API_BASE}/auth/login`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			email: email,
			password: password
		})
	})
}

export async function register(
	email: string,
	password: string,
	name: string,
	studentId: string,
	isExternal: boolean
) {
	return fetch(`${API_BASE}/auth/register`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			email: email,
			password: password,
			name: name,
			student_id: studentId,
			is_external: isExternal
		})
	})
}

export function getToken(): string {
	if (typeof window !== "undefined") {
		const data = localStorage.getItem("auth_data")
		if (data) {
			try {
				const parsed = JSON.parse(data)
				return parsed.token || parsed // handles if it's an object or just a string
			} catch {
				return data // if it's just a raw string
			}
		}
	}
	return ""
}
