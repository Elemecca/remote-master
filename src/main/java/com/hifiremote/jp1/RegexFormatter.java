package com.hifiremote.jp1;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.text.DefaultFormatter;

// TODO: Auto-generated Javadoc
/**
 * A regular expression based implementation of <code>AbstractFormatter</code>.
 */
public class RegexFormatter extends DefaultFormatter {
    
    /** The pattern. */
    private Pattern pattern;
    
    /** The matcher. */
    private Matcher matcher;

    /**
     * Instantiates a new regex formatter.
     */
    public RegexFormatter() {
        super();
    }

    /**
     * Creates a regular expression based <code>AbstractFormatter</code>.
     * <code>pattern</code> specifies the regular expression that will
     * be used to determine if a value is legal.
     * 
     * @param textPattern the text pattern
     * 
     * @throws PatternSyntaxException the pattern syntax exception
     */
    public RegexFormatter( String textPattern ) throws PatternSyntaxException {
        this();
        setPattern( textPattern );
    }

    /**
     * Creates a regular expression based <code>AbstractFormatter</code>.
     * <code>pattern</code> specifies the regular expression that will
     * be used to determine if a value is legal.
     * 
     * @param pattern the pattern
     */
    public RegexFormatter(Pattern pattern) {
        this();
        setPattern(pattern);
    }

    /**
     * Sets the pattern that will be used to determine if a value is
     * legal.
     * 
     * @param pattern the pattern
     */
    public void setPattern( Pattern pattern ) {
        this.pattern = pattern;
    }

    /**
     * Sets the pattern.
     * 
     * @param textPattern the new pattern
     * 
     * @throws PatternSyntaxException the pattern syntax exception
     */
    public void setPattern( String textPattern )
      throws PatternSyntaxException
    {
      setPattern( Pattern.compile( textPattern ));
    }

    /**
     * Returns the <code>Pattern</code> used to determine if a value is
     * legal.
     * 
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets the <code>Matcher</code> used in the most recent test
     * if a value is legal.
     * 
     * @param matcher the matcher
     */
    protected void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Returns the <code>Matcher</code> from the most test.
     * 
     * @return the matcher
     */
    protected Matcher getMatcher() {
        return matcher;
    }

    /**
     * Parses <code>text</code> returning an arbitrary Object. Some
     * formatters may return null.
     * <p>
     * If a <code>Pattern</code> has been specified and the text
     * completely matches the regular expression this will invoke
     * <code>setMatcher</code>.
     * 
     * @param text String to convert
     * 
     * @return Object representation of text
     * 
     * @throws ParseException if there is an error in the conversion
     */
    public Object stringToValue(String text) throws ParseException {
        Pattern pattern = getPattern();

        if (pattern != null) {
            Matcher matcher = pattern.matcher(text);

            if (matcher.matches()) {
                setMatcher(matcher);
                return super.stringToValue(text);
            }
            throw new ParseException("Pattern did not match", 0);
        }
        return text;
    }
}

