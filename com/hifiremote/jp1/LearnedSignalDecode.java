package com.hifiremote.jp1;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hifiremote.decodeir.DecodeIRCaller;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalDecode.
 */
public class LearnedSignalDecode
{

  /**
   * Instantiates a new learned signal decode.
   * 
   * @param decodeIRCaller
   *          the decode ir caller
   */
  public LearnedSignalDecode( DecodeIRCaller decodeIRCaller )
  {
    protocolName = decodeIRCaller.getProtocolName();
    device = decodeIRCaller.getDevice();
    subDevice = decodeIRCaller.getSubDevice();
    obc = decodeIRCaller.getOBC();
    int[] temp = decodeIRCaller.getHex();
    int len = 0;
    for ( int i = 0; i < temp.length && temp[ i ] >= 0; ++i )
    {
      ++len;
    }
    hex = new int[ len ];
    System.arraycopy( temp, 0, hex, 0, len );
    miscMessage = decodeIRCaller.getMiscMessage();
    errorMessage = decodeIRCaller.getErrorMessage();
  }

  public LearnedSignalDecode( LearnedSignalDecode decode )
  {
    protocolName = decode.protocolName;
    device = decode.device;
    subDevice = decode.subDevice;
    obc = decode.obc;
    hex = decode.hex;
    miscMessage = decode.miscMessage;
    errorMessage = decode.errorMessage;
    ignore = decode.ignore;
  }
  
  /**
   * Returns the hex value for this learned signal in the specified protocol,
   * preserving the OBC of the signal.
   */
  public Hex getProtocolHex( Protocol protocol, List< String > error )
  {
    Value[] values = new Value[ protocol.cmdParms.length ];
    for ( int i = 0; i < values.length; i++ )
    {
      values[ i ] = new Value( null, protocol.cmdParms[ i ].getDefaultValue() );
    }

    Hex pHex = new Hex( protocol.getDefaultCmd().length() );
    CmdParameter[] parms = protocol.cmdParms;
    for ( int i = 0; i < parms.length; i++ )
    {
      if ( parms[ i ].getName().toUpperCase().startsWith( "OBC" ) )
      {
        values[ i ] = new Value( obc );
        break;
      }
    }

    try
    {
      for ( int i = 0; i < protocol.cmdTranslators.length; i++ )
      {
        protocol.cmdTranslators[ i ].in( values, pHex, protocol.devParms, -1 );
      }
    }
    catch ( IllegalArgumentException ex )
    {
      pHex = null;
      error.add( "" );
      error.add( ex.getMessage() );
    }
    return pHex;
  }
  
  public Hex getSignalHex()
  {
    if ( hex == null )
    {
      return null;
    }
    Hex sHex = new Hex( hex.length );
    for ( int i = 0; i < hex.length; i++ )
    {
      sHex.set( ( short )hex[ i ], i );
    }
    return sHex;
  }
  
  public static boolean displayErrors( String protocolName, List< List< String > > failedToConvert )
  {
    String message = "<html>The following learned signals could not be converted for use with the " + protocolName
        + " protocol.<p>If you need help figuring out what to do about this, please post<br>"
        + "a question in the JP1 Forums at http://www.hifi-remote.com/forums</html>";

    JPanel panel = Protocol.getErrorPanel( message, failedToConvert );
    String[] buttonText =
      {
        "Continue conversion", "Abort conversion"
      };
    int rc = JOptionPane.showOptionDialog( null, panel, "Protocol Conversion Error", JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE, null, buttonText, buttonText[ 0 ] );
    return rc == JOptionPane.YES_OPTION;
  }

  /** The protocol name. */
  public String protocolName = null;

  /** The device. */
  public int device = 0;

  /** The sub device. */
  public int subDevice = 0;

  /** The obc. */
  public int obc = 0;

  /** The hex. */
  public int[] hex;

  /** The misc message. */
  public String miscMessage = null;

  /** The error message. */
  public String errorMessage = null;

  public boolean ignore = false;
}
