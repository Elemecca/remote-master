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
    int oldSelect = hexData.getData()[0] & 3;
    if (oldSelect==3)
      oldSelect = 0;
    int device = ((Integer)devParms[oldSelect*2].getValueOrDefault()).intValue();
    int flag = ((Integer)devParms[oldSelect*2+1].getValue()).intValue();
    if (parms[0]!=null && parms[0].getValue()!=null)
    {
      int select = ((Integer)parms[0].getValue()).intValue();
      device = ((Integer)devParms[select*2].getValueOrDefault()).intValue();
    }
    if (parms[1]!=null && parms[1].getValue()!=null)
    {
      flag = ((Integer)parms[1].getValue()).intValue() >> 6;
    }

    int select;
    int devN=0;
    int flagN = 1;
    int newSelect = 3;
    for (select=0; select<3; select++)
    {
      flagN = 1 - flagN;
      Integer devI = (Integer)devParms[select*2].getValue();
      if ( devI != null )
      {
        devN = devI.intValue();
        flagN = ((Integer)devParms[select*2+1].getValue()).intValue();
      }
      if ( device == devN )
      {
        newSelect = select;
        if ( flag == flagN )
          break;
      }
    }
    insert( hexData, 6, 2, newSelect );
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int trueSelect = hex.getData()[0] & 3;
    int select = trueSelect;
    if (trueSelect != 3)
    {
      int flag = 1;
      for (select=0; ; select++)
      {
        flag = 1 - flag;
        Integer devI = (Integer)devParms[select*2].getValue();
        if ( devI != null )
          flag = ((Integer)devParms[select*2+1].getValue()).intValue();
        if ( select>=trueSelect )
          break;
      }
      int device = ((Integer)devParms[select*2].getValueOrDefault()).intValue();
      parms[1] = insert( parms[1], 6, 1, flag );
      for (select=0; select<3; select++)
      {
        Integer deviceN = (Integer)devParms[select*2].getValue();
        if (deviceN != null && deviceN.intValue() == device)
          break;
      }
    }
    parms[ 0 ] = new Value( new Integer( select ), null );
  }

}

