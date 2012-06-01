package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class FixedData.
 */
public class FixedData
{
  
  public enum Location
  {
    // SIGBLK refers to the separate Signature block of JP1.4/JP2 remotes
    E2, SIGBLK
  };

  public FixedData( int addr, short[] data, Location location )
  {
    address = addr;
    this.data = data;
    this.location = location;
  }

  /**
   * Gets the address.
   * 
   * @return the address
   */
  public int getAddress()
  {
    return address;
  }

  /**
   * Gets the data.
   * 
   * @return the data
   */
  public short[] getData()
  {
    return data;
  }
  
  public Location getLocation()
  {
    return location;
  }

  /**
   * Parses the fixed data.
   * 
   * @param rdr
   *          the rdr
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static FixedData[] parse( RDFReader rdr ) throws Exception
  {
    java.util.List< FixedData > work = new ArrayList< FixedData >();
    java.util.List< Short > temp = new ArrayList< Short >();
    String line;
    int address = -1;
    int value = -1;
    Location location = Location.E2;
    Location newLocation = Location.E2;

    while ( true )
    {
      line = rdr.readLine();

      if ( ( line == null ) || ( line.length() == 0 ) )
        break;

      StringTokenizer st = new StringTokenizer( line, ",; \t" );
      String token = st.nextToken();
      while ( true )
      {
        if ( token.equals( "E2" ) )
        {
          newLocation = Location.E2;
        }
        else if ( token.equals( "SIG" ) )
        {
          newLocation = Location.SIGBLK;
        } 
        else if ( token.charAt( 0 ) == '=' ) // the last token was an address
        {
          token = token.substring( 1 );
          if ( address != -1 ) // we've seen some bytes
          {
            short[] b = new short[ temp.size() ];
            int i = 0;
            for ( Short val : temp )
            {
              b[ i++ ] = val.shortValue();
            }
            work.add( new FixedData( address, b, location ) );
            temp.clear();
          }
          address = value;
          location = newLocation;
          value = -1;
          if ( token.length() != 0 )
            continue;
        }
        else
        {
          int equal = token.indexOf( '=' );
          String saved = token;
          if ( equal != -1 )
          {
            token = token.substring( 0, equal );
          }
          if ( value != -1 )
          {
            temp.add( new Short( ( short )value ) );
          }
          value = RDFReader.parseNumber( token );
          if ( equal != -1 )
          {
            token = saved.substring( equal );
            continue;
          }
        }
        if ( !st.hasMoreTokens() )
          break;
        token = st.nextToken();
      }
    }
    temp.add( new Short( ( short )value ) );
    short[] b = new short[ temp.size() ];
    int j = 0;
    for ( Short by : temp )
    {
      b[ j++ ] = by.shortValue();
    }
    if ( address != -1 )
    {
      work.add( new FixedData( address, b, location ) );
    }
    return work.toArray( new FixedData[ work.size() ] );
  }

  public boolean check( short[] buffer )
  {
    if ( ( address + data.length ) > buffer.length )
    {
      return false;
    }
    for ( int i = 0; i < data.length; ++i )
    {
      // For an extender merge file, a buffer value 0x100 means that the value is absent in
      // the extender hex, which is deemed to be a match.
      if ( buffer[ address + i ] < 0x100 && data[ i ] != buffer[ address + i ] )
      {
        return false;
      }
    }
    return true;
  }
  
  public static Remote[] filter( List< Remote > remotes, short[] buffer, short[] sigData )
  {
    List< Remote > passed = new ArrayList< Remote >();
    for ( Remote remote : remotes )
    {
      boolean pass = true;
      for ( FixedData fixedData : remote.getRawFixedData() )
      {
        if ( fixedData.location == Location.E2 )
        {
          if ( ! fixedData.check( buffer ) )
          {
            pass = false;
            break;
          }
        }
        else if ( sigData != null ) // location = Location.SIGBLK
        {
          if ( ! fixedData.check( sigData ) )
          {
            pass = false;
            break;
          }
        }
      }
      if ( pass )
      {
        passed.add( remote );
      }   
    }    
    return passed.toArray( new Remote[ 0 ] );
  }

  public void store( short[] buffer )
  {
    System.arraycopy( data, 0, buffer, address, data.length );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 200 );
    temp.append( '$' ).append( Integer.toHexString( address ) ).append( " =" );
    for ( int i = 0; i < data.length; i++ )
    {
      temp.append( " $" );
      String str = Integer.toHexString( data[ i ] );
      int len = str.length();
      if ( len > 2 )
        str = str.substring( len - 2 );
      if ( len < 2 )
        temp.append( '0' );
      temp.append( str );
    }
    return temp.toString();
  }

  /** The address. */
  private int address;

  /** The data. */
  private short[] data;
  
  private Location location = Location.E2;
  
}
