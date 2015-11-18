package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.hifiremote.jp1.GeneralFunction.RMIcon;

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
  
  private ButtonPanel panel = null;
  
  /** The Constant buttonCol. */
  private static final int buttonCol = 0;
  private static final int deviceCol = 1;
  public static final int functionCol = 2;
  private static final int shiftedCol = 3;
  private static final int xShiftedCol = 4;
  public static final int aliasCol = 5;
  public static final int iconCol = 6;

  
  public void setPanel( ButtonPanel panel )
  {
    this.panel = panel;
  }

  /** The column names. */
  private static String[] columnNames =
  { "Button", "Device", "Function", "", "", "Alias", "Icon?" };
  
  private static final String[] colPrototypeNames =
  { "Btn", "Device", "Function", "Function", "Function", "Alias", "Icon?" };
  
  /** The Constant columnClasses. */
  private static final Class<?>[] columnClasses =
  { Button.class, DeviceButton.class, GeneralFunction.class, Function.class, 
    Function.class, Macro.class, RMIcon.class };

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

  public DeviceUpgrade getDeviceUpgrade()
  {
    return deviceUpgrade;
  }

  /**
   * Sets the buttons.
   */
  public void setButtons()
  {
    if ( deviceUpgrade == null )
      return;
    Remote remote = deviceUpgrade.getRemote();
    this.buttons = remote.getBaseUpgradeButtons();
    columnNames[ shiftedCol ] = remote.getShiftLabel();
    columnNames[ xShiftedCol ] = remote.getXShiftLabel();
    fireTableStructureChanged();

    int cols = getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      int col = getEffectiveColumn( i );
      panel.getTable().setColumnWidth( i, getColumnPrototypeName( i ), col == ButtonTableModel.iconCol, 0 );
    }
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
      if ( remote.usesEZRC() && deviceUpgrade.getRemoteConfig() != null )
      {
        count += 3;   // Adds device, alias and icon columns
      }
    }
    return count;
  }
  
  public int getEffectiveColumn( int col )
  {
    Remote remote = deviceUpgrade.getRemote();
    if ( !( remote.usesEZRC() && deviceUpgrade.getRemoteConfig() != null ) && col > 0 )
    {
      // skip Device column
      col++;
    }
    else if ( remote.usesEZRC() && col > 2 )
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
    
    int rawCol = col;
    col = getEffectiveColumn( col );
    if ( col == buttonCol || col == deviceCol )
      return false;
    
    Remote remote = deviceUpgrade.getRemote();
    Button b = buttons[ row ];
    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();

    if ( b == null )
      return false;
    if ( col == functionCol )
    {
      if ( remote.usesEZRC() )
      {
        GeneralFunction gf = ( GeneralFunction )getValueAt( row, rawCol );
        if ( gf instanceof LearnedSignal )
        {
          LearnedSignal ls = ( LearnedSignal )gf;
          return ls.getKeyCode() != b.getKeyCode();
        }
        else if ( gf instanceof Macro )
        {
          Macro macro = ( Macro )gf;
          return macro.getKeyCode() != b.getKeyCode();
        }
      }
      return ( b.allowsKeyMove() || map.isPresent( b ));
    }
    else if ( col == shiftedCol )
      return b.allowsShiftedKeyMove();
    else if ( col == xShiftedCol )
      return b.allowsXShiftedKeyMove();
    else if ( col == aliasCol || col == iconCol )
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
    col = getEffectiveColumn( col );
    Macro macro = null;
    GeneralFunction gf = null;
    if ( remote.usesEZRC() )
    {
      macro = deviceUpgrade.getMacroMap().get( ( int )button.getKeyCode() );
      LearnedSignal ls = deviceUpgrade.getLearnedMap().get( ( int )button.getKeyCode() );
      if ( ls != null )
      {
        gf = ls;
        macro = null;
      }
      else if ( macro != null )
      {
        if ( macro.isSystemMacro() )
        {
          gf = macro.getItems().get( 0 ).fn;
        }
        else
        {
          gf = macro;
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
        return macro;
      case iconCol:
        Function f = deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
        return macro != null && f != null ? f.icon : null; 
    }
   return null;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    Button button = buttons[ row ];
    Remote remote = deviceUpgrade.getRemote();
    Button relatedButton = null;
    switch ( getEffectiveColumn( col ) )
    {
      case buttonCol:
        break;
      case functionCol:
        GeneralFunction gf = ( GeneralFunction )value;
        GeneralFunction current = ( GeneralFunction )getValueAt( row, col );
        setFunction( deviceUpgrade, button, current, gf, panel );
        if ( current instanceof LearnedSignal && gf == null )
        {
          gf = ( GeneralFunction )getValueAt( row, col );
          setFunction( deviceUpgrade, button, null, gf, panel );
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
        if ( remote.isSoftButton( button ) )
        {
          Function f = deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
          f.setName( ( String )value );
        }
        break;
      case iconCol:
        if ( remote.isSoftButton( button ) )
        {
          Function f = deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
          f.icon = ( RMIcon )value;
        }
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
  
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ getEffectiveColumn( col ) ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return columnClasses[ getEffectiveColumn( col ) ];
  }
  
  public static void setFunction( DeviceUpgrade deviceUpgrade, Button button, GeneralFunction old, 
      GeneralFunction gf, KMPanel panel )
  {
    DeviceButton db = deviceUpgrade.getButtonRestriction();
    FunctionLabel label = null;
    if ( gf == null )
    {
      deviceUpgrade.setFunction( button, null, Button.NORMAL_STATE );
      if ( old != null && ( label = old.getLabel() ) != null )
      {
        label.showAssigned( db );
        label.updateToolTipText();
      }
    }
    else if ( gf instanceof Function || gf instanceof Macro )
    {
      deviceUpgrade.setFunction( button, gf, Button.NORMAL_STATE );
    }
    else if ( gf instanceof LearnedSignal )
    {
      // This case should not occur, as learned signals cannot be assigned through the
      // button and layout panels.
      LearnedSignal ls = ( LearnedSignal )gf;
      deviceUpgrade.getLearnedMap().put( ( int )button.getKeyCode(), ls );
      ls.addReference( db, button );
    }
    
    if ( gf != null && ( label = gf.getLabel() ) != null )
    {
      label.showAssigned( db );
      label.updateToolTipText();
    }
  }
}

