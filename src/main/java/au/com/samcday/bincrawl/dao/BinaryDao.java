package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.jnntp.Overview;

import java.util.List;

public interface BinaryDao {
    /**
     * Adds a part to an existing binary. Will also flag the binary as complete if enough parts have been received.
     */
    public String addBinaryPart(String group, String subject, int partNum, int numParts, Overview overview,
                                BinaryClassifier.Classification classification);

    /**
     * Deletes binary and all supporting data.
     * @param binaryHash
     */
    public void deleteBinary(String binaryHash);

    /**
     * Gets all data relating to a binary.
     * @param binaryHash
     */
    public Binary getBinary(String binaryHash);

    /**
     * Blocks for a completed binary and executes the handler with binary data.
     */
    public void processCompletedRelease(CompletedReleaseHandler handler);

    public static interface CompletedReleaseHandler {
        public boolean handle(List<Binary> completed) throws Exception;
    }
}
