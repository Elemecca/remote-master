package com.hifiremote.jp1;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class ByteRenderer.
 */
public class ByteRenderer
  extends DefaultTableCellRenderer
{
  
  /**
   * Instantiates a new byte renderer.
   */
  public ByteRenderer()
  {
    this( 10 );
  }

  /**
   * Instantiates a new byte renderer.
   * 
   * @param base the base
   */
  public ByteRenderer( int base )
  {
    setHorizontalAlignment( SwingConstants.CENTER );
  }

  /* (non-Javadoc)
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    if ( value != null )
      super.setValue( Integer.toString((( Integer )value ).intValue(), base ));
    else
      super.setValue( value );
  }

  /**
   * Sets the base.
   * 
   * @param base the new base
   */
  public void setBase( int base )
  {
    this.base = base;
  }

  /**
   * Gets the base.
   * 
   * @return the base
   */
  public int getBase(){ return base; }

  /** The base. */
  int base = 10;
}

