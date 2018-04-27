import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class View extends Application {
    @Override
    public void start(Stage stage) {
        WebView webview = new WebView();
        WebEngine engine = webview.getEngine();
        Scene scene = new Scene(webview, 800, 600);
        stage.setScene(scene);
        stage.setMinHeight(720);
        stage.setMinWidth(520);
        stage.setMaxHeight(1280);
        stage.setMaxWidth(640);
        stage.setWidth(480);
        stage.setHeight(640);
        engine.setJavaScriptEnabled(true);
//        engine.load("http://localhost:3000");
        System.out.println(this.getClass().getResource("/Frontend/index.html"));
//        System.out.println(engine);
        engine.load(this.getClass().getResource("/Frontend/index.html").toExternalForm());
        engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        Client.getAgent().bindEngine(engine);
                        JSObject win = (JSObject) engine.executeScript("window");
                        win.setMember("devbycmagent", Client.getAgent());
//                        engine.executeScript("(window['devbycmstream']).setRegistered(true, 'xiaoming2','fds6f78dsa6f78as6f78sa')");
//                        engine.executeScript("(window['devbycmstream']).setLoggedIn(true)");
                    }
                }
        );
        stage.show();

//        String jsonString = "";
//        engine.executeScript(String.format("window['devbycmstream'].addMessage(%s)", jsonString));
    }
}

