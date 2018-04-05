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
    this.remoteConfig = remoteConfig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public ProtocolUpgrade createRowObject( ProtocolUpgrade baseUpgrade )
  {
    Remote remote = remoteConfig.getRemote();
    ManualProtocol mp = null;
    boolean pidEditable = true;
    if ( ( baseUpgrade == null ) && useNewName )
    {
      // New
      mp = new ManualProtocol( null, null );
    }
    else if ( useNewName )
    {
      // Clone
      mp = new ManualProtocol( baseUpgrade.getManualProtocol( remote ).getIniSection() );
    }
    else 
    {
      // Edit
      pidEditable = false;
      mp = baseUpgrade.getManualProtocol( remote );
    }
    
    ManualSettingsDialog d = new ManualSettingsDialog( remoteConfig.getOwner(), mp );
    d.pid.setEditable( pidEditable );
    d.pid.setEnabled( pidEditable );
    d.remoteConfig = remoteConfig;
    if ( remote != null )
    {
      d.setSelectedCode( remote.getProcessor() );
      d.setMessage( 0 );
    }
    d.setVisible( true );
    mp = d.getProtocol();
    if ( mp != null )
    {
      if ( useNewName )
      {
        mp.setName( ManualProtocol.getDefaultName( mp.getID() ) );
        ProtocolManager.getProtocolManager().add( mp );
      }
      return mp.getProtocolUpgrade( remote );
    }

    return null;
  }
 
  @Override
  protected void newRowObject( ProtocolUpgrade baseUpgrade, int row, int modelRow, boolean select )
  {
    useNewName = true;
    super.newRowObject( baseUpgrade, row, modelRow, select );
  }
  
  @Override
  protected void editRowObject( int row )
  {
    useNewName = false;
    super.editRowObject( row );
  }
  
  private RemoteConfiguration remoteConfig = null;
  
  private boolean useNewName = true;

}


