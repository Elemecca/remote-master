/*
  Copyright (C) 2005  John Fine <john.fine@comcast.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

  Rewrite Sep 23, 2006  John Fine <john.fine@comcast.net>
  Inspired by, but not ultimately copying much from, a related module
  by Mineharu Takahara <mtakahar@yahoo.com>

  Edited Sep 28, 2009 by Graham Dixon to provide three-argument form of
  setBursts, which enables the count of extra bursts (those after the repeat
  part) to be passed to DecodeIR.
 */

//  Usage
//
//  To prepare to decode a signal, call all three of the following in any order
//
//     setBursts( int[] b, int r ) or setBursts( int[] b, int r, int e );
//     setFrequency( int f );
//     initDecoder();
//
//     b is an array of durations in microseconds.  All numbers should be positive.
//          Even (0 based) positions are On durations, odd positions are Off durations.
//     r is the length of the repeat part of b.
//     e is the length of the extra part of b (the part after the repeat part).
//     b.size()-r-e is the length of the one time part.
//     f is the frequency in hertz.
//
//  The two-argument form of setBursts is retained for compatibility, to allow
//  this class to be used with legacy code.  It is equivalent to the three-
//  argument form with e=0, but unless the array b really does not contain
//  extra bursts, or r is set to 0 (which makes all bursts be treated as
//  one-time) it will give the wrong value for the one-time count.
//
//  A signal may have multiple decodes.  For each decode call
//     decode()
//  If the result is true, call each of the following in any sequence to get the results
//
//     getProtocolName();
//     getDevice()
//     getSubDevice()
//     getOBC()
//     getHex()
//     getMiscMessage()
//     getErrorMessage()
package com.hifiremote.decodeir;

import java.io.File;

import com.hifiremote.LibraryLoader;

/**
 * The Class DecodeIRCaller.
 */
public class DecodeIRCaller
{

  /** The bursts. */
  private int[] bursts;

  /** The repeat part. */
  private int repeatPart;

  /** The extra part. */
  private int extraPart;

  /** The frequency. */
  private int frequency;

  /** The decoder_ctx. */
  private int[] decoder_ctx = new int[ 2 ];

  /** The device. */
  private int device;

  /** The sub device. */
  private int subDevice;

  /** The obc. */
  private int obc;

  /** The hex. */
  private int hex[] = new int[ 4 ];

  /** The protocol name. */
  private String protocolName = new String( "" );

  /** The misc message. */
  private String miscMessage = new String( "" );

  /** The error message. */
  private String errorMessage = new String( "" );

  /**
   * Instantiates a new decode ir caller.
   * 
   * @param folder
   *          the folder
   * @throws UnsatisfiedLinkError
   *           the unsatisfied link error
   */
  public DecodeIRCaller( File folder ) throws UnsatisfiedLinkError
  {
    System.err.println( "DecodeIR: Looking in '" + folder.getAbsolutePath() + "'." );
    LibraryLoader.loadLibrary( folder, libraryName );
  }

  /**
   * Instantiates a new decode ir caller.
   * 
   * @throws UnsatisfiedLinkError
   *           the unsatisfied link error
   */
  public DecodeIRCaller() throws UnsatisfiedLinkError
  {
    LibraryLoader.loadLibrary( libraryName );
  }

  /**
   * Sets the bursts.
   * 
   * @param b
   *          the b
   * @param r
   *          the r
   */

  // GD 2009 Added extraPart=0 to 2-arg form of setBursts and created
  // 3-arg form
  public void setBursts( int[] b, int r )
  {
    bursts = b;
    repeatPart = r;
    extraPart = 0;
  }

  public void setBursts( int[] b, int r, int e )
  {
    bursts = b;
    repeatPart = r;
    extraPart = e;
  }

  /**
   * Sets the frequency.
   * 
   * @param f
   *          the new frequency
   */
  public void setFrequency( int f )
  {
    frequency = f;
  }

  /**
   * Inits the decoder.
   */
  public void initDecoder()
  {
    decoder_ctx[ 0 ] = decoder_ctx[ 1 ] = 0;
  }

  /**
   * Decode.
   * 
   * @return true, if successful
   */
  public synchronized boolean decode()
  {
    return decode2( decoder_ctx, bursts, repeatPart, extraPart, frequency );
  }

  /**
   * Gets the protocol name.
   * 
   * @return the protocol name
   */
  public String getProtocolName()
  {
    return protocolName;
  }

  /**
   * Gets the device.
   * 
   * @return the device
   */
  public int getDevice()
  {
    return device;
  }

  /**
   * Gets the sub device.
   * 
   * @return the sub device
   */
  public int getSubDevice()
  {
    return subDevice;
  }

  /**
   * Gets the oBC.
   * 
   * @return the oBC
   */
  public int getOBC()
  {
    return obc;
  }

  /**
   * Gets the hex.
   * 
   * @return the hex
   */
  public int[] getHex()
  {
    return hex;
  }

  /**
   * Gets the misc message.
   * 
   * @return the misc message
   */
  public String getMiscMessage()
  {
    return miscMessage;
  }

  /**
   * Gets the error message.
   * 
   * @return the error message
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * Decode start.
   * 
   * @return the int
   */
  public int decodeStart()
  {
    return decoder_ctx[ 0 ] & 0xfffff;
  }

  /**
   * Decode size.
   * 
   * @return the int
   */
  public int decodeSize()
  {
    return 2 + ( decoder_ctx[ 1 ] >> 16 );
  }

  // native methods

  /**
   * Gets the version.
   * 
   * @return the version
   */
  public native String getVersion();

  /**
   * Decode.
   * 
   * @param decoder_ctx
   *          the decoder_ctx
   * @param bursts
   *          the bursts
   * @param r
   *          the r
   * @param e
   *          the e
   * @param freq
   *          the freq
   * @return true, if successful
   */

  // declaration of decode(...), now unused, retained so that it is
  // included in DecodeIRCaller.h
  private native boolean decode( int[] decoder_ctx, int[] bursts, int r, int freq );

  private native boolean decode2( int[] decoder_ctx, int[] bursts, int r, int e, int freq );

  private final static String libraryName = "DecodeIR";
}
