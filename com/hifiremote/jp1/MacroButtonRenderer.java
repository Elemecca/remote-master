package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroButtonRenderer.
 */
public class MacroButtonRenderer
  extends DefaultListCellRenderer
{
  
  /** The remote. */
  private Remote remote = null;
  
  /**
   * Instantiates a new macro button renderer.
   */
  public MacroButtonRenderer(){}
  
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
    int iVal = ( ( Number )value ).intValue();
    String text = remote.getButtonName( iVal & 0xFF );
    int duration = iVal >> 8;
    if ( duration > 0 )
    {
      text += "(" + duration / 10 + "." + duration % 10 + ")";
    }
    return super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
  }
}
