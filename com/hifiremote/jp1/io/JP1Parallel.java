package com.hifiremote.jp1.io;

import java.io.File;
import com.hifiremote.jp1.Hex;

public class JP1Parallel
  extends IO
{
  public native String getInterfaceName();
  public native String getInterfaceVersion();
  public native String[] getPortNames();
  public native String openRemote( String portName );
  public native void closeRemote();
  public native String getRemoteSignature();
  public native int getRemoteEepromAddress();
  public native int getRemoteEepromSize();
  public native int readRemote( int address, byte[] buffer, int length );
  public native int writeRemote( int address, byte[] buffer, int length );
  private static boolean isLoaded = false;

  public JP1Parallel()
    throws UnsatisfiedLinkError
  {
    if ( !isLoaded )
    {
      System.loadLibrary( "jp1parallel" );
      isLoaded = true;
    }
  }

  public JP1Parallel( File folder )
    throws UnsatisfiedLinkError
  {
    if ( !isLoaded )
    {
      File file = new File( folder, System.mapLibraryName( "jp1parallel" ));
      System.load( file.getAbsolutePath());
      isLoaded = true;
    }
  }

  public static void main( String[] args )
  {
    JP1Parallel test = new JP1Parallel();
    String portName = null;
    for ( int i = 0; i < args.length; ++i )
    {
      String arg = args[ i ];
      if ( arg.equals( "-port" ) && (( i + 1 ) < args.length ))
      {
        portName = args[ ++i ];
        System.err.println( "Using port " + portName );
      }
    }
    portName = test.openRemote( portName );
    if ( portName != null )
    {
      System.err.println( "Found remote on port " + portName );
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
      System.err.println( "No JP1 compatible remote found!" );
    }
  }
}
