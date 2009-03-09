package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroPanel.
 */
public class MacroPanel extends RMTablePanel< Macro >
{

  /**
   * Instantiates a new macro panel.
   */
  public MacroPanel()
  {
    super( new MacroTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( MacroTableModel )model ).set( remoteConfig );
    table.initColumns( model );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public Macro createRowObject( Macro baseMacro )
  {
    return MacroDialog.showDialog( this, baseMacro, ( ( MacroTableModel )model ).getRemoteConfig() );
  }

}
