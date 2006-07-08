package com.hifiremote.jp1;

import java.beans.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class RawDataPanel
  extends RMPanel
{
  public RawDataPanel()
  {
    model = new RawDataTableModel();
    JP1Table table = new JP1Table( model );

    table.setGridColor( Color.lightGray );
    table.getTableHeader().setResizingAllowed( false );
    table.setDefaultRenderer( UnsignedByte.class, byteRenderer );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.WEST );
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    model.set( remoteConfig );
    byteRenderer.setSavedData( remoteConfig.getSavedData());
  }
  
  public void addPropertyChangeListener( PropertyChangeListener l )
  {
    if (( model != null ) && ( l != null ))
      model.addPropertyChangeListener( l );
  }
  RawDataTableModel model = null;
  UnsignedByteRenderer byteRenderer = new UnsignedByteRenderer();
}
  
