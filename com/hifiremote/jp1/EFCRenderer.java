package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class EFCRenderer
  extends DefaultTableCellRenderer
{
  public EFCRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
    if ( df == null )
      df = new DecimalFormat( "000" );
  }

  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( df.format((( Integer )value ).intValue() & 0xFF ));
    else
      super.setValue( value );
  }

  private static DecimalFormat df = null;
}

