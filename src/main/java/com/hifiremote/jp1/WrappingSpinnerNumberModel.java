package com.hifiremote.jp1;

import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class WrappingSpinnerNumberModel.
 */
public class WrappingSpinnerNumberModel
  extends SpinnerNumberModel
{
  
  /**
   * Instantiates a new wrapping spinner number model.
   */
  public WrappingSpinnerNumberModel()
  {
    super();
  }
  
  /**
   * Instantiates a new wrapping spinner number model.
   * 
   * @param value the value
   * @param minimum the minimum
   * @param maximum the maximum
   * @param stepSize the step size
   */
  public WrappingSpinnerNumberModel( double value, double minimum, double maximum, double stepSize )
  {
   super( value, minimum, maximum, stepSize );
  }
  
  /**
   * Instantiates a new wrapping spinner number model.
   * 
   * @param value the value
   * @param minimum the minimum
   * @param maximum the maximum
   * @param stepSize the step size
   */
  public WrappingSpinnerNumberModel( int value, int minimum, int maximum, int stepSize )
  {
    super( value, minimum, maximum, stepSize );
  }
  
  /**
   * Instantiates a new wrapping spinner number model.
   * 
   * @param value the value
   * @param minimum the minimum
   * @param maximum the maximum
   * @param stepSize the step size
   */
  public WrappingSpinnerNumberModel( Number value, Comparable< Number > minimum, Comparable< Number > maximum, Number stepSize )
  {
    super( value, minimum, maximum, stepSize );
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SpinnerNumberModel#getNextValue()
   */
  public Object getNextValue()
  {
    if ( getNumber().equals( getMaximum()))
      return getMinimum();
    return super.getNextValue();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SpinnerNumberModel#getPreviousValue()
   */
  public Object getPreviousValue()
  {
    if ( getNumber().equals( getMinimum()))
      return getMaximum();
    return super.getPreviousValue();
  }
}
