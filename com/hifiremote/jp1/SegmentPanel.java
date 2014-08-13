package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;
import javax.swing.table.TableCellRenderer;

public class SegmentPanel extends RMPanel
{
  private SegmentTableModel model = null;
  private JP1Table table = null;
  private Font lblFont = ( new JLabel() ).getFont();
  private List< List< Integer > > rowColHeight = new ArrayList< List< Integer > >();
  
  public SegmentPanel()
  {    
    model = new SegmentTableModel();
    table = new JP1Table( model );
    table.setGridColor( Color.lightGray );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.CENTER );
  }

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    Remote remote = null;
    if ( remoteConfig != null && remoteConfig.hasSegments() && !( remote = remoteConfig.getRemote() ).isSSD() )
    {
      RemoteMaster rm = remoteConfig.getOwner();
      List< Segment > segments = new ArrayList< Segment >();
      short[] dataToShow = rm.useSavedData() ? remoteConfig.getSavedData() : remoteConfig.getData();
      
      // first two bytes are checksum, and in XSight remotes next 18 bytes are E2 info
      int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
      int segLength = 0;
      int segType = 0;
      while ( pos < remote.getEepromSize() && ( segLength = Hex.get( dataToShow, pos ) ) <= remote.getEepromSize() - pos  )
      {
        segType = dataToShow[ pos + 2 ];
        int segFlags = dataToShow[ pos + 3 ];
        Hex segData = new Hex( dataToShow, pos + 4, segLength - 4 );
        segments.add( new Segment( segType, segFlags, segData ) );
        pos += segLength;
      }
      model.setData( segments );
      table.initColumns();
      table.getColumnModel().getColumn(4).setCellRenderer( new TextAreaRenderer() );
    }
  }

  @Override
  public void addRMPropertyChangeListener( PropertyChangeListener l )
  {
    if ( model != null && l != null )
    {
      model.addPropertyChangeListener( l );
    }
  }
  
  private class TextAreaRenderer extends JTextArea implements TableCellRenderer
  {
    private Color selectionBackground = null;
    private Color selectionForeground = null;
    
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
      setForeground( isSelected ? selectionForeground : Color.BLACK );
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
}
