# Footage Cam (Forge 1.20.1)

Found-footage style first person camera, taken from SP-Backrooms Revamped and reduced to just the camera:

- **Tilt** - the camera rolls when you turn your head and when you strafe (half of the SP-Backrooms amount).
- **Shake** - handheld wobble while you walk and run (same as SP-Backrooms). None while standing still.
- **Camera follow** - the picture trails your mouse a little and catches up.
- **Idle sway** - very slow, very small cinematic sway while you stand still (like Camera Overhaul), fades out when you move.
- **Menus** - all sway, shake and tilt fade out while an inventory, chest or any container is open.
- **Hand lag** - hands, held items and TACZ guns trail behind the camera and catch up a moment later.
- **TACZ shots follow the gun** - while holding a TACZ gun, bullets leave where the front of the (delayed) gun points, not where your mouse aim is.
- **Aim down sights** - when you aim a TACZ gun (right click) the gun stays in the middle of the screen and the sway, tilt and camera delay are toned down, so you can still hit things. It blends in and out as the gun comes up.
- **Motion blur** - depth based camera motion blur (rotation and movement).

Client side only. Works on any server.

## Build (GitHub)

1. Create a repository and upload everything in this folder.
2. Open the **Actions** tab, the `Build mod` workflow runs on every push.
3. Download the `footagecam-jar` artifact. Use `footagecam-1.0.0.jar` (not the `-sources` one if present).

Local build: Java 17 and `gradle build` (Gradle 8.5).

## Config

`config/footagecam-client.toml` is created on first launch. Everything can be switched off or tuned
(tilt amounts, sway intensity, idle sway timing, camera smoothing, hand lag strength, gun aim, blur strength).
Defaults: tilt 3.5 (SP-Backrooms uses 7), smoothing 0.7. If you already have a config file, delete it once to get the new defaults.

## Notes

- The camera follow means the picture lags behind where you really aim. Turn it off in the config if that bothers you.
- TACZ shots: the real rotation is swapped for the gun direction for a moment each tick, only when a TACZ gun is in your main hand in first person. The gun direction is also sent at the exact moment TACZ fires, so the server does not use a rotation that is up to one tick old. Switch it off with `gun_aim.bulletsFollowGun`.
- Aiming: tune it in the `aim_down_sights` config section (gun centring, sway, tilt, camera delay).
- Motion blur turns itself off when Iris or Oculus is installed.
- License: GPL-3.0, see `LICENSE` and `NOTICE`.
