package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

public class SegmentPanel extends RMPanel
{
  private SegmentTableModel model = null;
  private JP1Table table = null;
  
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
      int base = remote.getBaseAddress();
      
      // first two bytes are checksum, and in XSight remotes next 18 bytes are E2 info
      int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
      int segLength = 0;
      int segType = 0;
      while ( pos < remote.getEepromSize() && ( segLength = Hex.get( dataToShow, pos ) ) <= remote.getEepromSize() - pos  )
      {
        segType = dataToShow[ pos + 2 ];
        int segFlags = dataToShow[ pos + 3 ];
        Hex segData = new Hex( dataToShow, pos + 4, segLength - 4 );
        Segment seg = new Segment( segType, segFlags, segData );
        seg.setAddress( base + pos );
        segments.add( seg );
        pos += segLength;
      }
      model.set( remote, segments );
      table.initColumns();
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
}
