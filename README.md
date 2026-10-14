# Nova - Personal Voice Assistant for Android

Nova is an advanced personal voice assistant built for Android devices.

## Features
- **Wake Word**: Continuous background listening for the keyword **"Nova"** using an Android Foreground Service.
- **Application Launcher**: Ability to search and open any installed application on your phone (e.g. WhatsApp, YouTube, Instagram, Camera, Settings, etc.).
- **Voice & Media Control**: YouTube playback, Google search, and phone calling automation.
- **Futuristic Mobile Interface**: SiriWave dynamic audio waveform with glowing interactive orb and slide-out chat history.

## Android Project Structure
The complete Android application source code is located in the [`nova-android/`](./nova-android) directory.

## Automated Cloud Build
This repository includes a GitHub Actions workflow in [`.github/workflows/build-apk.yml`](./.github/workflows/build-apk.yml) that automatically builds the `Nova-Assistant-APK` whenever code is pushed.
