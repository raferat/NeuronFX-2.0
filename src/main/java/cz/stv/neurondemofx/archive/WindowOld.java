/*
 * Copyright 2021 martin.vagner.
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


import cz.stv.neurondemofx.archive.TextView;
import javafx.application.Application;

import javafx.beans.value.ObservableValue;

import javafx.event.ActionEvent;

import javafx.scene.Scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.stage.Stage;


/**
 *
 * @author martin.vagner
 */
public class WindowOld extends Application
{
  


/**
 * JavaFX <code style="color:blue">Window</code>
 *
 * @author Raferat
 */


  /**
   * height of matrix
   */
  private static final int MATRIX_HEIGHT = 64;

  /**
   * width of matrix
   */
  private static final int MATRIX_WIDTH = 64;

  /**
   * scale of canvas
   *
   * @see canvas
   */
  private static final double CANVAS_SCALE = 10d;

  /**
   * width of canvas
   *
   * @see MATRIX_WIDTH
   * @see CANVAS_SCALE
   */
  private static final double CANVAS_WIDTH = MATRIX_WIDTH * CANVAS_SCALE;

  /**
   * height of canvas
   *
   * @see MATRIX_HEIGHT
   * @see CANVAS_SCALE
   */
  private static final double CANVAS_HEIGHT = MATRIX_HEIGHT * CANVAS_SCALE;

  private TextField correct;
  private TextField tresholdInput;

  /**
   * Canvas on which is showing <code>inputMatrix</code>
   *
   * @see inputMatrix
   *
   */
  private Canvas canvas;

  /**
   * graphicsContext is context of canvas.
   *
   * @see canvas
   */
  private GraphicsContext graphicsContext;

  /**
   * boolean that indicates if draw method should draw.
   *
   * @see draw
   * @see drawMoreEfficient
   * @see startPainting
   * @see stopPainting
   */
  private boolean isPainting = false;

  /**
   * boolean that indicates if mouse is out for method draw.
   *
   * @see draw
   * @see drawMoreEfficient
   * @see entered
   * @see exited
   */
  private boolean isOut = false;

  /**
   * inputMatrix is given to Neural Network as input
   *
   * @see cz.stv.neuronnetwork
   */
  private boolean inputMatrix[][] = new boolean[MATRIX_WIDTH][MATRIX_HEIGHT];

  /**
   * int that indicate last coordinate of mouse.
   */
  private int lastMouseX, lastMouseY;

  private double treshold = 0.1d;
  
  private TextView text;

  private Calculate calculate;
  private Scene scene;
  private Stage stage;
  private TextView output;

//===================================================================================================================================================
  /**
   * <code>initScene</code> is initializing the scene.
   *
   * @param stage is current window's stage
   * @see javafx.stage
   */
  private void initScene(Stage stage)
  {
    scene = new Scene(new VBox(initCanvas() , initTextEdit() , initMessages()));
    scene.setOnKeyPressed(this::pressed);
    scene.getRoot().setStyle("-fx-background-color: white");

    scene . getRoot().requestFocus();
    stage.setResizable(false);
    stage.setScene(scene);
    stage.setTitle("Neuron FX");
    stage.show();
  }

//------------------------------------------------------------------------------
  /**
   * <code>initCanvas</code> is initializing the canvas.
   *
   * @see canvas
   * @see javafx.scene.canvas.Canvas
   */
  private Canvas initCanvas()
  {
    //initializing canvas
    canvas = new Canvas(CANVAS_WIDTH , CANVAS_HEIGHT);
    graphicsContext = canvas.getGraphicsContext2D();

    //setting up listeners
    canvas.setOnMousePressed(this::startPainting);
    canvas.setOnMouseReleased(this::stopPainting);
    canvas.setOnMouseDragged(this::drawMoreEfficient);
    canvas.setOnMouseExited(this::exited);
    canvas.setOnMouseEntered(this::entered);
    
    return canvas;
  }
  
//------------------------------------------------------------------------------
  
  private HBox initMessages()
  {
    double textViewWidth = CANVAS_WIDTH / 2;
    double textViewHeight = 10;
    text = new TextView("Please fill the textfield");
    text.setPrefSize(textViewWidth, textViewHeight);
    text.setStyle("-fx-text-fill: red;"+ text.getStyle());
    text.setVisible(false);
    
    output = new TextView("Output: ");
    output.setPrefSize(textViewWidth, textViewHeight);
    
    return new HBox(text , output);
  }

//------------------------------------------------------------------------------
  
  private HBox initTextEdit()
  {
    HBox hbox = new HBox(2d);
    //------------
    // button part
    
    double buttonWidth = CANVAS_WIDTH / 7;
    Button[] buttons ={new Button("GO"),new Button("STOP"),new Button("SET")};
    buttons[0].setOnAction(this::startNeuralNetwork);
    buttons[1].setOnAction(this::stopButtonAction);
    buttons[2].setOnAction(this::setTreshold);
    
    for ( Button btn : buttons )
      btn.setPrefWidth(buttonWidth);
    
    //-------------
    //textfield part part
    
    double textWidth = (CANVAS_WIDTH - buttonWidth * buttons.length) / 2;
    correct = new TextField();
    correct.setPrefWidth(textWidth);
    correct.textProperty().addListener(this::textFormatterCorrect);
    
    
    
    tresholdInput = new TextField("" + treshold);
    tresholdInput . setPrefWidth(textWidth);
    tresholdInput.textProperty().addListener(this::textFormatterTreshold);
    
    
    
    

    hbox.getChildren().addAll(correct , buttons[0] , buttons[1] , tresholdInput , buttons[2]);

    return hbox;
  }
  
//===================================================================================================================================================
  
  void setTreshold(ActionEvent event)
  {
    try
    {
      treshold = Double.parseDouble(tresholdInput.getText());
      calculate.setTreshold(treshold);
      text.setVisible(false);
    }
    catch (NumberFormatException ex)
    {
      text.setVisible(true);
    }
  }
  
//------------------------------------------------------------------------------
  
  private void stopButtonAction( ActionEvent event )
  {
    if (calculate != null && calculate.isAlive())
    {
      try
      {
        calculate.cancel();
      }
      catch (InterruptedException ex)
      {
        ex.printStackTrace(System.err);
      }
    }
  }

//------------------------------------------------------------------------------
  
  private void startNeuralNetwork(ActionEvent event)
  {
    try
    {
      
      calculate.cancel();
      calculate = new Calculate(true);
      
      calculate.process(MATRIX_WIDTH , MATRIX_HEIGHT , inputMatrix , treshold , Integer.parseInt(correct.getText()));
      calculate.start();
      text.setVisible(false);
      
    }
    catch (InterruptedException ex)
    {
      ex.printStackTrace(System.err);
    }
    catch (NumberFormatException exception)
    {
      text.setVisible(true);
    }
  }

//===================================================================================================================================================
  
  private void textFormatterCorrect(final ObservableValue<? extends String> ov , final String oldValue , final String newValue)
  {
    String answer = newValue;
    if (correct.getText().length() > 1)
    {
      answer = oldValue;
    }
    if ( ! answer . matches("[0123456789]"))
      answer = answer . replaceAll ( "[^0123456789]" , "" );
    
    correct.setText(answer);
  }

//------------------------------------------------------------------------------
    
  private void textFormatterTreshold(final ObservableValue<? extends String> ov , final String oldValue , final String newValue)
  {
    if ( ! newValue . matches("[0123456789.]"))
      tresholdInput . setText ( newValue . replaceAll ( "[^0123456789.]" , "" ));
  }

//===================================================================================================================================================  
  private void startPainting(MouseEvent event)
  {
    isPainting = true;
    scene.getRoot().requestFocus();
  }

//------------------------------------------------------------------------------
  private void stopPainting(MouseEvent event)
  {
    isPainting = false;
    isOut = false;
  }

//------------------------------------------------------------------------------
  private void exited(MouseEvent event)
  {
    if (isPainting)
    {
      isPainting = false;
      isOut = true;
    }
  }

//------------------------------------------------------------------------------
  private void entered(MouseEvent event)
  {
    if (isOut)
    {
      isPainting = true;
    }
  }

//------------------------------------------------------------------------------  
  private void pressed(KeyEvent event)
  {
    KeyCode key = event.getCode();
    if (key == KeyCode.C)
    {
      graphicsContext.setFill(Color.WHITE);
      graphicsContext.fillRect(0 , 0 , CANVAS_WIDTH , CANVAS_HEIGHT);
      inputMatrix = new boolean[MATRIX_WIDTH][MATRIX_HEIGHT];
    }

  }

//===================================================================================================================================================
  private void draw(MouseEvent event)
  {
    if (isPainting)
    {
      int x = (int) (event.getX() / CANVAS_SCALE);
      int y = (int) (event.getY() / CANVAS_SCALE);

      inputMatrix[x][y] = true;

      graphicsContext.setFill(Color.BLACK);
      graphicsContext.fillRect(x * CANVAS_SCALE , y * CANVAS_SCALE , CANVAS_SCALE , CANVAS_SCALE);

    }

  }

  private void drawMoreEfficient(MouseEvent event)
  {
    if (lastMouseX == (int) event.getX() / CANVAS_SCALE)
    {
      return;
    }
    if (lastMouseY == (int) event.getY() / CANVAS_SCALE)
    {
      return;
    }

    if (isPainting)
    {
      int x = (int) (event.getX() / CANVAS_SCALE);
      int y = (int) (event.getY() / CANVAS_SCALE);

      if (inputMatrix[x][y] == false)
      {

        inputMatrix[x][y] = true;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(x * CANVAS_SCALE , y * CANVAS_SCALE , CANVAS_SCALE , CANVAS_SCALE);

      }
      lastMouseX = x;
      lastMouseY = y;

    }
  }

//===================================================================================================================================================  
  
  public void setOutputText(String str)
  {
    output.setText(str);
  }
  
  public String getOutputText ()
  {
    return output.getText();
  }
  
//===================================================================================================================================================  
  /**
   * Start method is called by JavaFX
   *
   * @param stage
   */
  @Override
  public void start(Stage stage)
  {
    calculate = new Calculate(true);
    this.stage = stage;
    initScene(stage);
    
    stage.setOnCloseRequest((event) ->
    {
      try
      {
        if (calculate . isAlive())
        {
          calculate.cancel();
        }
      }
      catch (InterruptedException ex)
      {
        ex.printStackTrace(System.err);
      }
    });
  }

//===================================================================================================================================================
  /**
   * startWindow is launching JavaFX
   *
   * @param args Arguments passed by command line
   */
  public static void startWindow(String[] args)
  {
    launch(args);
  }


}
