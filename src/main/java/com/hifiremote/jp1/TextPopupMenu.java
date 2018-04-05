package com.hifiremote.jp1;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class TextPopupMenu.
 */
public class TextPopupMenu
  extends JPopupMenu
{
  
  /**
   * Instantiates a new text popup menu.
   * 
   * @param comp the comp
   */
  TextPopupMenu( JTextComponent comp )
  {
    super();

    c = comp;

    c.setDragEnabled( true );

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
          if ( cutItem == null )
            initMenuItems();
          boolean flag = ( c.getSelectedText() != null );
          cutItem.setEnabled( flag && c.isEditable());
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

  /**
   * Inits the menu items.
   */
  private void initMenuItems()
  {
    Action[] actions = c.getActions();
    Hashtable< String, Action > h = new Hashtable< String, Action >();
    for ( int i = 0; i < actions.length; i++ )
    {
      String name = ( String )actions[ i ].getValue( javax.swing.Action.NAME );
      if ( name.equals( "cut-to-clipboard" ) || name.equals( "copy-to-clipboard" ) ||
           name.equals( "paste-from-clipboard" ) || name.equals( "select-all" ))
      {
        h.put( name, actions[ i ]);
      }
    }

    cutItem = addItem(( Action )h.get( "cut-to-clipboard" ), "Cut" );
    copyItem = addItem(( Action )h.get( "copy-to-clipboard" ), "Copy" );
    pasteItem = addItem(( Action )h.get( "paste-from-clipboard" ), "Paste" );
    addSeparator();
    selectItem = addItem(( Action )h.get( "select-all" ), "Select All" );    
  }

  /**
   * Adds the item.
   * 
   * @param a the a
   * @param text the text
   * 
   * @return the j menu item
   */
  private JMenuItem addItem( Action a, String text )
  {
    JMenuItem item = new JMenuItem( a );
    item.setText( text );
    add( item );
    return item;
  }
  
  /** The c. */
  private JTextComponent c = null;
  
  /** The copy item. */
  private JMenuItem copyItem = null;
  
  /** The cut item. */
  private JMenuItem cutItem = null;
  
  /** The paste item. */
  private JMenuItem pasteItem = null;
  
  /** The select item. */
  private JMenuItem selectItem = null;
}
