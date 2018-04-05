package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * The Class ProtocolEditor.
 */
public class ProtocolEditor extends JDialog implements ActionListener, PropertyChangeListener, DocumentListener,
    TreeSelectionListener
{

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main( String[] args )
  {
    try
    {
      System.setErr( new PrintStream( new FileOutputStream( new File( "pedit.err" ) ) ) );
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    JFrame frame = new JFrame( "Test" );
    ProtocolEditor e = new ProtocolEditor( frame );
    e.setVisible( true );
    System.exit( 0 );
  }

  /**
   * Instantiates a new protocol editor.
   * 
   * @param owner
   *          the owner
   */
  public ProtocolEditor( JFrame owner )
  {
    super( owner, "Protocol Editor", true );
    createGui( owner );
  }

  /**
   * Instantiates a new protocol editor.
   * 
   * @param owner
   *          the owner
   */
  public ProtocolEditor( JDialog owner )
  {
    super( owner, "Protocol Editor", true );
    createGui( owner );
  }

  /**
   * Creates the gui.
   * 
   * @param owner
   *          the owner
   */
  private void createGui( Component owner )
  {
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
        scrollPane.getBorder() ) );
    contentPane.add( scrollPane, BorderLayout.WEST );

    cardLayout = new CardLayout( 5, 5 );
    cardPanel = new JPanel( cardLayout );
    for ( Enumeration< ? > e = root.children(); e.hasMoreElements(); )
    {
      ProtocolEditorNode node = ( ProtocolEditorNode )e.nextElement();
      ProtocolEditorPanel panel = node.getEditingPanel();
      cardPanel.add( panel, panel.getTitle() );
    }

    ProtocolEditorNode node = new DevParmEditorNode();
    node.addPropertyChangeListener( "Name", this );
    ProtocolEditorPanel panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle() );
    node = ( ProtocolEditorNode )node.getFirstChild();
    panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle() );

    node = new CmdParmEditorNode();
    panel = node.getEditingPanel();
    cardPanel.add( panel, panel.getTitle() );

    contentPane.add( cardPanel, BorderLayout.CENTER );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING, 5, 5 ) );

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
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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
      cardPanel.add( newPanel, newPanel.getTitle() );
      for ( Enumeration< ? > en = newNode.children(); en.hasMoreElements(); )
      {
        ProtocolEditorNode child = ( ProtocolEditorNode )en.nextElement();
        TreePath path = new TreePath( child.getPath() );
        tree.expandPath( path );
        tree.scrollPathToVisible( path );
        // newPanel = child.getEditingPanel();
        // cardPanel.add( newPanel, newPanel.getTitle());
      }
      TreePath newPath = new TreePath( newNode.getPath() );
      tree.setSelectionPath( newPath );
    }
    else if ( source == viewButton )
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      for ( Enumeration< ? > children = root.children(); children.hasMoreElements(); )
      {
        ProtocolEditorNode node = ( ProtocolEditorNode )children.nextElement();
        node.print( pw );
      }
      pw.flush();
      JTextArea ta = new JTextArea( sw.toString() );
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
      tree.getSelectionModel().setSelectionPath( new TreePath( nodeToSelect.getPath() ) );
    }
    else if ( source == okButton )
    {
      setVisible( false );
      dispose();
    }
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    ProtocolEditorNode node = ( ProtocolEditorNode )e.getSource();
    String propertyName = e.getPropertyName();
    System.err.println( "PropertyChange for " + propertyName );
    if ( propertyName.equals( "Hex" ) )
    {
      enableButtons();
    }
    else if ( propertyName.equals( "Name" ) )
    {
      treeModel.nodeChanged( node );
      TreePath path = new TreePath( node.getPath() );
      tree.collapsePath( path );
      tree.expandPath( path );
    }
    else if ( propertyName.equals( "Code" ) )
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
  /**
   * Document changed.
   * 
   * @param e
   *          the e
   */
  public void documentChanged( DocumentEvent e )
  {}

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /**
   * Enable buttons.
   */
  public void enableButtons()
  {
    addButton.setEnabled( selectedNode.canAddChildren() );
    deleteButton.setEnabled( selectedNode.canDelete() );
  }

  // TreeSelectionListener methods
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
   */
  public void valueChanged( TreeSelectionEvent e )
  {
    selectedNode = ( ProtocolEditorNode )tree.getLastSelectedPathComponent();
    enableButtons();
    editorPanel = selectedNode.getEditingPanel();
    editorPanel.update( selectedNode );
    cardLayout.show( cardPanel, editorPanel.getTitle() );
    getContentPane().validate();
  }

  /** The card layout. */
  private CardLayout cardLayout;

  /** The card panel. */
  private JPanel cardPanel;

  /** The editor panel. */
  private ProtocolEditorPanel editorPanel = null;

  /** The tree model. */
  private DefaultTreeModel treeModel;

  /** The tree. */
  private JTree tree;

  /** The selected node. */
  private ProtocolEditorNode selectedNode;

  /** The root. */
  private DefaultMutableTreeNode root;

  /** The general node. */
  private GeneralEditorNode generalNode = null;

  /** The fixed data node. */
  private FixedDataEditorNode fixedDataNode = null;

  /** The cmd data node. */
  private CmdEditorNode cmdDataNode = null;

  /** The view button. */
  private JButton viewButton;

  /** The add button. */
  private JButton addButton;

  /** The delete button. */
  private JButton deleteButton;

  /** The ok button. */
  private JButton okButton;
}
