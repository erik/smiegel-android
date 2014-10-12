package net.erikprice.smiegel;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class APIClient {
    // all about that gcm.
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Gson jsonEncoder = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    private SecretKeySpec key;
    private URL apiURL;
    private Cipher cipher = null;

    public APIClient(String apiHost, int apiPort, String key) {
        try {
            this.apiURL = new URL("http", apiHost, apiPort, "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // There's no way this will fail unless the JVM is running on broken hardware.
        try {
            // Normalize key length to 256 bits
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            this.key = new SecretKeySpec(sha256.digest(key.getBytes()), "AES");
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public Response post(String path, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, encrypt(json));
        Request request = new Request.Builder()
                .url(new URL(apiURL, path))
                .post(body)
                .build();

        return client.newCall(request).execute();
    }

    public String encrypt(String msg) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return jsonEncoder.toJson(new String[]{
                    Base64.encodeBase64String(cipher.getIV()),
                    Base64.encodeBase64String(cipher.doFinal(msg.getBytes()))
            });
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            // These are all indicative of a programming error, not a runtime one...

            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(String encodedJson) {
        final String[] array = jsonEncoder.fromJson(encodedJson, String[].class);

        final IvParameterSpec ivSpec = new IvParameterSpec(Base64.decodeBase64(array[0]));
        final byte[] cipherText = Base64.decodeBase64(array[1]);

        // TODO: conceivably need to check InvalidKeyException
        // BadPaddingException -> AEADBadTagException -> tampered
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return new String(cipher.doFinal(cipherText));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
