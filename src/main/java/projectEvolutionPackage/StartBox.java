package projectEvolutionPackage;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class StartBox {
    private VBox mainBox;
    private final TextField widthField;
    private final TextField heightField;
    private final TextField startEnergyField;
    private final TextField moveEnergyField;
    private final TextField plantEnergyField;
    private final TextField beastNumberField;
    private final Slider jungleSlider;
    private final RadioButton magicButton;
    protected int width;
    protected int height;
    protected float jungleRatio;
    protected int plantEnergy;
    protected int startEnergy;
    protected boolean magic;
    protected int moveEnergy;
    protected int beastNumber;
    protected String title;


    public StartBox(String title) {
        mainBox = new VBox(10);
        widthField = new TextField("10");
        heightField = new TextField("8");
        startEnergyField = new TextField("200");
        moveEnergyField = new TextField("5");
        plantEnergyField = new TextField("50");
        beastNumberField = new TextField("20");
        jungleSlider = new Slider(0, 1, 0.5);
        jungleSlider.setShowTickMarks(true);
        jungleSlider.setShowTickLabels(true);
        magicButton = new RadioButton("Magic strategy");
        this.title = title;
        mainBox.getChildren().addAll(
                new Label("~ "+this.title+" ~"),
                new Label("Width: "),
                widthField,
                new Label("Height: "),
                heightField,
                new Label("Jungle ratio: "),
                jungleSlider,
                new Label("Initial beast number: "),
                beastNumberField,
                new Label("Start energy: "),
                startEnergyField,
                new Label("Move energy: "),
                moveEnergyField,
                new Label("Plant energy: "),
                plantEnergyField,
                magicButton
        );
    }

    public VBox getMainBox() {
        return mainBox;
    }

    public void readFields() throws NumberFormatException{
        width = Integer.parseInt(widthField.getText());
        height = Integer.parseInt(heightField.getText());
        jungleRatio = (float) jungleSlider.getValue();
        beastNumber = Integer.parseInt(beastNumberField.getText());
        startEnergy = Integer.parseInt(startEnergyField.getText());
        plantEnergy = Integer.parseInt(plantEnergyField.getText());
        moveEnergy = Integer.parseInt(moveEnergyField.getText());
        magic = magicButton.isSelected();
    }
}
