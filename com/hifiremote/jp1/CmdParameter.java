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

  public Object getValue( Object value )
  {
    if (( defaultValue != null ) && defaultValue.value().equals( value ))
      return null;
    else
      return value;
  }
  public Object convertValue( Object value )
  { 
    if (( defaultValue != null ) && ( value == null ))
      return defaultValue.value();
    return value; 
  }
  public abstract TableCellEditor getEditor();
  public abstract TableCellRenderer getRenderer();
  public abstract Class getValueClass();
}
