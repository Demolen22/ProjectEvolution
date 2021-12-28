package projectEvolutionPackage;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMapElement {
    protected Vector2d position;
    protected final AbstractMap map;
    protected final List<IPositionChangeObserver> positionChangeObservers;
    protected final VBox vBox;

    public AbstractMapElement(AbstractMap map, Vector2d position){
        this.map = map;
        this.position = position;
        this.positionChangeObservers = new ArrayList<>();
        this.vBox = new VBox(1);
    }

    public Vector2d getPosition(){ return new Vector2d(position.x, position.y); }

    public boolean isAt(Vector2d position){ return this.position.equals(position); }

    protected void addPositionChangeObserver(IPositionChangeObserver observer){
        positionChangeObservers.add(observer);
    }

    protected void removePositionChangeObserver(IPositionChangeObserver observer){
        positionChangeObservers.remove(observer);
    }

    @Override
    public boolean equals(Object other){
        if (this == other)
            return true;
        if (!(other instanceof AbstractMapElement))
            return false;
        AbstractMapElement that = (AbstractMapElement) other;
        return map.equals(that.map) && position.equals(that.getPosition()) && that.getClass().equals(this.getClass());
    }

//    public void setVBox(VBox vBox) {
//        this.vBox = vBox;
//    }

    public VBox getVBox() {
        return vBox;
    }

    protected void positionChanged(AbstractMapElement mapElement, Vector2d oldPosition){
        for (IPositionChangeObserver observer : positionChangeObservers){observer.positionChanged(mapElement, oldPosition);}
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(this.position.x, this.position.y);
//    }
}
