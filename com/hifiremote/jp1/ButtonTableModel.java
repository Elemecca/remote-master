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
  { Button.class, Button.class, Button.class, Button.class };

  public ButtonTableModel( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
  }

  public void setButtons()
  {
    System.err.println( "ButtonTableModel.setButtons()" );
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
    System.err.println( "ButtonTable.getColumnCount()" );
    Remote remote = deviceUpgrade.getRemote();
    if (( remote != null ) && remote.getXShiftEnabled())
      return 4;
    else
      return 3;
  }

  public Object getValueAt( int row, int col )
  {
    Button button = buttons[ row ];
    return button;
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
        button.setFunction(( Function )value );
        relatedButton = button.getBaseButton();
        break;
      case shiftedCol:
        button.setShiftedFunction(( Function )value );
        relatedButton = button.getShiftedButton();
        break;
      case xShiftedCol:
        button.setXShiftedFunction(( Function )value );
        relatedButton = button.getXShiftedButton();
      default:
        break;
    }
    int otherRow = row;
    if ( relatedButton != null )
    {
      for ( int i = 0; i < buttons.length; i++ )
        if ( buttons[ i ] == relatedButton )
        {
          otherRow = i;
          break;
        }
      
    }
    if ( row < otherRow )
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

