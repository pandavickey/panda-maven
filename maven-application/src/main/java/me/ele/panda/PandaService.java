package me.ele.panda;

import me.ele.panda.bean.PandaConfig;

/**
 * @author wankun.pwk
 * @date 2020/3/21
 */
public class PandaService {

    /**
     * 出生
     */
    public PandaConfig birth(PandaConfig config) {
        System.out.println("birth");
        return config;
    }

    /**
     * 上学
     */
    public void school(int age) {
        System.out.println("go to school, age : " + age);
    }

    private void love(int age) {
        System.out.println("fall in love age: " + age);
    }

    public void work() {
        System.out.println("work");
    }

    public void wedding() {
        System.out.println("wedding");
    }
}
