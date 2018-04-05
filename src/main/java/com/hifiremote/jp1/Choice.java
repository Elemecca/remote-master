package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Choice.
 */
public class Choice
{
  
  /**
   * Instantiates a new choice.
   * 
   * @param index the index
   * @param text the text
   */
  public Choice( int index, String text )
  {
    this( index, text, false );
  }

  /**
   * Instantiates a new choice.
   * 
   * @param index the index
   * @param text the text
   * @param hidden the hidden
   */
  public Choice( int index, String text, boolean hidden )
  {
    this.index = index;
    this.text = text;
    this.hidden = hidden;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString(){ return text; }
  
  /**
   * Gets the index.
   * 
   * @return the index
   */
  public int getIndex(){ return index; }
  
  /**
   * Gets the text.
   * 
   * @return the text
   */
  public String getText(){ return text; }
  
  /**
   * Sets the text.
   * 
   * @param text the new text
   */
  public void setText( String text )
  {
    this.text = text;
  }
  
  /**
   * Checks if is hidden.
   * 
   * @return true, if is hidden
   */
  public boolean isHidden(){ return hidden; }
  
  /**
   * Sets the hidden.
   * 
   * @param flag the new hidden
   */
  public void setHidden( boolean flag )
  {
    this.hidden = flag;
  }

  /** The index. */
  private int index;
  
  /** The text. */
  private String text;
  
  /** The hidden. */
  private boolean hidden;
}
