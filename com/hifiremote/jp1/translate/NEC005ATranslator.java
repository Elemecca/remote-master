package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;


public class NEC005ATranslator extends Translate
{

  public NEC005ATranslator( String[] textParms )
  {
    super( textParms );
    styleBits = Integer.parseInt( textParms[ 0 ], 16 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int specialRecHandling = -1;
    short[] hex = hexData.getData();
    hex[ 0 ] = ( short )( 0x20 | (short)styleBits );   //(bit 5 == 1):  specify dev.sub format
    Number deviceNumber = ( Number )parms[ 0 ].getUserValue();
    if ( deviceNumber == null )
      deviceNumber = new Integer( 0 );
    hex[ 1 ] = ( short )reverse( complement( deviceNumber.intValue() ) );
    Number subDevice = ( Number )parms[ 1 ].getUserValue();
    if ( subDevice == null )  //implies no subdevice rather than taking the default value of the subdevice param
    {
      hex[ 0 ] &=0xDF;  
      hex[ 2 ] = 0x00;  //matches UEI approach (post 15-1994) of setting redundant 3rd byte of fixed data to 0
    } 
    else
      hex[ 2 ] = ( short )reverse(complement(subDevice.intValue()) );
    if  ((styleBits & 0x10) == 0)   //remaining code handles NEC1/NEC2 additional params
    {
      specialRecHandling = ( (Number)parms[2].getValue() ).intValue();
      if (specialRecHandling > 0)  
      {
        if(subDevice == null)
          subDevice = new Integer( 0 );  //the 3rd fixed byte has meaning, so here null implies subdevice is 0 by default
        hex[ 2 ] = ( short )reverse( complement( subDevice.intValue() ) );
        hex[ 0 ] |= 0x04;  
        if (specialRecHandling == 1) 
          hex[ 0 ] &= 0xDF;   // Send "subdevice" and its complement instead of device and its complement5
        else
          hex[ 0 ] |= 0x20;   // Send dev.sub but with LSB of each flipped
      }
      if (styleBits == 0x00)   //NEC1 only
      {
        Number NEC2onRepeatButton = (Number)parms[ 3 ].getValue();
        hex[ 0 ] |= (short)NEC2onRepeatButton.intValue() << 3;  
        Number twiceThenDittos = ( Number )parms[ 4 ].getUserValue();
        hex[ 0 ] |= (short)twiceThenDittos.intValue() << 1;  
      }
    }
  }
  /*
      Bit 0  Style        0=NEC1; 1=NEC2
      Bit 1  NEC1 only-   0=send signal once, dittos; 1=twice, dittos
      Bit 2  NEC1/2 only- 0=no record handling; 1=change devs on record button using Bit5: 0=use 3rd FD as dev; 1= XOR LSBs of dev/sub
      Bit 3  NEC1 only-   0=NEC1 for all buttons; 1=send NEC2 for repeating group buttons
      Bit 4  Style        0=NEC; 1=NECx
      Bit 5  NEC1/2 only- 0=dev; 1=dev.sub;  the executor ignores bit 5 if (bit 2 == 1)
      
      On entry, the style bits corresponding to the NEC1, NEC2, NECx1, or NECx2 versions of this executor:
      NEC1:  0x00
      NEC2:  0x01
      NECx1: 0x30
      NECx2: 0x32
    */
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    short[] hex = hexData.getData();
    Integer deviceNumber = new Integer( byte2int( reverse( complement( hex[ 1 ] ) ) ) );
    Integer subDevice = new Integer( byte2int( reverse( complement( hex[ 2 ] ) ) ) );
    Integer controlBits = new Integer( hex[ 0 ] );
    boolean dev_subFormat = false;
    int specialRecHandling = 0;
    if ( ( controlBits.intValue() & styleBits ) != styleBits )
    {
      deviceNumber = null;   // control bits don't correspond to this protocol
      subDevice = null;
    }
    else
    {
      dev_subFormat = (controlBits & 0x20) == 0x20;
      if  ((controlBits & 0x10) == 0)   //NEC1 or NEC2
      {
        specialRecHandling = (controlBits & 0x04) >> 2;
        if (specialRecHandling == 1) 
        {
          if (dev_subFormat) 
            specialRecHandling = 2;
          else 
            specialRecHandling = 1;
        }
        parms[ 2 ] = new Value((specialRecHandling), null );
        if ((controlBits & 0x11) == 0)  //NEC1 only
        {
          parms[ 3 ]= new Value( (controlBits & 0x08) >> 3, null );
          parms[ 4 ]= new Value( (controlBits & 0x02) >> 1, null );  
        }
      }
    }
    parms[ 0 ] = new Value( deviceNumber, null );
    if ( dev_subFormat || specialRecHandling == 1 )
      parms[ 1 ] = new Value( subDevice, null );
    else 
      parms[ 1 ] = new Value( null, null );
  }


  private int styleBits;
}
