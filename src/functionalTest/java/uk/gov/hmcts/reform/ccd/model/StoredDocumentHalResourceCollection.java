package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StoredDocumentHalResourceCollection(
    @JsonProperty("_embedded") DocumentsEmbedded embedded
) {
    public record Link(String href) {}

    public record Links(
        @JsonProperty("self") Link self,
        @JsonProperty("binary") Link binary
    ) {}

    public record Version(
        @JsonProperty("_links") Links links,
        String createdOn,
        String originalDocumentName
    ) {}

    public record EmbeddedVersions(
        @JsonProperty("documentVersions") List<Version> documentVersions
    ) {}

    public record AllDocumentVersions(
        @JsonProperty("_embedded") EmbeddedVersions embedded
    ) {}

    public record DocumentEmbedded(
        @JsonProperty("allDocumentVersions") AllDocumentVersions allDocumentVersions
    ) {}

    public record StoredDocument(
        @JsonProperty("_links") Links links,
        @JsonProperty("_embedded") DocumentEmbedded embedded
    ) {}

    public record DocumentsEmbedded(
        @JsonProperty("documents") List<StoredDocument> documents
    ) {}
}

