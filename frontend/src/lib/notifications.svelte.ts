export interface NotificationItem {
	id: string
	title: string
	description: string
	time: string // ISO string timestamp
	unread: boolean
	type: "info" | "success" | "warning" | "error"
	link?: string
}

class NotificationsStore {
	private _list = $state<NotificationItem[]>([])
	private _initializedRole = $state<string | null>(null)
	private simInterval: any = null

	constructor() {
		if (typeof window !== "undefined") {
			try {
				const savedList = localStorage.getItem("seal_notifications")
				if (savedList) this._list = JSON.parse(savedList)

				const savedRole = localStorage.getItem("seal_notifications_role")
				if (savedRole) this._initializedRole = JSON.parse(savedRole)
			} catch (e) {
				console.error("Failed to load notifications from localStorage:", e)
			}
		}
	}

	get list() {
		return this._list
	}

	set list(val: NotificationItem[]) {
		this._list = val
		if (typeof window !== "undefined") {
			try {
				localStorage.setItem("seal_notifications", JSON.stringify(val))
			} catch (e) {
				console.error("Failed to save notifications to localStorage:", e)
			}
		}
	}

	get initializedRole() {
		return this._initializedRole
	}

	set initializedRole(val: string | null) {
		this._initializedRole = val
		if (typeof window !== "undefined") {
			try {
				if (val === null) {
					localStorage.removeItem("seal_notifications_role")
				} else {
					localStorage.setItem("seal_notifications_role", JSON.stringify(val))
				}
			} catch (e) {
				console.error("Failed to save initialized role to localStorage:", e)
			}
		}
	}

	initForRole(role: string) {
		if (typeof window === "undefined") return

		// If the role changed, clear first.
		if (this.initializedRole !== role) {
			this.initializedRole = role
			this.clearAll()
		}

		// If list is empty, load defaults
		if (this.list.length === 0) {
			let defaults: NotificationItem[] = []
			const now = new Date()

			if (role === "STUDENT") {
				defaults = [
					{
						id: "std-1",
						title: "Welcome to SEAL!",
						description: "Get started by checking out active seasons and events.",
						time: new Date(now.getTime() - 10 * 60000).toISOString(),
						unread: true,
						type: "info",
						link: "/student"
					},
					{
						id: "std-2",
						title: "Team Registration Open",
						description: "Create or join a team to participate in the hackathon.",
						time: new Date(now.getTime() - 60 * 60000).toISOString(),
						unread: true,
						type: "warning",
						link: "/student/teams"
					},
					{
						id: "std-3",
						title: "Profile Verified",
						description: "Your student status has been successfully verified.",
						time: new Date(now.getTime() - 24 * 60 * 60000).toISOString(),
						unread: false,
						type: "success",
						link: "/student/profile"
					}
				]
			} else if (role === "COORDINATOR") {
				defaults = [
					{
						id: "coord-1",
						title: "System Update",
						description: "The platform has been updated to version 1.1.0.",
						time: new Date(now.getTime() - 5 * 60000).toISOString(),
						unread: true,
						type: "info",
						link: "/coordinator"
					},
					{
						id: "coord-2",
						title: "Season Setup Required",
						description: "Please check Season 1 event timelines and round configurations.",
						time: new Date(now.getTime() - 180 * 60000).toISOString(),
						unread: true,
						type: "warning",
						link: "/coordinator/seasons"
					},
					{
						id: "coord-3",
						title: "Review Completed",
						description: "All pending team requests have been resolved.",
						time: new Date(now.getTime() - 2 * 24 * 60 * 60000).toISOString(),
						unread: false,
						type: "success",
						link: "/coordinator/teams"
					}
				]
			} else { // MENTOR, JUDGE, LECTURER
				defaults = [
					{
						id: "mentor-1",
						title: "Welcome Mentor!",
						description: "You have been registered as an expert for the upcoming hackathon.",
						time: new Date(now.getTime() - 20 * 60000).toISOString(),
						unread: true,
						type: "info",
						link: "/mentor"
					},
					{
						id: "mentor-2",
						title: "Evaluation Period",
						description: "Prepare to evaluate submissions for Round 1.",
						time: new Date(now.getTime() - 5 * 60 * 60000).toISOString(),
						unread: true,
						type: "warning",
						link: "/mentor"
					},
					{
						id: "mentor-3",
						title: "Profile Updated",
						description: "Your expert bio and details have been updated.",
						time: new Date(now.getTime() - 3 * 24 * 60 * 60000).toISOString(),
						unread: false,
						type: "success",
						link: "/mentor/profile"
					}
				]
			}

			this.list = defaults
		}

		// Start periodic simulation for live notifications
		this.startSimulation(role)
	}

	markAsRead(id: string) {
		this.list = this.list.map(item => (item.id === id ? { ...item, unread: false } : item))
	}

	markAllAsRead() {
		this.list = this.list.map(item => ({ ...item, unread: false }))
	}

	clearAll() {
		this.list = []
	}

	addNotification(item: Omit<NotificationItem, "id" | "unread">) {
		const newNotif: NotificationItem = {
			...item,
			id: "notif-" + Math.random().toString(36).substring(2, 9),
			unread: true
		}
		this.list = [newNotif, ...this.list]
	}

	startSimulation(role: string) {
		if (typeof window === "undefined") return
		if (this.simInterval) clearInterval(this.simInterval)

		this.simInterval = setInterval(() => {
			const now = new Date().toISOString()
			if (role === "STUDENT") {
				const randomMsg = [
					{
						title: "Team Invitation",
						description: "Team 'HackHustlers' invited you to join them.",
						link: "/student/teams",
						type: "info" as const
					},
					{
						title: "Review Feedback",
						description: "Your project submission has been reviewed by Mentor Alex.",
						link: "/student",
						type: "success" as const
					},
					{
						title: "Deadline Approaching",
						description: "Round 1 submission closes in 2 hours. Submit your work now!",
						link: "/student/submit-project",
						type: "error" as const
					}
				]
				const picked = randomMsg[Math.floor(Math.random() * randomMsg.length)]
				this.addNotification({ ...picked, time: now })
			} else if (role === "COORDINATOR") {
				const randomMsg = [
					{
						title: "New Team Registered",
						description: "Team 'DevDynasty' has submitted their registration.",
						link: "/coordinator/teams",
						type: "info" as const
					},
					{
						title: "Expert Joined",
						description: "Professor Liam registered as a Judge for Season 1.",
						link: "/coordinator/experts",
						type: "success" as const
					},
					{
						title: "System Alert",
						description: "Please audit inactive users registered over 30 days ago.",
						link: "/coordinator/users",
						type: "warning" as const
					}
				]
				const picked = randomMsg[Math.floor(Math.random() * randomMsg.length)]
				this.addNotification({ ...picked, time: now })
			} else {
				const randomMsg = [
					{
						title: "New Assignment",
						description: "You have been assigned to evaluate Team 'PixelPerfect'.",
						link: "/mentor",
						type: "info" as const
					},
					{
						title: "Evaluation Deadline",
						description: "Please complete all scoring by tonight at 11:59 PM.",
						link: "/mentor",
						type: "warning" as const
					},
					{
						title: "New Message",
						description: "Coordinator sent you a message regarding Round 1 criteria.",
						link: "/mentor/profile",
						type: "info" as const
					}
				]
				const picked = randomMsg[Math.floor(Math.random() * randomMsg.length)]
				this.addNotification({ ...picked, time: now })
			}
		}, 120000) // every 2 minutes
	}

	stopSimulation() {
		if (this.simInterval) {
			clearInterval(this.simInterval)
			this.simInterval = null
		}
	}

	clearRole() {
		this.stopSimulation()
		this.initializedRole = null
		this.clearAll()
	}
}

export const notificationsStore = new NotificationsStore()
