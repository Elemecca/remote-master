package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;

// TODO: Auto-generated Javadoc
/**
 * The Class RawDataPanel.
 */
public class RawDataPanel extends RMPanel
{

  /**
   * Instantiates a new raw data panel.
   */
  public RawDataPanel()
  {
    model = new RawDataTableModel();
    JP1Table table = new JP1Table( model );
    table.initColumns( model );
    table.setGridColor( Color.lightGray );
    table.getTableHeader().setResizingAllowed( false );
    table.setDefaultRenderer( UnsignedByte.class, byteRenderer );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.WEST );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    model.set( remoteConfig.getData(), remoteConfig.getRemote().getBaseAddress() );
    byteRenderer.setSavedData( remoteConfig.getSavedData() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener l )
  {
    if ( ( model != null ) && ( l != null ) )
      model.addPropertyChangeListener( l );
  }

  /** The model. */
  RawDataTableModel model = null;

  /** The byte renderer. */
  UnsignedByteRenderer byteRenderer = new UnsignedByteRenderer();
}
