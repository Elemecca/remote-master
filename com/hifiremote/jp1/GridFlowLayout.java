package com.hifiremote.jp1;

import java.awt.*;

public class GridFlowLayout implements LayoutManager
{
    public void addLayoutComponent( String name, Component comp ){}

    public void removeLayoutComponent( Component comp ){}

    public Dimension preferredLayoutSize( Container target )
    {
      Insets insets = target.getInsets();
      int width = target.getWidth() - (insets.left + insets.right);
      int nmembers = target.getComponentCount();
      int compWidth = 1;
      int rowHeight = 0;
      
      for ( int i = 0; i < nmembers; i++ )
      {
         Component m = target.getComponent( i );
         compWidth = Math.max( compWidth, m.getPreferredSize().width );
         if ( rowHeight == 0 );
           rowHeight = m.getPreferredSize().height;
      }

      int cols = width / compWidth;
      if ( cols == 0 ) cols = 1;
      int rows = ( nmembers + cols - 1 ) / cols;

      Dimension d = new Dimension( target.getWidth(),
                                   ( rows * rowHeight ) + insets.top + insets.bottom );
      return d;
    }

    public Dimension minimumLayoutSize( Container target ) 
    {
      Dimension dim = new Dimension(0, 0);
      int nmembers = target.getComponentCount();

      for ( int i = 0 ; i < nmembers ; i++ ) 
      {
        Component m = target.getComponent(i);
        if ( m.isVisible())
        {
          Dimension d = m.getMinimumSize();
          dim.height +=  d.height;
          dim.width = Math.max( dim.width, d.width );
        }
      }
      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
      return dim;
    }

    private void moveComponents( Container target, int x, int y, int width, int height,
                                int rowStart, int rowEnd, boolean ltr) 
    {
      for (int i = rowStart ; i < rowEnd ; i++) 
      {
        Component m = target.getComponent(i);
        if ( m.isVisible()) 
        {
          if (ltr) 
          {
            m.setLocation( x, y + ( height - m.getHeight()) / 2 );
          }
          else
          {
            m.setLocation( target.getWidth() - x - m.getWidth(), y + ( height - m.getHeight()) / 2 );
          }
          x += m.getWidth();
        }
      }
    }

    public void layoutContainer( Container target ) 
    {
      Insets insets = target.getInsets();
      int maxwidth = target.getWidth() - (insets.left + insets.right);
      int nmembers = target.getComponentCount();
      int x = 0, y = insets.top;
      int rowh = 0, start = 0;

      boolean ltr = target.getComponentOrientation().isLeftToRight();

      int forcedWidth = 0;
      for ( int i = 0; i < nmembers; i++ )
      {
         Component m = target.getComponent( i );
         if ( m.isVisible())
         {
           forcedWidth = Math.max( forcedWidth, m.getPreferredSize().width );
         }
      }

      if ( forcedWidth > 0 )
      {
        int compsOnRow = maxwidth / forcedWidth;
        forcedWidth = maxwidth / compsOnRow;
      }

      for ( int i = 0 ; i < nmembers ; i++ ) 
      {
          Component m = target.getComponent( i );
          if ( m.isVisible()) 
          {
              Dimension d = m.getPreferredSize();
              m.setSize( forcedWidth, d.height );

              if (( x == 0) || (( x + forcedWidth ) <= maxwidth ))
              {
                  x += forcedWidth;
                  rowh = Math.max(rowh, d.height);
              }
              else
              {
                  moveComponents(target, insets.left, y, maxwidth - x, rowh, start, i, ltr);
                  x = forcedWidth;
                  y += rowh;
                  rowh = d.height;
                  start = i;
              }
          }
      }
      moveComponents( target, insets.left, y, maxwidth - x, rowh, start, nmembers, ltr);
    }
}
