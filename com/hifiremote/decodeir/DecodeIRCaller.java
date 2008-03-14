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

 */

//  Usage
//
//  To prepare to decode a signal, call all three of the following in any order
//
//     setBursts( int[] b, int r );
//     setFrequency( int f );
//     initDecoder();
//
//     b is an array of durations in microseconds.  All numbers should be positive.
//          Even (0 based) positions are On durations, odd positions are Off durations.
//     r is the length of the repeat part of b (b.size()-r is the length of the one time part.)
//     f is the frequency in hertz.
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

import java.io.*;
import java.util.*;

public class DecodeIRCaller
{
    private int[] bursts;
    private int repeatPart;
    private int frequency;

    private int[] decoder_ctx = new int[2];
    private int device;
    private int subDevice;
    private int obc;
    private int hex[] = new int[4];
    private String protocolName = new String("");
    private String miscMessage = new String("");
    private String errorMessage = new String("");
    private static boolean loaded = false;

    public DecodeIRCaller( File folder )
      throws UnsatisfiedLinkError
    {
      if ( !loaded )
      {
        File file = new File( folder,
                              System.mapLibraryName( "DecodeIR" ));
        System.load( file.getAbsolutePath());
        loaded = true;
      }
    }

    public DecodeIRCaller()
      throws UnsatisfiedLinkError
    {
      if ( !loaded )
      {
        System.loadLibrary( "DecodeIR" );
        loaded = true;
      }
    }

    public void setBursts( int[] b, int r ) { bursts = b; repeatPart = r; }
    public void setFrequency( int f ) { frequency = f; }
    public void initDecoder()
    {
        decoder_ctx[0] = decoder_ctx[1] = 0;
    }

    public synchronized boolean decode()
    {
        return decode( decoder_ctx, bursts, repeatPart, frequency );
    }

    public String getProtocolName() { return protocolName; }
    public int getDevice() { return device; }
    public int getSubDevice() { return subDevice; }
    public int getOBC() { return obc; }
    public int[] getHex() { return hex; }
    public String getMiscMessage() { return miscMessage; }
    public String getErrorMessage() { return errorMessage; }
    public int decodeStart() { return decoder_ctx[0] & 0xfffff; }
    public int decodeSize() { return 2 + (decoder_ctx[1]>>16); }

    // native methods

    public native String getVersion();
    private native boolean decode( int[] decoder_ctx, int[] bursts, int r, int freq );

}
