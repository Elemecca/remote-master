package com.hifiremote.jp1;

public class PanasonicVcr
  extends Initializer
{
  public PanasonicVcr( String[] parms )
  {
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    int dev = devParms[0].getValueOrDefault().intValue();
    int sub = devParms[1].getValueOrDefault().intValue();
    ChoiceCmdParm devCmdParm = ( ChoiceCmdParm )cmdParms[ 0 ];
    ChoiceCmdParm subCmdParm = ( ChoiceCmdParm )cmdParms[ 1 ];
    Choice[] devChoices = devCmdParm.getChoices();
    Choice[] subChoices = subCmdParm.getChoices();
    devChoices[0].setText( Integer.toString(dev | 16) );
    devChoices[1].setText( Integer.toString(dev &~ 16) );
    subChoices[0].setText( Integer.toString(sub &~ 1) );
    subChoices[1].setText( Integer.toString(sub | 1) );
    devCmdParm.setDefault( new Integer( (dev&16)==0 ? 1 : 0 ) );
    subCmdParm.setDefault( new Integer( (sub&1)==0 ? 0 : 1 ) );
    (( ChoiceEditor )devCmdParm.getEditor()).initialize();
    (( ChoiceEditor )subCmdParm.getEditor()).initialize();
    if ( ( dev & sub & ~17 ) != 0 )
      KeyMapMaster.showMessage( "All four combinations will have wrong check bytes" );
    else if ( ( dev & 1 ) != 0 )
      KeyMapMaster.showMessage( dev +"."+ (sub|1) +" and "+ (dev^16) +"."+ (sub|1) +" will have wrong check bytes" );
    else if ( ( sub & 16 ) != 0 )
      KeyMapMaster.showMessage( (dev|16) +"."+ sub +" and "+ (dev|16) +"."+ (sub^1) +" will have wrong check bytes" );
  }
}
