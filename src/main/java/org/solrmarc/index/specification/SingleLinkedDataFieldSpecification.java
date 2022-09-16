package org.solrmarc.index.specification;

import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.specification.conditional.Condition;


public class SingleLinkedDataFieldSpecification extends SingleDataFieldSpecification
{
    String lnkTag;

    public SingleLinkedDataFieldSpecification(String tag, String subfields, Condition cond)
    {
        super(tag, subfields, cond);
        lnkTag = tag.substring(3);
        tags[0] = "880";
    }

    public SingleLinkedDataFieldSpecification(String tag, String subfields)
    {
        this(tag, subfields, null);
    }

    private SingleLinkedDataFieldSpecification(SingleLinkedDataFieldSpecification toClone)
    {
        super(toClone);
        this.lnkTag = toClone.lnkTag;
        tags[0] = "880";
    }

    @Override
    protected boolean specMatches(String tag, VariableField f)
    {
        String stag = this.lnkTag;
        if (tag.equals("880") && ((DataField) f).getSubfield('6') != null
                && ((DataField) f).getSubfield('6').getData().startsWith(stag))
        {
            return (true);
        }
        return false;
    }

    @Override
    public Object makeThreadSafeCopy()
    {
        return new SingleLinkedDataFieldSpecification(this);
    }

}
