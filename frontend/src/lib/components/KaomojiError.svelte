<script lang="ts">
	interface Props {
		kind: "bad" | "neutral" | "good"
		text?: string
	}

	const bad = ["(ó﹏ò｡)", "(｡ᵕ °ㅁ° ).ᐟ.ᐟ.ᐟ", "(ᵕ,,Ŏ▱Ŏ,,)", "(｡>﹏<)", "'°△°`||, !!", "(｡╯︵╰｡)"]
	const neutral = ["┐(￣ヘ￣)┌", "┐(￣∀￣)┌", '╮(￣▽￣"")╭', "(ᵕ,—ᴗ—,)"]
	const good = ["٩(^ᗜ^ )و ´-", "٩>ᴗ<)و", "◝(ᵔᗜᵔ)◜", "＼(^o^)／", "ᐠ( ᐛ )ᐟ"]

	const { kind, text }: Props = $props()

	let choices = $derived.by(() => {
		switch (kind) {
			case "bad":
				return bad
			case "neutral":
				return neutral
			case "good":
				return good
		}
	})
</script>

<div class="text-center flex flex-col gap-4 text-(--md-on-surface-variant)">
	<p class="text-3xl">{choices[Math.floor(Math.random() * choices.length)]}</p>

	{#if text}
		<p class="text-xl">{text}</p>
	{/if}
</div>
