package net.erikprice.smiegel.api;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {
    private static String TAG = Crypt.class.getCanonicalName();

    // all about that gcm.
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private final Gson jsonEncoder = new Gson();

    private SecretKeySpec sharedKey;
    private SecretKeySpec authTokenKey;

    private Cipher cipher = null;
    private Mac authenticator = null;

    public Crypt(String authToken, String sharedKey) {
        if (authToken.getBytes().length != 32 || sharedKey.getBytes().length != 32) {
            throw new IllegalArgumentException("bad key(s), needed 32 byte (256 bit)");
        }

        // There's no way this will fail unless the JVM is running on broken hardware.
        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            this.authenticator = Mac.getInstance("HmacSHA256", "SC");

            this.sharedKey = new SecretKeySpec(sharedKey.getBytes(), "AES");
            this.authTokenKey = new SecretKeySpec(authToken.getBytes(), "AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the base64 encoded MAC for the given message
     */
    public String authenticate(String msg) {
        try {
            authenticator.reset();
            authenticator.init(this.authTokenKey);

            byte[] hmac = authenticator.doFinal(msg.getBytes());
            return Base64.encodeBase64String(hmac);
        } catch (InvalidKeyException e) {
            // This shouldn't be possible...
            e.printStackTrace();
        }

        return null;
    }

    public boolean authenticate(String sig, String msg) {
        String expected = this.authenticate(msg);
        return sig.equals(expected);
    }

    /**
     * Return an array of base64 encoded strings representing [iv, cipher].
     * Cipher text is also authenticated.
     */
    public String[] encryptShared(String msg) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);

            return new String[]{
                    Base64.encodeBase64String(cipher.getIV()),
                    Base64.encodeBase64String(cipher.doFinal(msg.getBytes()))
            };
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            // These are all indicative of a programming error, not a runtime one...

            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param tuple base64 encoded strings of [iv, cipher]
     * @return decrypted string, or null if authentication fails
     */
    public String decryptShared(String[] tuple) {
        if (tuple.length != 2) {
            Log.e(TAG, "bad array passed to decryptShared (expected 2-tuple)");
            return null;
        }

        final IvParameterSpec ivSpec = new IvParameterSpec(Base64.decodeBase64(tuple[0]));
        final byte[] cipherText = Base64.decodeBase64(tuple[1]);

        // TODO: conceivably need to check InvalidKeyException
        // BadPaddingException -> AEADBadTagException -> tampered
        try {
            cipher.init(Cipher.DECRYPT_MODE, sharedKey, ivSpec);
            return new String(cipher.doFinal(cipherText));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            Log.e(TAG, "decryption failed: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public byte[] getSharedKey() {
        return sharedKey.getEncoded();
    }

    public byte[] getAuthToken() {
        return authTokenKey.getEncoded();
    }
}
