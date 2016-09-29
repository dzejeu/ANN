import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by swat on 29.07.16.
 */
public class Test {
    public static void main(String args[]){
        File dir = new File("spiny wirtualne/Dzejeu");
        File[] data = dir.listFiles();
        int iteration=0;
        double[] tempStats;
        double[] totalStats = new double[8], totalStageStats = new double[4];
        for(File psHistory: data) {
            try {
                System.out.println(psHistory.getName());
                PSDataConverter converter = new PSDataConverter(psHistory.getPath(), "in/in"+iteration+".txt", "out/out"+iteration+".txt","stats/stats"+iteration+".txt", "Dzejeu");
                converter.Run();
                tempStats=converter.printStats();
                for(int n=0;n<8;n++) totalStats[n]+=tempStats[n];
                tempStats=converter.stageStats;
                for(int n=0;n<totalStageStats.length;n++) totalStageStats[n]+=tempStats[n];
                //data[iteration].renameTo(new File("spiny wirtualne/Dzejeu/"+iteration+".txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            iteration++;
        }
        System.out.println("***************************");
        String[] moves = {"Call","Bet","Raise 3BB","Raise pot","Raise half pot","Fold","Check","All in"};
        double totalMoves=0;
        DecimalFormat df = new DecimalFormat("#.00");
        for(int n=0;n<totalStats.length;n++){
            System.out.println(moves[n]+": "+totalStats[n]);
            totalMoves+=totalStats[n];
        }
        System.out.println("***************************");
        System.out.println("Percentage results:");
        for(int n=0;n<totalStats.length;n++){
            System.out.println(moves[n]+"[%]: "+df.format(totalStats[n]/totalMoves*100));
        }
        System.out.println("***************************");
        System.out.println("Stage's stats:");
        System.out.println("Seen Flop [%]: "+totalStageStats[0]/totalStageStats[3]*100);
        System.out.println("Seen Turn [%]: "+totalStageStats[1]/totalStageStats[3]*100);
        System.out.println("Seen River [%]: "+totalStageStats[2]/totalStageStats[3]*100);
    }
}
