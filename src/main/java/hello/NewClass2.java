package hello;

import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * Created by ahmadjawid on 5/21/17.
 */
public class NewClass2 <I> implements NewClass1 <I> {
    @Override
    public I mapFieldSet(FieldSet fieldSet) throws BindException {

        I i = (I) new Object();
        return null;
    }
}
