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
          case indexIndex:
            index = val;
            break;
          case bitsIndex:
            bits = val;
            break;
          case bitOffsetIndex:
            bitOffset = val;
            break;
          case lsbOffsetIndex:
            lsbOffset = val;
            break;
          default:
            break;
        }
        parmIndex++;
      }
    }
  }

  public void in( Value[] parms, byte[] hex, DeviceParameter[] devParms, int onlyIndex )
  {
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
      Integer i = v.getValue();
      if ( i == null )
        System.err.println("Translator.in() index="+ index +" missing parameter value");
      else
        w = i.intValue() >> lsbOffset;
    }

    if ( comp )
    {
      w = 0xFFFFFFFF - w;
    }
    if ( lsb )
    {
      w = reverse(w, bits );
    }

    insert( hex, bitOffset, bits, w );
  }

  public void out( byte[] hex, Value[] parms, DeviceParameter[] devParms )
  {
    if ( index >= parms.length )
    {
      System.err.println("Translator.out() index="+ index +" exceeds "+ parms.length +" item buffer");
      return;
    }
    int w = extract( hex, bitOffset, bits);
    if ( comp )
    {
      w = (2<<(bits-1)) - 1 - w;
    }
    if ( lsb )
    {
      w = reverse(w, bits );
    }
    int old = 0;
    if ( parms[ index ] != null )
       old = parms[ index ].getValue().intValue() & ((1<<lsbOffset)-1);
    parms[ index ] = new Value( new Integer( old + (w<<lsbOffset) ), null );
  }

  public boolean getLSB(){ return lsb; }
  public boolean getComp(){ return comp; }
  public int getIndex(){ return index; }
  public int getBits(){ return bits; }
  public int getBitOffset(){ return bitOffset; }
  private boolean lsb = false;
  private boolean comp = false;
  private int index = 0;
  private int bits = 8;
  private int bitOffset = 0;
  private int lsbOffset = 0;

  private final static int indexIndex = 0;
  private final static int bitsIndex = 1;
  private final static int bitOffsetIndex = 2;
  private final static int lsbOffsetIndex = 3;
}


