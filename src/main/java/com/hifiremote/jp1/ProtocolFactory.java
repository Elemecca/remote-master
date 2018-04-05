package com.hifiremote.jp1;

import java.lang.reflect.Constructor;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Protocol objects.
 */
public class ProtocolFactory
{
  
  /**
   * Creates a new Protocol object.
   * 
   * @param name the name
   * @param id the id
   * @param type the type
   * @param props the props
   * 
   * @return the protocol
   */
  public static Protocol createProtocol( String name, Hex id,
                                         String type, Properties props )
  {
    Protocol rc = null;
    try
    {
      if ( type.indexOf( '.' ) == -1 )
        type = "com.hifiremote.jp1." + type;

      Class<?> cl = Class.forName( type );
      Class< ? extends Protocol > cl2 = cl.asSubclass( Protocol.class );
      Constructor< ? extends Protocol > ct = cl2.getConstructor( classes );
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

  /** The classes. */
  private static Class<?>[] classes =
    { String.class, Hex.class, Properties.class };
}

