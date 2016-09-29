import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by swat on 16.09.16.
 */
public class Test2 {


    public static void main(String[] args) throws IOException {
        BufferedReader readChance = new BufferedReader( new FileReader("chances.txt"));
        BufferedReader readStats = new BufferedReader(new FileReader("merged_stats.txt"));
        FileWriter writer = new FileWriter("stats.txt");

        String statsLine, chanceLine;

        while (true){
            statsLine = readStats.readLine();
            chanceLine = readChance.readLine();
            if (statsLine!=null){
                statsLine+="\t"+chanceLine;
                writer.write(statsLine+"\n");
            }
            else break;
        }
        writer.close();
    }
}
