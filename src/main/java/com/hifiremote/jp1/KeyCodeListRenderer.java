package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyCodeListRenderer.
 */
public class KeyCodeListRenderer
  extends DefaultListCellRenderer
{
  
  /* (non-Javadoc)
   * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public Component getListCellRendererComponent( JList list,
                                                 Object value,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus )
  {
    int keyCode = (( Integer )value ).intValue();
    String text = remote.getButtonName( keyCode );
    return super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
  }
  
  /**
   * Sets the remote.
   * 
   * @param remote the new remote
   */
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  
  /** The remote. */
  private Remote remote = null;
}
