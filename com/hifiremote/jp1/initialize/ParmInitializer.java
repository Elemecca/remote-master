package com.hifiremote.jp1.initialize;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class ParmInitializer.
 */
public class ParmInitializer extends Initializer
{

  /**
   * Instantiates a new parm initializer.
   * 
   * @param parms
   *          the parms
   */
  public ParmInitializer( String[] parms )
  {
    if ( parms.length != 4 )
    {
      throw new IllegalArgumentException( "ParmInitializer requires exactly four values" );
    }

    cmdNdx = Integer.parseInt( parms[ 0 ] );
    devNdx = Integer.parseInt( parms[ 1 ] );
    bitPos = Integer.parseInt( parms[ 2 ] );
    bitCnt = Integer.parseInt( parms[ 3 ] );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Initializer#initialize(com.hifiremote.jp1.DeviceParameter[],
   * com.hifiremote.jp1.CmdParameter[])
   */
  @Override
  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    int step = 1 << bitPos;
    int count = 1 << bitCnt;
    int mask = -step * ( count - 1 ) - 1;
    int deflt = ( ( Integer )devParms[ devNdx ].getValueOrDefault() ).intValue();
    ChoiceCmdParm devCmdParm = ( ChoiceCmdParm )cmdParms[ cmdNdx ];
    Choice[] choices = devCmdParm.getChoices();
    int i = 0;
    for ( ; i < choices.length && i < count; i++ )
    {
      int value = ( deflt & mask ) + i * step;
      Choice choice = choices[ i ];
      choice.setText( Integer.toString( value ) );
      if ( value == deflt )
      {
        devCmdParm.setDefault( i );
      }
    }
    ( ( ChoiceEditor )devCmdParm.getEditor() ).initialize();
  }

  /** The cmd ndx. */
  private int cmdNdx = 0;

  /** The dev ndx. */
  private int devNdx = 0;

  /** The bit pos. */
  private int bitPos = 0;

  /** The bit cnt. */
  private int bitCnt = 1;
}
