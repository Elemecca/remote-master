package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralPanel.
 */
public class GeneralPanel
  extends RMPanel
{
  
  /**
   * Instantiates a new general panel.
   */
  public GeneralPanel()
  {
    JPanel panel = new JPanel( new BorderLayout( 5, 0 ));
    add( panel, BorderLayout.NORTH );

    // first the device button table.
    // JTableX table = new JTableX( deviceModel );
    JP1Table table = new JP1Table( deviceModel );
    JScrollPane scrollPane = new JScrollPane( table, 
                                              JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Device Buttons" ),
        scrollPane.getBorder()));
    panel.add( scrollPane, BorderLayout.LINE_START );

    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 10 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );
    table.initColumns( deviceModel );

    // now the other settings table
    table = new JP1Table( settingModel );
    table.setCellEditorModel( settingModel );
    table.initColumns( settingModel );

    scrollPane = new JScrollPane( table );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Other Settings" ),
        scrollPane.getBorder()));
    panel.add( scrollPane, BorderLayout.CENTER );

    d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    d.height = 10 * table.getRowHeight();
    table.setPreferredScrollableViewportSize( d );

    notes = new JTextArea( 10, 20 );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    scrollPane = new JScrollPane( notes );
    scrollPane.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "General Notes" ),
        scrollPane.getBorder()));

    add( scrollPane, BorderLayout.CENTER );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    deviceModel.set( remoteConfig );
    settingModel.set( remoteConfig );
    String text = remoteConfig.getNotes();
    if ( text != null )
      notes.setText( remoteConfig.getNotes());
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( deviceModel != null )
        deviceModel.addPropertyChangeListener( listener );
      if ( settingModel != null )
        settingModel.addPropertyChangeListener( listener );
    }
  }
  
  /** The device model. */
  private DeviceButtonTableModel deviceModel = new DeviceButtonTableModel();
  
  /** The setting model. */
  private SettingsTableModel settingModel = new SettingsTableModel();
  
  /** The notes. */
  private JTextArea notes = null;
}
  
