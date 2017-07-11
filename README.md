# ANN
Simple artificial neural network.
Key features are listed below:
- cost function: RMSE (Root mean squared error)
- optimizer: stochastic gradient descent (SGD) with friction coefficient
- activation function: sigmoid

This neural network was used to classify cards images retrieved from online poker client. However it can be used for wide variety of simple tasks (ex. edge detection, simple image classifying, detecting objects, etc).

The usage is pretty simple.

Training phase:
'''java
int[] initVector = {100,30,10};   //100 input values -> 30 neurons in 1st layer -> 10 neurons in output layer
NeuralNet ann = new NeuralNet(initVector);
double[] inputData = new double[100];   //fill it with input values
double[] outputTrainingData = new double[10];   //fill it with outputs related to eariler loaded inputs
ann.setInputData(inputData);
ann.setDesiredOutputData(outputTrainingData);
double frictionCoeff = 0.6;   //declare friction amount (prevents getting stuck in local minima)
ann.trainNet(frictionCoeff);
ann.

//repeat until error is low enough

ann.saveWeightsToFile("w.txt");
'''

Using phase:
'''java
int[] initVector = {100,30,10};   //100 input values -> 30 neurons in 1st layer -> 10 neurons in output layer
NeuralNet ann = new NeuralNet(initVector);
ann.loadWeightsFromFile("w.txt");
double[] inputData = new double[100];   //fill it with input values
ann.setInputData(inputData);
ann.runNet();
'''
