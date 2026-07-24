<script lang="ts">
	import { NavigationRail, NavigationRailItem } from "m3-svelte"
	import type { Role } from "$lib/api"

	import iconHome from "@ktibow/iconset-material-symbols/home"
	import iconEvent from "@ktibow/iconset-material-symbols/event"
	import iconGroups from "@ktibow/iconset-material-symbols/groups"
	import iconAdd from "@ktibow/iconset-material-symbols/add"
	import iconMail from "@ktibow/iconset-material-symbols/mail"
	import iconRubric from "@ktibow/iconset-material-symbols/rubric"
	import iconPerson from "@ktibow/iconset-material-symbols/person"
	import iconRateReview from "@ktibow/iconset-material-symbols/rate-review"
	import iconSettings from "@ktibow/iconset-material-symbols/settings"
	import iconAssignmentReturn from "@ktibow/iconset-material-symbols/assignment-return"
	import iconLogout from "@ktibow/iconset-material-symbols/logout"
	import { auth, token } from "$lib/auth.svelte"
	import { goto } from "$app/navigation"

	interface Props {
		role: Role
	}

	let { role }: Props = $props()

	function handleLogout() {
		auth.value = undefined
		token.value = undefined
		goto("/auth/login")
	}
</script>

<NavigationRail>
	{#if role == "COORDINATOR"}
		<NavigationRailItem label="Dashboard" icon={iconHome} href="/coordinator" />

		<NavigationRailItem label="Events" icon={iconEvent} href="/coordinator/seasons" />

		<NavigationRailItem label="Teams" icon={iconGroups} href="/coordinator/teams" />

		<NavigationRailItem label="Users" icon={iconPerson} href="/coordinator/users" />

		<NavigationRailItem label="Lecturers" icon={iconPerson} href="/coordinator/experts" />

		<NavigationRailItem label="Criteria" icon={iconRubric} href="/coordinator/templates" />
	{:else if role == "STUDENT"}
		<NavigationRailItem label="Dashboard" icon={iconHome} href="/student" />

		<NavigationRailItem label="Events" icon={iconEvent} href="/student/events" />

		<NavigationRailItem label="View team" icon={iconGroups} href="/student/teams" />

		<NavigationRailItem label="Create team" icon={iconAdd} href="/student/create-team" />

		<NavigationRailItem label="Team Invitation" icon={iconMail} href="/student/teams?tab=invitations" />

		<NavigationRailItem label="Results" icon={iconAssignmentReturn} href="/student/results" />
	{:else if role == "LECTURER"}
		<NavigationRailItem label="Dashboard" icon={iconHome} href="/lecturer" />

		<NavigationRailItem label="Events" icon={iconEvent} href="/lecturer/events" />

		<NavigationRailItem label="Mentor" icon={iconGroups} href="/lecturer/teams" />

		<NavigationRailItem label="Grading" icon={iconRateReview} href="/lecturer/grading" />
	{/if}

	<NavigationRailItem label="Settings" icon={iconSettings} href="/{role.toLowerCase()}/settings" />
	<NavigationRailItem label="Logout" icon={iconLogout} onclick={handleLogout} />
</NavigationRail>
