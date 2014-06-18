package de.tudarmstadt.ukp.dkpro.tc.core.feature;

/**
 * Base class representing a feature value with a missing value.
 * 
 * @author daxenberger
 * 
 */
public class MissingValue
{

    private MissingValueType type;
    private Class<? extends Enum<?>> nominalClass;

    /**
     * Create a missing value for a non-nominal feature.
     * 
     * @param type
     *            numeric, boolean or string
     */
    public MissingValue(MissingValueNonNominalType type)
    {
        this.type = MissingValueType.valueOf(type.name());
    }

    /**
     * Create a missing value for a nominal feature.
     * 
     * @param clazz
     *            the enum class of the nominal feature value
     * @see MissingValue#MissingValue(Class)
     */
    public MissingValue(Class<? extends Enum<?>> clazz)
    {
        this.type = MissingValueType.NOMINAL;
        this.nominalClass = clazz;
    }

    /**
     * Gets the type of the feature for this missing value.
     * 
     * @return numeric, nominal, boolean or string
     */
    public MissingValueType getType()
    {
        return type;
    }

    /**
     * Gets the enum class of a nominal feature value.
     * 
     * @return an enum class
     */
    public Class<? extends Enum<?>> getNominalClass()
    {
        return nominalClass;
    }

    /**
     * Type of a feature: boolean, numeric, nominal, or string.
     * 
     */
    public enum MissingValueType
    {
        BOOLEAN, NUMERIC, STRING, NOMINAL
    }

    /**
     * Type of a non-nominal feature: boolean, numeric, or string.
     * 
     */
    public enum MissingValueNonNominalType
    {
        BOOLEAN, NUMERIC, STRING
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MissingValue && ((MissingValue) obj).getType().equals(this.type)) {
            if (((MissingValue) obj).getType().equals(MissingValueType.NOMINAL)) {
                if (this.getNominalClass() != null
                        && this.getNominalClass().getSimpleName()
                                .equals(((MissingValue) obj).getNominalClass().getSimpleName())) {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        return false;
    }
}
