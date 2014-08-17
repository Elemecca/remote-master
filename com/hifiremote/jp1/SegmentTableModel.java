package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.SegmentPanel.SegmentTable;

public class SegmentTableModel extends JP1TableModel< Segment >
{
  private RemoteConfiguration config = null;
  private SegmentTable table = null;
  private boolean changed = false;
  private boolean sorted = false;
  
  public SegmentTableModel()
  {}
  
  private static Font lblFont = ( new JLabel() ).getFont();
  
  private static final String[] colNames =
  {
    "    ", "Address", "Length", "Type", "Flags", "Data"
  };

  private static final String[] colPrototypeNames =
  {
    " 00 ", "Address_", "Length_", "Type_", "Flags_", "Data___________________________"
  };

  private static final Class< ? >[] colClasses =
  {
    Integer.class, String.class, String.class, String.class, String.class, Hex.class
  };

  public void setTable( SegmentTable table )
  {
    this.table = table;
  }
  
  @Override
  public String getToolTipText( int row, int col )
  {
    if ( col == 5 )
    {
      Segment seg = getRow( row );
      int type = seg.get_Type();
      String description = segmentDescription( type );
      return description != null ? description : "Segment type not known to RMIR";
    }
    return null;
  }
  
  private String segmentDescription( int type )
  {
    switch ( type )
    {
      case 0:
        return "Device Button segment";
      case 1:
        return "Macro segment";
      case 2:
        return "Multi Macro segment";
      case 3:
        return "Device Specific Macro segment";
      case 4:
        return "Activity Power Macro segment";
      case 7:
        return "Keycode Keymove segment";
      case 8:
        return "EFC-style Keymove segment";
      case 9:
        return "Learned Signal segment";
      case 0x0A:
        return "Soft Key Names segment";
      case 0x0B:
        return "Button Device Assignments segment";
      case 0x10:
        return "Upgrade Definition segment";
      case 0x11:
        return "Upgrade Assignments segment";
      case 0x12:
        return "Language Setting segment";
      case 0x15:
        return "Device Names segment";
      case 0x1D:
        return "Favorites Definition segment";
      case 0x1E:
        return "Activity Definition segment";
      case 0x1F:
        return "Activity Assist Definition segment";
      case 0x20:
        return "Function Names segment";
      case 0xDB:
        return "Activity Device Settings segment";
      case 0xDC:
        return "Activity Help Settings segment";
      default:
        return null;
    }
  }

  @Override
  public int getColumnCount()
  {
    return colNames.length;
  }
  
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
  public boolean isColumnWidthFixed( int col )
  {
    return col != 5;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 5 )
    {
      return new TextAreaRenderer();
    }
    return null;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 5 )
    {
      return new TextAreaEditor();
    }
    return null;
  }
  
  private boolean isTypeEditable( int type )
  {
    if ( useSavedData() )
    {
      return true;
    }
    else
    {
      return !config.getRemote().getSegmentTypes().contains( type )
          || segmentDescription( type ) == null;
    }
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    Segment seg = getRow( row );
    return ( isTypeEditable( seg.get_Type() ) && col > ( useSavedData() ? 2 : 3 ) );
  }

  @Override
  public Object getValueAt( int row, int col )
  {
    Segment seg = getRow( row );
    switch ( col )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return String.format( "%04X", seg.getAddress() );
      case 2:
        return String.format( "%04X", seg.getHex().length() + 4 );
      case 3:
        return String.format( "%02X", seg.get_Type() );
      case 4:
        return String.format( "%02X", seg.getFlags() );
      case 5:
        return seg.getHex().toString();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    Segment seg = getRow( row );
    if ( col == 3 )
    {
      int type = Integer.parseInt( ( String )value , 16 );
      seg.set_Type( type );
      if ( ( row == 0 || type != getRow( row - 1 ).get_Type() )
          && ( row == table.getRowCount() - 1 || type != getRow( row + 1 ).get_Type() ) )
      {
        sorted = false;
      }
    }
    else if ( col == 4 )
    {
      try
      {
        int flags = Integer.parseInt( ( String )value , 16 );
        if ( !useSavedData() )
        {
          // Do not allow bit 7 to be set to 0, as RMIR will delete the segment
          flags |= 0x80;
        }
        seg.setFlags( flags );
      }
      catch ( NumberFormatException e )
      {
        seg.setFlags( 0xFF );
      }
    }
    else if ( col == 5 )
    {
      seg.setHex( ( Hex )value );
    }
    changed = true;
    resetAddresses();
    fireTableDataChanged();
  }
  
  public boolean isChanged()
  {
    return changed;
  }

  public void setChanged( boolean changed )
  {
    this.changed = changed;
  }

  private boolean useSavedData()
  {
    return config.getOwner().useSavedData();
  }
  
  public void resetData()
  {
    set( config );
  }
  
  public void resetAddresses()
  {
    Remote remote = config.getRemote();
    int addr = remote.getBaseAddress();
    addr += remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
    for ( Segment seg : data )
    {
      seg.setAddress( addr );
      addr += seg.getHex().length() + 4;
    }
  }
  
  public void set( RemoteConfiguration config )
  {
    this.config = config;
    changed= false;
    Remote remote = config.getRemote();
    List< Segment > segments = new ArrayList< Segment >();
    short[] dataToShow = useSavedData() ? config.getSavedData() : config.getData();
    int base = remote.getBaseAddress();
    
    // first two bytes are checksum, and in XSight remotes next 18 bytes are E2 info
    int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
    int segLength = 0;
    while ( pos < remote.getEepromSize() && ( segLength = Hex.get( dataToShow, pos ) ) <= remote.getEepromSize() - pos  )
    {
      int segType = dataToShow[ pos + 2 ];
      int segFlags = dataToShow[ pos + 3 ];
      Hex segData = new Hex( dataToShow, pos + 4, segLength - 4 );
      Segment seg = new Segment( segType, segFlags, segData );
      seg.setAddress( base + pos );
      segments.add( seg );
      pos += segLength;
    }
    setData( segments );
    table.initColumns();
  }
  
  public void updateData()
  {
    Remote remote = config.getRemote();
    int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
    if ( useSavedData() )
    {
      pos = Segment.writeData( data, config.getSavedData(), pos );
      Hex.put( 0xFFFF, config.getData(), pos );
      return;
    }
    else
    {
      LinkedHashMap< Integer, List<Segment> > list = config.getSegments();
      int n = 0;
      for ( int type : config.getSegmentLoadOrder() )
      {
        List< Segment > segs = list.get( type );
        if ( segs == null )
        {
          continue;
        }
        if ( !isTypeEditable( type ) )
        {
          n += segs.size();
          continue;
        }
        for ( Segment seg : segs )
        {
          seg.setFlags( data.get( n ).getFlags() );
          seg.setHex( new Hex( data.get( n++ ).getHex() ) );
        }
      }
      propertyChangeSupport.firePropertyChange( "data", null, null );
    }
  }
  
  public boolean getSorted()
  {
    return sorted;
  }

  public void setSorted( boolean sorted )
  {
    this.sorted = sorted;
  }

  private class TextAreaRenderer extends JTextArea implements TableCellRenderer
  {
    private Color selectionBackground = null;
    private Color selectionForeground = null;
    private List< List< Integer > > rowColHeight = new ArrayList< List< Integer > >();
    
    public TextAreaRenderer() 
    {
      setLineWrap( true );
      setWrapStyleWord( true );
      setFont( lblFont );
      UIDefaults defaults = javax.swing.UIManager.getDefaults();
      selectionBackground = defaults.getColor("List.selectionBackground");
      selectionForeground = defaults.getColor("List.selectionForeground");
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int col) 
    {
      String text = ( String )value;
      setText( text );
      adjustRowHeight( table, row, col );
      setBackground( isSelected ? selectionBackground : Color.WHITE );
      setForeground( isSelected ? selectionForeground : useSavedData() ? Color.BLUE : Color.BLACK );
      return this;
    }
    
    private void adjustRowHeight(JTable table, int row, int column) {
      //The trick to get this to work properly is to set the width of the column to the
      //textarea. The reason for this is that getPreferredSize(), without a width tries
      //to place all the text in one line. By setting the size with the with of the column,
      //getPreferredSize() returns the proper height which the row should have in
      //order to make room for the text.
      int cWidth = table.getTableHeader().getColumnModel().getColumn( column ).getWidth();
      setSize( new Dimension( cWidth, 1000 ) );
      int prefH = getPreferredSize().height;
      while ( rowColHeight.size() <= row ) 
      {
        rowColHeight.add( new ArrayList<Integer>( column ) );
      }
      List<Integer> colHeights = rowColHeight.get( row );
      while ( colHeights.size() <= column ) 
      {
        colHeights.add( 0 );
      }
      colHeights.set( column, prefH );
      int maxH = prefH;
      for ( Integer colHeight : colHeights ) 
      {
        if ( colHeight > maxH ) 
        {
          maxH = colHeight;
        }
      }
      if ( table.getRowHeight(row) != maxH ) 
      {
        table.setRowHeight( row, maxH );
      }
    }
  }
  
  private class TextAreaEditor extends AbstractCellEditor implements TableCellEditor 
  {
    JComponent component = new JTextArea();
    JTextArea area = null;

    public TextAreaEditor()
    {
      super();
      area = ( JTextArea ) component;
      area.addKeyListener( new KeyAdapter()
      {
        public void keyPressed( KeyEvent e ) 
        {
          if ( e.isActionKey() || e.getKeyCode() == KeyEvent.VK_ENTER )
          {
            stopCellEditing();
          }
        }
      } );
    }
    
    @Override
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
        int rowIndex, int vColIndex) 
    {
      area.setFont( lblFont );
      area.setText( ( String ) value);
      area.setLineWrap( true );
      area.setWrapStyleWord( true );
      area.setForeground( useSavedData() ? Color.BLUE : Color.BLACK );
      return component;
    }

    @Override
    public Object getCellEditorValue() 
    {
      return ( new Hex( area.getText() ) );
    }
    
    @Override
    public boolean isCellEditable( EventObject event )
    {
      if ( event == null || !( event instanceof MouseEvent ) ||
          ( ( ( MouseEvent ) event).getClickCount() >= RMConstants.ClickCountToStart ) )
      {
        return true;
      }
      return false;
    }
  }
}
