package com.hifiremote.jp1;

public class RC5Initializer
  extends Initializer
{
  public RC5Initializer( String[] parms )
  {
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    Choice[] choices = (( ChoiceCmdParm )cmdParms[ 0 ]).getChoices();
    for ( int i = 0 ; i < choices.length; i++ )
    {
      int devIndex = 2 * i;
      Integer device = null;
      if (( devParms[ devIndex ] != null ) && ( devParms[ devIndex ].getValue() != null ))
        device = ( Integer )devParms[ devIndex ].getValue();
        
      Choice choice = choices[ i ];
      if ( device == null )
      {
        choice.setText( "n/a" );
        choice.setHidden( true );
      }
      else
      {
        String extra = null;
        int flag = (( Integer )devParms[ devIndex + 1 ].getValue()).intValue();
        if ( flag == 0 )
          extra = "OBC<64";
        else
          extra = "OBC>63";
  
        choice.setText( device.toString() + ',' + extra );
        choice.setHidden( false );
      }
    }
    (( ChoiceEditor )(( ChoiceCmdParm )cmdParms[ 0 ]).getEditor()).initialize();
  }

}
