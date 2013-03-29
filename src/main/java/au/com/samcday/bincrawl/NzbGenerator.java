package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.bincrawl.dao.entities.BinaryPart;
import au.com.samcday.bincrawl.dao.entities.Release;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;

public class NzbGenerator {
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    private CouchDbConnector couchDb;

    static {
        XML_OUTPUT_FACTORY.setProperty("javax.xml.stream.isRepairingNamespaces", true);
    }

    @Inject
    public NzbGenerator(CouchDbConnector couchDb) {
        this.couchDb = couchDb;
    }

    public byte[] build(String releaseId) {
        Release release = this.couchDb.get(Release.class, releaseId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter w = XML_OUTPUT_FACTORY.createXMLStreamWriter(baos);
            w.setDefaultNamespace("http://www.newzbin.com/DTD/2003/nzb");
            w.writeStartDocument();
            w.writeStartElement("http://www.newzbin.com/DTD/2003/nzb", "nzb"); {
                w.writeStartElement("head"); {
                    w.writeStartElement("meta"); w.writeAttribute("type", "name"); {
                        w.writeCharacters(release.getName());
                    }; w.writeEndElement();
                }; w.writeEndElement();

                for(Binary binary : ImmutableSortedSet.copyOf(Binary.COMPARATOR, release.getBinaries())) {
                    w.writeStartElement("file");
//                    w.writeAttribute("poster", binary.getPoster());
                    w.writeAttribute("subject", binary.getSubject());
                    w.writeAttribute("date", Long.toString(binary.getDate().getMillis()));
                    {
                        w.writeStartElement("groups"); {
                            w.writeStartElement("group"); {
                                w.writeCharacters(binary.getGroup());
                            }; w.writeEndElement();
                        }; w.writeEndElement();

                        w.writeStartElement("segments");
                        int num = 1;
                        for(BinaryPart segment : binary.getParts()) {
                            w.writeStartElement("segment");
                            w.writeAttribute("bytes", Long.toString(segment.getSize()));
                            w.writeAttribute("number", Integer.toString(num++));
                            {
                                w.writeCharacters(segment.getMessageId().substring(1, segment.getMessageId().length() - 1));
                            }; w.writeEndElement();
                        }
                        w.writeEndElement();
                    }; w.writeEndElement();
                }
            }; w.writeEndElement();
            w.writeEndDocument();
            w.close();

            return baos.toByteArray();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
