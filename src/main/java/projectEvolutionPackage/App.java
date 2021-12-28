package projectEvolutionPackage;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

public class App extends Application {
    protected MapBox mapBox1;
    protected MapBox mapBox2;
    private ScrollPane scrollPane;
    private StartBox startBox1;
    private StartBox startBox2;
    private Label dialogLabel;

    public void renderGrid(MapBox mapBox) throws FileNotFoundException {
        AbstractMap map = mapBox.map;
        Runnable engine = mapBox.engine;
        GridPane grid = mapBox.grid;
        grid.getChildren().clear();
        for(int i=-1;i<map.width;i++) {
            grid.getRowConstraints().add(new RowConstraints(45));
            for (int j=-1;j<map.height; j++) {
                Vector2d position = new Vector2d(i , j);
                VBox vBox = new VBox(1);
                vBox.setAlignment(Pos.CENTER);
                if (i == -1 && j == -1) {
                    vBox.getChildren().add(new Label("y/x"));
                }
                else if (i == -1){
                    vBox.getChildren().add(new Label(Integer.valueOf(j).toString()));
                }
                else if (j == -1) {
                    vBox.getChildren().add(new Label(Integer.valueOf(i).toString()));
                }
                else {
                    if (map.inJungle(position))
                        vBox.getChildren().add(new GuiElementBox("src/main/resources/boardJungle45.png", 45, 45).imageView);
                    else
                        vBox.getChildren().add(new GuiElementBox("src/main/resources/boardSteppe45.png", 45, 45).imageView);
                }
                grid.addColumn(i+1, vBox);
                Plant plant = map.plantAt(position);
                if (plant != null) {
                    plant.getVBox().setAlignment(Pos.CENTER);
                    plant.getVBox().getChildren().add(getDirectionImage(plant.toString()).imageView);
                    grid.add(plant.getVBox(), i+1, j+1);
                }
                HashSet<Beast> beasts = map.beastsAt(position);
                if (beasts != null && !beasts.isEmpty()) {
                    Beast beast = ((SimulationEngine)engine).getLeaders(beasts, null, false).get(0);
                    beast.getVBox().setAlignment(Pos.CENTER);
                    Label labelBeast;
                    if(beast.getEnergy()>0)
                        labelBeast = new Label(beast.toString() + " " + beast.getEnergy());
                    else labelBeast = new Label("*DEAD*");
                    beast.getVBox().getChildren().add(getDirectionImage(beast.toString()).imageView);
                    beast.getVBox().getChildren().add(labelBeast);
                    grid.add(beast.getVBox(), i+1, j+1);
                }
                GridPane.setHalignment(vBox, HPos.CENTER);
            }
            grid.getColumnConstraints().add(new ColumnConstraints(45));
        }
    }

    protected void updateStatisticsInfo(MapBox mapBox){
        AbstractMap map = mapBox.map;
        Runnable engine = mapBox.engine;
        if (map.statistics.getDaysCounter() % 100 == 0) {
            mapBox.seriesBeastsNumber.getData().clear();
            mapBox.xAxis.setLowerBound(map.statistics.getDaysCounter());
            mapBox.xAxis.setUpperBound(map.statistics.getDaysCounter()+100);
        }
        mapBox.seriesBeastsNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.beastsOnMap.size()));
        mapBox.seriesPlantsNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.plantsOnMap.size()));
        mapBox.seriesAverageChildrenNumber.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageChildrenNumber()));
        mapBox.seriesAverageEnergy.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageEnergy()));
        mapBox.seriesAverageLifeTime.getData().add(new XYChart.Data<>(map.statistics.getDaysCounter(), map.statistics.getAverageLifeTime()));
        mapBox.dominantGenomeLabel.setText("Dominant genome: "+map.statistics.computeDominantGenome());
        synchronized(engine) {
            ((SimulationEngine) engine).setUpdatingFinishedGuard(true);
            engine.notifyAll();
        }
    }

    public void refreshGrid(AbstractMapElement mapElement, MapBox mapBox) throws FileNotFoundException {
        AbstractMap map = mapBox.map;
        Runnable engine = mapBox.engine;
        mapBox.dayLabel.setText("Day: "+map.statistics.getDaysCounter());
        Beast chosenBeast = ((SimulationEngine) engine).getChosenBeast();
        boolean followInProgress = ((SimulationEngine) engine).getFollowInProgress();
        if (followInProgress){
            mapBox.followInProgressLabel.setText("FOLLOWED");
            mapBox.childrenSinceFollowedLabel.setText("Children: "+chosenBeast.getChildrenSinceFollowed());
            mapBox.offspringsNumberLabel.setText("Offsprings: "+chosenBeast.getOffspringsNumber());
            if (chosenBeast.isDead())
                mapBox.deadLabel.setText("Death day: "+chosenBeast.getDeathDay());
            else mapBox.deadLabel.setText("Death day: -");
        } else  mapBox.followInProgressLabel.setText("NOT FOLLOWED");
        mapBox.readMoveDelay();
        Vector2d position = mapElement.getPosition();
        mapBox.grid.getChildren().remove(mapElement.getVBox());
        Plant plant = map.plantAt(position);
        if (plant != null) {
            mapBox.grid.getChildren().remove(plant.getVBox());
            plant.getVBox().getChildren().clear();
            plant.getVBox().getChildren().add(getDirectionImage(plant.toString()).imageView);
            plant.getVBox().setAlignment(Pos.CENTER);
            mapBox.grid.add(plant.getVBox(), position.x+1, position.y+1);
        }
        if (map.beastsAt(position) != null){
        HashSet<Beast> beasts = new HashSet<>(map.beastsAt(position));
        if (!beasts.isEmpty()) {
            for (Beast beast : beasts) {
                mapBox.grid.getChildren().remove(beast.getVBox());
            }
            ArrayList<Beast> leaders = ((SimulationEngine) engine).getLeaders(beasts, null, false);
            if (leaders != null) {
                Beast beast = leaders.get(0);
                beast.getVBox().getChildren().clear();
                if (beast == chosenBeast){
                    mapBox.clickedBeastLabel.setText("Genome of the chosen beast: "+chosenBeast.getGenome().toString());
                    beast.getVBox().getChildren().add(new GuiElementBox("src/main/resources/redline.png",30,3).imageView);
                }
                if (beast.doesHaveDominantGenome()){
                    beast.getVBox().getChildren().add(new GuiElementBox("src/main/resources/redline.png",20,3).imageView);
                }
                Label labelBeast;
                if (beast.getEnergy() > 0)
                    labelBeast = new Label(beast.toString() + " " + beast.getEnergy());
                else labelBeast = new Label("*DEAD*");
                beast.getVBox().getChildren().add(getDirectionImage(beast.toString()).imageView);
                beast.getVBox().getChildren().add(labelBeast);
                beast.getVBox().setAlignment(Pos.CENTER);
                mapBox.grid.add(beast.getVBox(), position.x + 1, position.y + 1);
            }
        }
        }
        synchronized (engine) {
            ((SimulationEngine) engine).setRefreshFinishedGuard(true);
            engine.notifyAll();
        }
    }

    protected GuiElementBox getDirectionImage(String s) throws FileNotFoundException{
        switch (s){
            case  "N": return new GuiElementBox("src/main/resources/kapibara_north20.png",20,20);
            case  "S": return new GuiElementBox("src/main/resources/kapibara_south20.png",20,20);
            case  "E": return new GuiElementBox("src/main/resources/kapibara_east20.png",20,20);
            case  "NE": return new GuiElementBox("src/main/resources/kapibara_neast20.png",20,20);
            case  "SE": return new GuiElementBox("src/main/resources/kapibara_seast20.png",20,20);
            case  "SW":return new GuiElementBox("src/main/resources/kapibara_swest20.png",20,20);
            case "W": return new GuiElementBox("src/main/resources/kapibara_west20.png",20,20);
            case  "NW": return new GuiElementBox("src/main/resources/kapibara_nwest20.png",20,20);
            default: return new GuiElementBox("src/main/resources/grass20.png",42,42);
        }
    }

    private void createContent() {
        Button saveAndQuitButton = new Button("SAVE & QUIT");

        AbstractMap map1 = new Earth(startBox1.width, startBox1.height, startBox1.jungleRatio);
        SimulationEngine engine1 = new SimulationEngine(map1, startBox1.magic, startBox1.plantEnergy, startBox1.startEnergy, startBox1.moveEnergy, this);
        mapBox1 = new MapBox(startBox1.title, map1, engine1);
        engine1.setMapBox(mapBox1);
        engine1.createAndPlaceBeasts( startBox1.beastNumber, false, engine1.createGenomeslist(startBox1.beastNumber, true));

        AbstractMap map2 = new Cage(startBox2.width, startBox2.height, startBox2.jungleRatio);
        SimulationEngine engine2 = new SimulationEngine(map2, startBox2.magic, startBox2.plantEnergy, startBox2.startEnergy, startBox2.moveEnergy, this);
        mapBox2 = new MapBox(startBox2.title, map2, engine2);
        engine2.setMapBox(mapBox2);
        engine2.createAndPlaceBeasts(startBox2.beastNumber, false, engine2.createGenomeslist(startBox2.beastNumber, true));

        saveAndQuitButton.setOnAction(actionEvent -> {
            map1.statistics.finaliseFile();
            map2.statistics.finaliseFile();
            System.exit(0);
        });
        VBox vBox = new VBox(10);
        HBox hBox = new HBox(20);
        hBox.getChildren().addAll(mapBox1.getMainBox(), mapBox2.getMainBox());
        vBox.getChildren().addAll(saveAndQuitButton, hBox);
        vBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);
        scrollPane.setContent(vBox);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(scrollPane, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        scrollPane = new ScrollPane();
        scrollPane.pannableProperty().set(true);
        scrollPane.fitToWidthProperty().set(true);
        scrollPane.fitToHeightProperty().set(true);
        dialogLabel = new Label("Enter parameters:");
        Button confirmButton = new Button("CONFIRM");
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        startBox1 = new StartBox("EARTH");
        startBox2 = new StartBox("CAGE");
        hBox.getChildren().addAll(startBox1.getMainBox(), startBox2.getMainBox());
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(dialogLabel, hBox, confirmButton);
        scrollPane.setContent(vBox);
        confirmButton.setOnAction(actionEvent->{
            try {
                startBox1.readFields();
                startBox2.readFields();
                createContent();
                renderGrid(mapBox1);
                mapBox1.readMoveDelay();
                renderGrid(mapBox2);
                mapBox2.readMoveDelay();
            } catch (FileNotFoundException ex){
                dialogLabel.setText("Resources are incomplete");
            } catch (NumberFormatException ex){
                dialogLabel.setText("***Invalid parameter***");
            } catch (IllegalArgumentException ex){
                dialogLabel.setText("***Invalid parameter beast number***");
            }
        });
    }
}
