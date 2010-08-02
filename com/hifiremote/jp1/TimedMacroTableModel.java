package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class TimedMacroTableModel extends JP1TableModel< TimedMacro >
{

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      setData( remoteConfig.getTimedMacros() );
      dayScheduleEditor.setRemote( remote );
      dayScheduleEditor.setTitle( "Day Schedule Setter" );
      timeEditor.setRemote( remote );
      timeEditor.setTitle( "Time Setter" );
    }
  }
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Every Mon;Tue;Wed;Thu;Fri", "00:00_", 
      "A reasonable length macro with a reasonable number of steps ", 
      "A reasonable length note for a macro"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, DaySchedule.class, RMTime.class, String.class, String.class
  };
  
  private static final boolean[] colWidths =
  {
      true, false, true, false, false
  };
  
  private static final String[] colNames =
  {
      "#", "Days", "Time", "Macro Keys", "Notes"
  };
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }
  
  @Override
  public int getColumnCount()
  {
    return 5;
  }
  
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      return dayScheduleEditor;
    }
    else if ( col == 2 )
    {
      return timeEditor;
    }
    else
    {  
      return null;
    }
  }  
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    if ( col == 1 || col == 2 || col == 4 )
    {
      return true;
    }
    return false;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    TimedMacro timedMacro = remoteConfig.getTimedMacros().get( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return timedMacro.getDaySchedule();
      case 2:
        return timedMacro.getTime();
      case 3:
        return timedMacro.getValueString( remoteConfig );
      case 4:
        return timedMacro.getNotes();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    TimedMacro timedMacro = getRow( row );
    if ( col == 1 )
    {
      timedMacro.setDaySchedule( ( DaySchedule )value );
    }
    else if ( col == 2 )
    {
      timedMacro.setTime( ( RMTime )value );
    }
    else if ( col == 4 )
    {
      timedMacro.setNotes( ( String )value );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }
    
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  private RemoteConfiguration remoteConfig = null;

  
  private RMSetterEditor< DaySchedule, DayScheduleBox > dayScheduleEditor = 
    new RMSetterEditor< DaySchedule, DayScheduleBox >( DayScheduleBox.class );

  private RMSetterEditor< RMTime, RMTimePanel > timeEditor = 
    new RMSetterEditor< RMTime, RMTimePanel >( RMTimePanel.class );
}
