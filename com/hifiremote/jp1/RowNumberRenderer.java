package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import java.awt.color.*;
import javax.swing.*;
import javax.swing.table.*;

public class RowNumberRenderer
  extends DefaultTableCellRenderer
{
  public RowNumberRenderer()
  {
    JButton b = new JButton();
    setBackground( b.getBackground());
    BorderFactory.createBevelBorder( BevelBorder.RAISED );
    setHorizontalAlignment( SwingConstants.CENTER );
    setToolTipText( "Drag a row up or down to change the order." );
  }

  public Component getTableCellRendererComponent( JTable table, Object value, 
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int col )
  {
    return super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
  } 
}
