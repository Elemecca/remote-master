package com.hifiremote.jp1;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class EFCRenderer.
 */
public class EFCRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new eFC renderer.
   */
  public EFCRenderer()
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( value.toString());
    else
      super.setValue( value );
  }
}

