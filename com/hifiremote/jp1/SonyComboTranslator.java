package com.hifiremote.jp1;

public class SonyComboTranslator
  extends Translate
{
  public SonyComboTranslator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int parmToSet )
  {
    System.err.println( "SonyComboTranslator.in(), parmToSet=" + parmToSet );
    if ( parmToSet < 0 )
      return;
    int parm = (( Integer )parms[ parmToSet ].getValue()).intValue();

    switch ( parmToSet )
    {
      case 0: // OBC
        break;
      case 1: // Protocol
        {
          int protocol = parm;
          int force15 = 0;
          int device = 0;
          int subDevice = 0;
          if ( protocol == 0 ) // Sony12
          {
            insert( hexData, 7, 1, 0 );  // clear Sony15 bit
            insert( hexData, 13, 3, 0 ); // clear Sony20 bit and index
          }
          else if ( protocol == 1 ) // Sony15
          {
            insert( hexData, 7, 1, 1 ); // set Sony15 bit
          }
          else // Sony20
          {
            insert( hexData, 7, 1, 0 ); // clear Sony15 bit
            insert( hexData, 15, 1, 1 ); // set Sony20 bit
            insert( hexData, 13, 2, 0 ); // use index 0
          }
        }
        break;
      case 2: // Device
        {
          int device = parm;
          if ( parm > 31 )
            insert( hexData, 7, 1, 1 );  // set Sony15 bit
          boolean isSony15 = ( extract( hexData, 7, 1 ) == 1 );
          if ( isSony15 )
            insert( hexData, 8, 8, reverse( device, 8 )); // store as 8 bits
          else
            insert( hexData, 8, 5, reverse( device, 5 ));  // store as 5 bits
        }
        break;
      case 3: // SubDevice
        {
          int subDevice = parm;
          boolean isSony15 = ( extract( hexData, 7, 1 ) == 1 );
          if ( subDevice == 0 )
          {
            insert( hexData, 13, 3, 0 );  // clear index and Sony20 bit
          }
          else
          {
            insert( hexData, 7, 1, 0 );  // clear Sony15 bit
            insert( hexData, 13, 2, subDevice - 1 );  // set index
            insert( hexData, 15, 1, 1 ); // set Sony20 bit
          }
        }
        break;
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    System.err.println( "SonyComboTranslator( " + hex.toString() + " )" );
    boolean isSony15 = ( extract( hex, 7, 1 ) == 1 );
    if ( isSony15 )
    {
      int device = reverse( extract( hex, 8, 8 ), 8 );
      System.err.println( "Sony15 and device=" + device );
      parms[ 1 ] = new Value( new Integer( 1 ), null );
      parms[ 2 ] = new Value( new Integer( device ), null );
      parms[ 3 ] = new Value( new Integer( 0 ), null );
    }
    else
    {
      boolean isSony20 = ( extract( hex, 15, 1 ) == 1 );
      if ( isSony20 )
      {
        parms[ 1 ] = new Value( new Integer( 2 ), null );
        int index = extract( hex, 13, 2 ) + 1;
        System.err.println( "Sony20 and index=" + index );
        parms[ 3 ] = new Value( new Integer( index ), null );
      }
      else
      {
        System.err.print( "Sony12" );
        parms[ 1 ] = new Value( new Integer( 0 ), null );
        parms[ 3 ] = parms[ 1 ];
      }
      int device = reverse( extract( hex, 8, 5 ), 5 );
      System.err.println( " and device=" + device );
      parms[ 2 ] = new Value( new Integer( device ), null );
    }
  }
}
