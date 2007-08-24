import java.io.*;

public class Setup
{
  public static String doubleSlashes( String str )
  {
    StringBuffer buff = new StringBuffer( str.length() * 2 );
    char[] chars = str.toCharArray();
    for ( int i = 0; i < chars.length; i++ )
    {
      char ch = chars[ i ];
      buff.append( ch );
      if ( ch == '\\' )
        buff.append( ch );
    }
    return buff.toString();
  }

  public static void main( String[] args )
  {
    try
    {
      String workDir = System.getProperty( "user.dir" );
      String jarFile = workDir + "\\RemoteMaster.jar";
      String icoFile = workDir + "\\images\\RemoteMaster.ico";
      String javaDir = System.getProperty( "java.home" );
      String javaw = javaDir + "\\bin\\javaw.exe";

      File regFile = new File( workDir, "Setup.reg" );
      PrintWriter pw = new PrintWriter( new FileWriter( regFile ));

      pw.println( "REGEDIT4" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMDeviceUpgrade]" );
      pw.println( "@=\"Remote Master Device Upgrade\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMDeviceUpgrade\\DefaultIcon]" );
      pw.println( "@=\"" + doubleSlashes( icoFile ) + "\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMDeviceUpgrade\\Shell]" );
      pw.println( "@=\"\"");
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMDeviceUpgrade\\Shell\\open]" );
      pw.println( "@=\"\"");
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMDeviceUpgrade\\Shell\\open\\command]" );
      pw.println( "@=\"\\\"" + doubleSlashes( javaw ) +
                  "\\\" -jar \\\"" + doubleSlashes( jarFile ) +
                  "\\\" -h \\\"" + doubleSlashes( workDir ) +
                  "\\\" \\\"%1\\\"\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\.km]" );
      pw.println( "@=\"RMDeviceUpgrade\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\.rmdu]" );
      pw.println( "@=\"RMDeviceUpgrade\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMRemoteConfig]" );
      pw.println( "@=\"Remote Master Remote Configuration\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMRemoteConfig\\DefaultIcon]" );
      pw.println( "@=\"" + doubleSlashes( icoFile ) + "\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMRemoteConfig\\Shell]" );
      pw.println( "@=\"\"");
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMRemoteConfig\\Shell\\open]" );
      pw.println( "@=\"\"");
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\RMRemoteConfig\\Shell\\open\\command]" );
      pw.println( "@=\"\\\"" + doubleSlashes( javaw ) +
                  "\\\" -jar \\\"" + doubleSlashes( jarFile ) +
                  "\\\" -h \\\"" + doubleSlashes( workDir ) +
                  "\\\" -ir \\\"%1\\\"\"" );
      pw.println();

      pw.println( "[HKEY_CLASSES_ROOT\\.rmir]" );
      pw.println( "@=\"RMRemoteConfig\"" );
      pw.println();

      pw.flush();
      pw.close();

      File batFile = new File( workDir, "rmaster.bat" );
      pw = new PrintWriter( new FileWriter( batFile ));

      pw.println( "@javaw -jar \"" + jarFile + "\" -h \"" + workDir + "\" %*" );

      pw.flush();
      pw.close();

      batFile = new File( workDir, "rmir.bat" );
      pw = new PrintWriter( new FileWriter( batFile ));

      pw.println( "@javaw -jar \"" + jarFile + "\" -h \"" + workDir + "\" -ir %*" );

      pw.flush();
      pw.close();
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
  }
}
