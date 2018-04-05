package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class RowNumberRenderer.
 */
public class RowNumberRenderer extends DefaultTableCellRenderer
{

  /**
   * Instantiates a new row number renderer.
   */
  public RowNumberRenderer()
  {
    this( false );
  }

  /**
   * Instantiates a new row number renderer.
   * 
   * @param useHex
   *          the use hex
   */
  public RowNumberRenderer( boolean useHex )
  {
    JButton b = new JButton();
    setBackground( b.getBackground() );
    setBorder( BorderFactory.createRaisedBevelBorder() );
    setHorizontalAlignment( SwingConstants.CENTER );
    setToolTipText( "Drag a row up or down to change the order." );

    this.useHex = useHex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object,
   * boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int col )
  {
    if ( useHex )
      value = String.format( "%04X:", ( ( Integer )value ).intValue() );
    return super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
  }

  /** The use hex. */
  private boolean useHex = false;
}
