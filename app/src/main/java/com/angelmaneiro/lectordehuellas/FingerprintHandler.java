package com.angelmaneiro.lectordehuellas;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;


@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManagerCompat.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;
    private FingerprintListener fingerprintListener;

    public FingerprintHandler(Context context) {
        appContext = context;
        if(appContext instanceof FingerprintListener){
            fingerprintListener = (FingerprintListener) appContext;
        }
    }
    public void startAuth(FingerprintManagerCompat manager, FingerprintManagerCompat.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, 0, cancellationSignal, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.i("onAuthenticationError", errString.toString());
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Log.i("onAuthenticationHelp", helpString.toString());
    }

    @Override
    public void onAuthenticationFailed() {
        if(fingerprintListener!=null){
            fingerprintListener.invalidFingerprint();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        if(fingerprintListener!=null){
            fingerprintListener.validFingerprint();
        }
    }
}
