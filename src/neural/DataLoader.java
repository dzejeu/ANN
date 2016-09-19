
package neural;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by swat on 14.07.16.
 */
public class DataLoader {
    /**
     * Load data from a file that is built in a proper way that is shown in sample_data.txt
     * @return 2-dimensional array each row [x][row] is single input for neural network
     * */
    public static double[][] loadDataFromFile(String fileDirectory) throws IOException {
        FileReader fileReader = new FileReader(fileDirectory);
        BufferedReader lengthChecker = new BufferedReader(fileReader);
        String line;
        double[][] data;
        int rows=0,total=0;
        while((line = lengthChecker.readLine())!=null){
            if(Objects.equals(line.substring(0, 1), "X")) rows++;
            total++;
        }
        data = new double[rows][(total-rows)/rows];
        lengthChecker.close();
        fileReader = new FileReader(fileDirectory);
        BufferedReader reader = new BufferedReader(fileReader);
        for(int r=0;r<rows;r++){
            for(int c=0;c<(total-rows)/rows;c++){
                line=reader.readLine();
                if(!Objects.equals(line.substring(0,1), "X")) data[r][c]=Double.parseDouble(line.substring(0,line.length()));
            }
            reader.readLine();
        }
        return data;
    }
}
