package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class LreRunStatusReqWeb {

    @JsonProperty("Filters")
    private List<Filter> filters;

    @JsonProperty("Sorting")
    private List<Object> sorting;

    @JsonProperty("PageIndex")
    private int pageIndex;

    @JsonProperty("PageSize")
    private int pageSize;

    @Setter
    @Getter
    public static class Filter {

        @JsonProperty("Field")
        private String field;

        @JsonProperty("Type")
        private String type;

        @JsonProperty("Values")
        private List<Integer> values;

    }

    public static LreRunStatusReqWeb createRunStatusPayloadForRunId(int runId) {
        LreRunStatusReqWeb.Filter filter = new LreRunStatusReqWeb.Filter();
        filter.setField("Id");
        filter.setType("EqualTo");
        filter.setValues(Collections.singletonList(runId));

        LreRunStatusReqWeb request = new LreRunStatusReqWeb();
        request.setFilters(Collections.singletonList(filter));
        request.setSorting(Collections.emptyList());
        request.setPageIndex(-1);
        request.setPageSize(0);

        return request;
    }
}
