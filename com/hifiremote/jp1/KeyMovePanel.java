package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class KeyMovePanel
  extends RMTablePanel
{
  public KeyMovePanel()
  {
    super( new KeyMoveTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( KeyMoveTableModel )model ).set( remoteConfig ); 
  }
  
  protected Object createRowObject()
  {
    return null;
  }
}
  