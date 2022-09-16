package org.solrmarc.marc;

import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class MarcMultiplexReader implements MarcReader
{
    private final static Logger logger = LogManager.getLogger(MarcMultiplexReader.class);
    final Collection<MarcReader> readers;
    final Collection<String> readerNames;
    final Iterator<MarcReader> readerIterator;
    final Iterator<String> nameIterator;
    int readerCnt = 0;
    MarcReader curReader = null;
    
//    public MarcMultiplexReader(MarcReader ... marcReaders)
//    {
//        readers = Arrays.asList(marcReaders);
//        iterator = readers.iterator();
//    }
//
    public MarcMultiplexReader(final Collection<MarcReader> marcReaders, final Collection<String> readerNames)
    {
        readers = marcReaders;
        this.readerNames = readerNames;
        readerIterator = readers.iterator();
        nameIterator = this.readerNames.iterator();
    }

    @Override
    public boolean hasNext()
    {
        while (curReader == null || !curReader.hasNext())
        {
            if (readerIterator.hasNext())
            {
                String readerName = (nameIterator.hasNext()) ? nameIterator.next() : ""+readerCnt;
                logger.info("Switching to reader: "+readerName);
                curReader = readerIterator.next();
                readerCnt++;
            }
            else
            {
                curReader = null;
                return(false);
            }
        }
        return(curReader.hasNext());
    }

    @Override
    public Record next()
    {
        return(curReader.next());
    }
    
}
