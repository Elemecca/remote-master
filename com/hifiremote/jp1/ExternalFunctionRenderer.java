package com.hifiremote.jp1;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class ExternalFunctionRenderer.
 */
public class ExternalFunctionRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new external function renderer.
   */
  public ExternalFunctionRenderer()
  {
    setHorizontalAlignment( SwingConstants.LEFT );
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    Object rc = null;
    if ( value != null )
    {
      ExternalFunction f = ( ExternalFunction )value;
      rc = f.getValue();
    }

    super.setValue( rc );
  }
}

