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
public class Task extends NeuralNetwork
{
  private final int HEIGHT;
  private final int WIDTH;

  private final int correct;

  private final double [] inputDouble;
  
  private double[] outputs;
  
  public Task (final int width , final int height , final double [] inputDouble , int correct)
  {
    super(height * width , 2 , 9);
    HEIGHT = height;
    WIDTH = width;
    this.inputDouble = inputDouble;
    this.correct = correct;
    
    
    super.setLearningRate(0.05);
    super.setActivationFunction(new NeuralNetwork.ActivationFunction(Matrix.TANH , Matrix.TANH_DERIVATIVE));
  }
  
  public Answer run()
  {
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

    

    outputs = super.process(inputDouble);
    brain.train(inputDouble , correctOutputs);
    
    return getAnswer();
  }
  private NeuralNetwork brain;
  
  
  public void write(File file) throws Exception
  {
    brain.writeTo(file);
  }
  
  public Answer getAnswer ()
  {
    return arrayToAnswer(outputs);
  }
  
  private static Answer arrayToAnswer(double[] arr)
  {
    int biggestIndex = 0;
    
    for ( int index = 0 ; index < arr.length ; index ++ )
    {
      if ( arr[biggestIndex] < arr [index]  )
        biggestIndex = index;
    }
    
    
    return new Answer ( biggestIndex , arr[biggestIndex] );
  }
}
