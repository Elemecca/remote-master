package com.hifiremote.jp1;

import com.hifiremote.decodeir.*;

public class LearnedSignalDecode
{
  public LearnedSignalDecode( DecodeIRCaller decodeIRCaller )
  {
    protocolName = decodeIRCaller.getProtocolName();
    device = decodeIRCaller.getDevice();
    subDevice = decodeIRCaller.getSubDevice();
    obc = decodeIRCaller.getOBC();
    hex = decodeIRCaller.getHex();
    miscMessage = decodeIRCaller.getMiscMessage();
    errorMessage = decodeIRCaller.getErrorMessage();
  }

  public String protocolName = null;
  public int device = 0;
  public int subDevice = 0;
  public int obc = 0;
  public int[] hex;
  public String miscMessage = null;
  public String errorMessage = null;
}
