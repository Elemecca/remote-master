package com.hifiremote.jp1;

import javax.swing.*;

public class WrappingSpinnerNumberModel
  extends SpinnerNumberModel
{
  public WrappingSpinnerNumberModel()
  {
    super();
  }
  
  public WrappingSpinnerNumberModel( double value, double minimum, double maximum, double stepSize )
  {
   super( value, minimum, maximum, stepSize );
  }
  
  public WrappingSpinnerNumberModel( int value, int minimum, int maximum, int stepSize )
  {
    super( value, minimum, maximum, stepSize );
  }
  
  public WrappingSpinnerNumberModel( Number value, Comparable minimum, Comparable maximum, Number stepSize )
  {
    super( value, minimum, maximum, stepSize );
  }
  
  public Object getNextValue()
  {
    if ( getNumber().equals( getMaximum()))
      return getMinimum();
    return super.getNextValue();
  }
  
  public Object getPreviousValue()
  {
    if ( getNumber().equals( getMinimum()))
      return getMaximum();
    return super.getPreviousValue();
  }
}
