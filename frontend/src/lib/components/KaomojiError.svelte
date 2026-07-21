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

<div>
	<p>{choices[Math.floor(Math.random() * choices.length)]}</p>

	{#if text}
		<p>{text}</p>
	{/if}
</div>

<style>
	div {
		color: var(--md-sys-color-on-surface-variant);
		text-align: center;
		display: flex;
		flex-direction: column;
		gap: 1rem;

		:nth-child(1) {
			font-size: 1.5rem;
		}

		:nth-child(2) {
			font-size: 1rem;
		}
	}
</style>
