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
    if ( col == 0 )
    {
      if (( b.getFunction() == null ) && ( b.getShiftedFunction() == null ))
        setForeground( Color.red );
      else
        setForeground( Color.black );

      temp = b.getName();
      DeviceType devType = deviceUpgrade.getDeviceType();
      ButtonMap map = devType.getButtonMap();
      if (( map == null ) || !map.isPresent( b ))
        temp = temp + '*';
    }
    else
    {
      Function f = null;
    
      if ( col == 1 )
        f = b.getFunction();

      else if ( col == 2 )
      {
        f = b.getShiftedFunction();
        if ( !b.allowsShift())
        {
          tf.setEditable( false );
        }
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
//    if ( f != null )
//      c.setToolTipText( f.getNotes());
//    else
//      c.setToolTipText( "" );

    return c;
  }
  private DeviceUpgrade deviceUpgrade = null;
}
