package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceCombinerInitializer.
 */
public class DeviceCombinerInitializer
  extends Initializer
{
  
  /**
   * Instantiates a new device combiner initializer.
   * 
   * @param devices the devices
   * @param choice the choice
   */
  public DeviceCombinerInitializer( List< CombinerDevice > devices, ChoiceCmdParm choice )
  {
    this.devices = devices;
    this.choice = choice;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Initializer#initialize(com.hifiremote.jp1.DeviceParameter[], com.hifiremote.jp1.CmdParameter[])
   */
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

  /** The devices. */
  private List< CombinerDevice > devices = null;
  
  /** The choice. */
  private ChoiceCmdParm choice = null;
}
