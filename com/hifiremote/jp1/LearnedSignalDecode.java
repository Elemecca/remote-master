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

    // temporary until DecodeIR is fixed to return an array of the correct length
    int[] src = decodeIRCaller.getHex();
    int len = 0;
    for ( len = 0; len < src.length; ++len )
    {
      if ( src[ len ] == -1 )
        break;
    }
    hex = new int[ len ];
    System.arraycopy( src, 0, hex, 0, len );

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
