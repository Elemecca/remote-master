package com.hifiremote.jp1;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialFunctionTableModel.
 */
public class SpecialFunctionTableModel extends JP1TableModel< SpecialProtocolFunction >
{

  /**
   * Instantiates a new special function table model.
   */
  public SpecialFunctionTableModel()
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
    if ( remoteConfig != null )
    {
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      Remote remote = remoteConfig.getRemote();
      setData( remoteConfig.getSpecialFunctions() );
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
      keyRenderer.setRemote( remote );
      keyEditor.setRemote( remote );
    }
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
    int count = colNames.length - 1;
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  /** The col names. */
  private static String[] colNames =
  {
      "#", "<html>Device<br>Button</html>", "Key", "Type", "Function", "Hex", "Notes", "<html>Size &amp<br>Color</html>"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "_VCR/DVD_", "shift-Thumbs_Up", " ToadTog(0,ForceOff) ",
      "[Short]:DiscreteON;DiscreteON; [Long]:DiscreteOFF;DiscreteOFF", "00 11 22 33", 
      "A reasonable length note", "Color_"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /** The col widths. */
  private static boolean[] colWidths =
  {
      true, true, true, false, false, false, false, true
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, DeviceButton.class, Integer.class, String.class, String.class, Hex.class, 
      String.class, Color.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return !( col == 0 || col == 3 || col == 4 || col == 5 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    SpecialProtocolFunction sf = getRow( row );
    Remote r = remoteConfig.getRemote();
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        int index = sf.getDeviceButtonIndex();
        return r.getDeviceButton( index );
      case 2:
        return new Integer( sf.getKeyCode() );
      case 3:
        return sf.getDisplayType( remoteConfig );
      case 4:
        return sf.getValueString( remoteConfig );
      case 5:
        return sf.getCmd();
      case 6:
        return sf.getNotes();
      case 7:
        return sf.getHighlight();
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    SpecialProtocolFunction sf = getRow( row );
    if ( col == 1 )
    {
      Remote r = remoteConfig.getRemote();
      DeviceButton[] deviceButtons = r.getDeviceButtons();
      for ( int i = 0; i < deviceButtons.length; ++i )
      {
        if ( deviceButtons[ i ] == value )
        {
          sf.setDeviceButtonIndex( deviceButtons[ i ].getButtonIndex() );
          break;
        }
      }
    }
    else if ( col == 2 )
    {
      sf.setKeyCode( ( ( Integer )value ).intValue() );
    }
    else if ( col == 6 )
    {
      sf.setNotes( ( String )value );
    }
    else if ( col == 7 )
    {
      sf.setHighlight( ( Color  )value );
    }
    else
    {
      return;
    }
    propertyChangeSupport.firePropertyChange( col == 7 ? "highlight" : "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
      return editor;
    }
    else if ( col == 2 )
    {
      return keyEditor;
    }
    else if ( col == 6 )
    {
      return noteEditor;
    }
    else if ( col == 7 )
    {
      return colorEditor;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 2 )
    {
      return keyRenderer;
    }
    else if ( col == 7 )
    {
      return colorRenderer;
    }

    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device button box. */
  private JComboBox deviceButtonBox = new JComboBox();

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();

  private SelectAllCellEditor noteEditor = new SelectAllCellEditor();
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
}
