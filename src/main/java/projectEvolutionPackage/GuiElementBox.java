package projectEvolutionPackage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;

public class GuiElementBox {
//    private final Image image;
    public final ImageView imageView;

    public GuiElementBox(String path, int w, int h) throws java.io.FileNotFoundException{
        Image image = new Image(new FileInputStream(path));
        imageView = new ImageView(image);
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
    }
}
