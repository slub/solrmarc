package org.solrmarc.solr;

import java.io.PrintStream;
import java.util.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.driver.RecordAndDoc;

import com.google.gson.Gson;

public class NDJSONOutProxy extends SolrProxy
{
    PrintStream output;

    public NDJSONOutProxy(PrintStream out)
    {
        this.output = out;
    }

    public int addDoc(RecordAndDoc recdoc)
    {
        synchronized (output)
        {
            Map<String, List<String>> record = new HashMap<String, List<String>>();
            SolrInputDocument inputDoc = recdoc.getDoc();
            for (String name : inputDoc.getFieldNames()) {
                ArrayList<String> valList = new ArrayList<String>();

                Iterator values = inputDoc.get(name).iterator();

                while (values.hasNext()) {
                    valList.add(values.next().toString());
                }

                record.put(name, valList);
            }

            Gson gson = new Gson();

            String jsonOut = gson.toJson(record);

            output.print(jsonOut + "\n");

            return(1);
        }
    }

    @Override
    public int addDocs(Collection<RecordAndDoc> recdocQ)
    {
        int num = 0;
        for (RecordAndDoc recdoc : recdocQ)
        {
            num += this.addDoc(recdoc);
        }
        return(num);
    }

    @Override
    public void commit(boolean optimize)
    {
        output.flush();
    }

    @Override
    public void delete(String id)
    {
    }

    @Override
    public QueryResponse query(SolrQuery params)
    {
        return null;
    }

}

