import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class View extends Application {


    @Override
    public void start(Stage stage) {
        WebView webview = new WebView();
        WebEngine engine = webview.getEngine();
        Scene scene = new Scene(webview, 800, 600);
        stage.setScene(scene);
        engine.load("http://stackoverflow.com");
        stage.show();
    }

//    void launch() {
//        Application.launch();
//    }

    void show() {
        (new Thread(Application::launch)).start();
    }
}
