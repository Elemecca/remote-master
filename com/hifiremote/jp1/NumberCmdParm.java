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
    this( name, defaultValue, 0, (( 1 << bits ) - 1 ));
  }

  public NumberCmdParm( String name, DefaultValue defaultValue, int min, int max )
  {
    super( name, defaultValue );
    editor = new ByteEditor( min, max );
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

  private ByteEditor editor;
  private ByteRenderer renderer;
}
