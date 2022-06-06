import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestPost {
    public static void main(String[] args) {

        try {
            String result = sendPOST("http://localhost:3001/submission/add");
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sendPOST(String url) throws IOException {

        String result = "";
        HttpPost post = new HttpPost(url);
        post.addHeader("content-type", "application/x-www-form-urlencoded");
        String json = Files.readString(Path.of("exams/629add4463f3a89f13017668/submission.dat"));
        JSONObject js = new JSONObject(json);
        System.out.println(js);
        StringEntity data = new StringEntity(js.toString());
        System.out.println(js);
        // send a JSON data
        post.setEntity(data);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            result = EntityUtils.toString(response.getEntity());
        }

        return result;
    }
}
