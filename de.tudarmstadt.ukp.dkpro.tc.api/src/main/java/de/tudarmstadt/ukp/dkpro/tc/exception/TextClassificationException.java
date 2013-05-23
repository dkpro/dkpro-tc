package de.tudarmstadt.ukp.dkpro.tc.exception;

public class TextClassificationException
    extends Exception
{

    static final long serialVersionUID = 1L;

    public TextClassificationException()
    {
        super();
    }

    public TextClassificationException(String txt)
    {
        super(txt);
    }

    public TextClassificationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TextClassificationException(Throwable cause)
    {
        super(cause);
    }

}
