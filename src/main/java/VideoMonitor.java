import com.github.chen0040.objdetect.ObjectDetector;
import com.github.chen0040.objdetect.models.DetectedObj;
import com.github.sarxos.webcam.Webcam;
import com.xuggle.xuggler.video.ConverterFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VideoMonitor implements Runnable{
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (HH-mm-ss)");
    @Override
    public void run() {
        System.out.println("start");
        ObjectDetector detector = new ObjectDetector();
        try {
            detector.loadModel();
        } catch (Exception ignored) {

        }
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        System.out.println("ssss");

        while (true) {
            new File("data").mkdirs();
            try{
                BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
                List<DetectedObj> result = detector.detectObjects(image);

                System.out.println("There are " + result.size() + " objects detected");
                for(int i=0; i < result.size(); ++i){
                    System.out.println("# " + (i + 1) + ": " + result.get(i));
                    if(result.get(i).getLabel().equals("cell phone")){
                        ImageIO.write(image,"jpg",new File("images/Cell Phone- "+dtf.format(LocalDateTime.now())+".jpg"));
                    }
                }
                Thread.sleep(2000);
            }catch (RuntimeException | IOException | InterruptedException ignored){

            }

        }
    }
}
