package projectEvolutionPackage;

import javafx.scene.layout.VBox;

import java.util.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMap implements IPositionChangeObserver{
    protected final Map<Vector2d, HashSet<Beast>> beastsAtPosition;
    protected final Map<Vector2d, Plant> plantsAtPosition;
    protected  final HashSet<Plant> plantsOnMap;
    protected  final HashSet<Beast> beastsOnMap;
    protected final HashSet<Beast> deathNote;
    protected final int width;
    protected final int height;
    protected final int jWidth;
    protected final int jHeight;
    protected final Vector2d jungleUpperLeft;
    protected Statistics statistics;

    public AbstractMap(int width, int height, float jungleRatio){
        this.beastsAtPosition = new HashMap<>();
        this.plantsAtPosition = new HashMap<>();
        this.plantsOnMap = new HashSet<>();
        this.beastsOnMap = new HashSet<>();
        this.deathNote = new HashSet<>();
        this.width = width;
        this.height = height;
        this.jWidth = Math.round(width*jungleRatio);
        this.jHeight = Math.round(height*jungleRatio);
        this.jungleUpperLeft = new Vector2d(Math.round(((float)width)/2),Math.round(((float)height)/2)).subtract
                (new Vector2d(Math.round(((float)jWidth)/2),Math.round(((float)jHeight)/2)));
        this.statistics = new Statistics(this);
    }

    abstract public boolean canMoveTo(Vector2d position);

    private void placeAtPosition(AbstractMapElement mapElement) throws IllegalArgumentException {
        Vector2d position = mapElement.getPosition();
        if (inMap(position))
            throw new IllegalArgumentException("Position: "+position.toString()+" is invalid");
        else{
            if (!isOccupied(position)){
                if (inJungle(position))
                    statistics.incrementJOccupied();
                else
                    statistics.incrementSOccupied();
            }
            if (mapElement.getClass().equals(Beast.class)){
                beastsAtPosition.computeIfAbsent(position, k -> new HashSet<>());
                beastsAtPosition.get(position).add((Beast) mapElement);
            } else
                plantsAtPosition.put(position, (Plant) mapElement);
        }
    }

    public void placeOnMap(AbstractMapElement mapElement){
        mapElement.addPositionChangeObserver(this);
        placeAtPosition(mapElement);
        if (mapElement.getClass().equals(Beast.class)) {
            beastsOnMap.add((Beast) mapElement);
            statistics.addBeast((Beast) mapElement);
            statistics.totalEnergy += ((Beast) mapElement).getEnergy();
        } else plantsOnMap.add((Plant) mapElement);
    }

    private void removeFromPosition(AbstractMapElement mapElement, Vector2d oldPosition) throws IllegalArgumentException{
        if(mapElement.getClass().equals(Beast.class)){
            if (beastsAtPosition.get(oldPosition) == null)
                throw new IllegalArgumentException("cannot remove, mapElement not at position");
            else {
                beastsAtPosition.get(oldPosition).remove(mapElement);
                if (beastsAtPosition.get(oldPosition).isEmpty())
                    beastsAtPosition.remove(oldPosition);
            }
        }
        else {
            if (plantsAtPosition.get(oldPosition) == null)
                throw new IllegalArgumentException("cannot remove, mapElement not at position");
            else plantsAtPosition.remove(oldPosition, (Plant) mapElement);
        }
        if (!isOccupied(oldPosition)){
            if (inJungle(oldPosition))
                statistics.decrementJOccupied();
            else
                statistics.decrementSOccupied();
        }
    }

    public void removeFromMap(AbstractMapElement mapElement, Vector2d oldPosition){
        mapElement.removePositionChangeObserver(this);
        removeFromPosition(mapElement, oldPosition);
        if (mapElement.getClass().equals(Beast.class)) {
            beastsOnMap.remove((Beast) mapElement);
        }
        else plantsOnMap.remove((Plant) mapElement);
    }

    public boolean isOccupied(Vector2d position) {
        return (beastsAt(position) != null && !beastsAt(position).isEmpty()) || plantAt(position) != null;
    }

    public HashSet<Beast> beastsAt(Vector2d position) {
        return beastsAtPosition.get(position);
    }

    public Plant plantAt(Vector2d position){
        return plantsAtPosition.get(position);
    }

    public boolean inJungle(Vector2d position){
        return jungleUpperLeft.x <= position.x
                && jungleUpperLeft.y <= position.y
                && position.x <= jungleUpperLeft.x+jWidth-1
                && position.y <= jungleUpperLeft.y+jHeight-1;
    }

    public boolean inMap(Vector2d position){
        return position.x < 0
            || position.x > width-1
            || position.y < 0
            || position.y > height-1;
    }
    @Override
    public void positionChanged(AbstractMapElement mapElement, Vector2d oldPosition) {
        removeFromPosition(mapElement, oldPosition);
        placeAtPosition(mapElement);
    }
}
