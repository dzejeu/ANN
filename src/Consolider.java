import java.io.*;
import java.util.Arrays;

/**
 * Created by swat on 06.08.16.
 */
public class Consolider {
    static File inDir,outDir,statsDir;
    static File[] in,out,stats;
    private static BufferedReader inRead, outRead,statsReader;
    private static BufferedWriter inWriter,outWriter,statsWriter;
    public static void main(String[] args){
        inDir = new File("in");
        in = inDir.listFiles();
        Arrays.sort(in);
        outDir = new File("out");
        out = outDir.listFiles();
        Arrays.sort(out);
        statsDir = new File("stats");
        stats = statsDir.listFiles();
        Arrays.sort(stats);
        try {
            inWriter = new BufferedWriter(new FileWriter("merged_in.txt"));
            outWriter = new BufferedWriter(new FileWriter("merged_out.txt"));
            statsWriter = new BufferedWriter(new FileWriter("merged_stats.txt"));
            for(int n=0;n<in.length;n++){
                inRead = new BufferedReader(new FileReader(in[n]));
                outRead = new BufferedReader(new FileReader(out[n]));
                statsReader = new BufferedReader(new FileReader(stats[n]));
                String line="";
                while(line!=null){
                    line = inRead.readLine();
                    if(line!=null) inWriter.write(line+"\n");
                }
                line="";
                while(line!=null){
                    line=outRead.readLine();
                    if(line!=null) outWriter.write(line+"\n");
                }
                line="";
                while(line!=null){
                    line=statsReader.readLine();
                    if(line!=null) statsWriter.write(line+"\n");
                }
            }
            inWriter.close();
            outWriter.close();
            statsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
