package com.hifiremote.jp1;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.*;

public class TextPopupMenu
  extends JPopupMenu
{
  TextPopupMenu( JTextComponent comp )
  {
    super();

    c = comp;

    c.setDragEnabled( true );

    Action[] actions = c.getActions();
    JMenuItem item = null;
    for ( int i = 0; i < actions.length; i++ )
    {
      String name = ( String )actions[ i ].getValue( javax.swing.Action.NAME );
      String text = null;
      boolean separator = false;
      if ( name.equals( "cut-to-clipboard" ))
        text = "Cut";
      else if ( name.equals( "copy-to-clipboard" ))
        text = "Copy";
      else if ( name.equals( "paste-from-clipboard" ))
        text = "Paste";
      else if ( name.equals( "select-all" ))
      {
        text = "Select All";
        separator = true;
      }

      if ( text != null )
      {
        if ( separator )
          addSeparator();
        item = new JMenuItem( actions[ i ]);
        item.setText( text );
        add( item );

        if ( name.equals( "cut-to-clipboard" ))
          cutItem = item;
        else if ( name.equals( "copy-to-clipboard" ))
          copyItem = item;
        else if ( name.equals( "paste-from-clipboard" ))
          pasteItem = item;
        else if ( name.equals( "select-all" ))
          selectItem = item;
      }
    }

    MouseAdapter ml = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        maybeShowPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        maybeShowPopup( e );
      }

      private void maybeShowPopup( MouseEvent e ) 
      {
        if ( e.isPopupTrigger()) 
        {
          c.requestFocusInWindow();
          boolean flag = ( c.getSelectedText() != null );
          cutItem.setEnabled( flag );
          copyItem.setEnabled( flag );

          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable clipData = clipboard.getContents( clipboard );
         
          pasteItem.setEnabled(( clipData != null ) && 
                                c.getTransferHandler().canImport( c, clipData.getTransferDataFlavors())); 
 
          Document doc = c.getDocument();
          selectItem.setEnabled(( doc != null ) && ( doc.getLength() != 0 ));
          show( e.getComponent(), e.getX(), e.getY());
        }
      }
    };
    c.addMouseListener( ml );
  }
  
  private JTextComponent c = null;
  private JMenuItem copyItem = null;
  private JMenuItem cutItem = null;
  private JMenuItem pasteItem = null;
  private JMenuItem selectItem = null;
}
