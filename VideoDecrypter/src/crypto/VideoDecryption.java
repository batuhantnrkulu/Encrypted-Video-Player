package crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

public class VideoDecryption 
{
private static final Logger LOG = Logger.getLogger(Util.class.getSimpleName());
	
	public static String decyrptVideo(String filePath) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException
	{	
		String fileExtension = Util.getExtensionByStringHandling(filePath).get();
		
		if (!fileExtension.equals("mp4") && !fileExtension.equals("encryptedmp4"))
		{
			throw new RuntimeException("File extension should be mp4 or encryptedmp4 for decryption");
		}
		
		// Look for files here
		Path tempDir = Util.createTempDirectory();
	
		final Path decryptedPath = tempDir.resolve(
				"decrypted_output.mp4");
		
		try (InputStream encryptedData = Files.newInputStream(Paths.get(filePath));
				CipherInputStream decryptStream = new CipherInputStream(encryptedData, Util.getCipher(Util.CipherMode.DECRYPT));
				OutputStream decryptedOut = Files.newOutputStream(decryptedPath)) 
		{
			final byte[] bytes = new byte[1024];
			
			for (int length = decryptStream.read(bytes); length != -1; length = 
					decryptStream.read(bytes)) 
			{
				decryptedOut.write(bytes, 0, length);
			}
			
			//set hidden attribute
	        Files.setAttribute(decryptedPath, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
		} 
		catch (IOException ex) 
		{
			Logger.getLogger(VideoDecryption.class.getName())
				.log(Level.SEVERE, "Unable to decrypt", ex);
		}

		LOG.info("Decryption complete, open " + decryptedPath);
		
		return decryptedPath.toString();
	}
}
