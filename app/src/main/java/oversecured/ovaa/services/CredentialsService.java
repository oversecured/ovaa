package oversecured.ovaa.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import oversecured.ovaa.utils.LoginUtils;

public class CredentialsService extends Service {
    private final ICredentialsService.Stub binder = new ICredentialsService.Stub() {
        @Override
        public String getPassword() {
            return LoginUtils.getInstance(CredentialsService.this).getLoginData().password;
        }

        @Override
        public String readFile(String path) {
            if (path == null) {
                return null;
            }
            try {
                return FileUtils.readFileToString(new File(path), "UTF-8");
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public void storeToken(String token) {
            getSharedPreferences("secrets", MODE_PRIVATE).edit().putString("token", token).commit();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IBinder remote = intent.getExtras() != null ? intent.getExtras().getBinder("callback") : null;
        if (remote != null) {
            try {
                ICredentialsService.Stub.asInterface(remote)
                        .storeToken(LoginUtils.getInstance(this).getLoginData().password);
            } catch (RemoteException ignored) {
            }
        }
        return START_NOT_STICKY;
    }
}
