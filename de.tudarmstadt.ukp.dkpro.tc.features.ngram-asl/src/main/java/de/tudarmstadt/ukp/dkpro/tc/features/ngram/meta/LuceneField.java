package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.Reader;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;

public class LuceneField
    extends Field
{
    public static final FieldType TYPE_NOT_STORED = new FieldType();

    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        TYPE_NOT_STORED.setIndexed(true);
        TYPE_NOT_STORED.setTokenized(false);
        TYPE_NOT_STORED.setStoreTermVectors(true);
        TYPE_NOT_STORED.setStoreTermVectorPositions(false);
        TYPE_NOT_STORED.freeze();

        TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(false);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(false);
        TYPE_STORED.freeze();
    }

    /** Creates a new TextField with Reader value. */
    public LuceneField(String name, Reader reader, Store store)
    {
        super(name, reader, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }

    /** Creates a new TextField with String value. */
    public LuceneField(String name, String value, Store store)
    {
        super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }
}