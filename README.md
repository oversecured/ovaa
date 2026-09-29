## Description
OVAA (Oversecured Vulnerable Android App) is an Android app that aggregates all the platform's known and popular security vulnerabilities.

## List of vulnerabilities
This section only includes the list of vulnerabilities, without a detailed description or proof of concept. Examples from OVAA will receive detailed examination and analysis on [our blog](https://blog.oversecured.com/).

### Deeplinks, intents and component launches

1. Installation of an arbitrary `login_url` via deeplink `oversecured://ovaa/login?url=http://evil.com/`. Leads to the user's username and password being leaked when they log in.
2. Obtaining access to arbitrary content providers (not exported, but with the attribute `android:grantUriPermissions="true"`) via deeplink `oversecured://ovaa/grant_uri_permissions`. The attacker's app needs to process `oversecured.ovaa.action.GRANT_PERMISSIONS` and pass intent to `setResult(code, intent)` with flags such as `Intent.FLAG_GRANT_READ_URI_PERMISSION` and the URI of the content provider.
3. Access to arbitrary activities and to arbitrary content providers in `LoginActivity` by supplying an arbitrary `Intent` object to `redirect_intent`.
4. Intent redirection in `IntentVulnActivity` from the `forward_intent` extra and from `Intent.parseUri`.
5. Launching an attacker-configured component in `IntentVulnActivity` (package, class and action from extras).
6. Starting an attacker-named `Fragment` in `IntentVulnActivity`.
7. Granting content provider permissions in `IntentVulnActivity` via `grantUriPermission` and mutable `PendingIntent`s.
8. Deeplink redirection in `DeeplinkRedirectActivity` (`ovaa://redirect?next=...`), behind a caller check bypassable via `startsWith`.

### WebView

9. Vulnerable host validation when processing deeplink `oversecured://ovaa/webview?url=...`.
10. Opening arbitrary URLs via deeplink `oversecured://ovaa/webview?url=http://evilexample.com`. An attacker can use the vulnerable WebView setting `WebSettings.setAllowFileAccessFromFileURLs(true)` in the `WebViewActivity.java` file to steal arbitrary files by sending them XHR requests and obtaining their content.
11. Insecure settings in `VulnerableWebViewActivity`: file and universal file access from file URLs, content access, geolocation, mixed content, remote debugging, file-scheme and third-party cookies.
12. A JavaScript interface in `VulnerableWebViewActivity` exposing file reads, user media and the stored password.
13. XSS and HTML injection in `VulnerableWebViewActivity` via `loadData`, `loadDataWithBaseURL`, `evaluateJavascript` and a `javascript:` URL.
14. File theft in `VulnerableWebViewActivity` via `shouldInterceptRequest` and `onShowFileChooser`.
15. Attacker-controlled cookies and geolocation grants in `VulnerableWebViewActivity`.

### Theft and overwriting of files

16. Theft of arbitrary files in `MainActivity` by intercepting an activity launch from `Intent.ACTION_PICK` and passing the URI to any file as data, then copying it into the exported `LeakyProvider` where any app can read it.
17. Obtaining read/write access to arbitrary files in `TheftOverwriteProvider` via path traversal in the value of `uri.getLastPathSegment()`.
18. Copying private files to attacker-named directories in `FileAccessActivity`, plus overwriting, corrupting and deleting an attacker-supplied path.
19. World-readable and world-writable modes on attacker-supplied paths in `FileAccessActivity` and on executables in `CodeExecActivity`.
20. Theft of the file behind an activity result in `ResultTheftActivity`. Whether a third-party app can intercept the picker intent depends on the intent action, the requested MIME type and restrictions introduced in newer Android versions.
21. An exported `LeakyProvider` returning stored credentials, opening files by unvalidated path segment and deleting by attacker-supplied selection.

### User media

22. Theft of `MediaStore` images in `MediaTheftActivity`, copied to attacker-named directories, then sent out over the network, SMS, an implicit intent, the clipboard and the activity result.
23. Theft of an attacker-chosen image in `MediaTheftActivity`: the `media_uri` extra is read via `MediaStore.Images.Media.getBitmap` and written to the `content://` path in the `destination` extra and to the `server` extra.

### Data leakage

24. Insecure broadcast to `MainActivity` containing credentials. The attacker can register a broadcast receiver with action `oversecured.ovaa.action.UNPROTECTED_CREDENTIALS_DATA` and obtain the user's data.
25. Insecure activity launch in `MainActivity` with action `oversecured.ovaa.action.WEBVIEW`, containing the user's encrypted data in the query parameter `token`.
26. Obtaining access to app logs via `InsecureLoggerService`. Leak of credentials in `LoginActivity` `Log.d("ovaa", "Processing " + loginData)`.
27. Credentials and files sent out of `ExfiltrationActivity` via implicit activity, broadcast and service intents, the clipboard, SMS, a network upload, the logs and an attacker-controlled result intent.
28. Files and credentials pushed over an insecure Bluetooth socket and written to an NFC tag in `ConnectivityActivity`, which also posts and hides notifications on request.
29. `ICredentialsService`, an AIDL interface handing out the stored password, reading arbitrary files and passing the password to a caller-supplied binder.

### Deserialization

30. Deletion of arbitrary files via the insecure `DeleteFilesSerializable` deserialization object.
31. Memory corruption via the `MemoryCorruptionParcelable` object.
32. Memory corruption via the `MemoryCorruptionSerializable` object.
33. A `readObject` in `AutoLoadSerializable` that reads and uploads a file during deserialization.
34. Deserialization of attacker extras and intent forwarding in `VulnReceiver`, which also registers an unprotected receiver.

### Code execution

35. Arbitrary code execution in `OversecuredApplication` by launching code from third-party apps with no security checks.
36. Arbitrary code execution via a DEX library written into the app through the exported, writable `TheftOverwriteProvider`.
37. DEX and native library loading in `CodeExecActivity` from attacker-supplied paths, and class loading from a third-party package context.
38. Reflection calls and field writes in `CodeExecActivity` with class, method, field and value from extras.
39. OS command injection in `CodeExecActivity` via `Runtime.exec` and `ProcessBuilder`, and arbitrary system properties.

### Cryptography

40. Use of the hardcoded AES key in `WeakCrypto`.

### Network

41. Trust-all `X509TrustManager`, an always-true hostname verifier and null/anonymous ciphers over SSLv3 and TLSv1 in `InsecureNetworkActivity`.
42. Bypassable host checks in `InsecureNetworkActivity` (`contains`, `endsWith`, `startsWith`, a loose regex, backslash normalisation) and an HTTP cache at an attacker-supplied path.

### Storage and databases

43. SQL injection in `StorageActivity` via `rawQuery`, `execSQL` and a `query` selection.
44. Password storage in shared preferences, arbitrary preference files read and written, and world-accessible preferences in `StorageActivity`.

### Device control

45. In `DeviceControlActivity`: writing device settings, setting the wallpaper from an attacker URI, installing an APK, placing calls, killing processes, playing attacker audio, recording the microphone.
46. A local web server in `DeviceControlActivity` serving files by requested path.

### Configuration and secrets

47. Hardcoded credentials to a dev environment endpoint in the `test_url` entry of `strings.xml`.
48. Manifest issues: `debuggable`, `allowBackup` and `usesCleartextTraffic` all true, custom permissions at `normal` and `dangerous` levels, `protectionLevel` on a component, exported components, an `android_secret_code` receiver, unused permissions.

---------------------------------------
*Licensed under the Simplified BSD License*

*Copyright (c) 2026, Oversecured Inc*

https://oversecured.com/
