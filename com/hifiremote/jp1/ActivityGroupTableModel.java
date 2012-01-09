package com.hifiremote.jp1;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ActivityGroupTableModel extends JP1TableModel< ActivityGroup >
{
  
  public void set( Button btn, RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig; 
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      setData( remoteConfig.getActivities().get( btn ).getActivityGroups() );
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
    }
  }
  
  private static final String[] colNames =
  {
      "#", "Button Group", "Device", "Notes", "<html>Size &amp<br>Color</html>"
  };
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "A long list of button names______________________________", "Device Button",
      "A short note_______", "Color_"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, DeviceButton.class, String.class, Color.class
  };

  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  @Override
  public int getColumnCount()
  {
    int count = colNames.length - 1;
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return col > 1;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col == 0;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 2 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
      return editor;
    }
    else if ( col == 3 )
    {
      return selectAllEditor;
    }
    else if ( col == 4 )
    {
      return colorEditor;
    }
    return null;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 4 )
    {
      return colorRenderer;
    }
    else
      return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    ActivityGroup group = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return group.getButtons();
      case 2:
        return group.getDevice();
      case 3:
        return group.getNotes();
      case 4:
        return group.getHighlight();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    ActivityGroup group = getRow( row );
    if ( col == 2 )
    {
      group.setDevice( ( DeviceButton )value );
    }
    else if ( col == 3 )
    {
      group.setNotes( ( String )value );
    }
    else if ( col == 4 )
    {
      group.setHighlight( ( Color )value );
    }
    propertyChangeSupport.firePropertyChange( col == 4 ? "highlight" : "data", null, null );
  }
  
  private RemoteConfiguration remoteConfig = null;
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private JComboBox deviceButtonBox = new JComboBox();
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();

}
