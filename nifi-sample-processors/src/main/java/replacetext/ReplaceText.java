package replacetext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import replacetext.callbacks.ReplaceTextCallbacks;

@Tags({"replace", "word"})
@CapabilityDescription("Replaces specific words in incoming FlowFiles.")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class ReplaceText extends AbstractProcessor {

    public static final PropertyDescriptor WORD_TO_REPLACE = new PropertyDescriptor
            .Builder().name("Word to Replace")
            .displayName("Word to Replace")
            .description("The word to replace in incoming FlowFiles")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor REPLACEMENT_WORD = new PropertyDescriptor
            .Builder().name("Replacement Word")
            .displayName("Replacement Word")
            .description("The word to use as a replacement")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("success")
            .description("FlowFiles that have been successfully replaced")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("fail")
            .description("FlowFiles that have been Failed")
            .build();

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> propertyDescriptors;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        super.init(context);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);

        final ArrayList<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        propertyDescriptors.add(WORD_TO_REPLACE);
        propertyDescriptors.add(REPLACEMENT_WORD);
        this.propertyDescriptors = Collections.unmodifiableList(propertyDescriptors);
    }

    @Override
    public Set<Relationship> getRelationships(){
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors(){
        return propertyDescriptors;
    }
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final String wordToReplace = context.getProperty(WORD_TO_REPLACE).getValue();
        final String replacementWord = context.getProperty(REPLACEMENT_WORD).getValue();

        try {

            ReplaceTextCallbacks replaceTextCallbacks = new ReplaceTextCallbacks(wordToReplace,replacementWord,getLogger());
            flowFile = session.write(flowFile, replaceTextCallbacks);

            if(replaceTextCallbacks.SuccessOnProcessing()) {
                session.transfer(flowFile, SUCCESS);
                session.commit();
            }
            else
                session.transfer(flowFile,FAILURE);

        }catch (final Exception e) {
            getLogger().error("Failed to replace word in {} due to {} - Error", new Object[] {flowFile, e});
            session.transfer(flowFile,FAILURE);
        }
    }
}
