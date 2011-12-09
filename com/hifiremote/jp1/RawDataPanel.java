package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
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
    JP1Table table = new JP1Table( model )
    {
      @Override
      public String getToolTipText( MouseEvent e ) 
      {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int row = rowAtPoint( p );
        int col = columnAtPoint( p );
        int offset = 16 * row + col - 1;
        boolean showTip = false;
        if ( col != 0 && settingAddresses.containsKey( offset ) ) 
        { 
          tip = "Highlighted bits: ";
          int end = highlight.length - 1;
          for ( int i = 0; i < 8; i++ )
          {
            if ( !highlight[ end - 8 * settingAddresses.get( offset ) - i ].equals( Color.WHITE ) )
            {
              tip += i;
              showTip = true;
            }
          }
        } 
        return showTip ? tip : null;
      }
    };
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
    
    infoBox = Box.createVerticalBox();
    infoBox.setAlignmentX( LEFT_ALIGNMENT );
    infoBox.setBorder( BorderFactory.createEmptyBorder( 20, 10, 5, 5 ) );
    add( infoBox, BorderLayout.CENTER );

    infoBox.add( signatureLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( processorLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( interfaceLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( extenderLabel );
    infoBox.add( Box.createVerticalGlue());
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      model.set( remoteConfig.getData(), remote.getBaseAddress() );
      byteRenderer.setRemoteConfig( remoteConfig );
      highlight = remoteConfig.getHighlight();
      settingAddresses = remote.getSettingAddresses();
      String sig = remoteConfig.getSigString();
      if ( sig == null )
      {
        sig = remote.getSignature();
      }
      signatureLabel.setText( "Signature:  " + sig );
      processorLabel.setText( "Processor:  " + remote.getProcessorDescription() );
      interfaceLabel.setText( "Interface:  " + remote.getInterfaceType() );
      if ( remote.getExtenderVersionParm() != null )
      {
        extenderLabel.setText( "Extender version:  " + 
            remote.getExtenderVersionParm().getExtenderVersion( remoteConfig ) );
      }
      else
      {
        extenderLabel.setText( "" );
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener( PropertyChangeListener l )
  {
    if ( model != null && l != null )
    {
      model.addPropertyChangeListener( l );
    }
  }

  /** The model. */
  RawDataTableModel model = null;

  /** The byte renderer. */
  UnsignedByteRenderer byteRenderer = new UnsignedByteRenderer();
  
  JLabel signatureLabel = new JLabel();  
  JLabel processorLabel = new JLabel();  
  JLabel interfaceLabel = new JLabel();  
  JLabel extenderLabel = new JLabel();
  
  Box infoBox = null; 
  
  private Color[] highlight = null;
  private HashMap< Integer, Integer >settingAddresses = null;
  
}
