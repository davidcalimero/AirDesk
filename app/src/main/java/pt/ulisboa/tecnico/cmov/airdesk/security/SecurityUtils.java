package pt.ulisboa.tecnico.cmov.airdesk.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import pt.ulisboa.tecnico.cmov.airdesk.exception.CorruptMessageException;

//ESTA CLASS DEVE SER COPIADA PARA OS SITIOS ONDE E NECESSARIO FAZER ESTAS OPERACOES
public class SecurityUtils {

	private SecurityUtils() {
	}

	// Isto serve para ir buscar a chave publica de um serviçi externo qualquer
	public static PublicKey getPublicKey(byte[] pacote, String owner, PublicKey caPublicKey) {

		PublicKey pKey = null;

		try {
			X509Certificate.getInstance(pacote).verify(caPublicKey);
			pKey = X509Certificate.getInstance(pacote).getPublicKey();
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchProviderException e) {
			System.out.println(e.getMessage());
		} catch (SignatureException e) {
			System.out.println(e.getMessage());
		} catch (CertificateException e) {
			System.out.println(e.getMessage());
		}

		return pKey;
	}

	// Isto serve para converter o pacote recebido pelo CA num par de chaves
	public static KeyPair listByteToKeyPair(List<byte[]> keys)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keys.get(1)));
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keys.get(0)));
		KeyPair pair = new KeyPair(publicKey, privateKey);
		return pair;
	}

	// Isto serve para incriptar um objecto qualquer
	public static ArrayList<byte[]> encript(Object comprovativo, PrivateKey privateKey) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		MessageDigest messageDigest = null;
		byte[] digestedBytes = null;

		byte[] cipherBytes = null;
		Cipher cipher = null;

		ArrayList<byte[]> ret = new ArrayList<byte[]>(2);

		try {
			// Objecto em bytes
			out = new ObjectOutputStream(bos);
			out.writeObject(comprovativo);
			byte[] bytes = bos.toByteArray();

			// bytes digeridos
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(bytes);
			digestedBytes = messageDigest.digest();

			// Digestao encriptada
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			cipherBytes = cipher.doFinal(digestedBytes);

			// Construcao do array de retorno
			ret.add(bytes);
			ret.add(cipherBytes);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				out.close();
				bos.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		return ret;
	}

	// Isto serve para decriptar um objecto qualquer
	public static Object dencript(ArrayList<byte[]> pacote, PublicKey publicKey) throws CorruptMessageException {

		ByteArrayInputStream bis = null;
		ObjectInput in = null;
		Object o = null;

		MessageDigest messageDigest = null;
		byte[] digestedBytes = null;

		byte[] cipherBytes = null;
		Cipher cipher = null;

		byte[] bytes = pacote.get(0);
		byte[] encriptado = pacote.get(1);

		try {
			// bytes digeridos
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(bytes);
			digestedBytes = messageDigest.digest();

			// Digestao dencript
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			cipherBytes = cipher.doFinal(encriptado);
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println(e.getMessage());
		}

		// Confrima se sao iguais
		if (!Arrays.equals(digestedBytes, cipherBytes))
			throw new CorruptMessageException();

		// Constroi o objecto
		try {
			bis = new ByteArrayInputStream(bytes);
			in = new ObjectInputStream(bis);
			o = in.readObject();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				bis.close();
				in.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		return o;
	}
}