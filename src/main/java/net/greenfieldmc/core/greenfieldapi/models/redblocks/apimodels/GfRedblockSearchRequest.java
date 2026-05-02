package net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels;

import java.util.List;

public class GfRedblockSearchRequest {



    public class StatusFilter {

        public List<String> statuses;
        public MatchType matchType;

        public enum MatchType {

            OR("or"),
            NOT("not");

            private final String value;

            MatchType(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

        }

    }

    public class DeletionFilter {

        public List<Long> users;

    }

}
