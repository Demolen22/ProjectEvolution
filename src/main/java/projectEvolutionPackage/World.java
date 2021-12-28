package projectEvolutionPackage;

import javafx.application.Application;

public class World {
    public static void main(String[] args){
        try{
            Application.launch(App.class, args);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }
}
