package de.tudarmstadt.ukp.dkpro.tc.api.exception;

/**
 * Exception thrown by DKPro TC components.
 * 
 * @author zesch
 *
 */
public class TextClassificationException
    extends Exception
{

    static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TextClassificationException()
    {
        super();
    }

    /**
     * @param txt
     */
    public TextClassificationException(String txt)
    {
        super(txt);
    }

    /**
     * @param message
     * @param cause
     */
    public TextClassificationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TextClassificationException(Throwable cause)
    {
        super(cause);
    }

}
