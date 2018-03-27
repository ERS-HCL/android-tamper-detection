package com.hcltech.tamperresistance.securitylib;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

/**
 * Adding tamper resistance to the application by doing basic checks.
 */
public class TamperCheck {

    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

    /**
     * Google play installer.
     */
    public static final String INSTALLER_GOOGLE_PLAY_STORE = "com.android.vending";
    /**
     * Amazon app store installer.
     */
    public static final String INSTALLER_AMAZON_APP_STORE = "com.amazon.venezia";
    /**
     * Samsung app store installer.
     */
    public static final String INSTALLER_SAMSUNG_APP_STORE = "com.sec.android.app.samsungapps";


    /**
     * Trusted sources of installation
     */
    public enum InstallationSource {
        GOOGLE_PLAY_STORE, AMAZON_STORE, SAMSUNG_STORE
    }


    private Context mContext;
    private List<InstallationSource> mIstallationSource;
    private boolean mDebugMode = false;
    private boolean mRunOnEmulator = false;
    private boolean allowDebugKeystore = false;

    public TamperCheck(Context context) {
        this.mContext = context;
        this.mIstallationSource = new ArrayList<>();
    }

    /**
     * Get the list of allowed installation sources
     * @return
     */
    public List<InstallationSource> getInstallationSources() {
        return mIstallationSource;
    }

    /**
     * Set a list of allowed installation sources. Accepting only a know list of trusted sources to avoid
     * hard-setting of the installer package names.
     * @param installationSources
     * @return
     */
    public TamperCheck setInstallationSources(@NonNull List<InstallationSource> installationSources) {
        this.mIstallationSource = installationSources;
        return this;
    }

    /**
     * Get the current installer package name
     * @return
     */
    public String getCurrentInstaller() {
        return mContext.getPackageManager().getInstallerPackageName(mContext.getPackageName());
    }

    /**
     * Add allowed installation source.
     * @param installationSource
     * @return
     */
    public TamperCheck addInstallationSource(InstallationSource installationSource) {
        this.mIstallationSource.add(installationSource);
        return this;
    }

    /**
     * Check whether the current application build is a DEBUG build by checking the signing keystore,
     * {@link BuildConfig#DEBUG} and {@link ApplicationInfo#flags}, {@link ApplicationInfo#FLAG_DEBUGGABLE}
     * @return
     */
    public boolean isDebugBuild(){
        return BuildConfig.DEBUG
                || isSignedWithDebugKeystore()
                || ( 0 != ( mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE )
                || (!this.allowDebugKeystore && isSignedWithDebugKeystore()));
    }

    /**
     * Get whether debug mode is enabled
     * @return
     */
    public boolean isDebugModeEnabled() {
        return this.mDebugMode;
    }

    /**
     * Set whether the application is allowed to run in debug mode
     * @param debugMode
     * @return
     */
    public TamperCheck allowDebugMode(boolean debugMode) {
        this.mDebugMode = debugMode;
        return this;
    }

    /**
     * Set whether to exclude checking the signing keystore.
     * @return
     */
    public TamperCheck allowDebugKeystore(boolean keystoreCheck){
        this.allowDebugKeystore = keystoreCheck;
        return this;
    }

    /**
     * Check whether the application is signed with debug keystore
     * @return
     */
    private boolean isSignedWithDebugKeystore()
    {
        boolean debuggable = false;

        try
        {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),PackageManager.GET_SIGNATURES);
            Signature signatures[] = pInfo.signatures;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for ( int i = 0; i < signatures.length;i++)
            {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
        }
        catch (CertificateException e)
        {
        }
        return debuggable;
    }

    /**
     * Check whether the applicaiton is allowed to run on emulator
     * @return
     */
    public boolean isRunOnEmulatorEnabled() {
        return this.mRunOnEmulator;
    }

    /**
     * Set whether the application is allowed to run on emulator
     * @param runOnEmulator
     * @return
     */
    public TamperCheck canRunOnEmulator(boolean runOnEmulator) {
        this.mRunOnEmulator = runOnEmulator;
        return this;
    }

    /**
     * Check whether the application is currently running on emulator
     * @return
     */
    public boolean isRunningOnEmulator() {

        String buildDetails = (Build.FINGERPRINT + Build.DEVICE + Build.MODEL + Build.BRAND + Build.PRODUCT + Build.MANUFACTURER + Build.HARDWARE).toLowerCase();

        return buildDetails.contains("generic")
                || buildDetails.contains("unknown")
                || buildDetails.contains("emulator")
                || buildDetails.contains("sdk")
                || buildDetails.contains("genymotion")
                || buildDetails.contains("x86")
                || buildDetails.contains("goldfish")
                || buildDetails.contains("test-keys");
    }

    /**
     * Run tamper detection checks.
     *
     * @throws TamperCheckException
     */
    public void check() throws TamperCheckException{

        if(isDebugBuild() && !isDebugModeEnabled()){
            String errorMessage = "The device is running in DEBUG mode";
            throw new TamperCheckException(TamperCheckException.EXCEPTION_CODE_DEBUG_MODE, errorMessage);
        }

        if(isRunningOnEmulator() && !isRunOnEmulatorEnabled()){
            String errorMessage = String.format("Build.FINGERPRINT - %s \nBuild.DEVICE - %s \nBuild.MODEL - %s \nBuild.BRAND - %s \nBuild.PRODUCT - %s \nBuild.MANUFACTURER - %s \nBuild.HARDWARE - %s \n",
                    Build.FINGERPRINT , Build.DEVICE , Build.MODEL , Build.BRAND , Build.PRODUCT , Build.MANUFACTURER , Build.HARDWARE);
            throw new TamperCheckException(TamperCheckException.EXCEPTION_CODE_EMULATOR, errorMessage);
        }

        checkInstallationSource();
    }

    /**
     *  Check whether the current installer package is allowed
     */
    private void checkInstallationSource() throws TamperCheckException {
        String currentInstallerPackage = getCurrentInstaller();
        for(InstallationSource source : getInstallationSources()){
            switch (source){
                case AMAZON_STORE:
                    if(currentInstallerPackage.equalsIgnoreCase(INSTALLER_AMAZON_APP_STORE)){
                        return;
                    }
                    break;
                case GOOGLE_PLAY_STORE:
                    if(currentInstallerPackage.equalsIgnoreCase(INSTALLER_GOOGLE_PLAY_STORE)){
                        return;
                    }
                    break;
                case SAMSUNG_STORE:
                    if(currentInstallerPackage.equalsIgnoreCase(INSTALLER_SAMSUNG_APP_STORE)){
                        return;
                    }
                    break;
            }
        }
        throw new TamperCheckException(TamperCheckException.EXCEPTION_CODE_UNKNOWN_INSTALLER, "This application is installed from an unknown source : "+ currentInstallerPackage);
    }
}
