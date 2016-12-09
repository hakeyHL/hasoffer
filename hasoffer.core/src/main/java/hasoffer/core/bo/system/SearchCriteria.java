package hasoffer.core.bo.system;

import hasoffer.base.enums.SearchResultSort;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hs on 2016/6/21.
 */
public class SearchCriteria {
    private int comment;
    private Long minPrice;
    private Long maxPrice;
    private String keyword;
    private String categoryId;
    private int page = 1;
    private int pageSize = 10;
    private int level;
    private int priceFrom = -1;
    private int priceTo = -1;
    private List<String> pivotFields;
    private SearchResultSort sort = SearchResultSort.RELEVANCE;


    //String
    private String[] brand;
    private String[] netWork;
    private String[] screenResolution;
    private String[] opreatingSystem;
    private String[] expandableMemory;

    //int filter
    private String[] ram;
    private String[] batteryCapacity;
    private String[] internalMemory;

    //float
    private String[] primaryCamera;
    private String[] secondaryCamera;
    private String[] screenSize;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public Long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Long minPrice) {
        this.minPrice = minPrice;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getPage() {
        return page <= 1 ? 0 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize <= 1 ? 20 : pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(int priceFrom) {
        this.priceFrom = priceFrom;
    }

    public int getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(int priceTo) {
        this.priceTo = priceTo;
    }

    public SearchResultSort getSort() {
        return sort;
    }

    public void setSort(SearchResultSort sort) {
        this.sort = sort;
    }

    public List<String> getPivotFields() {
        return pivotFields;
    }

    public void setPivotFields(List<String> pivotFields) {
        this.pivotFields = pivotFields;
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "comment=" + comment +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", keyword='" + keyword + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", level=" + level +
                ", priceFrom=" + priceFrom +
                ", priceTo=" + priceTo +
                ", pivotFields=" + pivotFields +
                ", sort=" + sort +
                ", brand=" + Arrays.toString(brand) +
                ", netWork=" + Arrays.toString(netWork) +
                ", screenResolution=" + Arrays.toString(screenResolution) +
                ", opreatingSystem=" + Arrays.toString(opreatingSystem) +
                ", expandableMemory=" + Arrays.toString(expandableMemory) +
                ", ram=" + Arrays.toString(ram) +
                ", batteryCapacity=" + Arrays.toString(batteryCapacity) +
                ", internalMemory=" + Arrays.toString(internalMemory) +
                ", primaryCamera=" + Arrays.toString(primaryCamera) +
                ", secondaryCamera=" + Arrays.toString(secondaryCamera) +
                ", screenSize=" + Arrays.toString(screenSize) +
                '}';
    }

    public String[] getBrand() {
        return brand;
    }

    public void setBrand(String[] brand) {
        this.brand = brand;
    }

    public String[] getNetWork() {
        return netWork;
    }

    public void setNetWork(String[] netWork) {
        this.netWork = netWork;
    }

    public String[] getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String[] screenResolution) {
        this.screenResolution = screenResolution;
    }

    public String[] getOpreatingSystem() {
        return opreatingSystem;
    }

    public void setOpreatingSystem(String[] opreatingSystem) {
        this.opreatingSystem = opreatingSystem;
    }

    public String[] getExpandableMemory() {
        return expandableMemory;
    }

    public void setExpandableMemory(String[] expandableMemory) {
        this.expandableMemory = expandableMemory;
    }

    public String[] getRam() {
        return ram;
    }

    public void setRam(String[] ram) {
        this.ram = ram;
    }

    public String[] getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String[] batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String[] getInternalMemory() {
        return internalMemory;
    }

    public void setInternalMemory(String[] internalMemory) {
        this.internalMemory = internalMemory;
    }

    public String[] getPrimaryCamera() {
        return primaryCamera;
    }

    public void setPrimaryCamera(String[] primaryCamera) {
        this.primaryCamera = primaryCamera;
    }

    public String[] getSecondaryCamera() {
        return secondaryCamera;
    }

    public void setSecondaryCamera(String[] secondaryCamera) {
        this.secondaryCamera = secondaryCamera;
    }

    public String[] getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String[] screenSize) {
        this.screenSize = screenSize;
    }
}
