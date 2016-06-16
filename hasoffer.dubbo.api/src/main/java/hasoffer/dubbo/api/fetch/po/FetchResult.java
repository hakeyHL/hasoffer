package hasoffer.dubbo.api.fetch.po;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.fetch.model.ListProduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FetchResult implements Serializable {

    private String taskId;

    private TaskStatus taskStatus;

    private String keyword;

    private Website website;

    private int runCount;

    private List<ListProduct> listProducts;

    public FetchResult() {
    }

    public FetchResult(Website website, String keyword) {
        this.keyword = keyword;
        this.website = website;
    }

    public FetchResult(String taskId, TaskStatus taskStatus, List<ListProduct> listProducts) {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.listProducts = listProducts;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<ListProduct> getListProducts() {
        return listProducts;
    }

    public void setListProducts(List<ListProduct> listProducts) {
        this.listProducts = listProducts;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public void addProduct(ListProduct product){
        if(listProducts == null) {
            listProducts = new ArrayList<ListProduct>();
        }
        listProducts.add(product);
    }
}
