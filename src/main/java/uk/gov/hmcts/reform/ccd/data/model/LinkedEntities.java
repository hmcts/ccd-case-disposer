package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.util.List;

@Value
public class LinkedEntities<T> {
    T data;
    Long familyId;
    List<Long> links;
}
