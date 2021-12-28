package projectEvolutionPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Statistics {
    private final AbstractMap map;
    private final HashSet<Beast> allBeastsOfAllTimes;
    private int jOccupied;
    private int sOccupied;
    private int daysCounter;
    private LinkedList<Integer> dominantGenome;
    private int totalDead;
    private long totalLifeTime;
    protected long totalEnergy;
    protected int totalChildrenNumber;
    private File file;
    private PrintWriter writer;
    private int linesNum;
    private long beastNumFileSum;
    private long plantNumFileSum;
    private long avgLifeTimeFileSum;
    private long avgEnergyFileSum;
    private long avgChildrenNumFileSum;


    public Statistics(AbstractMap map){
        this.map = map;
        this.allBeastsOfAllTimes = new HashSet<>();
        this.jOccupied = 0;
        this.sOccupied = 0;
        this.daysCounter = 0;
        this.totalDead = 0;
        this.totalLifeTime = 0;
        this.totalEnergy = 0;
        this.totalChildrenNumber = 0;
        this.dominantGenome = computeDominantGenome();
        this.file = new File("src/main/"+map.getClass().toString().substring(30)+"Statistics.csv");
        try {
            this.writer = new PrintWriter(new FileWriter(map.getClass().toString().substring(30)+"Statistics.csv"));
            writer.println("Day,NumberOfBeasts,NumberOfPlants,AvgEnergy,AvgLifeTime,AvgNumberOfChildren");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        this.linesNum = 0;
        this.beastNumFileSum = 0;
        this.plantNumFileSum = 0;
        this.avgLifeTimeFileSum = 0;
        this.avgChildrenNumFileSum = 0;
        this.avgEnergyFileSum = 0;
    }

    public void writeCurrentStatisticsToFile(){
        int s1 = getDaysCounter();
        int s2 = map.beastsOnMap.size();
        int s3 = map.plantsOnMap.size();
        long s4 = getAverageEnergy();
        long s5 = getAverageLifeTime();
        int s6 = getAverageChildrenNumber();
        writer.println(s1+","+s2+","+s3+","+s4+","+s5+","+s6);
        linesNum++;
        beastNumFileSum += s2;
        plantNumFileSum += s3;
        avgEnergyFileSum += s4;
        avgLifeTimeFileSum += s5;
        avgChildrenNumFileSum += s6;
    }

    public void finaliseFile(){
        if (linesNum == 0) writer.println("AVG:-,0,0,0,0");
        else
            writer.println("AVG:-"
                    +","+((float)beastNumFileSum)/linesNum
                    +","+((float)plantNumFileSum)/linesNum
                    +","+((float)avgEnergyFileSum)/linesNum
                    +","+((float)avgLifeTimeFileSum)/linesNum
                    +","+((float)avgChildrenNumFileSum)/linesNum
            );
        writer.close();
    }

    public LinkedList<Integer> computeDominantGenome(){
        HashMap<LinkedList<Integer>, Integer> counter = new HashMap<>();
        for (Beast beast: map.beastsOnMap){
            if (!beast.isDead()){
                LinkedList<Integer> genome = beast.getGenome();
                if (counter.putIfAbsent(genome, 1)!=null)
                    counter.replace(genome,counter.get(genome)+1);
            }
        }
        LinkedList<Integer> dominantGenome = null;
        for (LinkedList<Integer> genome : counter.keySet()) {
            if (dominantGenome == null || counter.get(genome) > counter.get(dominantGenome))
                dominantGenome = genome;
        }
        return dominantGenome;
    }

    public void incrementDaysCounter(){ daysCounter++; }

    public long getAverageLifeTime(){
        if (totalDead == 0) return 0;
        return totalLifeTime/totalDead;
    }

    public long getAverageEnergy(){
        int alive = map.beastsOnMap.size()-map.deathNote.size();
        if (alive == 0) return 0;
        return totalEnergy/alive;
    }

    public int getAverageChildrenNumber(){
        int alive = map.beastsOnMap.size()-map.deathNote.size();
        if (alive == 0) return 0;
        return totalChildrenNumber /alive;
    }

    public int getDaysCounter(){ return daysCounter; }

    public int getJOccupied() { return jOccupied; }

    public int getSOccupied() {
        return sOccupied;
    }

    public void incrementJOccupied(){
        jOccupied++;
    }

    public void incrementSOccupied(){
        sOccupied++;
    }

    public void incrementTotalDead(Beast dead) {
        totalLifeTime+=dead.getDeathDay()-dead.getBirthDay()+1;
        totalDead++;
        totalEnergy-=dead.getEnergy();
        totalChildrenNumber -=dead.getChildrenNumber();
    }

    public void decrementJOccupied(){
        jOccupied--;
    }

    public void decrementSOccupied(){
        sOccupied--;
    }

    public void addBeast(Beast beast){allBeastsOfAllTimes.add(beast);}
}
