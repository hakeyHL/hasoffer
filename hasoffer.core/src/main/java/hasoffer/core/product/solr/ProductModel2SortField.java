package hasoffer.core.product.solr;

public enum ProductModel2SortField {

    F_RELEVANCE(ConstantUtil.API_DATA_EMPTYSTRINGstr_createTime),
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
