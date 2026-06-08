Because this is a monorepo with way too many different languages, we use
[treefmt](https://github.com/numtide/treefmt) to manage formatting across
every file.

Always format everything with treefmt. *Don't* have a separate commit
just to format files. Always keep files formatted.

A CI check will check that all files are properly formatted on every PR.

Main things to pay attention to:

## Java

- Use [google-java-format](https://github.com/google/google-java-format)
- 2 space indent (what google-java-format uses by default)
- No glob imports (`import package.*;`)
- Keep imports organized, google-java-format does this for you

## Svelte/TypeScript/JavaScript

- Use prettier
- 4 space indent
- No semicolons!!!!
- No single quotes, only double quotes
