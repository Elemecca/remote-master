package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class FunctionRenderer
  extends DefaultTableCellRenderer
{
  public FunctionRenderer( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
    setToolTipText( "Drag or double-click a function to set the functions for a button, or use the popup menu of available functions." );

  }

  public Component  getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col )
  {
    Button b = ( Button )value;
    String temp = null;
    JTextField tf = new JTextField();
    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();
    if ( col == 0 )
    {
      if (( b.getFunction() == null ) && 
          ( b.getShiftedFunction() == null ) && 
          ( b.getXShiftedFunction() == null ))
        setForeground( Color.red );
      else
        setForeground( Color.black );

      temp = b.getName();
      if (( map == null ) || !map.isPresent( b ))
        temp = temp + '*';
    }
    else
    {
      Function f = null;
    
      if ( col == 1 )
      {
        f = b.getFunction();
        if ( !b.allowsKeyMove() && !map.isPresent( b ))
          tf.setEditable( false );
      }
      else if ( col == 2 )
      {
        f = b.getShiftedFunction();
        if ( !b.allowsShiftedKeyMove())
          tf.setEditable( false );
      }
      else if ( col == 3 )
      {
        f = b.getXShiftedFunction();
        if ( !b.allowsXShiftedKeyMove())
          tf.setEditable( false );
      }

      if ( f != null )
      {
        temp = f.getName();
        setToolTipText( f.getNotes());
      }
      else
        setToolTipText( null );
    }
    setBackground( tf.getBackground());
    JComponent c = ( JComponent )super.getTableCellRendererComponent( table, temp,
                                                                      isSelected,
                                                                      hasFocus,
                                                                      row, col );

    return c;
  }
  private DeviceUpgrade deviceUpgrade = null;
}
