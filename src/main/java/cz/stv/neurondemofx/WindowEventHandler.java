package cz.stv.neurondemofx;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class WindowEventHandler
{

  private Window window;

  TextField go = new TextField();
  Stage s = new Stage();
  Scene textScene = new Scene(go);
  Label infoLabel = new Label();
  Scene infoScene = new Scene(infoLabel);

  public WindowEventHandler(Window w)
  {
    window = w;
    s.setAlwaysOnTop(true);
    s.initStyle(StageStyle.UNDECORATED);
    go.setStyle("-fx-background-color: green;");
    go.textProperty().addListener(new ChangeListener<String>()
    {
      @Override
      public void changed(ObservableValue<? extends String> observable , String oldValue , String newValue)
      {
        if (!newValue.matches("[0123456789]"))
        {
          go.setText(newValue.replaceAll("[^0123456789]" , ""));
        }

      }
    });
    go.setOnKeyPressed((KeyEvent e) ->
    {
      if (e.getCode() == KeyCode.ENTER)
      {
        window.imageList.setPosition(Integer.parseInt(go.getText()) - 1);
        repaintEfficient();
        window.updateControls();
        s.hide();
      }
    });

  }

  //------------------------------------------------------------------------------
  void exited(MouseEvent event)
  {
    if (window.isPainting)
    {
      window.isPainting = false;
      window.isOut = true;
    }
  }

  void repaintEfficient()
  {
    window.imageList.current().draw((short[][] matrix) ->
    {
      window.graphicsContext.setFill(Color.WHITE);
      window.graphicsContext.fillRect(0 , 0 , window.MATRIX_WIDTH * window.CANVAS_SCALE , window.MATRIX_HEIGHT * window.CANVAS_SCALE);
      for (int x = 0; x < matrix.length; x++)
      {
        for (int y = 0; y < matrix[x].length; y++)
        {
          if (matrix[x][y] != 0)
          {
            window.graphicsContext.setFill(Color.rgb(matrix[x][y] , matrix[x][y] , matrix[x][y]));
            window.graphicsContext.fillRect(x * window.CANVAS_SCALE , y * window.CANVAS_SCALE , window.CANVAS_SCALE , window.CANVAS_SCALE);
          }
        }
      }
    });
    window.udateIndicators();
  }

  //===================================================================================================================================================
  void startPainting(MouseEvent event)
  {
    window.isPainting = true;
    window.scene.getRoot().requestFocus();
  }

  //------------------------------------------------------------------------------
  void entered(MouseEvent event)
  {
    if (window.isOut)
    {
      window.isPainting = true;
    }
  }

  void buttonClicked(ActionEvent e)
  {
    if (e.getSource() instanceof Button)
    {
      Button b = (Button) e.getSource();
      int index = window.grayButtonChoosers.indexOf(b);
      window.currentColor = (byte) ((127 / window.grayButtonChoosers.size()) * index);
    }
  }
  //===================================================================================================================================================

  void drawMoreEfficient(MouseEvent event)
  {
    if (window.isPainting)
    {
      int xDraw = (int) (event.getX() / window.CANVAS_SCALE);
      int yDraw = (int) (event.getY() / window.CANVAS_SCALE);
      window.imageList.current().draw((short[][] matrix) ->
      {
        matrix[xDraw][yDraw] = window.currentColor;
        window.graphicsContext.setFill(Color.WHITE);
        window.graphicsContext.fillRect(0 , 0 , window.MATRIX_WIDTH * window.CANVAS_SCALE , window.MATRIX_HEIGHT * window.CANVAS_SCALE);
        for (int x = 0; x < matrix.length; x++)
        {
          for (int y = 0; y < matrix[x].length; y++)
          {
            if (matrix[x][y] != 0)
            {
              window.graphicsContext.setFill(Color.rgb(matrix[x][y] , matrix[x][y] , matrix[x][y]));
              window.graphicsContext.fillRect(x * window.CANVAS_SCALE , y * window.CANVAS_SCALE , window.CANVAS_SCALE , window.CANVAS_SCALE);
            }
          }
        }
      });
    }
    window.udateIndicators();
  }

  //------------------------------------------------------------------------------
  void pressed(KeyEvent event)
  {
    KeyCode key = event.getCode();
    if (null != key)
    {
      switch (key)
      {
        case C:
          window.imageList.current().erase();
          break;
        case A:
          window.imageList.add();
          repaintEfficient();
          window.updateControls();
          break;
        case D:
          window.imageList.delete();
          break;
        case G:
          s.setScene(textScene);
          go.requestFocus();
          s.show();
          break;
        case I:
          System.out.println(window.imageList.current());
          break;
        case LEFT:
          window.imageList.back();
          break;
        case RIGHT:
          window.imageList.next();
          break;
      }
    }

    repaintEfficient();
    window.updateControls();

  }

  //------------------------------------------------------------------------------
  void stopPainting(MouseEvent event)
  {
    window.isPainting = false;
    window.isOut = false;
  }

}
