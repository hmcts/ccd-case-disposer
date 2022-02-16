package uk.gov.hmcts.reform.ccd.data.model;

import lombok.Value;

import java.util.List;

@Value
public class LinkedEntity<T> {
    T t;
    List<Long> links;
}
