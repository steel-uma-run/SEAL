export interface TeamMember {
	id: string
	fullName: string
	email: string
	studentCode: string
	studentType: "FPT" | "EXTERNAL"
}

export interface Team {
	id: string
	name: string
	leaderId: string
	memberIds: string[]
	members?: TeamMember[]
	mentorId?: string
	season?: string
	track?: string
}
