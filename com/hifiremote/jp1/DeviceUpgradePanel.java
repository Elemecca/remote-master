package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceUpgradePanel.
 */
public class DeviceUpgradePanel extends RMTablePanel< DeviceUpgrade >
{

  /**
   * Instantiates a new device upgrade panel.
   */
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( DeviceUpgradeTableModel ) model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
    table.initColumns(model);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public DeviceUpgrade createRowObject( DeviceUpgrade baseUpgrade )
  {
    System.err.println( "DeviceUpgradePanel.createRowObject()" );
    DeviceUpgrade upgrade = null;
    if ( baseUpgrade == null )
    {
      upgrade = new DeviceUpgrade();
      upgrade.setRemote( remoteConfig.getRemote() );
    }
    else
      upgrade = new DeviceUpgrade( baseUpgrade );

    RemoteMaster rm = ( RemoteMaster ) SwingUtilities
        .getAncestorOfClass( RemoteMaster.class, table );
    List< Remote > remotes = new ArrayList< Remote >( 1 );
    remotes.add( remoteConfig.getRemote() );
    DeviceUpgradeEditor editor = new DeviceUpgradeEditor( rm, upgrade, remotes );
    upgrade = editor.getDeviceUpgrade();
    if ( upgrade == null )
      return null;

    int boundDeviceButtonIndex = remoteConfig.findBoundDeviceButtonIndex( upgrade );
    if ( boundDeviceButtonIndex == -1 )
    {
       return upgrade;
    }

    java.util.List< KeyMove > upgradeKeyMoves = upgrade.getKeyMoves();
    java.util.List< KeyMove > keyMoves = remoteConfig.getKeyMoves();
    for ( KeyMove upgradeKeyMove : upgradeKeyMoves )
    {
      for ( ListIterator< KeyMove > li = keyMoves.listIterator(); li.hasNext(); )
      {
        KeyMove keyMove = li.next();
        if ( keyMove.getDeviceButtonIndex() != boundDeviceButtonIndex )
          continue;
        if ( keyMove.getKeyCode() == upgradeKeyMove.getKeyCode() )
        {
          li.remove();
          Remote remote = remoteConfig.getRemote();
          System.err.println( "Removed keyMove assigned to "
              + remote.getDeviceButtons()[ boundDeviceButtonIndex ] + ':'
              + remote.getButtonName( keyMove.getKeyCode() )
              + " since there is one assigned in the device upgrade" );
          break;
        }
      }
    }
    return upgrade;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig;
}
