The frontend uses SvelteKit. It is configured to generate static
pages. It is similar to a Single Page Application (SPA) when the user
has JavaScript enabled. Otherwise, it's simply just another standard
collection of HTML pages.

Note however that JavaScript is required for major parts of the
functionality. Pages will render without it, but there will be no
interesting things happening.

All code inside the Svelte `<script>` tags will run **twice**: once at
*build time, on Node.js* and once at *runtime, on the user's browser*. The
only exception is code inside Svelte's `$effect` runes; these only run
on the browser.

To prevent code from running in browser, import `$app/environment`
and wrap code in an if:

`if (browser) {
  // this code will only run at build time on Node.js
}`

This model is to allow for static site generation (SSG). Code must be
run at build time for SvelteKit to figure out how to produce static
HTML pages. If, at build time, code accesses browser-only APIs (like
`window`), it will fail to build.

Svelte has a polyfill that allows using browser-only `fetch` on Node.js,
so `fetch` is an exception.
