package com.hifiremote.jp1;

import java.io.*;
import java.util.*;

public class ManualProtocol
  extends Protocol
{
  public final static int ONE_BYTE = 0;
  public final static int BEFORE_CMD = 1;
  public final static int AFTER_CMD = 2;

  public ManualProtocol( String name, Hex id, Properties props )
  {
    super( name, id, props );
    notes = props.getProperty( "Protocol.notes" );
  }

  public ManualProtocol( String name, Hex id, int cmdType, String signalStyle, 
                         int devBits, Vector parms, byte[] rawHex, int cmdBits )
  {
    super( name, id, new Properties());

    boolean lsb = false;
    boolean comp = false;
    if ( signalStyle.startsWith( "LSB" ))
      lsb = true;
    if ( signalStyle.endsWith( "COMP" ))
      comp = true;

    DirectDefaultValue defaultValue = new DirectDefaultValue( new Integer( 0 ));

    devParms = new DeviceParameter[ parms.size() ];
    deviceTranslators = new Translator[ parms.size() ];
    
    for ( int i = 0; i < parms.size(); i++ )
    {
      devParms[ i ] = new NumberDeviceParm( "Device " + ( i + 1 ), defaultValue, 10, devBits );
      devParms[ i ].setValue( parms.elementAt( i ));
      deviceTranslators[ i ] = new Translator( lsb, comp, i, devBits, i * 8 ); 
    }

    int offset = parms.size();
    byte[] fixedBytes = new byte[ offset + rawHex.length ];
    for ( int i = 0 ; i < rawHex.length; i++ )
      fixedBytes[ i + offset ] = rawHex[ i ];

    fixedData = new Hex( fixedBytes );

    int byte2Index = 0;
    switch ( cmdType )
    {
      case ONE_BYTE:
        defaultCmd = new Hex();
        cmdIndex = 0;
        break;
      case BEFORE_CMD:
        defaultCmd = new Hex( new byte[ 2 ]);
        cmdIndex = 1;
        byte2Index = 0;
        break;
      case AFTER_CMD:
        defaultCmd = new Hex( new byte[ 2 ]);
        cmdIndex = 0;
        byte2Index = 1;
        break;
    }

    cmdParms = new CmdParameter[ defaultCmd.length() ];
    cmdParms[ 0 ] = new NumberCmdParm( "OBC", null, cmdBits );
    cmdTranslators = new Translator[ defaultCmd.length() ];
    cmdTranslators[ 0 ] = new Translator( lsb, comp, 0, cmdBits, cmdIndex * 8 );
    if ( defaultCmd.length() > 1 )
    {
      cmdParms[ 1 ] = new NumberCmdParm( "Byte 2", defaultValue, 8 );
      cmdTranslators[ 1 ] = new Translator( false, false, 1, 8, byte2Index * 8 );  
    }
  }

  public void store( PrintWriter out )
    throws IOException
  {
    System.err.println( "ManualProtocol.store" );
    super.store( out );
    if ( devParms.length > 0 )
    {
      out.print( "DevParms=" );
      for ( int i = 0; i < devParms.length; i++ )
      {
        if ( i > 0 )
          out.print( ',' );
        DeviceParameter devParm = devParms[ i ];
        out.print( devParm );   
      }
      out.println();
    }
    if ( deviceTranslators.length > 0 )
    {
      out.print( "DeviceTranslator=" );
      for ( int i = 0; i < deviceTranslators.length; i++ )
      {
        if ( i > 0 )
          out.print( ' ' );
        out.print( deviceTranslators[ i ]);
      }
      out.println();
    }
    if ( cmdParms.length > 0 )
    {
      out.print( "CmdParms=" );
      for ( int i = 0; i < cmdParms.length; i++ )
      {
        if ( i > 0 )
          out.print( ',' );
        out.print( cmdParms[ i ]);
      }
      out.println();
    }
    if ( cmdTranslators.length > 0 )
    {
      out.print( "CmdTranslator=" );
      for ( int i = 0; i < cmdTranslators.length; i++ )
      {
        if ( i > 0 )
          out.print( ' ' );
        out.print( cmdTranslators[ i ]);
      }
      out.println();
    }
    out.println( "DefaultCmd=" + defaultCmd );
    out.println( "CmdIndex=" + cmdIndex );
    out.println( "FixedData=" + fixedData );
    for ( Iterator i = code.keySet().iterator(); i.hasNext(); )
    {
      Object key = i.next();
      out.print( "Code." + key + '=' );
      out.println( code.get( key ));
    }
    if ( notes != null )
      DeviceUpgrade.print( out, "Protocol.notes", notes );
  }
}
