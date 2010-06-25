package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class RC5Initializer.
 */
public class RC5Initializer extends Initializer
{

  /**
   * Instantiates a new r c5 initializer.
   * 
   * @param parms
   *          the parms
   */
  public RC5Initializer( String[] parms )
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
    Choice[] choices = ( ( ChoiceCmdParm )cmdParms[ 0 ] ).getChoices();
    for ( int i = 0; i < choices.length; i++ )
    {
      int devIndex = 2 * i;
      Integer device = null;
      if ( devParms[ devIndex ] != null && devParms[ devIndex ].getValue() != null )
      {
        device = ( Integer )devParms[ devIndex ].getValue();
      }

      Choice choice = choices[ i ];
      if ( device == null )
      {
        choice.setText( "n/a" );
        choice.setHidden( true );
      }
      else
      {
        String extra = null;
        int flag = ( ( Integer )devParms[ devIndex + 1 ].getValue() ).intValue();
        if ( flag == 0 )
        {
          extra = "OBC<64";
        }
        else
        {
          extra = "OBC>63";
        }

        choice.setText( device.toString() + ',' + extra );
        choice.setHidden( false );
      }
    }
    ( ( ChoiceEditor )( ( ChoiceCmdParm )cmdParms[ 0 ] ).getEditor() ).initialize();
  }

}
