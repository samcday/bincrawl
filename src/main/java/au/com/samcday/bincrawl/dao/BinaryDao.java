package au.com.samcday.bincrawl.dao;

import au.com.samcday.bincrawl.dao.entities.Binary;
import au.com.samcday.jnntp.Overview;

public interface BinaryDao {
    /**
     * Creates a new binary if one does not already exist.
     * @return a binary hash representing the binary just created/updated.
     */
    public String createBinary(String group, String processedSubject, int numParts, Overview overview);

    /**
     * Adds a part to an existing binary. Will also flag the binary as complete if enough parts have been received.
     */
    public void addBinaryPart(String binaryHash, int partNum, Overview overview);

    /**
     * Deletes binary and all supporting data.
     * @param binaryHash
     */
    public void deleteBinary(String binaryHash);

    /**
     * Updates a binary with the release it belongs to.
     * @param binaryHash
     * @param releaseId
     */
    public void setReleaseInfo(String binaryHash, String releaseId, int num);

    /**
     * Gets all data relating to a binary.
     * @param binaryHash
     */
    public Binary getBinary(String binaryHash);
}
