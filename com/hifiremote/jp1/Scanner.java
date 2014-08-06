package com.hifiremote.jp1;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hifiremote.jp1.io.JPS;

public class Scanner
{
  private int irdbAddress = 0;
  private int eepromAddress = 0;
  private int numberTableAddress = 0;
  private int numberTableSize = 0;
  private int executorIndexAddress = 0;
  private int executorCount = 0;
  private int setupCodeIndexAddress = 0;
  private int setupTypeIndexAddress = 0;
  private int setupCodeCount = 0;
  private int setupTypeCount = 0;
  private int indexTablesOffset = 0;
  private boolean codeIncludesType = true;
  private JPS io = null;
  
  public Scanner( JPS io, int irdbAddress )
  {
    this.io = io;
    this.irdbAddress = irdbAddress;
    this.eepromAddress = io.getRemoteEepromAddress();
  }
  
  public boolean scan()
  {
    Hex hex = new Hex( 0x40 );
    io.readRemote( irdbAddress, hex.getData() );
    numberTableAddress = hex.get( 0x14 ) * 2;
    setupCodeIndexAddress = hex.get( 0x16 ) * 2;
    setupTypeIndexAddress = hex.get( 0x1A ) * 2;
    executorIndexAddress = hex.get( 0x1C ) * 2;
    indexTablesOffset = hex.get( 0x22 ) * 2;
    setupCodeCount = getInt( setupCodeIndexAddress );
    executorCount = getInt( executorIndexAddress );
    
    if ( RemoteMaster.admin )
    {
      System.err.println();
      int regionalPointersAddress = hex.get( 0x24 ) * 2;
      int buttonMapDataLength = regionalPointersAddress - ( irdbAddress + 0x28 );
      if ( buttonMapDataLength > 0 )
      {
        int ptr = irdbAddress + 0x2A;
        int mapStart = getInt( ptr );
        int count = ( mapStart - ptr ) / 2;
        for ( int i = 0; i < count; i++ )
        {
          ptr = irdbAddress + 0x2A + 2 * i;
          mapStart = getInt( ptr );
          int mapEnd = mapStart;
          if ( mapEnd <= regionalPointersAddress )
          {
            while ( getByte( mapEnd++ ) != 0xFF ){};
            short[] mapData = new short[ mapEnd - mapStart - 1 ];
            io.readRemote( mapStart, mapData );
            String mapString = "Map " + i + " Data = ";
            for ( int j = 0; j < mapData.length; j++ )
            {
              mapString += ( j > 0 ? ", " : "" ) + String.format( "$%02X", mapData[ j ] );
            }
            System.err.println( mapString );
          }
          else
          {
            break;
          }
        }
      }
    }
    
    // Setup code index immediately follows number table
    int numberTableLength = setupCodeIndexAddress - numberTableAddress;
    if ( numberTableLength % 10 != 0 )
    {
      System.err.println( "Parsing failed: number table has invalid length" );
      return false;
    }
    numberTableSize = numberTableLength / 10;   
    if ( RemoteMaster.admin )
    {
      System.err.println( "Start address of setup code index: $" + Integer.toHexString( setupCodeIndexAddress ) );
      System.err.println( "Start address of setup type index: $" + Integer.toHexString( setupTypeIndexAddress ) );
      System.err.println( "Count of setup codes: " + Integer.toString( setupCodeCount ) );
      System.err.println( "Start address of executor index: $" + Integer.toHexString( executorIndexAddress ) );
      System.err.println( "Count of executors: " + Integer.toString( executorCount ) );
    }
    // Check if high nibble of setup code is the device type.  Some newer remotes
    // allow for setup codes > 0x0FFF by not including device type in high nibble.
    // It is necessary to know whether or not a remote does this to read correctly
    // the true setup code value.
    int type = -1;
    int typeLimit = getInt( setupTypeIndexAddress + 2 * type + 2 ) * 2;
    for ( int i = 0; i < setupCodeCount; i++ )
    {
      int codeAddress = setupCodeIndexAddress + 2 + 2 * i;
      if ( codeAddress == typeLimit )
      {
        type++;
        typeLimit = getInt( setupTypeIndexAddress + 2 * type + 2 ) * 2;
      }
      int setupCode = getInt( setupCodeIndexAddress + 2 * i + 2 );
      int typeFromSetupCode = setupCode >> 12;
      if ( codeIncludesType && type != typeFromSetupCode )
      {
        codeIncludesType = false;
      }
    }
    setupTypeCount = type + 1;

    // Now perform consistency checks.
    // Get all valid pids.  Run through setup codes, testing if pid is valid.
    // Get maximum digit map index used by a setup code.  Check that it lies
    // within the number table.
    List< Integer > pidList = new ArrayList< Integer >();
    for ( int i = 0; i < executorCount; i++ )
    {
      pidList.add( getInt( executorIndexAddress + 2 * i + 2 ) );
    }
    boolean chaining = false;
    int maxMap = 0;
    int maxMapPid = -1;
    for ( int i = 0; i < setupCodeCount; i++ )
    {
      int addr = getInt( setupCodeIndexAddress + 2 * setupCodeCount + 2 * i + 2 ) + indexTablesOffset;
      if ( addr >= eepromAddress )
      {
        // Check failed.
        System.err.println( "Parsing failed: setup code address out of range" );
        return false;
      }
      hex = new Hex( 4 );
      io.readRemote( addr, hex.getData() );
      int testPid = hex.get( 0 );
      if ( !pidList.contains( testPid ) )
      {
        // Nonzero top nibble may denote chaining
        if ( pidList.contains( testPid & 0x0FFF ) )
        {
          chaining = true;
          testPid &= 0x0FFF;
        }
        else
        {
          System.err.println( "Parsing failed: setup data contains nonexistent pid" );
          return false;
        }
      }
      if ( hex.getData()[ 2 ] > maxMap )
      {
        maxMap = hex.getData()[ 2 ];
        maxMapPid = testPid;
      }
      maxMap = Math.max( maxMap, hex.getData()[ 2 ] );
    }
    // Find number of bytes per entry in last used number table
    int index = pidList.indexOf( maxMapPid );
    int addr = getInt( executorIndexAddress + 2 * executorCount + 2 * index + 2 ) + indexTablesOffset;
    int numVar = getInt( addr + 2 ) & 0x000F;
    maxMap += numVar - 1;
    
    if ( maxMap > numberTableSize )
    {
      System.err.println( "Parsing failed: setup data contains nonexistent map imdex" );
      return false;
    }
    if ( RemoteMaster.admin )
    {
      System.err.println( "Start address of number tables: $" + Integer.toHexString( numberTableAddress ) );
      System.err.println( "Count of number tables/Last used: " + Integer.toString( numberTableSize ) + "/" + Integer.toString( maxMap ) );
      System.err.println( "Setup data " + ( chaining ? "uses" : "does not use" ) + " chaining" );
      System.err.println();
    }
    return true;
  }
  
  public void dump()
  {
    short[] buffer = new short[ eepromAddress - irdbAddress ];
    io.readRemote( irdbAddress, buffer );    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    try
    {
      Hex.print( pw, Arrays.copyOf( buffer, buffer.length ), irdbAddress );
    }
    catch ( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }
    System.err.print( sw.toString() );
    buffer = null;
  }
  
  public int getInt( int addr )
  {
    short[] buf = new short[ 2 ];
    io.readRemote( addr, buf );
    return buf[ 0 ] | buf[ 1 ] << 8 ;
  }
  
  public int getByte( int addr )
  {
    short[] buf = new short[ 2 ];
    io.readRemote( addr, buf );
    return buf[ 0 ];
  }

  public int getNumberTableAddress()
  {
    return numberTableAddress;
  }

  public int getNumberTableSize()
  {
    return numberTableSize;
  }

  public int getExecutorIndexAddress()
  {
    return executorIndexAddress;
  }

  public int getExecutorCount()
  {
    return executorCount;
  }

  public int getSetupCodeIndexAddress()
  {
    return setupCodeIndexAddress;
  }

  public int getSetupTypeIndexAddress()
  {
    return setupTypeIndexAddress;
  }

  public int getSetupCodeCount()
  {
    return setupCodeCount;
  }

  public int getSetupTypeCount()
  {
    return setupTypeCount;
  }

  public int getIndexTablesOffset()
  {
    return indexTablesOffset;
  }

  public boolean setupCodeIncludesType()
  {
    return codeIncludesType;
  }
  
}
