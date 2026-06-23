<script lang="ts">
	// https://m3.material.io/components/buttons/specs

	import { Button, type ButtonRootProps } from "bits-ui"
	import { iconStyles, type ButtonSize, type Props } from "./buttons"

	const styles: { [K in ButtonSize]: string } = {
		xs: "min-h-8 px-3 rounded-[16px] active:rounded-lg text-sm",
		sm: "min-h-10 px-4 rounded-[20px] active:rounded-lg text-sm",
		md: "min-h-14 px-6 rounded-[28px] active:rounded-xl text-base",
		lg: "min-h-24 px-12 rounded-[48px] active:rounded-2xl text-2xl",
		xl: "min-h-34 px-16 rounded-[68px] active:rounded-2xl text-3xl"
	}

	let { text, iconName, size, ...rest }: Props & ButtonRootProps = $props()
</script>

<Button.Root
	class={"transition-all ease-standard inline-flex items-center duration-50 bg-(--md-secondary-container) text-(--md-on-secondary-container) active:rounded-lg hover:cursor-pointer hover:opacity-95 " +
		styles[size]}
	{...rest}
>
	{#if iconName}
		<span class={"font-[Material_Icons] mr-1 " + iconStyles[size]}>{iconName}</span>
	{/if}

	<span>{text}</span>
</Button.Root>
