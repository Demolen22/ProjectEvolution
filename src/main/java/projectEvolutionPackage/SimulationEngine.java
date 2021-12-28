package projectEvolutionPackage;

import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.util.*;

public class SimulationEngine implements Runnable, IPositionChangeObserver, IMouseEventObserver{
    private final AbstractMap map;
    private final boolean magical;
    private final int nutritionalValues;
    private final App app;
    private MapBox mapBox;
    private boolean stop;
    private int continueFrom;
    private boolean refreshFinishedGuard;
    private boolean updatingFinishedGuard;
    private Beast chosenBeast;
    private Beast newChosenBeast;
    private boolean followInProgress;
    private boolean isBeastClicked;
    private boolean isFollowClicked;
    private boolean isStopFollowClicked;
    private boolean isShowClicked;
    private final int startEnergy;
    private final int moveEnergy;
    private int magicsLeft;

    public SimulationEngine(AbstractMap map, boolean magical, int nutritionalValues, int startEnergy, int moveEnergy, App app){
        this.map = map;
        this.magical = magical;
        this.nutritionalValues = nutritionalValues;
        this.app = app;
        this.stop = false;
        this.continueFrom = 0;
        this.refreshFinishedGuard = true;
        this.updatingFinishedGuard = true;
        this.chosenBeast = null;
        this.followInProgress = false;
        this.isBeastClicked = false;
        this.newChosenBeast = null;
        this.isFollowClicked = false;
        this.isStopFollowClicked = false;
        this.isShowClicked = false;
        this.startEnergy = startEnergy;
        this.moveEnergy = moveEnergy;
        this.magicsLeft = 3;
    }

    private synchronized void performDayActions(int from){
        if (!stop && from <= 0){
            removeDead();
            if (magical) {
                if (map.beastsOnMap.size() == 5 && magicsLeft > 0){
                    createAndPlaceBeasts(5, false, createGenomeslist(5, false));
                    magicsLeft--;}
                Platform.runLater(()->mapBox.magicLabel.setText("MAGIC STRATEGY left " + magicsLeft));
            }
            else Platform.runLater(()->mapBox.magicLabel.setText("NORMAL STRATEGY"));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            continueFrom++; }
        if (!stop && from <= 1){
            moveBeasts();
            continueFrom++; }
        if (!stop && from <= 2){
            feedBeasts();
            continueFrom++; }
        if (!stop && from <= 3) {
            copulateBeasts();
            continueFrom++; }
        if (!stop && from <= 4) {
            addNewPlants();
            continueFrom = 0;
            map.statistics.incrementDaysCounter();
        }
    }

    public ArrayList<Beast> getLeaders(HashSet<Beast> beastsAt, Beast ignored, boolean ignoreDead){
        if (beastsAt == null) return null;
        ArrayList<Beast> leaders = new ArrayList<>();
        for (Beast beast : beastsAt) {
            if(beast.equals(ignored)) continue;
            if (ignoreDead && beast.isDead()) continue;
            if (leaders.isEmpty() || leaders.get(0).getEnergy() == beast.getEnergy())
                leaders.add(beast);
            else if (leaders.get(0).getEnergy() < beast.getEnergy()) {
                leaders.clear();
                leaders.add(beast);
            }
        }
        return leaders;
    }

    private void removeDead(){
        for (Beast dead : map.deathNote) {
            map.removeFromMap(dead, dead.getPosition());
            dead.removePositionChangeObserver(this);
            dead.removeMouseEventObserver(this);
            appearanceChanged(dead, false);
        }
        map.deathNote.clear();
    }

    private void moveBeasts(){;
        for (Beast beast: map.beastsOnMap)
                beast.move(new GeneToDirectionParser().parse(beast.getGenome().get(new Random().nextInt(32))));
    }

    private void feedBeasts(){
        HashSet<Plant> plantsToRemove = new HashSet<>();
        for (Plant plant: map.plantsOnMap) {
            HashSet<Beast> beastsAt = map.beastsAt(plant.getPosition());
            if (beastsAt != null){
                ArrayList<Beast> leaders = getLeaders(beastsAt, null, true);
                if (leaders != null){
                    for (Beast beast: leaders)
                        beast.eat(plant.nutritionalValues/leaders.size());
                    plantsToRemove.add(plant);
                }
            }
        }
        for(Plant plant:plantsToRemove) {
            map.removeFromMap(plant, plant.getPosition());
            appearanceChanged(plant, false);
        }
    }

    private void copulateBeasts(){
        for (HashSet<Beast> beastsAt: map.beastsAtPosition.values()) {
            if (beastsAt.size() <= 1) continue;
            ArrayList<Beast> strongestLeaders = getLeaders(beastsAt, null, true);
            if (strongestLeaders == null || strongestLeaders.isEmpty()) continue;
            Random rand = new Random();
            Beast partner1 = strongestLeaders.get(rand.nextInt(strongestLeaders.size()));
            if (2*partner1.getEnergy() < partner1.startEnergy) continue;
            Beast partner2;
            if (strongestLeaders.size() >= 2) {
                partner2 = strongestLeaders.get(rand.nextInt(strongestLeaders.size()));
                while (partner2 == partner1) {
                    partner2 = strongestLeaders.get(rand.nextInt(strongestLeaders.size()));
                }
            } else {
                ArrayList<Beast> leaders = getLeaders(beastsAt, partner1, true);
                if (leaders == null || leaders.isEmpty()) continue;
                partner2 = leaders.get(rand.nextInt(leaders.size()));
            }
            if (2*partner2.getEnergy() < partner2.startEnergy) continue;
            Beast child = partner1.copulateWith(partner2);
            map.placeOnMap(child);
            child.addPositionChangeObserver(this);
            child.addMouseEventObserver(this);
            appearanceChanged(child, true);
        }
    }

    private void addNewPlants(){
        if(map.statistics.getJOccupied() < map.jWidth*map.jHeight)
            addPlant(map.jungleUpperLeft.x,map.jWidth,map.jungleUpperLeft.y,map.jHeight, false);
        if(map.statistics.getSOccupied() < map.width*map.height-map.jWidth*map.jHeight)
            addPlant(0,map.width,0,map.height, true);
    }

    private void addPlant(int fromX, int toX, int fromY, int toY, boolean jungleCondition){
        Random rand = new Random();
        Vector2d position = new Vector2d(fromX+rand.nextInt(toX),
                fromY+rand.nextInt(toY));
        boolean condition = false;
        if (jungleCondition)
            condition = map.inJungle(position);
        while (condition || map.isOccupied(position)){
            position = new Vector2d(fromX+rand.nextInt(toX), fromY+rand.nextInt(toY));
            if (jungleCondition)
                condition = map.inJungle(position);}
        Plant plant = new Plant(map, position, nutritionalValues);
        map.placeOnMap(plant);
        appearanceChanged(plant, false);
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void setMapBox(MapBox mapBox){
        this.mapBox = mapBox;
    }

    public void setRefreshFinishedGuard(boolean refreshFinishedGuard) {
        this.refreshFinishedGuard = refreshFinishedGuard;
    }

    public void setUpdatingFinishedGuard(boolean updatingFinishedGuard) {
        this.updatingFinishedGuard = updatingFinishedGuard;
    }

    public LinkedList<Integer> generateRandomGenome(){
        LinkedList<Integer> genome = new LinkedList<>();
        Random rand = new Random();
        for (int i=0;i<32;i++)
            genome.add(rand.nextInt(8));
        Collections.sort(genome);
        return genome;
    }

    public ArrayList<LinkedList<Integer>> createGenomeslist(int n, boolean random){
        ArrayList<LinkedList<Integer>> genomesList = new ArrayList<>();
        if (random) for (int i = 0; i < n; i++) genomesList.add(generateRandomGenome());
        else for (Beast beast : map.beastsOnMap) genomesList.add(beast.getGenome());
        return genomesList;
    }

    public void createAndPlaceBeasts(int n, boolean canPlaceAtOnePosition, ArrayList<LinkedList<Integer>> genomesList) throws IllegalArgumentException{
        if (genomesList.size() != n) throw new IllegalArgumentException("invalid genomes list");
        if (!canPlaceAtOnePosition && n>map.width*map.height-map.beastsOnMap.size()) throw new IllegalArgumentException("more beasts than place");
        Random rand = new Random();
        int iter = 0;
        for (int i=0;i<n;i++){
            Vector2d position = new Vector2d(rand.nextInt(map.width),rand.nextInt(map.height));
            while (!canPlaceAtOnePosition && map.beastsAt(position) != null && !map.beastsAt(position).isEmpty())
                position = new Vector2d(rand.nextInt(map.width),rand.nextInt(map.height));
            Beast beast = new Beast(map, position, startEnergy, startEnergy, moveEnergy, genomesList.get(iter));
            iter++;
            map.placeOnMap(beast);
            beast.addPositionChangeObserver(this);
            beast.addMouseEventObserver(this);
        }
    }

    public void follow(){
        if (chosenBeast != null) {
            followInProgress = true;
            chosenBeast.initializeFollow();
            appearanceChanged(chosenBeast, false);
        }
    }

    public void stopFollow(){
        followInProgress = false;
        for (Beast beast : map.beastsOnMap) {
            beast.setAncestor(null);
            beast.setFollowed(false);
        }
        if (chosenBeast != null)
            appearanceChanged(chosenBeast, false);
    }

    public void showBeastWithDominantGenome(){
        LinkedList<Integer> dominantGenome = map.statistics.computeDominantGenome();
        for (Beast beast : map.beastsOnMap){
            if (beast.getGenome().equals(dominantGenome)) {
                beast.setHasDominantGenome(true);
                appearanceChanged(beast, false);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        for (Beast beast : map.beastsOnMap){
            if (beast.getGenome().equals(dominantGenome)) {
                beast.setHasDominantGenome(false);
                appearanceChanged(beast, false);
            }
        }
    }


    @Override
    public synchronized void run() {
        while (true) {
            while (!stop) {
                performDayActions(continueFrom);
                updatingFinishedGuard = false;
                Platform.runLater(()->app.updateStatisticsInfo(mapBox));
                try {
                    while (!updatingFinishedGuard) wait();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
            }
            while (stop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
                if (isBeastClicked) {
                    stopFollow();
                    Beast oldChosen = chosenBeast;
                    chosenBeast = newChosenBeast;
                    if (oldChosen != null && map.beastsOnMap.contains(oldChosen))
                        appearanceChanged(oldChosen, false);
                    appearanceChanged(chosenBeast, false);
                    isBeastClicked = false;
                }
                if (isFollowClicked){
                    follow();
                    isFollowClicked = false;
                }
                if (isStopFollowClicked){
                    stopFollow();
                    isStopFollowClicked = false;
                }
                if (isShowClicked){
                    showBeastWithDominantGenome();
                    isShowClicked = false;
                }
            }
        }
    }

    @Override
    public void positionChanged(AbstractMapElement mapElement, Vector2d oldPosition) {
        appearanceChanged(mapElement, true);
    }

    public synchronized void appearanceChanged(AbstractMapElement mapElement, boolean delay) {
        refreshFinishedGuard = false;
        try {
            Platform.runLater(()->{
                try {
                    app.refreshGrid(mapElement, mapBox);
                } catch (FileNotFoundException ex) {
                    System.out.println(ex.getMessage());
                    System.exit(0);
                }
            });
            if (delay)
                Thread.sleep(mapBox.moveDelay);
            while (!refreshFinishedGuard) {wait();}
        }
        catch (InterruptedException ex){
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }

    public Beast getChosenBeast() {
        return chosenBeast;
    }

    public boolean getFollowInProgress(){
        return followInProgress;
    }

    public void followClicked(){
        isFollowClicked = true;
    }

    public void stopFollowClicked(){
        isStopFollowClicked = true;
    }

    public void showClicked(){
        isShowClicked = true;
    }

    @Override
    public void beastClicked(Beast beast) {
        isBeastClicked = true;
        newChosenBeast = beast;
    }
}
