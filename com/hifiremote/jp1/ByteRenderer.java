package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class ByteRenderer
  extends DefaultTableCellRenderer
{
  public ByteRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  protected void setValue( Object value )
  {
    if ( value != null )
//      super.setValue( Integer.toString( Translate.byte2int((( Integer )value ).byteValue())));
      super.setValue((( Integer )value ).toString());
    else
      super.setValue( value );
  }
}

