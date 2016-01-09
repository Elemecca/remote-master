package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class RMNewDialog extends JDialog implements ActionListener, ListSelectionListener
{
  private RMNewDialog( Component c )
  {
    super( ( JFrame )SwingUtilities.getRoot( c ) );
    setTitle( "Select Remote Type" );
    setModal( true );
    
    Collection< Remote > rm = RemoteManager.getRemoteManager().getRemotes();
    remotesArray.addAll( rm );

    remotesList = new JList( remotesModel );
    remotesList.setVisibleRowCount( 20 );
    remotesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    remotesList.addListSelectionListener( this );
    
    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    
    JPanel selectPanel = new JPanel( new BorderLayout() );
    selectPanel.setBorder( BorderFactory.createCompoundBorder( 
        BorderFactory.createLineBorder( Color.GRAY ), 
        BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );
    contentPane.add( selectPanel, BorderLayout.CENTER);
    
    selectPanel.add( new JScrollPane( remotesList ), BorderLayout.CENTER );
    
    JPanel auxPanel = new JPanel( new BorderLayout() );
    selectPanel.add( auxPanel, BorderLayout.PAGE_END );
    
    JPanel infoPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    infoPanel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
    auxPanel.add( infoPanel, BorderLayout.PAGE_START );
     
    JPanel sortPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    auxPanel.add( sortPanel, BorderLayout.PAGE_END );

    // Add the info buttons
    infoButton.addActionListener( this );
    imageButton.addActionListener( this );
    rdfButton.addActionListener( this );
    infoPanel.add( new JLabel( "View:  ") );
    infoPanel.add( infoButton );
    infoPanel.add( imageButton );
    infoPanel.add( rdfButton );
    infoButton.setEnabled( false );
    imageButton.setEnabled( false );
    rdfButton.setEnabled( false );
    
    // Add the sort order buttons
    descriptionButton.addActionListener( this );
    signatureButton.addActionListener( this );
    extenderCheck.addActionListener( this );
    sortPanel.add( new JLabel( "Sort by:  " ) );
    sortPanel.add( descriptionButton );
    sortPanel.add( signatureButton );
    sortPanel.add( new JLabel( "        ") );
    sortPanel.add( extenderCheck );
    ButtonGroup sortButtons = new ButtonGroup();
    sortButtons.add( descriptionButton );
    sortButtons.add( signatureButton );
    descriptionButton.setSelected( true );
    sortRemotes( DESCRIPTION );
    
    // Add the action buttons
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    contentPane.add( buttonPanel, BorderLayout.PAGE_END );
    
    okButton.addActionListener( this );
    buttonPanel.add( okButton );

    cancelButton.addActionListener( this );
    buttonPanel.add( cancelButton );
    
  }
  
  private void sortRemotes( DefaultListCellRenderer renderer )
  {
    if ( renderer == DESCRIPTION )
    {
      Collections.sort( remotesArray );
    }
    else if ( renderer == SIGNATURE )
    {
      Collections.sort( remotesArray, BY_SIGNATURE );
    }
      
    remotesModel.clear();
    for ( Remote remote : remotesArray )
    {
      if ( ! extenderCheck.isSelected() 
          || ! ( remote.getName().toUpperCase().contains( "EXTENDER" ) 
              || remote.getName().toUpperCase().contains( " EXT " ) ) )
      {
        remotesModel.addElement( remote );
      }
    }
    remotesList.setCellRenderer( renderer );
  }
  
  public static Remote showDialog( RemoteMaster owner )
  {
    dialog = new RMNewDialog( owner );
    dialog.pack();
    dialog.setLocationRelativeTo( owner );
    dialog.setVisible( true );
    return remote;
  }
  
  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    
    if ( source == okButton )
    {
      remote = ( Remote )remotesList.getSelectedValue();
      if ( remote == null )
      {
        String title = "Select Remote";
        String message = "You have not selected a remote!";
        JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
        return;
      }
      setVisible( false );
    }
    else if ( source == cancelButton )
    {
      remote = null;
      setVisible( false );
    }    
    else if ( source == descriptionButton )
    {
      sortRemotes( DESCRIPTION );
    }
    else if ( source == signatureButton )
    {
      sortRemotes( SIGNATURE );
    }
    else if ( source == extenderCheck )
    {
      sortRemotes( descriptionButton.isSelected() ? DESCRIPTION : SIGNATURE );
    }
    else if ( source == infoButton )
    {
      Remote rm = ( Remote )remotesList.getSelectedValue();
      rm.load();
      String title = "Information on Selected Remote";
      String message = "Name:  " + rm.getName() +
      "\nProcessor:  " + rm.getProcessorDescription() +
      "\nInterface:  " + rm.getInterfaceType() +
      "\nUses RDF for:  " + rm.getRdfName() +
      "\nSpecial Identification:  " + rm.getRdfIdentification();
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
    }
    else if ( source == imageButton )
    {
      Remote rm = ( Remote )remotesList.getSelectedValue();
      Remote.prelimLoad = true;
      rm.load();
      Remote.prelimLoad = false;
      
      ImageIcon image = rm.getImage();
      if ( image == null )
      {
        String title = "Missing image";
        String message = "There is no image available for the selected remote";
        JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
        return;
      }
      
      imageDialog = new JDialog( this );
      imageDialog.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
      imageDialog.setTitle( "Image of Selected Remote" );
      imageDialog.setModal( true );
      
      JComponent contentPane = ( JComponent )imageDialog.getContentPane();
      
      JPanel header = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
      header.add( new JLabel( rm.getName() ) );
      contentPane.add(  header, BorderLayout.PAGE_START );
      
      JPanel footer = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
      footer.add( imageOkButton );
      contentPane.add( footer, BorderLayout.PAGE_END );
      imageOkButton.addActionListener( this );
      
      JLabel imageLabel = new JLabel( rm.getImage() );
      imageLabel.setSize( rm.getWidth(), rm.getHeight() );

      contentPane.add( imageLabel, BorderLayout.CENTER );
      imageDialog.pack();
      imageDialog.setLocationRelativeTo( this );
      imageDialog.setVisible( true );
    }
    else if ( source == imageOkButton )
    {
      imageDialog.dispose();
    }
    else if ( source == rdfButton )
    {
      Remote remote = ( Remote )remotesList.getSelectedValue();
      Remote.prelimLoad = true;
      remote.load();
      Remote.prelimLoad = false;
      String title = "RDF of Selected Remote";
      TextFileViewer.showFile( this, remote, title, false );
      // If RDF file has been edited, the remotes in RemoteManager will have changed
      Collection< Remote > rm = RemoteManager.getRemoteManager().getRemotes();
      remotesArray.clear();
      remotesArray.addAll( rm );
      sortRemotes( descriptionButton.isSelected() ? DESCRIPTION : SIGNATURE );
    }
  }
  
  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting() )
      return;
    
    int selected = remotesList.getSelectedIndex();
    infoButton.setEnabled( selected >= 0 );
    imageButton.setEnabled( selected >= 0 );
    rdfButton.setEnabled( selected >= 0 );
  }
  
  private static final Comparator< Remote> BY_SIGNATURE = new Comparator< Remote>()
  {
    @Override
    public int compare( Remote o1, Remote o2 )
    {
      return o1.getSignature().compareToIgnoreCase( o2.getSignature() );
    }    
  };
  
  private static final DefaultListCellRenderer DESCRIPTION = new DefaultListCellRenderer()
  {
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
      Remote remote = ( Remote )value;
      String text = remote.getName() + " (" + remote.getSignature() + ")";
      return super.getListCellRendererComponent( list, text, index, 
          isSelected, cellHasFocus );
      
    }    
  };
  
  private static final DefaultListCellRenderer SIGNATURE = new DefaultListCellRenderer()
  {
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
      Remote remote = ( Remote )value;
      String text = remote.getSignature() + " (" + remote.getName() + ")";
      return super.getListCellRendererComponent( list, text, index, 
          isSelected, cellHasFocus );
      
    }    
  };
 
  private JList remotesList = null;
  private JButton okButton = new JButton( "OK" );
  private JButton imageOkButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
  private JButton infoButton = new JButton( "Information" );
  private JButton imageButton = new JButton( "Image" );
  private JButton rdfButton = new JButton( " RDF ");
  private JRadioButton descriptionButton = new JRadioButton( "Description" ); 
  private JRadioButton signatureButton = new JRadioButton( "Signature" );
  private JCheckBox extenderCheck = new JCheckBox( "Exclude extenders", true );
  private DefaultListModel remotesModel = new DefaultListModel();
  private ArrayList< Remote > remotesArray = new ArrayList< Remote >();
  private JDialog imageDialog = null;
  
  private static RMNewDialog dialog = null;
  private static Remote remote = null;
  
}
