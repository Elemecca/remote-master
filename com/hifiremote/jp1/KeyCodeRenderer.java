package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;

public class KeyCodeRenderer
  extends DefaultTableCellRenderer
{
  private Remote remote;

  public KeyCodeRenderer(){}

  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }

  protected void setValue( Object value )
  {
    int keyCode = (( Integer )value ).intValue();
    super.setValue( remote.getButtonName( keyCode ));
  }

  private static DecimalFormat df = null;
}

