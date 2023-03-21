package com.dobugs.yologaauthenticationapi.support.logging;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.dobugs.yologaauthenticationapi.support.FileGenerator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileLogger {

    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("a HH:mm:ss.SSS");
    private static final String FILE_EXTENSION = ".txt";
    private static final String OCCURRENCE_TIME_PREFIX = "[발생 시간]";

    private final FileGenerator fileGenerator;
    private final String savedDirectory;

    public void write(final String message) {
        final LocalDateTime now = LocalDateTime.now();
        final String fileName = getFileName(now);
        final String occurrenceTime = getOccurrenceTime(now);

        final File file = fileGenerator.createFile(savedDirectory, fileName);
        fileGenerator.write(file, occurrenceTime);
        fileGenerator.write(file, message);
        fileGenerator.write(file, "\n");
    }

    private String getFileName(final LocalDateTime now) {
        return String.join("", now.format(FILE_NAME_FORMAT), FILE_EXTENSION);
    }

    private String getOccurrenceTime(final LocalDateTime now) {
        return String.join(" ", OCCURRENCE_TIME_PREFIX, now.format(DATE_TIME_FORMATTER));
    }
}
