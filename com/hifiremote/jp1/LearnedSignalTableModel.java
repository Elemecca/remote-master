package com.hifiremote.jp1;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalTableModel.
 */
public class LearnedSignalTableModel extends JP1TableModel< LearnedSignal >
{

  /**
   * Instantiates a new learned signal table model.
   */
  public LearnedSignalTableModel()
  {
    deviceEditor.setClickCountToStart( RMConstants.ClickCountToStart );
  }

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
      deviceComboBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceButtons() ) );
      keyRenderer.setRemote( remoteConfig.getRemote() );
      keyEditor.setRemote( remoteConfig.getRemote() );
      setData( remoteConfig.getLearnedSignals() );
      if ( remoteConfig.getRemote().usesEZRC() )
      {
        selectAllEditor = new SelectAllCellEditor();
      }
    }
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Name", "<html>Device<br>Button</html>", "Key", "Notes", "Size", "Freq.", "Protocol", "Device",
      "<html>Sub<br>Device</html>", "OBC", "Hex Cmd", "Misc", "<html>Size &amp<br>Color</html>"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    int count = colNames.length - 2;
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
    {
      ++count;
    }
    return count;
  }
  
  private int getEffectiveColumn( int col )
  {
    if ( col > 0 && ( remoteConfig == null || !remoteConfig.getRemote().usesEZRC() ) )
    {
      col++;   // Skip name column
    }
    return col;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    col = getEffectiveColumn( col );
    return colNames[ col ];
  }

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Name______", "__VCR/DVD__", "_xshift-VCR/DVD_", "A longish comment or note", "1024", "99999", "Protocol", "Device",
      "Device", "OBC", "Hex Cmd", "Miscellaneous", "Color_"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    col = getEffectiveColumn( col );
    return colPrototypeNames[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 1 || col == 4 || col == 7 || col == 12 )
    {
      return false;
    }
    else
    {
      return true;
    }
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, // row
      String.class,  // name
      DeviceButton.class, // DeviceButton
      Integer.class, // keycode
      String.class, // notes
      Integer.class, // size
      Integer.class, // frequency
      String.class, // protocol
      Integer.class, // device
      Integer.class, // sub-device
      Integer.class, // OBC
      String.class, // hex cmd
      String.class, // misc
      Color.class // color
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    col = getEffectiveColumn( col );
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
    col = getEffectiveColumn( col );
    return ( col > 0 && col < 5 ) || col == 13;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    column = getEffectiveColumn( column );
    LearnedSignal l = getRow( row );
    UnpackLearned ul = l.getUnpackLearned();
    ArrayList< LearnedSignalDecode > da = l.getDecodes();
    int numDecodes = 0;
    int decodeIndex = 0;
    LearnedSignalDecode decode = null;
    if ( da != null )
    {
      numDecodes = da.size();
      if ( numDecodes > 1 )
      {
        for ( int i = 0; i < da.size(); ++i )
        {
          decode = da.get( i );
          if ( decode.ignore )
          {
            numDecodes-- ;
          }
          else
          {
            decodeIndex = i;
          }
        }
      }
    }
    if ( numDecodes != 1 && ( column > 7 && column < 13 && ( column != 12 || ul.error.isEmpty() ) ) )
    {
      return null;
    }
    if ( numDecodes == 1 && column > 6 && column < 13 )
    {
      decode = da.get( decodeIndex );
    }
    switch ( column )
    {
      case 0: // row number
        return new Integer( row + 1 );
      case 1:
        return l.getName();
      case 2: // deviceButton
        return remoteConfig.getRemote().getDeviceButton( l.getDeviceButtonIndex() );
      case 3: // key
        return new Integer( l.getKeyCode() );
      case 4: // notes
        return l.getNotes();
      case 5: // size
        return l.getData().length();
      case 6: // frequency
        return new Integer( ul.frequency );
      case 7: // protocol
        if ( numDecodes == 0 )
        {
          return "** None **";
        }
        if ( numDecodes > 1 )
        {
          return "** Multiple **";
        }
        return decode.protocolName;
      case 8: // device
        return new Integer( decode.device );
      case 9: // subDevice
        if ( decode.subDevice == -1 )
        {
          return null;
        }
        return new Integer( decode.subDevice );
      case 10: // obc
        return new Integer( decode.obc );
      case 11:
        return Hex.toString( decode.hex );
      case 12:
        String message = ul.error.isEmpty() ? "" : "Malformed signal: " + ul.error;
        if ( numDecodes == 1 )
        {
          message += ul.error.isEmpty() ? "" : "; ";
          message += decode.miscMessage;
        }
        return message;
      case 13:
        return l.getHighlight();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    col = getEffectiveColumn( col );
    LearnedSignal l = getRow( row );
    Remote remote = null;
    DeviceButton db = null;
    DeviceUpgrade upg = null;
    if ( remoteConfig != null && ( remote = remoteConfig.getRemote() ).usesEZRC() )
    {
      db = remote.getDeviceButton( l.getDeviceButtonIndex() );
      upg = db.getUpgrade();
    }
    switch ( col )
    {
      case 1:
        l.setName( ( String )value );
        if ( upg != null )
        {
          Function fn = upg.getFunction( l.getKeyCode() );
          if ( fn != null )
          {
            fn.setName( ( String )value );
          }
        }
        break;
      case 2:
      {
        if ( upg != null )
        {
          upg.getLearnedMap().remove( l.getKeyCode() );
          Button b = remote.getButton( l.getKeyCode() );
          l.removeReference( db, b );
          db = ( DeviceButton )value;
          upg = db.getUpgrade();
          upg.getLearnedMap().put( l.getKeyCode(), l );
          l.addReference( db, b );
        }
        db = ( DeviceButton )value;
        l.setDeviceButtonIndex( db.getButtonIndex() );
        break;
      }
      case 3:
        if ( upg != null )
        {
          upg.getLearnedMap().remove( l.getKeyCode() );
          Button b = remote.getButton( l.getKeyCode() );
          l.removeReference( db, b );
          int keyCode = ( Integer )value;
          upg.getLearnedMap().put( keyCode, l );
          b = remote.getButton( keyCode );
          l.addReference( db, b );
        }       
        l.setKeyCode( ( ( Integer )value ).shortValue() );
        break;
      case 4:
        l.setNotes( ( String )value );
        break;
      case 13:
        l.setHighlight( ( Color  )value );
        break;
    }
    propertyChangeSupport.firePropertyChange( col == 13 ? "highlight" : "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 3 )
    {
      return keyRenderer;
    }
    else if ( col == 13 )
    {
      return colorRenderer;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 1 )
    {
      return selectAllEditor;
    }
    else if ( col == 2 )
    {
      DefaultCellEditor e = new DefaultCellEditor( deviceComboBox );
      e.setClickCountToStart( RMConstants.ClickCountToStart );
      return e;
    }
    else if ( col == 3 )
    {
      return keyEditor;
    }
    else if ( col == 4 )
    {
      return noteEditor;
    }
    else if ( col == 13 )
    {
      return colorEditor;
    }

    return null;
  }
  
  @Override
  public void removeRow( int row )
  {
    Remote remote = null;
    if ( remoteConfig != null && ( remote = remoteConfig.getRemote() ).usesEZRC() )
    {
      LearnedSignal ls = getRow( row );
      DeviceButton db = remote.getDeviceButton( ls.getDeviceButtonIndex() );
      DeviceUpgrade upg = db.getUpgrade();
      upg.getLearnedMap().remove( ls.getKeyCode() );
    }
    super.removeRow( row );
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device combo box. */
  private JComboBox deviceComboBox = new JComboBox();
  private DefaultCellEditor deviceEditor = new DefaultCellEditor( deviceComboBox );

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();
  private SelectAllCellEditor noteEditor = new SelectAllCellEditor();
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private SelectAllCellEditor selectAllEditor = null;
}
