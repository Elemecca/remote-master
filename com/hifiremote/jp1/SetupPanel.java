package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

/**
 * The Class SetupPanel.
 */
public class SetupPanel extends KMPanel implements ActionListener, ItemListener, PropertyChangeListener,
    DocumentListener, FocusListener
{
  public static class AltPIDStatus
  {
    public boolean visible = true;
    public boolean required = false;
    public boolean editable = true;
    public boolean hasValue = false;
    public int value = 0;
    public int msgIndex = 0;
  }
  
  public static String getAltPIDReason( int index )
  {
    String reason = "";
    if ( index == 0 )
    {
      return reason;
    }
    switch ( index & 0xFF )
    {
      case 1:
        reason = "Protocol ID exceeds 01FF, not valid for this remote.  An Alternate PID "
          + "is required.";
        break;
      case 2:
        reason = "Protocol is a manual protocol.  Its PID may be changed by giving an Alternate PID.";
        break;
      case 3:
        reason = "Protocol ID conflicts with a built-in protocol.  To use both this and the built-in "
          + "protocol in device upgrades, this one needs to be given an Alternate PID.";
        break;
      case 4:
        reason = "Protocol ID conflicts with existing upgrade.  To use this built-in protocol, edit "
          + "the conflicting upgrade to give it an Alternate PID.";
        break;
    }
    if ( ( index & 0x800 ) == 0x800 )
    {
      if ( !reason.isEmpty() ) reason += "\n";
      reason += "Protocol has custom code.";
    }
    switch ( index & 0xF00 )
    {
      case 0x100:
        reason += "\nProtocol already used in another device upgrade, so the Alternate PID "
          + "has been taken from that upgrade and cannot be changed.";
        break;
      case 0x200:
        reason += "\nProtocol ID already used in another device upgrade by a different protocol. "
          + " An Alternate PID is required.";
        break;
      case 0x300:
        reason += "\nProtocol ID clashes with that of an unused protocol upgrade.  To keep that upgrade "
          + "accessible, this protocol needs an Alternate PID.";
      case 0x400:
        reason += "\nProtocol already used by another device upgrade without an Alternate PID, "
          + "so an alternate cannot be given for this upgrade.";
        break;
    }
    return reason;
  }
  
  /**
   * Instantiates a new setup panel.
   * 
   * @param deviceUpgrade
   *          the device upgrade
   */
  public SetupPanel( DeviceUpgrade deviceUpgrade )
  {
    super( "Setup", deviceUpgrade );

    protocolHolder = new JPanel( new BorderLayout() );
    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    protocolHolder.setBorder( border );

    Insets insets = border.getBorderInsets( protocolHolder );
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10; // space around border
    double i = 5; // space between rows
    double v = 20; // space between groupings
    double c = 10; // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
        {
            b, bl, p, b, p, br, c, f, b
        }, // cols
        {
            b, p, v, 0, 0, p, i, p, 0, 0, 0, v, bt, p, bb, f, p, b
        }
    // rows
    };
    tl = new TableLayout( size );
    setLayout( tl );

    int row = 1;

    JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
    add( label, "2, " + row );
    setupCode = new JTextField();
    SetupCodeFilter filter = new SetupCodeFilter( setupCode );
    ( ( AbstractDocument )setupCode.getDocument() ).setDocumentFilter( filter );
    // setupCode.addPropertyChangeListener( "value", this );
    setupCode.getDocument().addDocumentListener( this );
    FocusSelector.selectOnFocus( setupCode );
    label.setLabelFor( setupCode );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );

    add( setupCode, "4, " + row );

    row += 2;

    preserveLabel = new JLabel( "Preserve:", SwingConstants.RIGHT );
    String[] choices =
    {
        "OBC and function parameters", "EFC and function hex"
    };

    preserveBox = new JComboBox( choices );
    preserveLabel.setLabelFor( preserveBox );
    preserveRow = row;
    preserveBox.setToolTipText( "Select what to preserve when changing protocols" );
    preserveBox.setSelectedIndex( 0 );

    preserveBox.addActionListener( this );
    if ( Boolean.parseBoolean( JP1Frame.getProperties().getProperty( "enablePreserveSelection", "false" ) ) )
    {
      add( preserveLabel, "2, " + preserveRow );
      add( preserveBox, "4, " + preserveRow );
      tl.setRow( preserveRow, p );
      tl.setRow( preserveRow + 1, i );
    }

    row += 2;

    label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    add( label, "2, " + row );

    protocolList = new JComboBox();
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setMaximumRowCount( 25 );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    add( protocolList, "4, " + row );

    row += 2;

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    add( label, "2, " + row );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID (PID) that corresponds to the selected protocol." );
    add( protocolID, "4, " + row );
    
    row += 2;

    altPIDRow = row;
    altPIDLabel = new JLabel( "Alternate PID:", SwingConstants.RIGHT );
    add( altPIDLabel, "2, " + row );
    altPID = new JFormattedTextField( new HexFormat( 0, 2 ) );
    altPID.addPropertyChangeListener( "value", this );
    altPIDLabel.setLabelFor( altPID );
    altPID.setVisible( false );
    altPIDLabel.setVisible( false );
    altPID.setToolTipText( "An alternate PID to be used instead of the main PID." );
    add( altPID, "4, " + row );
    
    row++;
    
    altPIDMessage = new JLabel( "Prototype message" );
    altPIDMessage.setForeground( Color.RED );
    tl.setRow( row, altPIDMessage.getPreferredSize().height );
    add( altPIDMessage, "4, " + row );
    
    row++;
    tl.setRow( row, v - altPIDMessage.getPreferredSize().height );
    altPIDMessage.setText( "" );

    row ++;

    add( protocolHolder, "1, " + row + ", 5, " + ( row + 2 ) );

    row++ ;

    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    add( label, "2, " + row );

    fixedData = new JTextField();
    fixedData.setEditable( false );
    add( fixedData, "4, " + row );

    row++ ;

    notes = new JTextArea( 5, 50 );
    notes.setToolTipText( "Enter any notes about this device upgrade." );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    JScrollPane scrollPane = new JScrollPane( notes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Upgrade Notes" ),
        scrollPane.getBorder() ) );
    notes.getDocument().addDocumentListener( this );
    new TextPopupMenu( notes );
    add( scrollPane, "7, 1, 7, " + row );

    row++ ;

    protocolNotes = new JEditorPane();
    protocolNotes.setBackground( label.getBackground() );
    protocolNotes.setToolTipText( "Notes about the selected protocol." );
    protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    protocolNotes.setEditable( false );
    // protocolNotes.setLineWrap( true );
    // protocolNotes.setWrapStyleWord( true );
    scrollPane = new JScrollPane( protocolNotes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Protocol Notes" ),
        scrollPane.getBorder() ) );
    add( scrollPane, "1, " + row + ", 7, " + row );
    
    row++;
    
    altPIDReason = new JTextPane();
    Font font = altPIDReason.getFont();
    Font font2 = font.deriveFont( Font.BOLD, 12 );
    altPIDReason.setFont( font2 );
    int height = altPIDReason.getPreferredSize().height;
    altPIDReason.setBorder( BorderFactory.createEmptyBorder( 0, 5, -height, 5 ) );
    altPIDReason.setVisible( false );

    add( altPIDReason, "1, " + row + ", 7, " + row );
    JP1Frame.getProperties().addPropertyChangeListener( "enablePreserveSelection", this );
  } // SetupPanel
  
  private void showAltPID()
  {
    status = deviceUpgrade.testAltPID();
    altPIDLabel.setVisible( status.visible );
    altPID.setVisible( status.visible );
//    altPIDMessage.setVisible( status.visible );
    tl.setRow( altPIDRow - 1, status.visible ? 5 : 0 );
    tl.setRow( altPIDRow, status.visible ? TableLayout.PREFERRED : 0 );
    altPID.setEditable( status.editable );
    if ( status.hasValue )
    {
      Hex hx = ( new Hex( 2 ) );
      hx.put(  status.value, 0 );
      altPID.setValue( hx );
    }
    if ( status.visible && status.required )
    {
      protocolID.setText( protocolID.getText() + " : ALT PID REQUIRED" );
    }
    altPIDReason.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    altPIDReason.setText( getAltPIDReason( status.msgIndex ) );
    altPIDReason.setVisible( status.msgIndex > 0 );
    int height = altPIDReason.getPreferredSize().height;
    // It doesn't seem to work to use tl.setRow() on the altPIDReason row, but juggling with the
    // border size has the same effect, of making the row have zero height when reason not showing.
    altPIDReason.setBorder( BorderFactory.createEmptyBorder( 0, 5, status.msgIndex > 0 ? 0 : -height, 5 ) );
  }
  
//  protected boolean isPIDValid()
//  {
//    return altPIDMessage.getText().equals( "" ) && ( !status.required || ( ( Hex )altPID.getValue() ).length() > 0 );
//  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    if ( deviceUpgrade == null )
      return;
    updateInProgress = true;
    Protocol p = deviceUpgrade.getProtocol();
    Remote remote = deviceUpgrade.getRemote();
    setupCode.setText( SetupCode.toString( deviceUpgrade.getSetupCode() ) );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );
    java.util.List< Protocol > protocols = ProtocolManager.getProtocolManager().getProtocolsForRemote( remote );
    if ( !protocols.contains( p ) )
    {
      // ??? There should be a better way to handle this (the current protocol is
      // incompatible with the current remote), but this way is at least better than
      // the old way of displaying the first compatible protocol.
      protocols = new ArrayList< Protocol >( protocols );
      protocols.add( p );
    }

    Value[] vals = deviceUpgrade.getParmValues();
    p.setDeviceParms( vals );
    updateParameters();
    protocolList.setModel( new DefaultComboBoxModel( protocols.toArray() ) );
    protocolList.setSelectedItem( p );
    protocolID.setText( p.getID( remote, false ).toString() );
    notes.setText( deviceUpgrade.getNotes() );
    fixedData.setText( p.getFixedData( vals ).toString() );

    updateProtocolNotes( p.getNotes() );
    
    showAltPID();
    altPID.setValue( p.getRemoteAltPID().get( remote.getSignature() ) );
    setAltPIDMessage();

    updateInProgress = false;
  }

  /**
   * Update parameters.
   */
  public void updateParameters()
  {
    DeviceParameter[] newParameters = deviceUpgrade.getProtocol().getDeviceParameters();
    if ( parameters != newParameters )
    {
      if ( parameters != null )
      {
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].removeListener( this );
          remove( parameters[ i ].getLabel() );
          remove( parameters[ i ].getComponent() );
          tl.deleteRow( 13 );
          tl.deleteRow( 13 );
        }
      }
      parameters = newParameters;
      if ( parameters != null )
      {
        int row = 13;
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].addListener( this );
          tl.insertRow( row, TableLayout.PREFERRED );
          add( parameters[ i ].getLabel(), "2, " + row );
          add( parameters[ i ].getComponent(), "4, " + row );
          row++ ;
          tl.insertRow( row++ , 5 );
        }
        TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
        remove( protocolHolder );
        add( protocolHolder, tlc );
      }
    }
  }

  /**
   * Update fixed data.
   */
  public void updateFixedData()
  {
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    deviceUpgrade.setParmValues( p.getDeviceParmValues() );
    fixedData.setText( p.getFixedData( deviceUpgrade.getParmValues() ).toString() );
  }

  // ActionListener Methods
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      protocolList.hidePopup();
      Protocol newProtocol = getSelectedProtocol();
      Protocol oldProtocol = deviceUpgrade.getProtocol();
      RemoteConfiguration remoteConfig = deviceUpgrade.getRemoteConfig();
      if ( newProtocol != oldProtocol )
      {
        if ( deviceUpgrade.setProtocol( newProtocol ) )
        {
          Remote remote = deviceUpgrade.getRemote();
          protocolID.setText( newProtocol.getID( remote, false ).toString() );
          altPID.setValue( newProtocol.getRemoteAltPID().get( remote.getSignature() ) );
          showAltPID();
          setAltPIDMessage();
          updateParameters();
          fixedData.setText( newProtocol.getFixedData( newProtocol.getDeviceParmValues() ).toString() );
          updateProtocolNotes( newProtocol.getNotes() );
          revalidate();

          deviceUpgrade.checkSize();
          propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, newProtocol );
        }
        else
        {
          protocolList.removeActionListener( this );
          protocolList.setSelectedItem( oldProtocol );
          protocolList.addActionListener( this );
        }
      }
      else if ( !updateInProgress && remoteConfig != null )
      {
        // Protocol is unchanged, but if there is more than one custom protocol then cycle
        // through them
        String title = "Multiple custom protocols";
        String message = "You have reselected the existing protocol for this device upgrade\n"
            + "and there is more than one protocol upgrade in this remote that\n"
            + "can act as custom code for it.  Do you want to change to a different\n" + "custom code?\n\n"
            + "Repeating this and selecting YES each time will cycle through all\n"
            + "available compatible custom codes.";
        ProtocolUpgrade pu = oldProtocol.getCustomUpgrade( remoteConfig, false );
        if ( pu != null
            && oldProtocol.matched()
            && !pu.getCode().equals( deviceUpgrade.getCode() )
            && JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION )
        {
          // Save old custom code and install new code
          String proc = remoteConfig.getRemote().getProcessor().getEquivalentName();
          oldProtocol.newCustomCode = pu;
          oldProtocol.customCode.put( proc, pu.getCode() );
        }
      }
    }
    else if ( source == preserveBox )
    {
      deviceUpgrade.setPreserveOBC( preserveBox.getSelectedIndex() == 0 );
    }
    else
    {
      // must be a protocol parameter
      updateFixedData();
    }
  } // actionPerformed

  protected void updateProtocolNotes( String text )
  {
    String contentType = "text/plain";
    if ( text != null && text.startsWith( "<" ) )
    {
      contentType = "text/html";
    }
    EditorKit kit = protocolNotes.getEditorKitForContentType( contentType );
    protocolNotes.setEditorKit( kit );
    protocolNotes.setText( text );
    protocolNotes.setCaretPosition( 0 );
    protocolNotes.revalidate();
  }

  /**
   * Gets the selected protocol.
   * 
   * @return the selected protocol
   */
  public Protocol getSelectedProtocol()
  {
    Protocol protocol = ( Protocol )protocolList.getSelectedItem();
    return protocol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#commit()
   */
  public void commit()
  {
    deviceUpgrade.getProtocol().updateFunctions( deviceUpgrade.getFunctions() );
  }

  /**
   * Update notes.
   */
  private void updateNotes()
  {
    deviceUpgrade.setNotes( notes.getText() );
  }

  /**
   * Update setup code.
   */
  private void updateSetupCode()
  {
    String text = setupCode.getText();
    if ( text.equals( "" ) )
    {
      return;
    }
    int val = Integer.parseInt( setupCode.getText() );
    int oldSetupCode = deviceUpgrade.getSetupCode();
    deviceUpgrade.setSetupCode( val );
    propertyChangeSupport.firePropertyChange( "setupCode", oldSetupCode, val );
  }

  public void release()
  {
    for ( int i = 0; i < parameters.length; i++ )
    {
      parameters[ i ].removeListener( this );
    }
  }

  /**
   * Doc changed.
   * 
   * @param e
   *          the e
   */
  private void docChanged( DocumentEvent e )
  {
    if ( !updateInProgress )
    {
      Document doc = e.getDocument();
      if ( doc == notes.getDocument() )
        updateNotes();
      else if ( doc == setupCode.getDocument() )
        updateSetupCode();
      else
        updateFixedData();
    }
  }

  // DocumentListener
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // FocusListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained( FocusEvent e )
  {
    JP1Frame.clearMessage( controlWithFocus );
    controlWithFocus = ( JTextComponent )e.getSource();
    JP1Frame.clearMessage( controlWithFocus );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost( FocusEvent e )
  {}

  // ItemListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    if ( !updateInProgress )
      updateFixedData();
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    if ( event.getPropertyName().equals( "enablePreserveSelection" ) )
    {
      boolean oldValue = Boolean.parseBoolean( ( String )event.getOldValue() );
      boolean newValue = Boolean.parseBoolean( ( String )event.getNewValue() );
      if ( oldValue && !newValue )
      {
        remove( preserveLabel );
        remove( preserveBox );
        tl.setRow( preserveRow, 0 );
        tl.setRow( preserveRow + 1, 0 );
      }
      else
      {
        add( preserveLabel, "2, " + preserveRow );
        add( preserveBox, "4, " + preserveRow );
        tl.setRow( preserveRow, TableLayout.PREFERRED );
        tl.setRow( preserveRow + 1, 5 );
      }
    }
    else if ( event.getSource() == altPID )
    {
      Hex pid = ( Hex )altPID.getValue();
      if ( pid == null ) return;
      if ( pid.length() == 1 )
      {
        short val = pid.getData()[ 0 ];
        pid = new Hex( 2 );
        pid.getData()[ 1 ] = val;
        altPID.setValue( pid );
        return;
      }
      
      deviceUpgrade.getProtocol().setAltPID( deviceUpgrade.getRemote(), pid );
      setAltPIDMessage();
    }
    else if ( !updateInProgress )
    {
      updateSetupCode();
    }
  }
  
  private boolean setAltPIDMessage()
  {
    boolean valid = true;
    if ( status.visible )
    {
      Hex pid = ( Hex )altPID.getValue();
      if ( pid == null ) pid = new Hex( 0 );
      Remote remote = deviceUpgrade.getRemote();
      RemoteConfiguration remoteConfig = deviceUpgrade.getRemoteConfig();
      List< Protocol > builtIn = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote, pid );
      if ( !builtIn.isEmpty() )
      {
        altPIDMessage.setText( "Conflicts with built-in protocol" );
        altPID.setForeground( Color.RED );
        valid = false;
      }
      else if ( status != null && status.required && pid.length() == 0 )
      {
        altPIDMessage.setText( "Alternate PID cannot be null" );
        altPID.setForeground( Color.RED );
        valid = false;
      }
      else
      {
        if ( remoteConfig != null )
        {
          for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
          {
            if ( du == deviceUpgrade.getBaseUpgrade() || du.getProtocol() == deviceUpgrade.getProtocol() )
            {
              continue;
            }
            if ( du.getProtocol().getID( remote ).equals( deviceUpgrade.getProtocol().getID( remote ) ) )
            {
              // A different protocol with same PID is already used by a device upgrade.  Alternate
              // required.
              altPIDMessage.setText( "Conflicts with existing upgrade" );
              altPID.setForeground( Color.RED );
              valid = false;
              break;
            }
          }
        }
      }
      if ( valid == true )
      {
        altPIDMessage.setText( "" );
        altPID.setForeground( Color.BLACK );
        if ( deviceUpgrade.getProtocol().getCustomUpgrade( remoteConfig, false ) != null )
        {
          altPIDMessage.setText( "Conflicts with protocol upgrade" );
        }
      }
    }
    else if ( status.required )
    {
      valid = false;
      altPIDMessage.setText( "Protocol selection not valid" );
    }
    DeviceEditorPanel ePanel = (DeviceEditorPanel)SwingUtilities.getAncestorOfClass( DeviceEditorPanel.class, this );
    ePanel.tabbedPane.setEnabled( valid );
    JFrame frame = ePanel.getOwner();
    if ( frame instanceof DeviceUpgradeEditor )
    {
      DeviceUpgradeEditor editor = ( DeviceUpgradeEditor )frame;
      editor.okButton.setEnabled( valid );
      editor.saveAsButton.setEnabled( valid );
    }
    else if ( frame instanceof KeyMapMaster )
    {
      KeyMapMaster km = ( KeyMapMaster )frame;
      km.saveItem.setEnabled( valid && deviceUpgrade.getFile() != null );
      km.saveAsItem.setEnabled( valid );
    }
    return valid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( ( propertyChangeSupport != null ) && ( listener != null ) )
      propertyChangeSupport.addPropertyChangeListener( listener );
  }

  /** The setup code. */
  private JTextField setupCode = null;

  /** The protocol list. */
  private JComboBox protocolList = null;

  /** The protocol id. */
  private JTextField protocolID = null;
  private JFormattedTextField altPID = null;
  private JLabel altPIDLabel = null;
  private JLabel altPIDMessage = null;
  private JTextPane altPIDReason = null;
  private int altPIDRow = 0;
  private AltPIDStatus status = null;

  private JLabel preserveLabel = null;
  private JComboBox preserveBox = null;
  private int preserveRow = 0;

  /** The notes. */
  private JTextArea notes = null;

  /** The protocol holder. */
  private JPanel protocolHolder = null;

  /** The fixed data. */
  private JTextField fixedData = null;

  /** The protocol notes. */
  private JEditorPane protocolNotes = null;

  /** The parameters. */
  private DeviceParameter[] parameters = null;

  /** The tl. */
  private TableLayout tl;

  /** The update in progress. */
  private boolean updateInProgress = false;

  /** The control to select all. */
  private JTextComponent controlWithFocus = null;

  /** The property change support. */
  private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
}
