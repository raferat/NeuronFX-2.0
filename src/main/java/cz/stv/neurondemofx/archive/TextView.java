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


import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;


/**
 *
 * @author martin
 */
public class TextView extends TextField
{
  public TextView(String str)
  {
    super(str);
    Font f = new Font(Font.getFontNames().get(10), 15);
    
    setFont(f);
    setAlignment(Pos.BASELINE_CENTER);
    setEditable(false);
    setStyle("-fx-border-color: transparent;"
           + "-fx-faint-focus-color: transparent;"
           + "-fx-text-box-border: transparent;"
           + "-fx-focus-color: transparent;"
           + "-fx-highlight-fill: #1fb0ff;"
           + "-fx-highlight-text-fill: -fx-text-fill;");
  }
  
}
