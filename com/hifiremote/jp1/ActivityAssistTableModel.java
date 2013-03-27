package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.Activity.Assister;

public class ActivityAssistTableModel extends JP1TableModel< Activity.Assister >
{
  
  public void set( Button btn, int type, RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig; 
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
      activity = remoteConfig.getActivities().get( btn );
      setData( activity.getAssists().get( type ) );
      keyRenderer.setRemote( remote );
      keyEditor.setRemote( remote );
    }
  }
  
  private static final String[] colNames =
  {
      "#", "Device", "Button", "Function"
  };
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Device Button", "Button Name", "Function"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, DeviceButton.class, Integer.class, Function.class
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
    return 4;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return col > 0 && col != 3;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col == 0;
  }
  
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
    else
      return null;
  }
  
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
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    Assister assister = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return assister.device;
      case 2:
        if ( assister.button == null )
        {
          return null;
        }
        return new Integer( assister.button.getKeyCode() );
      case 3:
        return assister.function;
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    Assister assister = getRow( row );
    if ( col == 1 )
    {
      assister.setDevice( ( DeviceButton )value );
    }
    else if ( col == 2 )
    {
      assister.setButton( remoteConfig.getRemote().getButton( ( ( Integer )value ).intValue() ) );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }
  
  private RemoteConfiguration remoteConfig = null;
  private Activity activity = null;
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private KeyEditor keyEditor = new KeyEditor();
  private JComboBox deviceButtonBox = new JComboBox();

}
