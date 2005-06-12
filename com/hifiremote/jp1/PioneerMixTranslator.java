package com.hifiremote.jp1;

public class PioneerMixTranslator
  extends Translate
{
  public PioneerMixTranslator( String[] textParms )
  {
    super( textParms );
  }

  // called to store parms into hex data
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int flag = extract( hex, 13, 3 );
    boolean doInsert = true;
    for ( int i = 0; i < parms.length; i++ )
    {
      if (( onlyIndex != -1 ) && ( onlyIndex != i ))
        continue;
      if (( parms[ i ] != null ) && ( parms[ i ].getValue() != null ))
      {
        int val = (( Integer )parms[ i ].getValue()).intValue();
        switch( i )
        {
          case 0: // Prefix device
            if ( val == 0 )  // none
              flag = 0;
            else // dev1
              flag |= 1;
            break;
          case 1: // Prefix cmd
            if ( val == 0 ) // none
              flag = 0;
            else 
              flag = (( val - 1 ) << 1 ) + 1;
            break;
          case 2:  // Device
            if ( val == 0 ) // dev1
              flag = 0;
            else //dev2
              flag |= 1;
            break;
          case 3:  // OBC
            doInsert = false;
            break;
        }
      }
    }
    if ( doInsert )
      insert( hex, 13, 3, flag );
  }

  // called to extract parms from hex data
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int val = extract( hex, 13, 3 );
    Integer zero = new Integer( 0 );
    Integer one = new Integer( 1 );
    if ( val == 0 ) // single cmd
    {
      parms[ 0 ] = new Value( zero, null );  // PrefixDevice = none
      parms[ 1 ] = new Value( zero, null );  // PrefixCmd = none
      parms[ 2 ] = new Value( zero, null );  // Device = dev1
    }
    else
    {
        parms[ 0 ] = new Value( one, null );   // PrefixDevice = dev1
        int temp = ( val >> 1 ) + 1;
        parms[ 1 ] = new Value( new Integer( temp ), null );   // PrefixCmd = cmd1
        parms[ 2 ] = new Value( one, null );   // Device = dev2
    }
  }
}
