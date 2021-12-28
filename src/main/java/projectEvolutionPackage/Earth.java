package projectEvolutionPackage;

public class Earth extends AbstractMap{
    public Earth(int width, int height, float jungleRatio){ super(width, height, jungleRatio); }

    @Override
    public boolean canMoveTo(Vector2d position) {
        return true;
    }
}
