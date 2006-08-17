package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

public class KeyCodeListRenderer
  extends DefaultListCellRenderer
{
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
  
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  private Remote remote = null;
}
