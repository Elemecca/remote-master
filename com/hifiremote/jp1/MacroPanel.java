package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class MacroPanel
  extends RMTablePanel< Macro >
{
  public MacroPanel()
  {
    super( new MacroTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( MacroTableModel )model ).set( remoteConfig );
  }
  
  public Macro createRowObject()
  {
    return null;
  }

}
  
