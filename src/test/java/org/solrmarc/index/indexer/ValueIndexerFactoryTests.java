package org.solrmarc.index.indexer;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;


public class ValueIndexerFactoryTests
{
    private Record testRecord;
    private Record testRecord2;
    private final static String inputfilename="./src/test/resources/records/u233.mrc";
    private final static String inputfilename2="./src/test/resources/records/u5278992.mrc";
    private static ValueIndexerFactory factory;
    static
    {
        factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir", "src/test/resources")});
    }

    @Before
    public void setup() throws FileNotFoundException
    {
        InputStream input = new BufferedInputStream(new FileInputStream(inputfilename));
        MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
        testRecord = reader.next();
        testRecord.setId(1L);
        input = new BufferedInputStream(new FileInputStream(inputfilename2));
        reader = new MarcPermissiveStreamReader(input, true, true);
        testRecord2 = reader.next();
        testRecord2.setId(1L);
    }

    @Test
    public void testEmptyConfiguration() throws IllegalAccessException, InstantiationException
    {
        String[] configSpecs = { };
        final List<AbstractValueIndexer<?>> valueIndexers = factory.createValueIndexers(configSpecs);
        assertEquals(0, valueIndexers.size());
    }

    @Test
    public void testConstantIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("constant", "\"Test constant\"");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("constant", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Test constant", indexer.getFieldData(null).iterator().next());
    }

    @Test
    public void testFullRecordIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("fullRecord", "text");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("fullRecord", indexer.getSolrFieldNames().iterator().next());
        @SuppressWarnings("unused")
        String xml = indexer.getFieldData(testRecord).iterator().next().toString();
    }

    @Test
    public void testDateIndexedIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("dateIndexed", "dateRecordIndexed");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("dateIndexed", indexer.getSolrFieldNames().iterator().next());
        String indexed = indexer.getFieldData(testRecord).iterator().next().toString();
        String indexed2 = indexer.getFieldData(testRecord2).iterator().next().toString();
        assertEquals(indexed, indexed2);
    }

    @Test
    public void testFieldSpecDataFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("title_fields", "245abnp, cleanEnd");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("title_fields", indexer.getSolrFieldNames().iterator().next());
        Collection<String> result = indexer.getFieldData(testRecord);
        assertEquals(1, result.size());
        String value = result.iterator().next();
        assertEquals("Lascaux en Périgord noir : environnement, art pariétal et conservation", value);
    }

    @Test
    public void testFieldSpecControlFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("id", "001");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("id", indexer.getSolrFieldNames().iterator().next());
        Collection<String> result = indexer.getFieldData(testRecord);
        assertEquals(1, result.size());
        assertEquals("u233", result.iterator().next()); 
    }

    //  "008[35-37]:041a:041d"
    @Test
    public void testFieldSpecMultiFieldIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("language_facet", "008[35-37]:041a:041d, language_map.properties, unique");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("language_facet", indexer.getSolrFieldNames().iterator().next());
        Collection<String> result = indexer.getFieldData(testRecord);
        String[] expected = new String[] { "French", "English", "German", "Spanish" };
        assertThat(result, containsInAnyOrder(expected));
    }

    @Test
    public void testMixinIndexer() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("mixin", "custom, testMixinMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("mixin", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
    }

    @Test(expected = NullPointerException.class)
    public void testManyIndexers() throws Exception
    {
        String[] configs = {
        "mixin = custom, testMixinMethod",
        "constant = \"Test constant\"",
        "fullRecord = xml" };

        final List<AbstractValueIndexer<?>> valueIndexers = factory.createValueIndexers(configs);
        assertEquals(3, valueIndexers.size());

        for (final AbstractValueIndexer<?> valueIndexer : valueIndexers)
        {
            // This will fail because the full record indexer dosn't allow NULL as record.
            assertNotNull(valueIndexer.getFieldData(null));
        }
    }

    @Test
    public void testJavaIndexerInheritanceParent() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("javacall", "java(org.test.TestMixin), testMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("javacall", indexer.getSolrFieldNames().iterator().next());
        assertEquals("<null>", indexer.getFieldData(null).iterator().next());
        String recordAsText = indexer.getFieldData(testRecord).iterator().next();
        int leaderLength = recordAsText.indexOf("\n");
        String firstLine = recordAsText.substring(leaderLength+1, recordAsText.indexOf("\n", leaderLength+1));
        assertEquals("001 u233", firstLine);

    }

    @Test
    public void testJavaIndexerInheritanceChild() throws Exception
    {
        final AbstractValueIndexer<?> valueIndexer = factory.createValueIndexer("javachildcall", "java(org.solrmarc.ChildMixin), testMethod");

        final MultiValueIndexer indexer = (MultiValueIndexer) valueIndexer;
        assertEquals(1, indexer.getSolrFieldNames().size());
        assertEquals("javachildcall", indexer.getSolrFieldNames().iterator().next());
        assertEquals("Overwritten in ChildMixin", indexer.getFieldData(null).iterator().next());
    }
}
