package pt.ulisboa.tecnico.cmov.airdesk.security;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Adler32;

public class SecurityLayer {

    private HashMap<String, Key> keys = new HashMap<>();
    private transient KeyPair keyPair;


    private static SecurityLayer instance = new SecurityLayer();

    private SecurityLayer(){
        // Generate key pair for 1024-bit RSA encryption and decryption
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
            kpg.initialize(1024);
            keyPair = kpg.genKeyPair();
        } catch (Exception e) {
            Log.e("SecurityLayer", "RSA key pair error");
        }
    }

    public static SecurityLayer getInstance() {
        return instance;
    }

    public ArrayList<byte[]> encrypt(Object o){
        return SecurityUtils.encript(o, keyPair.getPrivate());
    }

    public Object decrypt(String id, ArrayList<byte[]> data){
        if(keys.get(id) == null)
            return SecurityUtils.dencript(data, keyPair.getPublic());
        return SecurityUtils.dencript(data, (PublicKey) keys.get(id));
    }

    public void removeKey(String ip){
        if(keys != null) keys.remove(ip);
    }

    public void addKey(String ip, Key key){
        keys.put(ip, key);
    }

    public long makeChecksum(byte[] data){
        Adler32 adler = new Adler32();
        adler.update(data);
        return adler.getValue();
    }

    public byte[] getEncodedPublicKey(){
        return keyPair.getPublic().getEncoded();
    }

    public Key getPublicKey(){
        return keyPair.getPublic();
    }

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static String readFileAsString(String filePath)
            throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        System.out.println(fileData.toString());
        return fileData.toString();
    }
}
