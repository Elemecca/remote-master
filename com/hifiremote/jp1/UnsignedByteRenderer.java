package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class UnsignedByteRenderer.
 */
public class UnsignedByteRenderer extends DefaultTableCellRenderer
{

  /**
   * Instantiates a new unsigned byte renderer.
   */
  public UnsignedByteRenderer()
  {
    baseFont = getFont();
    boldFont = baseFont.deriveFont( Font.BOLD );
    setHorizontalAlignment( SwingConstants.CENTER );
  }
  
  /**
   * Sets the saved data.
   * 
   * @param savedData
   *          the new saved data
   */
  public void setSavedData( short[] savedData )
  {
    this.savedData = savedData;
  }

  public void setRemoteConfig( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    this.remoteConfig = remoteConfig;
    normalData = remoteConfig.getData();
    savedData = remoteConfig.getSavedData();
    baselineData = remoteConfig.getBaselineData();
    settingAddresses = remote.getSettingAddresses();
    highlight = remoteConfig.getHighlight();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object,
   * boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int col )
  {
    component = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
    offset = 16 * row + col - 1;
    short cellValue = ( ( UnsignedByte )value ).getValue();
    
    if ( savedData != null && cellValue != savedData[ offset ] )
    {
      component.setFont( boldFont );
    }
    else
    {
      component.setFont( baseFont );
    }
    if ( !isSelected )
    {
      component.setForeground( changed() ? Color.YELLOW : useSavedData() ? Color.BLUE : Color.BLACK );
    }
    return component;
  }

  @Override
  public void paint( Graphics g )
  {
    if ( remoteConfig != null && remoteConfig.allowHighlighting() && !useSavedData() )
    {
      Dimension d = component.getSize();
      int end = highlight.length - 1;
      if ( changed() )
      {
        g.setColor( Color.RED );
        g.fillRect( 0, 0, d.width, d.height );
      }
      else if ( settingAddresses.containsKey( offset ) && highlight[ offset ] == Color.WHITE )
      {
        for ( int i = 0; i < 8; i++ )
        {
          g.setColor( highlight[ end - 8 * settingAddresses.get( offset ) - i ] );
          g.fillRect( d.width - 3 * i - 3, 0, 2, d.height );
        }
      }
      else
      {
        g.setColor( highlight[ offset ] );
        g.fillRect( 0, 0, d.width, d.height );
      }
    }
    super.paint( g );
  }
  
  private boolean useSavedData()
  {
    return remoteConfig != null ? remoteConfig.getOwner().useSavedData() : false;
  }
  
  private boolean changed()
  {
    return baselineData != null && !useSavedData() && normalData[ offset ] != baselineData[ offset ];
  }
  
  private int offset;
  private RemoteConfiguration remoteConfig = null;
  private Component component = null;
  private HashMap< Integer, Integer > settingAddresses = null;
  private Color[] highlight = null;
 
  private short[] normalData = null;
  private short[] savedData = null;
  private short[] baselineData = null;

  /** The base font. */
  private Font baseFont = null;

  /** The bold font. */
  private Font boldFont = null;
}
