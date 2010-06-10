import java.io.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Setup.
 */
public class Setup
{
  
  /**
   * Double slashes.
   * 
   * @param str the str
   * 
   * @return the string
   */
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

  /**
   * The main method.
   * 
   * @param args the arguments
   */
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

      File vbsFile = new File( workDir, "Setup.vbs" );
      pw = new PrintWriter( new FileWriter( vbsFile ) );

      pw.println( "Set WshShell = WScript.CreateObject(\"WScript.Shell\")" );
      pw.println( "sUserProfile = WshShell.Environment(\"PROCESS\").Item(\"USERPROFILE\")" );
      pw.println();
      pw.println( "Set objFS = WScript.CreateObject(\"Scripting.FileSystemObject\")" );
      pw.println( "sRMFolder = sUserProfile & \"\\Start Menu\\Programs\\Remote Master\"" );
      pw.println( "if Not objFS.FolderExists( sRMFolder ) Then" );
      pw.println( "   objFS.CreateFolder( sRMFolder )" );
      pw.println( "End If" );
      pw.println();
      pw.println( "sRMIcon = sRMFolder & \"\\Remote Master.LNK\"" );
      pw.println();
      pw.println( "Set oWS = WScript.CreateObject(\"WScript.Shell\")" );
      pw.println( "Set oLink = oWS.CreateShortcut(sRMIcon)" );
      pw.println();
      pw.println( "oLink.TargetPath = \"" + workDir + "\\RemoteMaster.jar\"" );
      pw.println( "oLink.Arguments = \"\"" );
      pw.println( "oLink.Description = \"RemoteMaster\"" );
      pw.println( "oLink.IconLocation = \"" + workDir + "\\RM.ICO\"" );
      pw.println( "oLink.WindowStyle = \"1\"" );
      pw.println( "oLink.WorkingDirectory = \"" + workDir + "\"" );
      pw.println( "oLink.Save" );
      pw.println();
      pw.println( "sRMIRIcon = sRMFolder & \"\\RMIR.LNK\"" );
      pw.println( "Set oLink = oWS.CreateShortcut(sRMIRIcon)" );
      pw.println();
      pw.println( "oLink.TargetPath = \"" + workDir + "\\RemoteMaster.jar\"" );
      pw.println( "oLink.Arguments = \"-ir\"" );
      pw.println( "oLink.Description = \"RMIR\"" );
      pw.println( "oLink.IconLocation = \"" + workDir + "\\RMIR.ICO\"" );
      pw.println( "oLink.WindowStyle = \"1\"" );
      pw.println( "oLink.WorkingDirectory = \"" + workDir + "\"" );
      pw.println( "oLink.Save" );
      pw.println();
      pw.println( "sReadmeIcon = sRMFolder & \"\\Read Me.LNK\"" );
      pw.println( "Set oLink = oWS.CreateShortcut(sReadmeIcon)" );
      pw.println();
      pw.println( "oLink.TargetPath = \"" + workDir + "\\Readme.html\"" );
      pw.println( "oLink.Arguments = \"\"" );
      pw.println( "oLink.Description = \"Readme\"" );
      pw.println( "oLink.WindowStyle = \"1\"" );
      pw.println( "oLink.WorkingDirectory = \"" + workDir + "\"" );
      pw.println( "oLink.Save" );
      pw.println();
      pw.println( "sTutorialIcon = sRMFolder & \"\\Tutorial.LNK\"" );
      pw.println( "Set oLink = oWS.CreateShortcut(sTutorialIcon)" );
      pw.println();
      pw.println( "oLink.TargetPath = \"" + workDir + "\\tutorial\\tutorial.html\"" );
      pw.println( "oLink.Arguments = \"\"" );
      pw.println( "oLink.Description = \"Tutorial\"" );
      pw.println( "oLink.WindowStyle = \"1\"" );
      pw.println( "oLink.WorkingDirectory = \"" + workDir + "\\tutorial\"" );
      pw.println( "oLink.Save" );

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
