# Manual verification guide

This sandbox has no hardware-accelerated Android emulator and no way to reach
`dl.google.com` (the Android SDK / AndroidX Maven host), so the parts of this
app that only make sense on a real device — the lock screen, real audio
output, real vibration motor — could not be exercised here. Everything that
*could* be verified without a device was: `:core`'s puzzle/alarm-time logic
(31 unit tests, run locally) and the full `:app` module build + Robolectric
tests (via the GitHub Actions workflow in `.github/workflows/android-ci.yml`).

The steps below are what's left: confirming on your own phone (or a
KVM-capable emulator) that the three behaviors the whole app is built around
actually work as intended.

## Get the app onto your phone

1. Once the GitHub Actions run on this branch is green, open the run in the
   **Actions** tab of the repo and download the `brainspresso-debug-apk`
   artifact — it's a zip containing `app-debug.apk`.
2. Copy that APK to your phone (email it to yourself, use a cable, whatever's
   easiest) and tap it to install. You'll need to allow "install unknown apps"
   for whichever app you used to open it — Android will prompt you for this.
3. Alternatively, if you have Android Studio: open this project, plug in a
   phone with USB debugging enabled, and press Run.

No Play Store listing or $25 developer fee is needed for this — sideloading
a debug APK onto your own device is free either way.

## First launch

- Grant the notification permission when prompted (Android 13+). Without it,
  the alarm's notification — and the full-screen puzzle it triggers — won't
  show.
- Add an alarm for 1-2 minutes from now with a memorable label, leave sound
  and vibration on, and go to the alarm list.

## 1. Does the puzzle screen really show over the lock screen?

- Lock your phone (power button) and wait for the alarm time.
- **Expected:** the screen turns on by itself and the puzzle appears directly
  over the lock screen — you should not need to swipe/enter your PIN first to
  see it.
- Try pressing the back button: nothing should happen (it's deliberately a
  no-op).

## 2. Does the foreground service really keep ringing?

- While the puzzle is showing, confirm the ringtone you picked (or your
  phone's default alarm sound if you left it on default) is looping, and the
  phone is vibrating in the on/off pattern, if vibration was enabled.
- Pull down the notification shade: you should see a persistent "Brainspresso
  alarm" notification that **cannot be swiped away** and has no snooze/stop
  button on it.
- Try swiping the app away from the recents/overview screen. The alarm should
  keep ringing — the foreground service is independent of the puzzle
  activity's lifecycle.

## 3. Does solving the puzzle really stop it — and only that?

- Deliberately get one puzzle wrong first (enter a wrong number, tap the
  wrong grid cell, etc.). Confirm: the alarm keeps ringing, and a **new,
  different** puzzle appears — you're never shown the same one to retry.
- If you set "puzzles required" to more than 1 in Settings, confirm a wrong
  answer resets your progress back to 0 solved (this is intentional — a wrong
  answer should cost you the whole streak, not just that one puzzle).
- Solve the required number of puzzles in a row correctly. **Expected:** the
  ringing and vibration stop immediately, the notification disappears, and
  you're returned to an unlocked device.

## Other things worth checking

- **Repeating alarms:** create an alarm with a couple of weekdays checked,
  let it fire, and confirm it's still enabled afterward (one-time alarms
  should auto-disable after firing; repeating ones should not).
- **Reboot survival:** with an alarm enabled, reboot the phone, then check
  the alarm still fires at its scheduled time — `BootReceiver` re-arms
  everything that AlarmManager forgets across a reboot.
- **Ringtone picker:** in Edit Alarm, tap "Alarm sound" — this opens the
  system's own ringtone picker scoped to alarm sounds, not a custom UI, so
  whatever's already on your phone should show up there.
- **Settings:** in Settings, try disabling all but one puzzle type (the app
  won't let you disable the last one), change difficulty, and change the
  required streak (1-5) — the next alarm should reflect all three.

## A known, unavoidable limitation

`PuzzleActivity` makes a best-effort attempt to reopen itself if you leave
via the home button or recents before solving the puzzle (see the comment on
`PuzzleActivity.onStop()`). This is a real mitigation used by several
alarm apps, but it is **not a guarantee** — Android does not give any
third-party app a way to fully block the home button or the system back
gesture. If you press home, the puzzle will very likely pop back to the
foreground within a fraction of a second on most devices, but a determined
user (or an unusual OEM launcher) can still get past it. There's no way
around this without a device-owner/kiosk-mode setup, which is out of scope
for a normal installable app.
