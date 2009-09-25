package playhub.tb2p.server;

import java.math.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;

public class ServerCipher {

    private PrivateKey privatekey;
    public Cipher cipherEnc = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    public Cipher cipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    private final static int ENC_CHUNK_SIZE = 32;

    public ServerCipher() throws Exception {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        RSAPrivateCrtKeySpec privspec = new RSAPrivateCrtKeySpec(
            new BigInteger("8193464417525831914499101857066083679880205650172638873549157895430438125711529789094998264607842386921014735307920778225404674406482571030531313310701163"),
            new BigInteger("65537"), // pub exp
            new BigInteger("7310570087352534006749393183879934131574454512624091095576346307205011968826326658530485206787920074952646754648250102235685347220289506396948909613293473"),
            new BigInteger("106980878441477200089980950112206677308774193425864707284793114087008625387313"), // BigInteger primeP,
            new BigInteger("76588120577155133106523932910047067852415315089359493535300263826250998671451"), // BigInteger primeQ,
            new BigInteger("3140687094639701740591167554448413066543808049672150034575002693187124757697"), // BigInteger primeExponentP,
            new BigInteger("46418919655235653785254697943451479044902707335771372548570453018028227081323"), // BigInteger primeExponentQ,
            new BigInteger("92177321879690027052996997756050372307760713211308559564809584660985303508631") // BigInteger crtCoefficient
        );
        privatekey = kf.generatePrivate(privspec);
        cipherEnc.init(Cipher.ENCRYPT_MODE, privatekey);
        cipherDec.init(Cipher.DECRYPT_MODE, privatekey);
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


}
