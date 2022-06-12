package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static java.net.URLDecoder.decode;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.FILES_FOLDER;

@Component
public class FileUtils {

    public File getResourceFile(final String fileName) throws UnsupportedEncodingException {
        final String file = getClass().getClassLoader().getResource(FILES_FOLDER + fileName).getFile();
        final String result = decode(file, "UTF-8");
        return new File(result);
    }
}