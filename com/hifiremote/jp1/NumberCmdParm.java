package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class NumberCmdParm
  extends CmdParameter
{
  public NumberCmdParm( String name, DefaultValue defaultValue )
  {
    this( name, defaultValue, 8 );
  }

  public NumberCmdParm( String name, DefaultValue defaultValue, int bits )
  {
    super( name, defaultValue );
    this.bits = bits;
    editor = new ByteEditor(  0, (( 1 << bits ) - 1 ));
    renderer = new ByteRenderer();
  }

  public TableCellEditor getEditor()
  {
    return editor;
  }

  public TableCellRenderer getRenderer()
  {
    return renderer;
  }

  public Class getValueClass()
  {
    return Integer.class;
  }

  public void setValue( Object value )
  {}

  public Object getValue()
  {
    return null;
  }

  public Object convertValue( Object value )
  {
    Object rc = null;
    Class c = value.getClass();
    if ( c == Integer.class )
      rc = value;
    else // assume String
      rc = new Integer(( String )value );
    return rc;
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append( name );
    if ( bits != 8 )
    {
      buff.append( ':' );
      buff.append( bits );
    }
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue );
    }
    return buff.toString();
  }

  private ByteEditor editor;
  private ByteRenderer renderer;
  private int bits = 8;
}
