package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class PreferredRemoteDialog
  extends JDialog
  implements ActionListener, ListSelectionListener
{
  public PreferredRemoteDialog( JFrame owner, Remote[] preferredRemotes )
  {
    super( owner, "Preferred Remotes", true );
    setLocationRelativeTo( owner );

    Remote[] remotes = RemoteManager.getRemoteManager().getRemotes();    

    unusedListModel = new DefaultListModel();
    int j = 0;
    int index = 0;
    for ( int i = 0; i < preferredRemotes.length; i++ )
    {
      Remote preferred = preferredRemotes[ i ];
      while( j < remotes.length )
      {
        Remote r = remotes[ j++ ];
        if ( r != preferred )
          unusedListModel.addElement( r );
        else
          break;
      }
    }
    while ( j < remotes.length )
      unusedListModel.addElement( remotes[ j++ ]);

    preferredListModel = new DefaultListModel();
    for ( int i = 0; i < preferredRemotes.length; i++ )
      preferredListModel.addElement( preferredRemotes[ i ]);      

    unusedList = new JList( unusedListModel );
    unusedList.setVisibleRowCount( 20 );
    unusedList.addListSelectionListener( this );
    preferredList = new JList( preferredListModel );
    preferredList.addListSelectionListener( this );

    Container contentPane = getContentPane();

    JLabel instructions = new JLabel( "Move remotes from the \"Available Remotes\" list to the \"Preferred Remotes\" list." );
    contentPane.add( instructions, BorderLayout.NORTH );

    Box outerBox = Box.createHorizontalBox();
    contentPane.add( outerBox, BorderLayout.CENTER );
    
    outerBox.add( Box.createHorizontalStrut( 10 ));

    Box box = Box.createVerticalBox();
    box.add( new JLabel( "Available Remotes" ));
    box.add( new JScrollPane( unusedList ));
    outerBox.add( box );
    outerBox.add( Box.createHorizontalStrut( 10 ));
    
    box = Box.createVerticalBox();
    add = new JButton( " -->> " );
    add.setEnabled( false );
    add.addActionListener( this );
    box.add( add );

    box.add( Box.createVerticalStrut( 10 ));

    remove = new JButton( " <<-- " );
    remove.setEnabled( false );
    remove.addActionListener( this );
    box.add( remove );

    outerBox.add( box );
    
    outerBox.add( Box.createHorizontalStrut( 10 ));

    box = Box.createVerticalBox();
    box.add( new JLabel( "Preferred Remotes" ));
    box.add( new JScrollPane( preferredList ));
    outerBox.add( box );
    outerBox.add( Box.createHorizontalStrut( 10 ));

    JPanel buttonPanel = new JPanel();
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

  public Remote[] getPreferredRemotes()
  {
    Remote[] remotes = new Remote[ preferredListModel.size()];
    int i = 0;
    for ( Enumeration e = preferredListModel.elements(); e.hasMoreElements(); )
      remotes[ i++ ] = ( Remote )e.nextElement(); 
    return remotes;
  }

  public int getUserAction()
  {
    return userAction;
  }

  private void transfer( JList fromList, JList toList )
  {
    DefaultListModel fromModel = ( DefaultListModel )fromList.getModel();
    int fromIndex = fromList.getMaxSelectionIndex();
    int first = fromList.getMinSelectionIndex();
    DefaultListModel toModel = ( DefaultListModel )toList.getModel();
    int toIndex = toModel.getSize();
    
    while ( fromIndex >= first )
    {
      if ( fromList.isSelectedIndex( fromIndex ))
      {
        Remote r = ( Remote )fromModel.getElementAt( fromIndex );
        fromModel.removeElementAt( fromIndex );

        while ( toIndex > 0 )
        {
          Remote r2 = ( Remote )toModel.elementAt( toIndex - 1 );
          int rc = r2.getName().compareTo( r.getName());
          
          if ( rc < 0 )
            break;
          --toIndex;
        }
        toModel.add( toIndex, r );
      }
      --fromIndex;
    }
  }

  public void valueChanged( ListSelectionEvent e )
  {
    Object source = e.getSource();
    if ( source == unusedList )
      add.setEnabled( !unusedList.isSelectionEmpty());
    else
      remove.setEnabled( !preferredList.isSelectionEmpty());
  }

  private DefaultListModel unusedListModel = null;
  private DefaultListModel preferredListModel = null;
  private JList unusedList = null;
  private JList preferredList = null;
  private JButton add = null;
  private JButton remove = null;
  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
