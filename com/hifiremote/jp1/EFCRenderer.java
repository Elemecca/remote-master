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
  }

  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( value.toString());
    else
      super.setValue( value );
  }
}

