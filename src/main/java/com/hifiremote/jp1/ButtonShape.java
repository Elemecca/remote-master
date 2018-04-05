package com.hifiremote.jp1;

import java.awt.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonShape.
 */
public class ButtonShape
{
  
  /**
   * Instantiates a new button shape.
   * 
   * @param shape the shape
   * @param button the button
   */
  public ButtonShape( Shape shape, Button button )
  {
    this.shape = shape;
    this.button = button;
  }

  /**
   * Sets the name.
   * 
   * @param name the new name
   */
  public void setName( String name ){ this.name = name; }
  
  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName(){ return name; }

  /**
   * Gets the shape.
   * 
   * @return the shape
   */
  public Shape getShape(){ return shape; }
  
  /**
   * Gets the button.
   * 
   * @return the button
   */
  public Button getButton(){ return button; }

  /** The name. */
  private String name = null;
  
  /** The shape. */
  private Shape shape = null;
  
  /** The button. */
  private Button button = null;
}
