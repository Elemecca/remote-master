package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class ExternalFunctionRenderer
  extends DefaultTableCellRenderer
{
  public ExternalFunctionRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  protected void setValue( Object value )
  {
    Object rc = null;
    if ( value != null )
    {
      ExternalFunction f = ( ExternalFunction )value;
      rc = f.toString();
    }

    super.setValue( rc );
  }

  private static final DecimalFormat df = new DecimalFormat( "000" );
}

