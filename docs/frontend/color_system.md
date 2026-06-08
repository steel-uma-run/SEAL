The frontend uses [Material Design 3's color
system](https://m3.material.io/styles/color/system/overview). The main
thing this system provides is an algorithm to generate a whole color
theme from 1 single source color.

The color theme guarantees good contrast, free dark mode and various
variants.

`src/routes/+layout.svelte` is where the color generation happens. It
creates a `<style>` tag consisting of all the color variables and insert
it into `<head>`. All other CSS can refer to these variables using
`var(--md-<token name>)`.
