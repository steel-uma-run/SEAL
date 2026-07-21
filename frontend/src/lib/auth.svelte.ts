import { localStore } from "./LocalStore.svelte"

import type { Lecturer, Student, Coordinator } from "./api"

export const auth = localStore<(Lecturer | Student | Coordinator) | undefined>("auth", undefined)
