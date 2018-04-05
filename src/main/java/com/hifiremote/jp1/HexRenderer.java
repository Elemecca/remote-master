package com.hifiremote.jp1;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class HexRenderer.
 */
public class HexRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new hex renderer.
   */
  public HexRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( (( Hex )value ).toString());
    else
      super.setValue( value );
  }
}

