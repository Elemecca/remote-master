package com.hifiremote.jp1;

import java.util.StringTokenizer;
import java.lang.reflect.Constructor;

public class InitializerFactory
{
  public static Initializer[] create( String text )
  {
    StringTokenizer st = new StringTokenizer( text );
    int count = st.countTokens();
    Initializer[] initializers = new Initializer[ count ];
    for ( int i = 0; i < count; i++ )
    {
      String temp = st.nextToken();
      StringTokenizer st2 = new StringTokenizer( temp, "()" );
      String name = st2.nextToken();
      if ( st2.hasMoreTokens() )
        temp = st2.nextToken();
      else
        temp = new String();
      String[] parms = temp.split(",");
      try
      {
        if ( name.indexOf( '.' ) == -1 )
          name = "com.hifiremote.jp1." + name;

        Class cl = Class.forName( name );
        Class[] classes = { String[].class };
        Constructor ct = cl.getConstructor( classes );
        Object[] ctParms = { parms };
        initializers[ i ] = ( Initializer )ct.newInstance( ctParms );
      }
      catch ( Exception e )
      {
        System.err.println( "InitializerFactory couldn't create an instance of " + name );
        e.printStackTrace( System.err );
      }
    }
    return initializers;
  }
}
