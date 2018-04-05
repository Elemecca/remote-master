package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Protocol;

// TODO: Auto-generated Javadoc
/**
 * The Class CmdIndexInitializer.
 */
public class CmdIndexInitializer extends Initializer
{

  /**
   * Instantiates a new cmd index initializer.
   * 
   * @param parmIndex
   *          the parm index
   * @param protocol
   *          the protocol
   */
  public CmdIndexInitializer( int parmIndex, Protocol protocol )
  {
    this.parmIndex = parmIndex;
    this.protocol = protocol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Initializer#initialize(com.hifiremote.jp1.DeviceParameter[],
   * com.hifiremote.jp1.CmdParameter[])
   */
  @Override
  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    protocol.setCmdIndex( ( ( Integer )devParms[ parmIndex ].getValueOrDefault() ).intValue() );
  }

  /** The parm index. */
  private int parmIndex = 0;

  /** The protocol. */
  private Protocol protocol = null;
}
