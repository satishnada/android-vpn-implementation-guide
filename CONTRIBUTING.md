# Contributing

Contributions that improve correctness, tests, accessibility, documentation, and current Android compatibility are welcome.

1. Create a focused issue or reference an existing one.
2. Keep changes small and avoid committing real VPN configurations or keys.
3. Run `./gradlew testDebugUnitTest lintDebug assembleDebug`.
4. Add tests for policy or validation changes.
5. Explain security and privacy impact in the pull request.

Changes that add traffic inspection, credential collection, stealth behavior, or undocumented data transfer will not be accepted.
