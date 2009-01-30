package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SpinnerCellEditor.
 */
public class SpinnerCellEditor
  extends AbstractCellEditor
  implements TableCellEditor
{
  
  /**
   * Instantiates a new spinner cell editor.
   * 
   * @param min the min
   * @param max the max
   * @param step the step
   */
  public SpinnerCellEditor( int min, int max, int step )
  {
    spinner = new JSpinner( new SpinnerNumberModel( max, min, max, step ));
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
   */
  public Component getTableCellEditorComponent( JTable table,
                                                Object value,
                                                boolean isSelected,
                                                int row,
                                                int column )
  {
    spinner.setValue( value );
    return spinner;
  }

  /* (non-Javadoc)
   * @see javax.swing.CellEditor#getCellEditorValue()
   */
  public Object getCellEditorValue()
  {
    return spinner.getValue();
  }

  /** The spinner. */
  private JSpinner spinner = null;

}

