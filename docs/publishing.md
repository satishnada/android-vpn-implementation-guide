# Publishing ScopeVPN as Version 2 of the Existing Repository

## Recommendation

Upgrade the existing repository:

```text
satishnada/android-vpn-implementation-guide
```

Do not launch a third competing repository for the same topic. The existing project already has public history, stars, forks, and backlinks. A transparent version 2 rewrite preserves that trust while making the technical correction visible.

Use **ScopeVPN** as the demo application name. Keep the repository slug during the launch. A later repository rename can preserve GitHub redirects, but it is not necessary for version 2.

## Safe migration flow

Clone the existing repository and preserve the old implementation on a tagged release and branch:

```bash
git clone https://github.com/satishnada/android-vpn-implementation-guide.git
cd android-vpn-implementation-guide

git checkout -b archive/v1-before-scopevpn
git tag -a v1.0.0-legacy -m "Legacy TUN interface learning sample"
git push origin archive/v1-before-scopevpn
git push origin v1.0.0-legacy

git checkout main
git checkout -b feature/scopevpn-v2
```

Copy the generated v2 files into the working tree. Preserve `.git`, then inspect changes:

```bash
git status
git diff --stat
```

## Generate the canonical Gradle wrapper

The prepared bundle contains checksum-verified Gradle bootstrap scripts but not the binary wrapper JAR. On a connected development machine, run:

```bash
./gradlew wrapper --gradle-version 9.3.1
```

Windows PowerShell:

```powershell
.\gradlew.bat wrapper --gradle-version 9.3.1
```

Confirm that these canonical wrapper files are committed:

```text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

## Local verification

Install JDK 17 and Android SDK 37, then run:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Also inspect the merged manifest:

```bash
./gradlew processDebugMainManifest
```

In Android Studio, verify that:

- only `ScopeWireGuardVpnService` is present as the VPN service
- the service is non-exported
- `BIND_VPN_SERVICE` is present
- the foreground-service type is merged correctly
- no unexpected broad package permission is added

## Secret checks

Before committing:

```bash
git grep -n "PrivateKey"
git grep -n "Endpoint ="
git grep -n "BEGIN PRIVATE"
```

Expected matches should be placeholders or documentation only. Inspect every result.

Also review screenshots, recordings, Gradle properties, local configuration, and Git history for private keys, endpoint hostnames, API tokens, and personal network data.

## Commit structure

A reviewable sequence is better than one opaque commit:

```text
chore: archive legacy VPN sample documentation
build: modernize Android and Compose toolchain
feat: add WireGuard tunnel controller
feat: add per-app routing policy and app selector
feat: add foreground VPN notification
feat: add animated Compose connection dashboard
 test: add routing and package validation coverage
 docs: publish architecture, testing, and article rewrite
```

Correct the leading space in the `test:` line when using it as a commit message.

## Pull request title

```text
ScopeVPN v2: real WireGuard tunnel, per-app routing, and Compose UI
```

## Pull request summary

```markdown
## Why

The legacy sample created an Android TUN interface but did not implement secure packet transport to a remote VPN gateway. This rewrite corrects that limitation and keeps the original repository history.

## What changed

- Integrated the official WireGuard Android tunnel backend
- Added all-app, allowlist, and denylist routing
- Added searchable package selection and manual package entry
- Added controlled reconnect for filter changes
- Added foreground-service notification behavior
- Added a Compose dashboard with connection animation and live statistics
- Added tests, CI, architecture documentation, and release guidance

## Validation

- [ ] `testDebugUnitTest`
- [ ] `lintDebug`
- [ ] `assembleDebug`
- [ ] Real tunnel verified on a physical device
- [ ] All three routing modes verified with two apps
- [ ] Notification verified while app is backgrounded
- [ ] Repository scanned for secrets
```

## Merge and release

After review and CI:

```bash
git checkout main
git pull --ff-only origin main
git merge --no-ff feature/scopevpn-v2
git push origin main

git tag -a v2.0.0 -m "ScopeVPN 2.0"
git push origin v2.0.0
```

Create a GitHub release from `v2.0.0` using the text in `docs/launch-package.md`.

## Repository settings

Update:

- description
- website URL after the Medium article is live
- topics
- social preview image
- default branch protection
- required Android CI check
- security-reporting contact

Pin the repository on the GitHub profile during the article launch.

## Article links

Replace both placeholders before publishing:

```text
https://github.com/satishnada/android-vpn-implementation-guide
[MEDIUM_ARTICLE_URL]
```

Use the tagged release or main repository URL for source links. Avoid linking readers to an unmerged feature branch.
