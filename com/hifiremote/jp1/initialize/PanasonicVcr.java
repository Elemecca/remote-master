package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class PanasonicVcr.
 */
public class PanasonicVcr extends Initializer
{

  /**
   * Instantiates a new panasonic vcr.
   * 
   * @param parms
   *          the parms
   */
  public PanasonicVcr( String[] parms )
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
    int dev = ( ( Integer )devParms[ 0 ].getValueOrDefault() ).intValue();
    int sub = ( ( Integer )devParms[ 1 ].getValueOrDefault() ).intValue();
    ChoiceCmdParm devCmdParm = ( ChoiceCmdParm )cmdParms[ 0 ];
    ChoiceCmdParm subCmdParm = ( ChoiceCmdParm )cmdParms[ 1 ];
    Choice[] devChoices = devCmdParm.getChoices();
    Choice[] subChoices = subCmdParm.getChoices();
    devChoices[ 0 ].setText( Integer.toString( dev | 16 ) );
    devChoices[ 1 ].setText( Integer.toString( dev & ~16 ) );
    subChoices[ 0 ].setText( Integer.toString( sub & ~1 ) );
    subChoices[ 1 ].setText( Integer.toString( sub | 1 ) );
    devCmdParm.setDefault( ( dev & 16 ) == 0 ? 1 : 0 );
    subCmdParm.setDefault( ( sub & 1 ) == 0 ? 0 : 1 );
    ( ( ChoiceEditor )devCmdParm.getEditor() ).initialize();
    ( ( ChoiceEditor )subCmdParm.getEditor() ).initialize();
    if ( ( dev & sub & ~17 ) != 0 )
    {
      throw new IllegalArgumentException( "All four combinations will have wrong check bytes" );
    }
    else if ( ( dev & 1 ) != 0 )
    {
      throw new IllegalArgumentException( dev + "." + ( sub | 1 ) + " and " + ( dev ^ 16 ) + "." + ( sub | 1 )
          + " will have wrong check bytes" );
    }
    else if ( ( sub & 16 ) != 0 )
    {
      throw new IllegalArgumentException( ( dev | 16 ) + "." + sub + " and " + ( dev | 16 ) + "." + ( sub ^ 1 )
          + " will have wrong check bytes" );
    }
  }
}
