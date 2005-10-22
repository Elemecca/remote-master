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
      Class c = value.getClass();
      if ( c == Integer.class )
        val = choices[ (( Integer )value ).intValue()].toString();
      else if ( c == Choice.class )
        val = (( Choice )value ).getText();
    }

    return super.getTableCellRendererComponent( table, val, isSelected, hasFocus, row, col );
  }

  private Choice[] choices = null;
}
