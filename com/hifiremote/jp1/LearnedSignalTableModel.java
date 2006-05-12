package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class LearnedSignalTableModel
  extends JP1TableModel
{
  public LearnedSignalTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    deviceComboBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceButtons()));
    keyRenderer.setRemote( remoteConfig.getRemote());
    keyEditor.setRemote( remoteConfig.getRemote());
    setData( remoteConfig.getLearnedSignals());
  }

  private static final String[] colNames = 
  {
    "#", "<html>Device<br>Button</html>", "Key", "Raw Signal", "Notes"
  };
  public int getColumnCount(){ return colNames.length; }
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  private static final String[] colPrototypeNames = 
  {
    "00", "__VCR/DVD__", "_xshift-VCR/DVD_", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", "A reasonable comment"
  };
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }
  
  public boolean isColumnWidthFixed( int col )
  {
    if ( col < 3 )
      return true;
    return false;
  }
  
  private static final Class[] colClasses =
  {
    Integer.class, DeviceButton.class, Integer.class, Hex.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if (( col == 1 ) || ( col == 2 ) || ( col == 4 ))
      return true;
    return false;
  }

  public Object getValueAt(int row, int column)
  {
    LearnedSignal l = ( LearnedSignal )getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return remoteConfig.getRemote().getDeviceButtons()[ l.getDeviceButtonIndex()];
      case 2:
        return new Integer( l.getKeyCode());
      case 3:
        return l.getData();
      case 4:
        return l.getNotes();
    }
    return null;
  }

  public void setValueAt( Object value, int row, int col )
  {
    LearnedSignal l = ( LearnedSignal )getRow( row );
    switch ( col )
    {
      case 1:
      {
        DeviceButton[] deviceButtons = remoteConfig.getRemote().getDeviceButtons();
        for ( short i = 0; i < deviceButtons.length; ++i )
        {
          if ( deviceButtons[ i ] == value )
            l.setDeviceButtonIndex( i );
        }
        break;
      }
      case 2:
        l.setKeyCode((( Integer )value ).shortValue());
        break;
      case 4:
        l.setNotes(( String )value );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 2 )
      return keyRenderer;
    return null;
  }
  
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor e = new DefaultCellEditor( deviceComboBox );
      e.setClickCountToStart( 2 );
      return e;
    }  
    else if ( col == 2 )
      return keyEditor;
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
  private JComboBox deviceComboBox = new JComboBox();
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private KeyEditor keyEditor = new KeyEditor();
}
