import { browser } from "$app/environment"

export class LocalStore<T> {
	key: string
	value: T

	constructor(key: string, value: T) {
		this.key = key
		this.value = $state(value)

		if (browser) {
			const item = localStorage.getItem(key)
			if (item) {
				this.value = this.deserialize(item)
			}
		}

		$effect.root(() => {
			$effect(() => {
				if (this.value === undefined) {
					localStorage.removeItem(key)
					return
				}

				localStorage.setItem(this.key, this.serialize(this.value))
			})
		})
	}

	serialize(value: T): string {
		return JSON.stringify(value)
	}

	deserialize(item: string): T {
		return JSON.parse(item)
	}
}

export function localStore<T>(key: string, value: T) {
	return new LocalStore(key, value)
}
