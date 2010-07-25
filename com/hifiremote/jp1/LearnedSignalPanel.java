package com.hifiremote.jp1;

import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalPanel.
 */
public class LearnedSignalPanel extends RMTablePanel< LearnedSignal >
{

  /**
   * Instantiates a new learned signal panel.
   */
  public LearnedSignalPanel()
  {
    super( new LearnedSignalTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( LearnedSignalTableModel )model ).set( remoteConfig );
    table.initColumns( model );
    newButton.setEnabled( remoteConfig != null && remoteConfig.getRemote().getLearnedAddress() != null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  @Override
  public LearnedSignal createRowObject( LearnedSignal learnedSignal )
  {
    LearnedSignalDialog.showDialog( SwingUtilities.getRoot( this ), learnedSignal );
    return null;
  }
}
