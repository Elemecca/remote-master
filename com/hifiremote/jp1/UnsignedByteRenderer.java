package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import java.awt.color.*;
import javax.swing.*;
import javax.swing.table.*;

public class UnsignedByteRenderer
  extends DefaultTableCellRenderer
{
  public UnsignedByteRenderer()
  {
    baseFont = getFont();
    boldFont = baseFont.deriveFont( Font.BOLD );
  }
  
  public void setSavedData( short[] savedData )
  {
    this.savedData = savedData;
  }

  public Component getTableCellRendererComponent( JTable table, Object value, 
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int col )
  {
    Component c = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
    if ((( savedData != null ) && ((( UnsignedByte )value ).getValue()) != savedData[ 16 * row + col - 1 ]))
    {
      if ( isSelected )
        c.setForeground( Color.YELLOW );
      else
        c.setForeground( Color.RED );
      c.setFont( boldFont );
    }
    else
    {
      if ( isSelected )
        c.setForeground( Color.WHITE );
      else
        c.setForeground( Color.BLACK );
      c.setFont( baseFont );
    }
    return c;
  }

  private short[] savedData = null;
  private Font baseFont = null;
  private Font boldFont = null;
}
