package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonTableModel.
 */
public class ButtonTableModel
  extends AbstractTableModel
{
  
  /** The buttons. */
  private Button[] buttons = null;
  
  /** The device upgrade. */
  private DeviceUpgrade deviceUpgrade = null;
  
  /** The Constant buttonCol. */
  private static final int buttonCol = 0;
  
  /** The Constant functionCol. */
  private static final int functionCol = 1;
  
  /** The Constant shiftedCol. */
  private static final int shiftedCol = 2;
  
  /** The Constant xShiftedCol. */
  private static final int xShiftedCol = 3;

  /** The column names. */
  private static String[] columnNames =
  { "Button", "Function", "", "" };
  
  /** The Constant columnClasses. */
  private static final Class<?>[] columnClasses =
  { Button.class, Function.class, Function.class, Function.class };

  /**
   * Instantiates a new button table model.
   * 
   * @param deviceUpgrade the device upgrade
   */
  public ButtonTableModel( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
  }
  
  /**
   * Sets the device upgrade.
   * 
   * @param deviceUpgrade the new device upgrade
   */
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    this.deviceUpgrade = deviceUpgrade;
    fireTableDataChanged();
  }

  /**
   * Sets the buttons.
   */
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

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    if ( buttons == null )
      return 0;
    else
      return buttons.length;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
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
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    if ( deviceUpgrade == null )
      return false;
    
    if ( row < 0 )
      return false;
    
    if ( col == buttonCol )
      return false;
    
    Button b = buttons[ row ];
    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();

    if ( b == null )
      return false;
    if ( col == 1 )
      return ( b.allowsKeyMove() || map.isPresent( b ));
    else if ( col == 2 )
      return b.allowsShiftedKeyMove();
    else if ( col == 3 )
      return b.allowsXShiftedKeyMove();
    return false;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
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

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
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

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return columnNames[ col ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return columnClasses[ col ];
  }
}

