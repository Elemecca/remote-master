package com.hifiremote.jp1;

public class PanasonicMixTranslator
  extends Translate
{
  public PanasonicMixTranslator( String[] textParms )
  {
    super( textParms );
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int iDev;
    if (onlyIndex > 1)
      return;
    Value dev = parms[0];
    Value sub = parms[1];
    if (dev == null)
    {
      int v = hexData.getData()[1] & 0x3F;
      for (iDev=6; iDev>0 && v > (0x20>>iDev); iDev--)
        ;
    }
    else
    {
      iDev = ((Integer)dev.getValue()).intValue();
    }
    iDev = 0x20 >> iDev;

    int iSub;
    if (sub == null)
    {
      int v = hexData.getData()[1] & 0x3F;
      for (iSub=5; iSub>0 && (v&(0x10>>iSub))==0; iSub--)
        ;
      if ( v == 0x10>>iSub )
        iSub = 0;
    }
    else
    {
      iSub = ((Integer)sub.getValue()).intValue();
    }
    if (iSub != 0)
      iDev |= (0x10 >> iSub);
    insert( hexData, 10, 6, iDev );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int v = hex.getData()[1] & 0x3F;
    int d;
    for (d=0; d<5 && v < (0x20>>d); d++)
      ;
    v -= (0x20>>d);
    int s;
    for (s=4; s>0 && v != (0x10>>s); s--)
      ;
    parms[ 0 ] = new Value( new Integer( d ), null );
    parms[ 1 ] = new Value( new Integer( s ), null );
  }

}

