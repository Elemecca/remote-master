package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class ByteRenderer
  extends DefaultTableCellRenderer
{
  public ByteRenderer()
  {
    this( 10 );
  }

  public ByteRenderer( int base )
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( Integer.toString((( Integer )value ).intValue(), base ));
    else
      super.setValue( value );
  }

  public void setBase( int base )
  {
    this.base = base;
  }

  public int getBase(){ return base; }

  int base = 10;
}

