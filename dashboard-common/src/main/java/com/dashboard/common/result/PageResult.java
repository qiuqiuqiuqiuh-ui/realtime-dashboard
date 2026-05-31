package com.dashboard.common.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;  // 总页数

    public PageResult() {}

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
}
