package projectEvolutionPackage;

public class Plant extends AbstractMapElement{
    public final int nutritionalValues;

    @Override
    public String toString(){return "*";}

    public Plant(AbstractMap map, Vector2d position, int nutritionalValues) {
        super(map, position);
        this.nutritionalValues = nutritionalValues;
    }
}
