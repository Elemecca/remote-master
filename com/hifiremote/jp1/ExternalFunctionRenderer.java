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
      byte[] b = f.getHex();
      if ( b == null )
        rc = "";
      else if ( f.getType() == ExternalFunction.EFCType )
        rc = df.format( f.getEFC().intValue());
      else
        rc = Protocol.hex2String( b );
    }

    super.setValue( rc );
  }

  private static final DecimalFormat df = new DecimalFormat( "000" );
}

