package com.cqfae.cipher;

/**
 * @Description: 监听手势密码控件状态
 * @Author: huashigen
 * @CreateDate: 2018/8/20 下午2:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2018/8/20 下午2:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public interface TraceListener {
    /**
     * 手指滑动完成,返回手指轨迹的一串代码
     * @param traceCode
     */
    public void onTraceFinished(String traceCode);

    /**
     * 滑动未完成(如未完成制定数量的点数)
     */
    public void onTraceUnFinish();
}
