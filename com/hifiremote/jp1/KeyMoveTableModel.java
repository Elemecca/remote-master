package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMoveTableModel.
 */
public class KeyMoveTableModel extends JP1TableModel< KeyMove >
{

  /**
   * Instantiates a new key move table model.
   */
  public KeyMoveTableModel()
  {}

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();

    setData( remoteConfig.getKeyMoves() );
    deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    keyRenderer.setRemote( remote );
    keyEditor.setRemote( remote );
    deviceTypeBox.setModel( new DefaultComboBoxModel( remote.getDeviceTypes() ) );
  }

  /**
   * Gets the remote config.
   * 
   * @return the remote config
   */
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return colNames.length;
  }

  /** The col names. */
  private static String[] colNames =
  {
      "#", "<html>Device<br>Button</html>", "Key", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>",
      "Raw Data", "Hex", "<html>EFC or<br>Key Name</html>", "Notes"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "__VCR/DVD__", "_xshift-Thumbs_Down_", "__VCR/DVD__", "Setup", "00 (key code)", "FF FF",
      "xshift-CBL/SAT", "A reasonable length long note"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /** The col widths. */
  private static boolean[] colWidths =
  {
      true, true, false, true, true, true, true, false, false
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, DeviceButton.class, Integer.class, DeviceType.class, SetupCode.class, Hex.class, Hex.class,
      String.class, String.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    if ( ( col == 0 ) || ( ( col > 4 ) && ( col < 8 ) ) )
      return false;

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    KeyMove keyMove = getRow( row );
    Remote r = remoteConfig.getRemote();
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return r.getDeviceButtons()[ keyMove.getDeviceButtonIndex() ];
      case 2:
        return new Integer( keyMove.getKeyCode() );
      case 3:
        return r.getDeviceTypeByIndex( keyMove.getDeviceType() );
      case 4:
        return new SetupCode( keyMove.getSetupCode() );
      case 5:
        return keyMove.getData();
      case 6:
        return keyMove.getCmd();
      case 7:
        return keyMove.getValueString( remoteConfig );
      case 8:
        return keyMove.getNotes();
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    KeyMove keyMove = getRow( row );
    if ( col == 1 )
    {
      Remote r = remoteConfig.getRemote();
      DeviceButton[] deviceButtons = r.getDeviceButtons();
      for ( int i = 0; i < deviceButtons.length; ++i )
        if ( deviceButtons[ i ] == value )
        {
          keyMove.setDeviceButtonIndex( i );
          break;
        }
    }
    else if ( col == 2 )
      keyMove.setKeyCode( ( ( Integer )value ).intValue() );
    else if ( col == 3 )
      keyMove.setDeviceType( ( ( DeviceType )value ).getNumber() );
    else if ( col == 4 )
    {
      SetupCode setupCode = null;
      if ( value.getClass() == String.class )
        setupCode = new SetupCode( ( String )value );
      else
        setupCode = ( SetupCode )value;
      keyMove.setSetupCode( setupCode.getValue() );
    }
    // else if (( col > 4 ) && ( col < 8 ))
    // {
    // if ( value != null )
    // setRow( row, ( KeyMove )value );
    // }
    else if ( col == 8 )
      keyMove.setNotes( ( String )value );
    else
      return;
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( 1 );
      return editor;
    }
    else if ( col == 2 )
    {
      return keyEditor;
    }
    else if ( col == 3 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceTypeBox );
      editor.setClickCountToStart( 1 );
      return editor;
    }
    else if ( col == 4 || col == 8 )
    {
      return selectAllEditor;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 2 )
      return keyRenderer;

    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device button box. */
  private JComboBox deviceButtonBox = new JComboBox();

  /** The device type box. */
  private JComboBox deviceTypeBox = new JComboBox();

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();

  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
}
