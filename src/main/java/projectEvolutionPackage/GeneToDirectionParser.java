package projectEvolutionPackage;

public class GeneToDirectionParser {
    public MoveDirection parse(Integer gene) throws IllegalArgumentException{
        switch (gene){
            case 0: return MoveDirection.FORWARD;
            case 1: return MoveDirection.RIGHT45;
            case 2: return MoveDirection.RIGHT90;
            case 3: return MoveDirection.RIGHT135;
            case 4: return MoveDirection.BACKWARD;
            case 5: return MoveDirection.LEFT135;
            case 6: return MoveDirection.LEFT90;
            case 7: return MoveDirection.LEFT45;
            default: throw new IllegalArgumentException("Illegal gene value");
        }
    }
}
