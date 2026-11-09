import model.Person;
import model.SIRModel;
import model.InfectionState;

class Main {
    public static void main(String[] args) {
        Person[] people = new Person[25];

        int i = 0;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                people[i] = new Person(x, y);
                i++; // Move to the next spot in the array
            }
        }

        for(int z =0;z<25;z++){
            System.out.println(people[z].x+" "+people[z].y);
            System.out.println(people[z].state);
            System.out.println("----------------");
        }

        for(int z = 0; z<25;z++){

        }



    }
}