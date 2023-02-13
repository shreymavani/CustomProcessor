package replacetext.callbacks;

import org.apache.nifi.action.Component;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.processor.io.StreamCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceTextCallbacks implements StreamCallback {
    private final String wordToReplace;
    private final String replacementWord;
    private boolean _isSuccess = false;
    private ComponentLog _logger = null;
    public ReplaceTextCallbacks(final String wordToReplace, final String replacementWord, ComponentLog logger) {
        this.wordToReplace = wordToReplace;
        this.replacementWord = replacementWord;
        this._logger = logger;
    }


    @Override
    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
        _logger.log(LogLevel.DEBUG,"Replacing Text");

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {

                Pattern pattern = Pattern.compile(wordToReplace);            // Compile the pattern

                Matcher matcher = pattern.matcher(line);             // Replace the password with the string ""

                writer.write(matcher.replaceAll(replacementWord) + "\n");
                writer.newLine();
            }
            _isSuccess = true;
        }
        catch (Exception e)
        {
            _logger.error(e.getMessage());
        }
    }

    public boolean SuccessOnProcessing() {
        return _isSuccess ;
    }
}
