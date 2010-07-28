package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellEditor;

// TODO: Auto-generated Javadoc
/**
 * The Class HexCodeEditor.
 */
public class HexCodeEditor extends DefaultCellEditor implements TableCellEditor, ActionListener, UndoableEditListener,
    FocusListener
{

  /** The button. */
  private JButton button;

  /** The hex. */
  private Hex hex = new Hex();

  /** The dialog. */
  private JDialog dialog = null;

  /** The text area. */
  private JTextArea textArea = new JTextArea();

  /** The preferred width. */
  private int preferredWidth = 0;

  /**
   * Show dialog.
   * 
   * @param c
   *          the c
   * @param text
   *          the text
   */
  private void showDialog( Component c, String text )
  {
    Component root = SwingUtilities.getRoot( c );
    if ( root instanceof JFrame )
    {
      dialog = new JDialog( ( JFrame )root );
    }
    else if ( root instanceof JDialog )
    {
      dialog = new JDialog( ( JDialog )root );
    }
    preferredWidth = c.getWidth();
    dialog.setUndecorated( true );
    // setModal( true );
    dialog.getContentPane().add( new JScrollPane( textArea ), BorderLayout.CENTER );
    textArea.setLineWrap( true );
    textArea.setWrapStyleWord( true );
    textArea.setFont( c.getFont() );
    textArea.setText( text );
    textArea.getDocument().addUndoableEditListener( this );
    textArea.addFocusListener( this );
    new TextPopupMenu( textArea );
    Dimension size = textArea.getPreferredSize();
    size.width = preferredWidth;
    textArea.setPreferredSize( size );

    KeyStroke stroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
    dialog.getRootPane().registerKeyboardAction( this, "CANCEL", stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

    stroke = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK );
    textArea.registerKeyboardAction( this, "STOP", stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

    stroke = KeyStroke.getKeyStroke( KeyEvent.VK_TAB, 0 );
    textArea.registerKeyboardAction( this, "STOP", stroke, JComponent.WHEN_IN_FOCUSED_WINDOW );

    dialog.pack();
    dialog.setLocationRelativeTo( c );
    Point p = dialog.getLocation();
    int h = dialog.getHeight();
    int ch = c.getHeight();
    p.translate( 0, ( h - ch ) / 2 );
    dialog.setLocation( p );
    dialog.setVisible( true );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
   */
  public void undoableEditHappened( UndoableEditEvent e )
  {
    update();
  }

  /**
   * Update.
   */
  private void update()
  {
    textArea.setPreferredSize( null );
    Dimension d = textArea.getPreferredSize();
    if ( d.width < preferredWidth )
    {
      d.width = preferredWidth;
    }
    textArea.setPreferredSize( d );
    dialog.pack();
  }

  /**
   * Instantiates a new hex code editor.
   */
  public HexCodeEditor()
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( "EDIT" );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }

  /**
   * Handles events from the editor button and from the dialog's OK button.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed( ActionEvent e )
  {
    String command = e.getActionCommand();
    System.err.println( "command=" + command );
    if ( "EDIT".equals( command ) )
    {
      showDialog( button, hex.toString( 16 ) );
    }
    else if ( "CANCEL".equals( command ) )
    {
      cancelCellEditing();
    }
    else
    {
      stopCellEditing();
    }
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue()
  {
    return hex;
  }

  // Implement the one method defined by TableCellEditor.
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int,
   * int)
   */
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    hex = ( Hex )value;
    if ( hex == null )
    {
      hex = new Hex();
    }
    button.setText( hex.toString() );
    return button;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#stopCellEditing()
   */
  @Override
  public boolean stopCellEditing()
  {
    System.err.println( "stopCellEditing()" );
    try
    {
      hex = new Hex( textArea.getText() );
      dialog.setVisible( false );
      return super.stopCellEditing();
    }
    catch ( Exception e )
    {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#cancelCellEditing()
   */
  @Override
  public void cancelCellEditing()
  {
    System.err.println( "cancelCellEditing()" );
    dialog.setVisible( false );
    super.cancelCellEditing();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained( FocusEvent event )
  {}

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost( FocusEvent event )
  {
    Component c = event.getOppositeComponent();
    System.err.println( "focusLost to " + c.getClass().getName() );
    if ( c.getClass() != JRootPane.class )
    {
      stopCellEditing();
    }
  }

}
