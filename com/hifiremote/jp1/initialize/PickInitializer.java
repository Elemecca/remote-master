package com.hifiremote.jp1.initialize;

import java.util.StringTokenizer;

import com.hifiremote.jp1.Choice;
import com.hifiremote.jp1.ChoiceCmdParm;
import com.hifiremote.jp1.ChoiceEditor;
import com.hifiremote.jp1.CmdParameter;
import com.hifiremote.jp1.DeviceParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class PickInitializer.
 */
public class PickInitializer extends Initializer
{

  /**
   * Instantiates a new pick initializer.
   * 
   * @param parms
   *          the parms
   */
  public PickInitializer( String[] parms )
  {
    index = Integer.parseInt( parms[ 0 ] ); // index within cmdParms
    sources = new int[ parms.length - 1 ];
    separators = new String[ sources.length ];
    for ( int i = 0; i < parms.length - 1; i++ ) // i selects within the choices in cmdParms[index]
    {
      String str = parms[ i + 1 ].trim().toUpperCase();
      sources[ i ] = 0;
      if ( str.length() != 0 && str.charAt( 0 ) == 'N' ) // N means hide if the corresponding
      // devParms item is blank
      {
        sources[ i ] = noDefault;
        str = str.substring( 1 ).trim();
      }
      if ( str.length() == 0 )
      {
        sources[ i ] = noChange; // Blank means don't change the original cmdParms choice
      }
      else
      {
        StringTokenizer st = new StringTokenizer( str, "./-\\,:;~", true );
        if ( st.countTokens() > 1 )
        {
          int main = Integer.parseInt( st.nextToken() );
          separators[ i ] = st.nextToken();
          int sub = Integer.parseInt( st.nextToken() );
          sources[ i ] |= main << 8 | sub;
        }
        else
        {
          int v = Integer.parseInt( str ); // v is the devParm index for replacing the i'th choice
          sources[ i ] |= v;
        }
      }
    }
  }

  /**
   * Gets the parm.
   * 
   * @param index
   *          the index
   * @param parms
   *          the parms
   * @param useDefault
   *          the use default
   * @return the parm
   */
  private String getParm( int index, DeviceParameter[] parms, boolean useDefault )
  {
    if ( index >= parms.length )
    {
      return null;
    }

    Object parm = parms[ index ].getValue();

    if ( parm == null && useDefault )
    {
      parm = parms[ index ].getDefaultValue();
    }

    if ( parm == null )
    {
      return null;
    }

    return parm.toString();
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
    Choice[] choices = ( ( ChoiceCmdParm )cmdParms[ index ] ).getChoices();
    for ( int i = 0; i < choices.length && i < sources.length; i++ )
    {
      int s = sources[ i ];
      boolean useDefault = ( s & noDefault ) == 0;
      String text = null;
      if ( s != noChange )
      {
        int j = s & indexPart; // devParm index from which to get the new choice value
        int k = 0;
        if ( separators[ i ] != null )
        {
          k = j & 0xFF;
          j >>= 8;

          String text1 = getParm( j, devParms, useDefault );
          String text2 = getParm( k, devParms, useDefault );
          if ( text1 != null )
          {
            if ( text2 != null )
            {
              text = text1 + separators[ i ] + text2;
            }
            else
            {
              text = text1;
            }
          }
        }
        else
        {
          text = getParm( j, devParms, useDefault );
        }
        Choice choice = choices[ i ];
        if ( text == null )
        {
          choice.setText( "n/a" );
          choice.setHidden( true );
        }
        else
        {
          choice.setText( text );
          choice.setHidden( false );
        }
      }
    }

    ( ( ChoiceEditor )( ( ChoiceCmdParm )cmdParms[ index ] ).getEditor() ).initialize();
  }

  /** The index. */
  private int index;

  /** The sources. */
  private int[] sources;

  /** The no default. */
  private final int noDefault = 0x10000;

  /** The no change. */
  private final int noChange = 0x20000;

  /** The index part. */
  private final int indexPart = 0xFFFF;

  /** The separators. */
  private String[] separators;
}
