# ScopeVPN Test Plan

## Automated checks

Run:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The GitHub Actions workflow executes the same command for pushes and pull requests.

Current JVM tests cover:

- `AppRoutingPolicy` validation
- routing-mode changes
- package selection toggles
- package-name validation

Recommended next automated tests:

- WireGuard configuration rebuild for each routing mode
- preservation of addresses, DNS, key pair, MTU, listen port, and peers
- rejection of a configuration containing both included and excluded apps
- ViewModel state transitions with a fake controller
- reconnect after an edited policy
- polling start and cancellation
- app-query result deduplication
- Compose semantics for connect, disconnect, and filter controls

## Test infrastructure

Use a WireGuard server and client profile created specifically for testing. Never use or record production credentials.

The test server should provide:

- a stable endpoint
- IPv4 support and, where applicable, IPv6 support
- a known public egress IP
- a way to revoke the client key
- test-only logs that do not capture traffic contents

Prepare two apps that can show their current public IP, for example two browsers or a browser and a simple internal test client.

## Core routing matrix

| Test | Routing mode | App A selected | App B selected | Expected App A IP | Expected App B IP |
|---|---|---:|---:|---|---|
| 1 | All apps | N/A | N/A | VPN egress | VPN egress |
| 2 | Only selected | Yes | No | VPN egress | Normal network |
| 3 | Only selected | No | Yes | Normal network | VPN egress |
| 4 | Exclude selected | Yes | No | Normal network | VPN egress |
| 5 | Exclude selected | No | Yes | VPN egress | Normal network |
| 6 | Exclude selected | No | No | VPN egress | VPN egress |

For each row:

1. Record the device's normal public IP.
2. Apply the policy and connect.
3. Force-stop and reopen each test app so stale connections are not reused.
4. Verify the observed source IP.
5. Generate traffic and confirm counters increase.
6. Disconnect and confirm both apps return to normal routing.

## Permission and consent

- First connection shows the Android VPN consent dialog.
- Denial returns the UI to a safe disconnected state.
- Approval continues to backend connection.
- A later connection does not show consent again unless another VPN app becomes prepared or the system state changes.
- Notification permission denial on Android 13+ does not crash connection handling.
- The active service still meets platform foreground requirements.

## Configuration import

- Valid `.conf` imports successfully.
- File with no peer fails.
- Peer with no endpoint fails for this demo.
- Peer with no allowed IP fails.
- Invalid key fails without logging the key.
- Oversized file is rejected.
- Unreadable URI produces a user-safe message.
- Import is disabled while connected.
- A second valid file replaces the in-memory profile only while disconnected.
- Process death removes the in-memory profile, as documented.

## App selector

- Search matches app label.
- Search matches package name.
- User/system/all filters produce expected results.
- Selecting visible apps affects only visible search results.
- Clearing visible apps preserves hidden selections.
- Manual valid package can be added.
- Invalid package syntax is rejected.
- Valid-looking but uninstalled package causes a handled connection error.
- Duplicate manual package does not create a duplicate row.
- Selected count is accurate after mode and search changes.
- Allowlist mode blocks apply with zero selected packages.

## Connection lifecycle

- Connect transitions `DISCONNECTED -> CONNECTING -> CONNECTED`.
- Disconnect transitions `CONNECTED -> DISCONNECTING -> DISCONNECTED`.
- Backend failure transitions to `ERROR` with an actionable message.
- Rapid repeated taps do not create overlapping backend calls.
- Applying changed filters while connected performs one controlled reconnect.
- Closing and recreating the activity preserves controller state within the same process.
- Moving the app to background leaves the tunnel active and notification visible.
- Swiping the task away behaves consistently with the documented service design.
- Disconnect removes the ongoing notification.

## Animation and accessibility

- Broadcasting rings run only while connecting or connected.
- Animation stops while disconnected or in error.
- The centre control has a clear content description.
- Screen reader output communicates connection state and primary action.
- Large font sizes do not hide connect or filter actions.
- Reduced animation settings are respected where feasible; consider disabling decorative motion in a production accessibility pass.
- Light and dark themes maintain contrast.

## Network transitions

Test on physical devices:

- Wi-Fi to mobile data
- mobile data to Wi-Fi
- temporary airplane mode
- captive portal Wi-Fi
- loss and restoration of endpoint reachability
- IPv4-only network
- IPv6-capable network
- DNS failure
- endpoint address change, if supported by the profile

The current demo does not promise automatic recovery for every transition. Record the observed behavior and turn product requirements into explicit retry rules before launch.

## Android versions and OEMs

Minimum recommended matrix:

- API 24 or 25 for minimum-SDK behavior
- Android 11 for package visibility behavior
- Android 13 for notification permission
- Android 14 for foreground-service type enforcement
- current target and latest available Android release

Include at least one Samsung, Pixel, and another OEM device when preparing a production release. Background restrictions and task-removal behavior can differ.

## Security checks

- Search logs for `PrivateKey`, configuration content, server secrets, and packet payloads.
- Confirm backup and data extraction are disabled.
- Confirm no real `.conf` file exists in Git history.
- Confirm no analytics or advertising SDK receives network metadata.
- Inspect the merged manifest for exported services and permissions.
- Confirm the VPN service is non-exported and guarded by `BIND_VPN_SERVICE`.
- Validate transport and DNS behavior with tools appropriate to your authorised test environment.
- Revoke the test client key and confirm connection fails safely.

## Release evidence

Before publishing a technical article or release, capture:

- successful CI run
- merged manifest excerpt
- connection and disconnection screen recording
- all three routing modes verified with two test apps
- foreground notification in background
- no-secret repository scan
- tested Android versions and devices

Do not claim "production ready" from a debug build or a single emulator test.
