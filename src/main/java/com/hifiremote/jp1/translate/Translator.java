package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DefaultValue;
import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.DirectDefaultValue;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Translator.
 */
public class Translator extends Translate
{

  /**
   * Instantiates a new translator.
   * 
   * @param textParms
   *          the text parms
   */
  public Translator( String[] textParms )
  {
    super( textParms );
    int parmIndex = 0;
    for ( int i = 0; i < textParms.length; i++ )
    {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ) )
      {
        lsb = true;
      }
      else if ( text.equalsIgnoreCase( "comp" ) )
      {
        comp = true;
      }
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
        parmIndex++ ;
      }
    }
  }

  /**
   * Instantiates a new translator.
   * 
   * @param lsb
   *          the lsb
   * @param comp
   *          the comp
   * @param index
   *          the index
   * @param bits
   *          the bits
   * @param bitOffset
   *          the bit offset
   */
  public Translator( boolean lsb, boolean comp, int index, int bits, int bitOffset )
  {
    super( null );
    this.lsb = lsb;
    this.comp = comp;
    this.index = index;
    this.bits = bits;
    this.bitOffset = bitOffset;
  }
  
  @Override
  public boolean equals( Object obj )
  {
    if ( obj == null || !( obj instanceof Translator ) )
    {
      return false;
    }
    Translator t = ( Translator )obj;
    return ( lsb == t.lsb && comp == t.comp && index == t.index && bits == t.bits && bitOffset == t.bitOffset );
  }

  /**
   * Adjust style and bits.
   * 
   * @param devParms
   *          the dev parms
   */
  private void adjustStyleAndBits( DeviceParameter[] devParms )
  {
    if ( styleIndex != -1 )
    {
      int style = ( ( Number )devParms[ styleIndex ].getValueOrDefault() ).intValue();
      lsb = ( style & 2 ) == 2;
      comp = ( style & 1 ) == 1;
    }
    if ( bitsIndex != -1 )
    {
      bits = ( ( Number )devParms[ bitsIndex ].getValueOrDefault() ).intValue();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    adjustStyleAndBits( devParms );
    if ( onlyIndex >= 0 && onlyIndex != index )
    {
      return;
    }
    if ( index >= parms.length )
    {
      System.err.println( "Translator.in() index=" + index + " exceeds " + parms.length + " item buffer" );
      return;
    }
    int w = 0;
    Value v = parms[ index ];
    if ( v == null )
    {
      System.err.println( "Translator.in() index=" + index + " missing parameter" );
    }
    else
    {
      Object o = v.getValue();
      Number i = null;
      if ( o == null )
      {
        System.err.println( "Translator.in() index=" + index + " missing parameter value" );
        return;
      }
      else if ( o.getClass() == Integer.class || o.getClass() == Short.class )
      {
        i = ( Number )v.getValue();
      }
      else if ( o.getClass() == String.class )
      {
        i = new Integer( ( String )o );
      }
      else if ( o instanceof DirectDefaultValue )
      {
        i = ( Number )( ( DefaultValue )o ).value();
      }

      w = i.intValue() + adjust >> lsbOffset;
    }

    if ( getComp() )
    {
      w = 0xFFFFFFFF - w;
    }
    if ( getLSB() )
    {
      w = reverse( w, bits );
    }

    insert( hexData, bitOffset, bits, w );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    adjustStyleAndBits( devParms );
    if ( index >= parms.length )
    {
      System.err.println( "Translator.out() index=" + index + " exceeds " + parms.length + " item buffer" );
      return;
    }
    int w = extract( hexData, bitOffset, bits );
    if ( getComp() )
    {
      w = ( 2 << bits - 1 ) - 1 - w;
    }
    if ( getLSB() )
    {
      w = reverse( w, bits );
    }
    parms[ index ] = insert( parms[ index ], lsbOffset, bits, w - adjust );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder buff = new StringBuilder();
    buff.append( "Translator(" );
    if ( getLSB() )
    {
      buff.append( "lsb," );
    }
    if ( getComp() )
    {
      buff.append( "comp," );
    }
    buff.append( index );
    if ( bits != 8 || bitOffset != 0 || lsbOffset != 0 )
    {
      buff.append( ',' );
      buff.append( bits );
    }
    if ( bitOffset != 0 || lsbOffset != 0 )
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

  /**
   * Gets the lSB.
   * 
   * @return the lSB
   */
  public boolean getLSB()
  {
    return lsb;
  }

  /**
   * Sets the lSB.
   * 
   * @param lsb
   *          the new lSB
   */
  public void setLSB( boolean lsb )
  {
    this.lsb = lsb;
  }

  /**
   * Gets the comp.
   * 
   * @return the comp
   */
  public boolean getComp()
  {
    return comp;
  }

  /**
   * Sets the comp.
   * 
   * @param comp
   *          the new comp
   */
  public void setComp( boolean comp )
  {
    this.comp = comp;
  }

  /**
   * Sets the style index.
   * 
   * @param index
   *          the new style index
   */
  public void setStyleIndex( int index )
  {
    styleIndex = index;
  }

  /**
   * Gets the index.
   * 
   * @return the index
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Sets the index.
   * 
   * @param newIndex
   *          the new index
   */
  public void setIndex( int newIndex )
  {
    index = newIndex;
  }

  /**
   * Gets the bits.
   * 
   * @return the bits
   */
  public int getBits()
  {
    return bits;
  }

  /**
   * Sets the bits.
   * 
   * @param bits
   *          the new bits
   */
  public void setBits( int bits )
  {
    this.bits = bits;
  }

  /**
   * Gets the bits index.
   * 
   * @return the bits index
   */
  public int getBitsIndex()
  {
    return bitsIndex;
  }

  /**
   * Sets the bits index.
   * 
   * @param index
   *          the new bits index
   */
  public void setBitsIndex( int index )
  {
    bitsIndex = index;
  }

  /**
   * Gets the bit offset.
   * 
   * @return the bit offset
   */
  public int getBitOffset()
  {
    return bitOffset;
  }

  /**
   * Sets the bit offset.
   * 
   * @param newOffset
   *          the new bit offset
   */
  public void setBitOffset( int newOffset )
  {
    bitOffset = newOffset;
  }

  /** The lsb. */
  protected boolean lsb = false;

  /** The comp. */
  protected boolean comp = false;

  /** The style index. */
  protected int styleIndex = -1;

  /** The index. */
  protected int index = 0;

  /** The bits. */
  protected int bits = 8;

  /** The bits index. */
  protected int bitsIndex = -1;

  /** The bit offset. */
  protected int bitOffset = 0;

  /** The lsb offset. */
  protected int lsbOffset = 0;

  /** The adjust. */
  protected int adjust = 0;

  /** The Constant IndexIndex. */
  protected final static int IndexIndex = 0;

  /** The Constant BitsIndex. */
  protected final static int BitsIndex = 1;

  /** The Constant BitOffsetIndex. */
  protected final static int BitOffsetIndex = 2;

  /** The Constant LsbOffsetIndex. */
  protected final static int LsbOffsetIndex = 3;

  /** The Constant AdjustOffset. */
  protected final static int AdjustOffset = 4;
}
