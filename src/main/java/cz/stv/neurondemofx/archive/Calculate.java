/*
 * Copyright 2021 martin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.stv.neurondemofx.archive;


import cz.stv.neuronnetworkfromnet.Matrix;
import cz.stv.neuronnetworkfromnet.MatrixException;
import cz.stv.neuronnetworkfromnet.NeuralNetwork;
import java.io.File;
import java.util.Arrays;


/**
 *
 * @author martin
 */
public class Calculate extends Thread
{

  private int MATRIX_HEIGHT;
  private int MATRIX_WIDTH;

  private double treshold;
  private int correct;

  private boolean[][] inputMatrix;

  private boolean run = true;
  
  private boolean readFromFile = false;

  public Calculate()
  {
    super();
  }
  
  public Calculate ( boolean readFromFile )
  {    
    super();
    this.readFromFile = readFromFile;
  }

  public Calculate process(final int width , final int height , final boolean[][] inputMatrix , double treshold , int correct)
  {
    MATRIX_HEIGHT = height;
    MATRIX_WIDTH = width;
    this.inputMatrix = inputMatrix;
    this.treshold = treshold;
    this.correct = correct;
    return this;
  }

  public void setCorrect(int correct)
  {
    this.correct = correct;
  }

  public void setTreshold(double treshold)
  {
    this.treshold = treshold;
  }

  public void cancel() throws InterruptedException
  {
    run = false;
    super.join();
  }

  @Override
  public void run()
  {
    NeuralNetwork brain = new NeuralNetwork(MATRIX_HEIGHT * MATRIX_WIDTH , 2 , 9);
    brain.setLearningRate(0.05);
    brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Matrix.TANH , Matrix.TANH_DERIVATIVE));
    if (readFromFile)
    {
      try
      {
        brain.readFrom(new File("out.matrix"));
      }
      catch (Exception ex)
      {
        ex.printStackTrace(System.err);
      }
    }

    double[] correctOutputs = new double[9];

    for (int i = 0; i < correctOutputs.length; i++)
    {
      if (i == correct)
      {
        correctOutputs[i] = 1d;
      }
      else
      {
        correctOutputs[i] = 0d;
      }
    }

    double[] inputDouble = new double[MATRIX_HEIGHT * MATRIX_WIDTH];

    for (int i = 0; i < inputMatrix.length; i++)
    {
      for (int j = 0; j < inputMatrix[i].length; j++)
      {
        inputDouble[i * inputMatrix.length + j] = inputMatrix[i][j] ? 1d : 0d;
      }
    }

    //almostEquals(brain.process(inputDouble) , correctOutputs , treshold);
    brain.train(inputDouble , correctOutputs);
    

    double[] outputs = brain.process(inputDouble);
    System.out.println("Test: " + arrayToString(outputs));
    try
    {
      brain.writeTo(new File("out.matrix"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace(System.err);
    }

  }
  
  
  private static String arrayToString(double[] arr)
  {
    int biggestIndex = 0;
    
    for ( int index = 0 ; index < arr.length ; index ++ )
    {
      if ( arr[biggestIndex] < arr [index]  )
        biggestIndex = index;
    }
    
    
    return "" + biggestIndex;
  }
  

  private static boolean almostEquals(double[] one , double[] two , double maxBetveen)
  {
    boolean areAlmostSame = false;
    if (one.length != two.length)
    {
      throw new MatrixException("Arrays aren't of the same size!");
    }

    if (isTheBiggest(one , two))
    {
      for (int i = 0; i < one.length; i++)
      {
        if (one[i] <= two[i] + maxBetveen
                && one[i] >= two[i] - maxBetveen)
        {
          areAlmostSame = true;
        }
        else
        {
          return false;
        }
      }
    }

    return areAlmostSame;
  }

  private static boolean almostEquals(double[] one , double[] two)
  {
    return almostEquals(one , two , 0.65d);
  }

  private static boolean isTheBiggest(double[] one , double[] two)
  {
    double[] tmp = one.clone();
    Arrays.sort(tmp);

    for (int i = 0; i < one.length; i++)
    {
      if (two[i] > 0)
      {
        if (one[i] == tmp[tmp.length - 1])
        {
          return true;
        }
      }
    }

    return false;
  }

}
