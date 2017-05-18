package start;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../resources/gui/mainWindow.fxml"));
        primaryStage.setTitle("Вакансии екатеринбурга");
        primaryStage.setMinWidth(920);
        primaryStage.setMinHeight(808);
        primaryStage.setScene(new Scene(root,808,920));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
