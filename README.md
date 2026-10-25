# Moonlight Fold

A modified build of [Moonlight for Android](https://github.com/moonlight-stream/moonlight-android) for the Samsung Galaxy Z Fold 6. It installs next to the official app.

This program is free software under the **GNU General Public License version 3**. The full license is in [`LICENSE.txt`](LICENSE.txt). Upstream libraries keep their own licenses (`moonlight-common-c` is GPLv3; see `enet/LICENSE` and `nanors/LICENSE` under that tree).

This is not an official Moonlight release and is not affiliated with the Moonlight project. The application id and launcher name are changed so crashes and installs stay separate from the Play Store build, which is what upstream asks of derivative apps.

## Upstream

Imported from tag **v12.2** (commit `b48494cb96bff23d8886c4775cc4f39a1075495d`, 2026-09-12).

| Tree | Commit |
| --- | --- |
| [moonlight-android](https://github.com/moonlight-stream/moonlight-android) | `b48494cb96bff23d8886c4775cc4f39a1075495d` |
| [moonlight-common-c](https://github.com/moonlight-stream/moonlight-common-c) | `874ac9548f1bd6f095ef2b435c42cdde460e7821` |
| [enet](https://github.com/cgutman/enet) | `aca87840b57f045a1f7f9299e4b1b9b8e2a5e2f1` |
| [nanors](https://github.com/sleepybishop/nanors) | `b1e3c22ca0cdc0bb83e3cd6ed1a2fc77869ed99a` |

Submodule sources are vendored in this repository (including `app/src/main/jni/moonlight-core/moonlight-common-c` and its `enet` and `nanors` dependencies). A fresh clone builds without `git submodule update`.

### Authors

Moonlight for Android was written by [Cameron Gutman](https://github.com/cgutman), [Diego Waxemberg](https://github.com/dwaxemberg), [Aaron Neyer](https://github.com/Aaronneyer), and [Andrew Hennessy](https://github.com/yetanothername), started by students at Case Western at MHacks.

## What changed

On an unfolded Fold, the inner screen is almost square and a 16:9 stream is letterboxed. Opening the on-screen keyboard used to cover the bottom of that picture, because `Game` is a fullscreen immersive activity (`FLAG_FULLSCREEN`, sticky immersive system UI) and `StreamView` only centers itself in the full window.

With **Pin stream to top when keyboard is open** enabled (the default, under Settings → Basic Settings):

- While the soft keyboard is open, the stream is scaled to fit the area above the keyboard, aspect ratio preserved, and aligned to the **top** of the screen. The keyboard sits underneath the video.
- While the keyboard is closed, the stream stays centered, or stretched full-screen if that existing option is on.
- Picture-in-picture keeps the normal layout.

The activity uses `windowSoftInputMode="adjustResize"`. On the app's target SDK the window stays edge-to-edge, so the layout reads the IME overlap (`WindowInsets.Type.ime()`), with the visible display frame and `InputMethodManager.getInputMethodWindowVisibleHeight()` as fallbacks. `KeyboardStreamLayout` turns those signals into a top pin and a bottom inset. `StreamView.onMeasure` fits the video into the remaining height.

Other identifiers:

| | Official | This build |
| --- | --- | --- |
| Application id | `com.limelight` | `com.limelight.custom` |
| Launcher name | Moonlight | Moonlight Fold |
| Version | 12.2 | 12.2-fold.2 |

The debug APK includes the upstream ABIs (arm64-v8a, armeabi-v7a, x86, x86_64). The Galaxy Z Fold 6 uses arm64-v8a.

Open the keyboard in a stream with a three-finger tap, the same gesture as upstream Moonlight.

Input Settings also has **Invert trackpad X axis** and **Invert trackpad Y axis**, both off by default. They flip movement from an external trackpad such as the Galaxy Tab S8 Ultra Book Cover Keyboard. A Bluetooth mouse is left alone: under pointer capture both devices send `SOURCE_MOUSE_RELATIVE` events, and the trackpad is recognized because the device still advertises `SOURCE_TOUCHPAD` (or its name is a keyboard cover). The on-screen touchscreen trackpad is not affected.

## Sideload

The debug APK is signed with the Android debug keystore. It does not update the official app; install it beside it.

1. On the Fold, enable Developer options and USB debugging, or allow installs from your file manager ("Install unknown apps").
2. Copy `moonlight-fold.2-nonRoot-debug.apk` to the phone and open it, or from a computer:

```bash
adb install -r moonlight-fold.2-nonRoot-debug.apk
```

3. Launch **Moonlight Fold**. Pair and stream as usual. The pin-to-top option is already on; turn it off under Basic Settings if you want the keyboard to overlay the stream the way official Moonlight does.

Uninstall with `adb uninstall com.limelight.custom` or from the app info screen. That does not remove official Moonlight.

## Build

Requirements: JDK 17 or newer, Android SDK platform 37, build-tools 37, and NDK **29.0.14206865**.

```bash
# local.properties is gitignored
echo "sdk.dir=$HOME/android-sdk" > local.properties
echo "ndk.dir=$HOME/android-sdk/ndk/29.0.14206865" >> local.properties

./gradlew assembleNonRootDebug
./gradlew testNonRootDebugUnitTest
```

The APK is written to `app/build/outputs/apk/nonRoot/debug/app-nonRoot-debug.apk`.

`assembleRootDebug` is the rooted variant (`com.limelight.root.custom`, launcher name "Moonlight Fold (Root)"). Release builds keep upstream's `.unofficial` application id suffix (`com.limelight.custom.unofficial`).
