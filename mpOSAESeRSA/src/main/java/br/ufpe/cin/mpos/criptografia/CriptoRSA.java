package br.ufpe.cin.mpos.criptografia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.os.Environment;
   
   
public class CriptoRSA {
	public static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final String PATH_CHAVE_PRIVADA = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BenchImageOutput/private.key";
	public static final String PATH_CHAVE_PUBLICA = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BenchImageOutput/public.key";
	private ObjectInputStream inputStream;  

    public Object encriptakey(byte[] mensagem) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
    	
    	inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PUBLICA));
        final PublicKey chavePublica = (PublicKey) inputStream.readObject();
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, chavePublica);
        Object encrypted = cipher.doFinal(mensagem);
      
        return encrypted;
    }
   
    public byte[] decriptakey(byte[] encriptada) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {

        inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PRIVADA));
        final PrivateKey chavePrivada = (PrivateKey) inputStream.readObject();
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, chavePrivada);
		byte[] decrypted = cipher.doFinal(encriptada);
   
        return decrypted;
   }
}