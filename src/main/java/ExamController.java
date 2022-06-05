import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;


public class ExamController implements Initializable {
    @FXML
    Label questionLbl;
    @FXML
    Label optionLbl;
    @FXML
    TextArea answerArea;



    JSONObject exam;
    JSONArray questions;
    int question = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(new VideoMonitor()).start();
    }
}
