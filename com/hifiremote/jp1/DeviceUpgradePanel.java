package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;

public class DeviceUpgradePanel
  extends RMTablePanel
{
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( DeviceUpgradeTableModel )model ).set( remoteConfig ); 
  }
  
  public Object createRowObject()
  {
    return null;
  }
}
  
