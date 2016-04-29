package playground.solrmarc.index.mapping.impl;

import java.util.Collection;

import playground.solrmarc.index.extractor.methodcall.MultiValueMappingMethodCall;
import playground.solrmarc.index.mapping.AbstractMultiValueMapping;

public class MethodCallMultiValueMapping implements AbstractMultiValueMapping
{

    private final Object[] parameters;
    private final MultiValueMappingMethodCall methodCall;

    public MethodCallMultiValueMapping(MultiValueMappingMethodCall methodCall, String[] parameters)
    {
        this.methodCall = methodCall;
        this.parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 1, parameters.length);
    }

    @Override
    public Collection<String> map(Collection<String> value) throws Exception
    {
        return (Collection<String>) (methodCall.invoke(value, parameters));
    }

}
