package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class NumberCmdParm
  extends CmdParameter
{
  public NumberCmdParm( String name, Integer defaultValue )
  {
    this( name, defaultValue, 8 );
  }

  public NumberCmdParm( String name, Integer defaultValue, int bits )
  {
    this( name, defaultValue, 0, (( 1 << bits ) - 1 ));
  }

  public NumberCmdParm( String name, Integer defaultValue, int min, int max )
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

  public void setValue( Integer value )
  {}

  public Integer getValue()
  {
    return null;
  }

  private ByteEditor editor;
  private ByteRenderer renderer;
}
