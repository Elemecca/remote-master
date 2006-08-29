package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

public class MacroButtonRenderer
  extends DefaultListCellRenderer
{
  private Remote remote = null;
  
  public MacroButtonRenderer(){}
  
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  
  public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
  {
    String text = remote.getButtonName((( Number )value ).intValue());
    return super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
  }
}
