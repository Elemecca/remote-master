package com.hifiremote.jp1;

import java.lang.reflect.Constructor;
import java.util.Properties;

public class ProtocolFactory
{
  public static Protocol createProtocol( String name, byte[] id,
                                         String type, Properties props )
  {
    Protocol rc = null;
    try
    {
      if ( type.indexOf( '.' ) == -1 )
        type = "com.hifiremote.jp1." + type;

      Class cl = Class.forName( type );
      Constructor ct = cl.getConstructor( classes );
      Object[] parms = { name, id, props };
      rc = ( Protocol )ct.newInstance( parms );

    }
    catch ( Exception e )
    {
      System.err.println( "ProtocolFactory couldn't create an instance of " + type );
      e.printStackTrace( System.err );
    }
    return rc;
  }

  private static Class[] classes =
    { String.class, byte[].class, Properties.class };
}

