package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class HexRenderer
  extends DefaultTableCellRenderer
{
  public HexRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( Protocol.hex2String(( byte [] )value ));
    else
      super.setValue( value );
  }

  private static DecimalFormat df = null;
}

