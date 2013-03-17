package au.com.samcday.bincrawl.data;

import au.com.samcday.jnntp.Overview;
import au.com.samcday.jnntp.Xref;

public interface BinaryDao {
    /**
     * Creates a new binary if one does not already exist.
     * @return a binary hash representing the binary just created/updated.
     */
    public String createBinary(String group, String processedSubject, int numParts, Overview overview);

    public void updateXref(String binaryHash, Xref xref);

    /**
     * Adds a part to an existing binary. Will also flag the binary as complete if enough parts have been received.
     */
    public void addBinaryPart(String binaryHash, int partNum, Overview overview);
}
