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

  public boolean verify( JComponent input )
  {
    System.err.println( "IntVerifier.verify(), allowNull=" + allowNull );
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
      int value = Integer.parseInt( text );
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
    System.err.println( "IntVerifier.shouldYieldFocus()" );
    boolean rc = verify( c );
    if ( !rc )
      KeyMapMaster.showMessage( "The value must be between " + min + " and " + max );
    else
      KeyMapMaster.clearMessage();
    return rc;
  }

  private int min = 0;
  private int max = 0;
  private boolean allowNull = false;
}
