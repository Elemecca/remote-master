package com.hifiremote.jp1;

public class ParmInitializer
  extends Initializer
{
  public ParmInitializer( String[] parms )
  {
    index = Integer.parseInt( parms[ 0 ]);
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    Choice[] choices = (( ChoiceCmdParm )cmdParms[ index ]).getChoices();
    int i = 0;
    for ( ; i < devParms.length; i++ )
    {
      Integer temp = ( Integer )devParms[ i ].getValue();
      Choice choice = choices[ i ];
      if ( temp == null )
      {
        choice.setText( "n/a" );
        choice.setHidden( true );
      }
      else
      {
        choice.setText( temp.toString());
        choice.setHidden( false );
      }
    }
    for ( ; i < choices.length; i++ )
      choices[ i ].setHidden( true );

    (( ChoiceEditor )(( ChoiceCmdParm )cmdParms[ index ]).getEditor()).initialize();
  }

  private int index;
}
