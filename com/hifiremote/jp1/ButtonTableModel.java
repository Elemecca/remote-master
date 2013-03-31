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
  private static final int deviceCol = 1;
  private static final int functionCol = 2;
  private static final int shiftedCol = 3;
  private static final int xShiftedCol = 4;
  private static final int aliasCol = 5;

  /** The column names. */
  private static String[] columnNames =
  { "Button", "Device", "Function", "", "", "Alias" };
  
  /** The Constant columnClasses. */
  private static final Class<?>[] columnClasses =
  { Button.class, DeviceButton.class, GeneralFunction.class, Function.class, 
    Function.class, Macro.class };

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
      if ( remote.isSSD() )
      {
        count += 2;   // Adds device and alias columns
      }
    }
    return count;
  }
  
  public int getEffectiveColumn( int col )
  {
    Remote remote = deviceUpgrade.getRemote();
    if ( !remote.isSSD() && col > 0 )
    {
      col++;
    }
    else if ( remote.isSSD() && col > 2 )
    {
      col += 2;
    }
    return col;
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
    
    col = getEffectiveColumn( col );
    if ( col == buttonCol || col == deviceCol )
      return false;
    
    Button b = buttons[ row ];
    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();

    if ( b == null )
      return false;
    if ( col == functionCol )
      return ( b.allowsKeyMove() || map.isPresent( b ));
    else if ( col == shiftedCol )
      return b.allowsShiftedKeyMove();
    else if ( col == xShiftedCol )
      return b.allowsXShiftedKeyMove();
    else if ( col == aliasCol )
      return getValueAt( row, 3 ) != null;
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
    Macro macro = null;
    col = getEffectiveColumn( col );
    GeneralFunction gf = null;
    if ( remote.isSSD() )
    {
      LearnedSignal ls = deviceUpgrade.getLearnedMap().get( ( int )button.getKeyCode() );
      if ( ls != null )
      {
        gf = ls;
      }
      macro = deviceUpgrade.getMacroMap().get( ( int )button.getKeyCode() );
      if ( gf == null && macro != null )
      {
        gf = macro.isSystemMacro() ? macro.getItems().get( 0 ).fn : macro;
        if ( !macro.isSystemMacro() )
        {
          macro = null;
        }
      }
    }
    if ( gf == null )
    {
      gf = deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
    }
    
    switch ( col )
    {
      case buttonCol:
        return button;
      case deviceCol:
        if ( gf == null )
        {
          return null;
        }
        else if ( macro != null )
        {
          return macro.getItems().get( 0 ).db;
        }
        return gf == null ? null : gf.getUpgrade( remote ).getButtonRestriction();
      case functionCol:
        return gf;
      case shiftedCol:
        return deviceUpgrade.getFunction( button, Button.SHIFTED_STATE );
      case xShiftedCol:
        return deviceUpgrade.getFunction( button, Button.XSHIFTED_STATE );
      case aliasCol:
        return macro;// == null ? null : macro.getName();
    }
   return null;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    DeviceButton db = deviceUpgrade.getButtonRestriction();
    Button button = buttons[ row ];
    Button relatedButton = null;
    col = getEffectiveColumn( col );
    switch ( col )
    {
      case buttonCol:
        break;
      case functionCol:
        GeneralFunction gf = ( GeneralFunction )value;
        GeneralFunction current = ( GeneralFunction )getValueAt( row, col );
        setFunction( deviceUpgrade, button, current, gf );
        if ( current instanceof LearnedSignal && gf == null )
        {
          gf = ( GeneralFunction )getValueAt( row, col );
          setFunction( deviceUpgrade, button, null, gf );
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
        break;
      case aliasCol:
        Macro macro = ( Macro )getValueAt( row, 3 );
        macro.setName( ( String )value );
        break;
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
    return columnNames[ getEffectiveColumn( col ) ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return columnClasses[ getEffectiveColumn( col ) ];
  }
  
  public static void setFunction( DeviceUpgrade deviceUpgrade, Button button, GeneralFunction old, GeneralFunction gf )
  {
    // A new value always becomes active so delete the reference to the
    // current value, but delete the current value itself only if the new
    // value is not a learned signal, as a learned signal sits on top of
    // the current value, hiding it but gets reinstated if the learned
    // signal is deleted

    DeviceButton db = deviceUpgrade.getButtonRestriction();
    if ( old instanceof Function && gf == null )
    {
      // Deletion of old reference is performed by setFunction()
      deviceUpgrade.setFunction( button, null, Button.NORMAL_STATE );
    }
    else if ( old instanceof Macro && gf == null )
    {
//      if ( !( gf instanceof LearnedSignal ) )
//      {
        deviceUpgrade.getMacroMap().remove( ( int )button.getKeyCode() );
//      }
      ( ( Macro )old ).removeReference( db, button );
    }
    else if ( old instanceof LearnedSignal && gf == null )
    {
      deviceUpgrade.getLearnedMap().remove( ( int )button.getKeyCode() );
      ( ( LearnedSignal )old ).removeReference( db, button );
      // Deleting a learned signal reinstates the value underneath it,
      // whose reference will have been deleted, so reset it
//      if ( gf == null )
//      {
//        gf = ( GeneralFunction )getValueAt( row, col );
//      }
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
      macro.addReference( db, button );
    }
    else if ( gf instanceof LearnedSignal )
    {
      LearnedSignal ls = ( LearnedSignal )gf;
      deviceUpgrade.getLearnedMap().put( ( int )button.getKeyCode(), ls );
      ls.addReference( db, button );
    }
  }
}

