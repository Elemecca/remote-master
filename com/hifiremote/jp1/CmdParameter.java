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

  public CmdParameter( String name, Integer defaultValue )
  {
    super( name, defaultValue );
  }

  public abstract TableCellEditor getEditor();
  public abstract TableCellRenderer getRenderer();
  public abstract Class getValueClass();
}
