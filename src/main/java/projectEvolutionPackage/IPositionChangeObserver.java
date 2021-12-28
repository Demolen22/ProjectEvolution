package projectEvolutionPackage;

public interface IPositionChangeObserver {

    void positionChanged(AbstractMapElement mapElement, Vector2d oldPosition);

}
