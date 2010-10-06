package com.hifiremote.jp1.io;

import java.io.File;

import com.hifiremote.jp1.Hex;

/**
 * The Class JP1Parallel.
 */
public class JP1Parallel extends IO
{

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getInterfaceName()
   */
  public native String getInterfaceName();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getInterfaceVersion()
   */
  public native String getInterfaceVersion();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getPortNames()
   */
  public native String[] getPortNames();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#openRemote(java.lang.String)
   */
  public native String openRemote( String portName );

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#closeRemote()
   */
  public native void closeRemote();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteSignature()
   */
  public native String getRemoteSignature();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteEepromAddress()
   */
//  public native int getRemoteEepromAddress();
  public int getRemoteEepromAddress()
  {
    // The native function in JP1Parallel.dll version 0.02 returns the value of an uninitialized
    // variable, which is not guaranteed to be 0.
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteEepromSize()
   */
//  public native int getRemoteEepromSize();
  public int getRemoteEepromSize()
  {
    // The native function in JP1Parallel.dll version 0.02 returns the value of an uninitialized
    // variable, which is not guaranteed to be 0 - and -1 is preferable, as in JP1USB.dll.
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#readRemote(int, byte[], int)
   */
  public native int readRemote( int address, byte[] buffer, int length );

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#writeRemote(int, byte[], int)
   */
  public native int writeRemote( int address, byte[] buffer, int length );

  /**
   * Instantiates a new j p1 parallel.
   * 
   * @throws UnsatisfiedLinkError
   *           the unsatisfied link error
   */
  public JP1Parallel() throws UnsatisfiedLinkError
  {
    super( libraryName );
  }

  /**
   * Instantiates a new j p1 parallel.
   * 
   * @param folder
   *          the folder
   * @throws UnsatisfiedLinkError
   *           the unsatisfied link error
   */
  public JP1Parallel( File folder ) throws UnsatisfiedLinkError
  {
    super( folder, libraryName );
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main( String[] args )
  {
    JP1Parallel test = new JP1Parallel();
    String portName = null;
    for ( int i = 0; i < args.length; ++i )
    {
      String arg = args[ i ];
      if ( arg.equals( "-port" ) && ( ( i + 1 ) < args.length ) )
      {
        portName = args[ ++i ];
        System.err.println( "Using port " + portName );
      }
    }
    portName = test.openRemote( portName );
    if ( portName != null )
    {
      System.err.println( "Found remote on port " + portName );
      System.err.println( "signature=" + test.getRemoteSignature() );
      int address = test.getRemoteEepromAddress();
      System.err.println( "address=" + Integer.toHexString( address ).toUpperCase() );
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
        System.err.print( ' ' );
        System.err.println( Hex.toString( buffer, 16 ) );
      }
      test.closeRemote();
    }
    else
    {
      System.err.println( "No JP1 compatible remote found!" );
    }
  }

  private final static String libraryName = "jp1parallel";
}
