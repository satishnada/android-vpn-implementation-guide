# ScopeVPN Demo Recording Shot List

Target duration: **45-60 seconds**

Record on a test device or emulator using test-only credentials. Crop or blur IP addresses, notifications, profile names, carrier details, and unrelated apps as required.

## Sequence

### 0-4 seconds - Hook

Show the disconnected home screen with the title overlay:

**A TUN interface is not a VPN.**

Small subtitle:

**Here is a real Android WireGuard client with app-wise routing.**

### 4-10 seconds - Import

Tap **Import .conf**, choose a test profile, and return to the screen. Show the imported profile summary without exposing endpoint or key material.

### 10-20 seconds - Per-app filter

Open **App filter**.

Quickly show:

- All apps
- Only selected apps
- Exclude selected apps

Choose **Only selected apps**, search for two safe test apps, and select one.

### 20-28 seconds - Connect

Tap **Connect** and approve Android's system VPN consent dialog.

Show the state changing from connecting to connected.

### 28-38 seconds - Broadcasting animation and statistics

Hold on the connected home screen. Show the expanding rings, connection timer, and RX/TX values increasing as the selected test app generates traffic.

Overlay:

**UI follows backend state - not a fake toggle.**

### 38-48 seconds - Apply new routing policy

Open the filter, change to **Exclude selected apps**, return, and tap **Apply app filter and reconnect**.

Show the controlled reconnect state.

### 48-54 seconds - Foreground behavior

Move the app to background and briefly show the ongoing ScopeVPN notification.

### 54-60 seconds - CTA

Return to the connected screen and show:

**WireGuard + Kotlin + Jetpack Compose**

**Source code and full guide by Satish Nada**

Use the repository URL or a short readable overlay. Do not display an untested claim such as "100% private" or "anonymous."

## Recording tips

- Use a clean test-device home screen.
- Disable personal notifications.
- Use a stable frame rate and native phone aspect ratio.
- Keep taps visible but avoid excessive pointer trails.
- Add captions because many LinkedIn viewers watch without audio.
- Use subtle music only when licensed.
- Export one vertical version for LinkedIn and one 16:9 or wide crop for Medium/GitHub.
