package cz.stv.neuronnetworkfromnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import cz.stv.neuronnetworkfromnet.Matrix.MatFunc;

/**
 * This class is used to create new Neural Networks, configure them
 * (completely), and alter their functionality.
 * 
 */
public class NeuralNetwork implements Cloneable
{
	/**
	 * The amount of input nodes that this network has
	 */
	public final int inputNodes;
	/**
	 * The amount of hidden layers that this network has
	 */
	public final int hiddenLayers;
	/**
	 * The amount of hidden nodes per layer that this network has
	 */
	public final int hiddenNodes;
	/**
	 * The amount of output nodes that this network has
	 */
	public final int outputNodes;
	/**
	 * The weights that each layer has
	 */
	public final Matrix[] weights;
	/**
	 * The biases that each layer has
	 */
	public final Matrix[] biases;
	/**
	 * This is beyond me and I suggest you google it
	 */
	private ActivationFunction activationFunction;
	/**
	 * This is how big the learning steps of this network will be. If
	 * too big, then the network might overshoot and fall further
	 * back in the learning process.
	 */
	private double learningRate;
	
	/**
	 * This creates a new randomized neural network with one hidden
	 * layer and the given input, hidden, and output nodes.
	 * 
	 * @param inputNodes
	 * @param hiddenNodes
	 * @param outputNodes
	 */
	public NeuralNetwork(int inputNodes, int hiddenNodes, int outputNodes)
	{
		this(inputNodes, 1, hiddenNodes, outputNodes);
	}
	
	public NeuralNetwork(int inputNodes, int hiddenLayers, int hiddenNodes, int outputNodes)
	{
		this.inputNodes = inputNodes;
		this.hiddenLayers = hiddenLayers;
		this.hiddenNodes = hiddenNodes;
		this.outputNodes = outputNodes;
		
		weights = new Matrix[hiddenLayers + 1];
		for(int i = 0; i < hiddenLayers + 1; i++)
		{
			if(i == 0)
			{
				weights[i] = new Matrix(hiddenNodes, inputNodes);
			}
			else if(i == hiddenLayers)
			{
				weights[i] = new Matrix(outputNodes, hiddenNodes);
			}
			else
			{
				weights[i] = new Matrix(hiddenNodes, hiddenNodes);
			}
			
			weights[i].randomize();
		}
		
		biases = new Matrix[hiddenLayers + 1];
		for(int i = 0; i < hiddenLayers + 1; i++)
		{
			if(i == hiddenLayers)
			{
				biases[i] = new Matrix(outputNodes, 1);
			}
			else
			{
				biases[i] = new Matrix(hiddenNodes, 1);
			}

			biases[i].randomize();
		}
		
		learningRate = 0.01;
		activationFunction = new ActivationFunction(Matrix.SIGMOID, Matrix.SIGMOID_DERIVATIVE);
	}
	
	public NeuralNetwork(NeuralNetwork copy)
	{
		this.inputNodes = copy.inputNodes;
		this.hiddenLayers = copy.hiddenLayers;
		this.hiddenNodes = copy.hiddenNodes;
		this.outputNodes = copy.outputNodes;

		weights = new Matrix[copy.weights.length];
		for(int i = 0; i < weights.length; i++)
		{
			weights[i] = copy.weights[i].clone();
		}
		
		biases = new Matrix[copy.biases.length];
		for(int i = 0; i < biases.length; i++)
		{
			biases[i] = copy.biases[i].clone();
		}
		learningRate = copy.learningRate;
		activationFunction = copy.activationFunction;
	}
	
	public double[] process(double[] inputArray)
	{
		if(inputArray.length != inputNodes) throw new IllegalNeuralNetworkArgumentException("Input must have " + inputNodes + " element" + (inputNodes == 1 ? "" : "s"));

		for(int i = 0; i < inputNodes; i++)
		{
			if(Math.abs(inputArray[i]) > 2)
			{
				System.err.println("Index " + i + " is a bit too out-there");
			}
		}
		
		Matrix input = Matrix.fromArray(inputArray);

		for(int i = 1; i < hiddenLayers + 2; i++)
		{
			input = weights[i - 1].mult(input).add(biases[i - 1]).map(activationFunction.function);
		}
		
		return input.toArray();
	}
	
	public void train(double[] inputArray, double[] correct)
	{
		if(inputArray.length != inputNodes) throw new IllegalNeuralNetworkArgumentException("Input must have " + inputNodes + " element" + (inputNodes == 1 ? "" : "s"));
		if(correct.length != outputNodes) throw new IllegalNeuralNetworkArgumentException("Output must have " + outputNodes + " element" + (outputNodes == 1 ? "" : "s"));

		Matrix input = Matrix.fromArray(inputArray);
		Matrix[] layers = new Matrix[hiddenLayers + 2];
		layers[0] = input;

		for(int i = 1; i < hiddenLayers + 2; i++)
		{
			input = weights[i - 1].mult(input).add(biases[i - 1]).map(activationFunction.function);
			layers[i] = input;
		}
		
		Matrix target = Matrix.fromArray(correct);
		for(int i = hiddenLayers + 1; i > 0; i--)
		{
			// Calculate Error
			Matrix error = target.subtract(layers[i]);
			
			// Calculate Gradient
			Matrix gradient = layers[i].map(activationFunction.derivative);
			gradient = gradient.elementMult(error);
			gradient = gradient.mult(learningRate);
			
			// Calculate Delta
			Matrix delta = gradient.mult(layers[i - 1].transpose());
			
			// Adjust weights and biases
			biases[i - 1] = biases[i - 1].add(gradient);
			weights[i - 1] = weights[i - 1].add(delta);
		
			// Reset target for next loop
			target = weights[i - 1].transpose().mult(error).add(layers[i - 1]);
		}
	}
	
	public double getLearningRate()
	{
		return learningRate;
	}
	
	public NeuralNetwork setLearningRate(double learningRate)
	{
		this.learningRate = learningRate;
		return this;
	}
	
	public ActivationFunction getActivationFunction()
	{
		return activationFunction;
	}
	
	public NeuralNetwork setActivationFunction(ActivationFunction activationFunction)
	{
		this.activationFunction = activationFunction;
		return this;
	}
	
	public NeuralNetwork quoteBreedUnquote(final NeuralNetwork other)
	{
		if(inputNodes != other.inputNodes || hiddenLayers != other.hiddenLayers || hiddenNodes != other.hiddenNodes || outputNodes != other.outputNodes)
		{
			throw new IllegalNeuralNetworkArgumentException("These neural networks aren't compatible");
		}
		
		NeuralNetwork nn = clone();
		for(int i = 0; i < hiddenLayers + 1; i++)
		{
			Matrix weight = nn.weights[i];
			
			final int indx = i;
			weight.map(new MatFunc()
			{
				@Override
				public double perform(double val, int r, int c)
				{
					return Math.random() >= 0.5 ? val : other.weights[indx].data[r][c];
				}
			});
		}
		return nn;
	}
	
	public NeuralNetwork mutateWeights(double chance)
	{
		return mutateWeights(chance, ThreadLocalRandom.current());
	}
	
	public NeuralNetwork mutateWeights(final double chance, final Random rand)
	{
    for (Matrix weight : weights)
    {
      weight.map(new MatFunc()
      {
        @Override
        public double perform(double val, int r, int c)
        {
          return rand.nextDouble() < chance ? val + (rand.nextDouble() * 2 - 1) / 10 : val;
        }
      });
    }
		return this;
	}
	
	public NeuralNetwork mutateBiases(double chance)
	{
		return mutateWeights(chance, ThreadLocalRandom.current());
	}
	
	public NeuralNetwork mutateBiases(final double chance, final Random rand)
	{
    for (Matrix weight : weights)
    {
      weight.map(new MatFunc()
      {
        @Override
        public double perform(double val, int r, int c)
        {
          return rand.nextDouble() < chance ? val + (rand.nextDouble() * 3 - 1.5) : val;
        }
      });
    }
		return this;
	}
	
	public NeuralNetwork clone()
	{
		return new NeuralNetwork(this);
	}
	
	public void writeTo(File file) throws Exception
	{
		FileOutputStream fout = new FileOutputStream(file);
		writeTo(fout);
		fout.close();
	}
	
	public void writeTo(OutputStream out) throws IOException
	{
		ObjectOutputStream oout = new ObjectOutputStream(out);
    for (Matrix weight : weights)
    {
      for(int x = 0; x < weight.rows; x++)
      {
        for(int y = 0; y < weight.cols; y++)
        {
          oout.writeDouble(weight.data[x][y]);
        }
      }
    }
    for (Matrix bias : biases)
    {
      for(int x = 0; x < bias.rows; x++)
      {
        for(int y = 0; y < bias.cols; y++)
        {
          oout.writeDouble(bias.data[x][y]);
        }
      }
    }
		oout.close();
	}
	
	public void readFrom(File file) throws Exception
	{
		FileInputStream fin = new FileInputStream(file);
		readFrom(fin);
		fin.close();
	}
	
	public void readFrom(InputStream in) throws IOException
	{
		ObjectInputStream oin = new ObjectInputStream(in);
    for (Matrix weight : weights)
    {
      for(int x = 0; x < weight.rows; x++)
      {
        for(int y = 0; y < weight.cols; y++)
        {
          weight.data[x][y] = oin.readDouble();
        }
      }
    }
    for (Matrix bias : biases)
    {
      for(int x = 0; x < bias.rows; x++)
      {
        for(int y = 0; y < bias.cols; y++)
        {
          bias.data[x][y] = oin.readDouble();
        }
      }
    }
		oin.close();
	}
	
	public static class ActivationFunction
	{
		public final MatFunc function, derivative;
		
		public ActivationFunction(MatFunc function, MatFunc derivative)
		{
			this.function = function;
			this.derivative = derivative;
		}
	}
}
