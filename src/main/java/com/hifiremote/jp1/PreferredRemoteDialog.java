package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// TODO: Auto-generated Javadoc
/**
 * The Class PreferredRemoteDialog.
 */
public class PreferredRemoteDialog extends JDialog implements ActionListener, ListSelectionListener
{

  /**
   * Instantiates a new preferred remote dialog.
   * 
   * @param owner
   *          the owner
   * @param preferredRemotes
   *          the preferred remotes
   */
  public PreferredRemoteDialog( JFrame owner, Collection< Remote > preferredRemotes )
  {
    super( owner, "Preferred Remotes", true );
    createGui( owner, preferredRemotes );
  }

  /**
   * Instantiates a new preferred remote dialog.
   * 
   * @param owner
   *          the owner
   * @param preferredRemotes
   *          the preferred remotes
   */
  public PreferredRemoteDialog( JDialog owner, Collection< Remote > preferredRemotes )
  {
    super( owner, "Preferred Remotes", true );
    createGui( owner, preferredRemotes );
  }

  /**
   * Creates the gui.
   * 
   * @param owner
   *          the owner
   * @param preferredRemotes
   *          the preferred remotes
   */
  private void createGui( Component owner, Collection< Remote > preferredRemotes )
  {
    if ( owner != null )
      setLocationRelativeTo( owner );

    Collection< Remote > remotes = RemoteManager.getRemoteManager().getRemotes();

    unusedListModel = new DefaultListModel();
    Iterator< Remote > iPref = preferredRemotes.iterator();
    Iterator< Remote > iAll = remotes.iterator();

    while ( iPref.hasNext() )
    {
      Remote preferred = iPref.next();
      while ( iAll.hasNext() )
      {
        Remote r = iAll.next();
        if ( r != preferred )
          unusedListModel.addElement( r );
        else
          break;
      }
    }
    while ( iAll.hasNext() )
      unusedListModel.addElement( iAll.next() );

    preferredListModel = new DefaultListModel();
    for ( Remote r : preferredRemotes )
      preferredListModel.addElement( r );

    unusedList = new JList( unusedListModel );
    unusedList.setVisibleRowCount( 20 );
    unusedList.addListSelectionListener( this );
    preferredList = new JList( preferredListModel );
    preferredList.addListSelectionListener( this );

    Container contentPane = getContentPane();

    JLabel instructions = new JLabel(
        "Move remotes from the \"Available Remotes\" list to the \"Preferred Remotes\" list." );
    instructions.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    contentPane.add( instructions, BorderLayout.NORTH );

    Box outerBox = Box.createHorizontalBox();
    contentPane.add( outerBox, BorderLayout.CENTER );

    outerBox.add( Box.createHorizontalStrut( 10 ) );

    Box box = Box.createVerticalBox();
    box.add( new JLabel( "Available Remotes" ) );
    box.add( new JScrollPane( unusedList ) );
    outerBox.add( box );
    outerBox.add( Box.createHorizontalStrut( 10 ) );

    box = Box.createVerticalBox();
    add = new JButton( " -->> " );
    add.setEnabled( false );
    add.addActionListener( this );
    box.add( add );

    box.add( Box.createVerticalStrut( 10 ) );

    remove = new JButton( " <<-- " );
    remove.setEnabled( false );
    remove.addActionListener( this );
    box.add( remove );

    outerBox.add( box );

    outerBox.add( Box.createHorizontalStrut( 10 ) );

    box = Box.createVerticalBox();
    box.add( new JLabel( "Preferred Remotes" ) );
    box.add( new JScrollPane( preferredList ) );
    outerBox.add( box );
    outerBox.add( Box.createHorizontalStrut( 10 ) );

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    FlowLayout fl = ( FlowLayout )buttonPanel.getLayout();
    fl.setAlignment( FlowLayout.RIGHT );

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    contentPane.add( buttonPanel, BorderLayout.SOUTH );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == cancel )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == ok )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == add )
    {
      transfer( unusedList, preferredList );
    }
    else if ( source == remove )
    {
      transfer( preferredList, unusedList );
    }
  }

  /**
   * Gets the preferred remotes.
   * 
   * @return the preferred remotes
   */
  public Collection< Remote > getPreferredRemotes()
  {
    ArrayList< Remote > remotes = new ArrayList< Remote >( preferredListModel.size() );
    for ( Enumeration< ? > e = preferredListModel.elements(); e.hasMoreElements(); )
      remotes.add( ( Remote )e.nextElement() );
    return remotes;
  }

  /**
   * Gets the user action.
   * 
   * @return the user action
   */
  public int getUserAction()
  {
    return userAction;
  }

  /**
   * Transfer.
   * 
   * @param fromList
   *          the from list
   * @param toList
   *          the to list
   */
  private void transfer( JList fromList, JList toList )
  {
    DefaultListModel fromModel = ( DefaultListModel )fromList.getModel();
    int fromIndex = fromList.getMaxSelectionIndex();
    int first = fromList.getMinSelectionIndex();
    DefaultListModel toModel = ( DefaultListModel )toList.getModel();
    int toIndex = toModel.getSize();

    while ( fromIndex >= first )
    {
      if ( fromList.isSelectedIndex( fromIndex ) )
      {
        Remote r = ( Remote )fromModel.getElementAt( fromIndex );
        fromModel.removeElementAt( fromIndex );

        while ( toIndex > 0 )
        {
          Remote r2 = ( Remote )toModel.elementAt( toIndex - 1 );
          int rc = r2.getName().compareTo( r.getName() );

          if ( rc < 0 )
            break;
          --toIndex;
        }
        toModel.add( toIndex, r );
      }
      --fromIndex;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged( ListSelectionEvent e )
  {
    Object source = e.getSource();
    if ( source == unusedList )
      add.setEnabled( !unusedList.isSelectionEmpty() );
    else
      remove.setEnabled( !preferredList.isSelectionEmpty() );
  }

  /** The unused list model. */
  private DefaultListModel unusedListModel = null;

  /** The preferred list model. */
  private DefaultListModel preferredListModel = null;

  /** The unused list. */
  private JList unusedList = null;

  /** The preferred list. */
  private JList preferredList = null;

  /** The add. */
  private JButton add = null;

  /** The remove. */
  private JButton remove = null;

  /** The ok. */
  private JButton ok = null;

  /** The cancel. */
  private JButton cancel = null;

  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
}
