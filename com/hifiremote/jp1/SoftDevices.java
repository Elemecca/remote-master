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
  private int countAddress = 0;
  private int sequenceAddress = 0;

  public void parse( String text, Remote remote ) throws Exception
  {
    // SoftDev=Use[, [AllowEmptyBtnSettings][, [CountAddr][, [SeqAddr]]]]
    List< String > settings = ParameterTokenizer.getTokens( text );
    use = RDFReader.parseFlag( settings.get( 0 ) );
    if ( use )
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

  public int getSequence( int index, short[] data )
  {
    int sequence = data[ sequenceAddress + index ];
    if ( sequence == 255 )
    {
      sequence = -1;
    }
    return sequence;
  }

  public void setSequence( int sequence, int index, short[] data )
  {
    if ( sequence == -1 )
    {
      sequence = 255;
    }
    data[ sequenceAddress + index ] = ( short )( sequence );
  }
}
