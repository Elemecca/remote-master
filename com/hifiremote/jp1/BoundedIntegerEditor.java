package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

/**
 * Implements a cell editor that uses a formatted text field
 * to edit Integer values.
 */
public class BoundedIntegerEditor
  extends DefaultCellEditor
{
  private int min = Integer.MIN_VALUE;
  private int max = Integer.MAX_VALUE;
  boolean usePrefix = false;

  public BoundedIntegerEditor()
  {
    super( new JTextField());
  }

  public BoundedIntegerEditor( int minValue, int maxValue )
  {
    super( new JTextField());
    this.min = minValue;
    this.max = maxValue;
  }

  public void setUsePrefix( boolean flag )
  {
    usePrefix = flag;
  }

  public void setMin( int min )
  {
    this.min = min;
  }

  public void setMax( int max )
  {
    this.max = max;
  }

  public Object getCellEditorValue() 
  {
    Object value = super.getCellEditorValue();
    if ( value.getClass() == String.class )
      value = new Integer(( String )value );

    int val = (( Integer )value ).intValue();
    if (( val < min ) || ( val > max ))
      throw new NumberFormatException( value.toString() + "isn't between " + min + " and " + max + '.' );

    return value;
  }
}

