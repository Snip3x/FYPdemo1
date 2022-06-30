import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class Controller {
    @FXML
    TextField nameFld;
    @FXML
    TextField emailFld;
    @FXML
    TextField codeFld;

    static boolean checkPassed = true;
    static String taskName = "";
    private Stage stageMain;
    private Scene sceneMain;
    private Parent rootMain;

    private final String[] illegalApp = {"discord" ,"skype" };

    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (HH-mm-ss)");

    public void startExam(ActionEvent event) throws IOException {
        TextInputDialog dialog = new TextInputDialog("");

        dialog.setTitle("Start Exam");
        dialog.setHeaderText("Enter Exam Code:");
        dialog.setContentText("Code:");

        Optional<String> result = dialog.showAndWait();
        States.examCode = "";
        result.ifPresent(name -> {
            States.examCode = name;
        });

        File examPath = new File("exams/"+States.examCode);
        if(!examPath.exists() || States.examCode.equals("")){
            //error here
            return;
        }



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
                    for (String app : illegalApp) {
                        if(processInfo.getName().toLowerCase().startsWith(app)){
                            checkPassed = false;
                            taskName = processInfo.getName();
                            return false;

                        }
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

                File submissionFile = new File("exams/"+States.examCode+"/submission.dat");
                if(submissionFile.exists()){
                    //error here
                    return;
                }else {
                    try {
                        submissionFile.createNewFile();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    new File("exams/"+States.examCode+"/events").mkdirs();
                }

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
                stageMain.focusedProperty().addListener((ov, hidden, shown) -> {
                    if(hidden){
                        Rectangle screenRect = new Rectangle(0, 0, 0, 0);
                        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                            screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
                        }
                        BufferedImage capture = null;
                        try {
                            capture = new Robot().createScreenCapture(screenRect);
                            ImageIO.write(capture,"jpg",new File("exams/"+States.examCode+"/events/LeftWindow- "+dtf.format(LocalDateTime.now()).toString()+".jpg"));
                        } catch (AWTException | IOException ex) {

                        }
                    }
                });
                stageMain.show();

            }
        });

        new Thread(task).start();
    }

    public void loadExam(ActionEvent actionEvent) throws IOException {
        if(nameFld.getText().equals("")
                || emailFld.getText().equals("")
                || codeFld.getText().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Provide all the info");
            String s ="Fill all the fields";
            alert.setContentText(s);
            alert.show();
            return;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String code = codeFld.getText();
        try {

            HttpGet request = new HttpGet("http://127.0.0.1:3001/exam/"+code);

            CloseableHttpResponse response = httpClient.execute(request);

            try {

                if(response.getStatusLine().getStatusCode() != 200){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Server not reachable");
                    String s ="Check your internet connection";
                    alert.setContentText(s);
                    alert.show();
                    return;
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    new File("exams/"+codeFld.getText()).mkdirs();
                    File examFile = new File("exams/"+codeFld.getText()+"/exam.dat");
                    if(examFile.exists()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Exam already Exists");
                        alert.show();
                        return;
                    }
                    examFile.createNewFile();
                    JSONObject data = new JSONObject(result);
                    data.put("studentName", nameFld.getText());
                    data.put("email",emailFld.getText());
                    Files.writeString(examFile.toPath(),data.toString());
                }

            } finally {
                response.close();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Server not reachable");
            String s ="Check your internet connection";
            alert.setContentText(s);
            alert.show();
            return;

        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Exam has been preloaded");
        alert.show();
        httpClient.close();

    }


}