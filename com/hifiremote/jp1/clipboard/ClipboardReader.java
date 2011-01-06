/**
 * 
 */
package com.hifiremote.jp1.clipboard;

import java.io.IOException;

/**
 * @author Greg
 */
public interface ClipboardReader
{
  public String[] readNextLine() throws IOException;

  public void close() throws IOException;
}
