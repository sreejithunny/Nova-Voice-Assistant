package com.nova.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppManager {
    private static final String TAG = "NovaAppManager";
    private final Context context;
    private final PackageManager packageManager;
    private final Map<String, String> installedApps = new HashMap<>();

    public AppManager(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        refreshAppList();
    }

    public synchronized void refreshAppList() {
        installedApps.clear();
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo resolveInfo : pkgAppsList) {
                CharSequence label = resolveInfo.loadLabel(packageManager);
                String appName = label != null ? label.toString().trim().toLowerCase(Locale.ROOT) : "";
                String packageName = resolveInfo.activityInfo.packageName;

                if (!appName.isEmpty()) {
                    installedApps.put(appName, packageName);
                    // Also register without spaces (e.g. "whatsapp" for "whats app")
                    String noSpaceName = appName.replace(" ", "");
                    if (!noSpaceName.equals(appName)) {
                        installedApps.put(noSpaceName, packageName);
                    }
                }
            }
            Log.d(TAG, "Indexed " + installedApps.size() + " launcher apps.");
        } catch (Exception e) {
            Log.e(TAG, "Error indexing apps: " + e.getMessage());
        }
    }

    /**
     * Attempts to find and open an installed application by spoken or typed name.
     * @param targetAppName e.g. "WhatsApp", "YouTube", "Instagram", "Camera", "Calculator", "Chrome"
     * @return true if successfully launched, false if not found.
     */
    public boolean openApplication(String targetAppName) {
        if (targetAppName == null || targetAppName.trim().isEmpty()) {
            return false;
        }

        String query = targetAppName.trim().toLowerCase(Locale.ROOT);
        // Clean common noise prefixes
        query = query.replace("nova", "")
                     .replace("please", "")
                     .replace("open", "")
                     .replace("launch", "")
                     .replace("start", "")
                     .trim();

        if (query.isEmpty()) return false;

        // 1. Direct match
        if (installedApps.containsKey(query)) {
            return launchPackage(installedApps.get(query));
        }

        // 2. Direct match without spaces
        String queryNoSpaces = query.replace(" ", "");
        if (installedApps.containsKey(queryNoSpaces)) {
            return launchPackage(installedApps.get(queryNoSpaces));
        }

        // 3. Substring match (e.g. "youtube music" matches "youtube")
        for (Map.Entry<String, String> entry : installedApps.entrySet()) {
            String appName = entry.getKey();
            if (appName.contains(query) || query.contains(appName)) {
                return launchPackage(entry.getValue());
            }
        }

        // 4. Common well-known package fallbacks
        String fallbackPackage = getCommonPackageFallback(queryNoSpaces);
        if (fallbackPackage != null) {
            return launchPackage(fallbackPackage);
        }

        return false;
    }

    private boolean launchPackage(String packageName) {
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch package " + packageName + ": " + e.getMessage());
        }
        return false;
    }

    private String getCommonPackageFallback(String name) {
        switch (name) {
            case "whatsapp": return "com.whatsapp";
            case "youtube": return "com.google.android.youtube";
            case "chrome": return "com.android.chrome";
            case "camera": return "com.google.android.GoogleCamera";
            case "settings": return "com.android.settings";
            case "instagram": return "com.instagram.android";
            case "facebook": return "com.facebook.katana";
            case "spotify": return "com.spotify.music";
            case "maps": return "com.google.android.apps.maps";
            case "gmail": return "com.google.android.gm";
            default: return null;
        }
    }
}
