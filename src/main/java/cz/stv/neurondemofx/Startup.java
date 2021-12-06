package cz.stv.neurondemofx;


import java.net.URL;


/**
 *
 * @author Raferat
 */
public class Startup
{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    URL u = Window.class.getResource("/Erase.svg");
    if ( u != null )
      System.out.println(u.getPath());
      
    Window.startWindow(args);
  }
  
}
