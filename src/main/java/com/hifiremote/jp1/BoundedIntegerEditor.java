package com.hifiremote.jp1;


// TODO: Auto-generated Javadoc
/**
 * Implements a cell editor that uses a formatted text field
 * to edit Integer values.
 */
public class BoundedIntegerEditor
  extends SelectAllCellEditor
{
  
  /** The min. */
  private int min = Integer.MIN_VALUE;
  
  /** The max. */
  private int max = Integer.MAX_VALUE;
  
  /** The use prefix. */
  boolean usePrefix = false;

  /**
   * Instantiates a new bounded integer editor.
   */
  public BoundedIntegerEditor()
  {
    super();
  }

  /**
   * Instantiates a new bounded integer editor.
   * 
   * @param minValue the min value
   * @param maxValue the max value
   */
  public BoundedIntegerEditor( int minValue, int maxValue )
  {
    super();
    this.min = minValue;
    this.max = maxValue;
  }

  /**
   * Sets the use prefix.
   * 
   * @param flag the new use prefix
   */
  public void setUsePrefix( boolean flag )
  {
    usePrefix = flag;
  }

  /**
   * Sets the min.
   * 
   * @param min the new min
   */
  public void setMin( int min )
  {
    this.min = min;
  }

  /**
   * Sets the max.
   * 
   * @param max the new max
   */
  public void setMax( int max )
  {
    this.max = max;
  }

  /* (non-Javadoc)
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
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

