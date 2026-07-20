<script lang="ts">
	import { onMount } from "svelte"
	import { theme } from "$lib/theme.svelte"
	import { Settings, Save, User, Lock, Mail } from "@lucide/svelte"
	import { getSelfProfile } from "$lib/api"
	import { goto } from "$app/navigation"

	let profile: any = $state(null)
	let isLoading = $state(true)
	let errorMessage = $state("")
	let successMessage = $state("")
	let isSaving = $state(false)

	// Form fields
	let name = $state("")
	let email = $state("")
	let currentPassword = $state("")
	let newPassword = $state("")
	let confirmPassword = $state("")

	onMount(async () => {
		try {
			const { data, response: res } = await getSelfProfile({ throwOnError: false })
			if (res?.ok && data) {
				profile = data
				name = data.name || ""
				email = data.email || ""
			} else {
				errorMessage = "Failed to load profile."
				goto("/auth/login")
			}
		} catch (error) {
			console.error("Error fetching profile:", error)
			errorMessage = "An error occurred while loading your profile."
		} finally {
			isLoading = false
		}
	})

	async function handleSave(e: Event) {
		e.preventDefault()
		errorMessage = ""
		successMessage = ""

		if (newPassword && newPassword !== confirmPassword) {
			errorMessage = "New passwords do not match."
			return
		}

		isSaving = true
		try {
			// Mocking update API as it doesn't exist yet
			await new Promise((resolve) => setTimeout(resolve, 800))

			// If we had an API, it would be called here
			// await updateProfile({ ... })

			successMessage = "Profile updated successfully! (Mocked)"
			currentPassword = ""
			newPassword = ""
			confirmPassword = ""
		} catch (error) {
			errorMessage = "Failed to update profile."
		} finally {
			isSaving = false
		}
	}
</script>

<div class="settings-page {theme.darkMode ? 'settings-page--dark' : ''}">
	<div class="settings-header">
		<div class="settings-header__icon-wrap">
			<Settings class="settings-header__icon" />
		</div>
		<div class="settings-header__text">
			<h1 class="settings-header__title">Account Settings</h1>
			<p class="settings-header__subtitle">Update your personal information and password.</p>
		</div>
	</div>

	{#if isLoading}
		<div class="loading-state">
			<div class="loading-state__spinner"></div>
		</div>
	{:else}
		<div class="settings-card">
			<div class="settings-card__inner">
				{#if errorMessage}
					<div class="alert alert--error">
						{errorMessage}
					</div>
				{/if}

				{#if successMessage}
					<div class="alert alert--success">
						{successMessage}
					</div>
				{/if}

				<form onsubmit={handleSave} class="settings-form">
					<!-- Personal Info Section -->
					<div class="form-section">
						<h3 class="section-title">
							<User class="section-title__icon" />
							Personal Information
						</h3>
						<div class="form-grid form-grid--2">
							<div class="form-group">
								<label class="form-label">Full Name</label>
								<input
									type="text"
									bind:value={name}
									required
									class="form-input"
									placeholder="Your full name"
								/>
							</div>

							<div class="form-group">
								<label class="form-label">Email Address</label>
								<div class="input-with-icon">
									<div class="input-with-icon__icon-wrap">
										<Mail class="input-with-icon__icon" />
									</div>
									<input
										type="email"
										value={email}
										disabled
										class="form-input form-input--with-icon form-input--disabled"
									/>
								</div>
								<p class="form-hint">Email address cannot be changed.</p>
							</div>
						</div>
					</div>

					<!-- Password Section -->
					<div class="form-section form-section--bordered">
						<h3 class="section-title">
							<Lock class="section-title__icon" />
							Change Password
						</h3>
						<div class="form-grid form-grid--single">
							<div class="form-group">
								<label class="form-label">Current Password</label>
								<input
									type="password"
									bind:value={currentPassword}
									class="form-input"
									placeholder="Leave blank to keep current"
								/>
							</div>
							<div class="form-group">
								<label class="form-label">New Password</label>
								<input
									type="password"
									bind:value={newPassword}
									class="form-input"
									placeholder="New password"
								/>
							</div>
							<div class="form-group">
								<label class="form-label">Confirm New Password</label>
								<input
									type="password"
									bind:value={confirmPassword}
									class="form-input"
									placeholder="Confirm new password"
								/>
							</div>
						</div>
					</div>

					<!-- Actions -->
					<div class="form-actions">
						<button
							type="button"
							class="btn btn--secondary"
							onclick={() => {
								name = profile?.name || ""
								currentPassword = ""
								newPassword = ""
								confirmPassword = ""
							}}
						>
							Cancel
						</button>
						<button type="submit" disabled={isSaving} class="btn btn--primary">
							{#if isSaving}
								<div class="btn__spinner"></div>
								Saving...
							{:else}
								<Save class="btn__icon" />
								Save Changes
							{/if}
						</button>
					</div>
				</form>
			</div>
		</div>
	{/if}
</div>

<style lang="scss">
	// ============================================================================
	// Account Settings - SCSS Conversion
	// ============================================================================
	$bp-md: 768px;

	.settings-page {
		max-width: 56rem; // max-w-4xl
		margin-left: auto;
		margin-right: auto;
		width: 100%;
		padding: 1rem; // p-4
		@media (min-width: $bp-md) {
			padding: 2rem;
		} // md:p-8

		&--dark {
		}
	}

	// Header - flex items-center gap-3 mb-8
	.settings-header {
		display: flex;
		align-items: center;
		gap: 0.75rem; // gap-3
		margin-bottom: 2rem; // mb-8

		&__icon-wrap {
			padding: 0.75rem; // p-3
			border-radius: 0.75rem; // rounded-xl
			background: #ffedd5; // bg-orange-100
			color: #ea580c; // text-orange-600
			.settings-page--dark & {
				background: rgba(67, 20, 7, 0.4); // bg-orange-950/40
				color: #fb923c; // text-orange-400
			}
		}

		&__icon {
			width: 1.5rem;
			height: 1.5rem;
		} // w-6 h-6

		&__title {
			font-size: 1.5rem; // text-2xl
			font-weight: 700;
			color: #1f2937; // text-gray-800
			.settings-page--dark & {
				color: #f4f4f5;
			} // text-zinc-100
		}

		&__subtitle {
			font-size: 0.875rem; // text-sm
			color: #6b7280; // text-gray-500
			.settings-page--dark & {
				color: #a1a1aa;
			} // text-zinc-400
		}
	}

	// Loading - flex justify-center py-12 + spinner h-8 w-8 border-t-2 border-b-2 border-orange-500
	.loading-state {
		display: flex;
		justify-content: center;
		padding: 3rem 0; // py-12

		&__spinner {
			width: 2rem; // h-8 w-8
			height: 2rem;
			border-radius: 9999px;
			border-top: 2px solid #f97316; // border-orange-500
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

	// Card - rounded-2xl border shadow-sm bg-white border-gray-100 / bg-zinc-900 border-zinc-800
	.settings-card {
		border-radius: 1rem; // rounded-2xl
		border: 1px solid #f3f4f6; // border-gray-100
		background: #fff; // bg-white
		box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); // shadow-sm
		transition: all 0.2s ease;

		.settings-page--dark & {
			background: #18181b; // bg-zinc-900
			border-color: #27272a; // border-zinc-800
		}

		&__inner {
			padding: 1.5rem; // p-6
			@media (min-width: $bp-md) {
				padding: 2rem;
			} // md:p-8
		}
	}

	// Alerts - mb-6 p-4 bg-red-50 text-red-600 rounded-xl border border-red-200 text-sm
	.alert {
		margin-bottom: 1.5rem; // mb-6
		padding: 1rem; // p-4
		border-radius: 0.75rem; // rounded-xl
		border: 1px solid;
		font-size: 0.875rem; // text-sm

		&--error {
			background: #fef2f2; // bg-red-50
			color: #dc2626; // text-red-600
			border-color: #fecaca; // border-red-200
		}

		&--success {
			background: #f0fdf4; // bg-green-50
			color: #15803d; // text-green-700
			border-color: #bbf7d0; // border-green-200
		}
	}

	// Form - space-y-8
	.settings-form {
		display: flex;
		flex-direction: column;
		gap: 2rem; // space-y-8
	}

	.form-section {
		// default no border
		&--bordered {
			padding-top: 1.5rem; // pt-6
			border-top: 1px solid #f3f4f6; // border-gray-100
			.settings-page--dark & {
				border-color: #27272a;
			} // border-zinc-800
		}
	}

	.section-title {
		font-size: 1.125rem; // text-lg
		font-weight: 700;
		margin-bottom: 1rem; // mb-4
		display: flex;
		align-items: center;
		gap: 0.5rem; // gap-2
		color: #1f2937; // text-gray-800
		.settings-page--dark & {
			color: #f4f4f5;
		} // text-zinc-100

		&__icon {
			width: 1.25rem;
			height: 1.25rem;
			color: #f97316;
		} // w-5 h-5 text-orange-500
	}

	// Grid - grid-cols-1 md:grid-cols-2 gap-6
	.form-grid {
		display: grid;
		gap: 1.5rem; // gap-6

		&--2 {
			grid-template-columns: 1fr;
			@media (min-width: $bp-md) {
				grid-template-columns: repeat(2, 1fr);
			}
		}

		&--single {
			grid-template-columns: 1fr;
			max-width: 28rem; // max-w-md
		}
	}

	.form-group {
		display: flex;
		flex-direction: column;
	}

	.form-label {
		display: block;
		font-size: 0.875rem; // text-sm
		font-weight: 600; // font-semibold
		margin-bottom: 0.5rem; // mb-2
		color: #374151; // text-gray-700
		.settings-page--dark & {
			color: #d4d4d8;
		} // text-zinc-300
	}

	// Input - w-full px-4 py-3 rounded-xl border focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all bg-gray-50 border-gray-200 / bg-zinc-950 border-zinc-800
	.form-input {
		width: 100%;
		padding: 0.75rem 1rem; // px-4 py-3
		border-radius: 0.75rem; // rounded-xl
		border: 1px solid #e5e7eb; // border-gray-200
		background: #f9fafb; // bg-gray-50
		color: #111827; // text-gray-900
		outline: none;
		transition: all 0.2s ease;
		font-size: 0.9rem;

		&::placeholder {
			color: #9ca3af;
		}

		&:focus {
			box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.2); // focus:ring-2 focus:ring-orange-500/20
			border-color: #f97316; // focus:border-orange-500
		}

		.settings-page--dark & {
			background: #09090b; // bg-zinc-950
			border-color: #27272a; // border-zinc-800
			color: #f4f4f5; // text-zinc-100
		}

		&--with-icon {
			padding-left: 2.75rem; // pl-11
			padding-right: 1rem; // pr-4
		}

		&--disabled {
			cursor: not-allowed;
			opacity: 0.7; // opacity-70
			background: #f3f4f6; // bg-gray-100
			.settings-page--dark & {
				background: #09090b;
			}
		}
	}

	// Input with icon wrapper
	.input-with-icon {
		position: relative;

		&__icon-wrap {
			position: absolute;
			top: 0;
			bottom: 0;
			left: 0;
			padding-left: 1rem; // pl-4
			display: flex;
			align-items: center;
			pointer-events: none;
		}

		&__icon {
			height: 1rem; // h-4
			width: 1rem; // w-4
			color: #9ca3af; // text-gray-400
		}
	}

	.form-hint {
		font-size: 0.75rem; // text-xs
		margin-top: 0.5rem; // mt-2
		color: #9ca3af; // text-gray-400
		.settings-page--dark & {
			color: #71717a;
		} // text-zinc-500
	}

	// Form actions - pt-6 border-t flex justify-end gap-4
	.form-actions {
		padding-top: 1.5rem; // pt-6
		border-top: 1px solid #f3f4f6; // border-gray-100
		display: flex;
		justify-content: flex-end;
		gap: 1rem; // gap-4
		.settings-page--dark & {
			border-color: #27272a;
		}
	}

	// Buttons
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem; // gap-2
		padding: 0.625rem 1.5rem; // px-6 py-2.5
		border-radius: 0.75rem; // rounded-xl
		font-weight: 600; // font-semibold
		transition: all 0.2s ease;
		cursor: pointer;
		border: none;
		font-size: 0.9rem;

		&:disabled {
			opacity: 0.7;
			cursor: not-allowed;
		}

		&__icon {
			width: 1rem;
			height: 1rem;
		} // w-4 h-4
		&__spinner {
			width: 1rem;
			height: 1rem; // h-4 w-4
			border-radius: 9999px;
			border-top: 2px solid #fff;
			border-bottom: 2px solid #fff;
			border-left: 2px solid transparent;
			border-right: 2px solid transparent;
			animation: spin 1s linear infinite;
		}

		&--secondary {
			background: #f3f4f6; // bg-gray-100
			color: #374151; // text-gray-700
			&:hover {
				background: #e5e7eb;
			} // hover:bg-gray-200

			.settings-page--dark & {
				background: #27272a; // bg-zinc-800
				color: #d4d4d8; // text-zinc-300
				&:hover {
					background: #3f3f46;
				} // hover:bg-zinc-700
			}
		}

		&--primary {
			background: #f97316; // bg-orange-500
			color: #fff;
			box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); // shadow-sm
			&:hover {
				background: #ea580c;
			} // hover:bg-orange-600
		}
	}
</style>
