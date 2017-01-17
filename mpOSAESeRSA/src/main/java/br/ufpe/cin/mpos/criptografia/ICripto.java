package br.ufpe.cin.mpos.criptografia;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface ICripto {

	public Object encripta(Object mensagem) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IOException, ClassNotFoundException;
	public Object decripta(byte[] encriptada) throws GeneralSecurityException, NoSuchPaddingException, IOException, ClassNotFoundException;
}