package com.hcltech.tamperresistance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hcltech.tamperresistance.securitylib.TamperCheck;
import com.hcltech.tamperresistance.securitylib.TamperCheckException;
import com.hcltech.tamperresistance.securitylib.TamperCheckUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void runTamperCheck(View view) {
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
    }
}
