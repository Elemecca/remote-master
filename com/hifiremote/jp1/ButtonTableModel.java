package com.hifiremote.jp1;

import javax.swing.table.*;
import java.util.*;

public class ButtonTableModel
  extends AbstractTableModel
{
  private Button[] buttons = null;
  private DeviceUpgrade deviceUpgrade = null;
  private static final int buttonCol = 0;
  private static final int functionCol = 1;
  private static final int shiftedCol = 2;
  private static final int xShiftedCol = 3;

  private static String[] columnNames =
  { "Button", "Function", "", "" };
  private static final Class[] columnClasses =
  { Button.class, Function.class, Function.class, Function.class };

  public ButtonTableModel( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
  }
  
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
    fireTableDataChanged();
  }

  public void setButtons()
  {
    if ( deviceUpgrade == null )
      return;
    Remote remote = deviceUpgrade.getRemote();
    this.buttons = remote.getUpgradeButtons();
    columnNames[ shiftedCol ] = remote.getShiftLabel();
    columnNames[ xShiftedCol ] = remote.getXShiftLabel();
    fireTableStructureChanged();
  }

  public int getRowCount()
  {
    if ( buttons == null )
      return 0;
    else
      return buttons.length;
  }

  public int getColumnCount()
  {
    if ( deviceUpgrade == null )
      return 3;
    Remote remote = deviceUpgrade.getRemote();
    if (( remote != null ) && remote.getXShiftEnabled())
      return 4;
    else
      return 3;
  }
  
  public boolean isCellEditable( int row, int col )
  {
    if ( col != buttonCol )
      return true;
    return false;
  }

  public Object getValueAt( int row, int col )
  {
    if ( row < 0 )
      return null;
    Button button = buttons[ row ];
    switch ( col )
    {
      case buttonCol:
        return button;
      case functionCol:
        return deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
      case shiftedCol:
        return deviceUpgrade.getFunction( button, Button.SHIFTED_STATE );
      case xShiftedCol:
        return deviceUpgrade.getFunction( button, Button.XSHIFTED_STATE );
    }
   return null;
  }

  public void setValueAt( Object value, int row, int col )
  {
    Button button = buttons[ row ];
    Button relatedButton = null;
    switch ( col )
    {
      case buttonCol:
        break;
      case functionCol:
        deviceUpgrade.setFunction( button, ( Function )value, Button.NORMAL_STATE );
        relatedButton = button.getBaseButton();
        break;
      case shiftedCol:
        deviceUpgrade.setFunction( button, ( Function )value, Button.SHIFTED_STATE );
        relatedButton = button.getShiftedButton();
        break;
      case xShiftedCol:
        deviceUpgrade.setFunction( button, ( Function )value, Button.XSHIFTED_STATE );
        relatedButton = button.getXShiftedButton();
      default:
        break;
    }
    int otherRow = row;
    if ( relatedButton != null )
    {
      for ( int i = 0; i < buttons.length; i++ )
      {
        if ( buttons[ i ] == relatedButton )
        {
          otherRow = i;
          break;
        }
      }
    }
    if ( row <= otherRow )
      fireTableRowsUpdated( row, otherRow );
    else
      fireTableRowsUpdated( otherRow, row );
  }

  public String getColumnName( int col )
  {
    return columnNames[ col ];
  }

  public Class getColumnClass( int col )
  {
    return columnClasses[ col ];
  }
}

