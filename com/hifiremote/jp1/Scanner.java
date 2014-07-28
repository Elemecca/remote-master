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
  private short[] buffer = null;
  
  public Scanner( JPS io, int irdbAddress )
  {
    this.io = io;
    this.irdbAddress = irdbAddress;
    this.eepromAddress = io.getRemoteEepromAddress();
  }
  
  public boolean scan()
  {
    base = irdbAddress;
    buffer = new short[ eepromAddress - irdbAddress ];
    io.readRemote( base, buffer );

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    try
    {
      Hex.print( pw, Arrays.copyOf( buffer, buffer.length ), base );
    }
    catch ( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
    System.err.print( sw.toString() );

    int size = 2;
    int first = base + 0x2A;
    int last = first;
    
    if ( irdbAddress < 0x10000 )
    {
      // Button map data only present when irDbAddress < 0x10000
      last = getInt( first ) - 2;
      System.err.println( "Count of button maps: " + Integer.toString( ( last - first ) / 2 + 1 ) );
      System.err.println( "Vector to first button map: $" + Integer.toHexString( first ) );
      first = getInt( last );  // ptr to last button map
      while ( getByte( first++ ) != 0xFF ){};
      // first now points past end of last button map
      first += ( first & 1 ) == 1 ? 3 : 2;
      // US remotes use size=2, European ones use size=1
      size = getInt( first ) < first ? 2 : 1;
    }
    // first now points to start of vectors to region/device code lists
    int minVec = 0x20000;
    int maxVec = 0;
    while ( first < minVec )
    {
      int vec = getInt( first ) * size;
      minVec = Math.min( vec, minVec );
      maxVec = Math.max( vec, maxVec );
      first += 2;
    }
    // minVec, maxVec are vectors to first and last lists of pointers to setup codes by brand.
    // Each list starts with count of following entries.  We now find the greatest of these pointers.
    first = minVec;
    last = maxVec;
    maxVec = 0;
    while ( first <= last )
    {
      int count = getInt( first );
      for( int i = 0; i < 2 * count; i += 2 )
      {
        maxVec = Math.max( maxVec, getInt( first + i + 2 ) * size );
      }
      first += 2 * count + 2;
    }
    first = maxVec + 2 * getInt( maxVec ) + 2;
    // first now points to start of number tables
    numberTableAddress = first;

    // Now find the index to executors, based on the fact that executor PIDs are
    // listed in ascending order and will number at least 30
    int count = 1;
    int reqd = getInt( first );
    while ( count++ < reqd )
    {
      if ( getInt( first + 2 * count - 2 ) >= getInt( first + 2 * count ) )
      {
        first += 2 * count - 4;
        count = 1;
        do
        {
          first += 2;
          reqd = getInt( first );
        } while ( reqd < 30 );
      }
    }
    // first now points to index of executors, which starts with count of executors
    executorIndexAddress = first;
    executorCount = getInt( executorIndexAddress );
    first = numberTableAddress;
    int diff = 0;
    do
    {
      first += 10;
      int n = getInt( first );
      diff = executorIndexAddress - ( first + 4 * n );
    } 
    while ( first < executorIndexAddress && ( diff < 0 || diff > 30 ) );
    // first now points to index to setup codes
    setupCodeIndexAddress = first;
    numberTableSize = ( setupCodeIndexAddress - numberTableAddress ) / 10;
    setupCodeCount = getInt( setupCodeIndexAddress );
    System.err.println( "Start address of number tables: $" + Integer.toHexString( numberTableAddress ) );
    System.err.println( "Count of number tables: " + Integer.toString( numberTableSize ) );
    System.err.println( "Start address of setup code index: $" + Integer.toHexString( setupCodeIndexAddress ) );
    System.err.println( "Count of setup codes: " + Integer.toString( setupCodeCount ) );
    System.err.println( "Start address of executor index: $" + Integer.toHexString( executorIndexAddress ) );
    System.err.println( "Count of executors: " + Integer.toString( executorCount ) );
    
    List< Integer > pidList = new ArrayList< Integer >();
    for ( int i = 0; i < executorCount; i++ )
    {
      pidList.add( getInt( executorIndexAddress + 2 * i + 2 ) );
    }
    // Set first to point past end of executor index.  This is first candidate for
    // address offset in both index tables.  Find the actual offset by incrementing until
    // first few setups start with a valid pid.
    first = executorIndexAddress + 4 * executorCount + 2;
    boolean done = false;
    while ( !done )
    {
      done = true;
      for ( int i = 0; i < 30; i++ )
      {
        int addr = getInt( setupCodeIndexAddress + 2 * setupCodeCount + 2 * i + 2 ) + first;
        if ( addr >= eepromAddress )
        {
          // Check failed.
          first = 0;
          break;
        }
        int testPid = getBigEndianInt( addr );
        if ( !pidList.contains( testPid ) )
        {
          done = false;
          first += 2;
          break;
        }
      }
    }
    buffer= null;
    if ( first > 0 )
    {
      indexTablesOffset = first;
      System.err.println( "Index tables offset = $" + Integer.toHexString( indexTablesOffset ) );
      return true;
    }
    else
    {
      System.err.println( "Parsing of IRDB failed." );
      return false;
    }
  }
  
  public int getInt( int addr )
  {
    return buffer[ addr - base ] + ( buffer[ addr - base + 1 ] << 8 );
  }
  
  public int getBigEndianInt( int addr )
  {
    return buffer[ addr - base + 1 ] + ( buffer[ addr - base ] << 8 );
  }
  
  public int getByte( int addr )
  {
    return buffer[ addr - base ];
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
