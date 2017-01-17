package br.ufpe.cin.mpos.criptografia;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Cripto implements ICripto {
	//private String chave = "marflksdomnhtklq"; // Usando chave de 128-bits (16 bytes)
	//private String chave = "marflksdomnhtklqlksbdnfi"; // Usando chave de 192-bits (24 bytes)
	private String chave = "marflksdomnhtklqlksbdnfixbchsuey"; // Usando chave de 256-bits (32 bytes)
	private Convert cvt = new Convert();
	
	public Object encripta(Object mensagem) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException, ClassNotFoundException {

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

		byte[] chaveAux = chave.getBytes();

		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chaveAux, "AES"));
		byte[] auxiliar = cvt.converter(mensagem);
		Object encrypted = cipher.doFinal(auxiliar);
		
		return encrypted;
	}

	public Object decripta(byte[] encriptada) throws GeneralSecurityException, NoSuchPaddingException, IOException, ClassNotFoundException {

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		byte[] aux = chave.getBytes();
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aux, "AES"));
		
		byte[] decrypted = cipher.doFinal(encriptada);
		Object mensagem = cvt.desconverter(decrypted);
		
		return mensagem;
	}
}