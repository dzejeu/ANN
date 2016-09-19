package neural;
import java.util.Random;

/**
 * Created by swat on 05.07.16.
 */
public class RandomNumbersGenerator extends Random {
    RandomNumbersGenerator(){super();}
    /**
     * @param rangeUp upper range from which draw occurs
     * @param rangeDown lower range from which draw occurs
     * @return pseudo-random double value from desired range
     * */
    public static double getRandomDouble(double rangeDown, double rangeUp){
        RandomNumbersGenerator rand = new RandomNumbersGenerator();
        return rand.nextDouble()*(rangeUp-rangeDown)+rangeDown;
    }

}
