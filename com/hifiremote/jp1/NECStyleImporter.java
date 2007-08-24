package com.hifiremote.jp1;

public class NECStyleImporter
  extends Translator
{
  int styleParmIndex = 0;
  public NECStyleImporter( String[] textParms )
  {
    super( textParms );
    styleParmIndex = Integer.parseInt( textParms[ 0 ]);
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    System.err.println( "NECStyleImporter.in" );
    System.err.println( "  parms:" );
    for ( int i = 0; i < parms.length; ++i )
    {
      Object obj = parms[ i ].getUserValue();
      System.err.println( "    ( " + obj.getClass().getName() + " )" + obj );
    }

    int styleIndex = 0;
    if ( styleParmIndex < parms.length ) 
    {
      Object obj = parms[ styleParmIndex ].getUserValue();
      if ( obj.getClass() == Integer.class )
        styleIndex = (( Integer )obj ).intValue() - 1;
      else
      {
        String styleStr = ( String )obj;
        if ( styleStr.equalsIgnoreCase( "x1" ))
          styleIndex = 2;
        else if ( styleStr.equalsIgnoreCase( "x2" ))
          styleIndex = 3;
      }
    }
    
    System.err.println( "  styleIndex=" + styleIndex );
    
    insert( hexData, 11, 1, styleIndex >> 1 );
    insert( hexData, 15, 1, styleIndex & 1 );
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {}
}
