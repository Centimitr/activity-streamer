import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class View extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();
        webEngine.load("app.html");
        webEngine.getLoadWorker()
                .stateProperty()
                .addListener((ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        stage.setTitle(webEngine.getLocation());
                    }
                });

        Scene scene = new Scene(webView);
        stage.setScene(scene);
        stage.show();
    }

    void launch() {
        Application.launch();
    }
}
