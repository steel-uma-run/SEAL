export type ButtonSize = "xs" | "sm" | "md" | "lg" | "xl"

export interface Props {
	text: string
	size: ButtonSize
	iconName?: string
}

export type Measurements = {
	height: number
	iconHeight: number
	iconGap: number
	px: number
	pressedCornerRadius: number
}

export const iconStyles: { [K in ButtonSize]: string } = {
	xs: "text-lg",
	sm: "text-lg",
	md: "text-xl",
	lg: "text-3xl",
	xl: "text-4xl"
}
