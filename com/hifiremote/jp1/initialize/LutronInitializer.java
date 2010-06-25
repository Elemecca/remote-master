package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class LutronInitializer.
 */
public class LutronInitializer extends Initializer
{

  /**
   * Instantiates a new lutron initializer.
   * 
   * @param parms
   *          the parms
   */
  public LutronInitializer( String[] parms )
  {}

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Initializer#initialize(com.hifiremote.jp1.DeviceParameter[],
   * com.hifiremote.jp1.CmdParameter[])
   */
  @Override
  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    int device = ( ( Integer )devParms[ 0 ].getValueOrDefault() ).intValue();
    int baseDevice = device & 0xFC;
    Choice[] choices = ( ( ChoiceCmdParm )cmdParms[ 1 ] ).getChoices();
    for ( int i = 0; i < choices.length; i++ )
    {
      int value = baseDevice + i;
      Choice choice = choices[ i ];
      choice.setText( Integer.toString( value ) );
    }
    ( ( ChoiceEditor )( ( ChoiceCmdParm )cmdParms[ 1 ] ).getEditor() ).initialize();
  }
}
