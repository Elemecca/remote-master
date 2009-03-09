package com.hifiremote.jp1;

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
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public ProtocolUpgrade createRowObject( ProtocolUpgrade baseUpgrade )
  {
    return null;
  }
}
