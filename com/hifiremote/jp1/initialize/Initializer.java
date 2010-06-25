package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class Initializer.
 */
public abstract class Initializer
{

  /**
   * Initialize.
   * 
   * @param devParms
   *          the dev parms
   * @param cmdParms
   *          the cmd parms
   */
  public abstract void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms );
}
