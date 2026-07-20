import { superValidate } from "sveltekit-superforms"
import { zod4 } from "sveltekit-superforms/adapters"
import { z } from "zod"

const schema = z.object({
	name: z.string()
})

export const load = async () => {
	const form = await superValidate(zod4(schema))

	return { form }
}
