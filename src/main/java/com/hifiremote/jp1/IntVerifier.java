package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.text.*;

// TODO: Auto-generated Javadoc
/**
 * The Class IntVerifier.
 */
public class IntVerifier
  extends InputVerifier
{
  
  /**
   * Instantiates a new int verifier.
   * 
   * @param min the min
   * @param max the max
   */
  public IntVerifier( int min, int max )
  {
    this( min, max, false );
  }
  
  /**
   * Instantiates a new int verifier.
   * 
   * @param min the min
   * @param max the max
   * @param allowNull the allow null
   */
  public IntVerifier( int min, int max, boolean allowNull )
  {
    this.min = min;
    this.max = max;
    this.allowNull = allowNull;
  }

  /**
   * Sets the base.
   * 
   * @param base the new base
   */
  public void setBase( int base )
  {
    this.base = base;
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
   * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
   */
  public boolean verify( JComponent input )
  {
    JTextComponent tc = ( JTextComponent )input;
    String text = tc.getText();
    if (( text == null ) || ( text.length() == 0 ))
    {
      if ( allowNull )
        return true;
      else
        return false;
    }
    try
    {
      int value = Integer.parseInt( text, base );
      if (( value < min ) || ( value > max ))
        return false;
      else
        return true;
    }
    catch ( NumberFormatException e )
    {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.InputVerifier#shouldYieldFocus(javax.swing.JComponent)
   */
  public boolean shouldYieldFocus( JComponent c )
  {
    boolean rc = verify( c );
    if ( !rc )
    {
      String minStr = Integer.toString( min, base );
      String maxStr = Integer.toString( max, base );
      KeyMapMaster.showMessage( "The value must be between " + minStr + " and " + maxStr, c );
    }
    else
      KeyMapMaster.clearMessage( c );
    return rc;
  }

  /** The min. */
  private int min = 0;
  
  /** The max. */
  private int max = 0;
  
  /** The base. */
  private int base = 10;
  
  /** The allow null. */
  private boolean allowNull = false;
}
