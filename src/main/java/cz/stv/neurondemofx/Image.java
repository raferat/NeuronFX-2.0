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
package cz.stv.neurondemofx;


import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import com.google.gson.*;
import java.io.BufferedInputStream;
import java.io.IOException;
/**
 *
 * @author martin.vagner
 */
public class Image implements Serializable
{
  
  public int width, height;
  
  public int label;
  
  public short[][] matrix;
  
  public Image(int width , int height)
  {
    this.width = width;
    this.height = height;
    matrix = new short[width][height];    
    
  }
  
  public Image()
  {
    
  }
  
  public int getWidth()
  {
    return matrix.length;
  }
  
  public int getHeight()
  {
    return matrix[0].length;
  }
  
  public static Image readNextImage ( BufferedInputStream fis , int size ) throws IOException
  {
    Image g = new Image(size , size);
    
    byte[] read = new byte[size];
    for (int y = 0; y < size; y++)
    {
      for ( int x = 0 ; x < size ; x ++ )
        g.setPixel(x , y , (short) (255 - fis.read()));
    }

    return g;
  }
  
  

  
  
  public static Image load(Reader reader)
  {
    Gson g = new Gson();
    
    Image image = g.fromJson(reader , Image.class);
    return image;
  }
  
  public void erase ()
  {
    for ( int x = 0 ; x < getWidth() ; x ++)
      for ( int y = 0 ; y < getHeight() ; y ++)
        matrix[x][y] = 0;
  }
  
  public void setPixel ( int x , int y , short value)
  {
    matrix[x][y] = value;
  }
  
  public void save (Writer writer) throws IOException
  {
    Gson g = new GsonBuilder().setPrettyPrinting().create();
    writer.write(g.toJson(this, Image.class));
    writer.flush();
  }
  
  public void draw(ImageDrawable imageDrawable)
  {
    imageDrawable.draw(matrix);
    
  }

  @Override
  public String toString()
  {
    return "" + label;
  }
}
