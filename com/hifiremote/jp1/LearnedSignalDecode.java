package com.hifiremote.jp1;

import com.hifiremote.decodeir.DecodeIRCaller;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalDecode.
 */
public class LearnedSignalDecode
{

  /**
   * Instantiates a new learned signal decode.
   * 
   * @param decodeIRCaller
   *          the decode ir caller
   */
  public LearnedSignalDecode( DecodeIRCaller decodeIRCaller )
  {
    protocolName = decodeIRCaller.getProtocolName();
    device = decodeIRCaller.getDevice();
    subDevice = decodeIRCaller.getSubDevice();
    obc = decodeIRCaller.getOBC();
    int[] temp = decodeIRCaller.getHex();
    int len = 0;
    for ( int i = 0; i < temp.length && temp[ i ] >= 0; ++i )
    {
      ++len;
    }
    hex = new int[ len ];
    System.arraycopy( temp, 0, hex, 0, len );
    miscMessage = decodeIRCaller.getMiscMessage();
    errorMessage = decodeIRCaller.getErrorMessage();
  }

  public LearnedSignalDecode( LearnedSignalDecode decode )
  {
    protocolName = decode.protocolName;
    device = decode.device;
    subDevice = decode.subDevice;
    obc = decode.obc;
    hex = decode.hex;
    miscMessage = decode.miscMessage;
    errorMessage = decode.errorMessage;
    ignore = decode.ignore;
  }

  /** The protocol name. */
  public String protocolName = null;

  /** The device. */
  public int device = 0;

  /** The sub device. */
  public int subDevice = 0;

  /** The obc. */
  public int obc = 0;

  /** The hex. */
  public int[] hex;

  /** The misc message. */
  public String miscMessage = null;

  /** The error message. */
  public String errorMessage = null;

  public boolean ignore = false;
}
