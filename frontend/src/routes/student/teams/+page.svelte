<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import {
		getSelfProfile,
		getAllInvites,
		acceptInvite,
		declineInvite,
		inviteToTeam,
		getAllSeasons,
		getEventsInSeason,
		getInterestedParticipants,
		getAllTeamsOfEvents,
		getAllTracksOfEvent,
		getRounds,
		getAllAccounts
	} from "$lib/api"
	import { goto } from "$app/navigation"
	import {
		Bell,
		CheckCircle,
		XCircle,
		Clock,
		Users,
		Star,
		UserPlus,
		LogOut,
		ArrowRightLeft
	} from "@lucide/svelte"

	let profile = $state<any>(null)
	let invites = $state<any[]>([])
	let myTeam = $state<any>(null)
	let isLoading = $state(true)
	let errorMessage = $state("")
	let processingId = $state<string | null>(null)

	let inviteStudentId = $state("")
	let isInviting = $state(false)
	let inviteMessage = $state("")
	let inviteError = $state(false)

	let studentUuid = $state<string | null>(null)
	let allParticipants = $state<any[]>([])
	let availableStudents = $state<any[]>([])
	let globalTeamsCache: any[] = []

	async function loadData() {
		try {
			// 1. Get profile
			const { data: profileData, response: profileRes } = await getSelfProfile({
				throwOnError: false
			})
			if (profileRes?.ok && profileData) {
				profile = profileData
			} else {
				goto("/auth/login")
				return
			}

			// Initialize team and invites to default values in case search yields nothing
			myTeam = null
			invites = []

			globalTeamsCache = []

			// 2. Fetch all seasons to find the student's team
			const { data: seasons, response: seasonsRes } = await getAllSeasons({ throwOnError: false })
			if (seasonsRes?.ok && seasons) {
				let resolvedTeam: any = null
				let resolvedStudentUuid = null
				let resolvedEventId = null
				let resolvedParticipants: any[] = []

				// Loop through seasons and events to locate the student's registration
				for (const season of seasons) {
					let allSeasonEvents: any[] = []

					// 2. Fetch from API
					const { data: events, response: eventsRes } = await getEventsInSeason({
						path: { seasonId: season.id },
						throwOnError: false
					})
					if (eventsRes?.ok && events) allSeasonEvents = events

					for (const event of allSeasonEvents) {
						let participantsList: any[] = []

						// 1. Check API for participants
						const { data: participants, response: participantsRes } =
							await getInterestedParticipants({
								path: { eventId: event.id },
								throwOnError: false
							})
						if (participantsRes?.ok && participants && participants.length > 0) {
							participantsList = participants
						}

						if (participantsList.length > 0) {
							const currentParticipant = participantsList.find(
								(p: any) => p.email === profile.email
							)
							if (currentParticipant) {
								resolvedStudentUuid = currentParticipant.id
								resolvedParticipants = participantsList

								let eventTeamsList: any[] = []
								// Always fetch teams to resolve invite team names
								const { data: teams, response: teamsRes } = await getAllTeamsOfEvents({
									path: { eventId: event.id } as any,
									throwOnError: false
								})
								if (teamsRes?.ok && teams && teams.length > 0) {
									eventTeamsList = teams
									globalTeamsCache.push(...teams)
								}
								// Store this globally or on the invite processing step so invites can use it
								// Since this is per-event, and invites are global, we will just use it to resolve resolvedTeam if they have one
								// But to map invite names, we should store a global map of teamId -> teamName
								eventTeamsList.forEach((t: any) => {
									if (typeof window !== "undefined") {
										// cache in session storage or just map it
									}
								})

								if (
									(currentParticipant.team_ids && currentParticipant.team_ids.length > 0) ||
									eventTeamsList.some(
										(t: any) =>
											t.leader_id === currentParticipant.id || t.leaderId === currentParticipant.id
									)
								) {
									if (eventTeamsList.length > 0) {
										const team = eventTeamsList.find(
											(t: any) =>
												(currentParticipant.team_ids &&
													currentParticipant.team_ids.includes(t.id)) ||
												t.leader_id === currentParticipant.id ||
												t.leaderId === currentParticipant.id
										)
										if (team) {
											resolvedTeam = team as any
											const teamMembers = participantsList
												.filter(
													(p: any) =>
														(p.team_ids && p.team_ids.includes(team.id)) ||
														team.leader_id === p.id ||
														team.leaderId === p.id
												)
												.map((p: any) => ({
													id: p.id,
													name: p.fullName || p.full_name || p.name || "Unknown",
													email: p.email,
													student_id: p.studentId || p.student_id || "",
													is_external: p.isExternal || p.is_external || false,
													school_name: p.schoolName || p.school_name || "",
													role:
														p.id === team.leader_id || p.id === team.leaderId ? "Leader" : "Member"
												}))
											resolvedTeam.members = teamMembers
											resolvedEventId = event.id
										}
									}
								}
								break
							}
						}
					}
					if (resolvedStudentUuid) break
				}

				if (resolvedTeam && resolvedEventId) {
					try {
						if (resolvedTeam.track_id || resolvedTeam.trackId) {
							const trackId = resolvedTeam.track_id || resolvedTeam.trackId
							const [tracksRes, accountsRes] = await Promise.all([
								getAllTracksOfEvent({
									path: { eventId: resolvedEventId },
									throwOnError: false
								}),
								getAllAccounts({
									throwOnError: false
								})
							])
							if (tracksRes.data && Array.isArray(tracksRes.data)) {
								const track = tracksRes.data.find((t: any) => t.id === trackId) as any
								if (track) {
									resolvedTeam.track_name = track.name || track.title || "Unknown Track"
									if (
										track.mentor_ids &&
										track.mentor_ids.length > 0 &&
										accountsRes.data &&
										Array.isArray(accountsRes.data)
									) {
										const mentors = accountsRes.data.filter((acc: any) =>
											track.mentor_ids.includes(acc.id)
										)
										resolvedTeam.mentors = mentors.map((m: any) => m.fullName || m.name || m.email)
									} else {
										resolvedTeam.mentors = []
									}
								}
							}
						}

						const { data: roundsData } = await getRounds({
							path: { eventId: resolvedEventId },
							throwOnError: false
						})
						if (roundsData && Array.isArray(roundsData)) {
							const activeRound = roundsData.find((r: any) => r.status === "ACTIVE") as any
							if (activeRound) {
								resolvedTeam.round_name = activeRound.name || activeRound.title || "Active Round"
							} else {
								resolvedTeam.round_name = "No Active Round"
							}
						}
					} catch (e) {
						console.error("Failed to fetch track or round info", e)
					}
				}

				studentUuid = resolvedStudentUuid
				myTeam = resolvedTeam
				allParticipants = resolvedParticipants
				availableStudents = resolvedParticipants.filter(
					(p: any) => !(p.team_ids && p.team_ids.length > 0) && p.id !== resolvedStudentUuid
				)
			}

			// 3. Get invites
			let invitesList: any[] = []
			const { data: invitesData, response: invitesRes } = await getAllInvites({
				throwOnError: false
			})
			if (invitesRes?.ok && invitesData) {
				invitesList = invitesData
			}

			if (invitesList.length > 0) {
				invites = invitesList.sort((a: any, b: any) => {
					if (a.status === "PENDING" && b.status !== "PENDING") return -1
					if (a.status !== "PENDING" && b.status === "PENDING") return 1
					return new Date(b.sent_at).getTime() - new Date(a.sent_at).getTime()
				})
			}

			// Fallback: If myTeam is null, but we have an ACCEPTED invite (fixes F5 refresh issue)
			if (!myTeam && invites.length > 0) {
				const acceptedInvite = invites.find((i: any) => i.status === "ACCEPTED")
				if (acceptedInvite && allParticipants.length > 0) {
					const targetTeamId = acceptedInvite.inviting_team_id
					const cachedTeam = globalTeamsCache.find((t: any) => t.id === targetTeamId)
					let fallbackTeam = cachedTeam ||
						acceptedInvite.team || {
							id: targetTeamId,
							name: targetTeamId,
							status: "APPROVED"
						}

					const existingMembers = allParticipants
						.filter((p: any) => p.team_ids && p.team_ids.includes(targetTeamId))
						.map((p: any) => ({
							id: p.id,
							name: p.fullName || p.full_name || p.name || "Unknown",
							email: p.email,
							student_id: p.studentId || p.student_id || "",
							is_external: p.isExternal || p.is_external || false,
							school_name: p.schoolName || p.school_name || "",
							role:
								p.id === fallbackTeam.leader_id || p.id === fallbackTeam.leaderId
									? "Leader"
									: "Member"
						}))

					const me = {
						id: profile.id,
						name: profile.fullName || profile.full_name || profile.name || "Me",
						email: profile.email,
						role: "Member"
					}

					if (!existingMembers.some((m: any) => m.id === me.id || m.email === me.email)) {
						fallbackTeam.members = [...existingMembers, me]
					} else {
						fallbackTeam.members = existingMembers
					}

					myTeam = fallbackTeam
				}
			}
		} catch (error) {
			errorMessage = "Error connecting to the server."
			console.error("loadData error:", error)
		} finally {
			isLoading = false
		}
	}

	onMount(() => {
		loadData()
	})

	async function handleAccept(inviteId: string) {
		processingId = inviteId
		try {
			const { response: res } = await acceptInvite({
				path: { inviteId },
				throwOnError: false
			})
			if (res?.ok) {
				// Manually extract team from invite just in case loadData fails to fetch it (due to GET cache)
				const acceptedInvite = invites.find((i) => i.id === inviteId)
				await loadData()
				if (!myTeam && acceptedInvite) {
					// Find the team in the teams list we just fetched (if it was cached) or from the invite
					const targetTeamId = acceptedInvite.inviting_team_id
					const cachedTeam = globalTeamsCache.find((t: any) => t.id === targetTeamId)
					let fallbackTeam = cachedTeam ||
						acceptedInvite.team || { id: targetTeamId, name: targetTeamId }

					// Find existing members in allParticipants
					const existingMembers = allParticipants
						.filter((p: any) => p.team_ids && p.team_ids.includes(targetTeamId))
						.map((p: any) => ({
							id: p.id,
							name: p.fullName || p.full_name || p.name || "Unknown",
							email: p.email,
							student_id: p.studentId || p.student_id || "",
							is_external: p.isExternal || p.is_external || false,
							school_name: p.schoolName || p.school_name || "",
							role:
								p.id === fallbackTeam.leader_id || p.id === fallbackTeam.leaderId
									? "Leader"
									: "Member"
						}))

					// Add the current user
					const me = {
						id: profile.id,
						name: profile.fullName || "Me",
						email: profile.email,
						role: "Member"
					}

					fallbackTeam.members = [...existingMembers, me]
					myTeam = fallbackTeam
				}
			} else {
				errorMessage = "Failed to accept invite: " + (res?.statusText || "Unknown error")
			}
		} catch (error) {
			alert("Error accepting invite.")
		} finally {
			processingId = null
		}
	}

	async function handleDecline(inviteId: string) {
		if (!confirm("Are you sure you want to decline this invite? This action is irreversible."))
			return
		processingId = inviteId
		try {
			const { response: res } = await declineInvite({
				path: { inviteId },
				throwOnError: false
			})
			if (res?.ok) {
				const idx = invites.findIndex((i) => i.id === inviteId)
				if (idx !== -1) invites[idx].status = "DECLINED"
			} else {
				alert("Failed to decline invite.")
			}
		} catch (error) {
			alert("Error declining invite.")
		} finally {
			processingId = null
		}
	}

	async function handleInviteMember(e?: Event) {
		if (e) e.preventDefault()
		if (!inviteStudentId.trim()) return

		isInviting = true
		inviteMessage = ""
		inviteError = false

		try {
			const invitee = allParticipants.find(
				(p: any) =>
					(p.studentId &&
						p.studentId.trim().toLowerCase() === inviteStudentId.trim().toLowerCase()) ||
					(p.student_id &&
						p.student_id.trim().toLowerCase() === inviteStudentId.trim().toLowerCase()) ||
					(p.email && p.email.trim().toLowerCase() === inviteStudentId.trim().toLowerCase())
			)

			if (!invitee) {
				inviteError = true
				inviteMessage =
					"Failed to send invite: Student not found or has not registered/marked interest in this event."
				isInviting = false
				return
			}

			const { response: res, error } = await inviteToTeam({
				path: {
					teamId: myTeam.id,
					studentId: invitee.id
				},
				throwOnError: false
			})
			if (res?.ok) {
				inviteMessage = "Invitation sent successfully!"
				inviteStudentId = ""
			} else {
				inviteError = true
				const errorDetails = error || (await res?.json().catch(() => null))
				inviteMessage = `Failed to send invite: ${errorDetails?.detail || errorDetails?.message || errorDetails?.title || res?.statusText || "User may already be in a team or has pending invites."}`
			}
		} catch (err: any) {
			inviteError = true
			inviteMessage = `Error sending invite: ${err?.message || "Unknown error"}`
		} finally {
			isInviting = false
		}
	}

	function formatDate(dateString: string) {
		return new Intl.DateTimeFormat("en-US", {
			month: "short",
			day: "numeric",
			hour: "2-digit",
			minute: "2-digit"
		}).format(new Date(dateString))
	}

	function handleLeaveTeam() {
		// BR-33: Leader cannot leave without transferring leadership
		if (myTeam && studentUuid === myTeam.leader_id) {
			alert(
				"Error: You are the team leader. You must transfer leadership to another member before leaving the team."
			)
			return
		}

		// BR-32: Cannot leave team after season has officially begun (ACTIVE status)
		// Assuming we can check season status if we had it, but for now we enforce the rule in UI
		if (myTeam && myTeam.status === "ACTIVE") {
			alert("Error: You cannot leave the team after the season has officially begun.")
			return
		}

		alert(`The "Leave Team" action is not currently supported by the backend API.`)
	}

	function handleTransferLeadership() {
		alert(`The "Transfer Leadership" action is not currently supported by the backend API.`)
	}
</script>

<svelte:head>
	<title>Teams - Student</title>
</svelte:head>

<div class="teams-page {theme.darkMode ? 'teams-page--dark' : ''}">
	<a href="/student" class="back-link">
		<svg class="back-link__icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M10 19l-7-7m0 0l7-7m-7 7h18"
			></path>
		</svg>
		Back to Dashboard
	</a>

	<div class="page-header">
		<div class="page-header__icon-wrap">
			<Users class="page-header__icon" />
		</div>
		<div class="page-header__text">
			<h1 class="page-header__title">Teams</h1>
			<p class="page-header__subtitle">Manage your team or pending invitations</p>
		</div>
	</div>

	{#if !myTeam && !isLoading}
		<div class="actions-bar">
			<a href="/student/create-team" class="btn btn--primary btn--create">
				<Users class="btn__icon" />
				Create New Team
			</a>
		</div>
	{/if}

	{#if isLoading}
		<div class="loading-block">
			<div class="loading-block__spinner"></div>
		</div>
	{:else if errorMessage}
		<div class="error-box">
			<p class="error-box__text">{errorMessage}</p>
		</div>
	{:else}
		{#if myTeam}
			<div class="team-card">
				<div class="team-card__top">
					<div class="team-card__identity">
						<div class="team-card__badge-icon">
							<Star class="team-card__badge-svg" />
						</div>
						<div>
							<h3 class="team-card__eyebrow">My Current Team</h3>
							<h2 class="team-card__name">{myTeam.name || "Untitled Team"}</h2>
						</div>
					</div>

					<div class="team-card__status-actions">
						{#if myTeam.status === "PENDING"}
							<span class="status-pill status-pill--pending">Status: Pending Approval</span>
						{:else if myTeam.status === "APPROVED"}
							<span class="status-pill status-pill--approved">Status: Approved</span>
						{/if}

						{#if myTeam.leader_id === profile.id}
							<a href="/student/submit-project" class="btn btn--primary btn--sm btn--submission">
								<svg class="btn__icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"
									><path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
									></path></svg
								>
								Submission
							</a>
						{/if}
					</div>
				</div>

				<div class="info-grid info-grid--2">
					<div class="info-card">
						<p class="info-card__label">Team ID</p>
						<p class="info-card__value info-card__value--mono">{myTeam.id}</p>
					</div>
					<div class="info-card">
						<p class="info-card__label">My Role</p>
						<p
							class="info-card__value"
							class:info-card__value--orange={studentUuid === myTeam.leader_id ||
								studentUuid === myTeam.leaderId}
							class:info-card__value--green={!(
								studentUuid === myTeam.leader_id || studentUuid === myTeam.leaderId
							)}
						>
							{studentUuid === myTeam.leader_id || studentUuid === myTeam.leaderId
								? "Team Leader"
								: "Member"}
						</p>
					</div>
				</div>

				<div class="info-grid info-grid--3">
					<div class="info-card">
						<p class="info-card__label">Track</p>
						<p class="info-card__value">{myTeam.track_name || "N/A"}</p>
					</div>
					<div class="info-card">
						<p class="info-card__label">Mentor(s)</p>
						{#if myTeam.mentors && myTeam.mentors.length > 0}
							<div class="tag-list">
								{#each myTeam.mentors as mentor}
									<span class="tag tag--violet">{mentor}</span>
								{/each}
							</div>
						{:else}
							<p class="info-card__value">N/A</p>
						{/if}
					</div>
					<div class="info-card">
						<p class="info-card__label">Active Round</p>
						<p class="info-card__value info-card__value--blue">{myTeam.round_name || "N/A"}</p>
					</div>
				</div>

				<div class="members-section">
					<div class="members-section__header">
						<h3 class="members-section__title">Team Members ({myTeam.members?.length || 1}/5)</h3>
					</div>

					<div class="members-list">
						{#if myTeam.members && myTeam.members.length > 0}
							{#each myTeam.members as member}
								<div class="member-row">
									<div class="member-row__details">
										<!-- Name -->
										<div class="member-row__cell member-row__cell--name">
											<span class="member-row__name">{member.name || "Unknown Member"}</span>
											{#if member.id === studentUuid}
												<span class="member-row__you">(You)</span>
											{/if}
										</div>
										<span class="member-row__sep">|</span>
										<!-- Student ID -->
										<div class="member-row__cell member-row__cell--id">
											{#if member.student_id}
												<span class="member-row__id-badge">{member.student_id}</span>
											{:else}
												<span class="member-row__none">NONE</span>
											{/if}
										</div>
										<span class="member-row__sep">|</span>
										<!-- Email -->
										<div class="member-row__cell member-row__cell--email">
											{member.email || "No Email"}
										</div>
										<span class="member-row__sep">|</span>
										<!-- School -->
										<div class="member-row__cell member-row__cell--school">
											{#if member.is_external}
												<span class="member-row__school member-row__school--external"
													>{member.school_name || "External"}</span
												>
											{:else}
												<span class="member-row__school member-row__school--fpt"
													>FPT University</span
												>
											{/if}
										</div>
									</div>

									<div class="member-row__role">
										{#if member.role === "Leader" || member.id === myTeam.leader_id || member.id === myTeam.leaderId}
											<span class="role-badge role-badge--leader">Leader</span>
										{:else}
											<span class="role-badge role-badge--member">Member</span>
										{/if}
									</div>
								</div>
							{/each}
						{/if}
					</div>

					{#if studentUuid === myTeam.leader_id}
						<div class="invite-panel">
							<h4 class="invite-panel__title">
								<UserPlus class="invite-panel__title-icon" />
								Invite New Member
							</h4>
							<p class="invite-panel__desc">
								Enter a student ID to invite them to your team. Teams can have up to 5 members.
							</p>

							{#if (myTeam.members?.length || 1) >= 5}
								<div class="alert alert--warning">
									Your team has reached the maximum size limit (5 members).
								</div>
							{:else}
								<form onsubmit={handleInviteMember} class="invite-form">
									<input
										type="text"
										bind:value={inviteStudentId}
										placeholder="Enter student ID (e.g. SE160123)..."
										required
										class="form-input form-input--flex"
									/>
									<button
										type="submit"
										disabled={isInviting || !inviteStudentId.trim()}
										class="btn btn--primary btn--invite"
									>
										{isInviting ? "Sending..." : "Send Invite"}
									</button>
								</form>

								{#if inviteMessage}
									<p
										class="form-message"
										class:form-message--error={inviteError}
										class:form-message--success={!inviteError}
									>
										{inviteMessage}
									</p>
								{/if}

								<div class="available-section">
									<h5 class="available-section__title">
										Students Without a Team ({availableStudents.length})
									</h5>
									{#if availableStudents.length > 0}
										<div class="students-list">
											{#each availableStudents as student}
												<div class="student-row">
													<div class="student-row__info">
														<p class="student-row__name">
															{student.fullName || student.full_name || student.name || "Unknown"}
														</p>
														<p class="student-row__meta">
															{student.studentId || student.student_id || student.email || "No ID"}
														</p>
													</div>
													<button
														onclick={() => {
															inviteStudentId =
																student.studentId || student.student_id || student.email || ""
															handleInviteMember()
														}}
														class="btn btn--primary btn--xs"
													>
														Invite
													</button>
												</div>
											{/each}
										</div>
									{:else}
										<div class="empty-box">
											<p class="empty-box__text">
												There are no available students without a team to invite right now.
											</p>
										</div>
									{/if}
								</div>
							{/if}
						</div>

						<div class="team-actions">
							<button onclick={handleTransferLeadership} class="btn btn--ghost">
								<ArrowRightLeft class="btn__icon-sm" />
								Transfer Leadership
							</button>
						</div>
					{/if}

					<div class="leave-action">
						<button onclick={handleLeaveTeam} class="btn btn--danger-ghost">
							<LogOut class="btn__icon-sm" />
							Leave Team
						</button>
					</div>
				</div>
			</div>
		{:else}
			{#if invites.length === 0}
				<div class="empty-invites">
					<Users class="empty-invites__icon" />
					<h3 class="empty-invites__title">No Pending Invitations</h3>
					<p class="empty-invites__desc">You haven't received any team invitations yet.</p>
				</div>
			{:else}
				<div class="invites-list">
					{#each invites as invite}
						<div class="invite-card">
							<div class="invite-card__main">
								<div class="invite-card__info">
									<div class="invite-card__icon-wrap">
										<Users class="invite-card__icon" />
									</div>
									<div class="invite-card__text">
										<h4 class="invite-card__title">Team Invitation</h4>
										<p class="invite-card__desc">
											You have been invited to join Team <span class="invite-card__team"
												>{invite.team?.name || invite.inviting_team_id || "Unknown"}</span
											>.
										</p>
										<div class="invite-card__meta">
											<span class="invite-card__date"
												><Clock class="invite-card__date-icon" /> Sent: {formatDate(
													invite.sent_at
												)}</span
											>
											{#if invite.status?.toUpperCase() === "PENDING"}
												<span class="status-pill status-pill--sm status-pill--pending">Pending</span
												>
											{:else if invite.status?.toUpperCase() === "ACCEPTED"}
												<span class="status-pill status-pill--sm status-pill--success"
													>Accepted</span
												>
											{:else if invite.status?.toUpperCase() === "DECLINED"}
												<span class="status-pill status-pill--sm status-pill--declined"
													>Declined</span
												>
											{/if}
										</div>
									</div>
								</div>

								{#if invite.status?.toUpperCase() === "PENDING"}
									<div class="invite-card__actions">
										<button
											onclick={() => handleAccept(invite.id)}
											disabled={processingId === invite.id}
											class="btn btn--success btn--sm"
										>
											<CheckCircle class="btn__icon-sm" />
											{processingId === invite.id ? "Processing..." : "Accept"}
										</button>
										<button
											onclick={() => handleDecline(invite.id)}
											disabled={processingId === invite.id}
											class="btn btn--ghost btn--sm"
										>
											<XCircle class="btn__icon-sm" />
											Decline
										</button>
									</div>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			{/if}
		{/if}
	{/if}
</div>

<style lang="scss">
	// ============================================================================
	// Teams Page - SCSS Conversion from Tailwind
	// ============================================================================
	$bp-sm: 640px;
	$bp-md: 768px;

	// Page wrapper
	.teams-page {
		max-width: 56rem; // max-w-4xl
		margin-left: auto;
		margin-right: auto;
		width: 100%;
		padding: 1rem; // p-4

		@media (min-width: $bp-md) {
			padding: 2rem;
		} // md:p-8

		// dark modifier - applied via {theme.darkMode ? 'teams-page--dark' : ''}
		&--dark {
			// context for nested dark styles if needed
		}
	}

	// Back link
	// inline-flex items-center gap-2 transition-colors mb-8 font-medium text-zinc-400 hover:text-orange-400 / gray-500 hover:orange-600
	.back-link {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		transition: color 0.2s ease;
		margin-bottom: 2rem;
		font-weight: 500;
		font-size: 0.9rem;
		text-decoration: none;
		cursor: pointer;
		color: #6b7280; // text-gray-500

		&:hover {
			color: #ea580c;
		} // hover:text-orange-600

		.teams-page--dark & {
			color: #a1a1aa; // text-zinc-400
			&:hover {
				color: #fb923c;
			} // hover:text-orange-400
		}

		&__icon {
			width: 1.25rem; // w-5 h-5
			height: 1.25rem;
		}
	}

	// Page header
	// flex items-center gap-3 mb-8
	.page-header {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-bottom: 2rem;

		&__icon-wrap {
			padding: 0.75rem; // p-3
			border-radius: 0.75rem; // rounded-xl
			background: #ffedd5; // bg-orange-100
			color: #ea580c; // text-orange-600

			.teams-page--dark & {
				background: rgba(67, 20, 7, 0.4); // bg-orange-950/40
				color: #fb923c; // text-orange-400
			}
		}
		&__icon {
			width: 1.5rem;
			height: 1.5rem;
		}

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			color: #1f2937; // text-gray-800
			.teams-page--dark & {
				color: #f4f4f5;
			} // text-zinc-100
		}
		&__subtitle {
			font-size: 0.875rem;
			color: #6b7280;
			.teams-page--dark & {
				color: #a1a1aa;
			}
		}
	}

	// Actions bar
	.actions-bar {
		margin-bottom: 1.5rem;
		display: flex;
		justify-content: flex-end;
	}

	// Buttons - shared
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		font-weight: 600;
		border-radius: 0.75rem; // rounded-xl
		transition: all 0.2s ease;
		cursor: pointer;
		border: none;
		text-decoration: none;
		white-space: nowrap;

		&:disabled {
			opacity: 0.5;
			cursor: not-allowed;
		}
		&:active:not(:disabled) {
			transform: scale(0.95);
		}

		&__icon {
			width: 1.25rem;
			height: 1.25rem;
		}
		&__icon-sm {
			width: 1rem;
			height: 1rem;
		}

		&--primary {
			background: #f26f21;
			color: #fff;
			padding: 0.625rem 1.25rem; // px-5 py-2.5
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
			&:hover {
				background: #d85c14;
			}
		}

		&--sm {
			padding: 0.375rem 1rem; // px-4 py-1.5
			border-radius: 0.5rem; // rounded-lg
			font-size: 0.875rem;
			font-weight: 700;
		}

		&--xs {
			font-size: 0.75rem;
			font-weight: 700;
			padding: 0.5rem 1.125rem; // px-4.5 py-2
			border-radius: 0.75rem;
		}

		&--create {
			@extend .btn--primary;
		}

		&--submission {
			@extend .btn--primary;
			padding: 0.375rem 1rem;
			border-radius: 0.5rem;
			font-size: 0.875rem;
		}

		&--invite {
			padding: 0.75rem 1.25rem;
			min-width: 120px;
			background: #f97316; // orange-500
			color: #fff;
			font-weight: 600;
			border-radius: 0.75rem;
			&:hover {
				background: #ea580c;
			}
		}

		&--success {
			background: #22c55e; // green-500
			color: #fff;
			padding: 0.5rem 1rem;
			border-radius: 0.5rem;
			font-size: 0.875rem;
			font-weight: 500;
			&:hover {
				background: #16a34a;
			}
		}

		&--ghost {
			background: transparent;
			border: 1px solid #d1d5db;
			color: #374151;
			padding: 0.5rem 1rem;
			border-radius: 0.5rem;
			font-size: 0.875rem;
			font-weight: 600;
			&:hover {
				background: #f3f4f6;
			}

			.teams-page--dark & {
				border-color: #3f3f46; // zinc-700
				color: #d4d4d8; // zinc-300
				&:hover {
					background: #27272a;
				} // zinc-800
			}
		}

		&--danger-ghost {
			background: transparent;
			color: #ef4444;
			padding: 0.5rem 1rem;
			border-radius: 0.5rem;
			font-size: 0.875rem;
			font-weight: 600;
			&:hover {
				background: #fef2f2;
			}
			.teams-page--dark & {
				&:hover {
					background: rgba(127, 29, 29, 0.3);
				}
			}
		}
	}

	// Loading
	.loading-block {
		display: flex;
		justify-content: center;
		padding: 3rem 0; // py-12
		&__spinner {
			width: 2rem;
			height: 2rem; // h-8 w-8
			border-radius: 9999px;
			border-top: 2px solid #f97316;
			border-bottom: 2px solid #f97316;
			border-left: 2px solid transparent;
			border-right: 2px solid transparent;
			animation: spin 1s linear infinite;
		}
	}
	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	// Error box - bg-red-50 border-l-4 border-red-500 p-4 rounded-r shadow-sm
	.error-box {
		background: #fef2f2; // bg-red-50
		border-left: 4px solid #ef4444; // border-red-500
		padding: 1rem;
		border-radius: 0 0.375rem 0.375rem 0;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		&__text {
			font-size: 0.875rem;
			color: #b91c1c; // text-red-700
		}
	}

	// Team card - mb-8 p-6 md:p-8 rounded-3xl border transition-all
	// dark: bg-zinc-900 border-orange-900/50 shadow-[0_4px_30px_rgba(234,88,12,0.1)]
	// light: bg-white border-gray-200 shadow-sm
	.team-card {
		margin-bottom: 2rem;
		padding: 1.5rem;
		border-radius: 1.5rem; // rounded-3xl
		border: 1px solid #e5e7eb; // border-gray-200
		background: #fff;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		transition: all 0.3s ease;

		@media (min-width: $bp-md) {
			padding: 2rem;
		} // md:p-8

		.teams-page--dark & {
			background: #18181b; // bg-zinc-900
			border-color: rgba(124, 45, 18, 0.5); // border-orange-900/50
			box-shadow: 0 4px 30px rgba(234, 88, 12, 0.1);
		}

		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 1rem;
			flex-wrap: wrap;
			margin-bottom: 1rem;
		}

		&__identity {
			display: flex;
			align-items: center;
			gap: 0.75rem;
		}

		&__badge-icon {
			padding: 0.625rem; // p-2.5
			border-radius: 0.75rem; // rounded-xl
			background: #f97316; // bg-orange-500
			color: #fff;
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		}
		&__badge-svg {
			width: 1.5rem;
			height: 1.5rem;
		}

		&__eyebrow {
			font-size: 0.75rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			color: #ea580c;
			.teams-page--dark & {
				color: #fb923c;
			}
		}

		&__name {
			font-size: 1.5rem;
			font-weight: 700;
			color: #111827;
			@media (min-width: $bp-md) {
				font-size: 1.875rem;
			}
			.teams-page--dark & {
				color: #f4f4f5;
			}
		}

		&__status-actions {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			flex-wrap: wrap;
		}
	}

	// Status pills
	// text-amber-800 bg-amber-100/70 px-3 py-1.5 rounded-lg border font-bold text-sm shadow-sm
	.status-pill {
		padding: 0.375rem 0.75rem;
		border-radius: 0.5rem; // rounded-lg
		border-width: 1px;
		border-style: solid;
		font-weight: 700;
		font-size: 0.875rem;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		display: inline-flex;
		align-items: center;

		&--sm {
			font-size: 0.75rem;
			padding: 0.125rem 0.5rem;
			font-weight: 600;
			border-radius: 0.375rem;
		}

		&--pending {
			color: #92400e;
			background: rgba(254, 243, 199, 0.7);
			border-color: #fde68a;
			.teams-page--dark & {
				color: #fbbf24;
				background: rgba(245, 158, 11, 0.1);
				border-color: rgba(245, 158, 11, 0.2);
			}
		}
		&--approved,
		&--success {
			color: #065f46;
			background: rgba(209, 250, 229, 0.7);
			border-color: #a7f3d0;
			.teams-page--dark & {
				color: #34d399;
				background: rgba(16, 185, 129, 0.1);
				border-color: rgba(16, 185, 129, 0.2);
			}
		}
		&--declined {
			color: #b91c1c;
			background: #fef2f2;
			border-color: #fecaca;
			.teams-page--dark & {
				color: #f87171;
				background: rgba(127, 29, 29, 0.3);
				border-color: rgba(127, 29, 29, 0.5);
			}
		}
	}

	// Info grid
	.info-grid {
		display: grid;
		gap: 1rem;
		margin-top: 2rem;
		margin-bottom: 2rem;

		&--2 {
			grid-template-columns: 1fr;
			@media (min-width: $bp-md) {
				grid-template-columns: repeat(2, 1fr);
			}
		}
		&--3 {
			grid-template-columns: 1fr;
			@media (min-width: $bp-md) {
				grid-template-columns: repeat(3, 1fr);
			}
		}
	}

	.info-card {
		padding: 1.25rem; // p-5
		border-radius: 1rem; // rounded-2xl
		border: 1px solid #f3f4f6; // border-gray-100
		background: #fff;

		.teams-page--dark & {
			background: rgba(9, 9, 11, 0.5);
			border-color: #27272a;
		} // bg-zinc-950/50 border-zinc-800

		&__label {
			font-size: 0.75rem;
			color: #9ca3af; // text-gray-400
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.05em;
			margin-bottom: 0.5rem;
		}

		&__value {
			font-weight: 600;
			font-size: 0.875rem;
			color: #374151;
			.teams-page--dark & {
				color: #d4d4d8;
			}
			&--mono {
				font-family: monospace;
				font-size: 0.875rem;
			}
			&--orange {
				color: #f97316;
			}
			&--green {
				color: #22c55e;
			}
			&--blue {
				color: #3b82f6;
			}
		}
	}

	.tag-list {
		display: flex;
		flex-wrap: wrap;
		gap: 0.375rem;
		margin-top: 0.25rem;
	}
	.tag {
		display: inline-flex;
		align-items: center;
		font-size: 0.75rem;
		font-weight: 700;
		padding: 0.25rem 0.625rem;
		border-radius: 0.5rem; // rounded-lg
		&--violet {
			background: #ede9fe;
			color: #6d28d9;
			.teams-page--dark & {
				background: rgba(139, 92, 246, 0.1);
				color: #a78bfa;
			}
		}
	}

	// Members
	.members-section {
		border-top: 1px solid #e5e7eb;
		padding-top: 2rem;
		.teams-page--dark & {
			border-color: #27272a;
		}

		&__header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 1rem;
		}
		&__title {
			font-size: 1.125rem;
			font-weight: 700;
			color: #1f2937;
			.teams-page--dark & {
				color: #f4f4f5;
			}
		}
	}

	.members-list {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
		margin-bottom: 2rem;
	}

	.member-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 1rem;
		border-radius: 0.75rem; // rounded-xl
		border: 1px solid #f3f4f6;
		background: #fff;

		.teams-page--dark & {
			background: rgba(9, 9, 11, 0.5);
			border-color: #27272a;
		}

		&__details {
			width: 100%;
			margin-right: 1rem;
			overflow: hidden;
			display: grid;
			grid-template-columns: 1fr;
			gap: 0.5rem 0;
			font-size: 0.875rem;

			@media (min-width: $bp-md) {
				grid-template-columns:
					minmax(140px, 2fr) auto minmax(90px, 1fr) auto minmax(160px, 3fr)
					auto minmax(100px, 1.5fr);
				align-items: center;
				column-gap: 1rem;
			}
		}

		&__cell {
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
			&--name {
				display: flex;
				align-items: center;
				gap: 0.5rem;
			}
			&--id {
			}
			&--email {
				font-size: 0.75rem;
				color: #4b5563;
				.teams-page--dark & {
					color: #a1a1aa;
				}
			}
			&--school {
			}
		}

		&__name {
			font-weight: 700;
		}
		&__you {
			font-size: 0.75rem;
			font-weight: 500;
			color: #6b7280;
			flex-shrink: 0;
		}
		&__sep {
			color: #d1d5db;
			display: none;
			@media (min-width: $bp-md) {
				display: inline;
			}
			.teams-page--dark & {
				color: #3f3f46;
			}
		}
		&__id-badge {
			font-family: monospace;
			background: #f4f4f5;
			padding: 0.125rem 0.5rem;
			border-radius: 0.25rem;
			font-size: 0.6875rem;
			font-weight: 600;
			color: #3f3f46;
			.teams-page--dark & {
				background: rgba(39, 39, 42, 0.8);
				color: #d4d4d8;
			}
		}
		&__none {
			font-size: 0.75rem;
			color: #6b7280;
			font-family: monospace;
		}
		&__school {
			font-size: 0.75rem;
			font-weight: 500;
			&--external {
				color: #f97316;
				.teams-page--dark & {
					color: #fb923c;
				}
			}
			&--fpt {
				color: #ea580c;
				.teams-page--dark & {
					color: #fb923c;
				}
			}
		}

		&__role {
			flex-shrink: 0;
			display: flex;
			align-items: center;
		}
	}

	.role-badge {
		display: inline-flex;
		justify-content: center;
		width: 5rem;
		font-size: 0.75rem;
		font-weight: 700;
		padding: 0.375rem 0;
		border-radius: 0.375rem;
		&--leader {
			color: #ea580c;
			background: #ffedd5;
			.teams-page--dark & {
				color: #fb923c;
				background: rgba(251, 146, 60, 0.1);
			}
		}
		&--member {
			color: #fff;
			background: #000;
			border: 1px solid transparent;
			.teams-page--dark & {
				background: #27272a;
				border-color: #3f3f46;
			}
		}
	}

	// Invite panel - p-6 rounded-2xl border bg-gray-50 border-gray-200 / bg-zinc-950/80 border-zinc-800
	.invite-panel {
		padding: 1.5rem;
		border-radius: 1rem;
		border: 1px solid #e5e7eb;
		background: #f9fafb;

		.teams-page--dark & {
			background: rgba(9, 9, 11, 0.8);
			border-color: #27272a;
		}

		&__title {
			font-weight: 700;
			margin-bottom: 0.5rem;
			display: flex;
			align-items: center;
			gap: 0.5rem;
			color: #1f2937;
			.teams-page--dark & {
				color: #f4f4f5;
			}
		}
		&__title-icon {
			width: 1.25rem;
			height: 1.25rem;
			color: #f97316;
		}
		&__desc {
			font-size: 0.875rem;
			margin-bottom: 1rem;
			color: #6b7280;
			.teams-page--dark & {
				color: #a1a1aa;
			}
		}
	}

	.alert {
		padding: 0.75rem;
		border-radius: 0.75rem;
		font-size: 0.875rem;
		font-weight: 500;
		border: 1px solid;
		&--warning {
			background: #fffbeb;
			color: #d97706;
			border-color: #fde68a;
			.teams-page--dark & {
				background: rgba(120, 53, 15, 0.3);
				color: #fbbf24;
				border-color: rgba(120, 53, 15, 0.5);
			}
		}
	}

	.invite-form {
		display: flex;
		gap: 0.75rem;
	}

	.form-input {
		border-radius: 0.75rem;
		padding: 0.75rem;
		border: 1px solid #d1d5db;
		outline: none;
		transition: all 0.2s;
		background: #fff;
		color: #111827;
		&:focus {
			box-shadow: 0 0 0 2px #f97316;
			border-color: transparent;
		}

		.teams-page--dark & {
			background: #18181b;
			border-color: #3f3f46;
			color: #f4f4f5;
		}

		&--flex {
			flex: 1;
		}
	}

	.form-message {
		margin-top: 0.75rem;
		font-size: 0.875rem;
		font-weight: 500;
		&--error {
			color: #ef4444;
		}
		&--success {
			color: #22c55e;
		}
	}

	.available-section {
		margin-top: 1.5rem;
		border-top: 1px solid #e5e7eb;
		padding-top: 1rem;
		.teams-page--dark & {
			border-color: #27272a;
		}

		&__title {
			font-size: 0.875rem;
			font-weight: 700;
			margin-bottom: 0.75rem;
			display: flex;
			align-items: center;
			gap: 0.5rem;
			color: #374151;
			.teams-page--dark & {
				color: #e4e4e7;
			}
		}
	}

	.students-list {
		max-height: 15rem;
		overflow-y: auto;
		padding-right: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		// custom scrollbar
		&::-webkit-scrollbar {
			width: 6px;
		}
		&::-webkit-scrollbar-thumb {
			background: #d1d5db;
			border-radius: 3px;
		}
	}

	.student-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0.75rem;
		border-radius: 0.75rem;
		border: 1px solid #f3f4f6;
		background: #fff;
		transition: border-color 0.2s;
		&:hover {
			border-color: #fed7aa;
		}

		.teams-page--dark & {
			background: #18181b;
			border-color: #27272a;
			&:hover {
				border-color: #3f3f46;
			}
		}

		&__name {
			font-size: 0.875rem;
			font-weight: 700;
			color: #1f2937;
			.teams-page--dark & {
				color: #e4e4e7;
			}
		}
		&__meta {
			font-size: 0.75rem;
			color: #6b7280;
			margin-top: 0.125rem;
		}
	}

	.empty-box {
		padding: 1rem;
		text-align: center;
		border-radius: 0.75rem;
		border: 1px dashed #e5e7eb;
		background: #f9fafb;
		.teams-page--dark & {
			background: rgba(9, 9, 11, 0.5);
			border-color: #27272a;
		}
		&__text {
			font-size: 0.875rem;
			color: #6b7280;
			.teams-page--dark & {
				color: #a1a1aa;
			}
		}
	}

	.team-actions {
		margin-top: 2rem;
		display: flex;
		gap: 1rem;
		border-top: 1px solid #e5e7eb;
		padding-top: 1.5rem;
		.teams-page--dark & {
			border-color: #27272a;
		}
	}

	.leave-action {
		margin-top: 1rem;
		display: flex;
		gap: 1rem;
	}

	// Empty invites - text-center py-16 border-2 border-dashed rounded-2xl
	.empty-invites {
		text-align: center;
		padding: 4rem 0;
		border: 2px dashed #e5e7eb;
		border-radius: 1rem;
		color: #9ca3af;
		.teams-page--dark & {
			border-color: #27272a;
			color: #71717a;
		}

		&__icon {
			width: 3rem;
			height: 3rem;
			margin: 0 auto 1rem;
			opacity: 0.5;
		}
		&__title {
			font-size: 1.125rem;
			font-weight: 500;
			color: inherit;
		}
		&__desc {
			font-size: 0.875rem;
			margin-top: 0.25rem;
		}
	}

	// Invites list
	.invites-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.invite-card {
		padding: 1.25rem;
		border-radius: 1rem;
		border: 1px solid #f3f4f6;
		background: #fff;
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
		transition: all 0.2s;
		&:hover {
			box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
		}

		.teams-page--dark & {
			background: #18181b;
			border-color: #27272a;
			&:hover {
				border-color: #3f3f46;
			}
		}

		&__main {
			display: flex;
			flex-direction: column;
			gap: 1rem;
			@media (min-width: $bp-sm) {
				flex-direction: row;
				align-items: center;
				justify-content: space-between;
			}
		}
		&__info {
			display: flex;
			align-items: flex-start;
			gap: 1rem;
		}
		&__icon-wrap {
			padding: 0.625rem;
			border-radius: 9999px;
			margin-top: 0.25rem;
			background: #eff6ff;
			color: #2563eb;
			.teams-page--dark & {
				background: rgba(30, 58, 138, 0.4);
				color: #60a5fa;
			}
		}
		&__icon {
			width: 1.25rem;
			height: 1.25rem;
		}

		&__title {
			font-weight: 600;
			font-size: 1.125rem;
			color: #111827;
			.teams-page--dark & {
				color: #f4f4f5;
			}
		}
		&__desc {
			font-size: 0.875rem;
			margin-top: 0.25rem;
			color: #4b5563;
			.teams-page--dark & {
				color: #a1a1aa;
			}
		}
		&__team {
			font-family: monospace;
			font-weight: 700;
			color: #f97316;
		}

		&__meta {
			display: flex;
			align-items: center;
			gap: 1rem;
			margin-top: 0.75rem;
			font-size: 0.75rem;
			font-weight: 500;
			color: #9ca3af;
			.teams-page--dark & {
				color: #71717a;
			}
		}
		&__date {
			display: flex;
			align-items: center;
			gap: 0.375rem;
		}
		&__date-icon {
			width: 0.875rem;
			height: 0.875rem;
		}

		&__actions {
			display: flex;
			align-items: center;
			gap: 0.75rem;
			align-self: flex-start;
			margin-left: 3.5rem;
			@media (min-width: $bp-sm) {
				align-self: center;
				margin-left: 0;
			}
		}
	}
</style>
