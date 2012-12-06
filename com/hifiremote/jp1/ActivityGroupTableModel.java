package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ActivityGroupTableModel extends JP1TableModel< ActivityGroup > implements CellEditorModel
{
  
  public void set( Button btn, RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig; 
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      activity = remoteConfig.getActivities().get( btn );
      setData( activity.getActivityGroups() );
      tabIndex = remote.getButtonGroups().get( "Activity" ).indexOf( btn );
      comboModel = new DefaultComboBoxModel( remote.getDeviceButtons() );
      if ( !remote.usesEZRC() )
      {
        comboModel.insertElementAt( DeviceButton.noButton, 0 );
      }
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
    Remote remote = remoteConfig.getRemote();
    DeviceButton[][][] activityControl = remote.getActivityControl();
    if ( activityControl != null && activityControl.length > 0 )
    {
      ActivityGroup group = getRow( row );
      return col > 2 || ( col == 2  && group.getDevice() != null 
          && group.getDevice() != DeviceButton.noButton 
          && activityControl[ tabIndex ][ row ].length > 1 );
    }
    else
    {
      return col > 1;
    }
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col == 0;
  }
  
  @Override
  public TableCellEditor getCellEditor( int row, int col )
  {
    if ( col == 2 )
    {
      JComboBox cb = ( JComboBox )comboEditor.getComponent();
      Remote remote = remoteConfig.getRemote();
      if ( remote.getActivityControl() != null && remote.getActivityControl().length > 0 )
      {
        cb.setModel( new DefaultComboBoxModel( remote.getActivityControl()[ tabIndex ][ row ] ) );
      }
      else
      {
        cb.setModel( comboModel );
      }
      comboEditor.setClickCountToStart( RMConstants.ClickCountToStart );
      return comboEditor;
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
      return agRenderer;
  }
  
  private DefaultTableCellRenderer agRenderer = new DefaultTableCellRenderer()
  {
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
      Color bgColor = ( col != 2 || isCellEditable( row, col ) ) ? Color.BLACK : Color.GRAY;
      c.setForeground( bgColor );
      return c;
    }
  };

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
      if ( remoteConfig.getRemote().hasActivityControl() )
      {
        for ( ActivityGroup g : activity.getActivityGroups() )
        {
          g.setHighlight( ( Color )value );
        }
      }
      else
      {
        group.setHighlight( ( Color )value );
      }
    }
    propertyChangeSupport.firePropertyChange( col == 4 ? "highlight" : "data", null, null );
  }
  
  private RemoteConfiguration remoteConfig = null;
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private DefaultCellEditor comboEditor = new DefaultCellEditor( new JComboBox() );
  private DefaultComboBoxModel comboModel = null;
  private Activity activity = null;
  private int tabIndex = -1;
}
