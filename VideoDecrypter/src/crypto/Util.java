package crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public final class Util 
{	
	private static Path tempDir = null;
	
	private static final String ALGORITHM = "AES";
	private static final String CIPHER = "AES/CBC/PKCS5PADDING";
	
	private static IvParameterSpec iv = null;
	private static SecretKeySpec skeySpec = null;
	
	static enum CipherMode {ENCRYPT, DECRYPT}
	
	static
	{
		SecureRandom sr = new SecureRandom();
		
		byte[] key = new byte[16];
		sr.nextBytes(key); // 128 bit key
		
		byte[] initVector = new byte[16];
		sr.nextBytes(initVector); // 16 bytes IV
		
		System.out.println("Random key=" + bytesToHex(key));
		System.out.println("initVector=" + bytesToHex(initVector));
		
		iv = new IvParameterSpec(initVector);
		skeySpec = new SecretKeySpec(key, ALGORITHM);
	}
	
	public static Cipher getCipher(CipherMode cipherMode) 
			throws InvalidKeyException, InvalidAlgorithmParameterException, 
			NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher cipher = Cipher.getInstance(CIPHER);
		
		if (cipherMode.equals(CipherMode.ENCRYPT))
		{
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		} 
		else if (cipherMode.equals(CipherMode.DECRYPT))
		{
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		}
		
		return cipher;
	}
	
	public static String bytesToHex(byte[] bytes) 
	{
        StringBuilder sb = new StringBuilder();
        
        for (byte b : bytes) 
        {
            sb.append(String.format("%02X ", b));
        }
        
        return sb.toString();
    }
	
	public static Optional<String> getExtensionByStringHandling(String filename) {
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}
	
	public static String getFilePath(String changedFilePath)
	{
		return changedFilePath.replace("Selected file: ", "");
	}
	
	public static Path createTempDirectory() throws IOException
	{
		if (tempDir != null)
		{
			return tempDir;
		}
		
		tempDir = Files.createTempDirectory("cyber-crypto");
		return tempDir;
	}
}
