package projectEvolutionPackage;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;

public class MapBox {
    protected AbstractMap map;
    protected Runnable engine;
    private Thread engineThread;
    protected GridPane grid;
    private final VBox mainBox;
    private final VBox controls;
    private final VBox infoBox;
    public int moveDelay;
    private final Slider speedSlider;
    protected LineChart<Number, Number> chart;
    protected NumberAxis xAxis;
    protected NumberAxis yAxis;
    protected Label dominantGenomeLabel;
    protected Label clickedBeastLabel;
    protected Label childrenSinceFollowedLabel;
    protected Label offspringsNumberLabel;
    protected Label dayLabel;
    protected Label deadLabel;
    protected Label followInProgressLabel;
    protected Label magicLabel;
    protected XYChart.Series seriesAverageEnergy;
    protected XYChart.Series seriesAverageLifeTime;
    protected XYChart.Series seriesAverageChildrenNumber;
    protected XYChart.Series seriesBeastsNumber;
    protected XYChart.Series seriesPlantsNumber;

    public MapBox(String title, AbstractMap map, Runnable engine){
        this.map = map;
        this.engine = engine;
        this.moveDelay = 500;
        this.grid = new GridPane();
        this.controls = new VBox(2);
        this.infoBox = new VBox(2);
        this.mainBox = new VBox(10);
        this.xAxis = new NumberAxis(0,100,1);
        this.yAxis = new NumberAxis();
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(1);
        xAxis.setLabel("Days");
        this.chart = new LineChart<Number, Number>(xAxis, yAxis);
        this.seriesBeastsNumber = new XYChart.Series();
        this.seriesPlantsNumber = new XYChart.Series();
        this.seriesAverageEnergy = new XYChart.Series();
        this.seriesAverageLifeTime = new XYChart.Series();
        this.seriesAverageChildrenNumber = new XYChart.Series();
        this.dominantGenomeLabel = new Label("Dominant genome: ");
        this.clickedBeastLabel = new Label("Genome of the chosen beast: ");
        this.offspringsNumberLabel = new Label("Offsprings: ");
        this.childrenSinceFollowedLabel = new Label("Children: ");
        this.followInProgressLabel = new Label("NOT FOLLOWED");
        this.magicLabel = new Label();
        this.dayLabel = new Label("Day: 0");
        this.deadLabel = new Label("Death day: -");
        this.speedSlider = new Slider(5,300,150);

        VBox plot = new VBox(10);
        HBox controlsPanel = new HBox(10);
        mainBox.getChildren().addAll(new Label("~ "+title+" ~"), controlsPanel, grid, plot);

        speedSlider.setMajorTickUnit(5.0);
        speedSlider.setMinorTickCount(5);
        speedSlider.setSnapToTicks(true);
        speedSlider.setShowTickLabels(true);

        createAndSetButtons();

        seriesBeastsNumber.setName("Beast number");
        seriesPlantsNumber.setName("Plants number");
        seriesAverageLifeTime.setName("Avg life time");
        seriesAverageEnergy.setName("Avg energy");
        seriesAverageChildrenNumber.setName("Avg children number");

        chart.getData().addAll(
                seriesBeastsNumber,
                seriesPlantsNumber,
                seriesAverageLifeTime,
                seriesAverageEnergy,
                seriesAverageChildrenNumber
        );
        chart.setLegendVisible(true);

        plot.getChildren().add(chart);
        infoBox.getChildren().addAll(
                followInProgressLabel,
                dominantGenomeLabel,
                clickedBeastLabel,
                offspringsNumberLabel,
                childrenSinceFollowedLabel,
                deadLabel
        );
        controls.getChildren().addAll(speedSlider, dayLabel, magicLabel);
        controlsPanel.getChildren().addAll(controls, infoBox);

        readMoveDelay();
    }

    private void createAndSetButtons(){
        Button startButton = new Button("START");
        Button stopButton = new Button("STOP");
        Button followButton = new Button("FOLLOW");
        Button stopFollowButton = new Button("STOP FOLLOW");
        Button showButton = new Button("DOMINANT GENOME BEASTS");
        Button writeButton = new Button("WRITE CURRENT STATISTICS");
        Button summarizeButton = new Button("SUMMARIZE STATISTICS");
        controls.getChildren().addAll(startButton, stopButton, showButton, writeButton, summarizeButton);
        infoBox.getChildren().addAll(followButton, stopFollowButton);
        startButton.setOnAction(actionEvent ->  {
            try {
                if (engineThread == null || !engineThread.isAlive()) {
                    engineThread = new Thread(engine);
                    engineThread.start();
                }
                ((SimulationEngine) engine).setStop(false);
            }
            catch (IllegalThreadStateException ex){
                System.out.println("simulation is already running");
            }
            catch (IllegalArgumentException ex){
                System.out.println("Invalid arguments");
            }
        });
        stopButton.setOnAction(actionEvent ->  {
            try {
                if (engineThread != null && engineThread.isAlive()){
                    ((SimulationEngine) engine).setStop(true);
                }
            }
            catch (IllegalThreadStateException ex){
                System.out.println("simulation is already running");
            }
            catch (IllegalArgumentException ex) {
                System.out.println("Invalid arguments");
            }
        });
        showButton.setOnAction(actionEvent-> ((SimulationEngine) engine).showClicked());
        followButton.setOnAction(actionEvent -> ((SimulationEngine) engine).followClicked());
        stopFollowButton.setOnAction(actionEvent -> ((SimulationEngine) engine).stopFollowClicked());
        writeButton.setOnAction(actionEvent -> map.statistics.writeCurrentStatisticsToFile());
        summarizeButton.setOnAction(actionEvent -> map.statistics.finaliseFile());
    }

    protected void updateStatisticsInfo(){
        if (map.statistics.getDaysCounter() % 100 == 0) {
            seriesBeastsNumber.getData().clear();
            xAxis.setLowerBound(map.statistics.getDaysCounter());
            xAxis.setUpperBound(map.statistics.getDaysCounter()+100);
        }
        seriesBeastsNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.beastsOnMap.size()));
        seriesPlantsNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.plantsOnMap.size()));
        seriesAverageChildrenNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageChildrenNumber()));
        seriesAverageEnergy.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageEnergy()));
        seriesAverageLifeTime.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageLifeTime()));
        dominantGenomeLabel.setText("Dominant genome: "+map.statistics.computeDominantGenome());
        synchronized(engine) {
            ((SimulationEngine) engine).setUpdatingFinishedGuard(true);
            engine.notifyAll();
        }
    }

    public VBox getMainBox() {
        return mainBox;
    }

    public void readMoveDelay() {
        this.moveDelay = (int)speedSlider.getValue();
    }
}
