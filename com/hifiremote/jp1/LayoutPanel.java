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
    super( devUpgrade );
    setLayout( new BorderLayout());
    imagePanel = new ImagePanel();
    // Don't know why, but tooltips don't work without this
    imagePanel.setToolTipText( "" );

//    JPanel fPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ));
//    fPanel.add( imagePanel );

//    add( new JScrollPane( fPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ),
//         BorderLayout.WEST );
    scrollPane = new JScrollPane( imagePanel,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
    add( scrollPane, BorderLayout.WEST );

    JPanel rightPanel = new JPanel( new BorderLayout());
    add( rightPanel, BorderLayout.CENTER );

    JPanel infoPanel = new JPanel( new GridLayout( 2, 3 ));

    infoPanel.add( new JLabel( "Button:" ));
    infoPanel.add( new JLabel( "Function:" ));
    infoPanel.add( new JLabel( "Shifted:" ));
    buttonName = new JTextField();
    buttonName.setEditable( false );
    infoPanel.add( buttonName );

    function = new JTextField();
    function.setEditable( false );
    infoPanel.add( function );

    shifted = new JTextField();
    shifted.setEditable( false );
    infoPanel.add( shifted );

    rightPanel.add( infoPanel, BorderLayout.NORTH );

    JPanel panel = new JPanel( new BorderLayout());
    JLabel label = new JLabel( "Available Functions:" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ));
    panel.add( label, BorderLayout.NORTH );
    rightPanel.add( panel, BorderLayout.CENTER );

    JPanel outerPanel = new JPanel( new BorderLayout());
    functionPanel = new JPanel( new GridFlowLayout());
//    functionPanel = new JPanel( new GridLayout( 0, 4 ));

    outerPanel.add( functionPanel, BorderLayout.CENTER );
//    outerPanel.add( new JScrollPane( functionPanel ), BorderLayout.NORTH );
    panel.add( outerPanel, BorderLayout.CENTER );

    panel = new JPanel();
    autoAssign = new JButton( "Auto assign" );
    autoAssign.setToolTipText( "Assign functions to buttons of the same name that don't have a functon." );
    autoAssign.addActionListener( this );
    panel.add( autoAssign );

    Box box = Box.createVerticalBox();
    box.add( new JSeparator());
    box.add( panel );

    rightPanel.add( box, BorderLayout.SOUTH );

    DropTarget dropTarget = new LayoutDropTarget();
    dropTarget.setComponent( imagePanel );

    MouseListener ml = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        Point p = e.getPoint();
        ButtonShape savedShape = currentShape;
        currentShape = getShapeAtPoint( p );
        if ( currentShape == null )
          return;
        Button button = currentShape.getButton();
        if ((( e.getModifiersEx() & e.CTRL_DOWN_MASK ) != 0 ) &&
             ( button.getShiftedButton() != null ))
            button = button.getShiftedButton();

        if ( currentShape != savedShape )
        {
          setButtonText( currentShape );
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
          if ( buttonShape != null )
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
    FunctionLabel label = null;
    Function function = null;

    Vector funcs = deviceUpgrade.getFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    funcs = deviceUpgrade.getExternalFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    addFunction( null );
  }

  public void update()
  {
    Remote r = deviceUpgrade.getRemote();
    ImageIcon icon = r.getImageIcon();
    if ( icon != null )
    {
      Dimension size = new Dimension( icon.getIconWidth(),
                                      icon.getIconHeight());
      imagePanel.setPreferredSize( size );
      imagePanel.revalidate();
    }
    ButtonShape[] buttonShapes = deviceUpgrade.getRemote().getButtonShapes();
    boolean found = false;
    for ( int i = 0; i < buttonShapes.length; i++ )
    {
      if ( currentShape == buttonShapes[ i ])
      {
        found = true;
        break;
      }
    }
    if ( !found )
      currentShape = null;
      

    setButtonText( currentShape );

    setFunctions();
    doRepaint();
  }

  private void doRepaint()
  {
    imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight());
  }

  private void setButtonText( ButtonShape buttonShape )
  {
    if ( buttonShape != null )
    {
      Button b = buttonShape.getButton();
      String name = buttonShape.getName();
      if ( name == null )
        name = b.getName();
      buttonName.setText( name );
      Function f = b.getFunction();
      if ( f != null )
        function.setText( f.getName());
      else
        function.setText( "" );
      f = b.getShiftedFunction();
      if ( f != null )
        shifted.setText( f.getName());
      else
        shifted.setText( "" );
    }
    else
    {
      buttonName.setText( "" );
      function.setText( "" );
      shifted.setText( "" );
    }
  }

  public ButtonShape getShapeAtPoint( Point p )
  {
    ButtonShape[] buttonShapes = deviceUpgrade.getRemote().getButtonShapes();
    for ( int i = 0; i < buttonShapes.length; i++ )
    {
      ButtonShape buttonShape = buttonShapes[ i ];
      Shape s = buttonShape.getShape();
      if (( s != null ) && s.contains( p ))
        return buttonShape;
    }
    return null;
  }

// From interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == autoAssign )
    {
      deviceUpgrade.autoAssignFunctions();
      doRepaint();
    }
    else if ( currentShape != null )
    {
      Button button = currentShape.getButton();
      Function function = (( FunctionItem )source ).getFunction();
      if (( e.getModifiers() & e.CTRL_MASK ) == 0 )
        button.setFunction( function );
      else
      {
        if ( button.getShiftedButton() != null )
        {
          button = button.getShiftedButton();
          button.setFunction( function );
        }
        else
          button.setShiftedFunction( function );
      }
      setButtonText( currentShape );
    }
  }

  class DoubleClickListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      if (( currentShape == null ) || ( e.getClickCount() < 2 ))
        e.consume();
      else
      {
        Button button = currentShape.getButton();
        FunctionLabel label = ( FunctionLabel )e.getSource();
        if (( e.getModifiersEx() & e.CTRL_DOWN_MASK ) == 0 )
          button.setFunction( label.getFunction());
        else
          button.setShiftedFunction( label.getFunction());
        setButtonText( currentShape );
        doRepaint();
      }
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
      int action = dtde.getDropAction();
      if (( currentShape != null ) &&
          (( action == DnDConstants.ACTION_COPY ) ||
            ( action == DnDConstants.ACTION_MOVE )))
      {
        dtde.acceptDrag( action );
      }
      else
        dtde.rejectDrag();
      doRepaint();
    }

    public void drop( DropTargetDropEvent dtde )
    {
      Point p = dtde.getLocation();
      currentShape = getShapeAtPoint( p );
      if ( currentShape != null )
      {
        Button button = currentShape.getButton();
        int action = dtde.getDropAction();
        dtde.acceptDrop( action );
        Transferable tf = dtde.getTransferable();
        DataFlavor[] flavors = tf.getTransferDataFlavors();
        try
        {
          Function f = ( Function )tf.getTransferData( LocalObjectTransferable.getFlavor());
          if ( action == DnDConstants.ACTION_COPY )
          {
            if ( button.getShiftedButton() != null )
            {
              button = button.getShiftedButton();
              button.setFunction( f );
            }
            else
              button.setShiftedFunction( f );
          }
          else if ( action == DnDConstants.ACTION_MOVE )
          {
            button.setFunction( f );
          }
          setButtonText( currentShape );
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
    public void paint( Graphics g )
    {
      Graphics2D g2 = ( Graphics2D ) g;
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON );
      Remote r = deviceUpgrade.getRemote();
      ImageIcon icon = r.getImageIcon();
      if ( icon != null )
        g2.drawImage( icon.getImage(), null, null );

      if ( currentShape != null )
      {
        g2.setPaint( Color.white );
        g2.setStroke( new BasicStroke( 4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.draw( currentShape.getShape());
      }

      g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));

      DeviceType devType = deviceUpgrade.getDeviceType();
      ButtonMap map = devType.getButtonMap();

      ButtonShape[] buttonShapes = r.getButtonShapes();
      for ( int i = 0; i < buttonShapes.length; i++ )
      {
        ButtonShape buttonShape = buttonShapes[ i ];
        Button b = buttonShape.getButton();
        Shape s = buttonShape.getShape();
        if ( s != null )
        {
          Function f = b.getFunction();
          Function sf = b.getShiftedFunction();
          if (( f != null ) && ( sf == null ))
          {
            g2.setPaint( Color.blue );
            g2.fill( s );
          }
          else if (( f == null ) && ( sf != null ))
          {
            g2.setPaint( Color.yellow );
            g2.fill( s );
          }
          else if (( f != null ) && ( sf != null ))
          {
            g2.setPaint( Color.green );
            g2.fill( s );
          }
          
          if ( map.isPresent( b ))
          {
            g2.setPaint( Color.orange );
            g2.draw( s );
          }
        }
      }
    }

    public String getToolTipText( MouseEvent e )
    {
      ButtonShape buttonShape = getShapeAtPoint( e.getPoint());
      if ( buttonShape != null )
      {
        Button b = buttonShape.getButton();
        String name = buttonShape.getName();
        if ( name != null )
          return name;
        if ((( e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK ) != 0 ) &&
            ( b.getShiftedButton() != null ))
          return b.getShiftedButton().getName();
        else
          return b.getName();
      }
      else
        return null;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
      ImageIcon icon = deviceUpgrade.getRemote().getImageIcon();
      Dimension rc = null;
      if ( icon != null )
      {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
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

  private ButtonShape currentShape = null;
  private JPanel imagePanel = null;
  private JTextField buttonName = null;
  private JTextField function = null;
  private JTextField shifted = null;
  private JButton autoAssign = null;
  private JPopupMenu popup = null;
  private JPanel functionPanel = null;
  private JScrollPane scrollPane = null;
  private DoubleClickListener doubleClickListener = new DoubleClickListener();
}
