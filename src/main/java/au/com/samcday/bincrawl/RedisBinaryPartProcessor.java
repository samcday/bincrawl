package au.com.samcday.bincrawl;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class RedisBinaryPartProcessor implements BinaryPartProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(RedisBinaryPartProcessor.class);

    private static final Joiner PIPE_JOINER = Joiner.on("|");


    @Override
    public void processPart(String group, String name, Date date, int size, String messageId, int part, int totalParts) {

    }
}
