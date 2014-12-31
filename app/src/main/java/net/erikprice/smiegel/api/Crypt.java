package net.erikprice.smiegel.api;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

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
        byte[] authTokenBytes = Base64.decode(authToken, Base64.DEFAULT);
        byte[] sharedKeyBytes = Base64.decode(sharedKey, Base64.DEFAULT);

        if (authTokenBytes.length != 32 || sharedKeyBytes.length != 32) {
            throw new IllegalArgumentException("bad key(s), needed 32 byte (256 bit)");
        }

        // There's no way this will fail unless the JVM is running on broken hardware.
        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            this.authenticator = Mac.getInstance("HmacSHA256", "SC");

            this.sharedKey = new SecretKeySpec(sharedKeyBytes, "AES");
            this.authTokenKey = new SecretKeySpec(authTokenBytes, "AES");
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
            return Base64.encodeToString(hmac, Base64.NO_WRAP);
        } catch (InvalidKeyException e) {
            // This shouldn't be possible...
            e.printStackTrace();
        }

        return null;
    }

    public boolean authenticate(String sig, String msg) {
        return sig.equals(this.authenticate(msg));
    }

    /**
     * Return an array of base64 encoded strings representing [iv, cipher].
     * Cipher text is also authenticated.
     */
    public String[] encryptShared(String msg) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);

            byte[] iv = cipher.getIV();
            byte[] cipherAndTag = cipher.doFinal(msg.getBytes());

            byte[] cipher = Arrays.copyOfRange(cipherAndTag, 0, cipherAndTag.length - 16);
            byte[] tag = Arrays.copyOfRange(cipherAndTag, cipherAndTag.length - 16, cipherAndTag.length);

            return new String[]{
                    Base64.encodeToString(iv, Base64.NO_WRAP),
                    Base64.encodeToString(tag, Base64.NO_WRAP),
                    Base64.encodeToString(cipher, Base64.NO_WRAP)
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

        final IvParameterSpec ivSpec = new IvParameterSpec(Base64.decode(tuple[0], Base64.DEFAULT));
        final byte[] cipherText = Base64.decode(tuple[1], Base64.DEFAULT);

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
