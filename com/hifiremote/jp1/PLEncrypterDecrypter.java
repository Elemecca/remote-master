package com.hifiremote.jp1;

public class PLEncrypterDecrypter
  extends EncrypterDecrypter
{
  public int encrypt( int val )
  {
    val &= 0xFF;

    if ( val == 0x7D )  // Special case for which the formula doesn't work.
      return 0x4E;

    int val1 = ( val >> 2 | val << 6 ) + 111;
    val1 &= 0x00FF;

    int val2 = ( val & 0x80 ) & ( val << 7 );
    int rc = ( val1 ^ val2 );
    return rc;
  }

  public int decrypt( int val )
  {
    if ( val == 0x4E )  // Special case for which the formula doesn't work.
      return 0x7D;

    int val1 = ( val + 145 ) & 0x00FF;
    int mask = ( val1 << 1 ) & ( val1 << 2 ) & 0x80; 
    int val2 = val1 ^ mask;
    return ( val2 << 2 | val2 >> 6 ) & 0xFF;
  }
}
