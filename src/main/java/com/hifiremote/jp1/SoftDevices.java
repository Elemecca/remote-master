/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

/**
 * @author Greg
 */
public class SoftDevices extends RDFParameter
{
  private boolean use = true;
  private boolean allowEmptyButtonSettings = false;
  private boolean setupCodesOnly = false;
  private int countAddress = 0;
  private int sequenceAddress = 0;

  public void parse( String text, Remote remote ) throws Exception
  {
    // SoftDev=Use[, [AllowEmptyBtnSettings][, [CountAddr][, [SeqAddr]]]]
    List< String > settings = ParameterTokenizer.getTokens( text );
    String useStr = settings.get( 0 );
    if ( useStr.equalsIgnoreCase( "SetupCodesOnly" ) )
    {
      setupCodesOnly = true;
    }
    else
    {
      use = RDFReader.parseFlag( settings.get( 0 ) );
    }
    if ( use && settings.size() > 1 )
    {
      allowEmptyButtonSettings = RDFReader.parseFlag( settings.get( 1 ) );
      if ( settings.size() > 2 )
        countAddress = RDFReader.parseNumber( settings.get( 2 ) );
      if ( settings.size() > 3 )
        sequenceAddress = RDFReader.parseNumber( settings.get( 3 ) );
    }
  }

  public boolean inUse()
  {
    return use;
  }

  public boolean isSetupCodesOnly()
  {
    return setupCodesOnly;
  }

  public boolean getAllowEmptyButtonSettings()
  {
    return allowEmptyButtonSettings;
  }

  public int getCountAddress()
  {
    return countAddress;
  }

  public boolean usesSequence()
  {
    return sequenceAddress != 0;
  }

  public int getSequenceIndex( int position, short[] data )
  {
    // Gets the device index for a given sequence position.
    // Index -1 signifies the position is empty
    int sequence = data[ sequenceAddress + position ];
    if ( sequence == 255 )
    {
      sequence = -1;
    }
    return sequence;
  }

  public void setSequenceIndex( int index, int position, short[] data )
  {
    // Sets the device index for a given sequence position.
    // Index -1 signifies the position is to be made empty.
    if ( index == -1 )
    {
      index = 255;
    }
    data[ sequenceAddress + position ] = ( short )( index );
  }
  
  public int getSequencePosition( int index, int limit, short[] data )
  {
    // Gets the sequence position for a given device index, searching
    // positions up to specified limit.  Returns -1 if not found.
    int position;
    for ( position = 0; position < limit; position++ )
    {
      if ( data[ sequenceAddress + position ] == index )
        break;
    }
    return ( position < limit ) ? position : -1;
  }

  public boolean deleteSequenceIndex( int index, int limit, short[] data )
  {
    int p = getSequencePosition( index, limit, data );
    if ( p == -1 )
      return false;
    for ( ; p < limit - 1; p++ )
    {
      data[ sequenceAddress + p ] = data[ sequenceAddress + p + 1 ];
    }
    data[ sequenceAddress + limit - 1 ] = 0xFF;
    return true;
  }

  public void insertSequenceIndex( int index, int position, int limit, short[] data )
  {
    for (int p = limit - 1; p > position; p-- )
    {
      data[ sequenceAddress + p ] = data[ sequenceAddress + p - 1];
    }
    data[ sequenceAddress + position ] = ( short )index;
  }
    
  public boolean usesFilledSlotCount()
  {
    return countAddress != 0;
  }
  
  public int getFilledSlotCount( short[] data )
  {
    return data[ countAddress ];
  }
  
  public void setFilledSlotCount( int count, short[] data )
  {
    if ( countAddress != 0 )
    {
      data[ countAddress ] = ( short )count;
    }
  }

}