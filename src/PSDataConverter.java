import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.*;
import java.util.Objects;

/**
 * Created by swat on 29.07.16.
 */
public class PSDataConverter {
    /**
     * writeInputData - writes to file data that is used as an input for neural net
     * writeOutputData - writes to file desired output for neural net
     * */
    BufferedReader readData;
    BufferedWriter writeInputData;
    BufferedWriter writeOutputData;
    BufferedWriter writeStats;
    String botName;
    private double[] movesStats = new double[8];
    public double[] stageStats = new double[4];
    /**
     * @param botName name of a player whose moves data we want to convert
     * */
    PSDataConverter(String readFilePath, String writeInputFilePath,String writeOutputFilePath,String writeStatsFilePath,String botName) throws IOException {
        readData = new BufferedReader(new FileReader(readFilePath));
        writeInputData = new BufferedWriter(new FileWriter(writeInputFilePath));
        writeOutputData = new BufferedWriter(new FileWriter(writeOutputFilePath));
        writeStats = new BufferedWriter(new FileWriter(writeStatsFilePath));
        this.botName=botName;
        for(int move=0; move<pokerMoves.length;move++){
            for(int n=0;n<pokerMoves[move].length;n++) {
                if (n == move) pokerMoves[move][n] = 1;
                else pokerMoves[move][n]=0;
            }
        }
        for(int card=0;card<cards.length;card++){
            for(int n=0;n<cards[card].length;n++) cards[card][n]=0;
        }
        for(int n=0;n<movesStats.length;n++) movesStats[n]=0;
    }

    private boolean endOfTournament = false;

    public double[] printStats(){
        String[] moves = {"Call","Bet","Raise 3BB","Raise pot","Raise half pot","Fold","Check","All in"};
        for(int n=0;n<moves.length;n++){
            System.out.println(moves[n]+": "+movesStats[n]);
        }
        return movesStats;
    }

    private boolean GetBlind() throws IOException {
        String line;
        int slashPosition=0, blindLength=0;
        /**
         * null pod koniec !!
         * */
        while((line=readData.readLine())!=null ) {
            if (line.contains("Rozdanie")) break;
            if (line.contains(botName + " zakończył turniej") || line.contains(botName + " wygrał turniej"))
                    return false;
        }
        if(line==null) return false;
        for (int sign=20;sign<line.length();sign++){
                    if(line.charAt(sign)=='/' && slashPosition==0) slashPosition=sign;
                    if(line.charAt(sign)==')') blindLength=sign;
            }
        bigBlind = Double.parseDouble(line.substring(slashPosition+1,blindLength));
        pot+=bigBlind*1.5;
        return true;
    }

    /**
     * Notation: 1- big blind, 2- small blind, 3- dealer
     * */
    private void getMyPositionAndStack() throws IOException {
        String line = readData.readLine();
        int dealer=-1;
        for(int sign=0; sign<line.length();sign++){
            if(line.charAt(sign)=='M') dealer = Integer.parseInt(line.substring(sign+11,sign+12));
        }
        while(!(line=readData.readLine()).contains(botName)){
            nrOfPlayers++;
        }
        while (!readData.readLine().contains("KARTY WŁASNE")) nrOfPlayers++;
        nrOfPlayers-=1;
        if(nrOfPlayers>3) nrOfPlayers=3;
        int myPosition = Integer.parseInt(line.substring(8,9));
        // my position
        if(myPosition==dealer && nrOfPlayers==3){
            this.myPosition=3;
        }
        else if(myPosition==(dealer+1)%3 && nrOfPlayers==3){
            this.myPosition=2;
        }
        else if(nrOfPlayers==3){
            this.myPosition=1;
        }
        else if(myPosition==dealer){
            this.myPosition=2;
        }
        else{
            this.myPosition=1;
        }
        if(!line.contains("nie gra"))    myStack=Double.parseDouble(line.substring(11+botName.length()+2,line.length()-10));
        else myStack=Double.parseDouble(line.substring(11+botName.length()+2,line.length()-17));
        if(this.myPosition == 1) myStack-=bigBlind;
        else if(this.myPosition == 2) myStack-=bigBlind/2;
        stage=1;
    }

    private int convertCard(String card){
        switch (card){
            case "T":
                return 10;
            case "J":
                return 11;
            case "Q":
                return 12;
            case "K":
                return 13;
            case "A":
                return 14;
            default:
                return Integer.parseInt(card);
        }
    }

    private int convertColor(String color){
        switch (color){
            case "d":
                return 1;
            case "h":
                return 2;
            case "s":
                return 3;
            case "c":
                return 4;
        }
        return 0;
    }

    private void getMyCards() throws IOException {
        String line = readData.readLine();
        cardsPokerFormat[0] = line.substring(line.length()-6,line.length()-5)+line.substring(line.length()-5,line.length()-4);
        cards[0][0]=convertCard(line.substring(line.length()-6,line.length()-5));
        cards[0][1]=convertColor(line.substring(line.length()-5,line.length()-4));
        cardsPokerFormat[1]=line.substring(line.length()-3,line.length()-2)+line.substring(line.length()-2,line.length()-1);
        cards[1][0]=convertCard(line.substring(line.length()-3,line.length()-2));
        cards[1][1]=convertColor(line.substring(line.length()-2,line.length()-1));
        for(int n=2;n<7;n++){
            cardsPokerFormat[n]="0";
            cards[n][0]=0;
            cards[n][1]=0;
        }
    }


    private void getFlopCards(String line) throws IOException {
        int arrayPos=2;
        for (int card=0;card<7;card+=3) {
            cardsPokerFormat[arrayPos] = line.substring(line.length() - 9+card, line.length() - 8+card)+line.substring(line.length() - 8+card, line.length() - 7+card);
            cards[arrayPos][0] = convertCard(line.substring(line.length() - 9+card, line.length() - 8+card));
            cards[arrayPos][1] = convertColor(line.substring(line.length() - 8+card, line.length() - 7+card));
            arrayPos++;
        }
    }

    private void getTurnCard(String line) throws IOException {
        cardsPokerFormat[5]=line.substring(line.length()-3,line.length()-2)+line.substring(line.length()-2,line.length()-1);
        cards[5][0]=convertCard(line.substring(line.length()-3,line.length()-2));
        cards[5][1]=convertColor(line.substring(line.length()-2,line.length()-1));
    }

    private void getRiverCard(String line) throws IOException {
        cardsPokerFormat[6]=line.substring(line.length()-3,line.length()-2)+line.substring(line.length()-2,line.length()-1);
        cards[6][0]=convertCard(line.substring(line.length()-3,line.length()-2));
        cards[6][1]=convertColor(line.substring(line.length()-2,line.length()-1));
    }

    private String getPotAndBotMove() throws IOException {
        double raise=0,potWhileMoving=0;
        int doubledotPlacement=0;
        String line,lineWithMove=null;
        while(!(line=readData.readLine()).contains("***")){
            if(line.contains(botName)){
                potWhileMoving=pot;
                lineWithMove=line;
            }
            else{
                for (int sign=0;sign<line.length();sign++) if(line.charAt(sign)==':') doubledotPlacement=sign;
                if(line.contains("sprawdza")){
                    if(!line.contains("all-in"))
                    pot+=Double.parseDouble(line.substring(doubledotPlacement+
                            11,line.length()));
                    else pot+=Double.parseDouble(line.substring(doubledotPlacement+
                            11,line.length()-17));
                }
                else if(line.contains("stawia")){
                    if(!line.contains("all-in")){
                        raise=Double.parseDouble(line.substring(doubledotPlacement+
                                9,line.length()));
                        this.raise=raise;
                    }
                    else{
                        raise=Double.parseDouble(line.substring(doubledotPlacement+
                                9,line.length()-18));
                        this.raise=raise;
                    }
                }
                else if(line.contains("przebija")){
                    for (int sign=doubledotPlacement;sign<line.length();sign++){
                        if(line.charAt(sign)=='d'){
                            raise=Double.parseDouble(line.substring(doubledotPlacement+11,sign-1));
                            this.raise=raise;
                            break;
                        }
                    }
                }
                else if(line.contains("pasuje")) nrOfPlayers--;
                pot+=raise;
                raise=0;
            }
            if(lineWithMove!=null && GetMove(lineWithMove, potWhileMoving, this.raise,0)) {
                writeInputData.write(String.valueOf(myPosition)+"\n");
                writeInputData.write(String.valueOf(myStack)+"\n");
                writeInputData.write(String.valueOf(nrOfPlayers)+"\n");
                writeInputData.write(String.valueOf(stage)+"\n");
                for(String card: cardsPokerFormat){
                    writeInputData.write(card+"\n");
                    System.out.print("|"+card+"|"+"\t");
                }
                //for(int n=0; n<7;n++) writeInputData.write(String.valueOf(cards[n][0]/14)+"\n"+String.valueOf(cards[n][1]/4)+"\n");
                System.out.print("##"+"\n");
                writeInputData.write(String.valueOf(this.raise)+"\n");
                writeInputData.write(String.valueOf(potWhileMoving)+ "\n");      //pot
                writeInputData.write("X" + "\n");
                lineWithMove=null;
                saveStatsToFile();
                raise=0;
                this.raise=0;
            }
        }
        return line;
    }
    private void saveStatsToFile() throws IOException {
        String stats = String.valueOf(myPosition)+"\t"+String.valueOf(myStack)+"\t"+String.valueOf(nrOfPlayers)+"\t"+String.valueOf(stage)+"\t";
        for (double card[]: cards
             ) {
            stats+=String.valueOf(card[0])+"\t";
        }
        stats+=String.valueOf(raise)+"\t"+String.valueOf(pot)+"\t"+actualMove+"\n";
        writeStats.write(stats);
    }
    private String[] movesAsStrings = {"Call", "Bet", "Raise 3BB", "Raise 0.5 pot", "Raise pot", "Fold", "Check", "All in"};
    private String actualMove;
    private String[] cardsPokerFormat = new String[7];
    private double[][] cards = new double[7][2];
    private byte[][] pokerMoves= new byte[8][8];
    private double pot=0,bigBlind,myStack,myPosition,stage=1,nrOfPlayers=0,raise=0;

    private void writeMove(byte[] move, int moveNr, int enableEncoding) throws IOException {
        if(enableEncoding==0){
            boolean wasWritten=false;
            for(int n=0;n<move.length-1;n++){
                if(n>0 && n<5){
                    if(move[n]==1){
                        writeOutputData.write("1"+"\n");
                        wasWritten=true;
                    }
                    else if(n==4 && !wasWritten) writeOutputData.write("0"+"\n");
                }
                else writeOutputData.write(String.valueOf(move[n])+"\n");
            }
            //for(byte mv : move ) writeOutputData.write(String.valueOf(mv)+"\n");
            writeOutputData.write("X"+"\n");
        }
        else if(enableEncoding==1){
            for(int n=1;n<=16;n++){
                if((moveNr)%n==0) writeOutputData.write("1"+"\n");
                else writeOutputData.write("0"+"\n");
            }
            writeOutputData.write("X"+"\n");
        }
        else if(enableEncoding==2){
            int[] binaryOut = new int[3];
            for(int out :binaryOut) out=0;
            double power=0;
            for(int n=2;n>=0;n--){
                power+=Math.pow(2,n);
                if(power==moveNr){
                    binaryOut[n]=1;
                    break;
                }
                else if(power>moveNr) power-=Math.pow(2,n);
                else binaryOut[n]=1;
            }
            for(int out=0;out<3;out++) writeOutputData.write(binaryOut[out]+"\n");
            writeOutputData.write("X"+"\n");
        }
        else {
            for(int digit : myEncoding[moveNr]) writeOutputData.write(digit+"\n");
            writeOutputData.write("X"+"\n");
        }
    }

    private int myEncoding[][]={
            {1,0,0,0,0,0,1,0,0,0,0,0,1,1,0,1,0,1,0,1,1},    //call
            {1,1,0,0,0,0,0,1,0,0,0,1,1,1,0,0,0,0,0,1,1},    //bet
            {0,1,1,0,1,0,1,0,1,0,0,0,0,1,1,0,0,0,0,1,0},    //raise 3BB
            {1,0,1,0,0,1,0,1,1,0,0,0,1,0,1,0,0,0,1,0,1},    //raise pot
            {1,1,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1,1,0},    //raise 0.5 pot
            {0,0,1,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,0,1,1},    //fold
            {0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,1},    //check
            {0,0,0,1,0,1,0,0,0,1,1,0,1,1,1,0,0,1,0,0,0}    //all in
    };

    private boolean GetMove(String line,double pot,double opponentRaise, int encoding) throws IOException {
        int doubledotPlacement=0;
        double raise=0;
        for (int sign=0;sign<line.length();sign++) if(line.charAt(sign)==':') doubledotPlacement=sign;
        if(line.contains("przebija") && !line.contains("all-in")){
            for (int sign=doubledotPlacement;sign<line.length();sign++) if(line.charAt(sign)=='d')
                raise=Double.parseDouble(line.substring(doubledotPlacement+11,sign-1));
            this.pot+=raise;
            if(raise/bigBlind >= 2 && raise/bigBlind <3.2){
                writeMove(pokerMoves[2],2,encoding);      // raise 3BB
                movesStats[2]++;
                actualMove= movesAsStrings[2];
            }
            else if(raise/pot > 0.4 && raise/pot < 0.7){
                writeMove(pokerMoves[4],4,encoding);     //raise 0.5pot
                movesStats[4]++;
                actualMove= movesAsStrings[4];
            }
            else if(raise/pot > 0.7){
                writeMove(pokerMoves[3],3,encoding);      //raise pot
                movesStats[3]++;
                actualMove= movesAsStrings[3];
            }
            else if(raise<=2*bigBlind){
                writeMove(pokerMoves[1],1,encoding);      //min bet
                movesStats[1]++;
                actualMove= movesAsStrings[1];
            }
            else{
                writeMove(pokerMoves[3],3,encoding);      //raise pot
                movesStats[3]++;
                actualMove= movesAsStrings[3];
            }
            myStack-=raise;
            return true;
        }
        else if(line.contains("all-in")){
            writeMove(pokerMoves[7],7,encoding);    //change to 7 to obtain all in into stats
            movesStats[7]++;
            actualMove= movesAsStrings[7];
            return true;
        }
        else if(line.contains("sprawdza")){
            writeMove(pokerMoves[0],0,encoding);
            movesStats[0]++;
            myStack-=opponentRaise;
            actualMove= movesAsStrings[0];
            return true;
        }
        else if(line.contains("stawia")){
            raise=Double.parseDouble(line.substring(doubledotPlacement+9,line.length()));
            myStack-=raise;
            if(raise/bigBlind >= 2 && raise/bigBlind <3.2){
                writeMove(pokerMoves[2],2,encoding);      // raise 3BB
                movesStats[2]++;
                actualMove= movesAsStrings[2];
            }
            else if(raise/pot > 0.4 && raise/pot < 0.7){
                writeMove(pokerMoves[4],4,encoding);     //raise 0.5pot
                movesStats[4]++;
                actualMove= movesAsStrings[4];
            }
            else if(raise/pot > 0.7){
                writeMove(pokerMoves[3],3,encoding);      //raise pot
                movesStats[3]++;
                actualMove= movesAsStrings[3];
            }
            else if(raise<=2*bigBlind){
                writeMove(pokerMoves[1],1,encoding);      //min bet
                movesStats[1]++;
                actualMove= movesAsStrings[1];
            }
            else{
                writeMove(pokerMoves[3],3,encoding);      //raise pot
                movesStats[3]++;
                actualMove= movesAsStrings[3];
            }
            return true;
        }
        else if(line.contains("czeka")){
            writeMove(pokerMoves[6],6,encoding);
            movesStats[6]++;
            actualMove= movesAsStrings[6];
            return true;
        }
        else if(line.contains("pasuje")){
            writeMove(pokerMoves[5],5,encoding);
            movesStats[5]++;
            actualMove= movesAsStrings[5];
            return true;
        }
        else if(line.contains(botName+" wygrał turniej") || line.contains(botName+" zakończył turniej")){
            endOfTournament=true;
            return false;
        }
        else return false;
    }
    private void resetCards(){
        for(double[] card : cards){
            card[0]=0;
            card[1]=0;
        }
        for(String card : cardsPokerFormat) card="";
    }

    public void Run() throws IOException {
        String line ="";
        while(line!=null) {
            if(!GetBlind()) break;
            getMyPositionAndStack();
            getMyCards();
            line = getPotAndBotMove();
            stageStats[3]++;
            if(!line.contains("PODSUMOWANIE") && !line.contains("POKAZANIE")){
                stage++;
                stageStats[0]++;
                getFlopCards(line);
                line=getPotAndBotMove();
                if(!line.contains("PODSUMOWANIE") && !line.contains("POKAZANIE")) {
                    stage++;
                    stageStats[1]++;
                    getTurnCard(line);
                    line = getPotAndBotMove();
                    if (!line.contains("PODSUMOWANIE") && !line.contains("POKAZANIE")) {
                        stage++;
                        stageStats[2]++;
                        getRiverCard(line);
                        line = getPotAndBotMove();
                    }
                }
            }
            if(endOfTournament) break;
            pot=0;
            nrOfPlayers=0;
            stage=0;
            raise=0;
            resetCards();
        }
        writeInputData.close();
        readData.close();
        writeOutputData.close();
        writeStats.close();

    }

}


















