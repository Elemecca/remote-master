package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class AssemblerOpCode implements Cloneable
{
  private String name = "*";
  private AddressMode mode = new AddressMode();
  private int index = 0;        // Index to next list if multi-byte opcode
  private Hex hex = new Hex( 0 );
  private int length = 1;
  
  public enum TokenType { SYMBOL, PREFIX, LABEL, NUMBER, OFFSET, CONDITION_CODE, NULL, ERROR };
  
  public AssemblerOpCode(){};
  
  public AssemblerOpCode( Processor p, String[] parms )
  {
    name = parms[ 0 ];
    mode = p.getAddressModes().get( parms[ 1 ] );
    if ( mode == null )
    {
      mode = new AddressMode();
    }
    if ( parms.length > 2 )
    {
        index = Integer.parseInt( parms[ 2 ] ); 
    }             
  }
  
  public static class Token
  {
    public Character symbol = null;
    public String text = null;
    public Integer value = null;
    public TokenType type = null;
    
    private String source = null;
    private Processor proc = null;
    private int ndx = 0;
    
    public Token(){};
    
    public Token( String source, Processor proc )
    {
      this.source = source;
      this.proc = proc;
    }
    
    public Token nextToken()
    {
      Token t = new Token( source, proc );
      t.ndx = ndx;
      Character ch = null;
      String s = "";
      int startNdx = ndx;
      while ( true )
      {
        ch = nextChar();
        if ( ch == null ) break;
        if ( !Character.isLetterOrDigit( ch ) ) break;
        s += ch;
      }
      if ( ch == null && s.isEmpty() )
      {
        clean();
        return null;
      }
      if ( s.isEmpty() )
      {
        t.symbol = ch;
        t.type = TokenType.SYMBOL;
      }
      else 
      {
        if ( ch != null ) ndx--;  // Point to ch as next token
        if ( type == TokenType.SYMBOL && symbol == '$' )
        {
          Integer val = getValue( s, 16 );
          if ( val == null )
          {
            value = 0;
            type = TokenType.OFFSET;
          }
          else
          {
            type = TokenType.NULL;
            t.value = val;
            t.type = TokenType.NUMBER;
          }
        }
        else if ( type == TokenType.PREFIX )
        {
          t.value = getValue( s, 16 );
          t.type = TokenType.NUMBER;
        }
        else if ( proc != null ) for ( String px : proc.getHexPrefixes() )
        {
          if ( s.startsWith( px ) && getValue( s.substring( px.length() ), 16 ) != null )
          {
            t.text = px;
            t.type = TokenType.PREFIX;
            ndx = startNdx;
            // Set ndx so that only prefix has been read
            for ( int i = 0; i < px.length(); i++ ) nextChar();
          }
        }
      }
      
      if ( t.type == null )
      {
        t.value = getValue( s, 10 );
        if ( t.value != null )
        {
          t.type = TokenType.NUMBER;
        }
        else if ( proc != null && proc.getConditionIndex( s ) >= 0 )
        {
          t.text = s;
          t.type = TokenType.CONDITION_CODE;
        }
        else
        {
          t.text = s;
          t.type = TokenType.LABEL;
        }
      }
      t.ndx = ndx;
      clean();
      return t;
    }
    

    private Character nextChar()
    {
      while ( ndx < source.length() && Character.isWhitespace( source.charAt( ndx ) ) ) ndx++;
      if ( ndx >= source.length() ) return null;
      return source.charAt( ndx++ );
    }
    
    private Integer getValue( String s, int base )
    {
      if ( base == 10 && s.endsWith( "H" ) ) 
      {
        base = 16;
        s = s.substring( 0, s.length() - 1 );
      }
      if ( s.isEmpty() ) return null;
      String hexChars = "0123456789ABCDEF";
      int val = 0;
      for ( int i = 0; i < s.length(); i++ )
      {
        int n = hexChars.indexOf( s.charAt( i ) );
        if ( n < 0 || base == 10 && n > 9 ) return null;
        val = val * base + n;
      }
      return val;
    }
    
    private void clean()
    {
      source = null;
      ndx = 0;
    }
  }
  
  public static class AddressMode
  {
    public String name = "";
    public int length = 0;        // Number of argument bytes   
    public int relMap = 0;        // Which arg bytes are relative addresses
    public int nibbleMap = 0;     // Which data nibbles are used as args
    public int ccMap = 0;         // Which data nibbles are mapped to condition codes
    public int absMap = 0;        // Which data bytes start a 2-byte absolute address
    public int zeroMap = 0;       // Which data bytes are zero-page or register addresses
    public int modifier = 0;      // Which modifier function to use
    public int nibbleBytes = 0;   // (Calculated) number of arg bytes split into nibbles
    public int nibbleArgs = 0;    // (Calculated) number of data nibbles used as args
    public String format = "";    // Print format
    public String outline = "";   // Outline of print format
    public Integer[] argMap = new Integer[ 4 ];
    public int[] argLimits = new int[ 4 ];
    
    public AddressMode(){};

    public AddressMode( String[] parms )
    {
      List< Integer > keyPositions = new ArrayList< Integer >(); 
      name = parms[ 0 ];
      String s = parms[ 1 ];
      for ( int i = 0; i < s.length(); i++ )
      {
        if ( !Character.isDigit( s.charAt( i ) ) )
        {
          keyPositions.add( i );
        }
      }
      keyPositions.add(  s.length() );
      for ( int i = 0; i < keyPositions.size() - 1; i++ )
      {
        String key = s.substring( keyPositions.get( i ), keyPositions.get( i ) + 1 );
        String val = s.substring( keyPositions.get( i ) + 1, keyPositions.get( i + 1 ) );
        if ( key.equals( "C" ) )
        {
          ccMap = Integer.parseInt( val );
        }
        else if ( key.equals( "N" ) )
        {
          nibbleMap = Integer.parseInt( val );
          for ( int n = 0; nibbleMap >> n != 0; n++ )
          {
            if ( ( ( nibbleMap >> n ) & 1 ) == 1 ) nibbleArgs++;
            if ( n > 1 && ( n & 1 ) == 0 ) nibbleBytes++;
          }
          length += nibbleBytes;
        }
        else if ( key.equals( "B" ) )
        {
          length += Integer.parseInt( val );
        }
        else if ( key.equals( "R" ) )
        {
          relMap = Integer.parseInt( val );
        }
        else if ( key.equals( "A" ) )
        {
          absMap = Integer.parseInt( val );
        }
        else if ( key.equals( "Z" ) )
        {
          zeroMap = Integer.parseInt( val );
        }
        else if ( key.equals( "M" ) )
        {
          modifier = Integer.parseInt( val );
        }
      }
      
      format = parms[ 2 ];
      Arrays.fill(  argMap, 0 );
      Arrays.fill(  argLimits, 0 );
      int[][] formatStarts = getFormatStarts( format );
      
      int[][] starts = new int[ formatStarts.length ][ 3 ];   // reordered format starts
      for ( int i = 0; i < starts.length; i++ ) Arrays.fill( starts[ i ], -1 );
      for ( int i = 0; i < starts.length; i++ )
      {
        // Order by occurrence in format
        int n = formatStarts[ i ][ 2 ];
        if ( n >= 0 )
        {
          starts[ n ][ 0 ] = formatStarts[ i ][ 0 ];
          starts[ n ][ 1 ] = formatStarts[ i ][ 1 ];
          starts[ n ][ 2 ] = i; 
        }
      }
      boolean gap = true;
      for ( int i = 0, m = 0, n = 0; i < format.length(); i++ )
      {
        if ( n >= starts.length || starts[ n ][ 0 ] < 0 || i <= starts[ n ][ 0 ] )
        {
          char ch = format.charAt( i );
          if ( gap || ch != '%' ) outline += format.charAt( i );
          if ( ch != '%' ) gap = true;
          continue;
        }
        i = starts[ n ][ 1 ] + 1;
        if ( format.substring( i ).startsWith( "02X" ) )
        {
          if ( gap ) outline += "X";
          else m--;
          argLimits[ m ] = ( argLimits[ m ] << 8 ) + 0xFF;
          argMap[ m ] = argMap[ m ] << 4;
          i += 2;
        }
        else if ( format.substring( i ).startsWith( "04X" ) )
        {
          outline += "X";
          argLimits[ m ] = 0xFFFF;
          i += 2;
        }
        else if ( format.substring( i ).startsWith( "X" ) )
        {
          outline += "X";
          argLimits[ m ] = 0xF;
        }
        else if ( format.substring( i ).startsWith( "d" ) )
        {
          outline += "X";
          argLimits[ m ] = 7;
        }
        else if ( format.substring( i ).startsWith( "s" ) )
        {
          outline += "s, ";
        }
        argMap[ m++ ] += starts[ n++ ][ 2 ] + 1;
        gap = ( format.substring( i ).startsWith( "s" ) ) ? true : false;
        
      }
      outline = outline.replace( "%XH", "%X" );
      outline = outline.replace( "$%X", "%X" );
    }
  }
  
  public static class OpArg extends ArrayList< Token >
  { 
    public String outline = "";
    
    public OpArg(){};
    
    public OpArg( String arg, Processor proc, LinkedHashMap< String, String > labels )
    {
      if ( arg == null ) return;
      Token t = new Token( arg, proc );
      while ( true )
      {
        t = t.nextToken();
        if ( t == null ) break;
        if ( t.type == TokenType.LABEL && labels != null && labels.containsKey( t.text ) )
        {
          t = new Token( labels.get( t.text ) + t.source.substring( t.ndx ), proc );
          continue;
        }
        add( t );
      }
      doGroup( Arrays.asList( '(', ')') );
      
      Iterator< Token > it = iterator();
      while ( it.hasNext() )
      {
        t = it.next();
        switch ( t.type )
        {
          case SYMBOL:
            outline += t.symbol;
            it.remove();
            break;
          case PREFIX:
          case LABEL:
            outline += t.text;
          case NULL:
            it.remove();
            break;
          case OFFSET:
          case NUMBER:
            outline += "%X";
            break;
          case CONDITION_CODE:
            outline += "%s";
            break;
        }

      }
    }
    
    public static OpArg getArgs ( String argText, Processor processor, LinkedHashMap< String, String > labels  )
    {
      OpArg args = new OpArg();
      argText = argText.toUpperCase() + ",";
      while ( argText.length() > 1 )
      {
        int pos = argText.indexOf( ',' );
        OpArg arg = new OpArg( argText.substring( 0, pos++ ), processor, labels );
        args.addAll( arg );
        args.outline += arg.outline + ", ";
        argText = argText.substring( pos );
      }
      args.outline = args.outline.substring( 0, Math.max( args.outline.length() - 2, 0 ) );
      return args;
    }
    
    private void doGroup( List< Character > brackets )
    {
      int initSize = 0;
      do
      {
        int start = -1;
        int end = 0;
        initSize = size();
        for ( ; end < size(); end++ )
        {
          int n = brackets.indexOf( get( end ).symbol );
          if ( n == 0 ) start = end;
          if ( n == 1 ) break;
        }

        doUnary( Arrays.asList( '+', '-' ), start + 1, end );
        doCalc( Arrays.asList( '*', '/', '%' ), start + 1, end + size() - initSize );
        doCalc( Arrays.asList( '+', '-' ), start + 1, end + size() - initSize );
        end -= initSize - size();
        // Don't remove brackets enclosing whole token
        if ( ( start > 0 || end < size() - 1 ) && end == start + 2 )
        {
          remove( end );
          remove( start );
        }
      } while ( size() < initSize );
    }
    
    private void doCalc( List< Character > ops, int start, int end )
    {
      for ( int i = start + 1; i < end - 1; i++ )
      {
        Character op = get( i ).symbol;
        if ( ops.contains( op ) )
        {
          Integer op1 = get( i - 1 ).value;   
          Integer op2 = get( i + 1 ).value;
          if ( op1 != null && op2 != null )
          {
            Token t = new Token();
            t.value = ( op == '*' ) ? op1 * op2 : ( op == '/' ) ? op1 / op2 
                : ( op == '%' ) ? op1 % op2 : ( op == '+' ) ? ( op1 + op2 ) : ( op1 - op2 );
            t.type = resultType( get( i - 1 ).type, op, get( i + 1 ).type );
            removeRange( i - 1, i + 2 );
            add( i - 1, t );
            end -= 2;
            i--;
          }
        }
      }
    }

    private void doUnary( List< Character > ops, int start, int end )
    {
      for ( int i = start; i < end - 1; i++ )
      {
        if ( i > 0 && get( i - 1 ).type != TokenType.SYMBOL ) continue;
        Character op = get( i ).symbol;
        if ( ops.contains( op ) && get( i + 1 ).value != null )
        {
          if ( op == '-' ) 
          {
            get( i + 1 ).value *= -1;
            if ( get( i + 1 ).type == TokenType.OFFSET ) get( i + 1 ).type = TokenType.ERROR;
          }
          remove( i );
          end--;
        }
      }
    }
    
    private TokenType resultType( TokenType t1, Character op, TokenType t2 )
    {
      if ( t1 == TokenType.NUMBER && t2 == TokenType.NUMBER ) return TokenType.NUMBER;
      if ( t1 == TokenType.OFFSET && t2 == TokenType.NUMBER ) return TokenType.OFFSET;
      if ( t1 == TokenType.NUMBER && t2 == TokenType.OFFSET && op == '+' ) return TokenType.OFFSET;
      return TokenType.ERROR;
    }
  }
  
  public static int[][] getFormatStarts( String format )
  {
    int[][] starts = new int[ 4 ][ 3 ];
    for ( int i = 0; i < starts.length; i++ ) Arrays.fill( starts[ i ], -1 );
    for ( int i = 0, j = 0; i < format.length(); i++ )
    {
      if ( format.charAt( i ) == '%' )
      {
        int pos = j++;  // implicit arg index
        int start = i;
        int k = i + 1;
        for ( ; k < format.length() && Character.isDigit( format.charAt( k ) ); k++ );
        if ( k < format.length() && format.charAt( k ) == '$' )
        {
          pos = Integer.parseInt( format.substring( i + 1, k ) ) - 1; // explicit arg index
          start = k;
        }
        starts[ pos ][ 0 ] = i;
        starts[ pos ][ 1 ] = start;
        starts[ pos ][ 2 ] = j - 1;
      }
    }
    return starts;
  }
  
  @Override
  public AssemblerOpCode clone()
  {
    AssemblerOpCode opCode = new AssemblerOpCode();
    opCode.name = this.name;
    opCode.mode = this.mode;
    opCode.index = this.index;
    opCode.length = this.length;
    opCode.hex = new Hex( this.hex );
    return opCode;
  }
  
  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public AddressMode getMode()
  {
    return mode;
  }

  public void setMode( AddressMode mode )
  {
    this.mode = mode;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex( int index )
  {
    this.index = index;
  }

  public Hex getHex()
  {
    return hex;
  }

  public void setHex( Hex hex )
  {
    this.hex = hex;
  }

  public int getLength()
  {
    return length;
  }

  public void setLength( int length )
  {
    this.length = length;
  }
  
}

