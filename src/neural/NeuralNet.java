package neural;
import java.io.*;
import java.util.ArrayList;

/**
 * @author Michal Swat
 * @version 2.0 (the network can learn !)
 * Created by swat on 05.07.16.
 */
public class NeuralNet {
    /**
     * position_0 defines input layer size
     * last_position defines output layer size
     * */
    private int[] layersSize;
    private ArrayList<Neuron[]> layersList = new ArrayList<>();
    private double[] inputData;
    public double[] outputMatrix;
    private double[] desiredOutput;
    private double[] outputErrorMatrix;
    /**
     * @param layersSize an array consisting of every layer's size
     * ex. layerSize = [10,20,5] means 10 input values, 20 sigmoid neurons, 5 (sigmoid) output neurons
     * Creates each of the specified layers with weights and biases initialized randomly
     * */
    public NeuralNet(int[] layersSize){
        this.layersSize=layersSize;
        inputData = new double[layersSize[0]];
        outputMatrix = new double[layersSize[layersSize.length-1]];
        outputErrorMatrix = new double[outputMatrix.length];
        //create layers
        for(int listPos=1;listPos<layersSize.length;listPos++){
            layersList.add(new Neuron[layersSize[listPos]]);
        }
        int previousLayerNumber = 0;
        for(Neuron[] neuronsArray:layersList){
            for(int arrayPos=0;arrayPos<neuronsArray.length;arrayPos++) neuronsArray[arrayPos] = new Neuron(layersSize[previousLayerNumber]);
            previousLayerNumber++;
        }
    }
    public void saveWeightsToFile(String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        for(int layer=0;layer<layersList.size();layer++){
            for(int neuron=0;neuron<layersList.get(layer).length;neuron++){
                for(int weight=0;weight<layersList.get(layer)[neuron].weights.length;weight++){
                    writer.write(String.valueOf(layersList.get(layer)[neuron].weights[weight]));
                    writer.write("\n");
                }
                writer.write(String.valueOf(layersList.get(layer)[neuron].bias));
                writer.write("\n");
            }
        } // iterate over layers
        writer.close();
    }

    public void loadWeightsFromFile(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(reader);
        for(int layer=0;layer<layersList.size();layer++){
            for(int neuron=0;neuron<layersList.get(layer).length;neuron++){
                for(int weight=0;weight<layersList.get(layer)[neuron].weights.length;weight++)
                    layersList.get(layer)[neuron].weights[weight]=Double.parseDouble(bufferedReader.readLine());
                layersList.get(layer)[neuron].bias=Double.parseDouble(bufferedReader.readLine());
            }
        }
        bufferedReader.close();
    }

    /**
     * Sets an input data for initialized network
     * */
    public void setInputData(double[] inputData){
        this.inputData=inputData;
    }

    public void setDesiredOutput(double[] desiredOutput){this.desiredOutput=desiredOutput; }
    /**
     * Should be used only after setting the input data, forces the net to set up calculations
     * @param learningRatio ratio by which partial derivatives are multiplied to find a global minimum
     * */
    public void trainNet(double learningRatio){
        feedFirstLayer();
        feedForward();
        evaluateSquaredErrors();
        backPropagation(0.5);
    }
    public void runNet(){
        feedFirstLayer();
        feedForward();
    }
    /**
     * Executes sigmoid function for every neuron in a 1st layer by using input data
     * */
    private void feedFirstLayer(){
        for(int arrayPos=0;arrayPos<layersList.get(0).length;arrayPos++) layersList.get(0)[arrayPos].activationFunction(inputData);
    }
    /**
     * Executes activation function for every neuron in a layer by using output data from previous layer
     * */
    private void feedForward(){
        for(int listPos=1;listPos<layersList.size();listPos++){
            double[] hiddenOutput = new double[layersList.get(listPos-1).length];
            for(int arrayPos=0;arrayPos<layersList.get(listPos-1).length;arrayPos++) hiddenOutput[arrayPos]=layersList.get(listPos-1)[arrayPos].output;
            for(int arrayPos=0;arrayPos<layersList.get(listPos).length;arrayPos++) layersList.get(listPos)[arrayPos].activationFunction(hiddenOutput);
        }//end for over all layers except the 1st
        /**
         * last layer's outputs are being copied to neural net output field
         * */
        for(int arrayPos=0;arrayPos<outputMatrix.length;arrayPos++) outputMatrix[arrayPos]=layersList.get(layersList.size()-1)[arrayPos].output;
    }
    /**
     * Evaluates partial errors based on quadratic cost function and sums them up as a totalError field
     * */
    private void evaluateSquaredErrors(){
        for(int arrayPos=0;arrayPos<desiredOutput.length;arrayPos++)
            outputErrorMatrix[arrayPos]=outputMatrix[arrayPos]-desiredOutput[arrayPos];
    }
    /**
     * Backpropagates calculated errors and adjust weights to find a global minimum of a cost function
     * */
    private void backPropagation(double learningRatio){
        int outputLayer=layersList.size()-1;
        for(int outputNeuron=0;outputNeuron<layersList.get(outputLayer).length;outputNeuron++){
            layersList.get(outputLayer)[outputNeuron].signalError= outputMatrix[outputNeuron]*(1-outputMatrix[outputNeuron])
                    *outputErrorMatrix[outputNeuron];   //calculate signal error
            for(int weight=0;weight<layersList.get(outputLayer-1).length;weight++){
                layersList.get(outputLayer)[outputNeuron].oldWeights[weight]=layersList.get(outputLayer)[outputNeuron].weights[weight];     //store old weight
                layersList.get(outputLayer)[outputNeuron].weights[weight]-=learningRatio*layersList.get(outputLayer-1)[weight].output*
                        layersList.get(outputLayer)[outputNeuron].signalError;      //update weight
            }// iterate over all weights in the neuron
        }//iterate over all neurons in output layer

        /*
        * Updating weights of neurons in hidden layers
        * */

        for(int layer = layersList.size()-2;layer>0;layer--){
            for(int neuron=0; neuron<layersList.get(layer).length;neuron++){
                double sum=0;
                /*Calculation of signal errors for every neuron*/
                for(int neuronInNextLayer=0;neuronInNextLayer<layersList.get(layer+1).length;neuronInNextLayer++)
                    sum+=layersList.get(layer+1)[neuronInNextLayer].signalError*layersList.get(layer+1)[neuronInNextLayer].oldWeights[neuron];
                layersList.get(layer)[neuron].signalError=sum*layersList.get(layer)[neuron].output*(1-layersList.get(layer)[neuron].output);

                /*Store old weights and update main weights*/
                for(int weight=0;weight<layersList.get(layer-1).length;weight++){
                    layersList.get(layer)[neuron].oldWeights[weight]=layersList.get(layer)[neuron].weights[weight];
                    layersList.get(layer)[neuron].weights[weight]-=learningRatio*layersList.get(layer-1)[weight].output*
                            layersList.get(layer)[neuron].signalError;
                }
            }// iterate over all neurons in a layer
        }//for iterating over hidden layers

        /*
        * Updating weights of first hidden layer (connected with input)
        * */
        for(int neuron=0; neuron<layersList.get(0).length;neuron++){
            double sum=0;
                /*Calculation of signal errors for every neuron*/
            for(int neuronInNextLayer=0;neuronInNextLayer<layersList.get(1).length;neuronInNextLayer++)
                sum+=layersList.get(1)[neuronInNextLayer].signalError*layersList.get(1)[neuronInNextLayer].oldWeights[neuron];
            layersList.get(0)[neuron].signalError=sum*layersList.get(0)[neuron].output*(1-layersList.get(0)[neuron].output);

                /*Store old weights and update main weights*/
            for(int weight=0;weight<inputData.length;weight++){
                layersList.get(0)[neuron].oldWeights[weight]=layersList.get(0)[neuron].weights[weight];
                layersList.get(0)[neuron].weights[weight]-=learningRatio*inputData[weight]*
                        layersList.get(0)[neuron].signalError;
            }
        }
    }


    class Neuron {
        /**
         * Each neuron consists of an array of weights based on previous layer size
         * Weights are initialized randomly as well as bias
         * */
        private double biasRangeDown=0, biasRangeUp=0.3, weightRangeDown=0.01, weightRangeUp=0.05;
        private double[] weights;
        private double bias;
        private double output;
        private double[] oldWeights;
        private double signalError;
        /**
         * Initializes sigmoid neuron object: creates weights array and bias - fills it randomly
         * */
        Neuron(int previousLayerSize){
            this.weights = new double[previousLayerSize];
            oldWeights = new double[previousLayerSize];
            //init randomly weights
            for (int arrayPosition=0;arrayPosition<weights.length;arrayPosition++)
                weights[arrayPosition]= RandomNumbersGenerator.getRandomDouble(weightRangeDown,weightRangeUp);
            bias = RandomNumbersGenerator.getRandomDouble(biasRangeDown,biasRangeUp);
        }
        /**
         * Calculates sigmoid function which is used as an input for next layer
         *  ,the result is assigned to neuron's output field
         * */
        private double sigmoidFunction(double sum, double bias){
            return 1/(1+Math.exp(-sum+bias));
        }
        private void activationFunction(double[] inputs){
            double sum=0;
            for(int listPosition=0;listPosition<weights.length;listPosition++)
                sum+=weights[listPosition]*inputs[listPosition];
            output = sigmoidFunction(sum,bias); // sigmoid function
        }
    }
}
