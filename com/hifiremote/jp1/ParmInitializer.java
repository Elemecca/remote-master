package com.hifiremote.jp1;

public class ParmInitializer
  extends Initializer
{
  public ParmInitializer( String[] parms )
  {
    if ( parms.length != 4 )
      throw new IllegalArgumentException( "ParmInitializer requires exactly four values" );

    cmdNdx = Integer.parseInt( parms[ 0 ]);
    devNdx = Integer.parseInt( parms[ 1 ]);
    bitPos = Integer.parseInt( parms[ 2 ]);
    bitCnt = Integer.parseInt( parms[ 3 ]);
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    int step = 1 << bitPos;
    int count = 1 << bitCnt;
    int mask = -step*(count-1) - 1;
    int deflt = (( Integer )devParms[ devNdx ].getValueOrDefault()).intValue();
    ChoiceCmdParm devCmdParm = ( ChoiceCmdParm )cmdParms[ cmdNdx ];
    Choice[] choices = devCmdParm.getChoices();
    int i = 0;
    for ( ; i < choices.length && i < count; i++ )
    {
      int value = (deflt & mask) + (i * step);
      Choice choice = choices[ i ];
      choice.setText( Integer.toString(value) );
      if ( value == deflt )
      {
        devCmdParm.setDefault( i );
      }
    }
    (( ChoiceEditor )devCmdParm.getEditor()).initialize();
  }

  private int cmdNdx=0;
  private int devNdx=0;
  private int bitPos=0;
  private int bitCnt=1;
}
