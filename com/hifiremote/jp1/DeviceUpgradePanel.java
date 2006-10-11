package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
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
    upgrade = editor.getDeviceUpgrade();
    if ( upgrade == null )
      return null;
    
    int boundDeviceButtonIndex = remoteConfig.findBoundDeviceButtonIndex( upgrade );
    if ( boundDeviceButtonIndex == -1 )
      return upgrade;
    
    java.util.List< KeyMove > upgradeKeyMoves = upgrade.getKeyMoves();
    java.util.List< KeyMove > keyMoves = remoteConfig.getKeyMoves();
    for ( KeyMove upgradeKeyMove : upgradeKeyMoves )
    {
      for ( ListIterator< KeyMove > li = keyMoves.listIterator(); li.hasNext(); )
      {
        KeyMove keyMove = li.next();
        if ( keyMove.getDeviceButtonIndex() != boundDeviceButtonIndex )
          continue;
        if ( keyMove.getKeyCode() == upgradeKeyMove.getKeyCode())
        {
          li.remove();
          Remote remote = remoteConfig.getRemote();
          System.err.println( "Removed keyMove assigned to " + remote.getDeviceButtons()[ boundDeviceButtonIndex ] + 
                              ':' + remote.getButtonName( keyMove.getKeyCode()) + " since there is one assigned in the device upgrade" );
          break;
        }
      }
    }
    return upgrade;
  }

  private RemoteConfiguration remoteConfig;
}
  
