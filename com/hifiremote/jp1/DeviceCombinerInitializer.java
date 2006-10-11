package com.hifiremote.jp1;

import java.util.*;

public class DeviceCombinerInitializer
  extends Initializer
{
  public DeviceCombinerInitializer( List< CombinerDevice > devices, ChoiceCmdParm choice )
  {
    this.devices = devices;
    this.choice = choice;
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    Choice[] choices = choice.getChoices();
    int i = 0;
    for ( CombinerDevice device : devices )
    {
      Choice choice = choices[ i++ ];
      choice.setText( device.toString());
      choice.setHidden( false );
    }
    for (; i < 16; i++ )
    {
      choices[ i ].setText( "n/a" );
      choices[ i ].setHidden( true );
    }
    (( ChoiceEditor )choice.getEditor()).initialize();
  } 

  private List< CombinerDevice > devices = null;
  private ChoiceCmdParm choice = null;
}
