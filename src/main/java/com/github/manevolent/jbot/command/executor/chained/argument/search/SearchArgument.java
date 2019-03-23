package com.github.manevolent.jbot.command.executor.chained.argument.search;

public final class SearchArgument {
    private final SearchOperator operator;
    private final String value;

    SearchArgument(SearchOperator operator, String value) {
        this.operator = operator;
        this.value = value;
    }

    public SearchOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

}
