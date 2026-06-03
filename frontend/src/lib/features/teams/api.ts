import type { Team, TeamMember } from "./types"

// Mock DB
let mockTeams: Team[] = [
	{
		id: "team-1",
		name: "Tech Innovators",
		season: "Summer Hackathon 2026",
		track: "AI & Data",
		members: [
			{ id: "u1", name: "Nguyễn Văn A", email: "nva@example.com", role: "Leader" },
			{ id: "u2", name: "Trần Thị B", email: "ttb@example.com", role: "Member" }
		]
	}
]

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

export async function createTeam(
	creatorEmail: string,
	teamName: string,
	season: string,
	track: string
): Promise<Team> {
	await delay(500)
	if (mockTeams.some((t) => t.name === teamName)) {
		throw new Error("Tên đội đã tồn tại")
	}

	const newTeam: Team = {
		id: `team-${Date.now()}`,
		name: teamName,
		season: season,
		track: track,
		members: [
			{
				id: `u-${Date.now()}`,
				name: "Bạn (Leader)",
				email: creatorEmail,
				role: "Leader"
			}
		]
	}
	mockTeams.push(newTeam)
	return newTeam
}

export async function addMemberByEmail(
	teamId: string,
	email: string,
	role: "Member" | "Mentor"
): Promise<Team> {
	await delay(500)
	const team = mockTeams.find((t) => t.id === teamId)
	if (!team) throw new Error("Không tìm thấy đội")

	if (team.members.some((m) => m.email === email)) {
		throw new Error("Thành viên này đã ở trong đội")
	}

	team.members.push({
		id: `u-${Date.now()}`,
		name: `User ${email.split("@")[0]}`,
		email: email,
		role: role
	})

	return team
}

export async function getMyTeam(email: string): Promise<Team | null> {
	await delay(300)
	const team = mockTeams.find((t) => t.members.some((m) => m.email === email))
	return team || null
}
