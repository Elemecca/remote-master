package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class FunctionRenderer
  extends DefaultTableCellRenderer
{
  public Component  getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col )
  {
    Function f = ( Function )value;
    String str = null;
    if ( f != null )
      str = f.getName();
    JComponent c = ( JComponent )super.getTableCellRendererComponent( table, str,
                                                                      isSelected,
                                                                      hasFocus,
                                                                      row, col );
    if ( f != null )
      c.setToolTipText( f.getNotes());
    else
      c.setToolTipText( "" );
    return c;
  }
}
