package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ChoiceCmdParm
  extends CmdParameter
{
  public ChoiceCmdParm( String name, Integer defaultValue, String[] textChoices )
  {
    super( name, defaultValue );
    choices = new Choice[ textChoices.length ];
    for ( int i = 0; i < choices.length; i++ )
    {
      choices[ i ] = new Choice( i, textChoices[ i ] );
    }

    editor = new ChoiceEditor( choices );
    renderer = new ChoiceRenderer( choices );
    renderer.setHorizontalAlignment( SwingConstants.CENTER );
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

  public Integer getValue()
  {
    return null;
  }

  public void setValue( Integer value )
  {
  }

  public Choice[] getChoices(){ return choices; }

  private DefaultCellEditor editor = null;
  private DefaultTableCellRenderer renderer = null;
  private Choice[] choices = null;
}
