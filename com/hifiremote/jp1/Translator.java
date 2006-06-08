package com.hifiremote.jp1;

public class Translator
  extends Translate
{
  public Translator( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i ++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ))
        lsb = true;
      else if ( text.equalsIgnoreCase( "comp" ))
        comp = true;
      else
      {
        int val = Integer.parseInt( text );
        switch ( parmIndex )
        {
          case IndexIndex:
            index = val;
            break;
          case BitsIndex:
            bits = val;
            break;
          case BitOffsetIndex:
            bitOffset = val;
            break;
          case LsbOffsetIndex:
            lsbOffset = val;
            break;
          case AdjustOffset:
          {
            adjust = val;
            break;
          }
          default:
            break;
        }
        parmIndex++;
      }
    }
  }

  public Translator( boolean lsb, boolean comp, int index, int bits, int bitOffset )
  {
    super( null );
    this.lsb = lsb;
    this.comp = comp;
    this.index = index;
    this.bits = bits;
    this.bitOffset = bitOffset;
  }

  private void adjustStyleAndBits( DeviceParameter[] devParms )
  {
    if ( styleIndex != -1 )
    {
      int style = (( Number )devParms[ styleIndex ].getValueOrDefault()).intValue();
      lsb = (( style & 2 ) == 2 );
      comp = (( style & 1 ) == 1 );
    }
    if ( bitsIndex != - 1 )
      bits = (( Number )devParms[ bitsIndex ].getValueOrDefault()).intValue();
  }

  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    adjustStyleAndBits( devParms );
    if (onlyIndex >= 0 && onlyIndex != index)
      return;
    if ( index >= parms.length )
    {
      System.err.println("Translator.in() index="+ index +" exceeds "+ parms.length +" item buffer");
      return;
    }
    int w=0;
    Value v = parms[index];
    if ( v == null )
    {
      System.err.println("Translator.in() index="+ index +" missing parameter");
    }
    else
    {
      Object o = v.getValue();
      Number i = null;
      if ( o == null )
      {
        System.err.println("Translator.in() index="+ index +" missing parameter value");
        return;
      }
      else if (( o.getClass() == Integer.class ) || ( o.getClass() == Short.class ))
        i = ( Number )v.getValue();
      else if ( o.getClass() == String.class )
        i = new Integer(( String )o );
      else if ( o instanceof DirectDefaultValue )
        i = ( Number )(( DefaultValue )o ).value();

      w = ( i.intValue() + adjust ) >> lsbOffset;
    }

    if ( getComp())
    {
      w = 0xFFFFFFFF - w;
    }
    if ( getLSB() )
    {
      w = reverse(w, bits );
    }

    insert( hexData, bitOffset, bits, w );
  }

  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    adjustStyleAndBits( devParms );
    if ( index >= parms.length )
    {
      System.err.println("Translator.out() index="+ index +" exceeds "+ parms.length +" item buffer");
      return;
    }
    int w = extract( hexData, bitOffset, bits);
    if ( getComp())
    {
      w = (2<<(bits-1)) - 1 - w;
    }
    if ( getLSB())
    {
      w = reverse(w, bits );
    }
    parms[ index ] = insert( parms[ index ], lsbOffset, bits, w - adjust );
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append( "Translator(" );
    if ( getLSB())
      buff.append( "lsb," );
    if ( getComp())
      buff.append( "comp," );
    buff.append( index );
    if (( bits != 8 ) || ( bitOffset != 0 ) || ( lsbOffset != 0 ))
    {
      buff.append( ',' );
      buff.append( bits );
    }
    if (( bitOffset != 0 ) || ( lsbOffset != 0 ))
    {
      buff.append( ',' );
      buff.append( bitOffset );
    }
    if ( lsbOffset != 0 )
    {
      buff.append( ',' );
      buff.append( lsbOffset );
    }
    buff.append( ')' );
    return buff.toString();
  }

  public boolean getLSB(){ return lsb; }
  public void setLSB( boolean lsb ){ this.lsb = lsb; }
  public boolean getComp(){ return comp; }
  public void setComp( boolean comp ){ this.comp = comp; }
  public void setStyleIndex( int index ){ styleIndex = index; }
  public int getIndex(){ return index; }
  public int getBits(){ return bits; }
  public void setBits( int bits ){ this.bits = bits; }
  public void setBitsIndex( int index ){ bitsIndex = index; }
  public int getBitOffset(){ return bitOffset; }
  protected boolean lsb = false;
  protected boolean comp = false;
  protected int styleIndex = -1;
  protected int index = 0;
  protected int bits = 8;
  protected int bitsIndex = -1;
  protected int bitOffset = 0;
  protected int lsbOffset = 0;
  protected int adjust = 0;

  protected final static int IndexIndex = 0;
  protected final static int BitsIndex = 1;
  protected final static int BitOffsetIndex = 2;
  protected final static int LsbOffsetIndex = 3;
  protected final static int AdjustOffset = 4;
}


