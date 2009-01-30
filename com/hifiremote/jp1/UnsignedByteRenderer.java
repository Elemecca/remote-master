package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class UnsignedByteRenderer.
 */
public class UnsignedByteRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new unsigned byte renderer.
   */
  public UnsignedByteRenderer()
  {
    baseFont = getFont();
    boldFont = baseFont.deriveFont( Font.BOLD );
  }
  
  /**
   * Sets the saved data.
   * 
   * @param savedData the new saved data
   */
  public void setSavedData( short[] savedData )
  {
    this.savedData = savedData;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int col )
  {
    Component c = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
    if ((( savedData != null ) && ((( UnsignedByte )value ).getValue()) != savedData[ 16 * row + col - 1 ]))
    {
      if ( isSelected )
        c.setForeground( Color.YELLOW );
      else
        c.setForeground( Color.RED );
      c.setFont( boldFont );
    }
    else
    {
      if ( isSelected )
        c.setForeground( Color.WHITE );
      else
        c.setForeground( Color.BLACK );
      c.setFont( baseFont );
    }
    return c;
  }

  /** The saved data. */
  private short[] savedData = null;
  
  /** The base font. */
  private Font baseFont = null;
  
  /** The bold font. */
  private Font boldFont = null;
}
