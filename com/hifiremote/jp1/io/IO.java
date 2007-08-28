package com.hifiremote.jp1.io;

public abstract class IO
{
  public abstract String getInterfaceName();
  public abstract String getInterfaceVersion();
  public abstract String[] getPortNames();
  public abstract String openRemote( String portName );
  public abstract void closeRemote();
  public abstract String getRemoteSignature();
  public abstract int getRemoteEepromAddress();
  public abstract int getRemoteEepromSize();

  public String openRemote()
  {
    return openRemote( null );
  }
  
  public int readRemote( int address, short[] sBuffer )
  {
    return readRemote( address, sBuffer, sBuffer.length );
  }

  public int readRemote( int address, short[] sBuffer, int length )
  {
    byte[] buffer = new byte[ length ];
    int len = readRemote( address, buffer, length );
    for ( int i = 0; i < len ; ++i )
      sBuffer[ i ] = ( short )( buffer [ i ] & 0xFF );
    return len;
  }

  public int readRemote( int address, byte[] buffer )
  {
    return readRemote( address, buffer, buffer.length );
  }
  public abstract int readRemote( int address, byte[] buffer, int length );

  public int writeRemote( int address, short[] sBuffer )
  {
    return writeRemote( address, sBuffer, sBuffer.length );
  }

  public int writeRemote( int address, short[] sBuffer, int length )
  {
    byte[] buffer = new byte[ length ];
    for ( int i = 0; i < length ; ++i )
      buffer[ i ] = ( byte )( sBuffer [ i ] & 0xFF );
    return writeRemote( address, buffer, length );
  }

  public int writeRemote( int address, byte[] buffer )
  {
    return writeRemote( address, buffer, buffer.length );
  }

  public abstract int writeRemote( int address, byte[] buffer, int length );
}
