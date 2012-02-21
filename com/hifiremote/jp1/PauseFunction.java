package com.hifiremote.jp1;

import java.text.DecimalFormat;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class PauseFunction.
 */
public class PauseFunction extends SpecialProtocolFunction
{

  /**
   * Instantiates a new pause function.
   * 
   * @param keyMove
   *          the key move
   */
  public PauseFunction( KeyMove keyMove )
  {
    super( keyMove );
  }
  
  public PauseFunction( Macro macro )
  {
    super( macro );
  }

  /**
   * Instantiates a new pause function.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param cmd
   *          the cmd
   * @param notes
   *          the notes
   */
  public PauseFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
  }

  /**
   * Instantiates a new pause function.
   * 
   * @param props
   *          the props
   */
  public PauseFunction( Properties props )
  {
    super( props );
  }

  /**
   * Gets the duration.
   * 
   * @return the duration
   */
//  public int getDuration()
//  {
//    return getCmd().getData()[ 0 ];
//  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getType()
   */
  @Override
  public String getType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[ 0 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#getDisplayType()
   */
  @Override
  public String getDisplayType( RemoteConfiguration remoteConfig )
  {
    return getUserFunctions( remoteConfig )[0];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KeyMove#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    return pauseFormat.format( getPauseDuration( remoteConfig ) ) + "secs";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.SpecialProtocolFunction#update(com.hifiremote.jp1.SpecialFunctionDialog)
   */
  @Override
  public void update( SpecialFunctionDialog dlg )
  {
    dlg.setPauseDuration( getPauseDuration( dlg.getRemoteConfiguration() ) );
  }

  /**
   * Creates the hex.
   * 
   * @param dlg
   *          the dlg
   * @return the hex
   */
  public static Hex createHex( SpecialFunctionDialog dlg, PauseParameters params )
  {
    short[] hex = new short[ params.getDataLength() ];
    Float pauseDuration = dlg.getPauseDuration();
    if ( pauseDuration == null )
    {
      return null;
    }
    int duration = Math.round( pauseDuration * params.getMultiplier() );
    if ( params.getBytesUsed() == 2 )
    {
      if ( duration > 0xFFFF )
      {
        duration = 0xFFFF;
      }
      int lsb = params.isLsb() ? 0 : 1;
      hex[ lsb ] = ( short )( duration & 0xFF );
      hex[ 1 - lsb ] = ( short )( duration >> 8 );
    }
    else
    {
      if ( duration > 0xFF )
      {
        duration = 0xFF;
      }
      hex[ params.getOffset() ] = ( short )duration;
      if ( params.getDataLength() == 2 )
      {
        // In both cases make hex[ 1 ] be the EFC of hex[ 0 ]
        if ( params.getOffset() == 0 )
        {
          hex[ 1 ] = ( short )( EFC.parseHex( hex[ 0 ] ) & 0xFF );
        }
        else
        {
          hex[ 0 ] = EFC.toHex( hex[ 1 ] ).getData()[ 0 ];
        }
      }
    }
 
    return new Hex( hex );
  }
  
  public float getPauseDuration( RemoteConfiguration remoteConfig )
  {
    PauseParameters params = PauseSpecialProtocol.getPauseParameters(
        getUserFunctions( remoteConfig )[ 0 ], remoteConfig.getRemote() );
    short[] data = getCmd().getData();
    int duration = 0;
    if ( params.getBytesUsed() == 2 )
    {
      int lsb = params.isLsb() ? 0 : 1;
      duration = ( data[ 1 - lsb ] << 8 ) | data[ lsb ];
    }
    else
    {
      duration = data[ params.getOffset() ];
    }
    return duration / params.getMultiplier();
  }
  
  public static final DecimalFormat pauseFormat = new DecimalFormat( "0.0##" );
}
