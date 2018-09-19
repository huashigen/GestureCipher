package com.cqfae.cipher;

/**
 * @Description: 描述每个点的位置等信息
 * @Author: huashigen
 * @CreateDate: 2018/8/17 上午10:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2018/8/17 上午10:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Point {
    //x坐标
    private float x;
    //y坐标
    private float y;
    //是否被选中
    private boolean selected;
    //记录上一个选中的Point的位置,用于画线
    private int lastPostIndex = -1;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getLastPostIndex() {
        return lastPostIndex;
    }

    public void setLastPostIndex(int lastPostIndex) {
        this.lastPostIndex = lastPostIndex;
    }
}
