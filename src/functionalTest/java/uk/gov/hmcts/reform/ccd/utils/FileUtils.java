package uk.gov.hmcts.reform.ccd.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static java.net.URLDecoder.decode;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.FILES_FOLDER;
import static uk.gov.hmcts.reform.ccd.constants.TestConstants.JSON_FOLDER;

@Component
public class FileUtils {

    public File getResourceFile(final String fileName) throws UnsupportedEncodingException {
        final String file = getClass().getClassLoader().getResource(FILES_FOLDER + fileName).getFile();
        final String result = decode(file, "UTF-8");
        return new File(result);
    }

    public String getJsonFromFile(final String fileName) throws IOException {
        final File file = ResourceUtils.getFile(this.getClass().getResource(JSON_FOLDER + fileName));
        final String fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return fileContent;
    }
}
