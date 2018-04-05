package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FavScanButtonRenderer extends DefaultListCellRenderer
{
  
  /** The remote. */
  private Remote remote = null;
  
  /**
   * Instantiates a new macro button renderer.
   */
  public FavScanButtonRenderer(){}
  
  /**
   * Sets the remote.
   * 
   * @param remote the new remote
   */
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
  {
    int buttonIndex = (( Number )value ).intValue();
    String text = null;
    if ( buttonIndex == 0 )
    {
      text = "{Pause}";
    }
    else
    {
      text = remote.getButtonName( buttonIndex );
    }
    return super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
  }
}
