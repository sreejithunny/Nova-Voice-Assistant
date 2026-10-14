# Nova Voice Assistant (Android APK)

An advanced Android Voice Assistant application inspired by Jarvis, featuring a futuristic holographic SiriWave UI, continuous wake-word listening ("Nova"), and full app-launching capabilities.

---

## Features

1. **Wake-Up Word ("Nova")**:
   - Runs a persistent Android **Foreground Service** (`NovaWakeService`).
   - Listens continuously for the wake word **"Nova"**.
   - Calling *"Nova"* automatically activates the assistant, prompts *"Yes, I am listening"*, and prepares to receive commands.

2. **Access & Open Any Installed Application**:
   - Uses Android's `PackageManager` with `QUERY_ALL_PACKAGES` permission (`AppManager.java`).
   - Voice commands like:
     - *"Nova, open WhatsApp"*
     - *"Nova, launch YouTube"*
     - *"Nova, open Camera"*
     - *"Nova, open Spotify"*
     - *"Nova, open Instagram"*
   - Automatically searches and launches the app, or falls back to Google Play Store if not installed.

3. **Media & Web Automation**:
   - *"Play [song name] on YouTube"* -> Opens YouTube and plays the track.
   - *"Search Google for [query]"* -> Performs quick search in default browser.
   - *"Call [name/number]"* -> Launches Android phone dialer.
   - Conversational questions (Time, Date, identity, greetings).

4. **Futuristic Mobile Interface**:
   - Dark theme (`#05050A`) with dynamic glowing particle ring.
   - Real-time SiriWave audio waveform.
   - Slide-out conversation history drawer.
   - Interactive mic and text input.

---

## How to Build the APK

### Method 1: Open in Android Studio (Recommended)
1. Open **Android Studio**.
2. Click **Open** and select the folder `nova-android`.
3. Wait for Gradle sync to complete.
4. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will generate the APK at:
   `app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Automated Cloud Build with GitHub Actions
Because your repository includes `.github/workflows/build-apk.yml`:
1. Push this project to your GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Nova Voice Assistant for Android"
   git remote add origin <your-github-repo-url>
   git push -u origin main
   ```
2. Navigate to the **Actions** tab on your GitHub repository.
3. The **Build Nova APK** workflow will compile the APK in the cloud.
4. Download the `Nova-Assistant-APK` artifact directly to your phone and install!

### Method 3: Local Command-Line Build
Run `build_local.bat` in the `nova-android` folder:
```cmd
cd nova-android
build_local.bat
```

---

## How to Install on Your Android Phone

1. Transfer the `.apk` file (`app-debug.apk`) to your phone via USB cable, Google Drive, WhatsApp, or email.
2. Tap the APK on your phone to install.
3. If prompted, enable **"Install unknown apps"** in your phone settings.
4. Open **Nova** and grant the **Microphone** and **Notification** permissions when requested.
5. Say **"Nova"** or tap the mic button to start using your assistant!
