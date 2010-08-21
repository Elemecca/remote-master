package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextFileViewer extends JDialog implements ActionListener, KeyListener
{
  private TextFileViewer( Component c, File file, String title, boolean Editable )
  {
    super( ( c instanceof JDialog ) ? (JDialog)c : ( JFrame )SwingUtilities.getRoot( c ) );
    this.file = file;
    openedFromToolBar = ( c instanceof RemoteMaster );
    
    setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    setTitle( title );
    if ( openedFromToolBar )
    {
      remoteMaster = ( RemoteMaster )c;
    }
    else
    {
      setModal( true );
    }
    
    textArea = new JTextArea( 30, 80 );
    textArea.setEditable( file.canWrite() );
    if ( textArea.isEditable() )
    {
      textArea.addKeyListener( this );
    }
    
    JScrollPane scrollPane = new JScrollPane( textArea );

    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.add( scrollPane, BorderLayout.CENTER );
    
    JPanel footer = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    footer.add( saveButton );
    footer.add( closeButton );
    contentPane.add( footer, BorderLayout.PAGE_END );
    saveButton.setEnabled( false );
    saveButton.addActionListener( this );
    closeButton.addActionListener( this );
    
    FileReader in = null;
    try
    {
      in = new FileReader( file );
      textArea.read( in, file.toString() );
      in.close();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
  }
  
  public static void showFile( Component locationComp, File file, String title, boolean editable )
  {
    TextFileViewer viewer = new TextFileViewer( locationComp, file, title, editable );
    viewer.pack();
    viewer.setLocationRelativeTo( locationComp );
    viewer.setVisible( true ); 
  }

  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();

    if ( source == closeButton )
    {
      dispose();
    }
    else if ( source == saveButton )
    {
      String message = null;
      if ( openedFromToolBar )
      {
        message = "Do you want to apply your changes?\n\n" +
        "Yes = save and apply changes\n" +
        "No = save changes without applying them\n" +
        "Cancel = abort the save operation";        
      }
      else
      {
        message = "Are you sure you want to save your changes?";
      }
      String title = "Confirm save";
      int response = JOptionPane.showConfirmDialog( this, message, title, 
          ( ! openedFromToolBar ) ? JOptionPane.OK_CANCEL_OPTION : JOptionPane.YES_NO_CANCEL_OPTION ,
          JOptionPane.QUESTION_MESSAGE );
      if (  response == JOptionPane.CANCEL_OPTION )
      {
        return;
      }
      FileWriter out = null;
      try
      {
        out = new FileWriter( file );
        textArea.write( out );
        out.flush();
        out.close();
      }
      catch ( Exception e )
      {
        e.printStackTrace();
      }
      saveButton.setEnabled( false );
      if ( response == JOptionPane.YES_OPTION )
      {
        String rmTitle = remoteMaster.getTitle();
        RemoteConfiguration remoteConfig = remoteMaster.getRemoteConfiguration();
        remoteConfig.setSavedData();
        Remote oldRemote = remoteConfig.getRemote();
        Remote newRemote = new Remote();
        newRemote.setFile( oldRemote.getFile() );
        newRemote.load();
        remoteConfig.setRemote( newRemote );
        SetupCode.setMax( newRemote.usesTwoBytePID() ? 4095 : 2047 );
        remoteConfig.updateImage();
        remoteMaster.update();
        remoteMaster.setTitle( rmTitle );
      }
    }
  }
  
  @Override
  public void keyPressed( KeyEvent e ) {}

  @Override
  public void keyReleased( KeyEvent e ) {}

  @Override
  public void keyTyped( KeyEvent e )
  {
    saveButton.setEnabled( true );
  }
  
  private File file = null;
  private JTextArea textArea = null;
  private boolean openedFromToolBar = false;
  private RemoteMaster remoteMaster = null;
  
  private JButton saveButton = new JButton( "Save" );
  private JButton closeButton = new JButton( "Close" );

}
