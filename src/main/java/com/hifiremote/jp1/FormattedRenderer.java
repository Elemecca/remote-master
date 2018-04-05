package com.hifiremote.jp1;

import java.text.Format;

import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class FormattedRenderer.
 */
public class FormattedRenderer
  extends DefaultTableCellRenderer
{
  
  /** The format. */
  private Format format;
  
  /**
   * Instantiates a new formatted renderer.
   * 
   * @param format the format
   */
  public FormattedRenderer( Format format )
  {
    super();
    this.format = format;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    super.setValue( format.format( value ));
  }
}
