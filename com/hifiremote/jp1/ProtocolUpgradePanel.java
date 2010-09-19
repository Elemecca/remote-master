package com.hifiremote.jp1;

import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolUpgradePanel.
 */
public class ProtocolUpgradePanel extends RMTablePanel< ProtocolUpgrade >
{

  /**
   * Instantiates a new protocol upgrade panel.
   */
  public ProtocolUpgradePanel()
  {
    super( new ProtocolUpgradeTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( ProtocolUpgradeTableModel )model ).set( remoteConfig );
    table.initColumns( model );
    this.remoteConfig = remoteConfig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public ProtocolUpgrade createRowObject( ProtocolUpgrade baseUpgrade )
  {
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
    Remote remote = remoteConfig.getRemote();
    ManualProtocol mp = null;
    if ( baseUpgrade == null )
    {      
      mp = new ManualProtocol( null, null );
    }
    else 
    {
      int pid = baseUpgrade.getPid();
      short[] hex = new short[ 2 ];
      hex[ 0 ] = ( short )( pid / 0x100 );
      hex[ 1 ] = ( short )( pid % 0x100 );

      Protocol p = ProtocolManager.getProtocolManager().findProtocolForRemote( remote, new Hex( hex ), true );
      if ( p != null && ( p instanceof ManualProtocol ) )
      {
        mp = ( ManualProtocol )p;
      }
      else
      {
        return null;
      }
    }
    
    ManualSettingsDialog d = new ManualSettingsDialog( rm, mp );
    d.setVisible( true );
    mp = d.getProtocol();
    if ( mp != null && mp.getCode( remote ) != null )
    {
      mp.setName( "Manual Settings: " + mp.getID().toString() );
      ProtocolManager.getProtocolManager().add( mp );
      ProtocolUpgrade pu = new ProtocolUpgrade( mp.getID().get( 0 ), mp.getCode( remote ), null );
      return pu;
    }

    return null;
  }

  private RemoteConfiguration remoteConfig = null;
}


