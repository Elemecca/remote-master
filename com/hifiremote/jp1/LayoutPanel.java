package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import java.text.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import info.clearthought.layout.*;

public class LayoutPanel
  extends KMPanel
  implements ActionListener
{
  public LayoutPanel( DeviceUpgrade devUpgrade )
  {
    super( "Layout", devUpgrade );
    setLayout( new BorderLayout());
    imagePanel = new ImagePanel();
    // Don't know why, but tooltips don't work without this
    imagePanel.setToolTipText( "" );
    imagePanel.setFocusable( true );
    imagePanel.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0), "delete");
    deleteAction = new AbstractAction( "Remove" ) 
    {
      public void actionPerformed( ActionEvent e ) 
      {
        setFunction( currentShape, null );
        doRepaint();
      }
    };
 
    imagePanel.getActionMap().put( "delete", deleteAction ); 

    JPanel leftPanel = new JPanel( new BorderLayout());
    scrollPanel = Box.createHorizontalBox();
    leftPanel.add( scrollPanel, BorderLayout.SOUTH );
    scrollLeft = new JButton( "<" );
    scrollLeft.setEnabled( false );
    scrollLeft.addActionListener( this );
    scrollPanel.add( scrollLeft );

    scrollPanel.add( Box.createHorizontalGlue());
    scrollPanel.add( new JLabel( "Scroll" ));
    scrollPanel.add( Box.createHorizontalGlue());

    scrollRight = new JButton( ">" );
    scrollRight.setEnabled( false );
    scrollRight.addActionListener( this );
    scrollPanel.add( scrollRight );
    
    scrollPane = new JScrollPane( imagePanel,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
    leftPanel.add( scrollPane, BorderLayout.WEST );
    add( leftPanel, BorderLayout.WEST );

    JPanel rightPanel = new JPanel( new BorderLayout());
    add( rightPanel, BorderLayout.CENTER );

    JPanel modePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
    modePanel.setBorder( BorderFactory.createTitledBorder( "Mode" ));

    ButtonGroup group = new ButtonGroup();

    normalMode = new JRadioButton( "Normal", true );
    normalMode.addActionListener( this );
    group.add( normalMode );
    modePanel.add( normalMode );

    shiftMode = new JRadioButton( "Shift" );
    shiftMode.addActionListener( this );
    group.add( shiftMode );
    modePanel.add( shiftMode );

    xShiftMode = new JRadioButton( "XShift" );
    xShiftMode.addActionListener( this );
    group.add( xShiftMode );
    modePanel.add( xShiftMode );

    JPanel infoPanel = new JPanel( new GridLayout( 2, 2 ));

    JLabel label = new JLabel( "Button:" );
    label.setToolTipText( "button label" );
    infoPanel.add( label );
    buttonName = new JTextField();
    buttonName.setEditable( false );
    infoPanel.add( buttonName );

    infoPanel.add( new JLabel( "Function:" ));
    function = new JTextField();
    function.setEditable( false );
    infoPanel.add( function );

    Box box = Box.createVerticalBox();
    box.add( modePanel );
    box.add( infoPanel );

    rightPanel.add( box, BorderLayout.NORTH );

    JPanel panel = new JPanel( new BorderLayout());
    label = new JLabel( "Available Functions:" );
    label.setToolTipText( "available functions label" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ));
    panel.add( label, BorderLayout.NORTH );
    rightPanel.add( panel, BorderLayout.CENTER );

    JPanel outerPanel = new JPanel( new BorderLayout());
    functionPanel = new GridFlowPanel();

    outerPanel.add( new JScrollPane( functionPanel ), BorderLayout.CENTER );
    panel.add( outerPanel, BorderLayout.CENTER );

    panel = new JPanel();
    autoAssign = new JButton( "Auto assign" );
    autoAssign.setToolTipText( "Automatically assign functions to buttons, by matching names." );
    autoAssign.addActionListener( this );
    panel.add( autoAssign );

    JButton button = new JButton( deleteAction );
    button.setToolTipText( "Remove the assigned function from the button." );
    panel.add( button );

    box = Box.createVerticalBox();
    box.add( new JSeparator());
    box.add( panel );

    rightPanel.add( box, BorderLayout.SOUTH );

    DropTarget dropTarget = new LayoutDropTarget();
    dropTarget.setComponent( imagePanel );

    MouseListener ml = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        imagePanel.requestFocusInWindow();
        Point p = e.getPoint();
        ButtonShape savedShape = currentShape;
        currentShape = getShapeAtPoint( p );
        Button b = getButtonForShape( currentShape );
        if ( b == null )
          currentShape = null;

        if ( currentShape == null )
          return;

        if ( currentShape != savedShape )
        {
          setButtonText( currentShape, getButtonForShape( currentShape ));
          doRepaint();
        }
        showPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }

      private void showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          ButtonShape buttonShape = getShapeAtPoint( e.getPoint());
          if (( buttonShape != null ) && ( getButtonForShape( buttonShape ) != null ))
          {
            currentShape = buttonShape;
            doRepaint();
            popup.show( imagePanel, e.getX(), e.getY());
          }
        }
      }
    };
    imagePanel.addMouseListener( ml );
  }

  private void addFunction( Function f )
  {
    if (( f == null ) ||
        (( f.getHex() != null ) && ( f.getName() != null ) && (f.getName().length() > 0 )))
    {
      FunctionLabel l;
      if ( f == null )
        l = new FunctionLabel( null );
      else
        l = f.getLabel();
      l.addMouseListener( doubleClickListener );
      functionPanel.add( l );

      FunctionItem item;
      if ( f == null )
        item = new FunctionItem( null );
      else
        item = f.getItem();
      item.addActionListener( this );
      popup.add( item );
    }
  }

  private void setFunctions()
  {
    popup = new JPopupMenu();
    popup.setLayout( new GridLayout( 0, 3 ));
    FunctionItem item = null;

    functionPanel.removeAll();

    for ( Function function : deviceUpgrade.getFunctions())
      addFunction( function );

    for ( Function function : deviceUpgrade.getExternalFunctions())
      addFunction( function );
  }

  private void enableScrollButtons()
  {
    if ( maps.length > 1 )
    {
      scrollPanel.setVisible( true );
      scrollLeft.setEnabled( screenIndex > 0 );
      scrollRight.setEnabled( screenIndex < maps.length - 1 );
    }
    else
    {
      scrollPanel.setVisible( false );
      scrollLeft.setEnabled( false );
      scrollRight.setEnabled( false );
    }
  }

  public void update()
  {
    Remote r = deviceUpgrade.getRemote();
    maps = r.getImageMaps( deviceUpgrade.getDeviceType());
    if ( screenIndex >= maps.length )
      screenIndex = maps.length - 1;
    enableScrollButtons();
    map = maps[ screenIndex ];
    Dimension d = new Dimension( r.getWidth(), r.getHeight());
    imagePanel.setPreferredSize( d );
    imagePanel.setMinimumSize( d );
    imagePanel.setMaximumSize( d );
    imagePanel.revalidate();
    boolean found = false;
    for ( ButtonShape shape : map.getShapes())
    {
      if ( currentShape == shape )
      {
        found = true;
        break;
      }
    }
    if ( !found )
      currentShape = null;

    setButtonText( currentShape, getButtonForShape( currentShape ));

    setFunctions();

    shiftMode.setText( r.getShiftLabel());
    xShiftMode.setText( r.getXShiftLabel());
    if ( r.getXShiftEnabled())
      xShiftMode.setEnabled( true );
    else
    {
      xShiftMode.setEnabled( false );
      if ( xShiftMode.isSelected())
        normalMode.setSelected( true );
    }
    doRepaint();
  }

  private void doRepaint()
  {
    imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight());
  }

  private void setButtonText( ButtonShape buttonShape, Button b )
  {
    if (( buttonShape != null ) && ( b != null ))
    {
      String name = buttonShape.getName();
      if ( name == null )
      {
        if ( normalMode.isSelected())
          name = b.getName();
        else if ( shiftMode.isSelected())
          name = b.getShiftedName();
        else if ( xShiftMode.isSelected())
          name = b.getXShiftedName();
      }
      buttonName.setText( name );
      Function f = null;
      if ( normalMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
      else if ( shiftMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
      else if ( xShiftMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );
      if ( f != null )
        function.setText( f.getName());
      else
        function.setText( "" );
      deleteAction.setEnabled( f != null );
    }
    else
    {
      buttonName.setText( "" );
      function.setText( "" );
      deleteAction.setEnabled( false );
    }
  }

  public ButtonShape getShapeAtPoint( Point p )
  {
    ButtonMap buttonMap = deviceUpgrade.getDeviceType().getButtonMap();
    ButtonShape closestMatch = null;
    for ( ButtonShape buttonShape : map.getShapes())
    {
      Shape s = buttonShape.getShape();
      if (( s != null ) && s.contains( p ))
      {
        if ( closestMatch == null )
          closestMatch = buttonShape;
        Button b = getButtonForShape( buttonShape );
        if ( buttonMap.isPresent( b ))
          return buttonShape;
      }
    }
    return closestMatch;
  }
  
  public Button getButtonForShape( ButtonShape buttonShape )
  {
    if ( buttonShape == null )
      return null;

    Button b = buttonShape.getButton();
    ButtonMap buttonMap = deviceUpgrade.getDeviceType().getButtonMap();
    
    if ( !b.getIsNormal() && !normalMode.isSelected())
      return null;
    
    if ( normalMode.isSelected())
    {
      if ( b.allowsKeyMove() || buttonMap.isPresent( b ))
        return b;
    }
    else if ( shiftMode.isSelected())
    {
      if ( b.getIsNormal())
      {
        if ( !b.allowsShiftedKeyMove())
          return null;
        if (( b.getShiftedButton() != null ))
          b = b.getShiftedButton();
        else
          return b;
      }
      if ( b.getIsShifted())
      {
        if ( b.allowsKeyMove() || buttonMap.isPresent( b ))
          return b.getBaseButton();
        else
          return null;
      }
      else
        return null;
    }
    else if ( xShiftMode.isSelected())
    {
      if ( b.getIsNormal())
      {
        if ( !b.allowsXShiftedKeyMove())
          return null;
        if ( b.getXShiftedButton() != null )
          b = b.getXShiftedButton();
        else
          return b;
      }
      if ( b.getIsXShifted())
      {
        if ( b.allowsKeyMove() || buttonMap.isPresent( b ))
//          return b.getBaseButton();
          return b;
      }
      else if ( b.allowsXShiftedKeyMove())
        return b;
    }
    return null;
  }

  private void setFunction( ButtonShape shape, Function f )
  {
    Button b = getButtonForShape( shape );
    if ( b != null )
    {
      if ( normalMode.isSelected())
        deviceUpgrade.setFunction( b, f, Button.NORMAL_STATE );
      else if ( shiftMode.isSelected())
        deviceUpgrade.setFunction( b, f, Button.SHIFTED_STATE );
      else if ( xShiftMode.isSelected())
        deviceUpgrade.setFunction( b, f, Button.XSHIFTED_STATE );
      setButtonText( currentShape, b );
      deviceUpgrade.checkSize();
    }
  }

  // From interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == scrollLeft )
    {
      map = maps[ --screenIndex ];
      
      enableScrollButtons();
      doRepaint();
    }
    else if ( source == scrollRight )
    {
      map = maps[ ++screenIndex ];
      enableScrollButtons();
      doRepaint();
    }
    else if ( source == autoAssign )
    {
      deviceUpgrade.autoAssignFunctions();
      doRepaint();
    }
    else if (( source == normalMode ) ||
             ( source == shiftMode ) ||
             ( source == xShiftMode ))
    {
      Button b = getButtonForShape( currentShape );
      if ( b == null )
        currentShape = null;
      setButtonText( currentShape, b );
      doRepaint();
    }
    else
    {
      setFunction( currentShape, (( FunctionItem )source ).getFunction()); 
      doRepaint();
    }
  }

  class DoubleClickListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      if (( currentShape != null ) && ( e.getClickCount() >= 2 ))
      {
        Button button = getButtonForShape( currentShape );
        if ( button == null )
          return;

        Function f = ( Function )(( FunctionLabel )e.getSource()).getFunction();
        setFunction( currentShape, f );
      }
      doRepaint();
    }
  }

  class LayoutDropTarget
    extends DropTarget
  {
    public LayoutDropTarget()
    {
      setDefaultActions( DnDConstants.ACTION_COPY );
    }

    public void dragOver( DropTargetDragEvent dtde )
    {
      Point p = dtde.getLocation();
      currentShape = getShapeAtPoint( p );
      Button button = getButtonForShape( currentShape );
      if ( button != null )
        dtde.acceptDrag( dtde.getDropAction());
      else
      {
        currentShape = null;
        dtde.rejectDrag();
      }
      doRepaint();
    }

    public void drop( DropTargetDropEvent dtde )
    {
      Point p = dtde.getLocation();
      currentShape = getShapeAtPoint( p );
      Button button = getButtonForShape( currentShape );
      if ( button != null )
      {
        int action = dtde.getDropAction();
        dtde.acceptDrop( action );
        Transferable tf = dtde.getTransferable();
        DataFlavor[] flavors = tf.getTransferDataFlavors();
        try
        {
          Function f = ( Function )tf.getTransferData( LocalObjectTransferable.getFlavor());
          setFunction( currentShape, f );
        }
        catch ( Exception e )
        {
          e.printStackTrace( System.err );
        }
      }
      else
        dtde.rejectDrop();
      doRepaint();
    }
  }

  private class ImagePanel
    extends JPanel
    implements Scrollable
  {
    public ImagePanel()
    {
      super();
//      setHorizontalAlignment( SwingConstants.LEFT );
//      setVerticalAlignment( SwingConstants.TOP );      
    }

    public void paint( Graphics g )
    {
      super.paint( g );
      Graphics2D g2 = ( Graphics2D ) g;
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON );
      Remote r = deviceUpgrade.getRemote();
      ImageIcon icon = map.getImage();
      if ( icon != null )
        g2.drawImage( icon.getImage(), null, null );

      g2.setPaint( Color.darkGray );
      for ( ButtonShape shape : r.getPhantomShapes())
        g2.fill( shape.getShape());

      if ( currentShape != null )
      {
        g2.setPaint( Color.white );
        g2.setStroke( new BasicStroke( 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.draw( currentShape.getShape());
      }

      g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));

      DeviceType devType = deviceUpgrade.getDeviceType();
      ButtonMap buttonMap = devType.getButtonMap();

      for ( ButtonShape buttonShape : map.getShapes())
      {
        Button b = getButtonForShape( buttonShape );
        if ( b == null )
          continue;

        Shape s = buttonShape.getShape();

        Function f = null;
        if ( normalMode.isSelected())
          f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
        else if ( shiftMode.isSelected())
          f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
        else if ( xShiftMode.isSelected())
          f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );

        if ( f != null )
        {
          g2.setPaint( Color.yellow );
          g2.fill( s );
        }

        if ( shiftMode.isSelected())
          b = b.getShiftedButton();
        else if ( xShiftMode.isSelected())
          b = b.getXShiftedButton();

        if ( buttonMap.isPresent( b ))
        {
          if (( currentShape != null ) && ( s == currentShape.getShape()))
          {
            g2.setPaint( Color.white );
            g2.setStroke( new BasicStroke( 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
            g2.draw( currentShape.getShape());
            g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
          }
          g2.setPaint( Color.orange );
          g2.draw( s );
        }
      }
    }

    public String getToolTipText( MouseEvent e )
    {
      ButtonShape buttonShape = getShapeAtPoint( e.getPoint());
      if ( buttonShape == null )
        return null;

      Button b = getButtonForShape( buttonShape );
      if ( b == null )
        return null;

      String name = buttonShape.getName();
      if ( name == null )
      {
        Remote r = deviceUpgrade.getRemote();
        if ( normalMode.isSelected())
          name = b.getName();
        else if ( shiftMode.isSelected())
          name = b.getShiftedName();
        else if ( xShiftMode.isSelected())
          name = b.getXShiftedName();
      }
      Function f = null;
      if ( normalMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
      else if ( shiftMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
      else if ( xShiftMode.isSelected())
        f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );

      String text = name;
      if ( f != null )
        text = name + " = " + f.getName();

      return text;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
      Dimension rc = null;

      if ( map != null )
      {
        int w = deviceUpgrade.getRemote().getWidth();
        int h = deviceUpgrade.getRemote().getHeight();
        if ( scrollPane.getViewport().getExtentSize().height < h )
          w += scrollPane.getVerticalScrollBar().getWidth();

        rc = new Dimension( w, h );
      }
      else
        rc = new Dimension( 0, 0 );

      return rc;
    }

    public int getScrollableUnitIncrement( Rectangle visibleRect,
                                           int orientation,
                                           int direction )
    {
      return 1;
    }

    public int getScrollableBlockIncrement( Rectangle visibleRect,
                                            int orientation,
                                            int direction )
    {
      return visibleRect.height;
    }

    public boolean getScrollableTracksViewportWidth()
    {
      return true;
    }

    public boolean getScrollableTracksViewportHeight()
    {
      return false;
    }
  }

  private ImageMap[] maps = null;
  private ImageMap map = null;
  private int screenIndex = 0;
  private ButtonShape currentShape = null;
  private Box scrollPanel = null;
  private JButton scrollLeft = null;
  private JButton scrollRight = null;
  private ImagePanel imagePanel = null;
  private JRadioButton normalMode = null;
  private JRadioButton shiftMode = null;
  private JRadioButton xShiftMode = null;
  private JTextField buttonName = null;
  private JTextField function = null;
  private JButton autoAssign = null;
  private AbstractAction deleteAction = null;
  private JPopupMenu popup = null;
  private JPanel functionPanel = null;
  private JScrollPane scrollPane = null;
  private DoubleClickListener doubleClickListener = new DoubleClickListener();
}
