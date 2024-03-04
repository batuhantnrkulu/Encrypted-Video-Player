package videoplayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.crypto.NoSuchPaddingException;

import crypto.Util;
import crypto.VideoDecryption;
import crypto.VideoEncryption;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class VideoPlayerController 
{
	@FXML
	private MediaView mediaView;

	@FXML
	private Button playPauseButton;
	
	@FXML
	private Button singleFileChooserButton;
	
	@FXML
	private Label singleFileLabel;
	
	@FXML
	private Button encryptFileButton;
	
	@FXML
	private AnchorPane progressPane;

	private MediaPlayer mediaPlayer;
	private boolean playing = false;
	private List<String> allowedFileExtensions;
	
	private static List<String> decryptedFilesList;

	static String tempFilePath = "";
	
	public static List<String> getDecryptedFilesList()
	{
		return decryptedFilesList;
	}
	
	public void closeMediaPlayer()
	{
		try
		{
			singleFileLabel.setText("");
			mediaView.setMediaPlayer(null);
			mediaPlayer.dispose();
		}
		catch (Exception ex)
		{
			System.out.println("Media settings reset.");
		}
	}
	
	public void initialize() 
	{
		decryptedFilesList = new ArrayList<>();
		
		allowedFileExtensions = new ArrayList<>();
		allowedFileExtensions.add("*.mp4");
		allowedFileExtensions.add("*.encryptedmp4");
		
		progressPane.setVisible(false);
		
		// bind the MediaView's width/height to the scene's width/height
		DoubleProperty width = mediaView.fitWidthProperty();
		DoubleProperty height = mediaView.fitHeightProperty();
		width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
		height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
	}
	
	public void changeMedia(String path)
	{
		// removes 'Selected File' from path
		String filePath = Util.getFilePath(singleFileLabel.getText());

		try
		{
			// get URL of the video file
			URL url = new File(filePath).toURI().toURL();

			// create a Media object for the specified URL
			Media media = new Media(url.toExternalForm());

			// create a MediaPlayer to control Media playback
			mediaPlayer = new MediaPlayer(media);

			// specify which MediaPlayer to display in the MediaView
			mediaView.setMediaPlayer(mediaPlayer);
			
			// set handler to be called when the video completes playing
			mediaPlayer.setOnEndOfMedia(() -> {
				playing = false;
				playPauseButton.setText("Play");
				mediaPlayer.seek(Duration.ZERO);
				mediaPlayer.pause();
			});

			// set handler that displays an ExceptionDialog if an error occurs
			mediaPlayer.setOnError(() -> 
			{
				try
				{
					decryptVideo(filePath);
				}
				catch (Exception ex)
				{
					Alert alert = new Alert(AlertType.ERROR, mediaPlayer.getError().toString());
					alert.showAndWait();
				}
			});
			
			tempFilePath = singleFileLabel.getText();
		}
		catch (MediaException ex)
		{
			if (Util.getExtensionByStringHandling(filePath).get().equals("mp4")) 
			{
				decryptVideo(filePath);
			}
			else
			{
				ex.printStackTrace();
			}
		}
		catch (Exception ex)
		{
			Alert alert = new Alert(AlertType.ERROR, ex.getMessage());
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.showAndWait();
		}
		
		if (path != null && !path.isBlank() && !decryptedFilesList.contains(path))
		{
			decryptedFilesList.add(path);
			System.out.println(decryptedFilesList);
		}
	}
	
	public void decryptVideo(String filePath)
	{	
		Task<Void> task = new Task<Void>() 
		{
			String str = "";
			
			@Override
			protected Void call() throws Exception 
			{
				mediaView.setMediaPlayer(null);
				progressPane.setVisible(true);
				
				String decryptedPath = VideoDecryption.decyrptVideo(filePath);
				str = decryptedPath;
				Thread.sleep(300);
				
				Platform.runLater(new Runnable() 
				{
                    @Override
                    public void run() 
                    {
                    	singleFileLabel.setText(decryptedPath);
                    }
                });
				
				return null;
			}
			
			@Override
			protected void succeeded()
			{
				progressPane.setVisible(false);
				changeMedia(str);
			}
		};
		
		Thread thread = new Thread(task);
		thread.start();
		
	}
	
	public void deletePreviousVideo()
	{
		for (String path : getDecryptedFilesList())
		{
			try 
			{
				File f = new File(path); // file to be delete
				
				if (f.delete()) // returns Boolean value
				{
					System.out.println(f.getName() + " deleted"); // getting and printing the file name
				} 
				else 
				{
					System.out.println("failed");
				}
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		}
	}

	// toggle media playback and the text on the playPauseButton
	@FXML
	private void playPauseButtonPressed(ActionEvent e) 
	{
		if (mediaPlayer.getMedia() == null && 
				(singleFileLabel.getText() == null || singleFileLabel.getText().isBlank()))
		{
			Alert alert = new Alert(AlertType.ERROR, "The video is not selected");
			alert.showAndWait();
			return;
		}
		else if ((mediaPlayer.getMedia() == null && 
				(singleFileLabel.getText() != null && !singleFileLabel.getText().isBlank())) ||
				((mediaPlayer.getMedia() != null && 
				(singleFileLabel.getText() != null && !singleFileLabel.getText().isBlank() && !singleFileLabel.getText().equals(tempFilePath)))))
		{
			changeMedia(null);
		}
		
		playing = !playing;

		if (playing) 
		{	
			playPauseButton.setText("Pause");
			mediaPlayer.play();
		} 
		else 
		{
			playPauseButton.setText("Play");
			mediaPlayer.pause();
		}
	}
	
	// choose file from directory
	@FXML
	private void singleFileChooserButtonPressed(ActionEvent e) throws InvalidKeyException, InvalidAlgorithmParameterException 
	{
		deletePreviousVideo();
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Video Files", allowedFileExtensions));
		
		File file = fileChooser.showOpenDialog(null);
		
		if (file != null)
		{
			singleFileLabel.setText("Selected file: " + file.getAbsolutePath());
		}
		
		try
		{
			if (Util.getExtensionByStringHandling(file.getAbsolutePath()).get().equals("encryptedmp4"))
			{
				try 
				{
					decryptVideo(file.getAbsolutePath());
					return;
				} 
				catch (Exception ex) 
				{
					Alert alert = new Alert(AlertType.ERROR, "The error is occurred while decrypting the video file");
					alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
					alert.showAndWait();
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("Video decrypted");
		}
		
		
		changeMedia(null);
	}
	
	// encrypt file and save from given file
	@FXML
	private void encryptFileButtonPressed(ActionEvent e) 
	{
		String encryptedVideoPath = "";
		
		try 
		{
			encryptedVideoPath = VideoEncryption.encryptVideo(Util.getFilePath(singleFileLabel.getText()));
		} 
		catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| NoSuchPaddingException | IOException | NoSuchElementException e1) 
		{
			Alert alert = new Alert(AlertType.ERROR, "The error is occurred while encrypting the video");
			alert.showAndWait();
			return;
		}
		
		Alert alert = new Alert(AlertType.CONFIRMATION, "The video successfully encrypted and saved to this location: " + encryptedVideoPath);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}
}
