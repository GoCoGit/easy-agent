package ink.goco.agent.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GResult {

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;
    
    private String ok;	// 不使用

    public static GResult build(Integer status, String msg, Object data) {
        return new GResult(status, msg, data);
    }

    public static GResult build(Integer status, String msg, Object data, String ok) {
        return new GResult(status, msg, data, ok);
    }
    
    public static GResult ok(Object data) {
        return new GResult(data);
    }

    public static GResult ok() {
        return new GResult(null);
    }
    
    public static GResult errorMsg(String msg) {
        return new GResult(500, msg, null);
    }
    
    public static GResult errorException(String msg) {
        return new GResult(555, msg, null);
    }

    public GResult() {

    }

    public GResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    
    public GResult(Integer status, String msg, Object data, String ok) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.ok = ok;
    }

    public GResult(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    public Boolean isOK() {
        return this.status == 200;
    }

}
