package com.hifiremote.jp1;

import java.util.*;
import java.io.*;

public class DeviceCombiner
  extends Protocol
{
  public DeviceCombiner( String name, Hex id, Properties props )
  {
    super( name, id, props );
    cmdParmInit = new Initializer[ 1 ];
    cmdParmInit[ 0 ] = 
      new DeviceCombinerInitializer( devices, ( ChoiceCmdParm )cmdParms[ 0 ]);
  }

  public void setProperties( Properties props )
  {
    for ( int i = 0; i < 16; i++ )
    {
      String prefix = "Combiner." + i;
      System.err.println( "Looking for " + prefix + ".name" );
      String nameStr = props.getProperty( prefix + ".name" );
      System.err.println( "Got " + nameStr );
      if ( nameStr == null )
        break;
      Hex pid = new Hex( props.getProperty( prefix + ".id" ));
      String variantName = props.getProperty( prefix + ".variant" );
      Protocol p = 
        ProtocolManager.getProtocolManager().findNearestProtocol( nameStr, pid, variantName );
      
      String parmStr = props.getProperty( prefix + ".parms" );
      Value[] values = DeviceUpgrade.stringToValueArray( parmStr );
      devices.add( new CombinerDevice( p, values ));
    }
  }

  public KMPanel getPanel( DeviceUpgrade deviceUpgrade )
  {
    if ( panel == null )
      panel = new DeviceCombinerPanel( deviceUpgrade );
    else
      panel.setDeviceUpgrade( deviceUpgrade );

    return panel;
  }

  public Vector getDevices(){ return devices; }
  
  public void store( PropertyWriter out )
    throws IOException
  {
    System.err.println( "DeviceCombiner.store" );
    super.store( out );
    int i = 0;
    for ( Enumeration e = devices.elements(); e.hasMoreElements(); )
    {
      CombinerDevice device = ( CombinerDevice )e.nextElement();
      String prefix = "Combiner." + i++;
      Protocol p = device.getProtocol();
      out.print( prefix + ".name", p.getName());
      out.print( prefix + ".id", p.getID().toString());
      out.print( prefix + ".variant", p.getVariantName());
      Value[] values = device.getValues();
      out.print( prefix + ".parms", DeviceUpgrade.valueArrayToString( values )); 
    }
  }

  private DeviceCombinerPanel panel = null;
  private Vector devices = new Vector();
}
