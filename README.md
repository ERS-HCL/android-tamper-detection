# Android Application Tamper Detection

This project aims at enabling the application to do self check of possible tampers. This library provies additional mechanisms to detect tampers by reverse engineering process. 


## Checklists

This idea provides mechanism to detect whether the android application is tampered or not. The minimum required check are listed below.
1)	Check the source from where the application is installed
2)	Verifying the checksum of the application package
3)	Application signature check at runtime
4)	Check whether the application package is renamed
5)	Implementing SafetyNet API to protect the app and app's data against security threats, including device tampering, bad URLs, potentially harmful apps, and fake users.
6)	Check whether Google Play Services is available or not
7)	Check whether the app is running on a rooted device and provide appropriate warning to the user
8)	Check whether the app is signed with Debug Keystore
9)	Don't allow the app to run on Device Emulator

## How to use
Sample usage:<br>
```java
try {
	new TamperCheck(this)
			.allowDebugMode(false)
			.canRunOnEmulator(false)
			.addInstallationSource(TamperCheck.InstallationSource.GOOGLE_PLAY_STORE)
			.allowDebugKeystore(true)
			.check();
} catch (TamperCheckException e) {
	switch (e.getExceptionCode()) {
		case TamperCheckException.EXCEPTION_CODE_DEBUG_MODE:
			// Do stuff to handle debug mode
			break;
		case TamperCheckException.EXCEPTION_CODE_EMULATOR:
			// Do stuff to handle emulators
			break;
		case TamperCheckException.EXCEPTION_CODE_UNKNOWN_INSTALLER:
			// Do stuff to handle installation from unknown sources
			break;
		default:
			break;
	}
	e.printStackTrace();
}

// Get the SHA1Fingerprint of the signing keystore. Add it to the HTTP headers of your
// secured API calls to validate the requests
TamperCheckUtil.getCertificateSHA1Fingerprint(this);
```

## License

This project is licensed under MIT License.