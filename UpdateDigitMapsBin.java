import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

public class UpdateDigitMapsBin
{
  public static void main( String[] args )
  {
    try
    {
      File folder = new File( System.getProperty( "user.dir" ) );

      FilenameFilter filter = new FilenameFilter()
      {
        public boolean accept( File folder, String name )
        {
          return name.startsWith( "num-master" ) && name.endsWith( ".txt" );
        }
      };
      String[] names = folder.list(filter);
      if ( names.length == 0 )
        return;
      Arrays.sort( names );
      String name = names[ names.length - 1];
      System.out.println( "name=" + name );
      File file = new File( folder, name );
      BufferedReader rdr = new BufferedReader( new FileReader( file ) );
      BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( new File( folder,
          "digitmaps.bin" ) ) );
      String line = null;
      while ( ( line = rdr.readLine() ) != null )
      {
        StringTokenizer st = new StringTokenizer( line );
        while ( st.hasMoreTokens() )
        {
          int hex = Integer.parseInt( st.nextToken(), 16 ) & 0xFF;
          out.write( hex );
        }
      }
      out.close();
      rdr.close();

      for ( String aName : names )
      {
        File aFile = new File( folder, aName );
        aFile.delete();
      }
    }
    catch ( IOException ioe )
    {
      ioe.printStackTrace( System.err );
    }
  }
}
