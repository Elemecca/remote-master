package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class KeyMoveRenderer
  extends DefaultTableCellRenderer
{
  public KeyMoveRenderer()
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
    KeyMove keyMove = ( KeyMove )value;
    String temp = null;
    if ( col == 5 )
    {
      temp = keyMove.getData().toString();
      if ( keyMove instanceof KeyMoveKey )
        temp += " (keycode)";
    }
    else if ( col == 6 )
    {
      Hex hex = keyMove.getCmd();
      if ( hex != null )
        temp = hex.toString();
    }
    else if ( col == 7 )
      temp = keyMove.getValueString( remote );
    return super.getTableCellRendererComponent( table, temp, isSelected, hasFocus, row, col );
  }
  
  private Remote remote = null;
}
