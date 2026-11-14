# Footage Cam (Forge 1.20.1)

Found-footage style first person camera, taken from SP-Backrooms Revamped and reduced to just the camera:

- **Tilt** - the camera rolls when you turn your head and when you strafe.
- **Sway** - constant handheld wobble that gets stronger and faster while you move.
- **Camera follow** - the picture trails your mouse a little and catches up.
- **Hand lag** - hands and held items trail behind the camera and catch up a moment later.
- **Motion blur** - depth based camera motion blur (rotation and movement).

Client side only. Works on any server.

## Build (GitHub)

1. Create a repository and upload everything in this folder.
2. Open the **Actions** tab, the `Build mod` workflow runs on every push.
3. Download the `footagecam-jar` artifact. Use `footagecam-1.0.0.jar` (not the `-sources` one if present).

Local build: Java 17 and `gradle build` (Gradle 8.5).

## Config

`config/footagecam-client.toml` is created on first launch. Everything can be switched off or tuned
(tilt amounts, sway intensity, camera smoothing, hand lag strength, blur strength).

## Notes

- The camera follow means the picture lags behind where you really aim. Turn it off in the config if that bothers you.
- Motion blur turns itself off when Iris or Oculus is installed.
- License: GPL-3.0, see `LICENSE` and `NOTICE`.
