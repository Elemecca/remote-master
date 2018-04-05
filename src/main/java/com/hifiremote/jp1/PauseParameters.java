/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

/**
 * @author Greg
 */
public class PauseParameters extends RDFParameter
{
  // Default value corresponds to:
  //   PauseParams = <userName>, 2/1, 10.66  for S3C8 remotes
  //   PauseParams = <userName>, 2/1, 16  for all other remotes, including S3C8+
  
  private String userName = "";

  private int dataLength = 2;

  private int offset = 0;

  private int bytesUsed = 1;

  private boolean lsb = false;

  private float multiplier = 1f;
  
  public PauseParameters( String userName, Remote remote )
  {
    this.userName = userName;
    if ( remote.getProcessor().getName().equals( "S3C80" ) 
        && remote.getRAMAddress() != 0xFF00 )
    {
      // Processor is S3C8 (and not S3C8+)
      multiplier = 10.66f;
    }
    else
    {
      multiplier = 16f;
    }        
  }
  
  public PauseParameters(){};

  public void parse( String text, Remote remote ) throws Exception
  {
    List< String > parms = ParameterTokenizer.getTokens( text );
    userName = parms.get( 0 );
    String value = parms.get( 1 );
    if ( value.equals( "1" ) )
    {
      dataLength = 1;
      offset = 0;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/1" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/2" ) )
    {
      dataLength = 2;
      offset = 1;
      bytesUsed = 1;
      lsb = false;
    }
    else if ( value.equals( "2/B" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 2;
      lsb = false;
    }
    else if ( value.equals( "2/L" ) )
    {
      dataLength = 2;
      offset = 0;
      bytesUsed = 2;
      lsb = true;
    }

    multiplier = Float.parseFloat( parms.get( 2 ) );
  }

  public String getUserName()
  {
    return userName;
  }

  public int getDataLength()
  {
    return dataLength;
  }

  public int getOffset()
  {
    return offset;
  }

  public int getBytesUsed()
  {
    return bytesUsed;
  }

  public boolean isLsb()
  {
    return lsb;
  }

  public float getMultiplier()
  {
    return multiplier;
  }
  
}
