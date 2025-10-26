package cn.zhangchuangla.medicine.common.core.base;

import java.util.List;

/**
 * @author Chuang
 */
public class PageResult<T> {

    /**
     * 当前页码
     */
    public Long pageNum;

    /**
     * 每页记录数
     */
    public Long pageSize;

    /**
     * 总记录数
     */
    public Long total;

    /**
     * 列表数据
     */
    public List<T> rows;

    public PageResult() {
    }

    public PageResult(Long pageNum, Long pageSize, Long total, List<T> rows) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.rows = rows;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
