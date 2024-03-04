package videoplayer;

import java.io.File;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class VideoPlayer extends Application 
{
	@Override
	public void start(Stage stage) throws Exception 
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoPlayer.fxml"));
		Parent root = loader.load();
		VideoPlayerController controller = loader.getController();
				
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() 
		{
			@Override
			public void handle(WindowEvent e) 
			{	
				controller.closeMediaPlayer();
				
				for (String path : VideoPlayerController.getDecryptedFilesList())
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
		});

		Scene scene = new Scene(root);
		stage.setTitle("Encrypted Video Player");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) 
	{
		launch(args);
	}
}
