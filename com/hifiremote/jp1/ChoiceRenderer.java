package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class ChoiceRenderer.
 */
public class ChoiceRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new choice renderer.
   * 
   * @param choices the choices
   */
  public ChoiceRenderer( Choice[] choices )
  {
    this.choices = choices;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col )
  {
    String val = null;
    if ( value != null )
    {
      Class<?> c = value.getClass();
      if ( c == Integer.class )
        val = choices[ (( Integer )value ).intValue()].toString();
      else if ( c == Choice.class )
        val = (( Choice )value ).getText();
    }

    return super.getTableCellRendererComponent( table, val, isSelected, hasFocus, row, col );
  }

  /** The choices. */
  private Choice[] choices = null;
}
