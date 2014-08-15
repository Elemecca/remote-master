package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
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

public class SegmentTableModel extends JP1TableModel< Segment >
{
  private RemoteConfiguration config = null;
  
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
          && !Arrays.asList( RemoteConfiguration.segmentsKnown ).contains( type );
    }
  }
  
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    Segment seg = getRow( row );
    return ( isTypeEditable( seg.get_Type() ) && col > 3 );
  }
  
  public void set( RemoteConfiguration config, List< Segment > segments )
  {
    this.config = config;
    setData( segments );
    fireTableDataChanged();
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
    if ( col == 5 )
    {
      seg.setHex( ( Hex )value );
    }
  }
  
  private boolean useSavedData()
  {
    return config.getOwner().useSavedData();
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
          seg.setHex( new Hex( data.get( n++ ).getHex() ) );
        }
      }
      propertyChangeSupport.firePropertyChange( "data", null, null );
    }
    
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

    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
        int rowIndex, int vColIndex) 
    {
      JTextArea area = ( JTextArea ) component;
      area.setFont( lblFont );
      area.setText( ( String ) value);
      area.setLineWrap( true );
      area.setWrapStyleWord( true );
      return component;
    }

    public Object getCellEditorValue() {
      return ( new Hex( ( ( JTextArea )component ).getText() ) );
    }
  }
}
