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


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


public class ImageList
{

  private final LinkedList<Image> images = new LinkedList<>();

  private int currentIndex = 0;

  public final int imageWidth;
  public final int imageHeight;

  public ImageList(int imageWidth , int imageHeight)
  {
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public boolean empty()
  {
    return images.isEmpty();
  }

  public int position()
  {
    return currentIndex;
  }
  
  public void setPosition ( int position )
  {
    if ( position >= 0 && position < size() )
      currentIndex = position;
  }

  public int size()
  {
    return images.size();
  }

  public Image current()
  {
    if (!empty())
    {
      return images.get(currentIndex);
    }
    else
    {
      return null;
    }
  }

  public void add()
  {
    Image image = new Image(imageWidth , imageHeight);
    add(image);
  }

  private void add(Image image)
  {
    images.add(image);
    currentIndex = images.size() - 1;
  }
  
  private static int fromByteArray ( byte[] bytes )
  {
    return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
  }
  
  public static ImageList parse ( File[] files )
  {
    ImageList list = new ImageList(28, 28);
    for (File f : files)
    {
      try (BufferedInputStream stream = new BufferedInputStream ( new FileInputStream ( f )) )
      {
        byte[] magicNumber = stream.readNBytes ( 4 );
        if (Arrays.equals ( magicNumber , new byte[]
        {
          0 , 0 , 8 , 3
        } ))
        {
          int max = fromByteArray ( stream . readNBytes(4) );
          System.out.println(max);
          stream.readNBytes (8);
          for ( int i = 0 ; i < max ; i ++ )
            list . add ( Image . readNextImage ( stream , 28 ) );
        }
        else if (Arrays.equals ( magicNumber , new byte[]
        {
          0 , 0 , 8 , 1
        } ))
        {
          stream.readNBytes ( 4 );
          for ( Image i : list . images )
            i . label = stream.read ();
        }

      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }

    }
    
    return list;
  }
  

  public void delete()
  {
    if ( currentIndex > 0 )
    {
      images.remove(currentIndex);
      currentIndex--;
    }
  }

  public void save(File file)
  {
    if (file != null)
    {
      if (! file.getName() . endsWith(".rafeson") )
        file = new File(file.getAbsolutePath()+".rafeson");
        
      try (FileWriter writer = new FileWriter(file))
      {
        if (!file.exists())
        {
          file.createNewFile();
        }

        current().save(writer);
      }
      catch (IOException ex)
      {
        ex.printStackTrace(System.err);
      }
    }
  }

  public void save(String filePath)
  {
    if (filePath != null)
    {
      save(new File(filePath));
    }
  }

  public void load(String filePath)
  {
    if (filePath != null)
    {
      load(new File(filePath));
    }
  }

  public void load(File file)
  {
    if (file != null)
    {
      try (FileReader reader = new FileReader(file))
      {
        if (file.exists())
        {
          add(Image.load(reader));
        }
      }
      catch (IOException e)
      {
        e.printStackTrace(System.err);
      }
    }
  }

  public void back()
  {
    currentIndex = currentIndex == 0 ? size() - 1 : currentIndex - 1;
  }

  public void next()
  {
    currentIndex = currentIndex == size() - 1 ? 0 : currentIndex + 1;
  }

}
