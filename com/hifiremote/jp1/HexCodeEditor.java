package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class HexCodeEditor
  extends DefaultCellEditor
  implements TableCellEditor, ActionListener, UndoableEditListener, FocusListener
{
  private JButton button;
  private Hex hex = new Hex();
  private JDialog dialog = null;
  private JTextArea textArea = new JTextArea();
  private TextPopupMenu popup = null;
  private int preferredWidth = 0;

  private void showDialog( Component c, String text )
  {
    Component root = SwingUtilities.getRoot( c );
    if ( root instanceof JFrame )
      dialog = new JDialog(( JFrame )root );
    else if ( root instanceof JDialog )
      dialog = new JDialog(( JDialog )root );
    preferredWidth = c.getWidth();
    dialog.setUndecorated( true );
    // setModal( true );
    dialog.getContentPane().add( new JScrollPane( textArea ), BorderLayout.CENTER );
    textArea.setLineWrap( true );
    textArea.setWrapStyleWord( true );
    textArea.setFont( c.getFont());
    textArea.setText( text );
    textArea.getDocument().addUndoableEditListener( this );
    textArea.addFocusListener( this );
    popup = new TextPopupMenu( textArea );
    Dimension size = textArea.getPreferredSize();
    size.width = preferredWidth;
    textArea.setPreferredSize( size );

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
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

  public void undoableEditHappened( UndoableEditEvent e )
  {
    update();
  }

  private void update()
  {
    textArea.setPreferredSize( null );
    Dimension d = textArea.getPreferredSize();
    if ( d.width < preferredWidth )
      d.width = preferredWidth;
    textArea.setPreferredSize( d );
    dialog.pack();
  }

  public HexCodeEditor()
  {
    super( new JTextField());
    setClickCountToStart( 2 );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ));
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( "EDIT" );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }

  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  public void actionPerformed( ActionEvent e )
  {
    String command = e.getActionCommand();
    System.err.println( "command=" + command );
    if ( "EDIT".equals( command ))
      showDialog( button, hex.toString( 16 ));
    else if ( "CANCEL".equals( command ))
      cancelCellEditing();
    else // if ( "STOP".equals( command ))
      stopCellEditing();
  }

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue()
  {
    return hex;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    hex = ( Hex )value;
    if ( hex == null )
      hex = new Hex();
    button.setText( hex.toString());
    return button;
  }

  public boolean stopCellEditing()
  {
    System.err.println( "stopCellEditing()" );
    try
    {
      hex = new Hex( textArea.getText());
      dialog.setVisible( false );
      return super.stopCellEditing();
    }
    catch ( Exception e )
    {
      return false;
    }
  }

  public void cancelCellEditing()
  {
    System.err.println( "cancelCellEditing()" );
    dialog.setVisible( false );
    super.cancelCellEditing();
  }
  
  public void focusGained( FocusEvent event ){}

  public void focusLost( FocusEvent event )
  {
    Component c = event.getOppositeComponent();
    System.err.println( "focusLost to " + c.getClass().getName());
    if ( c.getClass() != JRootPane.class )
      stopCellEditing();
  }

}
