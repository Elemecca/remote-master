package com.hifiremote.jp1;

import java.util.StringTokenizer;
import java.lang.reflect.Constructor;

public class ImporterFactory
{
  public static Importer[] createImporters( String text )
  {
    StringTokenizer st = new StringTokenizer( text );
    int count = st.countTokens();
    Importer[] importers = new Importer[ count ];
    for ( int i = 0; i < count; i++ )
    {
      String temp = st.nextToken();
      StringTokenizer st2 = new StringTokenizer( temp, "()" );
      String name = st2.nextToken();
      String[] parms = null;
      if ( st2.hasMoreTokens() )
      {
        temp = st2.nextToken();
        StringTokenizer st3 = new StringTokenizer( temp, "," );
        int parmCount = st3.countTokens();
        parms = new String[ parmCount ];
        for ( int j = 0; j < parmCount; j++ )
          parms[ j ] = st3.nextToken();
      }
      else
        parms = new String[ 0 ];

      try
      {
        if ( name.indexOf( '.' ) == -1 )
          name = "com.hifiremote.jp1." + name;

        Class cl = Class.forName( name );
        Class[] classes = { String[].class };
        Constructor ct = cl.getConstructor( classes );
        Object[] ctParms = { parms };
        importers[ i ] = ( Importer )ct.newInstance( ctParms );
      }
      catch ( Exception e )
      {
        System.err.println( "ImporterFactory couldn't create an instance of " + name );
        e.printStackTrace( System.err );
      }
    }
    return importers;
  }
}
