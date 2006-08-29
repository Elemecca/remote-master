package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class DeviceUpgradePanel
  extends RMTablePanel< DeviceUpgrade >
{
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( DeviceUpgradeTableModel )model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
  }
  
  public DeviceUpgrade createRowObject( DeviceUpgrade baseUpgrade )
  {
    System.err.println( "DeviceUpgradePanel.createRowObject()" );
    DeviceUpgrade upgrade = null;
    if ( baseUpgrade == null )
    {
      upgrade = new DeviceUpgrade();
      upgrade.setRemote( remoteConfig.getRemote());
    }
    else
      upgrade = new DeviceUpgrade( baseUpgrade );
      
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, table );
    Remote[] remotes = new Remote[ 1 ];
    remotes[ 0 ] = remoteConfig.getRemote();
    DeviceUpgradeEditor editor = new DeviceUpgradeEditor( rm, upgrade, remotes );
    return editor.getDeviceUpgrade();
  }

  private RemoteConfiguration remoteConfig;
}
  
