package de.anonytix.analysis;

import java.util.UUID;

public interface AnalysisDirectory {

    AnalysisDescriptor getBySubmissionId(UUID submissionId);
}
