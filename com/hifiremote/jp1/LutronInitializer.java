package com.hifiremote.jp1;

public class LutronInitializer
  extends Initializer
{
  public LutronInitializer( String[] parms )
  {
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    int device = (( Integer )devParms[ 0 ].getValueOrDefault()).intValue();
    int baseDevice = device & 0xFC;
    Choice[] choices = (( ChoiceCmdParm )cmdParms[ 1 ]).getChoices();
    for ( int i = 0 ; i < choices.length; i++ )
    {
      int value = baseDevice + i;
      Choice choice = choices[ i ];
      choice.setText( Integer.toString( value ));
    }
    (( ChoiceEditor )(( ChoiceCmdParm )cmdParms[ 1 ]).getEditor()).initialize();
  }
}
