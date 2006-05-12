package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import java.awt.color.*;
import javax.swing.*;
import javax.swing.table.*;

public class RowNumberRenderer
  extends DefaultTableCellRenderer
{
  public RowNumberRenderer()
  {
    this( false );
  }

  public RowNumberRenderer( boolean useHex )
  {
    JButton b = new JButton();
    setBackground( b.getBackground());
    setBorder( BorderFactory.createRaisedBevelBorder());
    setHorizontalAlignment( SwingConstants.CENTER );
    setToolTipText( "Drag a row up or down to change the order." );

    this.useHex = useHex;
  }

  public Component getTableCellRendererComponent( JTable table, Object value, 
                                                  boolean isSelected, boolean hasFocus,
                                                  int row, int col )
  {
    if ( useHex )
      value = RemoteConfiguration.toHex((( Integer )value ).intValue());
    return super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
  }

  private boolean useHex = false;
}
