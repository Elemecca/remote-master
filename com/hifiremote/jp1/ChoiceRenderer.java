package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ChoiceRenderer
  extends DefaultTableCellRenderer
{
  public ChoiceRenderer( Choice[] choices )
  {
    this.choices = choices;
  }

  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col )
  {
    String val = null;
    if ( value != null )
    {
      int temp = (( Integer ) value ).intValue();
      for ( int i = 0; i < choices.length; i++ )
      {
        if ( choices[ i ].getIndex() == temp )
        {
          val = choices[ i ].getText();
          break;
        }
      }
    }
    return super.getTableCellRendererComponent( table, val, isSelected, hasFocus, row, col );
  }

  private Choice[] choices = null;
}
