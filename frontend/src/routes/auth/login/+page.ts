import { superValidate } from "sveltekit-superforms"
import { zod4 } from "sveltekit-superforms/adapters"

import { zLoginRequestPayload } from "$lib/api/zod.gen"

export const load = async () => {
	const form = await superValidate(zod4(zLoginRequestPayload))

	return { form }
}
