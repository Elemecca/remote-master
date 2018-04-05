package com.hifiremote.jp1;

import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class EncrypterDecrypter.
 */
public abstract class EncrypterDecrypter
{
  public static EncrypterDecrypter createInstance( String text )
  {
    EncrypterDecrypter encDec = null;
    StringTokenizer st = new StringTokenizer( text, "=()" );
    String className = st.nextToken();
    String textParm = null;
    if ( st.hasMoreTokens() )
      textParm = st.nextToken();
    try
    {
      if ( className.indexOf( '.' ) == -1 )
        className = "com.hifiremote.jp1." + className;

      Class< ? > cl = Class.forName( className );
      Class< ? extends EncrypterDecrypter > cl2 = cl.asSubclass( EncrypterDecrypter.class );
      Class< ? >[] parmClasses =
      { String.class };
      Constructor< ? extends EncrypterDecrypter > ct = cl2.getConstructor( parmClasses );
      Object[] ctParms =
      { textParm };
      encDec = ct.newInstance( ctParms );
    }
    catch ( Exception e )
    {
      System.err.println( "Error creating an instance of " + className );
      e.printStackTrace( System.err );
    }
    return encDec;
  }

  /**
   * Encrypt.
   * 
   * @param val
   *          the val
   * 
   * @return the short
   */
  public abstract short encrypt( short val );

  /**
   * Decrypt.
   * 
   * @param val
   *          the val
   * 
   * @return the short
   */
  public abstract short decrypt( short val );
}
