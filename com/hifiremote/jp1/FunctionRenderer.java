package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

// TODO: Auto-generated Javadoc
/**
 * The Class FunctionRenderer.
 */
public class FunctionRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new function renderer.
   * 
   * @param deviceUpgrade the device upgrade
   */
  public FunctionRenderer( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
    setToolTipText( "Drag or double-click a function to set the functions for a button, or use the popup menu of available functions." );

  }
  
  /**
   * Sets the device upgrade.
   * 
   * @param deviceUpgrade the new device upgrade
   */
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component  getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col )
  {
    Button b = ( Button )value;
    String temp = null;
    JTextField tf = new JTextField();
    if ( deviceUpgrade == null )
      return null;
    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();
    if ( col == 0 )
    {
      boolean unassigned = ( deviceUpgrade.getFunction( b, Button.NORMAL_STATE ) == null ) && 
          ( deviceUpgrade.getFunction( b, Button.SHIFTED_STATE ) == null ) && 
          ( deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE ) == null );
      if ( deviceUpgrade.getRemote().usesEZRC() )
      {
        int keyCode = b.getKeyCode();
        unassigned = unassigned && deviceUpgrade.getKmMap().get( keyCode ) == null
            && deviceUpgrade.getMacroMap().get( keyCode ) == null
            && deviceUpgrade.getLearnedMap().get( keyCode ) == null;
      }
      if ( unassigned )
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
        f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
        if ( !b.allowsKeyMove() && !map.isPresent( b ))
          tf.setEditable( false );
      }
      else if ( col == 2 )
      {
        f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
        if ( !b.allowsShiftedKeyMove())
          tf.setEditable( false );
      }
      else if ( col == 3 )
      {
        f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );
        if ( !b.allowsXShiftedKeyMove())
          tf.setEditable( false );
      }

      if ( f != null )
        temp = f.getName();
    }
    setBackground( tf.getBackground());
    JComponent c = ( JComponent )super.getTableCellRendererComponent( table, temp,
                                                                      isSelected,
                                                                      hasFocus,
                                                                      row, col );

    return c;
  }
  
  /** The device upgrade. */
  private DeviceUpgrade deviceUpgrade = null;
}
