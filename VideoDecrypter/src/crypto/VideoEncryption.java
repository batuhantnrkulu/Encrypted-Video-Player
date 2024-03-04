package crypto;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

public class VideoEncryption 
{
	private static final Logger LOG = Logger.getLogger(Util.class.getSimpleName());
	
	public static String encryptVideo(String filePath) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException
	{	
		String fileExtension = Util.getExtensionByStringHandling(filePath).get();
		
		if (!fileExtension.equals("mp4"))
		{
			throw new RuntimeException("File extension should be mp4");
		}
		
		// Look for files here
		Path tempDir = Util.createTempDirectory();
		
		final Path encryptedPath = tempDir.resolve(
				"output.encryptedmp4");
		
		try (InputStream fin = new FileInputStream(filePath);
				OutputStream fout = Files.newOutputStream(encryptedPath);
				CipherOutputStream cipherOut = new CipherOutputStream(fout, Util.getCipher(Util.CipherMode.ENCRYPT))) 
		{
			final byte[] bytes = new byte[1024];
			
			for (int length = fin.read(bytes); length != -1; length = fin.read(bytes)) 
			{
				cipherOut.write(bytes, 0, length);
			}
		}
		catch (IOException e) 
		{
			LOG.log(Level.INFO, "Unable to encrypt", e);
		}
		
		LOG.info("Encryption finished, saved at " + encryptedPath);
		
		return encryptedPath.toString();
	}
}
