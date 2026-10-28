# Google Play and VPN Release Checklist

This is an engineering preparation checklist, not legal advice. Google Play policy and Android platform requirements change. Re-check the official policy and Android documentation immediately before every release.

## Core eligibility

- [ ] `VpnService` is a core user-facing function of the app or fits an explicitly permitted use case.
- [ ] The store listing clearly explains why the app creates a VPN connection.
- [ ] The app does not manipulate traffic for monetisation without prominent disclosure and valid consent.
- [ ] The app does not redirect or inspect traffic for undisclosed purposes.
- [ ] The app does not claim anonymity or security properties that have not been verified.

## Google Play declaration

- [ ] Complete the current `VpnService` declaration in Play Console.
- [ ] Provide an accurate demonstration video when requested.
- [ ] Ensure the video shows the real in-app flow and core VPN function.
- [ ] Ensure package name, app name, and submitted binary match the declaration.
- [ ] Keep supporting architecture and privacy documentation available for review.

## Traffic protection

- [ ] Traffic between client and VPN endpoint uses strong encryption.
- [ ] The app does not describe a plain TUN interface or unencrypted proxy as a secure VPN.
- [ ] Keys are generated, distributed, stored, rotated, and revoked under a documented design.
- [ ] Real private keys are absent from source control, crash reports, screenshots, and support tickets.
- [ ] DNS and IPv6 behavior are tested to prevent misleading protection claims.

## Data safety and privacy

- [ ] Inventory every data item collected by the app, SDKs, control plane, and VPN infrastructure.
- [ ] Complete Play Data safety answers consistently with actual behavior.
- [ ] Publish a privacy policy that names VPN-specific processing.
- [ ] Disclose handling of IP addresses, account data, device identifiers, diagnostics, connection metadata, and traffic data.
- [ ] Define retention and deletion periods.
- [ ] Obtain prominent in-app disclosure and consent when required.
- [ ] Do not sell, share, or repurpose traffic data contrary to policy or user expectation.
- [ ] Review third-party SDKs; a VPN app cannot outsource away responsibility for their collection.

## Android implementation

- [ ] Use `VpnService.prepare()` and handle denial.
- [ ] Declare the VPN service with `android.permission.BIND_VPN_SERVICE`.
- [ ] Keep the VPN service non-exported unless a reviewed use case requires otherwise.
- [ ] Display an ongoing notification while active.
- [ ] Declare and satisfy the applicable foreground-service permission and type.
- [ ] Request notification permission contextually on Android 13+.
- [ ] Avoid `QUERY_ALL_PACKAGES` unless the use case and Play policy truly justify it.
- [ ] Apply allowlist or denylist package routing before `establish()`; never combine both.
- [ ] Re-establish the interface when routing rules change.
- [ ] Implement service shutdown and notification removal correctly.
- [ ] Test always-on and lockdown only if the app explicitly supports them.

## Store listing and communication

- [ ] Screenshots reflect the shipped product.
- [ ] Do not call a demo or unfinished client "military-grade," "100% anonymous," or "untraceable."
- [ ] State whether a subscription, account, or external server is required.
- [ ] Identify countries or networks where service is unavailable.
- [ ] Explain the app-wise routing modes in plain language.
- [ ] Provide support and security-reporting contacts.

## Operational readiness

- [ ] Monitor endpoint health without collecting traffic contents.
- [ ] Define incident response for key compromise, server breach, certificate or endpoint changes, and abuse reports.
- [ ] Maintain server capacity and patching.
- [ ] Provide client-key revocation.
- [ ] Document lawful-request handling and jurisdictional considerations.
- [ ] Perform an independent security assessment before high-risk deployment.
- [ ] Test app updates while a VPN is active.
- [ ] Test rollback and emergency disablement.

## Official sources to re-check

- Google Play `VpnService` policy: https://support.google.com/googleplay/android-developer/answer/12564964
- Android VPN developer guide: https://developer.android.com/develop/connectivity/vpn
- Android foreground-service types: https://developer.android.com/develop/background-work/services/fgs/service-types
- Android user-data policy: https://support.google.com/googleplay/android-developer/topic/9877467
- Play Data safety guidance: https://support.google.com/googleplay/android-developer/answer/10787469
