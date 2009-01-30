package com.hifiremote.jp1;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalPanel.
 */
public class LearnedSignalPanel
  extends RMTablePanel< LearnedSignal >
{
  
  /**
   * Instantiates a new learned signal panel.
   */
  public LearnedSignalPanel()
  {
    super( new LearnedSignalTableModel());
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    (( LearnedSignalTableModel )model ).set( remoteConfig );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public LearnedSignal createRowObject( LearnedSignal learnedSignal )
  {
    LearnedSignalDialog.showDialog(( JFrame )SwingUtilities.getRoot( this ),
                                   learnedSignal );
    return null;
  }
}

