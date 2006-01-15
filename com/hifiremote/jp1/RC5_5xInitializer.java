package com.hifiremote.jp1;

public class RC5_5xInitializer
  extends Initializer
{
  public RC5_5xInitializer( String[] parms )
  {
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    ChoiceCmdParm choiceParm = ( ChoiceCmdParm )cmdParms[ 3 ];
    Choice[] choices = choiceParm.getChoices();
    for ( int i = 0; i < choices.length; i++ )
    {
      int devIndex = i * 2;
      int value = (( Integer )devParms[ devIndex ].getValueOrDefault()).intValue();
      Choice choice = choices[ i ];
      int flag = (( Integer )devParms[ devIndex + 1 ].getValueOrDefault()).intValue();
      choice.setText( Integer.toString( value ) + extra[ flag ]);
    }
    (( ChoiceEditor )choiceParm.getEditor()).initialize();
  }

  private final static String[] extra = { ",Sub<64", ",Sub>63" };
}
