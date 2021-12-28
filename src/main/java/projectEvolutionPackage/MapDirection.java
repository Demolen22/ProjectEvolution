package projectEvolutionPackage;

public enum MapDirection {
    NORTH,
    NEAST,
    EAST,
    SEAST,
    SOUTH,
    SWEST,
    WEST,
    NWEST;

    public String toString(){
        switch(this) {
            case NORTH: return "N";
            case SOUTH: return "S";
            case EAST: return "E";
            case NEAST: return "NE";
            case SEAST: return "SE";
            case SWEST: return "SW";
            case NWEST: return "NW";
            default: return "W";
        }
    }

    public MapDirection previous(){
        switch(this) {
            case NORTH: return NWEST;
            case SOUTH: return SEAST;
            case EAST: return NEAST;
            case NEAST: return NORTH;
            case SEAST: return EAST;
            case SWEST: return SOUTH;
            case NWEST: return WEST;
            default: return SWEST;
        }
    }

    public MapDirection next(){
        switch(this) {
            case NORTH: return NEAST;
            case SOUTH: return SWEST;
            case EAST: return SEAST;
            case NEAST: return EAST;
            case SEAST: return SOUTH;
            case SWEST: return WEST;
            case NWEST: return NORTH;
            default: return NWEST;
        }
    }

    public Vector2d toUnitVector(){
        switch(this) {
            case NORTH: return new Vector2d(0,-1);
            case NEAST: return new Vector2d(1,-1);
            case EAST: return new Vector2d(1,0);
            case SEAST: return new Vector2d(1, 1);
            case SOUTH: return new Vector2d(0,1);
            case SWEST: return new Vector2d(-1, 1);
            case NWEST: return new Vector2d(-1, -1);
            default: return new Vector2d(-1,0);
        }
    }
}


