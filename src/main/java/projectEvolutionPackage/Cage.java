package projectEvolutionPackage;

import java.util.LinkedList;

public class Cage extends AbstractMap{
    public Cage(int width, int height, float jungleRatio){
        super(width, height, jungleRatio);
    }

    @Override
    public boolean canMoveTo(Vector2d position) {
        return position.x >= 0 && position.x <= width-1 && position.y >= 0 && position.y <= height-1;
    }
}
