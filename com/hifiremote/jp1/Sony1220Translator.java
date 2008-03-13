package com.hifiremote.jp1;

public class Sony1220Translator
  extends Translate
{
  public Sony1220Translator( String[] textParms )
  {
    super( textParms );
    if (( textParms == null ) || textParms.length == 0 )
      return;
    
    deviceIndex = Integer.parseInt( textParms[ 0 ]);
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int device = 0;
    int flag = 1;
    
    Value val = parms[ deviceIndex ];
    if ( val.hasUserValue())
    {
      flag = 0;
      device = (( Number )val.getUserValue()).intValue();
    }
    
    insert( hexData, 7, 1, flag );
    insert( hexData, 8, 8, reverse( device ));
 }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int flag = extract( hexData, 7, 1 );
    Integer device = null;
    if ( flag == 0 )
      device = new Integer( reverse( extract( hexData, 8, 8 )));

    parms[ deviceIndex ] = new Value( device, null );
  }

  private int deviceIndex = 1;
}
