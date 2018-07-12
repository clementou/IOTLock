package com.clement.iotlock;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.clement.iotlock.ssh.CloseTask;
import com.clement.iotlock.ssh.OpenTask;

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

public class MainActivity extends AppCompatActivity {
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch lockSwitch = findViewById(R.id.lockSwitch);
        TextView textView = findViewById(R.id.textView);

        lockSwitch.setClickable(true);
        lockSwitch.setChecked(true);

        FingerprintManager fingerprintManager = (FingerprintManager)
                getSystemService(FINGERPRINT_SERVICE);
        assert fingerprintManager != null;
        if (!fingerprintManager.isHardwareDetected()) {
            textView.setText("Your device doesn't " + "support fingerprint authentication");
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            textView.setText("No fingerprint " + "configured. Please register at least one " +
                    "fingerprint in your device's Settings");
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        assert keyguardManager != null;
        if (!keyguardManager.isKeyguardSecure()) textView.setText("Please enable lock screen " +
                "security in your device's Settings");
        else {
            try {
                generateKey();
            } catch (FingerprintException e) {
                e.printStackTrace();
            }
            if (initCipher()) {
                FingerprintManager.CryptoObject cryptoObject = new FingerprintManager
                        .CryptoObject(cipher);
                FingerPrintHandler helper = new FingerPrintHandler(this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }

        lockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) new CloseTask().execute();
                else new OpenTask().execute();
            }
        });
    }

    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    public boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

private class FingerprintException extends Exception {
    FingerprintException(Exception e) {
        super(e);
    }




    }
}
