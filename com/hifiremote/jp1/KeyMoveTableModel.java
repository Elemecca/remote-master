package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
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
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
      keyRenderer.setRemote( remote );
      keyEditor.setRemote( remote );
      deviceTypeBox.setModel( new DefaultComboBoxModel( remote.getDeviceTypes() ) );

      StringBuilder sb = new StringBuilder( "<html>EFC" );
      if ( remote.getEFCDigits() == 5 )
      {
        sb.append( "-5" );
      }
      if ( remote.supportsKeyCodeKeyMoves() )
      {
        sb.append( ", Key" );
      }
      sb.append( " or<br/>Function Name</html>" );
      colNames[ 7 ] = sb.toString();

      refresh();
      setData( allKeyMoves );
    }
  }

  public void refresh()
  {
    allKeyMoves = new ArrayList< KeyMove >( remoteConfig.getKeyMoves() );
    DeviceButton[] deviceButtons = remoteConfig.getRemote().getDeviceButtons();
    upgradeKeyMoveCount = 0;
    for ( int i = 0; i < deviceButtons.length; i++ )
    {
      DeviceUpgrade du = remoteConfig.getAssignedDeviceUpgrade( deviceButtons[ i ] );
      if ( du != null )
      {
        for ( KeyMove keyMove : du.getKeyMoves() )
        {
          keyMove.setDeviceButtonIndex( i );
          allKeyMoves.add( keyMove );
          upgradeKeyMoveCount++ ;
        }
      }
    }
    data = allKeyMoves;
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
      "Raw Data", "Hex", "<html>EFC, Key, or<br>Function Name</html>", "Notes"
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
      " 00 ", "__VCR/DVD__", "_xshift-Thumbs_Down_", "__VCR/DVD__", "Setup", "00 (key code)", "FF FF",
      "xshift-CBL/SAT", "A reasonable length long note"
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
      true, true, false, true, true, true, true, false, false
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
      Integer.class, DeviceButton.class, Integer.class, DeviceType.class, SetupCode.class, Hex.class, Hex.class,
      String.class, String.class
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
    if ( row >= remoteConfig.getKeyMoves().size() )
    {
      return false;
    }
    if ( col == 0 || col > 4 && col < 8 )
    {
      return false;
    }

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
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    KeyMove keyMove = getRow( row );
    if ( col == 1 )
    {
      Remote r = remoteConfig.getRemote();
      DeviceButton[] deviceButtons = r.getDeviceButtons();
      for ( int i = 0; i < deviceButtons.length; ++i )
      {
        if ( deviceButtons[ i ] == value )
        {
          keyMove.setDeviceButtonIndex( i );
          break;
        }
      }
    }
    else if ( col == 2 )
    {
      keyMove.setKeyCode( ( ( Integer )value ).intValue() );
    }
    else if ( col == 3 )
    {
      keyMove.setDeviceType( ( ( DeviceType )value ).getNumber() );
    }
    else if ( col == 4 )
    {
      SetupCode setupCode = null;
      if ( value.getClass() == String.class )
      {
        setupCode = new SetupCode( ( String )value );
      }
      else
      {
        setupCode = ( SetupCode )value;
      }
      keyMove.setSetupCode( setupCode.getValue() );
    }
    // else if (( col > 4 ) && ( col < 8 ))
    // {
    // if ( value != null )
    // setRow( row, ( KeyMove )value );
    // }
    else if ( col == 8 )
    {
      keyMove.setNotes( ( String )value );
    }
    else
    {
      return;
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
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
    else if ( col == 3 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceTypeBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
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

    return kmRenderer;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device button box. */
  private JComboBox deviceButtonBox = new JComboBox();

  /** The device type box. */
  private JComboBox deviceTypeBox = new JComboBox();

  private static final Color normalColor = Color.black;
  private static final Color normalBGColor = Color.white;
  private static final Color normalSelectedBGColor = Color.blue;

  private static final Color attachedColor = Color.white;
  private static final Color attachedBGColor = Color.lightGray;
  private static final Color attachedSelectedBGColor = Color.darkGray;

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer()
  {
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      int modelRow = sorter.modelIndex( row );
      Component c = super.getTableCellRendererComponent( table, value, isSelected, false, modelRow, col );
      boolean isNormal = modelRow < remoteConfig.getKeyMoves().size();
      // c.setForeground( isNormal ? normalColor : attachedColor );
      Color bgColor = isNormal ? normalBGColor : attachedBGColor;
      if ( isSelected )
      {
        bgColor = isNormal ? normalSelectedBGColor : attachedSelectedBGColor;
      }
      c.setBackground( bgColor );

      return c;
    }
  };

  private DefaultTableCellRenderer kmRenderer = new DefaultTableCellRenderer()
  {
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      int modelRow = sorter.modelIndex( row );
      Component c = super.getTableCellRendererComponent( table, value, isSelected, false, modelRow, col );
      boolean isNormal = modelRow < remoteConfig.getKeyMoves().size();
      // c.setForeground( isNormal ? normalColor : attachedColor );
      Color bgColor = isNormal ? normalBGColor : attachedBGColor;
      if ( isSelected )
      {
        bgColor = isNormal ? normalSelectedBGColor : attachedSelectedBGColor;
      }
      c.setBackground( bgColor );

      return c;
    }
  };

  public void resetKeyMoves()
  {
    java.util.List< KeyMove > keymoves = new ArrayList< KeyMove >();
    keymoves.addAll( allKeyMoves.subList( 0, allKeyMoves.size() - upgradeKeyMoveCount ) );
    remoteConfig.setKeyMoves( keymoves );
  }

  private int upgradeKeyMoveCount = 0;

  public int getUpgradeKeyMoveCount()
  {
    return upgradeKeyMoveCount;
  }

  private java.util.List< KeyMove > allKeyMoves = null;

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();

  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();

  public TableSorter sorter = null;
}
