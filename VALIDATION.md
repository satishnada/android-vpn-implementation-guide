# Validation Notes

Validation performed in the artifact environment on 2026-09-06.

## Passed

- Pure Kotlin domain code compiled with the installed Kotlin compiler.
- Routing-policy and package-name assertions executed successfully.
- All 10 XML files parsed successfully.
- All 3 GitHub YAML files parsed successfully.
- 26 Kotlin and Gradle Kotlin files passed a basic delimiter-integrity scan.
- Local Markdown link targets resolved.
- Shell syntax check passed for the Unix Gradle bootstrap script.
- A targeted scan found no private-key blocks, GitHub tokens, or Google API keys.
- All required source, documentation, CI, test, and sample-config files are present.

## Not completed in this environment

The full Android Gradle build could not be executed because the isolated build environment could not resolve `services.gradle.org` or download Android and Maven dependencies. The failed bootstrap did not create project artifacts.

Run the following on a connected workstation before publishing:

```bash
./gradlew wrapper --gradle-version 9.3.1
./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace
```

Then install the debug build on physical devices and complete the routing, foreground-service, network-transition, and security checks in `docs/testing.md`.

## Integration note

`WireGuardBackendContext` redirects the pinned WireGuard backend's explicit service start to `ScopeWireGuardVpnService`. The design matches the current backend source used while preparing this project, but it is intentionally version-sensitive. Reinspect and retest that integration whenever `com.wireguard.android:tunnel` is upgraded.
