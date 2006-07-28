package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MacroRenderer
  extends DefaultTableCellRenderer
{
  public MacroRenderer()
  {
  }
  
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  
  public Component  getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col )
  {
    return super.getTableCellRendererComponent( table, (( Macro )value ).getValueString( remote ), isSelected, hasFocus, row, col );
  }
  
  private Remote remote = null;
}
