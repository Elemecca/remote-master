package com.hifiremote.jp1;

import java.io.*;

// TODO: Auto-generated Javadoc
/**
 * The Class TranslatorEditorNode.
 */
public class TranslatorEditorNode
  extends ProtocolEditorNode
{
  
  /**
   * Instantiates a new translator editor node.
   */
  public TranslatorEditorNode()
  {
    super( "Translator", false );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#getEditingPanel()
   */
  public ProtocolEditorPanel getEditingPanel()
  {
    if ( panel == null )
      panel = new TranslatorEditorPanel();
    return panel;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#print(java.io.PrintWriter)
   */
  public void print( PrintWriter pw )
  {
    if ( order == LSB )
      pw.print( "lsb," );
    if ( comp )
      pw.print( "comp," );
    pw.print( getBits());
    pw.print( ',' );
    pw.print( msbOffset );
    if (( lsbOffset != 0 ) || ( adjust != 0 ))
    {
      pw.print( ',' );
      pw.print( lsbOffset );
    }
    if ( adjust != 0 )
    {
      pw.print( ',' );
      pw.print( adjust );
    }      
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorNode#canDelete()
   */
  public boolean canDelete(){ return true; }
  
  /**
   * Gets the bits.
   * 
   * @return the bits
   */
  public int getBits()
  {
    if ( bits == -1 )
      return (( HexParmEditorNode )getParent()).getBits();
    return bits;
  }

  /**
   * Sets the bits.
   * 
   * @param newBits the new bits
   */
  public void setBits( int newBits )
  {
    bits = newBits;
  }

  /**
   * Gets the bit order.
   * 
   * @return the bit order
   */
  public int getBitOrder(){ return order; }
  
  /**
   * Sets the bit order.
   * 
   * @param newOrder the new bit order
   */
  public void setBitOrder( int newOrder ){ order = newOrder; }

  /**
   * Gets the comp.
   * 
   * @return the comp
   */
  public boolean getComp(){ return comp; }
  
  /**
   * Sets the comp.
   * 
   * @param newComp the new comp
   */
  public void setComp( boolean newComp ){ comp = newComp; }

  /**
   * Gets the mSB offset.
   * 
   * @return the mSB offset
   */
  public int getMSBOffset(){ return msbOffset; }
  
  /**
   * Sets the mSB offset.
   * 
   * @param newOffset the new mSB offset
   */
  public void setMSBOffset( int newOffset ){ msbOffset = newOffset; }

  /**
   * Gets the lSB offset.
   * 
   * @return the lSB offset
   */
  public int getLSBOffset(){ return lsbOffset; }
  
  /**
   * Sets the lSB offset.
   * 
   * @param newOffset the new lSB offset
   */
  public void setLSBOffset( int newOffset ){ lsbOffset = newOffset; }

  /**
   * Gets the adjust.
   * 
   * @return the adjust
   */
  public int getAdjust(){ return adjust; }
  
  /**
   * Sets the adjust.
   * 
   * @param newAdjust the new adjust
   */
  public void setAdjust( int newAdjust ){ adjust = newAdjust; }

  /** The panel. */
  private static TranslatorEditorPanel panel = null;
  
  /** The bits. */
  private int bits = -1;
  
  /** The order. */
  private int order = MSB;
  
  /** The comp. */
  private boolean comp = false;
  
  /** The msb offset. */
  private int msbOffset = 0;
  
  /** The lsb offset. */
  private int lsbOffset = 0;
  
  /** The adjust. */
  private int adjust = 0;

  /** The Constant MSB. */
  public final static int MSB = 0;
  
  /** The Constant LSB. */
  public final static int LSB = 1;
}
