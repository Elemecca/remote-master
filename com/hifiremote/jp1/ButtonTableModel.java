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
  { Button.class, GeneralFunction.class, Function.class, Function.class };

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
    int count = 3;
    if ( deviceUpgrade != null )
    {
      Remote remote = deviceUpgrade.getRemote();
      if (( remote != null ) && remote.getXShiftEnabled())
      {
        count++;
      }
      if (( remote != null ) && !remote.getShiftEnabled())
      {
        count--;
      }
    }
    return count;
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
    Remote remote = deviceUpgrade.getRemote();
    switch ( col )
    {
      case buttonCol:
        return button;
      case functionCol:
        if ( remote.isSSD() )
        {
          LearnedSignal ls = deviceUpgrade.getLearnedMap().get( ( int )button.getKeyCode() );
          if ( ls != null )
          {
            return ls;
          }
          Macro macro = deviceUpgrade.getMacroMap().get( ( int )button.getKeyCode() );
          if ( macro != null )
          {
            return macro;
          }
          KeyMove km = deviceUpgrade.getKmMap().get( ( int )button.getKeyCode() );
          if ( km != null )
          {
            return km;
          }
        }
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
        GeneralFunction gf = ( GeneralFunction )value;
        Object current = getValueAt( row, col );

        // A new value always becomes active so delete the reference to the
        // current value, but delete the current value itself only if the new
        // value is not a learned signal, as a learned signal sits on top of
        // the current value, hiding it but gets reinstated if the learned
        // signal is deleted

        if ( current instanceof Function )
        {
          if ( !( gf instanceof LearnedSignal ) )
          {
            deviceUpgrade.setFunction( button, null, Button.NORMAL_STATE );
          }
          ( ( Function )current ).removeReference( button );
        }
        else if ( current instanceof Macro )
        {
          if ( !( gf instanceof LearnedSignal ) )
          {
            deviceUpgrade.getMacroMap().remove( ( int )button.getKeyCode() );
          }
          ( ( Macro )current ).removeReference( button );
        }
        else if ( current instanceof KeyMove )
        {
          if ( !( gf instanceof LearnedSignal ) )
          {
            deviceUpgrade.getKmMap().remove( ( int )button.getKeyCode() );
          }
          ( ( KeyMove )current ).removeReference( button );
        }
        else if ( current instanceof LearnedSignal )
        {
          deviceUpgrade.getLearnedMap().remove( ( int )button.getKeyCode() );
          ( ( LearnedSignal )current ).removeReference( button );
          // Deleting a learned signal reinstates the value underneath it,
          // whose reference will have been deleted, so reset it
          if ( gf == null )
          {
            gf = ( GeneralFunction )getValueAt( row, col );
          }
        }

        if ( gf instanceof Function )
        {
          Function f = ( Function )gf;
          deviceUpgrade.setFunction( button, f, Button.NORMAL_STATE );
        }
        else if ( gf instanceof Macro )
        {
          Macro macro = ( Macro )gf;
          deviceUpgrade.getMacroMap().put( ( int )button.getKeyCode(), macro );
          macro.addReference( button );
        }
        else if ( gf instanceof KeyMove )
        {
          KeyMove km = ( KeyMove )gf;
          deviceUpgrade.getKmMap().put( ( int )button.getKeyCode(), km );
          km.addReference( button );
        }
        else if ( gf instanceof LearnedSignal )
        {
          LearnedSignal ls = ( LearnedSignal )gf;
          deviceUpgrade.getLearnedMap().put( ( int )button.getKeyCode(), ls );
          ls.addReference( button );
        }
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

