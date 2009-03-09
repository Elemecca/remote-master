package com.hifiremote.jp1;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class SpecialFunctionPanel.
 */
public class SpecialFunctionPanel extends RMTablePanel< SpecialProtocolFunction >
{

  /**
   * Instantiates a new special function panel.
   */
  public SpecialFunctionPanel()
  {
    super( new SpecialFunctionTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( SpecialFunctionTableModel )model ).set( remoteConfig );
    table.initColumns( model );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  protected SpecialProtocolFunction createRowObject( SpecialProtocolFunction baseFunction )
  {
    return SpecialFunctionDialog.showDialog( ( JFrame )SwingUtilities.getRoot( this ), baseFunction,
        ( ( SpecialFunctionTableModel )model ).getRemoteConfig() );
  }
}
