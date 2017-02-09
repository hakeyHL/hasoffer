package hasoffer.core.product.solr;

import hasoffer.core.utils.ConstantUtil;

public enum ProductModel2SortField {

    F_RELEVANCE(ConstantUtil.API_DATA_EMPTYSTRING),
    F_POPULARITY("searchCount"),
    F_PRICE("minPrice"),
    F_RATING("rating");

    private String fieldName;

    ProductModel2SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
