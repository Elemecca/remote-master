package com.hifiremote.jp1;

public class SharpDVDTranslator
  extends Translate
{
  public SharpDVDTranslator( String[] textParms )
  {
    super( textParms );
  }
  
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int data = 0x70;
    int temp = ( 1 + extract( hex, 0, 1 ) + extract( hex, 4, 1 )) % 2;
    if ( temp != 0 )
      data |= 0x08;
    temp = ( extract( hex, 1, 1 ) + extract( hex, 5, 1 )) % 2;
    if ( temp != 0 )
      data |= 0x04;
    temp = ( 1 + extract( hex, 2, 1 ) + extract( hex, 6, 1 )) % 2;
    if ( temp != 0 )
      data |= 0x02;
    temp = ( extract( hex, 3, 1 ) + extract( hex, 7, 1 )) % 2;
    if ( temp != 0 )
      data |= 0x01;
    insert( hex, 8, 8, data );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms ){}
  
}
