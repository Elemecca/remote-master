package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class ProtocolUpgradePanel
  extends RMTablePanel< ProtocolUpgrade >
{
  public ProtocolUpgradePanel()
  {
    super( new ProtocolUpgradeTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( ProtocolUpgradeTableModel )model ).set( remoteConfig ); 
  }
  
  public ProtocolUpgrade createRowObject()
  {
    return null;
  }
}
  
