package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.text.*;

public class IntVerifier
  extends InputVerifier
{
  public IntVerifier( int min, int max )
  {
    this( min, max, false );
  }
  public IntVerifier( int min, int max, boolean allowNull )
  {
    this.min = min;
    this.max = max;
    this.allowNull = allowNull;
  }

  public void setBase( int base )
  {
    this.base = base;
  }

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

  private int min = 0;
  private int max = 0;
  private int base = 10;
  private boolean allowNull = false;
}
