package cz.stv.neurondemofx;

import cz.stv.neuronnetworkfromnet.NeuralNetwork;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;



import javafx.scene.Scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javafx.stage.Stage;


/**
 * JavaFX <code style="color:blue">Window</code>
 *
 * @author Raferat
 */
public class Window extends Application
{

  /**
   * height of matrix
   */
  final int MATRIX_HEIGHT;

  /**
   * width of matrix
   */
  final int MATRIX_WIDTH;

  /**
   * scale of canvas
   *
   * @see canvas
   */
  final double CANVAS_SCALE = 30d;

  /**
   * width of canvas
   *
   * @see MATRIX_WIDTH
   * @see CANVAS_SCALE
   */
  final double CANVAS_WIDTH;

  /**
   * height of canvas
   *
   * @see MATRIX_HEIGHT
   * @see CANVAS_SCALE
   */
  final double CANVAS_HEIGHT;

  TextField correct;
  TextField tresholdInput;

  Canvas canvas;

  /**
   * graphicsContext is context of canvas.
   *
   * @see canvas
   */
  GraphicsContext graphicsContext;

  /**
   * boolean that indicates if draw method should draw.
   *
   * @see draw
   * @see drawMoreEfficient
   * @see startPainting
   * @see stopPainting
   */
  boolean isPainting = false;

  /**
   * boolean that indicates if mouse is out for method draw.
   *
   * @see draw
   * @see drawMoreEfficient
   * @see entered
   * @see exited
   */
  boolean isOut = false;

  final ImageList imageList;

  byte currentColor = 1;

  /**
   * int that indicate last coordinate of mouse.
   */
  Scene scene;
  Stage stage;

  final Button buttonNext = new Button( ">" );
  final Button buttonBack = new Button( "<" );

  final Button buttonErase = new Button( "Erase" );
  final Button buttonLoad = new Button( "Open" );
  final Button buttonSave = new Button( "Save" );
  final Button buttonAdd = new Button( "Add" );
  final Button buttonRemove = new Button( "Delete" );
  
  final Button buttonWhite; 

  final Label status = new Label( "1/1" );
  
  final int GRAY_COLOR_COUNT = 5;
  
  final ArrayList<Button> grayButtonChoosers = new ArrayList<>();
  
  VBox colorPicker = new VBox();

  
  final FileChooser fileChooser;
  
  final WindowEventHandler windowEventHandler = new WindowEventHandler(this);
  
  final ProgressBar[] indicators = new ProgressBar[10];
  
  final NeuralNetwork neuralNetwork;
  
  public Window()
  {
    fileChooser = new FileChooser();
    fileChooser.setTitle("Open mnist files");
    List<File> l = fileChooser.showOpenMultipleDialog(stage);
    imageList = ImageList.parse( l . toArray(new File[l.size()] ));
    MATRIX_HEIGHT = imageList.imageHeight;
    MATRIX_WIDTH = imageList.imageWidth;
    
    CANVAS_WIDTH = MATRIX_WIDTH * CANVAS_SCALE;
    CANVAS_HEIGHT = MATRIX_HEIGHT * CANVAS_SCALE;
    
    Image img = new javafx.scene.image.Image(Window.class.getResourceAsStream("/Eraser.png"));
    ImageView view = new ImageView( img );
    buttonWhite = new Button("", view );
    
    
    fileChooser.setTitle("Folder to save:");
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Rafeson", ".rafeson"));
    
    neuralNetwork = new NeuralNetwork( imageList.imageHeight * imageList.imageWidth , 12 , 10);
    try
    {
      neuralNetwork . readFrom(Window.class.getResourceAsStream("/2.matrix"));
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
  
  
  
//===================================================================================================================================================
  
  private void updateColorPicker()
  {
    add();
    
    double gab = 10d;
    double buttonWidth = 100d;
    double buttonHeight = ( MATRIX_HEIGHT * CANVAS_SCALE + gab) / ( grayButtonChoosers.size() ) ;
    
    
    for ( int i = 0 ; i < grayButtonChoosers.size() ; i ++ )
    {
      Canvas c = new Canvas((int)buttonHeight - 10, (int)buttonHeight - 10);
      
      Color col = Color.rgb( ( 127 / grayButtonChoosers.size() ) * i , ( 127 / grayButtonChoosers.size() ) * i , ( 127 / grayButtonChoosers.size() ) * i );
      
      c.getGraphicsContext2D().setFill( col );
      c.getGraphicsContext2D().fillRect(0, 0, c.getWidth(), c.getHeight());
      
      Button b = new Button("" , c);
      b.setPrefSize(buttonWidth , buttonHeight);
      b.setOnAction(windowEventHandler::buttonClicked);
      grayButtonChoosers.set(i , b);
    }
  }
  
  private void add()
  {
    double gab = 10d;
    double buttonWidth = 100d;
    double buttonHeight = ( MATRIX_HEIGHT * CANVAS_SCALE + gab) / ( grayButtonChoosers.size() + 1 ) ;
    int newButtonIndex = grayButtonChoosers.size();
    
    Canvas c = new Canvas((int)buttonHeight - 10, (int)buttonHeight - 10);
      
    Color col = Color.rgb(( 127 / grayButtonChoosers.size()+1 ) * newButtonIndex , ( 127 / grayButtonChoosers.size()+1 ) * newButtonIndex , ( 127 / grayButtonChoosers.size()+1 ) * newButtonIndex );
      
    c.getGraphicsContext2D().setFill( col );
    c.getGraphicsContext2D().fillRect(0, 0, c.getWidth(), c.getHeight());
      
    Button b = new Button("" , c);
    b.setPrefSize(buttonWidth , buttonHeight);
    b.setOnAction(windowEventHandler::buttonClicked);
      
    grayButtonChoosers . add (grayButtonChoosers.size()-2 , b); 
    
    colorPicker.getChildren().add(b);
    
  }
  


//------------------------------------------------------------------------------  
  void updateControls ()
  {
    updateLabel();
    updateButtons();
  }
  
  private void updateButtons()
  {
    buttonRemove . setDisable(imageList.size() == 1);
    buttonBack . setDisable(imageList.position() == 0);
    buttonNext . setDisable(imageList.position() == imageList.size()-1);
    buttonAdd . setDisable(imageList.size() == 420);
  }
  
  private void updateLabel()
  {
    String s = String.format( "%d/%d" , imageList.position()+1 , imageList.size() );
    status.setText( s );
  }

  void udateIndicators()
  {
    double[] input = new double[imageList.imageWidth*imageList.imageHeight];
    int counter = 0;
    for ( int x = 0 ; x < imageList.current().matrix.length ; x ++ )
      for ( int y = 0 ; y < imageList.current().matrix[x].length ; y ++ )
      {
        input[counter] = (imageList.current().matrix[y][x]+1) / 255;
        counter ++;
      }
    
    double [] output = neuralNetwork.process(input);
    
    System.out.println(Arrays.toString(output));
    
    for ( int i = 0 ; i < output.length ; i ++ )
      indicators[i].setProgress(output[i]);
      
  }

//===================================================================================================================================================  
  /**
   * Start method is called by JavaFX
   *
   * @param stage
   */
  @Override
  public void start ( Stage stage )
  {
    this.stage = stage;
    if ( imageList.size() == 0 )
      imageList.add();
    
    
    new WindowInitializer() . initScene(stage , this );
    
    windowEventHandler.repaintEfficient();
  }

//===================================================================================================================================================
  /**
   * startWindow is launching JavaFX
   *
   * @param args Arguments passed by command line
   */
  public static void startWindow ( String[] args )
  {
    launch( args );
  }
}
