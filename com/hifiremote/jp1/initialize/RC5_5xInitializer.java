package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class RC5_5xInitializer.
 */
public class RC5_5xInitializer extends Initializer
{

  /**
   * Instantiates a new r c5_5x initializer.
   * 
   * @param parms
   *          the parms
   */
  public RC5_5xInitializer( String[] parms )
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
    ChoiceCmdParm choiceParm = ( ChoiceCmdParm )cmdParms[ 3 ];
    Choice[] choices = choiceParm.getChoices();
    for ( int i = 0; i < choices.length; i++ )
    {
      int devIndex = i * 2;
      int value = ( ( Integer )devParms[ devIndex ].getValueOrDefault() ).intValue();
      Choice choice = choices[ i ];
      int flag = ( ( Integer )devParms[ devIndex + 1 ].getValueOrDefault() ).intValue();
      choice.setText( Integer.toString( value ) + extra[ flag ] );
    }
    ( ( ChoiceEditor )choiceParm.getEditor() ).initialize();
  }

  /** The Constant extra. */
  private final static String[] extra =
  {
      ",Sub<64", ",Sub>63"
  };
}
