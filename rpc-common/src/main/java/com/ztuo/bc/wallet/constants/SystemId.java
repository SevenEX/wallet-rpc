/**
 * Copyright (C), 2016-2019, XXX有限公司
 * FileName: SystemId
 * Author:   Administrator
 * Date:     2019/11/25/025 17:36
 * Description: 系统id
 * History:
 * <author>          <time>          <version>          <desc>
 * simon          修改时间           版本号              描述
 */
package com.ztuo.bc.wallet.constants;

import lombok.Getter;
import lombok.Setter;

/**
 * 〈一句话功能简述〉<br>
 * 〈系统id〉
 *
 * @author Administrator
 * @create 2019/11/25/025
 * @since 1.0.0
 */
public enum SystemId {
    /*系统类型*/
    APP("app", 1), APPMGR("app-mgr", 2), MGR("mgr", 3);
    // 成员变量
    private String name;
    private int index;
    
    private SystemId(String name, int index) {
        this.name = name;
        this.index = index;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
}
