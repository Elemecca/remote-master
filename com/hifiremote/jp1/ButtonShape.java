package com.hifiremote.jp1;

import java.awt.*;

public class ButtonShape
{
  public ButtonShape( Shape shape, Button button )
  {
    this.shape = shape;
    this.button = button;
  }

  public void setName( String name ){ this.name = name; }
  public String getName(){ return name; }

  public Shape getShape(){ return shape; }
  public Button getButton(){ return button; }

  private String name = null;
  private Shape shape = null;
  private Button button = null;
}
