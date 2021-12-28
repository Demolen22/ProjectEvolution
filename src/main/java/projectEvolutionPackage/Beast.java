package projectEvolutionPackage;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.*;

public class Beast extends AbstractMapElement{
    private ArrayList<IMouseEventObserver> mouseEventObservers;
    private HashSet<Beast> offsprings;
    public final int startEnergy;
    private int energy;
    private final int moveEnergy;
    private MapDirection direction;
    private final LinkedList<Integer> genome;
    private final int birthDay;
    private int deathDay;
    private int childrenNumber;
    private int childrenSinceFollowed;
    private boolean followed;
    private Beast ancestor;
    private boolean hasDominantGenome;

    public Beast(AbstractMap map, Vector2d position, int startEnergy, int energy, int moveEnergy, LinkedList<Integer> genome) {
        super(map, position);
        this.mouseEventObservers = new ArrayList<>();
        this.energy = energy;
        this.moveEnergy = moveEnergy;
        this.startEnergy = startEnergy;
        this.genome = genome;
        this.birthDay =  map.statistics.getDaysCounter();
        this.direction = MapDirection.values()[new Random().nextInt(8)];
        this.childrenNumber = 0;
        this.followed = false;
        this.ancestor = null;
        this.hasDominantGenome = false;
        this.vBox.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                beastClicked();
            }
        });
    }

    protected void addMouseEventObserver(IMouseEventObserver observer){
        mouseEventObservers.add(observer);
    }

    protected void removeMouseEventObserver(IMouseEventObserver observer){
        mouseEventObservers.remove(observer);
    }

    protected void beastClicked(){
        for (IMouseEventObserver observer : mouseEventObservers)
            observer.beastClicked(this);
    }

    public boolean doesHaveDominantGenome(){
        return hasDominantGenome;
    }

    public void setHasDominantGenome(boolean hasDominantGenome){
        this.hasDominantGenome = hasDominantGenome;
    }

    public int getBirthDay() { return birthDay; }

    public int getDeathDay() { return deathDay; }

    public int getEnergy(){
        return energy;
    }

    public LinkedList<Integer> getGenome() { return new LinkedList<>(genome); }

    public MapDirection getDirection(){
        return direction;
    }

    public int getChildrenNumber() { return childrenNumber; }

    public boolean isDead(){
        return energy <= 0;
    }

    public void eat(int nutritionalValues){
        if (!isDead()){
            energy += nutritionalValues;
            map.statistics.totalEnergy += nutritionalValues;
        }
    }

    private int earthFunction(int z, int border){
        if (0 <= z && z < border) return z;
        else if (z < 0) return border-1;
        else return 0;
    }

    public void move(MoveDirection direction){
        energy -= moveEnergy;
        map.statistics.totalEnergy -= moveEnergy;
        if (!isDead()) {
            switch (direction) {
                case FORWARD:
                    if (map.canMoveTo(position.add(this.direction.toUnitVector()))) {
                        Vector2d oldPosition = getPosition();
                        position = position.add(this.direction.toUnitVector());
                        position = new Vector2d(earthFunction(position.x, map.width), earthFunction(position.y, map.height));
                        this.positionChanged(this, oldPosition);
                    }
                    break;
                case BACKWARD:
                    if (map.canMoveTo(position.subtract(this.direction.toUnitVector()))) {
                        Vector2d oldPosition = getPosition();
                        position = position.subtract(this.direction.toUnitVector());
                        position = new Vector2d(earthFunction(position.x, map.width), earthFunction(position.y, map.height));
                        this.positionChanged(this, oldPosition);
                    }
                    break;
                case RIGHT45:
                    this.direction = this.direction.next();
                    this.positionChanged(this, getPosition());
                    break;
                case RIGHT90:
                    this.direction = this.direction.next().next();
                    this.positionChanged(this, getPosition());
                    break;
                case RIGHT135:
                    this.direction = this.direction.next().next().next();
                    this.positionChanged(this, getPosition());
                    break;
                case LEFT45:
                    this.direction = this.direction.previous();
                    this.positionChanged(this, getPosition());
                    break;
                case LEFT90:
                    this.direction = this.direction.previous().previous();
                    this.positionChanged(this, getPosition());
                    break;
                case LEFT135:
                    this.direction = this.direction.previous().previous().previous();
                    this.positionChanged(this, getPosition());
                    break;
            }
        }
        else {
            map.deathNote.add(this);
            deathDay = map.statistics.getDaysCounter();
            map.statistics.incrementTotalDead(this);
        }
    }

    public Beast copulateWith(Beast partner){
        Beast strongerPartner = this;
        Beast weakerPartner = partner;
        if (partner.energy > this.energy) {
            strongerPartner = partner;
            weakerPartner = this;
        }
        int splitIndex = 32*strongerPartner.energy/(this.energy + partner.energy);
        LinkedList<Integer> childGenome = new LinkedList<>();
        if (new Random().nextInt(2) == 0) {
            childGenome.addAll(strongerPartner.genome.subList(0, splitIndex));
            childGenome.addAll(weakerPartner.genome.subList(splitIndex, 32));
        } else {
            childGenome.addAll(strongerPartner.genome.subList(32-splitIndex, 32));
            childGenome.addAll(weakerPartner.genome.subList(0, 32-splitIndex));
        }
        Collections.sort(childGenome);
        int energy1 = this.energy/4;
        int energy2 = partner.energy/4;
        this.energy -= energy1;
        partner.energy -= energy2;
        map.statistics.totalEnergy -= energy1+energy2;
        this.childrenNumber++;
        partner.childrenNumber++;
        map.statistics.totalChildrenNumber += 2;
        Beast child = new Beast(map, getPosition(), startEnergy, energy1+energy2, moveEnergy, childGenome);
        if (followed)
            childrenSinceFollowed++;
        if (ancestor != null){
            ancestor.offsprings.add(child);
            child.setAncestor(ancestor);
        }
        return child;
    }

    public void initializeFollow(){
        followed = true;
        offsprings = new HashSet<>();
        childrenSinceFollowed = 0;
        ancestor = this;
    }

    public int getOffspringsNumber(){
        return offsprings.size();
    }

    public int getChildrenSinceFollowed() {
        return childrenSinceFollowed;
    }

    public void setAncestor(Beast ancestor) {
        this.ancestor = ancestor;
    }

    public void setFollowed(boolean followed) { this.followed = followed; }

    @Override
    public boolean equals(Object other){
        if (this == other)
            return true;
        if (!(other instanceof Beast))
            return false;
        Beast that = (Beast) other;
        return map.equals(that.map) && position.equals(that.getPosition()) && this.direction.equals(that.direction) &&
                this.energy == that.energy && this.genome.equals(that.genome);
    }

    @Override
    public String toString(){ return direction.toString(); }
}
