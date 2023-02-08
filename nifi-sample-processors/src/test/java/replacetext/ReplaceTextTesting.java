package replacetext;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

public class ReplaceTextTesting {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(ReplaceText.class);
    }

    @Test
    public void testReplaceWordProcessor() {
        testRunner.setProperty(ReplaceText.WORD_TO_REPLACE, "bad");
        testRunner.setProperty(ReplaceText.REPLACEMENT_WORD, "good");

        testRunner.enqueue("The weather is bad today".getBytes());

        testRunner.run();

        testRunner.assertAllFlowFilesTransferred(ReplaceText.SUCCESS, 1);
        final MockFlowFile out = testRunner.getFlowFilesForRelationship(ReplaceText.SUCCESS).get(0);

        out.assertContentEquals("The weather is good today");
    }
}
