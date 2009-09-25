package playhub.tb2p.client;

import java.math.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;

public class ClientCipher {

    private PublicKey publickey;
    public Cipher cipherEnc = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    public Cipher cipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    private final static int ENC_CHUNK_SIZE = 32;

    public ClientCipher() throws Exception {

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pubspec = new RSAPublicKeySpec(
                new BigInteger("8193464417525831914499101857066083679880205650172638873549157895430438125711529789094998264607842386921014735307920778225404674406482571030531313310701163"), // mod
                new BigInteger("65537") // public exp
                );
        publickey = kf.generatePublic(pubspec);
        cipherEnc.init(Cipher.ENCRYPT_MODE, publickey);
        cipherDec.init(Cipher.DECRYPT_MODE, publickey);
    }

    public byte[] encrypt(byte[] input)  throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] chunk = new byte[ENC_CHUNK_SIZE];
        int readBytes=0;
        int chunk_i=0;
        while ((readBytes=(is.read(chunk,0,ENC_CHUNK_SIZE)))>0) {
            chunk_i++;
            byte[] encrypted = cipherEnc.doFinal(chunk,0,readBytes);
            bos.write(encrypted);
        }
        return bos.toByteArray();
    }

    public byte[] decrypt(byte[] input) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] chunk = new byte[ENC_CHUNK_SIZE*2];
        int readBytes=0;
        while ((readBytes=(is.read(chunk)))>0) {
            bos.write(cipherDec.doFinal(chunk,0,readBytes));
        }
        return bos.toByteArray();
    }


}
