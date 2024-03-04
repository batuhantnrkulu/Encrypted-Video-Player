module VideoDecrypter {
	requires javafx.controls;
	requires javafx.base;
    requires javafx.media;
    requires javafx.web;
    requires javafx.fxml;
	requires java.logging;
	opens videoplayer to javafx.graphics, javafx.fxml;
}
