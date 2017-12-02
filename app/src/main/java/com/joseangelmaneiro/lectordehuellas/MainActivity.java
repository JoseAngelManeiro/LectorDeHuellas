package com.joseangelmaneiro.lectordehuellas;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity implements
        FingerprintHandler.FingerprintListener {

    private static final String KEY_NAME = "example_key";

    private FingerprintManagerCompat fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManagerCompat.CryptoObject cryptoObject;

    private TextView textViewLabel;
    private ImageView imageViewIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLabel = (TextView)findViewById(R.id.label_TextView);
        imageViewIcon = (ImageView)findViewById(R.id.icon_ImageView);

        if(checkFingerprint()){

            generateKey();

            if (cipherInit()) {
                cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }

    }

    private boolean checkFingerprint(){
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = FingerprintManagerCompat.from(this);

        if (!fingerprintManager.isHardwareDetected()) {
            textViewLabel.setText("El dispositivo no tiene sensor de huellas digitales");
            return false;
        }

        if (!keyguardManager.isKeyguardSecure()) {
            textViewLabel.setText("La seguridad de la pantalla de bloqueo no está habilitada en Ajustes");
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            textViewLabel.setText("El permiso de autenticación de huellas digitales no está habilitado");
            return false;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            textViewLabel.setText("Debe registrar al menos una huella digital en Ajustes");
            return false;
        }

        return true;
    }


    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Error al obtener la instancia de KeyGenerator", e);
        }
        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {

            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Error al obtener Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al iniciar Cipher", e);
        }
    }

    @Override
    public void validFingerprint() {
        imageViewIcon.setImageResource(R.drawable.ic_ok);
        textViewLabel.setText("Huella válida");
    }

    @Override
    public void invalidFingerprint() {
        imageViewIcon.setImageResource(R.drawable.ic_error);
        textViewLabel.setText("Huella no válida");
    }

}
