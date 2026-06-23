export const bad = [
	"(ó﹏ò｡)",
	"(｡ᵕ °ㅁ° ).ᐟ.ᐟ.ᐟ",
	"(ᵕ,,Ŏ▱Ŏ,,)",
	"(｡>﹏<)",
	"'°△°`||, !!",
	"(｡╯︵╰｡)"
]

export const neutral = ["┐(￣ヘ￣)┌", "┐(￣∀￣)┌", '╮(￣▽￣"")╭', "(ᵕ,—ᴗ—,)"]

export const good = ["٩(^ᗜ^ )و ´-", "٩>ᴗ<)و", "◝(ᵔᗜᵔ)◜", "＼(^o^)／", "ᐠ( ᐛ )ᐟ"]

export function random(array: string[]): string {
	return array[Math.floor(Math.random() * array.length)]
}
