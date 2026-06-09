import { API_BASE } from "."

export async function login(email: string, password: string) {
	return fetch(`${API_BASE}/auth/login`, {
		method: "POST",
		headers: {
			"Content-Type": "application/x-www-form-urlencoded"
		},
		body: new URLSearchParams({
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
