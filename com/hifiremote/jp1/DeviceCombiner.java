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

  public void reset()
  {
    devices.clear();
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

  public Hex getCode( Remote r )
  {
    byte[] header = new byte[ devices.size() + 1 ];
    Hex base = super.getCode( r );
    if ( base == null )
      return null;
    int offset = base.length() + header.length;
    Hex[] ids = new Hex[ devices.size()];
    Hex[] data = new Hex[ ids.length ];
    int i = 0;
    for ( Enumeration e = devices.elements(); e.hasMoreElements(); )
    {
      header[ i ] = ( byte )offset;
      CombinerDevice device = ( CombinerDevice )e.nextElement();
      ids[ i ] = device.getProtocol().getID();
      offset += 2;
      Hex hex = device.getFixedData();
      data[ i ] = hex;
      offset += hex.length();
      i++;
    }
    header[ i ] = ( byte )offset;

    byte[] code = new byte[ offset ];
    System.arraycopy( base.getData(), 0, code, 0, base.length() );
    offset = base.length();
    System.arraycopy( header, 0, code, offset, header.length );
    offset += header.length;
    for ( i = 0; i < data.length; i++ )
    {
      byte[] src = ids[ i ].getData();
      System.arraycopy( src, 0, code, offset, src.length );
      offset += src.length;
      src = data[ i ].getData();
      System.arraycopy( src, 0, code, offset, src.length );
      offset += src.length;
    }
        
    return new Hex( code );
  }

  private DeviceCombinerPanel panel = null;
  private Vector devices = new Vector();
}
