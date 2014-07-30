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
  private int setupCodeCount = 0;
  private int indexTablesOffset = 0;
  private int base = 0;
  private JPS io = null;
  
  public Scanner( JPS io, int irdbAddress )
  {
    this.io = io;
    this.irdbAddress = irdbAddress;
    this.eepromAddress = io.getRemoteEepromAddress();
  }
  
  public boolean scan()
  {
    base = irdbAddress;
    Hex hex = new Hex( 0x40 );
    io.readRemote( base, hex.getData() );
    numberTableAddress = hex.get( 0x14 ) * 2;
    setupCodeIndexAddress = hex.get( 0x16 ) * 2;
    executorIndexAddress = hex.get( 0x1C ) * 2;
    indexTablesOffset = hex.get( 0x22 ) * 2;
    setupCodeCount = getInt( setupCodeIndexAddress );
    executorCount = getInt( executorIndexAddress );
    
    // Setup code index immediately follows number table
    int numberTableLength = setupCodeIndexAddress - numberTableAddress;
    if ( numberTableLength % 10 != 0 )
    {
      System.err.println( "Parsing failed: number table has invalid length" );
      return false;
    }
    numberTableSize = numberTableLength / 10;    
    System.err.println( "Start address of setup code index: $" + Integer.toHexString( setupCodeIndexAddress ) );
    System.err.println( "Count of setup codes: " + Integer.toString( setupCodeCount ) );
    System.err.println( "Start address of executor index: $" + Integer.toHexString( executorIndexAddress ) );
    System.err.println( "Count of executors: " + Integer.toString( executorCount ) );
    
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
      if ( hex.getData()[ 2 ] > maxMap )
      {
        maxMap = hex.getData()[ 2 ];
        maxMapPid = testPid;
      }
      maxMap = Math.max( maxMap, hex.getData()[ 2 ] );
      if ( !pidList.contains( testPid ) )
      {
        // Nonzero top nibble may denote chaining
        if ( pidList.contains( testPid & 0x0FFF ) )
        {
          chaining = true;
        }
        else
        {
          System.err.println( "Parsing failed: setup data contains nonexistent pid" );
          return false;
        }
      }
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
    System.err.println( "Start address of number tables: $" + Integer.toHexString( numberTableAddress ) );
    System.err.println( "Count of number tables/Last used: " + Integer.toString( numberTableSize ) + "/" + Integer.toString( maxMap ) );
    System.err.println( "Setup data " + ( chaining ? "uses" : "does not use" ) + " chaining" );
    return true;
  }
  
  public int getInt( int addr )
  {
    short[] buf = new short[ 2 ];
    io.readRemote( addr, buf );
    return buf[ 0 ] | buf[ 1 ] << 8 ;
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

  public int getSetupCodeCount()
  {
    return setupCodeCount;
  }

  public int getIndexTablesOffset()
  {
    return indexTablesOffset;
  }
  
}
