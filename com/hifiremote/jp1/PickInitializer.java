package com.hifiremote.jp1;

public class PickInitializer
  extends Initializer
{
  public PickInitializer( String[] parms )
  {
    index = Integer.parseInt( parms[ 0 ]);      // index within cmdParms
    sources = new int[ parms.length-1 ];
    for (int i=0; i<parms.length-1; i++)        // i selects within the choices in cmdParms[index]
    {
      String str = parms[i+1].trim().toUpperCase();
      sources[i] = 0;
      if ( str.length() != 0 && str.charAt(0) == 'N' )  // N means hide if the corresponding devParms item is blank
      {
        sources[i] = noDefault;
        str = str.substring(1).trim();
      }
      if ( str.length() == 0 )
      {
        sources[i] = noChange;  // Blank means don't change the original cmdParms choice
      }
      else
      {
        int v=Integer.parseInt( str );  // v is the devParm index for replacing the i'th choice
        sources[i] += v;
      }
    }
  }

  public void initialize( DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    Choice[] choices = (( ChoiceCmdParm )cmdParms[ index ]).getChoices();
    for (int i = 0 ; i < choices.length && i < sources.length; i++ )
    {
      int s = sources[ i ];
      if ( s != noChange )
      {
        int j = s & indexPart;   // devParm index from which to get the new choice value
        Object parm = null;
        if ( j < devParms.length )  // If that index is valid
        {
          parm = devParms[ j ].getValue();
          if (parm == null && (s & noDefault) == 0)
          {
            parm = devParms[ j ].getDefaultValue();
          }
        }
        Choice choice = choices[ i ];
        if ( parm == null )
        {
          choice.setText( "n/a" );
          choice.setHidden( true );
        }
        else
        {
          choice.setText( parm.toString());
          choice.setHidden( false );
        }
      }
    }

    (( ChoiceEditor )(( ChoiceCmdParm )cmdParms[ index ]).getEditor()).initialize();
  }

  private int index;
  private int[] sources;
  private final int noDefault=0x10000;
  private final int noChange=0x20000;
  private final int indexPart=0xFFFF;
}
