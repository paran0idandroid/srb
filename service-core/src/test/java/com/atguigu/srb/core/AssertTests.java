package com.atguigu.srb.core;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author 罗海强
 * @version 1.0
 * @date 2021/4/21 14:43
 */
public class AssertTests {

    @Test
    public void test1(){
        Object o = null;
        if(o == null){
            throw new IllegalArgumentException("参数错误");
        }
    }

    @Test
    public void test2(){
        Object o = null;
        //用断言替代if结构
        Assert.assertNotNull("参数错误", o);
    }
}
