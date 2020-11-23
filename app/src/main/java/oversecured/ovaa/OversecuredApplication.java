package oversecured.ovaa;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class OversecuredApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        updateChecker();
        invokePlugins();
    }

    private void invokePlugins() {
        for(PackageInfo info : getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA)) {
            String packageName = info.packageName;
            Bundle meta = info.applicationInfo.metaData;
            if(packageName.startsWith("oversecured.plugin.")
                    && meta.getInt("version", -1) >= 10) {

                try {
                    Context packageContext = createPackageContext(packageName,
                            CONTEXT_INCLUDE_CODE | CONTEXT_IGNORE_SECURITY);
                    packageContext.getClassLoader()
                            .loadClass("oversecured.plugin.Loader")
                            .getMethod("loadMetadata", Context.class)
                            .invoke(null, this);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void updateChecker() {
        try {
            File file = new File("/sdcard/updater.apk");
            if (file.exists() && file.isFile() && file.length() <= 1000) {
                DexClassLoader cl = new DexClassLoader(file.getAbsolutePath(), getCacheDir().getAbsolutePath(),
                        null, getClassLoader());
                int version = (int) cl.loadClass("oversecured.ovaa.updater.VersionCheck")
                        .getDeclaredMethod("getLatestVersion").invoke(null);
                if(Build.VERSION.SDK_INT < version) {
                    Toast.makeText(this, "Update required!", Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            //ignore
        }
    }
}
