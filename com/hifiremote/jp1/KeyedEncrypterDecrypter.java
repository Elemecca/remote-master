package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyedEncrypterDecrypter.
 */
public class KeyedEncrypterDecrypter
  extends EncrypterDecrypter
{
  
  /**
   * Instantiates a new keyed encrypter decrypter.
   * 
   * @param textParms the text parms
   */
  public KeyedEncrypterDecrypter( String textParms )
  {
    key = Integer.parseInt( textParms );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EncrypterDecrypter#encrypt(short)
   */
  public short encrypt( short val )
  {
    val &= 0xFF;

    if ( val == 0x7D )  // Special cases for which the formula doesn't work.
      val = 0x7F;
    else if ( val == 0x7F )
      val = 0x7D;

    int val1 = ( val >> 2 | val << 6 ) + key;
    val1 &= 0x00FF;

    int val2 = ( val & 0x80 ) & ( val << 7 );
    int rc = ( val1 ^ val2 );
    return ( short )rc;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EncrypterDecrypter#decrypt(short)
   */
  public short decrypt( short val )
  {
    int val1 = ( val + 256 - key ) & 0x00FF;
    int mask = ( val1 << 1 ) & ( val1 << 2 ) & 0x80; 
    int val2 = val1 ^ mask;
    short rc = ( short )(( val2 << 2 | val2 >> 6 ) & 0xFF );
    
    if ( rc == 0x7D )  // Special cases for which the formula doesn't work.
      return 0x7F;
    if ( rc == 0x7F )
      return 0x7D;

    return rc;
  }
  
  /** The key. */
  private int key = 0;
}
