package com.hifiremote.jp1;

public class Rc5Translator
  extends Translate
{
  public Rc5Translator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int select = 0;
    int obc = 0;
    
    if ( onlyIndex == 0 )
    {
      select = (( Integer )parms[ 0 ].getValue()).intValue();
      insert( hexData, 6, 2, select );
    }
    else if ( onlyIndex == 1 )
    {
      obc = (( Integer )parms[ 1 ].getValue()).intValue() & 0x1F;
      insert( hexData, 0, 6, complement( obc, 6 ));
    }
    else
    {
      select = (( Integer )parms[ 0 ].getValue()).intValue();
      insert( hexData, 6, 2, select );
      obc = (( Integer )parms[ 1 ].getValue()).intValue();
      insert( hexData, 0, 6, complement( obc, 6 ));
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    // first do the device selector 
    int select = hex.getData()[0] & 3;
    if ( select == 3 )
      select = 0;
    parms[ 0 ] = new Value( new Integer( select ));

    // Now do the OBC
    int obc = complement( extract( hex, 0, 6 ), 6 );
    while ( select >= 0 )
    {
      int index = 2 * select;
      if (( devParms[ index ] != null ) &&
          ( devParms[ index ].getValue() != null ))
      {
        int flag = (( Integer )devParms[ index + 1 ].getValue()).intValue();
        if ( flag != 0 )
          obc |= 64;
        parms[ 1 ] = new Value( new Integer( obc ));
        break;
      }
      select--;
    }
  }
}

