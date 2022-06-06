import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;


public class ExamController implements Initializable {
    @FXML
    Label questionLbl;
    @FXML
    Label optionLbl;
    @FXML
    TextArea answerArea;


    Thread videoMonitor;
    JSONObject exam;
    JSONArray questions;
    int question = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoMonitor  = new Thread(new VideoMonitor());
        videoMonitor.start();
        try {
            exam = new JSONObject(Files.readString(Path.of("exams/"+States.examCode+"/exam.dat")));
            exam.put("email_id",exam.remove("_id"));
            questions = exam.getJSONArray("questionsList");
            JSONObject q = (JSONObject) questions.get(question);
            questionLbl.setText(q.getString("question"));
            if(Boolean.parseBoolean((String) q.get("mcqType"))){
                optionLbl.setText(q.getString("options"));
            }else {
                optionLbl.setText("");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void nextQuestion(ActionEvent actionEvent) {
        question++;
        if(question>=questions.length()){
            question--;
            return;
        }
        JSONObject q = (JSONObject) questions.get(question);
        q.put("answer", answerArea.getText());
        questionLbl.setText(q.getString("question"));
        if(Boolean.parseBoolean((String) q.get("mcqType"))){
            optionLbl.setText(q.get("options").toString());
        }else {
            optionLbl.setText("");
        }
        answerArea.setText("");
    }
    public void prevQuestion(ActionEvent actionEvent) {
        question--;
        if(question<0){
            question++;
            return;}
        JSONObject q = (JSONObject) questions.get(question);
        q.put("answer", answerArea.getText());
        questionLbl.setText(q.getString("question"));
        if(Boolean.parseBoolean((String) q.get("mcqType"))){
            optionLbl.setText(q.get("options").toString());
        }else {
            optionLbl.setText("");
        }
        answerArea.setText("");
    }
    public void submitExam(ActionEvent actionEvent) throws IOException {
        JSONObject q = (JSONObject) questions.get(question);
        q.put("answer", answerArea.getText());
        Files.writeString(Path.of("exams/"+States.examCode+"/submission.dat"),exam.toString());
        videoMonitor.stop();
        System.exit(0);

    }
}
