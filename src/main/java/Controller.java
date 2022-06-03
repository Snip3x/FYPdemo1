import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Controller {
    static boolean checkPassed = true;
    static String taskName = "";
    private Stage stageMain;
    private Scene sceneMain;
    private Parent rootMain;

    public void startExam(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Loading.fxml")));
        Stage stage = new Stage();
        stage.setTitle("Setting Up");
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<ProcessInfo> processesList = JProcesses.getProcessList();
                for (ProcessInfo processInfo : processesList) {
                    if(processInfo.getName().startsWith("Discord")){
                        checkPassed = false;
                        taskName = processInfo.getName();
                        return false;

                    }
                }
                checkPassed = true;
                return true;
            }
        };
        task.setOnSucceeded(e ->{
            stage.hide();
            //Dont forget the Negation here
            if(checkPassed){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Illegal Application Running in the background");
                alert.setContentText("Please close "+taskName+" and start again");
                alert.showAndWait();
            }else {
                try {
                    rootMain = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Exam.fxml")));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                stageMain = (Stage) (((Node)event.getSource()).getScene().getWindow());
                stageMain.setTitle("Exam");
                sceneMain = new Scene(rootMain);
                stageMain.setScene(sceneMain);
                stageMain.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                stageMain.setFullScreen(true);
                stageMain.show();
            }
        });

        new Thread(task).start();
    }
}