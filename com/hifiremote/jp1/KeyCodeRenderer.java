package com.hifiremote.jp1;

import javax.swing.table.DefaultTableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyCodeRenderer.
 */
public class KeyCodeRenderer extends DefaultTableCellRenderer
{

  /** The remote. */
  private Remote remote;

  /**
   * Instantiates a new key code renderer.
   */
  public KeyCodeRenderer()
  {}

  /**
   * Sets the remote.
   * 
   * @param remote
   *          the new remote
   */
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
   */
  protected void setValue( Object value )
  {
    if ( value == null )
      super.setValue( null );
    else
    {
      int keyCode = ( ( Integer )value ).intValue();
      super.setValue( remote.getButtonName( keyCode ) );
    }
  }
}
