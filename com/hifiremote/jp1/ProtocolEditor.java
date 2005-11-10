package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;

public class ProtocolEditor
  extends JDialog
  implements ActionListener,
             PropertyChangeListener,
             DocumentListener,
             TreeSelectionListener
{
  public static void main( String[] args )
  {
    try
    {
      System.setErr( new PrintStream( new FileOutputStream( new File ( "pedit.err" ))));
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    JFrame frame = new JFrame( "Test" );
    ProtocolEditor e = new ProtocolEditor( frame );
    e.show();
    System.exit( 0 );
  }

  public ProtocolEditor( JFrame owner )
  {
    super( owner, "Protocol Editor", true );
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();

    root = new DefaultMutableTreeNode( "Root", true );
    generalNode = new GeneralEditorNode();
    generalNode.addPropertyChangeListener( "Code", this );
    root.add( generalNode );
    
    fixedDataNode = new FixedDataEditorNode( 0 );
    root.add( fixedDataNode );

    cmdDataNode = new CmdEditorNode( 0 );
    root.add( cmdDataNode );

    treeModel = new DefaultTreeModel( root, true );
    tree = new JTree( treeModel );
    tree.setRootVisible( false );
    tree.setShowsRootHandles( true );
    tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
    tree.expandRow( 1 );
    tree.collapseRow( 1 );
    tree.addTreeSelectionListener( this );

    JScrollPane scrollPane = new JScrollPane( tree );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ),
                                                              scrollPane.getBorder()));
    contentPane.add( scrollPane, BorderLayout.WEST );

    cardLayout = new CardLayout( 5, 5 );
    cardPanel = new JPanel( cardLayout );
    for ( Enumeration e = root.children(); e.hasMoreElements(); )
    {
      ProtocolEditorNode node = ( ProtocolEditorNode )e.nextElement();
      ProtocolEditorPanel panel = node.getEditingPanel();
      cardPanel.add( panel, panel.getTitle());
    }

    ProtocolEditorNode node = new DevParmEditorNode();
    node.addPropertyChangeListener( "Name", this );
    ProtocolEditorPanel panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle());
    node = ( ProtocolEditorNode )node.getFirstChild();
    panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle());

    node = new CmdParmEditorNode();
    panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle());

    contentPane.add( cardPanel, BorderLayout.CENTER );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING, 5, 5 ));

    viewButton = new JButton( "View" );
    viewButton.setToolTipText( "View the protocols.ini entry for this protocol." );
    viewButton.addActionListener( this );
    buttonPanel.add( viewButton );

    addButton = new JButton( "Add" );
    addButton.addActionListener( this );
    addButton.setEnabled( false );
    buttonPanel.add( addButton );

    deleteButton = new JButton( "Delete" );
    deleteButton.addActionListener( this );
    deleteButton.setEnabled( false );
    buttonPanel.add( deleteButton );

    okButton = new JButton( "OK" );
    okButton.addActionListener( this );
    buttonPanel.add( okButton );

    contentPane.add( buttonPanel, BorderLayout.SOUTH );

    tree.addSelectionRow( 0 );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  // ActionListener methods
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == addButton )
    {
      int children = selectedNode.getChildCount();
      ProtocolEditorNode newNode = selectedNode.createChild();
      newNode.addPropertyChangeListener( "Name", this );
      treeModel.insertNodeInto( newNode, selectedNode, children );
      ProtocolEditorPanel newPanel = newNode.getEditingPanel();
      cardPanel.add( newPanel, newPanel.getTitle());
      for ( Enumeration enum = newNode.children(); enum.hasMoreElements(); )
      {
        ProtocolEditorNode child = ( ProtocolEditorNode )enum.nextElement();
        TreePath path = new TreePath( child.getPath());
        tree.expandPath( path );
        tree.scrollPathToVisible( path );
//        newPanel = child.getEditingPanel();
//        cardPanel.add( newPanel, newPanel.getTitle());
      }
      TreePath newPath = new TreePath( newNode.getPath());
      tree.setSelectionPath( newPath );
    }
    else if ( source == viewButton )
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      for ( Enumeration children = root.children(); children.hasMoreElements(); )
      {
        ProtocolEditorNode node = ( ProtocolEditorNode )children.nextElement();
        node.print( pw );
      }
      pw.flush();
      JTextArea ta = new JTextArea( sw.toString());
      new TextPopupMenu( ta );
      ta.setEditable( false );
      ta.setColumns( 80 );
      JOptionPane.showMessageDialog( this, new JScrollPane( ta ), "Protocol.ini entry text", JOptionPane.PLAIN_MESSAGE );
    }
    else if ( source == deleteButton )
    {
      DefaultMutableTreeNode nodeToSelect = selectedNode.getNextSibling();
      if ( nodeToSelect == null )
        nodeToSelect = selectedNode.getPreviousSibling();
      if ( nodeToSelect == null )
        nodeToSelect = ( DefaultMutableTreeNode )selectedNode.getParent();

      treeModel.removeNodeFromParent( selectedNode );
      selectedNode.removePropertyChangeListener( "Name", this );
      tree.getSelectionModel().setSelectionPath( new TreePath( nodeToSelect.getPath()));
    }
    else if ( source == okButton )
    {
      setVisible( false );
      dispose();
    }
  }

  // PropertyChangeListener methods
  public void propertyChange( PropertyChangeEvent e )
  {
    ProtocolEditorNode node = ( ProtocolEditorNode )e.getSource();
    String propertyName = e.getPropertyName();
    System.err.println( "PropertyChange for " + propertyName );
    if ( propertyName.equals( "Hex" ))
    {
      enableButtons();
    }
    else if ( propertyName.equals( "Name" ))
    {
      treeModel.nodeChanged( node );
      TreePath path = new TreePath( node.getPath());
      tree.collapsePath( path );
      tree.expandPath( path );
    }
    else if ( propertyName.equals( "Code" ))
    {
      Hex newValue = ( Hex )e.getNewValue();
      int sizes = newValue.getData()[ 2 ] & 0x00FF;
      int fixedLength = sizes >> 4;
      fixedDataNode.setLength( fixedLength );
      int cmdLength = sizes & 0x000F;
      cmdDataNode.setLength( cmdLength );
    }
  }

  // DocumentListener methods
  public void documentChanged( DocumentEvent e )
  {
  }

  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  public void enableButtons()
  {
    addButton.setEnabled( selectedNode.canAddChildren());
    deleteButton.setEnabled( selectedNode.canDelete());
  }

  // TreeSelectionListener methods
  public void valueChanged( TreeSelectionEvent e )
  {
    selectedNode =
      ( ProtocolEditorNode )tree.getLastSelectedPathComponent();
    enableButtons();
    editorPanel = selectedNode.getEditingPanel();
    editorPanel.update( selectedNode );
    cardLayout.show( cardPanel, editorPanel.getTitle());
    getContentPane().validate();
  }

  private CardLayout cardLayout;
  private JPanel cardPanel;
  private JLabel title;
  private ProtocolEditorPanel editorPanel = null;
  private DefaultTreeModel treeModel;
  private JTree tree;
  private ProtocolEditorNode selectedNode;
  private DefaultMutableTreeNode root;
  private String nodeInfo;
  private GeneralEditorNode generalNode = null;
  private FixedDataEditorNode fixedDataNode = null;
  private CmdEditorNode cmdDataNode = null;
  private JButton viewButton;
  private JButton addButton;
  private JButton deleteButton;
  private JButton okButton;
}
