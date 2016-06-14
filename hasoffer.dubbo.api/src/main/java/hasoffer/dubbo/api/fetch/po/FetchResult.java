package hasoffer.dubbo.api.fetch.po;

import hasoffer.base.model.TaskStatus;
import hasoffer.fetch.model.ListProduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FetchResult implements Serializable {

    private String taskId;

    private TaskStatus taskStatus;

    private List<ListProduct> listProducts;

    public FetchResult() {
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

    public void addProduct(ListProduct product){
        if(listProducts == null) {
            listProducts = new ArrayList<ListProduct>();
        }
        listProducts.add(product);
    }
}
