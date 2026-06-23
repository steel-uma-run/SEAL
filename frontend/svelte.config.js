import adapter from "@sveltejs/adapter-static"

const config = {
	compilerOptions: {
		runes: ({ filename }) => (filename.split(/[/\\]/).includes("node_modules") ? undefined : true),
		experimental: { async: true }
	},
	kit: {
		adapter: adapter()
	}
}

export default config
