# J.A.R.V.I.S. — Android voice assistant

A simple Java Android starter project, compatible with Android API 23+.

## Features
- Russian speech recognition after tapping the microphone button.
- Russian spoken responses using Android TextToSpeech.
- Responds to the phrase “Эй, Джарвис”.
- Announces the current time and date.
- Opens YouTube, Chrome, Minecraft, Roblox and Telegram when installed.
- Dark blue interface.

## Build
The project uses Android Gradle Plugin 7.4.2 and compile SDK 33. Open it in AIDE/AndroidIDE or use the GitHub Actions workflow in this repository.

## Important limitations
- This first version listens only after the user taps the button. It does not run an always-on wake-word listener.
- Speech recognition may require internet and an installed Android recognition service.
- A Russian TextToSpeech voice must be installed on the device.
- Only the listed apps and commands are supported.
