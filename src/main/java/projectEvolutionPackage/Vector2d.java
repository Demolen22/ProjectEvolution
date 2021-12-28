package projectEvolutionPackage;


import java.util.Objects;

public class Vector2d {
    public final int x;
    public final int y;

    public Vector2d(int x, int y){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return "(" + Integer.toString(x) + "," + Integer.toString(y) + ")";
    }

    public boolean precedes(Vector2d other){
        return other.x == this.x && other.y == this.y;
    }

    public boolean follows(Vector2d other){
        return this.x >= other.x && this.y >= other.y;
    }

    public Vector2d add(Vector2d other){
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d subtract(Vector2d other){
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public boolean equals(Object other){
        if (this == other)
            return true;
        if (!(other instanceof Vector2d))
            return false;
        Vector2d that = (Vector2d) other;
        return precedes(that);
    }

    public Vector2d opposite(){
        return new Vector2d(-x, -y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
