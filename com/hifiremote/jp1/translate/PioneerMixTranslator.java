package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class PioneerMixTranslator.
 */
public class PioneerMixTranslator extends Translate
{

  /**
   * Instantiates a new pioneer mix translator.
   * 
   * @param textParms
   *          the text parms
   */
  public PioneerMixTranslator( String[] textParms )
  {
    super( textParms );
    if( textParms.length > 0) 
      execVariant = Integer.parseInt( textParms[ 0 ] ); 
    else 
      execVariant = 3;
   if (execVariant == 5) 
     mask = 0x3F;
   else
     mask = 0x07;
  }

  // called to store parms into hex data
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    int flag = extract( hex, 10, 6 );
    boolean doInsert = true, hasPrefix = false;
    for ( int i = 0; i < parms.length; i++ )
    {
      if ( onlyIndex != -1 && onlyIndex != i )
        continue;
      if ( parms[ i ] != null && parms[ i ].getValue() != null )  {
        int val = ( ( Number )parms[ i ].getValue() ).intValue();
        if ( parms[ 0 ] != null && parms[ 0 ].getValue() != null )
          hasPrefix = (( (Number)parms[0].getValue() ).intValue() & 0x01) == 1;
        switch ( i ) {
          case 0: // Prefix device
            if ( val == 0 ) 
              flag &= 0xFE;
            else 
              flag |= 1;
            break;
          case 1: // Prefix cmd
            if ( val == 0 ) 
              flag &= 0xFE;
            else 
              flag = (flag & 0xF9) | ((val - 1) << 1); // bits 1 and 2
            break;
          case 2: // Device
            if (execVariant == 3) {
                if  (val < 4)  
                  flag &= 0xFE;
                else 
                  flag |= 1;
            } else if (execVariant == 4) {
              if (val < 4)  // 1 part signal   
                flag = (flag & 0xF8) | (val << 1); // bits 1 and 2
                else 
                  if (val == 4)  // 2 part signal
                    flag |=0x01;    
            } else if (execVariant == 5) {
              if  (val < 4  && !hasPrefix)    // 1 part signal   
                flag = (flag & 0xF8) | (val << 1); // bits 1 and 2
              else if  (val == 4)
                flag &= 0xDF;  //turn off bit 5
              else if (val > 0){
                flag = (flag & 0xE7) | ((val - 1) << 3 ) | 0x21; // bits 3 and 4
              }
            }
            break;
          case 3: // OBC
            if ( onlyIndex == 3 ) {
              doInsert = false;
            }
            break;
        }
      }
    }
    if ( doInsert ) {
      insert( hex, 10, 6, flag & mask );
    }
  }

  // called to extract parms from hex data
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    int val = extract( hex, 10, 6 );
    int val21 = (val >> 1) & 0x03;
    int val43 = (val >> 3) & 0x03;
    Integer zero = new Integer( 0 );
    Integer one = new Integer( 1 );
    if ( (val & 0x01) == 0 ) {  // 1 part signal
      parms[ 0 ] = new Value( zero, null ); // PrefixDevice = none
      parms[ 1 ] = new Value( zero, null ); // PrefixCmd = none
      if (execVariant == 3) 
        parms[ 2 ] = new Value( zero, null ); // Device = dev1
      else 
        parms[ 2 ] = new Value( new Integer( val21 ), null ); // Device can be dev1 to dev4
    }
    else {     // 2 part signal
      parms[ 0 ] = new Value( one, null ); // PrefixDevice = dev1
      if (execVariant == 3) {
        parms[ 1 ] = new Value( new Integer( val21 + 1 ), null ); // PrefixCmd = cmd1
        parms[ 2 ] = new Value( one, null ); // Device = dev2
      }
      else {
        parms[ 1 ] = new Value( new Integer( val21 + 1), null ); // PrefixCmd
        if ((val & 0x20) == 0) 
          parms[ 2 ] = new Value( new Integer( 4 ), null ); 
        else                                   // only true if Variant 5
          parms[ 2 ] = new Value( new Integer( val43 + 1 ), null );  
      }
    }
  }
  private int execVariant = 3;
  private int mask = 0xFF;
  private int flag = 0;
}
