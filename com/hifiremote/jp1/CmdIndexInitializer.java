package com.hifiremote.jp1;

import java.util.*;

public class CmdIndexInitializer
  extends Initializer
{
  public CmdIndexInitializer( int parmIndex, Protocol protocol )
  {
    this.parmIndex = parmIndex;
    this.protocol = protocol;
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    protocol.setCmdIndex((( Integer )devParms[ parmIndex ].getValueOrDefault()).intValue());
  } 

  private int parmIndex = 0;
  private Protocol protocol = null;
}
