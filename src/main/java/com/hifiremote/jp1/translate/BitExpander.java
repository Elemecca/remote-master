package com.hifiremote.jp1.translate;
import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

/*
  BitExpander(devCmdIndex, numChunksToConvert, destOffset, srcLsbOffset, dstBitsPerChunk, <chunkDefs>)
  Expands digit(s) to a sequence of bits (a "chunk"). Either device or command data may be expanded.
    Primarily useful for executors which send phase shifting IR protocols. 
    Each bit in a chunk represents an interval of time in a pattern of flashes.
    A zero bit in a chunk represent Off, and a one represents On
*/
public class BitExpander extends Translate
{
  public BitExpander( String[] textParms )
  { 
    super( textParms );
    int i;
    String[] nonLsbParms = new String[textParms.length];
    int ParmIndex = 0;
    for ( i = 0; i < textParms.length; i++ ) {
      String text = textParms[ i ];
      if ( text.equalsIgnoreCase( "lsb" ) ) lsb = true;
      else  {
          nonLsbParms[ParmIndex] = text;
          ParmIndex++ ;
      }
    }
    devCmdIndex = Integer.parseInt( nonLsbParms[ 0 ] );
    numChunksToConvert = Integer.parseInt( nonLsbParms[ 1 ] );
    dstMsbOffset = Integer.parseInt( nonLsbParms[ 2 ] );
    srcLsbOffset = Integer.parseInt( nonLsbParms[ 3 ] );
    dstBitsPerChunk = Integer.parseInt( nonLsbParms[ 4 ] );
    int numChunkDefs = ParmIndex-5;
    i = numChunkDefs;
    while (i > 1) {
      srcBitsPerChunk++;
      i >>= 1;
    }
    chunkDefs = new int[numChunkDefs];
    for ( i = 0; i < numChunkDefs; i++ )  {
      chunkDefs[i] = Integer.parseInt( nonLsbParms[ i+5 ], 16 );
    }
  }
  
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    int w, i, aChunk,  hex, totalXbits = 0;
    int mask = (1 << srcBitsPerChunk) - 1;
    int value = ( ( Number )parms[ devCmdIndex ].getValue() ).intValue();
    w = value >> srcLsbOffset;
    if ( lsb ) w = reverse( w, numChunksToConvert * srcBitsPerChunk);
    for (i = numChunksToConvert - 1; i > -1; i-- ) {
      aChunk = w >> (i * srcBitsPerChunk);
      hex = chunkDefs[aChunk & mask];
      insert( hexData, dstMsbOffset + totalXbits, dstBitsPerChunk, hex );
      totalXbits += dstBitsPerChunk;
    }
  }
  
  private int indexOfXbits( int Xchunk) {
    int i, top;
    top = (1 << srcBitsPerChunk);
    for (i = 0; i < top;  i++ ) {
      if (chunkDefs[i] == Xchunk) {
        return i;
      }
    }
    return 0;
  }
  
  public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
  {
    int i, Xchunk, theXbits, uncoded, w = 0;
    int mask = (1 << dstBitsPerChunk) - 1;
    theXbits = extract( hexData, dstMsbOffset, dstBitsPerChunk * numChunksToConvert );
    for (i = 0; i < numChunksToConvert;  i++ ) {
      Xchunk = theXbits >> (i * dstBitsPerChunk);
      uncoded = indexOfXbits(Xchunk & mask);
      if (uncoded < 0) {
        System.err.println( "BitExpander.out() can't decode " + Xchunk );
        return;
      }
      w |= (uncoded << (i * srcBitsPerChunk ));
    }
    i = numChunksToConvert * srcBitsPerChunk;
    if ( lsb ) w = reverse( w, i);
    parms[ devCmdIndex ] = insert( parms[ devCmdIndex ], srcLsbOffset, i, w );
  }
  
  private int devCmdIndex = 0;        //selects (0 based) an item from DevParm or CmdParm
  private int numChunksToConvert = 0; //number of chunks to convert
  private int dstMsbOffset = 0;       //the msb bit position at which to store the bit expansion.
  private int srcLsbOffset = 0;       //an lsb bit position within the DevParms or CmdParms item 
  private int srcBitsPerChunk = 0;    //the number of bits required to enumerate the chunks
  private int dstBitsPerChunk = 0;    //the number of bits in each expanded chunk
  private boolean lsb = false;        //if true, reverse the src bits before expanding
  private int[] chunkDefs;            //list of chunks in hexadecimal notation (right justified) 



}
