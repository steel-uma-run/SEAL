import { zCreateEventRequest } from "$lib/api/zod.gen"
import { superValidate } from "sveltekit-superforms"
import { zod4 } from "sveltekit-superforms/adapters"

export const load = async () => {
	const form = await superValidate(zod4(zCreateEventRequest))

	return { form }
}
