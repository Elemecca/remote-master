package com.hifiremote.jp1.io;

import com.hifiremote.jp1.Hex;

public class JP12Serial
  extends IO
{
  public native String getInterfaceName();
  public native String getInterfaceVersion();
  public native String openRemote( String portName );
  public native void closeRemote();
  public native String getRemoteSignature();
  public native int getRemoteEepromAddress();
  public native int getRemoteEepromSize();
  public native int readRemote( int address, byte[] buffer, int length );
  public native int writeRemote( int address, byte[] buffer, int length );

  static
  {
    System.loadLibrary( "jp12serial" );
  }

  public static void main( String[] args )
  {
    JP12Serial test = new JP12Serial();
    String portName = test.openRemote( null );
    System.err.println( "portName=" + portName );
    if ( portName != null )
    {
      System.err.println( "signature=" + test.getRemoteSignature());
      int address = test.getRemoteEepromAddress();
      System.err.println( "address=" + Integer.toHexString( address ).toUpperCase());
      int size = test.getRemoteEepromSize();
      System.err.println( "size=" + size );
      short[] buffer = new short[ 0x20 ];
      int len = test.readRemote( address, buffer );
      if ( len < 0 )
      {
        System.err.println( "Error reading from remote!" );
      }
      else
      {
        System.err.println( "Start of EEPROM:" );
        System.err.print( ' '  );
        System.err.println( Hex.toString( buffer, 16 ));
      }
      test.closeRemote();
    }
    else
    {
      System.err.println( "No JP1.2 compatible remote found!" );
    }
  }
}
