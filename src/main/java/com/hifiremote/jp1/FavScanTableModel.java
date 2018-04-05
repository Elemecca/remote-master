package com.hifiremote.jp1;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.GeneralFunction.IconPanel;
import com.hifiremote.jp1.GeneralFunction.IconRenderer;
import com.hifiremote.jp1.GeneralFunction.RMIcon;

public class FavScanTableModel extends JP1TableModel< FavScan >
{

  public FavScanTableModel()
  {
  // TODO Auto-generated constructor stub
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig; 
    if ( remoteConfig != null )
    {
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      setData( remoteConfig.getFavScans() );
    }
    if ( remoteConfig.getRemote().isSSD() )
    {
      iconEditor = new RMSetterEditor< RMIcon, IconPanel >( IconPanel.class );
      iconEditor.setRemoteConfiguration( remoteConfig );
      iconEditor.setTitle( "Icon Editor" );
      iconRenderer = new IconRenderer();
    }
  }
  
  private int getEffectiveColumn( int col )
  {
    if ( remoteConfig != null && !remoteConfig.getRemote().usesEZRC() && col > 0 )
    {
      col++;
    }
    if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() && col > 1 )
    {
      col++;
    }
    if ( remoteConfig != null && !remoteConfig.getRemote().usesEZRC() && col > 2 )
    {
      col++;
    }
    if ( remoteConfig != null && !remoteConfig.getRemote().isSSD() && col > 4 )
    {
      col++;
    }
    if ( remoteConfig != null && !( remoteConfig.getRemote().hasProfiles() 
        && panel instanceof FavoritesPanel && ( ( FavoritesPanel )panel ).showProfile() ) && col > 5 )
    {
      col++;
    }
    return col;
  }

  private static final String[] colPrototypeNames =
  {
      " 00 ", "Name of a favorite ", "A reasonable length macro with several steps ", 
      "Channel Number_", "A reasonable length note", "Icon?_", "Profile?_", "Color_"
  };

  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, false, false, true, false, true, true, true
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ getEffectiveColumn( col ) ];
  }

  @Override
  public boolean isCellEditable( int row, int col )
  {
    col = getEffectiveColumn( col );
    if ( col != 1 && col < 3 )
    {
      return false;
    }
    return true;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Name", "Macro Keys", "Channel Number", "Notes", "Icon?", "<html>In<br>profile&#63;</html>", "<html>Size &amp<br>Color</html>"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ getEffectiveColumn( col ) ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class, String.class, String.class, RMIcon.class, Boolean.class, Color.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ getEffectiveColumn( col ) ];
  }

  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ getEffectiveColumn( col ) ];
  }

  @Override
  public int getColumnCount()
  {
    int count = colNames.length - 5;
    if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
    {
      ++count;
    }
    if ( remoteConfig != null && remoteConfig.getRemote().isSSD() )
    {
      ++count;
    }
    if ( remoteConfig != null && remoteConfig.getRemote().hasProfiles()
        && panel instanceof FavoritesPanel && ( ( FavoritesPanel )panel ).showProfile() )
    {
      ++count;
    }
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 5 )
    {
      return iconRenderer;
    }
    else if ( col == 7 )
    {
      return colorRenderer;
    }
    return null;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 1 || col == 3 )
    {
      return selectAllEditor;
    }
    if ( col == 5 )
    {
      return iconEditor;
    }
    if ( col == 6 )
    {
      JCheckBox check =  new JCheckBox();
      check.setHorizontalAlignment( SwingConstants.CENTER );
      DefaultCellEditor e = new DefaultCellEditor( check );
      e.setClickCountToStart( 2 );
      return e;
    }
    if ( col == 7 )
    {
      return colorEditor;
    }
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    FavScan favScan = remoteConfig.getFavScans().get( row );
    column = getEffectiveColumn( column );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return favScan.getName();
      case 2:
        return favScan.getValueString( remoteConfig );
      case 3:
        return favScan.getChannel();
      case 4:
        return favScan.getNotes();
      case 5:
        return favScan.icon;
      case 6:
        if ( panel instanceof FavoritesPanel )
        {
          Activity activity = ( Activity )( ( FavoritesPanel )panel ).getActivity();
          return activity != null && favScan.getProfileIndices().contains( activity.getProfileIndex() );
        }
        else
        {
          return false;
        }
      case 7:
        return favScan.getHighlight();
      default:
        return null;
    }
  }

  @Override
  public void setValueAt( Object value, int row, int col )
  {
    FavScan favScan = getRow( row );
    col = getEffectiveColumn( col );
    if ( col == 1 )
    {
      favScan.setName( ( String )value );
    }
    else if ( col == 3 )
    {
      String s = ( String )value;
      String channel = "";
      for ( Character ch : s.toCharArray() )
      {
        ch = ( ch == '-' ) ? '.' : ch;
        if ( Character.isDigit( ch ) || ch == '.' )
        {
          channel += ch;
        }
      }
      favScan.setChannel( resizeChannel( channel ) );
    }
    else if ( col == 4 )
    {
      favScan.setNotes( ( String )value );
    }
    else if ( col == 5 )
    {
      favScan.icon = ( RMIcon )value;
    }
    else if ( col == 6 )
    {
      int index = ( ( Activity )( ( FavoritesPanel )panel ).getActivity() ).getProfileIndex();
      if ( ( Boolean )value )
      {
        if ( !favScan.getProfileIndices().contains( index ) )
        {
          favScan.getProfileIndices().add( index );
        }
      }
      else
      {
        favScan.getProfileIndices().remove( ( Integer )index );
      }
    }
    else if ( col == 7 )
    {
      favScan.setHighlight( ( Color  )value );
    }
    propertyChangeSupport.firePropertyChange( col == 3 ? "highlight" : "data", null, null );
  }

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  public void setPanel( RMPanel panel )
  {
    this.panel = panel;
  }
  
  public String resizeChannel( String channel )
  {
    DeviceButton favDb = remoteConfig.getFavKeyDevButton();
    int favWidth = favDb.getFavoriteWidth();
    Remote remote = remoteConfig.getRemote();
    int dot = channel.indexOf( '.' );
    String body = dot >= 0 ? channel.substring( 0, dot ) : channel;
    String tail = dot >= 0 ? channel.substring( dot ) : "";
    int size = body.length();
    if ( size > favWidth )
    {
      body = body.substring( size - favWidth, size );
    }
    if ( remote.isSSD() && size < favWidth )
    {
      body = "00000000".substring( 0, favWidth - size ) + body;
    }
    return body + tail;
  }
  
  @Override
  public String getToolTipText( int row, int col )
  {
    col = getEffectiveColumn( col );
    int thisCell = row + col * 0x100;
    if ( thisCell == lastCell )
    {
      return null;
    }
    lastCell = thisCell;
    if ( col == 5 )
    {
      return "<html>Double click this column to open Icon Editor to set or<br>"
          + "remove a user icon from this favorite and/or import an<br>"
          + "icon from a file or export one to a file.</html>";
    }
    else if ( col == 6 )
    {
      return "<html>Double click this column to add or remove a favorite<br>"
          + "from the selected profile.</html>";
    }
    return null;
  }
  
  private int lastCell = 0;
  private RemoteConfiguration remoteConfig = null;
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private RMSetterEditor< RMIcon, IconPanel > iconEditor = null;
  private IconRenderer iconRenderer = null;
  private RMPanel panel = null;
}
