package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class CmdParameter
  extends Parameter
{
  public CmdParameter( String name )
  {
    super( name );
  }

  public CmdParameter( String name, DefaultValue defaultValue )
  {
    super( name, defaultValue );
  }

  public Object getValue( Object value ){ return value; }
  public Object convertValue( Object value ){ return value; }
  public abstract TableCellEditor getEditor();
  public abstract TableCellRenderer getRenderer();
  public abstract Class getValueClass();
}
