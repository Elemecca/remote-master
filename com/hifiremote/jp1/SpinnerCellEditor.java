package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class SpinnerCellEditor
  extends AbstractCellEditor
  implements TableCellEditor
{
  public SpinnerCellEditor( int min, int max, int step )
  {
    spinner = new JSpinner( new SpinnerNumberModel( max, min, max, step ));
  }

  public Component getTableCellEditorComponent( JTable table,
                                                Object value,
                                                boolean isSelected,
                                                int row,
                                                int column )
  {
    spinner.setValue( value );
    return spinner;
  }

  public Object getCellEditorValue()
  {
    return spinner.getValue();
  }

  private JSpinner spinner = null;

}

