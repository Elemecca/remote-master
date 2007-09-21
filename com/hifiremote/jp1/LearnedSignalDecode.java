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
    int[] temp = decodeIRCaller.getHex();
    int len = 0;
    for ( int i = 0; ( i < temp.length ) && ( temp[ i ] >= 0 ); ++i )
      ++len;
    hex = new int[ len ];
    System.arraycopy( temp, 0, hex, 0, len );
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
