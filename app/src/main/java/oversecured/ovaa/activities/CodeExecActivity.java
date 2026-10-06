package oversecured.ovaa.activities;

import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class CodeExecActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String className = intent.getStringExtra("class");
        String methodName = intent.getStringExtra("method");
        String argument = intent.getStringExtra("arg");
        String fieldName = intent.getStringExtra("field");
        String value = intent.getStringExtra("value");
        String propertyKey = intent.getStringExtra("key");
        String packageName = intent.getStringExtra("package");
        String command = intent.getStringExtra("cmd");

        loadFromControlledPath(path);
        loadNativeLibrary(path);
        callViaReflection(className, methodName, argument);
        setFieldViaReflection(className, fieldName, value);
        defineClass(path, className);
        overrideSystemProperty(propertyKey, value);
        loadFromPackageContext(packageName);
        makeExecutableWorldWritable(path);
        runCommand(command);
        loadArbitraryClass(className);
        finish();
    }

    private void loadFromControlledPath(String path) {
        if (path == null) {
            return;
        }
        DexClassLoader loader = new DexClassLoader(path, getCacheDir().getAbsolutePath(), null, getClassLoader());
        try {
            loader.loadClass("com.example.Plugin").getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
        }
    }

    private void loadNativeLibrary(String path) {
        if (path == null) {
            return;
        }
        System.load(path);
    }

    private void callViaReflection(String className, String methodName, String arg) {
        if (className == null || methodName == null) {
            return;
        }
        try {
            Class<?> klass = Class.forName(className);
            Method method = klass.getMethod(methodName, String.class);
            method.invoke(klass.getDeclaredConstructor().newInstance(), arg);
        } catch (Exception ignored) {
        }
    }

    private void setFieldViaReflection(String className, String fieldName, String value) {
        if (className == null || fieldName == null) {
            return;
        }
        try {
            Class<?> klass = Class.forName(className);
            Field field = klass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception ignored) {
        }
    }

    private void defineClass(String path, String className) {
        if (path == null || className == null) {
            return;
        }
        try {
            PathClassLoader loader = new PathClassLoader(path, getClassLoader());
            loader.loadClass(className);
        } catch (Exception ignored) {
        }
    }

    private void overrideSystemProperty(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        System.setProperty(key, value);
    }

    private void loadFromPackageContext(String packageName) {
        if (packageName == null) {
            return;
        }
        try {
            Context other = createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            other.getClassLoader().loadClass("com.example.Entry").getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
        }
    }

    private void makeExecutableWorldWritable(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        file.setWritable(true, false);
        file.setReadable(true, false);
        file.setExecutable(true, false);
    }

    private void runCommand(String cmd) {
        if (cmd == null) {
            return;
        }
        try {
            Runtime.getRuntime().exec(new String[] {"/system/bin/sh", "-c", "echo " + cmd});
            new ProcessBuilder("/system/bin/ping", cmd).start();
        } catch (Exception ignored) {
        }
    }

    private void loadArbitraryClass(String className) {
        if (className == null) {
            return;
        }
        try {
            getClassLoader().loadClass(className);
        } catch (Exception ignored) {
        }
    }
}
