package cz.stv.neurondemofx;


import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 *
 * @author martin
 */
public class WindowInitializer
{
  
  
  
  //------------------------------------------------------------------------------
  /**
   * <code>initCanvas</code> is initializing the canvas.
   *
   * @see canvas
   * @see javafx.scene.canvas.Canvas
   */
  private Canvas initCanvas(Window window)
  {
    //initializing canvas
    window.canvas = new Canvas(window.CANVAS_WIDTH , window.CANVAS_HEIGHT);
    window.graphicsContext = window.canvas.getGraphicsContext2D();
    //setting up listeners
    window.canvas.setOnMousePressed(window.windowEventHandler::startPainting);
    window.canvas.setOnMouseReleased(window.windowEventHandler::stopPainting);
    window.canvas.setOnMouseDragged(window.windowEventHandler::drawMoreEfficient);
    window.canvas.setOnMouseExited(window.windowEventHandler::exited);
    window.canvas.setOnMouseEntered(window.windowEventHandler::entered);
    return window.canvas;
  }

  private VBox initVBox(Node... nodes)
  {
    return new VBox(nodes);
  }

  private void initButtons(Window window)
  {
    window.buttonErase.setOnAction(e ->
    {
      window.imageList.current().erase();
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonNext.setOnAction(e ->
    {
      window.imageList.next();
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonBack.setOnAction(e ->
    {
      window.imageList.back();
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonSave.setOnAction(e ->
    {
      window.imageList.save(window.fileChooser.showSaveDialog(window.stage));
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonLoad.setOnAction(e ->
    {
      window.imageList.load(window.fileChooser.showOpenDialog(window.stage));
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonAdd.setOnAction(e ->
    {
      window.imageList.add();
      window.updateControls();
      window.windowEventHandler.repaintEfficient();
    });
    window.buttonRemove.setOnAction(e ->
    {
      if (window.imageList.position() != 0)
      {
        window.imageList.delete();
        if (window.imageList.position() == window.imageList.size())
        {
          window.imageList.back();
        }
        window.updateControls();
        window.windowEventHandler.repaintEfficient();
      }
    });
  }
  
  private GridPane initNeuralNetwork( Window window )
  {
    
    for ( int i = 0 ; i < 10 ; i ++ )
    {
      window.indicators[i] = new ProgressBar();
    }
    
    Label[] labels = new Label[10];
    
    for ( int i = 0 ; i < labels.length ; i ++ )
    {
      labels[i] = new Label(""+i);
    }
    
    GridPane row1 = new GridPane();
    //HBox row2 = new HBox(window.indicators);
    
    row1.setMinWidth(window.CANVAS_WIDTH+window.colorPicker.getWidth());
    row1.addRow(0 , labels);
    row1.addRow(1 , window.indicators);
    
    row1.setAlignment(Pos.CENTER);
    
    
    return row1;
  }

  private VBox initClolorPicker(Window window)
  {
    window.grayButtonChoosers.add(window.buttonWhite);
    double gab = 10.0;
    //double buttonWidth = 100d;
    double buttonHeight = ((window.MATRIX_HEIGHT * window.CANVAS_SCALE + 50.0) - gab * (window.GRAY_COLOR_COUNT + window.grayButtonChoosers.size() - 1)) / (window.GRAY_COLOR_COUNT + window.grayButtonChoosers.size());
    /*buttonWhite.setPrefSize(buttonHeight , buttonHeight);
    buttonWhite.setMinSize(buttonHeight , buttonHeight);
    buttonWhite.setMaxSize(buttonHeight , buttonHeight);*/
    ((ImageView) window.buttonWhite.getGraphic()).setPreserveRatio(true);
    ((ImageView) window.buttonWhite.getGraphic()).setFitHeight(buttonHeight - 10);
    ((ImageView) window.buttonWhite.getGraphic()).setFitWidth(buttonHeight - 10);
    ((ImageView) window.buttonWhite.getGraphic()).setSmooth(true);
    window.buttonWhite.setOnAction(window.windowEventHandler::buttonClicked);
    for (int i = 0; i < window.GRAY_COLOR_COUNT; i++)
    {
      Canvas c = new Canvas((int) buttonHeight - 10 , (int) buttonHeight - 10);
      Color col = Color.rgb((127 / window.GRAY_COLOR_COUNT) * i , (127 / window.GRAY_COLOR_COUNT) * i , (127 / window.GRAY_COLOR_COUNT) * i);
      c.getGraphicsContext2D().setFill(col);
      c.getGraphicsContext2D().fillRect(0 , 0 , c.getWidth() , c.getHeight());
      Button b = new Button("" , c);
      b.setPrefSize(buttonHeight , buttonHeight);
      b.setOnAction(window.windowEventHandler::buttonClicked);
      window.grayButtonChoosers.add(b);
    }
    window.colorPicker = new VBox(gab , window.grayButtonChoosers.toArray(new Button[window.grayButtonChoosers.size()]));
    return window.colorPicker;
  }

  //===================================================================================================================================================
  /**
   * <code>initScene</code> is initializing the scene.
   *
   * @param stage is current window's stage
   * @see javafx.stage
   */
  void initScene(Stage stage , Window window)
  {
    window.scene = new Scene(initVBox(new HBox(initVBox(initCanvas( window ) , initToolbar( window )) , initClolorPicker(window)),initNeuralNetwork(window)));
    window.scene.setOnKeyPressed(window.windowEventHandler::pressed);
    window.scene.getRoot().setStyle("-fx-background-color: white");
    window.scene.getRoot().requestFocus();
    stage.setResizable(false);
    stage.setScene(window.scene);
    stage.setTitle("Neuron FX");
    window.updateControls();
    stage.show();
  }

  private HBox initToolbar(Window window)
  {
    initButtons(window);
    HBox hBox = new HBox(10.0 , window.buttonBack , window.status , window.buttonAdd , window.buttonRemove , window.buttonErase , window.buttonLoad , window.buttonSave , window.buttonNext);
    hBox.setAlignment(Pos.CENTER);
    ObservableList<Node> content = hBox.getChildren();
    double buttonWidth = (window.canvas.getWidth() - hBox.getSpacing() * (content.size() - 1)) / content.size();
    for (Node node : content)
    {
      if (node instanceof Button)
      {
        Button tmp = (Button) node;
        tmp.setPrefSize(buttonWidth , 50.0);
      }
      else if (node instanceof Label)
      {
        Label tmp = (Label) node;
        tmp.setPrefSize(buttonWidth , 50.0);
        tmp.setFont(new Font(20.0));
        tmp.setAlignment(Pos.CENTER);
      }
    }
    return hBox;
  }
  
}
