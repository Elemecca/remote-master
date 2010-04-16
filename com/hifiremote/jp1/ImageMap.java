package com.hifiremote.jp1;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageMap.
 */
public class ImageMap
{

  /**
   * Instantiates a new image map.
   * 
   * @param mapFile
   *          the map file
   */
  public ImageMap( File mapFile )
  {
    this.mapFile = mapFile;
  }

  /**
   * Parses the.
   * 
   * @param remote
   *          the remote
   * @throws Exception
   *           the exception
   */
  public void parse( Remote remote ) throws Exception
  {
    BufferedReader in = new BufferedReader( new FileReader( mapFile ) );
    String line = in.readLine();

    if ( line.startsWith( "#$" ) )
    {
      // This MAP file is a NCSA map file, probably created by Map This!
      while ( ( line = in.readLine() ) != null )
      {
        if ( line.startsWith( "#$GIF:" ) )
        {
          imageFile = new File( mapFile.getParentFile(), line.substring( 6 ) );
        }
        else if ( !line.startsWith( "#" ) )
        {
          StringTokenizer st = new StringTokenizer( line, " ," );
          String type = st.nextToken();
          if ( type.equals( "default" ) )
            continue;
          String displayName = null;
          String keyCodeText = null;
          String buttonName = st.nextToken();
          // check if keycode is used
          int pos = buttonName.indexOf( ':' );
          if ( pos != -1 )
          {
            keyCodeText = buttonName.substring( 0, pos );
            buttonName = buttonName.substring( pos + 1 );
          }
          pos = buttonName.indexOf( '=' );
          if ( pos != -1 )
          {
            displayName = buttonName.substring( 0, pos );
            buttonName = buttonName.substring( pos + 1 );
          }
          Button button = null;
          // check if keycode is used
          if ( keyCodeText != null )
          {
            int keyCode;
            if ( keyCodeText.charAt( 0 ) == '$' )
              keyCode = Integer.parseInt( keyCodeText.substring( 1 ), 16 );
            else
              keyCode = Integer.parseInt( keyCodeText );
            button = remote.getButton( keyCode );
          }
          else
            button = remote.getButton( buttonName );
          Shape shape = null;
          if ( button == null )
          {
            System.err.println( "Warning: Shape defined for unknown button " + buttonName );
            continue;
          }
          if ( type.equals( "rect" ) )
          {
            double x = Double.parseDouble( st.nextToken() );
            double y = Double.parseDouble( st.nextToken() );
            double x2 = Double.parseDouble( st.nextToken() );
            double y2 = Double.parseDouble( st.nextToken() );
            double w = x2 - x;
            double h = y2 - y;
            shape = new Rectangle2D.Double( x, y, w, h );
          }
          else if ( type.equals( "circle" ) )
          {
            double x = Double.parseDouble( st.nextToken() );
            double y = Double.parseDouble( st.nextToken() );
            double x2 = Double.parseDouble( st.nextToken() );
            double y2 = Double.parseDouble( st.nextToken() );
            double w = x2 - x;
            double h = y2 - y;
            if ( w == 0 )
              w = h;
            x -= w;
            w += w;
            y -= h;
            h += h;
            shape = new Ellipse2D.Double( x, y, w, h );
          }
          else if ( type.equals( "poly" ) )
          {
            GeneralPath path = new GeneralPath( GeneralPath.WIND_EVEN_ODD, st.countTokens() / 2 );
            float x1 = Float.parseFloat( st.nextToken() );
            float y1 = Float.parseFloat( st.nextToken() );
            path.moveTo( x1, y1 );

            while ( st.hasMoreTokens() )
            {
              float x = Float.parseFloat( st.nextToken() );
              float y = Float.parseFloat( st.nextToken() );
              if ( ( x == x1 ) && ( y == y1 ) )
                break;
              path.lineTo( x, y );
            }
            path.closePath();
            shape = path;
          }
          ButtonShape buttonShape = new ButtonShape( shape, button );
          button.setHasShape( true );
          if ( displayName != null )
            buttonShape.setName( displayName );
          shapes.add( buttonShape );
        }
      }
    }
    in.close();
  }

  /**
   * Gets the image.
   * 
   * @return the image
   */
  public File getImageFile()
  {
    return imageFile;
  }

  /**
   * Gets the shapes.
   * 
   * @return the shapes
   */
  public java.util.List< ButtonShape > getShapes()
  {
    return shapes;
  }

  /** The map file. */
  private File mapFile;

  /** The image. */
  private File imageFile;

  /** The shapes. */
  private java.util.List< ButtonShape > shapes = new ArrayList< ButtonShape >();
}
