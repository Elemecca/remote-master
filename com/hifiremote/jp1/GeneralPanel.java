package com.hifiremote.jp1;

import java.awt.*;
import java.beans.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class GeneralPanel
  extends RMPanel
{
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

  public void set( RemoteConfiguration remoteConfig )
  {
    deviceModel.set( remoteConfig );
    settingModel.set( remoteConfig );
    String text = remoteConfig.getNotes();
    if ( text != null )
      notes.setText( remoteConfig.getNotes());
  }

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
  
  private DeviceButtonTableModel deviceModel = new DeviceButtonTableModel();
  private SettingsTableModel settingModel = new SettingsTableModel();
  private JTextArea notes = null;
}
  
