package oversecured.ovaa;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;

public class OversecuredApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        invokePlugins();
    }

    private void invokePlugins() {
        for(PackageInfo info : getPackageManager().getInstalledPackages(0)) {
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
}
